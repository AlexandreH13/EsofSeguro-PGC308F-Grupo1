#!/usr/bin/env python3
"""
Sessão de verificação da Etapa 5, executada dentro da imagem oficial do ZAP.

Quatro passos: sobe o ZAP em modo daemon, gera o tráfego do roteiro pelo proxy (uso legítimo e os
abusos das etapas 1 e 2), roda o scan passivo e o ativo sobre o que o proxy viu e grava os relatórios
em /zap/relatorios, que o compose monta em evidencias/etapa-5.
"""
import base64
import json
import os
import subprocess
import sys
import time
from collections import Counter
from datetime import datetime, timezone

import requests
from zapv2 import ZAPv2

APP = os.environ.get("APP", "http://transcricao:8080")
PROXY = "http://127.0.0.1:8090"
PROXIES = {"http": PROXY, "https": PROXY}
SAIDA = "/zap/relatorios"
CONTEXTO = "transcricao"
linhas = []


def log(texto):
    print(texto, flush=True)
    linhas.append(texto)


def esperar(condicao, descricao, tentativas=90):
    for _ in range(tentativas):
        try:
            if condicao():
                return
        except Exception:
            pass
        time.sleep(2)
    sys.exit(f"Tempo esgotado: {descricao}")


def chamar(metodo, caminho, esperado, nota, token=None, corpo=None):
    cabecalhos = {"Authorization": f"Bearer {token}"} if token else {}
    r = requests.request(metodo, APP + caminho, headers=cabecalhos, json=corpo, proxies=PROXIES, timeout=30)
    marca = "ok" if r.status_code == esperado else "DIVERGENTE"
    log(f"[{marca}] {metodo} {caminho} -> {r.status_code} (esperado {esperado}). {nota}")
    return r


def token_idp(promotor, segundo_fator):
    r = chamar("POST", "/idp-fake/token", 200,
               f"IdP simulado emite token de {promotor}, segundoFator={str(segundo_fator).lower()}.",
               corpo={"promotorId": promotor, "segundoFator": segundo_fator})
    return r.json()["idToken"]


def sessao(id_token, esperado, nota):
    r = chamar("POST", "/api/sessao", esperado, nota, corpo={"idToken": id_token})
    return r.json().get("tokenSessao") if r.status_code == 200 else None


def adulterar(jwt, claim, valor):
    """Troca um claim do payload e mantém a assinatura original."""
    cabecalho, payload, assinatura = jwt.split(".")
    dados = json.loads(base64.urlsafe_b64decode(payload + "=" * (-len(payload) % 4)))
    dados[claim] = valor
    novo = base64.urlsafe_b64encode(json.dumps(dados, separators=(",", ":")).encode()).decode().rstrip("=")
    return f"{cabecalho}.{novo}.{assinatura}"


def trafego():
    log("== Tráfego do roteiro pelo proxy do ZAP")
    for pagina in ["/", "/index.html", "/estilo.css", "/app.js", "/transcricoes.html",
                   "/transcricao.html", "/sessao-encerrada.html"]:
        chamar("GET", pagina, 200, "Página estática.")
    chamar("GET", "/idp-fake/promotores", 200, "Contas do IdP simulado (tela T1).")

    log("-- Prática 1: sessão só com segundo fator (RS02)")
    sem_mfa = token_idp("promotor-a", False)
    sessao(sem_mfa, 401, "TS01: token válido sem mfa não cria sessão.")
    com_mfa = token_idp("promotor-a", True)
    token_a = sessao(com_mfa, 200, "TS02: token com mfa cria a sessão de Ana.")
    sessao(adulterar(com_mfa, "sub", "promotor-b"), 401, "Payload adulterado com a assinatura original.")
    sessao(adulterar(sem_mfa, "amr", ["pwd", "mfa"]), 401, "Claim amr forjado no token sem mfa.")
    sessao(com_mfa.rsplit(".", 1)[0] + ".assinaturafalsa", 401, "Assinatura inválida.")
    sessao("", 401, "Token vazio.")
    token_b = sessao(token_idp("promotor-b", True), 200, "Sessão de Beatriz.")

    log("-- Rotas de dados sem sessão")
    chamar("GET", "/api/transcricoes", 401, "Sem cabeçalho Authorization.")
    chamar("GET", "/api/transcricoes", 401, "Token de sessão inventado.", token="inventado")
    chamar("GET", "/api/transcricoes", 401, "Token de identidade usado no lugar da sessão.", token=com_mfa)

    log("-- Uso legítimo")
    chamar("GET", "/api/sessao", 200, "Promotor da sessão de Ana.", token=token_a)
    de_ana = chamar("GET", "/api/transcricoes", 200, "Lista de Ana.", token=token_a).json()
    por_status = {t["status"]: t["id"] for t in de_ana}
    if set(por_status) != {"NA_FILA", "EM_PROCESSAMENTO", "FINALIZADA", "FALHA"}:
        sys.exit("A aplicação não está com os dados iniciais (uma sessão anterior excluiu transcrições). "
                 "Recrie com: docker compose up -d --force-recreate transcricao")
    for t in de_ana:
        chamar("GET", f"/api/transcricoes/{t['id']}", 200,
               f"Ana abre a própria transcrição ({t['status']}).", token=token_a)
    de_beatriz = chamar("GET", "/api/transcricoes", 200, "Lista de Beatriz.", token=token_b).json()
    chamar("GET", f"/api/transcricoes/{de_beatriz[0]['id']}", 200, "Beatriz abre a própria transcrição.",
           token=token_b)

    log("-- Prática 2: autorização por propriedade do recurso (RS01)")
    alvo = por_status["FINALIZADA"]
    chamar("GET", f"/api/transcricoes/{alvo}", 403, "TS04: Beatriz tenta abrir transcrição de Ana.",
           token=token_b)
    chamar("DELETE", f"/api/transcricoes/{alvo}", 403, "TS04: Beatriz tenta excluir transcrição de Ana.",
           token=token_b)
    chamar("GET", "/api/transcricoes/00000000-0000-0000-0000-000000000000", 403,
           "TS04: id inexistente recebe a mesma resposta.", token=token_b)
    chamar("GET", "/api/transcricoes/1", 400, "Id fora do formato UUID.", token=token_b)
    chamar("DELETE", f"/api/transcricoes/{por_status['EM_PROCESSAMENTO']}", 409,
           "Exclusão durante o processamento.", token=token_a)
    chamar("DELETE", f"/api/transcricoes/{por_status['FALHA']}", 204,
           "Ana exclui a própria transcrição com falha.", token=token_a)
    chamar("GET", f"/api/transcricoes/{alvo}", 200, "Transcrição de Ana segue intacta.", token=token_a)

    log("-- Encerramento")
    chamar("DELETE", "/api/sessao", 204, "Beatriz sai.", token=token_b)
    chamar("GET", "/api/transcricoes", 401, "Token de Beatriz depois de sair.", token=token_b)
    return token_a


def verificar(zap, token_a):
    log("== Scan passivo e ativo")
    cid = zap.context.new_context(CONTEXTO)
    zap.context.include_in_context(CONTEXTO, f"{APP}.*")
    # O scan ativo não toca em /api/sessao: um DELETE derrubaria o token que o próprio scan usa.
    zap.ascan.exclude_from_scan(f"{APP}/api/sessao")
    zap.replacer.add_rule(description="sessao do scan", enabled=True, matchtype="REQ_HEADER",
                          matchregex=False, matchstring="Authorization", replacement=f"Bearer {token_a}")
    # Vetores de entrada do scan ativo: query (1), corpo (2) e segmento de caminho (16), onde ficam os ids.
    zap.ascan.set_option_target_params_injectable(1 | 2 | 16)

    sid = zap.spider.scan(url=APP + "/", contextname=CONTEXTO)
    esperar(lambda: int(zap.spider.status(sid)) >= 100, "spider terminar")
    log(f"Spider: {len(zap.spider.results(sid))} URLs.")
    esperar(lambda: int(zap.pscan.records_to_scan) == 0, "scan passivo terminar")

    inicio = time.time()
    # A raiz do site, sem a barra final: com ela o ZAP escolhe só o nó "/" e não desce aos demais.
    aid = zap.ascan.scan(url=APP, recurse=True, inscopeonly=True, contextid=cid)
    if not str(aid).isdigit():
        sys.exit(f"Scan ativo não iniciou: {aid}")
    ultimo = -1
    while int(zap.ascan.status(aid)) < 100:
        atual = int(zap.ascan.status(aid)) // 25 * 25
        if atual != ultimo:
            print(f"Scan ativo: {zap.ascan.status(aid)}%", flush=True)
            ultimo = atual
        time.sleep(10)
    esperar(lambda: int(zap.pscan.records_to_scan) == 0, "scan passivo terminar")
    duracao = int(time.time() - inicio)
    mensagens = len(zap.ascan.messages_ids(aid))
    log(f"Scan ativo: {duracao // 60} min {duracao % 60} s, {mensagens} mensagens.")
    return duracao, mensagens


def relatorios(zap, duracao, mensagens_ativo):
    os.makedirs(SAIDA, exist_ok=True)
    titulo = "Verificação do protótipo, Etapa 5"
    for modelo, nome in [("traditional-html", "zap-relatorio.html"), ("traditional-json", "zap-relatorio.json")]:
        zap.reports.generate(title=titulo, template=modelo, reportdir=SAIDA, reportfilename=nome)

    alertas = zap.alert.alerts(baseurl=APP)
    ordem = {"High": 0, "Medium": 1, "Low": 2, "Informational": 3}
    nomes = {"High": "Alto", "Medium": "Médio", "Low": "Baixo", "Informational": "Informativo"}
    grupos = Counter((a["risk"], a["name"]) for a in alertas)
    exemplo = {}
    for a in alertas:
        exemplo.setdefault((a["risk"], a["name"]), (a["url"], a.get("param", ""), a["pluginId"]))
    with open(os.path.join(SAIDA, "alertas.md"), "w", encoding="utf-8") as f:
        f.write(f"# Alertas do ZAP\n\nGerado em {datetime.now(timezone.utc).isoformat(timespec='seconds')}. Alvo {APP}.\n\n")
        f.write("| Risco | Alerta | Regra | Ocorrências | Exemplo de URL | Parâmetro |\n| --- | --- | --- | --- | --- | --- |\n")
        for (risco, nome), n in sorted(grupos.items(), key=lambda kv: (ordem[kv[0][0]], kv[0][1])):
            url, param, plugin = exemplo[(risco, nome)]
            f.write(f"| {nomes[risco]} | {nome} | {plugin} | {n} | `{url.replace(APP, '')}` | {param} |\n")

    with open(os.path.join(SAIDA, "trafego.log"), "w", encoding="utf-8") as f:
        f.write("\n".join(linhas) + "\n")

    resumo = zap.core.alerts_summary(baseurl=APP)
    with open(os.path.join(SAIDA, "sessao.txt"), "w", encoding="utf-8") as f:
        f.write(f"Data: {datetime.now(timezone.utc).isoformat(timespec='seconds')}\n")
        f.write(f"ZAP: {zap.core.version}\n")
        f.write(f"Alvo: {APP} (serviço transcricao do compose)\n")
        f.write(f"Contexto: {CONTEXTO}, inclui {APP}.*; scan ativo exclui {APP}/api/sessao\n")
        f.write("Autenticação: cabeçalho Authorization: Bearer com a sessão de Ana, injetado pela regra replacer\n")
        f.write("Política do scan ativo: padrão do ZAP; vetores de entrada query, corpo e caminho\n")
        urls = sorted(u.replace(APP, "") for u in zap.core.urls(baseurl=APP))
        f.write(f"URLs no contexto ({len(urls)}): {', '.join(urls)}\n")
        f.write(f"Mensagens no histórico: {zap.core.number_of_messages(baseurl=APP)}\n")
        f.write(f"Mensagens do scan ativo: {mensagens_ativo}\n")
        f.write(f"Duração do scan ativo: {duracao} s\n")
        f.write(f"Alertas por risco: {json.dumps(resumo)}\n")
    log(f"Alertas por risco: {json.dumps(resumo)}")
    log(f"Relatórios em {SAIDA}: zap-relatorio.html, zap-relatorio.json, alertas.md, trafego.log, sessao.txt")


def main():
    zap_proc = subprocess.Popen(
        ["zap.sh", "-daemon", "-host", "127.0.0.1", "-port", "8090",
         "-config", "api.disablekey=true", "-config", "view.locale=pt_BR"],
        stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    zap = ZAPv2(proxies=PROXIES)
    esperar(lambda: zap.core.version, "ZAP subir")
    log(f"ZAP {zap.core.version} no ar.")
    esperar(lambda: requests.get(APP + "/idp-fake/promotores", proxies=PROXIES, timeout=5).status_code == 200,
            "aplicação responder")
    token_a = trafego()
    divergentes = [l for l in linhas if l.startswith("[DIVERGENTE]")]
    if divergentes:
        sys.exit(f"Tráfego divergiu do esperado em {len(divergentes)} chamadas; veja acima.")
    duracao, mensagens_ativo = verificar(zap, token_a)
    relatorios(zap, duracao, mensagens_ativo)
    zap.core.shutdown()
    zap_proc.wait(timeout=60)


if __name__ == "__main__":
    main()

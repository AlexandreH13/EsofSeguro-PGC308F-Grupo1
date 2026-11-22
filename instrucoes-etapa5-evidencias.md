# Etapa 5, sessão de verificação e evidências

Instruções para rodar a sessão de verificação do ZAP contra o protótipo e registrar as evidências, em dois commits: os arquivos gerados pela sessão e a seção 4 do documento. É a parte da Etapa 5 que produz os números que as seções 5 e 6, de outros colegas, vão citar.

## Antes de começar

A `main` precisa ter a Etapa 4 completa e o PR que abriu a Etapa 5, com o serviço `zap` no compose e as seções 1 a 3 de [docs/etapa5-verificacao-vulnerabilidades.md](docs/etapa5-verificacao-vulnerabilidades.md). Leia essas três seções e a seção "Verificação de vulnerabilidades" de [prototipo/README.md](prototipo/README.md). O script que roda a sessão é [prototipo/zap/sessao.py](prototipo/zap/sessao.py); vale ler o comentário do topo e o de cada função.

Precisa só de Docker. Na primeira execução o compose baixa a imagem do ZAP, uns 1,2 GB. Crie a branch `alexandre-etapa5-evidencias` a partir da `main` atualizada.

## Commit 1: a sessão e os arquivos gerados

Na pasta `prototipo`, com a porta 8080 livre (ou `PORTA=8081` na frente dos dois comandos):

```
docker compose up -d --force-recreate transcricao
docker compose run --rm zap
```

O primeiro comando sobe a aplicação com os dados iniciais. O roteiro exclui uma transcrição, então sem isso o script para com a mensagem "A aplicação não está com os dados iniciais". O segundo roda a sessão inteira em cerca de um minuto. O scan ativo em si dura uns dez segundos, porque a aplicação é pequena e responde rápido; a linha de progresso costuma mostrar só `Scan ativo: 0%` e logo o total. A saída esperada: uma linha `[ok]` por chamada do roteiro, nenhuma `[DIVERGENTE]`, depois `Spider: N URLs.` e, no fim:

```
Alertas por risco: {"High": 0, "Low": ..., "Medium": ..., "Informational": ...}
Relatórios em /zap/relatorios: zap-relatorio.html, zap-relatorio.json, alertas.md, trafego.log, sessao.txt
```

`High` tem que ser 0, e `evidencias/etapa-5/alertas.md` tem que listar estes cinco alertas, com estes números de regra: Content Security Policy (CSP) Header Not Set (10038), Missing Anti-clickjacking Header (10020), X-Content-Type-Options Header Missing (10021), Modern Web Application (10109) e Session Management Response Identified (10112). As ocorrências podem variar em uma ou duas unidades de uma máquina para outra; os alertas não. No relatório HTML os três primeiros aparecem como "Systemic" em vez de contagem; a contagem está só no `alertas.md`. Se aparecer alerta diferente, ou uma chamada divergente, pare e avise no grupo antes de commitar.

Acrescente duas capturas de tela do relatório na mesma pasta: abra `evidencias/etapa-5/zap-relatorio.html` no navegador, capture o topo, com "Summary of Alerts" e "Insights", como `relatorio-zap-resumo.png`, e a tabela "Alertas" com o início do "Alert Detail" como `relatorio-zap-alertas.png`.

Antes de derrubar a aplicação, rode os dois comandos `curl -sI` descritos no commit 2 e guarde a saída, porque o documento cita os cabeçalhos da resposta. Depois `docker compose down`. O commit leva os sete arquivos de `evidencias/etapa-5`. Mensagem:

```
Registra as evidências da sessão de verificação com o ZAP
```

## Commit 2: a seção 4 do documento

Em `docs/etapa5-verificacao-vulnerabilidades.md`, substitua o `Pendente.` da seção 4 pelo texto abaixo, trocando cada marcador «assim» pelo valor da sua sessão:

| Marcador | De onde tirar |
| --- | --- |
| «N_CHAMADAS» | Número de linhas `[ok]` em `trafego.log` |
| «DATA» | Data local do dia em que você rodou, no formato dd/mm/aaaa (o `sessao.txt` grava em UTC, que pode cair no dia seguinte) |
| «N_URLS» | "URLs no contexto" em `sessao.txt` |
| «N_MENSAGENS» | "Mensagens no histórico" em `sessao.txt`, com ponto de milhar |
| «N_ATIVO» | "Mensagens do scan ativo" em `sessao.txt`, com ponto de milhar |
| «DURACAO» | "Duração do scan ativo" em `sessao.txt`, só o número |
| «OC_10038», «OC_10020», «OC_10021», «OC_10109», «OC_10112» | Coluna "Ocorrências" de `alertas.md`, regra por regra |
| «TRECHO_TRAFEGO» | Cinco linhas copiadas do seu `trafego.log`, nesta ordem: as que contêm `TS01:`, `TS02:`, `Payload adulterado`, `TS04: Beatriz tenta abrir` e `TS04: id inexistente` |
| «BLOCO_CURL» | Os dois comandos abaixo e a saída de cada um na sua máquina, com a porta que você usou, sem a linha `Date` e sem o espaço que o curl imprime depois do `200` |

Formato do «BLOCO_CURL», com a sua porta:

```
$ curl -sI http://localhost:8080/index.html
HTTP/1.1 200
Last-Modified: ...
Accept-Ranges: bytes
Content-Type: text/html
Content-Length: ...

$ curl -sI http://localhost:8080/idp-fake/promotores
HTTP/1.1 200
Content-Type: application/json
Content-Length: ...
```

Texto da seção 4:

````markdown
Os arquivos estão em [evidencias/etapa-5](../evidencias/etapa-5):

| Arquivo | Conteúdo |
| --- | --- |
| `zap-relatorio.html` | Relatório completo do ZAP, com descrição, instâncias e solução de cada alerta |
| `zap-relatorio.json` | O mesmo relatório em JSON |
| `alertas.md` | Resumo dos alertas por risco, com regra, ocorrências e exemplo de URL |
| `trafego.log` | As «N_CHAMADAS» chamadas do roteiro, com o status obtido e o esperado em cada uma |
| `sessao.txt` | Data, versão do ZAP, contexto, URLs cobertas, mensagens e duração |
| `relatorio-zap-resumo.png` e `relatorio-zap-alertas.png` | Capturas do relatório HTML |

Números da sessão de «DATA»: «N_URLS» URLs no contexto, «N_MENSAGENS» mensagens no histórico, das quais «N_ATIVO» do scan ativo, que durou «DURACAO» segundos. Nenhum alerta alto. Dois médios, um baixo e dois informativos:

| Risco | Alerta | Regra | Ocorrências |
| --- | --- | --- | --- |
| Médio | Content Security Policy (CSP) Header Not Set | 10038 | «OC_10038» |
| Médio | Missing Anti-clickjacking Header | 10020 | «OC_10020» |
| Baixo | X-Content-Type-Options Header Missing | 10021 | «OC_10021» |
| Informativo | Modern Web Application | 10109 | «OC_10109» |
| Informativo | Session Management Response Identified | 10112 | «OC_10112» |

![Resumo do relatório do ZAP](../evidencias/etapa-5/relatorio-zap-resumo.png)

O tráfego do roteiro terminou com todas as chamadas no status esperado. Trecho do `trafego.log`:

```
«TRECHO_TRAFEGO»
```

A ausência dos cabeçalhos se confirma fora do ZAP, na resposta da aplicação (porta publicada nesta máquina):

```
«BLOCO_CURL»
```
````

Em `TODO.md`, troque a linha da Etapa 5 por:

```markdown
- Etapa 5 em andamento em [docs/etapa5-verificacao-vulnerabilidades.md](docs/etapa5-verificacao-vulnerabilidades.md): serviço do ZAP no compose do protótipo, seções 1 a 3 e a sessão com as evidências (seção 4) prontos. Faltam a análise dos achados (seção 5) e os descartes e limites (seções 6 e 7). Etapas 6 e 7 e o artigo final não começaram.
```

Na seção "Pendências da Etapa 5", remova a linha da sessão e das evidências e deixe as outras duas.

Mensagem do commit:

```
Descreve a evidência da execução da verificação na Etapa 5
```

## Pull request

Abra o PR para a `main` com os dois commits. No texto, cite os números da sessão (chamadas, URLs, mensagens, alertas por risco) e aponte a pasta `evidencias/etapa-5`. Quem revisar deve abrir o `zap-relatorio.html` e conferir os cinco alertas, e conferir que o `trafego.log` não tem linha `[DIVERGENTE]`.

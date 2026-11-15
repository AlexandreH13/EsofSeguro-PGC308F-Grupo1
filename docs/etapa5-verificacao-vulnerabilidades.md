## Verificação de Vulnerabilidades

### Descrição

Este documento registra uma sessão de verificação dinâmica do protótipo da [Etapa 4](etapa4-codigo-seguro.md) com o ZAP, a ferramenta sugerida no enunciado. O alvo é o próprio sistema, não o Juice Shop: o protótipo implementa as duas práticas de código seguro e a interface, o que permite cruzar os alertas com os riscos da [análise de risco](etapa2-analise-de-risco.md) e os requisitos da [arquitetura segura](etapa3-arquitetura-segura.md). As seções seguem os seis itens do entregável mínimo: sistema testado, ferramenta, configuração do teste, evidência da execução, análise de três achados e correção proposta. A seção 6 explica os alertas descartados e o que o scanner não alcança.

---

### 1. Sistema testado

Protótipo do serviço de transcrição, na versão completa da Etapa 4: Backend Spring Boot com o filtro de sessão, as rotas de transcrição com autorização por propriedade (prática 2), a criação de sessão só com segundo fator (prática 1), o provedor de identidade simulado em `/idp-fake` e as páginas estáticas das telas T1, T3, T4 e T6. O chat temporário (T5) e o download em DOCX, acrescentados ao protótipo depois das práticas, ficam fora desta sessão. Dados iniciais de dois promotores e cinco transcrições. Tudo roda no Docker Compose do repositório, e o ZAP alcança a aplicação pelo nome interno `http://transcricao:8080`, sem sair da rede do compose. É um ambiente próprio, sem terceiros envolvidos.

---

### 2. Ferramenta

ZAP 2.17.0, pela imagem oficial `ghcr.io/zaproxy/zaproxy:stable`, como serviço `zap` do [compose do protótipo](../prototipo/compose.yaml), no perfil `verificacao`. O script [prototipo/zap/sessao.py](../prototipo/zap/sessao.py) controla o ZAP pela API e executa a sessão inteira com um comando, descrito no [README do protótipo](../prototipo/README.md).

---

### 3. Configuração básica do teste

| Item | Configuração |
| --- | --- |
| Modo | ZAP em daemon, proxy em `127.0.0.1:8090` dentro do contêiner, API sem chave e alcançável só de dentro |
| Contexto | `transcricao`, incluindo `http://transcricao:8080.*` |
| Autenticação | Regra `replacer` que injeta `Authorization: Bearer <token>` em toda requisição do spider e do scan ativo, com a sessão da promotora Ana criada pelo fluxo normal de login |
| Exclusão | `/api/sessao` fora do scan ativo, porque um `DELETE` encerraria a sessão que o próprio scan usa |
| Vetores de entrada | Query, corpo (incluindo JSON) e segmento de caminho, onde ficam os ids das transcrições |
| Política | Política padrão do ZAP, spider padrão, scan passivo em todo o histórico |
| Relatórios | `traditional-html` e `traditional-json` do ZAP, mais `alertas.md`, `trafego.log` e `sessao.txt` gerados pelo script |

A sessão tem quatro passos. Primeiro o script repete pelo proxy o fluxo da demonstração (login, lista, abrir, excluir), com o uso legítimo e os abusos das etapas 1 e 2: login sem segundo fator (TS01), token com o payload adulterado e a assinatura original, claim `amr` forjado, assinatura inválida, rotas de dados sem sessão, promotora abrindo e excluindo transcrição de colega e id inexistente (TS04), id fora do formato e exclusão durante o processamento. Depois rodam o spider, o scan passivo e o scan ativo com a sessão de Ana. Por fim o script grava os relatórios. O comando, na pasta `prototipo`:

```
docker compose up -d --force-recreate transcricao
docker compose run --rm zap
```

---

### 4. Evidência da execução

Pendente.

---

### 5. Análise dos achados

Pendente.

---

### 6. Alertas descartados e limites da verificação

Pendente.

---

### 7. Referências

Pendente.

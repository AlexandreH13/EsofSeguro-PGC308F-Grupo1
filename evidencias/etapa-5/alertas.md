# Alertas do ZAP

Gerado em 2026-11-15T18:51:45+00:00. Alvo http://transcricao:8080.

| Risco | Alerta | Regra | Ocorrências | Exemplo de URL | Parâmetro |
| --- | --- | --- | --- | --- | --- |
| Médio | Content Security Policy (CSP) Header Not Set | 10038 | 6 | `/index.html` |  |
| Médio | Missing Anti-clickjacking Header | 10020 | 6 | `/` | x-frame-options |
| Baixo | X-Content-Type-Options Header Missing | 10021 | 18 | `/idp-fake/promotores` | x-content-type-options |
| Informativo | Modern Web Application | 10109 | 5 | `/transcricao.html` |  |
| Informativo | Session Management Response Identified | 10112 | 2 | `/idp-fake/token` | idToken |

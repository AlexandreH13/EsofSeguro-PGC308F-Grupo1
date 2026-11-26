# TODO

## Estado em 26/11

- Etapa 1 (modelagem de ameaças) entregue: 16 ameaças STRIDE e 7 casos de abuso em [docs/etapa1-modelagem-ameacas.md](docs/etapa1-modelagem-ameacas.md). Falta só a síntese final pedida no item 8.7 do enunciado (ameaças mais preocupantes, ativos mais importantes, abusos de maior impacto e dificuldades do grupo).
- Etapa 2 entregue: análise com registro RI01 a RI16, justificativas e priorização em [docs/etapa2-analise-de-risco.md](docs/etapa2-analise-de-risco.md); plano de tratamento, residual, ordem de implementação e conclusão em [docs/etapa2-tratamento-de-risco-nist.md](docs/etapa2-tratamento-de-risco-nist.md). As pendências de 27/09 foram fechadas no PR #17.
- Etapa 3 entregue em [docs/etapa3-arquitetura-segura.md](docs/etapa3-arquitetura-segura.md): requisitos de segurança (seção 1, RS01 a RS03, derivados de RI01, RI13 e RI04), vulnerabilidades catalogadas (seção 2, V01 a V03), diagrama da arquitetura segura (seção 3) e decisões de arquitetura (seção 4).
- Etapa 4 entregue em [docs/etapa4-codigo-seguro.md](docs/etapa4-codigo-seguro.md) e [prototipo](prototipo): escopo, fase 0, prática 1 (sessão com segundo fator, TS01 a TS03), prática 2 (autorização por propriedade, TS04 a TS06) e interface, mais chat temporário e download em DOCX como extras.
- Etapa 5 entregue em [docs/etapa5-verificacao-vulnerabilidades.md](docs/etapa5-verificacao-vulnerabilidades.md): sessão do ZAP no compose, evidências em [evidencias/etapa-5](evidencias/etapa-5), três achados de cabeçalho analisados com correção proposta.
- Etapa 6 entregue em [docs/etapa6-deteccao-de-intrusoes.md](docs/etapa6-deteccao-de-intrusoes.md)
- Etapa 7 entregue em [docs/etapa7-pipeline-devsecops.md](docs/etapa7-pipeline-devsecops.md): diagrama, os oito momentos do pipeline, ferramentas de análise de código, dependências e imagem, e cinco condições de parada (P1 a P5).


Referências de formato: [exemplo do professor](https://github.com/sequincozes/exemplo-atividade-es-seguro) e [VitaLink](https://github.com/camillabdt/VitaLink).

## Pendências da Etapa 6

- Três regras de detecção (seção 4), uma linha por regra na tabela do enunciado: risco observado, fonte de dados, condição de alerta e resposta inicial. Cada regra deve usar eventos da seção 3 e citar o risco e o caso de abuso. Sugestões: acessos negados por propriedade do mesmo promotor acima de um limite em poucos minutos (E5, RI01, CAE1); recusas de token de identidade ou requisições sem sessão em sequência da mesma origem (E2, RI03 e RI13, CAE3); sessão criada de país ou provedor de acesso nunca usado pelo promotor, ou downloads de um promotor fora do padrão no dia (E1 ou E6, RI13 e RI07, CAE7). O tráfego da Etapa 5 serve de exemplo do aspecto de uma campanha nos registros.
- O que acontece depois do alerta (seção 5): quem recebe (a operação do serviço, papel definido no plano da Etapa 2), como confirma com o promotor, contenção (revogar as sessões dele, bloquear a origem, limitar novas tentativas), registro do incidente e revisão da regra quando o alerta é falso positivo. O plano da Etapa 2 já descreve a resposta de RI13 (confirmar com o promotor e revogar as sessões); reaproveitar.
- Referências das seções 4 e 5 na seção 6 e a linha da Etapa 6 como entregue neste TODO.

## Etapa 7

- Pipeline DevSecOps em `docs/etapa7-devsecops-e-video-final.md`, na tabela sugerida pelo enunciado (momento, atividade de segurança, evidência produzida, condição para continuar), cobrindo os oito momentos do item 31. Preencher cada linha com o que o grupo já produziu: STRIDE e casos de abuso (Etapa 1), registro e plano de tratamento (Etapa 2), requisitos RS01 a RS03 e decisões (Etapa 3), testes TS01 a TS06 rodando no serviço `testes` do compose (Etapa 4), sessão do ZAP no serviço `zap` (Etapa 5) e eventos e regras (Etapa 6). Para análise de código e dependências, que nenhuma etapa cobriu, sugerir ferramentas e onde entrariam no `mvn`, por exemplo OWASP Dependency-Check, SpotBugs e gitleaks.
- Pelo menos três condições que param o pipeline, ligadas ao projeto: teste TS01 a TS06 reprovado; alerta Médio ou acima do ZAP sem análise e decisão registrada (os dois achados Médio da Etapa 5, CSP e anti-clickjacking, só passam depois que a seção 5 os analisar e propuser a correção); segredo no repositório; dependência com vulnerabilidade conhecida; alerta `ACESSO_NEGADO` em teste que esperava sucesso.
- Roteiro da apresentação presencial, de 10 a 15 minutos, com os nove itens do item 32, versionado em `docs/`. Dividir a fala pelos quatro integrantes conforme a etapa em que cada um mais contribuiu. O vídeo é opcional e não substitui a apresentação.

## Pendências gerais

- Implementar o filtro de cabeçalhos proposto na Etapa 5 (CSP, X-Frame-Options, nosniff), tirar os scripts e estilos embutidos das telas e repetir a sessão do ZAP como prova.
- Atualizar o caso de uso [RQF1](docs/requisitos%20funcionais/RQF1-acesso-com-credenciais.md): o passo "Valida assinatura, emissor e validade do token" passa a conferir também audiência e papel de promotor (RI03) e o claim de segundo fator (RS02). Pelo mesmo motivo, anotar o TLS mútuo do endpoint de status no [RQF4](docs/requisitos%20funcionais/RQF4-acompanhamento-do-status-da-transcricao.md) (RS03).
- Escrever as considerações finais da Etapa 1 (item 8.7 do enunciado).
- Artigo no template SBC ou IEEE relatando todas as etapas (checklist final do enunciado).

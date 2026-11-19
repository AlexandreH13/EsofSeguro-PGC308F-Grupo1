# TODO

## Estado em 01/11

- Etapa 1 (modelagem de ameaças) entregue: 16 ameaças STRIDE e 7 casos de abuso em [docs/etapa1-modelagem-ameacas.md](docs/etapa1-modelagem-ameacas.md). Falta só a síntese final pedida no item 8.7 do enunciado (ameaças mais preocupantes, ativos mais importantes, abusos de maior impacto e dificuldades do grupo).
- Etapa 2 entregue: análise com registro RI01 a RI16, justificativas e priorização em [docs/etapa2-analise-de-risco.md](docs/etapa2-analise-de-risco.md); plano de tratamento, residual, ordem de implementação e conclusão em [docs/etapa2-tratamento-de-risco-nist.md](docs/etapa2-tratamento-de-risco-nist.md). As pendências de 27/09 foram fechadas no PR #17.
- Etapa 3 em andamento em [docs/etapa3-arquitetura-segura.md](docs/etapa3-arquitetura-segura.md): requisitos de segurança (seção 1, RS01 a RS03, derivados de RI01, RI13 e RI04) e vulnerabilidades catalogadas (seção 2, V01 a V03) prontos. Faltam o diagrama (seção 3) e as decisões de arquitetura (seção 4).
- Etapa 4 entregue em [docs/etapa4-codigo-seguro.md](docs/etapa4-codigo-seguro.md) e [prototipo](prototipo): escopo, fase 0, prática 1 (sessão com segundo fator, TS01 a TS03), prática 2 (autorização por propriedade, TS04 a TS06) e interface, mais chat temporário e download em DOCX como extras.
- Etapa 5 em andamento em [docs/etapa5-verificacao-vulnerabilidades.md](docs/etapa5-verificacao-vulnerabilidades.md): serviço do ZAP no compose do protótipo, seções 1 a 3 e a sessão com as evidências (seção 4) prontos. Faltam a análise dos achados (seção 5) e os descartes e limites (seções 6 e 7). Etapas 6 e 7 e o artigo final não começaram.

Referências de formato: [exemplo do professor](https://github.com/sequincozes/exemplo-atividade-es-seguro) e [VitaLink](https://github.com/camillabdt/VitaLink).

## Pendências da Etapa 3

- 18.3: Gabriel e Alexandre dividem diagramas, usando como referência [requisitos funcionais](docs/requisitos%20funcionais).
- 18.4: Carlos. Três decisões, uma por requisito, derivadas de RS01 a RS03 e dos controles do plano de tratamento. O enunciado (item 18.4) exige em cada uma problema ou risco tratado, decisão, motivo, componente afetado e resultado esperado; a tabela atual tem só decisão, rastreabilidade e critério de aceitação, então acrescentar as colunas que faltam.

## Pendências da Etapa 5

- Análise dos três achados e correção proposta (seção 5).
- Alertas descartados, limites e referências (seções 6 e 7).

## Pendências gerais

- Atualizar o caso de uso [RQF1](docs/requisitos%20funcionais/RQF1-acesso-com-credenciais.md) quando as decisões da Etapa 3 fecharem: o passo "Valida assinatura, emissor e validade do token" passa a conferir também audiência e papel de promotor (RI03) e o claim de segundo fator (RS02). Pelo mesmo motivo, anotar o TLS mútuo do endpoint de status no [RQF4](docs/requisitos%20funcionais/RQF4-acompanhamento-do-status-da-transcricao.md) (RS03).
- Escrever as considerações finais da Etapa 1 (item 8.7 do enunciado).
- Artigo no template SBC ou IEEE relatando todas as etapas (checklist final do enunciado).

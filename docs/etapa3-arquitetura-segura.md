## Projeto da Arquitetura Segura

### Descrição

Este documento transforma os riscos prioritários da [análise de risco](etapa2-analise-de-risco.md) e os controles do [plano de tratamento](etapa2-tratamento-de-risco-nist.md) em requisitos de segurança e decisões de arquitetura. Ele responde como o serviço de transcrição seria organizado para reduzir esses riscos, em quatro partes: os requisitos de segurança derivados de três riscos altos ou muito altos, uma vulnerabilidade catalogada para cada requisito, o diagrama da arquitetura segura com a posição dos controles e as decisões de arquitetura que cumprem os requisitos.

---

### 1. Requisitos de segurança

Os riscos vêm da priorização da análise: RI01 e RI07 são MUITO ALTO (16), RI02, RI04 e RI13 são ALTO (12). RI07 fica de fora porque o plano o aceita. Reduzi-lo exigiria retirar o download e a exibição da transcrição. RI02 e RI03 ficam cobertos pelas mesmas decisões de RS01 (acesso decidido só pela sessão) e RS02 (validação do token no RQF1). Restam RI01, RI13 e RI04, um controle em cada ponto: autorização no Backend, autenticação do promotor e autenticação entre serviços. Cada requisito deriva do controle do plano de tratamento.

| ID | Risco de origem | Requisito de segurança | Critério de verificação |
| --- | --- | --- | --- |
| RS01 | RI01 (AME1) | O Backend deverá verificar, em toda rota que recebe um id de transcrição ou de chat (RQF4, RQF6 a RQF10), que o recurso pertence ao promotor da sessão antes de executar a operação, e deverá gerar esses ids como UUID v4, nunca sequenciais. | Com as sessões de dois promotores, A e B, cada uma dessas rotas chamada por A com um id de B responde acesso negado, não devolve nenhum dado do recurso, não altera nem exclui nada e gera um alerta. Ids criados em sequência são UUID versão 4 e não têm relação entre si. |
| RS02 | RI13 (AME13) | O Backend deverá criar a sessão (RQF1) somente a partir de token de identidade que comprove autenticação com segundo fator, pelo claim `amr` contendo `mfa` (RFC 8176) ou pelo valor de `acr` combinado com o provedor de identidade, e deverá recusar com erro de autenticação qualquer outro token, mesmo com assinatura, emissor, audiência e prazo válidos. | Um token assinado pelo provedor, com emissor, audiência e prazo válidos, mas sem a indicação de segundo fator, não cria sessão, recebe erro de autenticação e não abre nenhuma rota de dados. O mesmo token com `amr` contendo `mfa` cria a sessão. |
| RS03 | RI04 (AME4) | O endpoint de atualização de status (RQF4) deverá aceitar somente chamadas com TLS mútuo que apresentem o certificado de cliente exclusivo do Módulo I.A., emitido pela autoridade certificadora interna do serviço, e deverá receber apenas o id, o status e o hash do texto ou o motivo da falha. O caminho no Storage é derivado do id pelo próprio Backend, e nenhuma mudança é aceita depois do status finalizada, salvo o mesmo status repetido na reentrega da RQNF5.1. | Chamadas sem certificado, com certificado de outro serviço ou com certificado revogado são recusadas antes de qualquer leitura do registro; chamadas com campo de caminho ou que tentam mudar uma transcrição finalizada são recusadas sem alterar o registro. Cada recusa gera um alerta. A chamada com o certificado do Módulo I.A. e apenas os campos previstos atualiza o status. |

---

### 2. Vulnerabilidades catalogadas

Pendente.

---

### 3. Diagrama da arquitetura segura

Pendente.

---

### 4. Decisões de arquitetura

| ID | Decisão | Rastreabilidade | Critério de aceitação |
| --- | --- | --- | --- |
| DA01 | Qual a decisão arquitetural para cumprir o requisito de segurança associado? (Devemos responder essa pergunta aqui) | RS01, V01, AME1 | Qual teste deve ser feito |

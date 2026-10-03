## Estratégias para tratamento dos riscos

### Descrição

Este documento define o tratamento dos riscos levantados na [análise de risco](etapa2-analise-de-risco.md), com base no framework NIST CSF 2.0. Para cada risco, ele responde o que fazer, quem faz e o que comprova que foi feito.

---

### As funções do NIST

A tabela abaixo resume as funções do NIST CSF 2.0 e as categorias citadas no plano.

| Função   | Descrição |
|----------|-----------|
| Govern   | Define política, papéis e responsabilidades. Categoria usada: GV.PO (política). |
| Identify | Identifica ativos, vulnerabilidades e riscos e escolhe a resposta a cada risco. Categoria usada: ID.RA (avaliação de risco), incluindo ID.RA-06, a escolha e o registro da resposta. |
| Protect  | Implanta salvaguardas que reduzem a probabilidade, o impacto ou ambos. Categorias usadas: PR.AA (identidade, autenticação e controle de acesso), PR.DS (segurança dos dados), PR.PS (segurança da plataforma) e PR.IR (resiliência da infraestrutura). |
| Detect   | Identifica eventos adversos. Categorias usadas: DE.CM (monitoramento contínuo) e DE.AE (análise de eventos adversos). |
| Respond  | Contém, analisa e trata um incidente detectado. Categoria usada: RS.MI (mitigação). |
| Recover  | Restaura dados e serviços afetados por um incidente, malicioso ou não. O plano não usa esta função, porque a restauração depende do backup diário da RQNF5.7, já exigido. |

---

### Estratégias de tratamento

Estratégias que podem ser adotadas para cada risco:

| Estratégia   | Descrição |
|--------------|-----------|
| Evitar       | Eliminar a funcionalidade ou a condição que permite o risco. |
| Reduzir      | Implantar mecanismos que diminuem a probabilidade ou o impacto do risco. |
| Compartilhar | Dividir com terceiros a responsabilidade pelo tratamento do risco. |
| Aceitar      | Manter o risco sem tratamento adicional, por decisão registrada do dono do risco, e continuar a monitorá-lo. |

---

### Plano de tratamento dos riscos

Os limites numéricos são valores iniciais. O servico.md não descreve equipes; o plano supõe desenvolvimento do Backend e do Módulo I.A., infraestrutura e operação do serviço, que recebe os alertas, além da gestão do MP. Provedor de identidade e Sistema Processos ficam fora do escopo do serviço, e a linha diz quando um controle depende deles.

Os controles alteram os casos de uso RQF1 (RI03, RI13), RQF3 (RI01, RI04, RI05, RI09), RQF4 (RI04, RI06, RI11), RQF7 e RQF10 (RI12), RQF8 e RQF9 (RI08, RI11) e RQF11 (RI08), e criam no Backend a revogação das sessões de um promotor (RI13). Nenhum desses mecanismos consta ainda de requisitos.md.

| Risco | Estratégia | Controle | Função NIST CSF 2.0 | Responsáveis | Evidência necessária | Risco residual |
|-------|------------|----------|--------------------|--------------|----------------------|----------------|
| RI01 | Reduzir | Fazer a conferência de propriedade, que os casos de uso RQF4 e RQF6 a RQF9 já preveem, numa verificação única do Backend para toda rota com id de transcrição ou de chat (RQF10), e gerar ids aleatórios (UUID v4). Acesso negado por propriedade gera alerta. | Protect (PR.AA), Detect (DE.AE) | Desenvolvimento do Backend | Suíte automatizada, executada a cada entrega, com um teste por rota em que o promotor A usa um id de B, recebe acesso negado e dispara o alerta. | BAIXO (4) |
| RI02 | Reduzir | Montar a identidade repassada ao Sistema Processos (RQF2 e RQF3) só a partir da sessão, inclusive quando a requisição não traz nenhuma; requisição com campo de identidade é recusada e gera alerta. Depende da premissa 5 dos casos de uso. | Protect (PR.AA), Detect (DE.AE) | Desenvolvimento do Backend | Testes em que a requisição com identidade de outro promotor é recusada com alerta e em que toda chamada ao Sistema Processos leva a identidade da sessão. | BAIXO (4) |
| RI03 | Reduzir | Aceitar só o token que o Backend obtém na troca do código (RQF1), com audiência (`audience`) deste serviço, e exigir nele o papel de promotor, como pede a RQF1.2. Premissa: o provedor de identidade emite esse papel. | Protect (PR.AA) | Desenvolvimento do Backend; equipe do provedor de identidade | Configuração do provedor que atribui o papel de promotor; testes em que token de outro sistema do MP e token sem o papel são recusados. | BAIXO (3) |
| RI04 | Reduzir | Exigir no endpoint de status TLS mútuo com certificado exclusivo do Módulo I.A., recusando com alerta as demais chamadas. O endpoint deixa de receber caminho, que Backend e Módulo I.A. derivam do id, e recusa mudança após finalizada, salvo o mesmo status repetido na reentrega da RQNF5.1. | Protect (PR.AA), Detect (DE.AE) | Desenvolvimento do Backend e do Módulo I.A.; infraestrutura | Testes em que chamadas sem certificado, com certificado de outro serviço ou que mudam transcrição finalizada são recusadas com alerta. | BAIXO (4) |
| RI05, RI16 | Reduzir | Separar as credenciais da fila (o Backend só publica, o Módulo I.A. só consome). O Backend assina cada mensagem com chave mantida em cofre de chaves da nuvem, premissa não descrita no servico.md, e o Módulo I.A. recusa com alerta mensagem sem assinatura válida. | Protect (PR.AA, PR.DS), Detect (DE.AE) | Desenvolvimento do Backend e do Módulo I.A.; infraestrutura | Testes em que mensagem sem assinatura, com outra chave ou com campo alterado é recusada sem acesso ao Storage; testes das permissões das duas credenciais. | BAIXO (4) para ambos |
| RI06 | Reduzir | Premissa da AME6, ausente do servico.md e do RQF4: o Módulo I.A. decodifica a mídia. Fazer isso em contêiner descartável por mídia, sem rede, credencial nem privilégio, com limite de tempo e memória e alerta em falha, e atualizar o decodificador até 7 dias após vulnerabilidade publicada. | Identify (ID.RA), Protect (PR.PS, PR.IR), Detect (DE.AE) | Desenvolvimento do Módulo I.A.; infraestrutura; operação do serviço | Testes no contêiner em que a rede falha, não há credencial e o estouro de limite termina em falha com alerta; histórico de versões do decodificador diante das vulnerabilidades publicadas. | MÉDIO (6) |
| RI07 | Aceitar | Nenhum controle do serviço impede colar em outra I.A. o texto baixado (RQF8) ou exibido (RQF6). A gestão do MP publica regra que proíbe levar transcrições a serviços externos, e o Backend registra cada download para apoiar a apuração de um vazamento. | Govern (GV.PO), Identify (ID.RA), Detect (DE.CM) | Gestão do MP; desenvolvimento do Backend | Decisão de aceite registrada pela gestão do MP; regra publicada; evento de download na trilha de auditoria, em teste. | MUITO ALTO (16) |
| RI08 | Reduzir | Expirar a sessão após 15 minutos sem tecla, clique ou rolagem e após 8 horas. Com apoio do provedor de identidade, o RQF11 também encerra a sessão no provedor, e download e exclusão pedem nova autenticação após 15 minutos. A operação acompanha quantas sessões terminam por expiração. | Protect (PR.AA), Detect (DE.CM) | Desenvolvimento do Backend; equipe do provedor de identidade; operação do serviço | Testes de expiração (15 minutos sem interação e 8 horas), de novo acesso após o RQF11 pedindo credenciais e de download e exclusão pedindo autenticação. | MÉDIO (6) |
| RI09 | Reduzir | Limitar cada promotor a 10 solicitações simultâneas na fila ou em processamento, até 3 na fila de mídias grandes (RQNF4.4), recusando o excesso antes da cópia. Espera acima de 30 minutos gera alerta, e a operação amplia as instâncias do Módulo I.A. | Protect (PR.IR), Detect (DE.CM) | Desenvolvimento do Backend; operação do serviço | Teste em que o 11º pedido simultâneo do mesmo promotor é recusado sem cópia no Storage; alerta com espera simulada acima de 30 minutos. | BAIXO (4) |
| RI10 | Aceitar | Nenhum controle novo: a cota da I.A. se recompõe sozinha, e as transcrições perdidas podem ser pedidas de novo. A operação acompanha as falhas da I.A. por promotor a partir do motivo que a RQNF5.3 grava. | Identify (ID.RA), Detect (DE.CM) | Gestão do MP; operação do serviço | Decisão de aceite registrada; relatório das falhas da I.A. por promotor, gerado do registro de transcrições. | BAIXO (4) |
| RI11 | Reduzir | Apagar a cópia da mídia do Storage quando a transcrição é finalizada; a reentrega da RQNF5.1 não a procura, pela idempotência da RQNF5.2. Mídia de transcrição com falha fica 7 dias para a operação analisar os alertas do RI06 e do RI15. Ocupação acima de 80% gera alerta. | Protect (PR.IR), Detect (DE.CM) | Desenvolvimento do Backend; infraestrutura; operação do serviço | Testes em que a mídia some ao finalizar sem quebrar a reentrega, e a de falha some após 7 dias; alerta com ocupação simulada acima de 80%. | BAIXO (3) |
| RI12 | Reduzir | Limitar cada promotor a 2 chats abertos e 3 perguntas por minuto, novas tentativas incluídas, e guardar em memória só as 20 últimas trocas de cada chat, que são as enviadas à I.A. | Protect (PR.IR) | Desenvolvimento do Backend | Testes em que o 3º chat e a 4ª pergunta no mesmo minuto são recusados; teste de carga com todos os promotores em 2 chats sobre a maior transcrição, sem esgotar a memória. | BAIXO (2) |
| RI13 | Reduzir | O provedor de identidade exige segundo fator dos promotores, e o Backend aceita só token que o indique (acr ou amr). O Backend alerta quando um promotor entra de país ou provedor de acesso nunca usado por ele; a operação confirma com o promotor e revoga as sessões dele. | Protect (PR.AA), Detect (DE.AE), Respond (RS.MI) | Equipe do provedor de identidade; desenvolvimento do Backend; operação do serviço | Configuração do provedor com segundo fator obrigatório; testes em que token sem segundo fator é recusado, login de origem nova gera alerta e a revogação encerra as sessões do promotor. | MÉDIO (8) |
| RI14 | Aceitar | Nenhum controle novo. A P = 1 da análise já supõe a RQNF6.2 aplicada como bloqueio de sobrescrita no Storage, e os eventos de auditoria da RQNF6.3 servem de monitoramento. | Identify (ID.RA), Detect (DE.CM) | Gestão do MP; infraestrutura | Decisão de aceite registrada; teste em que o Storage recusa a sobrescrita de transcrição finalizada pelas credenciais do Backend e do Módulo I.A. | BAIXO (4) |
| RI15 | Reduzir | Levar o hash da RQNF6.1 na mensagem assinada do RI05 e RI16 e permitir gravar mídias só à credencial do Backend. A divergência, que o RQF4 já trata como falha, também gera alerta. | Protect (PR.DS, PR.AA), Detect (DE.AE) | Desenvolvimento do Backend e do Módulo I.A.; infraestrutura | Teste em que a mídia trocada após a cópia termina em falha com alerta; teste em que a credencial do Módulo I.A. não grava no local das mídias. | BAIXO (4) |

---

### Risco residual e condições de aceite

O residual usa a escala da análise: nota igual a P × I e a mesma tabela de referência. Controle que impede o evento reduz P; controle que limita o dano do evento reduz I. Residual BAIXO é aceito com as evidências da linha no plano. Residual MÉDIO ou acima, e toda linha em Aceitar, exige também decisão registrada da gestão do MP, dona dos riscos (ID.RA-06), e o monitoramento indicado como Detect no plano.

| Risco | Nível inicial | Nível residual esperado | Condição para aceitar o residual |
| --- | --- | --- | --- |
| RI01 | MUITO ALTO (P 4 × I 4 = 16) | BAIXO (P 1 × I 4 = 4) | Na data do aceite, a suíte cobre todas as rotas que recebem id e está ligada à esteira de entrega. |
| RI02 | ALTO (P 3 × I 4 = 12) | BAIXO (P 1 × I 4 = 4) | O responsável pelo Sistema Processos confirma por escrito a premissa 5. |
| RI03 | ALTO (P 3 × I 3 = 9) | BAIXO (P 1 × I 3 = 3) | A configuração apresentada atribui o papel de promotor só a promotores. |
| RI04 | ALTO (P 3 × I 4 = 12) | BAIXO (P 1 × I 4 = 4) | O isolamento do RI06 está comprovado, para que o certificado do Módulo I.A. fique fora do alcance de código injetado. |
| RI05 | MÉDIO (P 2 × I 4 = 8) | BAIXO (P 1 × I 4 = 4) | A chave de assinatura só existe no cofre, sem cópia no Backend. |
| RI06 | ALTO (P 3 × I 4 = 12) | MÉDIO (P 3 × I 2 = 6) | A premissa da decodificação no Módulo I.A. está confirmada; se só a I.A. externa decodifica, a linha deve ser refeita. |
| RI07 | MUITO ALTO (P 4 × I 4 = 16) | MUITO ALTO (P 4 × I 4 = 16) | Decisão da gestão do MP, reavaliada a cada 12 meses. A única forma de reduzir P é retirar download e exibição, o que elimina a finalidade do serviço. |
| RI08 | ALTO (P 3 × I 3 = 9) | MÉDIO (P 2 × I 3 = 6) | O provedor de identidade atende ao encerramento e à reautenticação pedidos pelo Backend. |
| RI09 | MÉDIO (P 3 × I 2 = 6) | BAIXO (P 2 × I 2 = 4) | A operação consegue ampliar o Módulo I.A. dentro da cota contratada da I.A. |
| RI10 | BAIXO (P 2 × I 2 = 4) | BAIXO (P 2 × I 2 = 4) | Decisão da gestão do MP; falhas repetidas de um mesmo promotor levam à reavaliação do risco. |
| RI11 | MÉDIO (P 2 × I 3 = 6) | BAIXO (P 1 × I 3 = 3) | A remoção não alcança mídia de transcrição na fila ou em processamento. |
| RI12 | MÉDIO (P 2 × I 3 = 6) | BAIXO (P 1 × I 2 = 2) | O teste de carga usa o número real de promotores do MP. |
| RI13 | ALTO (P 3 × I 4 = 12) | MÉDIO (P 2 × I 4 = 8) | O provedor de identidade emite acr ou amr no token. |
| RI14 | BAIXO (P 1 × I 4 = 4) | BAIXO (P 1 × I 4 = 4) | Decisão da gestão do MP, com o bloqueio de sobrescrita comprovado. |
| RI15 | MÉDIO (P 2 × I 4 = 8) | BAIXO (P 1 × I 4 = 4) | A assinatura do RI05 e RI16 funciona. A adulteração no Sistema Processos, antes da cópia, cabe ao responsável por aquele sistema. |
| RI16 | MÉDIO (P 2 × I 4 = 8) | BAIXO (P 1 × I 4 = 4) | Mesma condição do RI05; a reentrega de mensagem legítima não gera nova transcrição (RQNF5.2). |

Se uma evidência não for produzida, um teste falhar ou uma dependência externa não se confirmar, o residual daquela linha volta ao nível inicial e o risco deve ser reavaliado.

---

### Ordem inicial de implementação

**Descrição**: Precisamos de uma tabela que descreva qual será a ordem de implemetação das estratégias de maneira resumida. Olhar como exemplo o repositório (https://github.com/camillabdt/VitaLink/blob/main/docs/etapa2-riscos-e-tratamento.md).

A implementação das estratégias deve priorizar inicialmente os controles relacionados ao **controle de acesso e autenticação**, pois eles impedem que usuários não autorizados obtenham acesso às transcrições e aos processos. Em seguida, devem ser implementados os mecanismos de integridade e, por fim, os controles de monitoramento e detecção de comportamentos anômalos.

| Ordem | Riscos | Estratégia | Implementação resumida |
| --- | --- | --- | --- |
| 1 | RI01, RI02, RI03 e RI13 | Reduzir | Implementar os controles de autenticação e autorização no Backend, garantindo que o usuário autenticado tenha acesso somente às transcrições e processos que lhe pertencem e que sua identidade não possa ser alterada por parâmetros enviados pelo cliente. |
| 2 | RI04 e RI06 | Reduzir | Proteger o endpoint utilizado pelo Módulo I.A. para atualização das transcrições, exigindo autenticação de serviço e validando os identificadores recebidos, e isolar a decodificação da mídia no Módulo I.A. |
| 3 | RI05, RI16 e RI15 | Reduzir | Implementar o controle de integridade das mídias por meio de hash e da assinatura das mensagens da fila. |
| 4 | RI07 | Aceitar | Implementar o controle de downloads, registrando a quantidade de downloads realizados por usuário. |
| 5 | RI08, RI09, RI11 e RI12 | Reduzir | Implementar os limites de uso (sessão, solicitações simultâneas, chats e perguntas) e a remoção das mídias já transcritas, com alerta quando o padrão esperado é ultrapassado. |

RI10 e RI14 ficam em Aceitar sem controle novo e não entram na ordem.

---

### Conclusão

As estratégias definidas buscam reduzir os riscos identificados por meio de controles preventivos, de integridade e de monitoramento. A prioridade inicial deve ser o controle de autenticação e autorização, pois esses mecanismos estabelecem quem pode acessar os processos e transcrições e impedem que a identidade ou o escopo de acesso sejam manipulados pelo cliente.

Em seguida, os controles relacionados ao endpoint do Módulo I.A. e à integridade das transcrições reduzem a possibilidade de alterações ou atualizações indevidas nos registros. Por fim, o monitoramento da quantidade de downloads permite identificar comportamentos anômalos que podem indicar uso indevido do sistema.

Os riscos foram agrupados quando possuem controles ou mecanismos de tratamento semelhantes, mantendo a rastreabilidade entre os riscos e as estratégias propostas. Os níveis de risco residual apresentados na tabela representam uma estimativa após a implementação dos controles e devem ser reavaliados posteriormente com base nos resultados dos testes e nas evidências obtidas durante a implementação.

## Monitoramento e Detecção de Intrusões

### Descrição

Este roteiro descreve como o serviço de transcrição deveria reconhecer comportamentos suspeitos depois de entrar em operação. Ele parte dos riscos da [análise de risco](etapa2-analise-de-risco.md), dos controles que o [plano de tratamento](etapa2-tratamento-de-risco-nist.md) marcou nas funções Detect e Respond do NIST CSF 2.0, dos requisitos da [arquitetura segura](etapa3-arquitetura-segura.md) e do que o [protótipo](../prototipo) da Etapa 4 já registra.

---

### 1. O que é detecção de intrusões

Detecção de intrusões é a observação contínua de um sistema em operação para reconhecer uso que viola a política de segurança e avisar quem pode agir. A base são os eventos que o próprio sistema registra: autenticações, acessos negados, erros, requisições e mudanças de estado. Um detector lê esses eventos, aplica regras e produz alertas. Um sistema de detecção de intrusões (IDS) é o componente que automatiza essa leitura; nesta etapa ele não é instalado, mas os eventos e as regras que ele consumiria ficam definidos.

Há dois pontos de observação possíveis: (1) um detector de rede examina o tráfego entre máquinas, (2) um detector de host ou de aplicação examina o que acontece dentro de um servidor ou de um programa. No serviço de transcrição todo o tráfego do promotor chega como requisição HTTP ao Backend, e o que separa uso legítimo de abuso é quem pediu o quê: qual promotor, qual transcrição, com qual sessão. Essa informação só existe dentro da aplicação, por isso o roteiro trata de detecção no nível da aplicação, a partir dos registros do Backend e do Módulo I.A., sem equipamento de rede dedicado.

Há também dois tipos de regra. Uma regra de assinatura descreve um comportamento já conhecido como suspeito, por exemplo "dez acessos negados do mesmo promotor em um minuto". Uma regra de anomalia descreve um desvio do padrão habitual, por exemplo "promotor entrou de um país de onde nunca entrou". O plano de tratamento previu os dois tipos: o primeiro nas recusas com alerta de RI01, RI04 e RI15, o segundo no login de origem nova de RI13. Regras de assinatura dependem só de contagens sobre os eventos da seção 3; regras de anomalia exigem guardar o histórico de cada promotor para saber o que é habitual.

---

### 2. Prevenir e detectar

Prevenir é impedir a ação indevida no momento em que ela é tentada. As duas práticas da [Etapa 4](etapa4-codigo-seguro.md) são controles preventivos: a prática 1 responde 401 ao token de identidade sem segundo fator, e a prática 2 responde 403 a qualquer rota chamada com o id de uma transcrição de outro promotor. Detectar é perceber que a ação indevida foi tentada ou aconteceu, mesmo quando o controle preventivo a bloqueou, e levar o fato a alguém que possa reagir.

Os dois se complementam por três motivos.
1. A prevenção age requisição por requisição e não enxerga a campanha. Na [verificação da Etapa 5](etapa5-verificacao-vulnerabilidades.md) o script enviou ao Backend cinco tokens de identidade recusados, quatro requisições de dados sem sessão válida, duas tentativas de abrir ou excluir a transcrição de outro promotor e uma com id inexistente. Todas foram recusadas com o status esperado, mas ninguém foi avisado: o protótipo grava um aviso por token recusado e um registro `ACESSO_NEGADO` por 403, o 401 sem sessão não deixa registro, e nenhum componente conta esses registros. Um promotor executando o caso de abuso CAE1 recebe 403 em cada tentativa e continua tentando; só a detecção revela que ele tentou cem ids em dez minutos. 
2. Alguns riscos não têm prevenção. RI07, o promotor que leva a transcrição baixada a um serviço externo, foi aceito: nenhum controle do serviço impede colar o texto em outra ferramenta, e o plano escolheu registrar cada download para apoiar a apuração de um vazamento. RI10 também foi aceito, e a operação só acompanha as falhas da I.A. por promotor. Para esses dois riscos o registro e a detecção são o único controle que o serviço tem. No RI14, também aceito, o bloqueio de sobrescrita da RQNF6.2 continua valendo, e a detecção cobre o que ele não alcança. 
3. A prevenção pode ser contornada com credenciais legítimas. Um atacante com a senha e o segundo fator de um promotor (RI13), ou alguém que usa a sessão aberta em um computador fora do MP (RI08), passa por todos os controles preventivos, porque para o Backend ele é o promotor. O que o distingue é o comportamento: origem nunca vista, horário incomum, volume de downloads fora do padrão. Só a detecção alcança esse caso.

Detectar tem um custo que prevenir não tem. Cada alerta exige alguém para ler, confirmar e responder, e regras difusas produzem falsos positivos que a equipe aprende a ignorar. Por isso a seção 3 limita os eventos ao que as regras precisam, e a seção 5 define quem responde e como.

---

### 3. Eventos que o sistema deveria registrar

Os eventos desta seção são registros (log) do que o sistema fez; alerta é o que uma regra da seção 4 produz ao ler esses registros. O protótipo chama de alerta a linha `ALERTA ACESSO_NEGADO` da classe `Auditoria`, mas pela definição deste roteiro ela é um registro de 403, e nenhuma regra a lê ainda.

Todo evento leva o instante em UTC, o componente que o gerou, a rota e o método quando vem de uma requisição, o código de resposta, o endereço de origem e o id do promotor quando há sessão. Nenhum evento leva o conteúdo de mídias, transcrições ou mensagens de chat, nem tokens de identidade ou de sessão: o que se registra são identificadores, regra que o registro `ACESSO_NEGADO` do protótipo já segue. Os registros saem do contêiner pela saída padrão e vão para um coletor central, onde só podem ser acrescentados, nunca alterados, e fora do alcance das credenciais do Backend e do Módulo I.A., porque apagar o rastro é parte do que um intruso faz. O prazo de retenção é decisão da gestão do MP, dona dos riscos.

| Evento | Campos além dos comuns | Risco ou controle atendido |
| --- | --- | --- |
| E1. Sessão criada | promotor, provedor de identidade, valor do claim `amr`, país e provedor de acesso da origem | RI13 (login de origem nova), RI08 |
| E2. Token de identidade recusado | motivo da recusa (assinatura, emissor, audiência, prazo, segundo fator ausente), sem o token | RS02, RI13, RI03 |
| E3. Requisição sem sessão válida | rota pedida | RI08, token de identidade usado no lugar da sessão |
| E4. Sessão encerrada, expirada ou revogada | promotor, motivo (saída, 15 minutos sem interação, 8 horas, revogada pela operação) | RI08 (a operação acompanha quantas expiram), RI13 (revogação após alerta) |
| E5. Acesso negado por propriedade | promotor, operação, id do recurso | RI01, RS01 |
| E6. Transcrição exibida ou baixada | promotor, id da transcrição, formato | RI07 (aceito; trilha para apuração) |
| E7. Solicitação, chat ou pergunta recusados por limite | promotor, pedidos, chats ou perguntas em andamento | RI09, RI12 |
| E8. Chamada ao endpoint de status recusada | resultado do TLS mútuo, certificado apresentado, id, motivo (sem certificado, de outro serviço, revogado, campo de caminho, mudança após finalizada) | RS03, RI04 |
| E9. Mensagem da fila recusada ou hash divergente | id, motivo (assinatura inválida, hash da mídia diferente) | RI05, RI16, RI15 |
| E10. Integridade de transcrição finalizada violada | id, arquivo ausente ou hash diferente do registrado | RI14 |
| E11. Transcrição com falha | id, promotor, motivo | RI10, RI06 |

Os eventos E1, E2, E3 e E5 bastam para regras de assinatura sobre autenticação e autorização, e dois deles, E2 e E5, já existem no protótipo. E6 cobre o risco aceito de maior nota. E8 a E10 dependem de componentes que o protótipo não tem e ficam como requisito para a implementação completa.

---

### 4. Regras de detecção

As regras propostas correlacionam os eventos registrados pelo Backend e pelo Módulo I.A. Os limiares são parâmetros iniciais para implantação e devem ser ajustados após a observação do comportamento legítimo do serviço. A ocorrência de um evento isolado não representa necessariamente um incidente; a classificação considera a frequência, a combinação de eventos e a criticidade do controle envolvido.

As regras de assinatura utilizam contagens em janelas de tempo. As regras de anomalia comparam o comportamento atual com o histórico do promotor, especialmente quanto à origem dos acessos e ao volume de downloads. Quando não houver histórico suficiente, o evento deve ser registrado para formação da linha de base, sem gerar automaticamente uma conclusão de comprometimento.

| Regra | Condição para alerta | Eventos relacionados | Risco | Severidade inicial |
|---|---|---|---|---|
| **RD01. Repetição de falhas de autenticação** | Cinco ou mais tokens de identidade recusados para o mesmo promotor ou origem em cinco minutos. | E2 | RI13, RI03 | Média |
| **RD02. Requisições sem sessão válida** | Quatro ou mais requisições sem sessão válida para a mesma origem em cinco minutos. | E3 | RI08 | Média |
| **RD03. Tentativas de acesso a recursos de terceiros** | Três ou mais acessos negados por propriedade em dez minutos, ou tentativa de consultar dez ou mais identificadores distintos no mesmo intervalo. | E5 | RI01 | Alta |
| **RD04. Acesso de origem não habitual** | Criação de sessão a partir de país ou provedor de acesso ainda não observado no histórico do promotor. | E1 | RI13, RI08 | Média |
| **RD05. Volume atípico de downloads** | Volume de downloads superior a três vezes a média histórica do promotor em período equivalente. | E6 | RI07 | Média |
| **RD06. Uso recorrente acima dos limites** | Repetição de recusas por limite de solicitações, chats ou perguntas em uma janela de dez minutos. | E7 | RI09, RI12 | Média |
| **RD07. Falha de autenticação entre serviços** | Qualquer chamada recusada no endpoint de atualização de status por ausência, invalidade ou inadequação do certificado mTLS. | E8 | RI04 | Alta |
| **RD08. Falha de integridade da fila** | Qualquer mensagem recusada por assinatura inválida ou divergência entre o hash recebido e o hash esperado. | E9 | RI05, RI15, RI16 | Alta |
| **RD09. Violação de integridade de transcrição** | Arquivo ausente ou divergência de hash após a finalização da transcrição. | E10 | RI14 | Alta |
| **RD10. Repetição de falhas de processamento** | Cinco ou mais transcrições com falha para o mesmo promotor em dez minutos. | E11 | RI10, RI06 | Média |

As regras RD01, RD02 e RD03 procuram identificar tentativas repetidas de autenticação e acesso indevido. Os limiares permitem diferenciar uma falha pontual de um comportamento persistente. Na RD03, a quantidade de identificadores distintos é especialmente relevante: a tentativa de consultar vários recursos em sequência pode indicar enumeração de identificadores, mesmo que todas as requisições sejam bloqueadas pela autorização por propriedade.

A RD04 depende de histórico de acesso por promotor. Uma origem inédita não comprova, isoladamente, uma invasão, pois o usuário pode estar utilizando outra rede ou dispositivo. Por isso, o alerta deve ser tratado como sinal para verificação, principalmente quando acompanhado de falhas de autenticação ou de comportamento incompatível com o histórico.

A RD05 acompanha o risco RI07, aceito no plano de tratamento. Como o serviço não consegue impedir que um promotor copie uma transcrição já baixada para outra ferramenta, o volume de downloads funciona como indicador para investigação. A comparação com a média histórica evita estabelecer uma quantidade fixa que possa ser inadequada para promotores com rotinas diferentes.

As regras RD07, RD08 e RD09 possuem limiar unitário. Uma falha de autenticação entre serviços, uma assinatura inválida ou uma divergência de integridade representa uma ocorrência relevante em um controle de segurança crítico. Nesses casos, a equipe responsável deve verificar a origem do evento antes de permitir a continuidade da operação afetada.

Os alertas devem conter o identificador da regra, a severidade, o período analisado, os identificadores dos eventos relacionados e os dados necessários à investigação. Não devem conter transcrições, mídias, mensagens de chat, senhas ou tokens. A centralização e a correlação dos registros permitem identificar sequências de eventos que, individualmente, poderiam parecer legítimas.

Os limiares e as regras desta seção constituem uma especificação para a implementação do monitoramento. No protótipo, os registros de token recusado e de acesso negado já fornecem parte dos dados necessários, mas não existe ainda um componente que execute essas regras e gere os alertas correspondentes.

---

### 5. Depois do alerta

A geração de um alerta inicia um processo de triagem e resposta. O
alerta não deve ser tratado automaticamente como confirmação de
incidente: primeiro é necessário verificar se os registros são
consistentes, se o comportamento pode ser explicado por uma atividade
legítima e se há evidências de que um controle foi contornado ou
violado.

O fluxo de resposta proposto segue cinco etapas:

1.  **Triagem e classificação.** A equipe responsável pelo serviço
    verifica a regra acionada, a severidade, o período, o promotor
    envolvido e os eventos relacionados. Deve consultar os registros
    anteriores e posteriores à ocorrência para identificar se há uma
    sequência de tentativas. Alertas duplicados ou explicados por uma
    atividade legítima podem ser encerrados como falso positivo, com a
    justificativa registrada.
2.  **Contenção.** Quando houver indícios de comprometimento, a equipe
    técnica deve limitar a continuidade da atividade suspeita. Conforme
    o caso, isso pode envolver revogar as sessões do promotor, solicitar
    nova autenticação, bloquear temporariamente uma origem, suspender o
    processamento de mensagens suspeitas da fila ou isolar o componente
    afetado. A contenção deve preservar a disponibilidade do serviço
    sempre que possível e não deve eliminar os registros necessários à
    investigação.
3.  **Investigação e preservação de evidências.** A equipe deve reunir
    os eventos relacionados, os horários, os identificadores de
    recursos, as respostas HTTP, a origem das requisições e as
    alterações de estado. Os registros devem ser preservados no
    repositório central, com controle de acesso e proteção contra
    alteração. A investigação deve verificar se houve acesso efetivo a
    dados, alteração de estado, uso indevido de sessão ou
    comprometimento da comunicação entre serviços. Conteúdos de
    transcrições e mídias não devem ser copiados para os registros de
    investigação sem necessidade e autorização.
4.  **Erradicação e recuperação.** Confirmada a causa, a equipe técnica
    corrige a vulnerabilidade ou configuração responsável, revoga
    credenciais comprometidas, substitui certificados quando necessário
    e valida a integridade dos componentes afetados. O serviço só deve
    retornar à operação normal após a verificação dos controles
    envolvidos e a confirmação de que a condição que originou o alerta
    foi eliminada.
5.  **Encerramento e melhoria.** A equipe registra a causa identificada,
    o impacto, as ações executadas, o tempo de resposta e a conclusão da
    investigação. Quando aplicável, as regras de detecção devem ser
    ajustadas para reduzir falsos positivos ou identificar melhor o
    comportamento observado. Riscos residuais e incidentes que indiquem
    falha de controles devem ser comunicados à gestão responsável.

A prioridade da resposta deve considerar a severidade inicial e o
impacto confirmado. Alertas de severidade alta, como falha de
autenticação mútua, assinatura inválida, divergência de hash ou violação
da integridade de uma transcrição finalizada, devem ser encaminhados
imediatamente à equipe técnica responsável. Alertas de severidade média
devem passar por triagem, com escalonamento quando houver recorrência ou
associação com outros eventos.

A responsabilidade pela execução técnica da contenção e recuperação é da
equipe que administra o serviço. A gestão do Ministério Público deve ser
comunicada quando houver suspeita de exposição de transcrições,
comprometimento de contas, impacto sobre dados institucionais ou
necessidade de aceitar risco residual. A decisão sobre comunicação
institucional e medidas administrativas deve seguir os procedimentos
internos aplicáveis.

Para avaliar a efetividade do processo, devem ser acompanhados, no
mínimo, o tempo entre a ocorrência e a geração do alerta, o tempo até a
triagem, o tempo de contenção, a quantidade de falsos positivos, os
incidentes confirmados e a recorrência de eventos após a correção. Esses
indicadores permitem verificar se as regras estão produzindo alertas
úteis e se a resposta está reduzindo o impacto dos incidentes.

Assim, o monitoramento complementa os controles preventivos definidos
nas etapas anteriores. Enquanto a arquitetura segura e as práticas de
código seguro procuram impedir acessos e operações indevidas, as regras
desta etapa procuram identificar tentativas recorrentes, comportamentos
atípicos e violações que não podem ser integralmente evitadas por
controles preventivos.
---

### 6. Referências

- NIST Cybersecurity Framework 2.0, funções Detect (DE.CM, DE.AE) e Respond (RS.MI): <https://doi.org/10.6028/NIST.CSWP.29>
- NIST SP 800-94, Guide to Intrusion Detection and Prevention Systems (IDPS): <https://csrc.nist.gov/pubs/sp/800/94/final>
- OWASP Top 10 2021, A09 Security Logging and Monitoring Failures: <https://owasp.org/Top10/2021/A09_2021-Security_Logging_and_Monitoring_Failures/>
- OWASP Logging Cheat Sheet: <https://cheatsheetseries.owasp.org/cheatsheets/Logging_Cheat_Sheet.html>
- CWE-778, Insufficient Logging: <https://cwe.mitre.org/data/definitions/778.html>

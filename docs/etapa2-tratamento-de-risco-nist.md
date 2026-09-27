## Estratégias para tratamento dos riscos

### Descrição

Neste documento será implementado a estratégia para mitigação dos riscos já levantados [neste documento](etapa2-analise-de-risco.md). Para isso iresmos utilizar o framework NIST CSF 2.0. Este documento tem como objetivo responder para cada risco levantado **o que fazer** com o mesmo.

---

### As funções do NIST

A tabela abaixo detalha as **funções** sugeridas pelo framework NIST CSF 2.0.


| Função   | Descrição                                                                                                   |
|----------|-------------------------------------------------------------------------------------------------------------|
| Govern   | Define as políticas, responsabilidades e pessoas que devem tratar cada risco.                               |
| Identify | Identificar os ativos, dependências, vulnerabilidades e riscos.                                             |
| Protect  | Implementar salvaguardas para mitigar a probabilidade ou o impacto, ou ambos.                               |
| Detect   | Identificar eventos suspeitos. Uso de logs e monitoramento.                                                 |
| Respond  | Após a identificação, como responder a um evento suspeito ou malicioso? Conter a ameaça, analisar e tratar. |
| Recover  | Quando um evento malicioso acontece, como reverter o prejuízo?                                              |

---

### Estratégias de tratamento

Descrição resumida das  estratégias que podem ser adotadas para tratamento de cada risco

| Estratégia   | Descrição                                                                                 |
|--------------|-------------------------------------------------------------------------------------------|
| Evitar       | Eliminar a funcionalidade ou condição que permite o risco.                                |
| Reduzir      | Implementar mecanismos que mitigam a probabilidade ou o impacto do risco.                 |
| Compartilhar | Compartilhar com terceiros a responsabilidade e tratamento do risco.                      |
| Aceitar      | Aceitar a existência do risco. Decisão consciente. No entanto, mantém o risco monitorado. |

---

### Plano de tratamento dos riscos

A tabela abaixo contém o plano concreto para tratamento dos riscos. A coluna especifica o mecânismo que será implementado para mitigar o(s) risco(s).

| Risco      | Estratégia | Controle                                                                                                                                                                                                                                                                              | Função NIST CSF 2.0                | Responsáveis              | Evidência necessária                                                                                                                                                                | Risco residual |
|------------|------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------|---------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------|
| RI01       | Reduzir    | Implementar autorização por objeto no Backend, validando se a transcrição solicitada pertence ao promotor autenticado antes de permitir visualização, download, exclusão ou uso no chat.                                                                                              | Protect                            | Equipe de desenvolvimento | Testes demonstrando que um promotor não consegue acessar transcrições pertencentes a outro promotor; validação do código de autorização.                                            | BAIXO          |
| RI02       | Reduzir    | O Backend deve utilizar exclusivamente a identidade do promotor obtida da sessão autenticada ao realizar consultas ao Sistema Processos, impedindo que a identidade seja definida por parâmetros enviados pelo cliente.                                                               | Protect                            | Equipe de desenvolvimento | Testes de autorização demonstrando que a alteração da identidade na requisição não permite acesso a processos de outro promotor; registros das requisições realizadas pelo Backend. | BAIXO          |
| RI03       | Reduzir    | Validar, além da assinatura e do emissor do token, a audiência (audience) e o papel/perfil do usuário antes de criar a sessão, permitindo acesso às funcionalidades somente para usuários autorizados como promotores.                                                                | Protect                            | Equipe de desenvolvimento | Testes com tokens válidos, porém destinados a outro sistema ou pertencentes a usuários sem o perfil de promotor, demonstrando que o acesso é negado.                                | BAIXO          |
| RI04       | Reduzir    | Exigir autenticação de serviço no endpoint utilizado pelo Módulo I.A. para atualização do status da transcrição e validar a origem, o identificador da transcrição e o caminho do arquivo antes de atualizar o registro.                                                              | Protect                            | Equipe de desenvolvimento | Testes de autenticação do endpoint e testes demonstrando que requisições não autenticadas ou com caminhos arbitrários são rejeitadas.                                               | BAIXO          |
| RI05, RI16 | Reduzir    | Exigir autenticação para publicar na fila e assinar cada mensagem com chave do Backend, validando no consumidor a assinatura, o identificador da solicitação e o caminho da mídia antes de qualquer leitura ou escrita no Storage. | Protect, Detect | Equipes de desenvolvimento e infraestrutura | Testes com mensagens não assinadas, adulteradas e com caminho fora da solicitação, demonstrando rejeição e evento em log. | BAIXO |
| RI06       | Reduzir    | Executar a decodificação de mídia em ambiente isolado (sandbox) com credencial restrita aos caminhos da própria solicitação, mantendo a validação de formato e antimalware (RQNF6.5) como primeira barreira. | Protect | Equipe de infraestrutura | Teste com mídia malformada demonstrando contenção no isolamento; revisão das permissões da credencial do Módulo I.A. | BAIXO |
| RI07       | Reduzir    | Limitar a quantidade de downloads de transcrição por usuário e gerar alerta quando o volume passar do considerado "normal" (por que um usuário baixaria 5 cópias da mesma transcrição?). Aplicar marca d'água com identificação do promotor e data no .docx gerado e registrar cada download em trilha de auditoria, permitindo atribuir a origem de uma cópia que vaze para fora do ambiente do MP. | Identify, Protect, Detect, Respond | Equipe de desenvolvimento | Contador de downloads no banco de dados; teste do alerta de volume anômalo; verificação da marca d'água no arquivo gerado e do evento de download no log de auditoria.              | MÉDIO          |
| RI08       | Reduzir    | Expirar a sessão por inatividade, exigir nova autenticação antes de download e exclusão e registrar a origem (rede e dispositivo) de cada acesso, apoiado por política institucional de uso em ambiente externo. | Govern, Protect, Detect | Equipe de desenvolvimento e gestão do MP | Teste de expiração de sessão; log de acessos com origem; política de uso publicada. | MÉDIO |
| RI09       | Reduzir    | Aplicar rate limiting por usuário e sessão nas solicitações de transcrição (RQF3), com distribuição justa da fila entre usuários. | Protect | Equipe de desenvolvimento | Teste de carga demonstrando bloqueio acima do limite sem afetar os demais usuários. | BAIXO |
| RI10       | Reduzir    | Validar a mídia antes do envio à I.A. externa e limitar retries com backoff, descartando a solicitação após um número máximo de falhas e alertando falhas repetidas do mesmo usuário. | Protect, Detect | Equipe de desenvolvimento | Teste com mídia corrompida demonstrando descarte após o limite de retries e alerta gerado. | BAIXO |
| RI11       | Reduzir    | Definir quota de Storage por usuário e política de retenção com limpeza automática das mídias após a transcrição, monitorando a capacidade com alertas. | Protect, Detect, Recover | Equipe de infraestrutura | Teste de quota excedida; execução da rotina de limpeza; alerta de capacidade disparado em teste. | BAIXO |
| RI12       | Reduzir    | Limitar sessões de chat simultâneas e a frequência de perguntas por usuário, com teto para o histórico mantido em memória por sessão. | Protect | Equipe de desenvolvimento | Teste demonstrando bloqueio acima dos limites sem degradar as demais sessões. | BAIXO |
| RI13       | Reduzir    | Exigir autenticação multifator, bloquear a conta após tentativas falhas consecutivas, notificar login de novo dispositivo e permitir revogação imediata das sessões ativas. | Protect, Detect, Respond | Equipe de desenvolvimento | Testes de MFA, de bloqueio por tentativas e de revogação de sessão; registro dos logins com dispositivo e origem. | MÉDIO |
| RI14       | Reduzir    | Uma transcrição não pode ser alterada depois de finalizada. O promotor pode alterar apenas uma cópia do arquivo que ele baixou. Mas se ele subir um arquivo diferente pra contexto da I.A, uma verificação de hash deve ser feita para garantir que ele é identico ao que foi gerado. | Protect, Detect, Respond           | Equipe de desenvolvimento | Evento em log da verificação do hash e teste para forçar transcrição adulterada.                                                                                                    | BAIXO          |
| RI15       | Reduzir    | Calcular o hash da mídia no momento da cópia do Sistema Processos e verificá-lo no Módulo I.A. antes do processamento (RQNF6.1), rejeitando a solicitação e alertando em caso de divergência. | Protect, Detect | Equipe de desenvolvimento | Teste substituindo a mídia após a cópia, demonstrando rejeição e evento em log. | BAIXO |

---

### Risco residual e condições de aceite

O risco residual é o nível que permanece após a implementação e a validação dos controles do plano acima. Os valores são estimativas de planejamento: um controle proposto não é um controle implementado, e o residual só se confirma quando as evidências da tabela do plano forem produzidas. A condição de aceite descreve o que precisa estar demonstrado para o grupo conviver com o risco restante sem tratamento adicional.

| Risco | Nível inicial | Nível residual esperado | Condição para aceitar o residual |
| --- | --- | --- | --- |
| RI01 | MUITO ALTO | BAIXO | Testes negativos de acesso cruzado passando: nenhum promotor acessa, exclui ou usa no chat transcrição de outro. |
| RI02 | ALTO | BAIXO | Teste demonstrando que a identidade repassada ao Sistema Processos vem só da sessão e que sua manipulação é rejeitada. |
| RI03 | ALTO | BAIXO | Tokens válidos de outro sistema ou de usuário sem perfil de promotor negados em teste. |
| RI04 | ALTO | BAIXO | Requisições sem autenticação de serviço ou com caminho arbitrário rejeitadas em teste. |
| RI05 | MÉDIO | BAIXO | Mensagens não assinadas, adulteradas ou com caminho fora da solicitação rejeitadas, com evento em log. |
| RI06 | ALTO | BAIXO | Contenção de mídia malformada demonstrada no isolamento e credencial do Módulo I.A. revisada para alcance mínimo. |
| RI07 | MUITO ALTO | MÉDIO | Marca d'água e trilha de auditoria de downloads funcionando. O reuso externo de uma cópia baixada não é bloqueável; o grupo aceita o residual porque cada cópia passa a ser atribuível e monitorada. |
| RI08 | ALTO | MÉDIO | Expiração de sessão, log de origem por acesso e política de uso externo publicada. O comportamento fora do sistema permanece fora de controle técnico; o residual é aceito com monitoramento dos acessos externos. |
| RI09 | MÉDIO | BAIXO | Teste de carga com rate limiting bloqueando o excesso sem afetar os demais usuários. |
| RI10 | BAIXO | BAIXO | Descarte da solicitação após o limite de retries e alerta gerado em teste com mídia corrompida. |
| RI11 | MÉDIO | BAIXO | Quota excedida bloqueada em teste, rotina de limpeza executada e alerta de capacidade disparado. |
| RI12 | MÉDIO | BAIXO | Limites de sessões e de frequência do chat bloqueando o excesso em teste, sem degradar as demais sessões. |
| RI13 | ALTO | MÉDIO | MFA, bloqueio por tentativas e revogação de sessão testados. Phishing capaz de contornar MFA permanece possível; o residual é aceito com alertas de login de novo dispositivo. |
| RI14 | BAIXO | BAIXO | Verificação de hash executada em toda leitura de transcrição finalizada, com evento em log e teste de adulteração forçada. |
| RI15 | MÉDIO | BAIXO | Mídia substituída após a cópia rejeitada antes do processamento, com evento em log. |
| RI16 | MÉDIO | BAIXO | Mesmas evidências do RI05: assinatura validada no consumidor e mensagens adulteradas rejeitadas. |

Se alguma evidência não for produzida ou algum teste falhar, o residual correspondente volta a valer o nível inicial e o risco deve ser retratado.

---

### Ordem inicial de implementação

**Descrição**: Precisamos de uma tabela que descreva qual será a ordem de implemetação das estratégias de maneira resumida. Olhar como exemplo o repositório (https://github.com/camillabdt/VitaLink/blob/main/docs/etapa2-riscos-e-tratamento.md).

---

### Conclusão

Fazer uma consideração final das estratégias que levantamos para o tratamento dos riscos.

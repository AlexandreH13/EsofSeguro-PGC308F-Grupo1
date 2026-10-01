## Estratégias para tratamento dos riscos

### Descrição

Neste documento será implementado a estratégia para mitigação dos riscos já levantados [neste documento](etapa2-analise-de-risco.md). Para isso iresmos utilizar o framework NIST CSF 2.0. Este documento tem como objetivo responder para cada risco levantado **o que fazer** com o mesmo.

---

### As funções do NIST

A tabela abaixo detalha as **funções** sugeridas pelo framework NIST CSF 2.0.


| Função | Descrição |
| --- | --- |
| Govern | Define as políticas, responsabilidades e pessoas que devem tratar cada risco. |
| Identify | Identificar os ativos, dependências, vulnerabilidades e riscos. |
| Protect | Implementar salvaguardas para mitigar a probabilidade ou o impacto, ou ambos. |
| Detect | Identificar eventos suspeitos. Uso de logs e monitoramento. |
| Respond | Após a identificação, como responder a um evento suspeito ou malicioso? Conter a ameaça, analisar e tratar. |
| Recover | Quando um evento malicioso acontece, como reverter o prejuízo? |

---

### Estratégias de tratamento

Descrição resumida das  estratégias que podem ser adotadas para tratamento de cada risco

| Estratégia | Descrição |
| --- | --- |
| Evitar | Eliminar a funcionalidade ou condição que permite o risco. |
| Reduzir | Implementar mecanismos que mitigam a probabilidade ou o impacto do risco. |
| Compartilhar | Compartilhar com terceiros a responsabilidade e tratamento do risco. |
| Aceitar | Aceitar a existência do risco. Decisão consciente. No entanto, mantém o risco monitorado. |

---

### Plano de tratamento dos riscos

A tabela abaixo contém o plano concreto para tratamento dos riscos. A coluna especifica o mecânismo que será implementado para mitigar o(s) risco(s).

| Risco | Estratégia | Controle | Função NIST CSF 2.0 | Responsáveis | Evidência necessária | Risco residual |
| --- | --- | --- | --- | --- | --- | --- |
| RI01, RI02 | Reduzir | Implementar autorização por objeto no Backend, utilizando exclusivamente a identidade do promotor obtida da sessão autenticada. Antes de permitir visualização, download, exclusão ou uso de uma transcrição, o Backend deve validar se ela pertence ao promotor autenticado. Da mesma forma, ao consultar o Sistema Processos, a identidade do promotor não deve ser definida por parâmetros enviados pelo cliente. | Protect | Equipe de desenvolvimento | Testes demonstrando que um promotor não consegue acessar transcrições ou processos pertencentes a outro promotor, inclusive quando tenta alterar a identidade informada na requisição. Validação do código de autorização. | BAIXO |
| RI03 | Reduzir | Validar, além da assinatura e do emissor do token, a audiência (`audience`) e o papel/perfil do usuário antes de criar a sessão, permitindo acesso às funcionalidades somente para usuários autorizados como promotores. | Protect | Equipe de desenvolvimento | Testes com tokens válidos, porém destinados a outro sistema ou pertencentes a usuários sem o perfil de promotor, demonstrando que o acesso é negado. | BAIXO |
| RI04 | Reduzir | Exigir autenticação de serviço no endpoint utilizado pelo Módulo I.A. para atualização do status da transcrição e validar a origem, o identificador da transcrição e o caminho do arquivo antes de atualizar o registro. | Protect | Equipe de desenvolvimento | Testes de autenticação do endpoint e testes demonstrando que requisições não autenticadas ou com caminhos arbitrários são rejeitadas. | BAIXO |
| RI08, RI09| Reduzir | Uma transcrição não pode ser alterada depois de finalizada. O promotor pode alterar apenas uma cópia do arquivo que ele baixou. Mas se ele subir um arquivo diferente pra contexto da I.A, uma verificação de hash deve ser feita para garantir que ele é identico ao que foi gerado. | Protect, Detect, Respond | Equipe de desenvolvimento | Evento em log da verificação do hash e teste para forçar transcrição adulterada. | BAIXO |
| RI05, RI06 | Reduzir | Limita a quantidade de downloads da transcrição por usuário. Gerar um alerta caso a quantidade passar de um limite considerado "normal". Por exemplo, por que um usuário faria download de 5 cópias da mesma transcrição? | Identify, Protect, Detect, Respond | Equipe de desenvolvimento | Incluir contador de downloads como um campo no banco de dados | BAIXO |

---

### Ordem inicial de implementação

**Descrição**: Precisamos de uma tabela que descreva qual será a ordem de implemetação das estratégias de maneira resumida. Olhar como exemplo o repositório (https://github.com/camillabdt/VitaLink/blob/main/docs/etapa2-riscos-e-tratamento.md).

A implementação das estratégias deve priorizar inicialmente os controles relacionados ao **controle de acesso e autenticação**, pois eles impedem que usuários não autorizados obtenham acesso às transcrições e aos processos. Em seguida, devem ser implementados os mecanismos de integridade e, por fim, os controles de monitoramento e detecção de comportamentos anômalos.

| Ordem | Riscos | Estratégia | Implementação resumida |
| --- | --- | --- | --- |
| 1 | RI01, RI02 e RI03 | Reduzir | Implementar os controles de autenticação e autorização no Backend, garantindo que o usuário autenticado tenha acesso somente às transcrições e processos que lhe pertencem e que sua identidade não possa ser alterada por parâmetros enviados pelo cliente. |
| 2 | RI04 | Reduzir | Proteger o endpoint utilizado pelo Módulo I.A. para atualização das transcrições, exigindo autenticação de serviço e validando os identificadores e caminhos recebidos. |
| 3 | RI08, RI09 | Reduzir | Implementar o controle de integridade das transcrições por meio de hash, impedindo alterações em transcrições finalizadas e verificando arquivos enviados novamente para utilização no contexto da I.A. |
| 4 | RI05, RI06 | Reduzir | Implementar o controle de downloads, registrando a quantidade de downloads realizados por usuário e criando mecanismos de alerta para comportamentos que ultrapassem o padrão esperado. |

---

### Conclusão

As estratégias definidas buscam reduzir os riscos identificados por meio de controles preventivos, de integridade e de monitoramento. A prioridade inicial deve ser o controle de autenticação e autorização, pois esses mecanismos estabelecem quem pode acessar os processos e transcrições e impedem que a identidade ou o escopo de acesso sejam manipulados pelo cliente.

Em seguida, os controles relacionados ao endpoint do Módulo I.A. e à integridade das transcrições reduzem a possibilidade de alterações ou atualizações indevidas nos registros. Por fim, o monitoramento da quantidade de downloads permite identificar comportamentos anômalos que podem indicar uso indevido do sistema.

Os riscos foram agrupados quando possuem controles ou mecanismos de tratamento semelhantes, mantendo a rastreabilidade entre os riscos e as estratégias propostas. Os níveis de risco residual apresentados na tabela representam uma estimativa após a implementação dos controles e devem ser reavaliados posteriormente com base nos resultados dos testes e nas evidências obtidas durante a implementação.

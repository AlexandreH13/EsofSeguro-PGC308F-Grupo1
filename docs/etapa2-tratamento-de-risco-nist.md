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

A tabela abaixo contém o plano concreto para tratamento de cada risco e todas as informações necessárias.

| Risco | Estratégia | Controle | Função NIST CSF 2.0 | Responsáveis | Evidência necessária | Risco residual |
| --- | --- | --- | --- | --- | --- | --- |
| RI01 | Reduzir | Implementar autorização por objeto no Backend, validando se a transcrição solicitada pertence ao promotor autenticado antes de permitir visualização, download, exclusão ou uso no chat. | Protect | Equipe de desenvolvimento | Testes demonstrando que um promotor não consegue acessar transcrições pertencentes a outro promotor; validação do código de autorização. | BAIXO |
| RI02 | Reduzir | O Backend deve utilizar exclusivamente a identidade do promotor obtida da sessão autenticada ao realizar consultas ao Sistema Processos, impedindo que a identidade seja definida por parâmetros enviados pelo cliente. | Protect | Equipe de desenvolvimento | Testes de autorização demonstrando que a alteração da identidade na requisição não permite acesso a processos de outro promotor; registros das requisições realizadas pelo Backend. | BAIXO |
| RI03 | Reduzir | Validar, além da assinatura e do emissor do token, a audiência (audience) e o papel/perfil do usuário antes de criar a sessão, permitindo acesso às funcionalidades somente para usuários autorizados como promotores. | Protect | Equipe de desenvolvimento | Testes com tokens válidos, porém destinados a outro sistema ou pertencentes a usuários sem o perfil de promotor, demonstrando que o acesso é negado. | BAIXO |
| RI04 | Reduzir | Exigir autenticação de serviço no endpoint utilizado pelo Módulo I.A. para atualização do status da transcrição e validar a origem, o identificador da transcrição e o caminho do arquivo antes de atualizar o registro. | Protect | Equipe de desenvolvimento | Testes de autenticação do endpoint e testes demonstrando que requisições não autenticadas ou com caminhos arbitrários são rejeitadas. | BAIXO |

---

### Ordem inicial de implementação

**Descrição**: Precisamos de uma tabela que descreva qual será a ordem de implemetação das estratégias de maneira resumida. Olhar como exemplo o repositório (https://github.com/camillabdt/VitaLink/blob/main/docs/etapa2-riscos-e-tratamento.md).

---

### Conclusão

Fazer uma consideração final das estratégias que levantamos para o tratamento dos riscos.

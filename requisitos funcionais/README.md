## Casos de Uso

Cada requisito funcional listado em [REQUISITOS.md](../REQUISITOS.md) tem um diagrama de sequência em um arquivo próprio nesta pasta. Os diagramas mostram as interações entre os componentes descritos em [SERVICO.md](../SERVICO.md), pois cada interação e cada fronteira entre componentes é um ponto candidato a ameaça na [modelagem STRIDE](../MODELAGEM_AMEACAS.md).

---

### 1. Convenções

**Participantes dos diagramas**

| Participante           | Papel                                                                                                                                                 |
|------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------|
| Promotor               | Ator primário (o Usuário em SERVICO.md). Membro da promotoria autenticado no sistema                                                                  |
| Backend                | Recebe as requisições do promotor, valida sessão e permissões, orquestra os demais componentes                                                        |
| Provedor de Identidade | Serviço de autenticação institucional do Ministério Público. Valida credenciais e emite o token de identidade                                         |
| Sistema Processos      | Sistema do Estado que detém os dados dos processos e as mídias originais. O Backend consome sua API diretamente (componente Integração em SERVICO.md) |
| Storage                | Armazena a cópia da mídia e a transcrição gerada                                                                                                      |
| Fila                   | Desacopla a solicitação do processamento assíncrono (componente Enfileiramento em SERVICO.md)                                                         |
| Módulo I.A             | Consome a fila, envia a mídia para a I.A e persiste o resultado                                                                                       |
| I.A                    | Serviço externo de I.A acessado via API, usado para transcrição e chat                                                                                |

**Registro de transcrições.** O Backend mantém um registro com o identificador de cada transcrição, o promotor solicitante, o processo, a mídia, o caminho no Storage, os hashes da mídia e do texto (RQNF6.1, RQNF6.3) e o status. Nos diagramas, operações internas do Backend, como as sobre esse registro e a validação de sessão, aparecem como mensagens do Backend para ele mesmo. O high-level design em SERVICO.md ainda não possui um componente de persistência para esse registro.

**Status de uma transcrição.** Os valores possíveis são `na fila`, `em processamento`, `finalizada` e `falha`. As transições ocorrem em RQF3 e RQF4.

**Validação de sessão.** Em toda requisição do promotor, o Backend valida o token de sessão emitido em RQF1. Quando a validação falha, o Backend responde com erro de sessão inválida e o fluxo termina.

**Premissas adotadas**

1. O promotor não faz upload de arquivos. Ele escolhe uma mídia já existente no Sistema Processos e o Backend transfere uma cópia para o Storage. O limite de 80MB em RQNF3.1 se aplica a essa transferência.
2. O Módulo I.A informa o Backend sobre mudanças de status da transcrição.
3. No chat, o Backend chama a I.A diretamente, sem passar pelo Módulo I.A nem pela fila.
4. O Backend gera o arquivo .docx sob demanda a partir da transcrição armazenada no Storage.
5. As mídias vêm do Sistema Processos. O Backend as acessa pela API desse sistema, com três operações: consultar processo e listar mídias, consultar metadados de uma mídia e buscar o arquivo de uma mídia. O serviço confia na decisão de autorização por processo que essa API retorna. Não confia, porém, no conteúdo do arquivo recebido: o Backend valida formato, tamanho declarado e ausência de conteúdo malicioso antes de copiar a mídia para o Storage.
6. O Backend apenas valida o token de identidade emitido pelo provedor de identidade e cria a sessão local. O papel do provedor e o que fica fora do escopo deste serviço estão em SERVICO.md.

---

### 2. Índice

| Requisito | Caso de uso |
| --- | --- |
| RQF1 | [Acesso com credenciais](RQF1-acesso-com-credenciais.md) |
| RQF2 | [Pesquisa de mídias de um processo](RQF2-pesquisa-de-midias-de-um-processo.md) |
| RQF3 | [Solicitação de transcrição de mídia](RQF3-solicitacao-de-transcricao-de-midia.md) |
| RQF4 | [Acompanhamento do status da transcrição](RQF4-acompanhamento-do-status-da-transcricao.md) |
| RQF5 | [Listagem de transcrições por processo](RQF5-listagem-de-transcricoes-por-processo.md) |
| RQF6 | [Acesso a transcrição finalizada](RQF6-acesso-a-transcricao-finalizada.md) |
| RQF7 | [Seleção de transcrição como contexto para a I.A](RQF7-selecao-de-transcricao-como-contexto-para-a-ia.md) |
| RQF8 | [Download da transcrição em .docx](RQF8-download-da-transcricao-em-docx.md) |
| RQF9 | [Exclusão de transcrição](RQF9-exclusao-de-transcricao.md) |
| RQF10 | [Interação com a I.A](RQF10-interacao-com-a-ia.md) |
| RQF11 | [Encerramento de sessão](RQF11-encerramento-de-sessao.md) |

---

### 3. Telas de referência

Mockups de baixa fidelidade, para referência, não o design final. A variante `<tela>--RQFn.png` destaca em azul o elemento do requisito na tela.

| Tela | Nome                                                                          | Requisitos       |
|------|-------------------------------------------------------------------------------|------------------|
| T1   | [Entrada](../resources/mockups/T1-entrada.png)                                | RQF1             |
| T2   | [Processo](../resources/mockups/T2-processo.png)                              | RQF2             |
| T2b  | [Transcrição solicitada](../resources/mockups/T2b-transcricao-solicitada.png) | RQF3             |
| T3   | [Minhas transcrições](../resources/mockups/T3-minhas-transcricoes.png)        | RQF4, RQF5       |
| T3b  | [Confirmar exclusão](../resources/mockups/T3b-confirmar-exclusao.png)         | RQF9             |
| T4   | [Transcrição](../resources/mockups/T4-transcricao.png)                        | RQF6, RQF7, RQF8 |
| T5   | [Chat com a I.A](../resources/mockups/T5-chat.png)                            | RQF7, RQF10      |
| T6   | [Sessão encerrada](../resources/mockups/T6-sessao-encerrada.png)              | RQF11            |

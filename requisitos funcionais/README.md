## Casos de Uso

Cada requisito funcional listado em [REQUISITOS.md](../REQUISITOS.md) tem um diagrama de sequência em um arquivo próprio nesta pasta. Este documento reúne as convenções comuns a todos os diagramas e o índice. Os diagramas mostram as interações entre os componentes descritos em [SERVICO.md](../SERVICO.md), pois cada interação e cada fronteira entre componentes é um ponto candidato a ameaça na [modelagem STRIDE](../MODELAGEM_AMEACAS.md).

---

### 1. Convenções

**Participantes dos diagramas**

| Participante           | Componente em SERVICO.md | Papel                                                                                                                                                 |
|------------------------|--------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------|
| Promotor               | Usuário                  | Ator primário. Membro da promotoria autenticado no sistema                                                                                            |
| Backend                | Backend                  | Recebe as requisições do promotor, valida sessão e permissões, orquestra os demais componentes                                                        |
| Provedor de Identidade | Provedor de Identidade   | Serviço de autenticação institucional do Ministério Público. Valida credenciais e emite o token de identidade                                         |
| Sistema Processos      | Sistema Processos        | Sistema do Estado que detém os dados dos processos e as mídias originais. O Backend consome sua API diretamente (componente Integração em SERVICO.md) |
| Storage                | Storage                  | Armazena a cópia da mídia e a transcrição gerada                                                                                                      |
| Fila                   | Enfileiramento           | Desacopla a solicitação do processamento assíncrono                                                                                                   |
| Módulo I.A             | Módulo I.A               | Consome a fila, envia a mídia para a I.A e persiste o resultado                                                                                       |
| I.A                    | I.A                      | Serviço externo de I.A acessado via API, usado para transcrição e chat                                                                                |

**Registro de transcrições.** O Backend mantém um registro com o identificador de cada transcrição, o promotor solicitante, o processo, a mídia, o caminho no Storage e o status. Nos diagramas, operações sobre esse registro aparecem como mensagens do Backend para ele mesmo. O high-level design em SERVICO.md ainda não possui um componente de persistência para esse registro.

**Status de uma transcrição.** Os valores possíveis são `na fila`, `em processamento`, `finalizada` e `falha`. As transições ocorrem em RQF3 e RQF4.

**Validação de sessão.** Em toda requisição do promotor, o Backend valida o token de sessão emitido em RQF1. Quando a validação falha, o Backend responde com erro de sessão inválida e o fluxo termina. Essa etapa aparece nos diagramas como uma mensagem do Backend para ele mesmo.

**Premissas adotadas**

1. O promotor não faz upload de arquivos. Ele escolhe uma mídia já existente no Sistema Processos e o Backend transfere uma cópia para o Storage. O limite de 80MB em RQNF3.1 se aplica a essa transferência.
2. O Módulo I.A informa o Backend sobre mudanças de status da transcrição.
3. No chat, o Backend chama a I.A diretamente, sem passar pelo Módulo I.A nem pela fila.
4. O Backend gera o arquivo .docx sob demanda a partir da transcrição armazenada no Storage.
5. As mídias vêm do Sistema Processos, sistema externo do Estado. Como elas entram lá é responsabilidade de terceiros e está fora do escopo deste serviço. O acesso acontece somente pela API do Sistema Processos, que o Backend chama diretamente, com três operações: consultar processo e listar mídias, consultar metadados de uma mídia e buscar o arquivo de uma mídia. O serviço nunca escreve no Sistema Processos e confia na decisão de autorização por processo que esse sistema retorna. Não confia, porém, no conteúdo do arquivo recebido: o Backend valida formato, tamanho declarado e ausência de conteúdo malicioso antes de copiar a mídia para o Storage.
6. A identidade do promotor é institucional: ele se autentica no provedor de identidade do Ministério Público (SSO). Este serviço não possui cadastro próprio de usuários, não permite auto-cadastro e não gerencia senhas. O cadastro e a desativação de promotores acontecem no provedor de identidade. O Backend apenas valida o token de identidade emitido pelo provedor e cria a sessão local.

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

Mockups de baixa fidelidade, só para o leitor visualizar cada requisito. Não são o design final. Cada requisito funcional aponta para pelo menos uma tela. Quando uma tela atende mais de um requisito, o arquivo do requisito usa a variante `<tela>--RQFn.png`, que destaca em azul o elemento daquele requisito. A tabela abaixo lista as imagens base, sem destaque.

| Tela | Nome | Requisitos | Imagem |
| --- | --- | --- | --- |
| T1 | Entrada | RQF1 | [T1-entrada.png](../resources/mockups/T1-entrada.png) |
| T2 | Processo | RQF2 | [T2-processo.png](../resources/mockups/T2-processo.png) |
| T2b | Transcrição solicitada | RQF3 | [T2b-transcricao-solicitada.png](../resources/mockups/T2b-transcricao-solicitada.png) |
| T3 | Minhas transcrições | RQF4, RQF5 | [T3-minhas-transcricoes.png](../resources/mockups/T3-minhas-transcricoes.png) |
| T3b | Confirmar exclusão | RQF9 | [T3b-confirmar-exclusao.png](../resources/mockups/T3b-confirmar-exclusao.png) |
| T4 | Transcrição | RQF6, RQF7, RQF8 | [T4-transcricao.png](../resources/mockups/T4-transcricao.png) |
| T5 | Chat com a I.A | RQF7, RQF10 | [T5-chat.png](../resources/mockups/T5-chat.png) |
| T6 | Sessão encerrada | RQF11 | [T6-sessao-encerrada.png](../resources/mockups/T6-sessao-encerrada.png) |

---

### 4. Manutenção deste documento

- Ao incluir, alterar ou remover um requisito funcional em REQUISITOS.md, criar, alterar ou remover o arquivo correspondente nesta pasta e atualizar a tabela do índice. O nome do arquivo segue o padrão `RQFn-nome-do-caso.md` (minúsculas, sem acentos, hífens) e o heading do arquivo começa com o código do requisito, pois o script de renderização usa esse heading para nomear o PNG.
- `npm run diagramas` gera os PNGs em `resources/diagramas/` a partir dos arquivos desta pasta (requer Node.js e, na primeira vez, `npm install`). Regenerar após qualquer alteração em um diagrama. `--classic` desliga o estilo sketch. Cada arquivo exibe o PNG e mantém o código mermaid em um bloco recolhível, que é a fonte do diagrama.
- As telas vivem em `mockups/*.html` com o estilo em `mockups/estilo.css`. `npm run mockups` gera os PNGs em `resources/mockups/`. Regenerar após qualquer alteração em uma tela. Os elementos de cada requisito são marcados no HTML com `data-rqf="RQFn"`, e o script gera uma variante destacada por código encontrado. Ao criar uma tela, adicionar a linha na tabela da seção 3 e a referência no arquivo do requisito.
- Ao alterar a tabela de componentes em SERVICO.md, atualizar a tabela de participantes na seção 1 e os nomes usados nos diagramas.
- Ao resolver uma premissa da seção 1, remover a premissa e ajustar os diagramas afetados.
- Cada ameaça em MODELAGEM_AMEACAS.md deve apontar para um RQF deste documento. Se uma ameaça não encontrar interação correspondente em nenhum diagrama, o diagrama está incompleto.

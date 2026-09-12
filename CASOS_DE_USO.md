## Casos de Uso

Este documento expande cada requisito funcional listado em [REQUISITOS.md](REQUISITOS.md) em um diagrama de sequência. O objetivo é tornar explícitas as interações entre os componentes descritos em [SERVICO.md](SERVICO.md), pois cada interação e cada fronteira entre componentes é um ponto candidato a ameaça na [modelagem STRIDE](MODELAGEM_AMEACAS.md).

---

### 1. Convenções

**Participantes dos diagramas**

| Participante      | Componente em SERVICO.md | Papel                                                                                          |
|-------------------|--------------------------|------------------------------------------------------------------------------------------------|
| Promotor          | Usuário                  | Ator primário. Membro da promotoria autenticado no sistema                                     |
| Backend           | Backend                  | Recebe as requisições do promotor, valida sessão e permissões, orquestra os demais componentes |
| Integração        | Integração               | API que intermedeia o acesso ao Sistema Processos                                              |
| Sistema Processos | Sistema Processos        | Sistema do Estado que detém os dados dos processos e as mídias originais                       |
| Storage           | Storage                  | Armazena a cópia da mídia e a transcrição gerada                                               |
| Fila              | Enfileiramento           | Desacopla a solicitação do processamento assíncrono                                            |
| Módulo I.A        | Módulo I.A               | Consome a fila, envia a mídia para a I.A e persiste o resultado                                |
| I.A               | I.A                      | Serviço externo de I.A acessado via API, usado para transcrição e chat                         |

**Registro de transcrições.** O Backend mantém um registro com o identificador de cada transcrição, o promotor solicitante, o processo, a mídia, o caminho no Storage e o status. Nos diagramas, operações sobre esse registro aparecem como mensagens do Backend para ele mesmo. O high-level design em SERVICO.md ainda não possui um componente de persistência para esse registro.

**Status de uma transcrição.** Os valores possíveis são `na fila`, `em processamento`, `finalizada` e `falha`. As transições ocorrem em RQF3 e RQF4.

**Validação de sessão.** Toda requisição do promotor ao Backend passa por validação do token de sessão emitido em RQF1. Quando a validação falha, o Backend responde com erro de sessão inválida e o fluxo termina. Essa etapa aparece nos diagramas como uma mensagem do Backend para ele mesmo.

**Premissas adotadas**

1. O promotor não faz upload de arquivos. Ele escolhe uma mídia já existente no Sistema Processos e o Backend transfere uma cópia para o Storage. O limite de 80MB em RQNF3.1 se aplica a essa transferência.
2. O Módulo I.A informa o Backend sobre mudanças de status da transcrição.
3. No chat, o Backend chama a I.A diretamente, sem passar pelo Módulo I.A e sem passar pela fila.
4. O arquivo .docx é gerado pelo Backend sob demanda a partir da transcrição armazenada no Storage.
5. A origem das mídias é o Sistema Processos, sistema externo do Estado. Como as mídias entram lá é responsabilidade de terceiros e está fora do escopo deste serviço. O acesso ocorre somente via Integração, com três operações: consultar processo e listar mídias, consultar metadados de uma mídia e buscar o arquivo de uma mídia. O serviço nunca escreve no Sistema Processos. O serviço confia na decisão de autorização por processo retornada pelo Sistema Processos. O serviço não confia no conteúdo do arquivo recebido: formato, tamanho declarado e ausência de conteúdo malicioso são validados pelo Backend antes da cópia para o Storage.

---

### 2. Índice

| Requisito | Caso de uso                                                                                             |
|-----------|---------------------------------------------------------------------------------------------------------|
| RQF1      | [Acesso com credenciais](#rqf1-acesso-com-credenciais)                                                  |
| RQF2      | [Pesquisa de mídias de um processo](#rqf2-pesquisa-de-mídias-de-um-processo)                            |
| RQF3      | [Solicitação de transcrição de mídia](#rqf3-solicitação-de-transcrição-de-mídia)                        |
| RQF4      | [Acompanhamento do status da transcrição](#rqf4-acompanhamento-do-status-da-transcrição)                |
| RQF5      | [Listagem de transcrições por processo](#rqf5-listagem-de-transcrições-por-processo)                    |
| RQF6      | [Acesso a transcrição finalizada](#rqf6-acesso-a-transcrição-finalizada)                                |
| RQF7      | [Seleção de transcrição como contexto para a I.A](#rqf7-seleção-de-transcrição-como-contexto-para-a-ia) |
| RQF8      | [Download da transcrição em .docx](#rqf8-download-da-transcrição-em-docx)                               |
| RQF9      | [Exclusão de transcrição](#rqf9-exclusão-de-transcrição)                                                |
| RQF10     | [Interação com a I.A](#rqf10-interação-com-a-ia)                                                        |
| RQF11     | [Encerramento de sessão](#rqf11-encerramento-de-sessão)                                                 |

---

### RQF1: Acesso com credenciais

**Ator:** Promotor.

**Pré-condições:** o promotor possui credenciais cadastradas.

**Pós-condições:** em caso de sucesso, existe uma sessão ativa vinculada ao promotor e o token de sessão foi entregue a ele.

```mermaid
sequenceDiagram
    actor P as Promotor
    participant B as Backend
    P->>B: Envia credenciais (login e senha)
    B->>B: Valida credenciais
    alt Credenciais válidas
        B->>B: Cria sessão
        B-->>P: Retorna token de sessão
    else Credenciais inválidas
        B->>B: Registra tentativa falha
        B-->>P: Erro de autenticação com mensagem genérica
    else Conta bloqueada por excesso de tentativas
        B-->>P: Erro de conta bloqueada
    end
```

**Notas:** a mensagem genérica evita revelar se o login existe. O mecanismo de validação (base própria ou provedor de identidade do Estado) ainda não está definido em SERVICO.md.

---

### RQF2: Pesquisa de mídias de um processo

**Ator:** Promotor.

**Pré-condições:** sessão ativa. O promotor conhece o número do processo.

**Pós-condições:** o promotor visualiza a lista de mídias do processo, ou recebe a razão pela qual a lista não pôde ser exibida.

```mermaid
sequenceDiagram
    actor P as Promotor
    participant B as Backend
    participant I as Integração
    participant SP as Sistema Processos
    P->>B: Pesquisa processo (número do processo)
    B->>B: Valida sessão
    B->>I: Consulta processo (número e identidade do promotor)
    I->>SP: Busca dados do processo e lista de mídias
    alt Processo encontrado e promotor autorizado
        SP-->>I: Dados do processo e lista de mídias
        I-->>B: Dados do processo e lista de mídias
        B-->>P: Exibe mídias disponíveis
    else Processo não encontrado
        SP-->>I: Não encontrado
        I-->>B: Não encontrado
        B-->>P: Mensagem de processo inexistente
    else Promotor sem permissão no processo
        SP-->>I: Acesso negado
        I-->>B: Acesso negado
        B-->>P: Mensagem de acesso negado
    end
```

**Notas:** a autorização por processo é decidida pelo Sistema Processos com base na identidade do promotor repassada pelo Backend.

---

### RQF3: Solicitação de transcrição de mídia

**Ator:** Promotor.

**Pré-condições:** sessão ativa. O promotor visualizou a lista de mídias do processo (RQF2).

**Pós-condições:** em caso de sucesso, a mídia está copiada no Storage, existe um registro de transcrição com status `na fila` e uma mensagem foi publicada na fila.

```mermaid
sequenceDiagram
    actor P as Promotor
    participant B as Backend
    participant I as Integração
    participant SP as Sistema Processos
    participant S as Storage
    participant F as Fila
    P->>B: Seleciona mídia para transcrição
    B->>B: Valida sessão e permissão no processo
    alt Mídia já possui transcrição solicitada ou finalizada
        B-->>P: Informa transcrição existente
    else Nova solicitação
        B->>I: Consulta metadados da mídia (tamanho e formato)
        I->>SP: Busca metadados
        SP-->>I: Metadados da mídia
        I-->>B: Metadados da mídia
        alt Mídia acima de 80MB (RQNF3.1)
            B-->>P: Recusa solicitação por tamanho
        else Mídia dentro do limite
            B->>I: Solicita arquivo da mídia
            I->>SP: Busca arquivo
            SP-->>I: Arquivo da mídia
            I-->>B: Arquivo da mídia
            B->>B: Valida formato e tamanho real do arquivo
            B->>S: Upload da mídia
            S-->>B: Confirmação com caminho no Storage
            B->>B: Cria registro com status "na fila"
            B->>F: Publica mensagem (id da transcrição, caminho no Storage, promotor)
            Note over B,F: Fila escolhida pelo tamanho da mídia (RQNF4.4)
            B-->>P: Confirma solicitação e retorna id da transcrição
        end
    end
```

**Notas:** a verificação de tamanho pelos metadados ocorre antes da transferência do arquivo para evitar tráfego desnecessário. A validação após o recebimento cobre o caso em que o arquivo real diverge dos metadados declarados (premissa 5). Se essa validação falhar, o Backend recusa a solicitação e não copia o arquivo para o Storage. O promotor pode solicitar mais de uma transcrição (RQNF4.2), então o fluxo se repete por mídia.

---

### RQF4: Acompanhamento do status da transcrição

**Ator:** Promotor.

**Pré-condições:** existe um registro de transcrição com status `na fila` (RQF3).

**Pós-condições:** o registro termina com status `finalizada` ou `falha`. Em caso de sucesso, a transcrição está salva no Storage.

```mermaid
sequenceDiagram
    actor P as Promotor
    participant B as Backend
    participant F as Fila
    participant M as Módulo I.A
    participant S as Storage
    participant IA as I.A
    par Processamento assíncrono
        M->>F: Consome mensagem
        F-->>M: Mensagem (id da transcrição, caminho no Storage)
        M->>B: Atualiza status "em processamento"
        M->>S: Baixa mídia
        S-->>M: Arquivo da mídia
        M->>IA: Envia mídia para transcrição
        alt Transcrição concluída
            IA-->>M: Texto transcrito
            M->>M: Formata transcrição
            M->>S: Salva transcrição
            S-->>M: Confirmação com caminho no Storage
            M->>B: Atualiza status "finalizada"
        else Falha na I.A ou tempo acima de 1 hora (RQNF3.2)
            M->>B: Atualiza status "falha" com motivo
        end
        M->>F: Confirma consumo da mensagem
    and Acompanhamento pelo promotor
        loop Enquanto status for "na fila" ou "em processamento"
            P->>B: Consulta status (id da transcrição)
            B->>B: Valida sessão e propriedade da transcrição
            B-->>P: Status atual
        end
    end
```

**Notas:** a mensagem só é confirmada na fila após o registro do status final, para que uma queda do Módulo I.A no meio do processamento permita reprocessamento. Uma consulta a transcrição de outro promotor retorna acesso negado.

---

### RQF5: Listagem de transcrições por processo

**Ator:** Promotor.

**Pré-condições:** sessão ativa.

**Pós-condições:** o promotor visualiza suas transcrições agrupadas por processo, com o status de cada uma.

```mermaid
sequenceDiagram
    actor P as Promotor
    participant B as Backend
    participant I as Integração
    participant SP as Sistema Processos
    P->>B: Solicita lista de transcrições
    B->>B: Valida sessão
    B->>B: Busca transcrições do promotor no registro
    alt Nenhuma transcrição
        B-->>P: Lista vazia
    else Existem transcrições
        opt Enriquecer com dados do processo
            B->>I: Consulta dados dos processos (números)
            I->>SP: Busca dados
            SP-->>I: Dados dos processos
            I-->>B: Dados dos processos
        end
        B->>B: Agrupa por processo
        B-->>P: Lista agrupada por processo com status de cada transcrição
    end
```

**Notas:** a lista contém apenas transcrições do próprio promotor. Se a consulta ao Sistema Processos falhar, a lista é exibida somente com os números dos processos.

---

### RQF6: Acesso a transcrição finalizada

**Ator:** Promotor.

**Pré-condições:** sessão ativa. Existe uma transcrição do promotor com status `finalizada`.

**Pós-condições:** o promotor visualiza o conteúdo da transcrição.

```mermaid
sequenceDiagram
    actor P as Promotor
    participant B as Backend
    participant S as Storage
    P->>B: Abre transcrição (id da transcrição)
    B->>B: Valida sessão e propriedade da transcrição
    alt Transcrição não pertence ao promotor
        B-->>P: Acesso negado
    else Status diferente de "finalizada"
        B-->>P: Informa status atual, sem conteúdo
    else Status "finalizada"
        B->>S: Busca arquivo da transcrição
        alt Arquivo encontrado
            S-->>B: Conteúdo da transcrição
            B-->>P: Exibe transcrição
        else Arquivo ausente
            S-->>B: Não encontrado
            B->>B: Atualiza status "falha" com motivo "arquivo ausente"
            B-->>P: Erro, transcrição indisponível
        end
    end
```

**Notas:** um arquivo ausente para uma transcrição com status `finalizada` indica perda ou remoção indevida no Storage e deve gerar evento de auditoria.

---

### RQF7: Seleção de transcrição como contexto para a I.A

**Ator:** Promotor.

**Pré-condições:** sessão ativa. Existe uma transcrição do promotor com status `finalizada`.

**Pós-condições:** existe uma sessão de chat em memória no Backend, vinculada ao promotor, com a transcrição carregada como contexto. A interação em si ocorre em RQF10.

```mermaid
sequenceDiagram
    actor P as Promotor
    participant B as Backend
    participant S as Storage
    P->>B: Seleciona transcrição finalizada como contexto
    B->>B: Valida sessão, propriedade e status "finalizada"
    alt Validação falhou
        B-->>P: Acesso negado ou transcrição não finalizada
    else Validação ok
        B->>S: Busca conteúdo da transcrição
        S-->>B: Conteúdo da transcrição
        B->>B: Cria sessão de chat em memória com a transcrição como contexto (RQNF3.4)
        B-->>P: Chat iniciado, pronto para perguntas
    end
```

**Notas:** a transcrição é passada como contexto automaticamente, sem ação adicional do promotor (RQNF3.4). O contexto vive apenas em memória e é descartado ao encerrar o chat (RQNF3.3).

---

### RQF8: Download da transcrição em .docx

**Ator:** Promotor.

**Pré-condições:** sessão ativa. Existe uma transcrição do promotor com status `finalizada`.

**Pós-condições:** o promotor recebe um arquivo .docx com o conteúdo da transcrição.

```mermaid
sequenceDiagram
    actor P as Promotor
    participant B as Backend
    participant S as Storage
    P->>B: Solicita download em .docx (id da transcrição)
    B->>B: Valida sessão, propriedade e status "finalizada"
    alt Validação falhou
        B-->>P: Acesso negado ou transcrição não finalizada
    else Validação ok
        B->>S: Busca conteúdo da transcrição
        S-->>B: Conteúdo da transcrição
        B->>B: Gera arquivo .docx
        B-->>P: Envia arquivo .docx
    end
```

**Notas:** o arquivo sai do controle do sistema após o download. Esse é o ponto em que dados dos ativos A2, A3 e A5 deixam o perímetro da aplicação.

---

### RQF9: Exclusão de transcrição

**Ator:** Promotor.

**Pré-condições:** sessão ativa. Existe uma transcrição do promotor com status `finalizada` ou `falha`.

**Pós-condições:** em caso de sucesso, a mídia e a transcrição foram removidas do Storage e o registro foi removido. A mídia original permanece no Sistema Processos.

```mermaid
sequenceDiagram
    actor P as Promotor
    participant B as Backend
    participant S as Storage
    P->>B: Solicita exclusão (id da transcrição)
    B->>B: Valida sessão e propriedade da transcrição
    alt Transcrição não pertence ao promotor
        B-->>P: Acesso negado
    else Status "na fila" ou "em processamento"
        B-->>P: Exclusão não permitida durante o processamento
    else Status "finalizada" ou "falha"
        B-->>P: Pede confirmação
        P->>B: Confirma exclusão
        B->>S: Remove mídia e transcrição
        alt Remoção concluída
            S-->>B: Confirmação
            B->>B: Remove registro da transcrição
            B-->>P: Transcrição excluída
        else Falha no Storage
            S-->>B: Erro
            B-->>P: Falha na exclusão, registro mantido
        end
    end
```

**Notas:** o registro só é removido após a confirmação do Storage, para não deixar arquivos órfãos com dados sensíveis.

---

### RQF10: Interação com a I.A

**Ator:** Promotor.

**Pré-condições:** existe uma sessão de chat criada em RQF7.

**Pós-condições:** ao encerrar, o contexto e o histórico do chat foram descartados (RQNF3.3).

```mermaid
sequenceDiagram
    actor P as Promotor
    participant B as Backend
    participant IA as I.A
    loop Para cada pergunta
        P->>B: Envia pergunta
        B->>B: Valida sessão e recupera contexto do chat
        B->>IA: Envia transcrição como contexto, histórico e pergunta
        alt I.A respondeu
            IA-->>B: Resposta
            B->>B: Acrescenta pergunta e resposta ao histórico em memória
            B-->>P: Exibe resposta
        else Falha ou timeout na I.A
            IA-->>B: Erro
            B-->>P: Informa falha e permite tentar novamente
        end
    end
    P->>B: Encerra chat
    B->>B: Descarta contexto e histórico (RQNF3.3)
    B-->>P: Chat encerrado
```

**Notas:** a transcrição inteira e as perguntas do promotor trafegam para a I.A externa a cada mensagem. Esse é o principal ponto de exposição do ativo A6.

---

### RQF11: Encerramento de sessão

**Ator:** Promotor.

**Pré-condições:** sessão ativa.

**Pós-condições:** o token de sessão está inválido e qualquer chat ativo foi descartado.

```mermaid
sequenceDiagram
    actor P as Promotor
    participant B as Backend
    P->>B: Solicita encerramento da sessão
    B->>B: Invalida token de sessão
    opt Existe chat ativo
        B->>B: Descarta contexto e histórico do chat (RQNF3.3)
    end
    B-->>P: Sessão encerrada
    Note over B: A sessão também expira por inatividade
```

**Notas:** o tempo de expiração por inatividade ainda não está definido nos requisitos não funcionais.

---

### 3. Manutenção deste documento

- Ao incluir, alterar ou remover um requisito funcional em REQUISITOS.md, atualizar a seção correspondente aqui e a tabela do índice.
- Ao alterar a tabela de componentes em SERVICO.md, atualizar a tabela de participantes na seção 1 e os nomes usados nos diagramas.
- Ao resolver uma premissa da seção 1, remover a premissa e ajustar os diagramas afetados.
- Cada ameaça em MODELAGEM_AMEACAS.md deve apontar para um RQF deste documento. Se uma ameaça não encontrar interação correspondente em nenhum diagrama, o diagrama está incompleto.

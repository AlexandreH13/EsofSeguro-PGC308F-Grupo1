### RQF3: Solicitação de transcrição de mídia

Convenções, participantes e premissas estão no [README](README.md) desta pasta.

**Pré-condições:** sessão ativa. O promotor visualizou a lista de mídias do processo (RQF2).

**Pós-condições:** em caso de sucesso, a mídia está copiada no Storage, existe um registro de transcrição com status `na fila` e uma mensagem foi publicada na fila.

**Tela de referência:** T2b Transcrição solicitada.

![T2b Transcrição solicitada](../../resources/mockups/T2b-transcricao-solicitada--RQF3.png)

![RQF3](../../resources/diagramas/RQF3.png)

<details>
<summary>Código mermaid</summary>

```mermaid
sequenceDiagram
    actor P as Promotor
    participant B as Backend
    participant SP as Sistema Processos
    participant S as Storage
    participant F as Fila
    P->>B: Seleciona mídia para transcrição
    B->>B: Valida sessão e permissão no processo
    alt Mídia já possui transcrição solicitada ou finalizada
        B-->>P: Informa transcrição existente
    else Nova solicitação
        B->>SP: Consulta metadados da mídia (tamanho e formato)
        SP-->>B: Metadados da mídia
        alt Mídia acima de 80MB (RQNF3.1)
            B-->>P: Recusa solicitação por tamanho
        else Mídia dentro do limite
            B->>SP: Solicita arquivo da mídia
            SP-->>B: Arquivo da mídia
            B->>B: Valida formato, tamanho real e antimalware (RQNF6.5)
            B->>B: Calcula hash SHA-256 da mídia (RQNF6.1)
            B->>S: Upload da mídia
            S-->>B: Confirmação com caminho no Storage
            B->>B: Cria registro com status "na fila" e o hash
            B->>F: Publica mensagem (id da transcrição, caminho no Storage, hash, promotor)
            Note over B,F: Fila escolhida pelo tamanho da mídia (RQNF4.4)
            alt Fila confirmou
                B-->>P: Confirma solicitação e retorna id da transcrição
            else Publicação falhou após 3 tentativas (RQNF5.4)
                B->>S: Remove a cópia da mídia
                B->>B: Atualiza status "falha" com motivo
                B-->>P: Informa falha na solicitação
            end
        end
    end
```

</details>

**Notas:** verificar o tamanho pelos metadados antes de transferir evita tráfego desnecessário. A validação após o recebimento cobre o caso em que o arquivo real diverge dos metadados declarados (premissa 5). Se ela falhar, o Backend recusa a solicitação e não copia o arquivo para o Storage. O promotor pode solicitar mais de uma transcrição (RQNF4.2), então o fluxo se repete por mídia.

### RQF2: Pesquisa de mídias de um processo

Convenções, participantes e premissas estão no [README](README.md) desta pasta.

**Pré-condições:** sessão ativa. O promotor conhece o número do processo.

**Pós-condições:** em caso de sucesso, o promotor tem a lista de mídias do processo.

**Tela de referência:** T2 Processo.

![T2 Processo](../../resources/mockups/T2-processo--RQF2.png)

![RQF2](../../resources/diagramas/RQF2.png)

<details>
<summary>Código mermaid</summary>

```mermaid
sequenceDiagram
    actor P as Promotor
    participant B as Backend
    participant SP as Sistema Processos
    P->>B: Pesquisa processo (número do processo)
    B->>B: Valida sessão
    B->>SP: Consulta processo (número e identidade do promotor)
    alt Processo encontrado e promotor autorizado
        SP-->>B: Dados do processo e lista de mídias
        B-->>P: Exibe mídias disponíveis
    else Processo não encontrado
        SP-->>B: Não encontrado
        B-->>P: Mensagem de processo inexistente
    else Promotor sem permissão no processo
        SP-->>B: Acesso negado
        B-->>P: Mensagem de acesso negado
    end
```

</details>

**Notas:** o Sistema Processos decide a autorização por processo com base na identidade do promotor repassada pelo Backend.

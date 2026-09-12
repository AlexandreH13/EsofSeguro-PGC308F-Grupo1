### RQF5: Listagem de transcrições por processo

Convenções, participantes e premissas estão no [README](README.md) desta pasta.

**Ator:** Promotor.

**Pré-condições:** sessão ativa.

**Pós-condições:** o promotor visualiza suas transcrições agrupadas por processo, com o status de cada uma.

**Tela de referência:** T3 Minhas transcrições.

![T3 Minhas transcrições](../resources/mockups/T3-minhas-transcricoes--RQF5.png)

![RQF5](../resources/diagramas/RQF5.png)

<details>
<summary>Código mermaid</summary>

```mermaid
sequenceDiagram
    actor P as Promotor
    participant B as Backend
    participant SP as Sistema Processos
    P->>B: Solicita lista de transcrições
    B->>B: Valida sessão
    B->>B: Busca transcrições do promotor no registro
    alt Nenhuma transcrição
        B-->>P: Lista vazia
    else Existem transcrições
        opt Enriquecer com dados do processo
            B->>SP: Consulta dados dos processos (números)
            SP-->>B: Dados dos processos
        end
        B->>B: Agrupa por processo
        B-->>P: Lista agrupada por processo com status de cada transcrição
    end
```

</details>

**Notas:** a lista contém apenas transcrições do próprio promotor. Se a consulta ao Sistema Processos falhar, a lista mostra somente os números dos processos.

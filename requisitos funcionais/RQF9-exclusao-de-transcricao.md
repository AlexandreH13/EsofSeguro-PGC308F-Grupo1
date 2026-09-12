### RQF9: Exclusão de transcrição

Convenções, participantes e premissas estão no [README](README.md) desta pasta.

**Ator:** Promotor.

**Pré-condições:** sessão ativa. Existe uma transcrição do promotor com status `finalizada` ou `falha`.

**Pós-condições:** em caso de sucesso, o Backend removeu a mídia e a transcrição do Storage e apagou o registro. A mídia original permanece no Sistema Processos.

**Tela de referência:** T3b Confirmar exclusão.

![T3b Confirmar exclusão](../resources/mockups/T3b-confirmar-exclusao--RQF9.png)

![RQF9](../resources/diagramas/RQF9.png)

<details>
<summary>Código mermaid</summary>

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

</details>

**Notas:** o Backend só remove o registro após a confirmação do Storage, para não deixar arquivos órfãos com dados sensíveis.

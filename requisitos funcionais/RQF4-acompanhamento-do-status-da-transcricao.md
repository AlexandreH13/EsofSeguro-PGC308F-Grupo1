### RQF4: Acompanhamento do status da transcrição

Convenções, participantes e premissas estão no [README](README.md) desta pasta.

**Pré-condições:** existe um registro de transcrição com status `na fila` (RQF3).

**Pós-condições:** o registro termina com status `finalizada` ou `falha`. Em caso de sucesso, a transcrição está salva no Storage.

**Tela de referência:** T3 Minhas transcrições.

![T3 Minhas transcrições](../resources/mockups/T3-minhas-transcricoes--RQF4.png)

![RQF4](../resources/diagramas/RQF4.png)

<details>
<summary>Código mermaid</summary>

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
        F-->>M: Mensagem (id da transcrição, caminho no Storage, hash)
        M->>B: Atualiza status "em processamento"
        M->>S: Baixa mídia
        S-->>M: Arquivo da mídia
        M->>M: Recalcula o hash e compara com o da mensagem (RQNF6.1)
        alt Hash diverge
            M->>B: Atualiza status "falha" com motivo "mídia alterada"
        else Hash confere
            loop Até 3 novas tentativas com espera crescente (RQNF5.3)
                M->>IA: Envia mídia para transcrição
            end
            alt Transcrição concluída
                IA-->>M: Texto transcrito
                M->>M: Formata transcrição e calcula hash do texto
                M->>S: Salva transcrição
                S-->>M: Confirmação com caminho no Storage
                M->>B: Atualiza status "finalizada" com o hash do texto
            else Tentativas esgotadas ou tempo acima de 1 hora (RQNF3.2, RQNF5.3)
                M->>B: Atualiza status "falha" com motivo
            end
        end
        M->>F: Confirma consumo da mensagem (RQNF5.1)
    and Acompanhamento pelo promotor
        loop Enquanto status for "na fila" ou "em processamento"
            P->>B: Consulta status (id da transcrição)
            B->>B: Valida sessão e propriedade da transcrição
            B-->>P: Status atual
        end
    end
```

</details>

**Notas:** o Módulo I.A só confirma a mensagem na fila após registrar o status final, para que a mensagem possa ser reprocessada se o módulo cair no meio do processamento. Uma consulta a transcrição de outro promotor retorna acesso negado.

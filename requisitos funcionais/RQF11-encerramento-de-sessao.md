### RQF11: Encerramento de sessão

Convenções, participantes e premissas estão no [README](README.md) desta pasta.

**Ator:** Promotor.

**Pré-condições:** sessão ativa.

**Pós-condições:** o token de sessão está inválido e o Backend descartou qualquer chat ativo.

**Tela de referência:** T6 Sessão encerrada.

![T6 Sessão encerrada](../resources/mockups/T6-sessao-encerrada--RQF11.png)

![RQF11](../resources/diagramas/RQF11.png)

<details>
<summary>Código mermaid</summary>

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

</details>

**Notas:** o tempo de expiração por inatividade ainda não está definido nos requisitos não funcionais. O encerramento invalida apenas a sessão deste serviço. A sessão no provedor de identidade permanece, pois single logout não está previsto.

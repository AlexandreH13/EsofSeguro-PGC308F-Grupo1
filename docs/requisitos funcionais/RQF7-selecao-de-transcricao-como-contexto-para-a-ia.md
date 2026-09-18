### RQF7: Seleção de transcrição como contexto para a I.A

Convenções, participantes e premissas estão no [README](README.md) desta pasta.

**Pré-condições:** sessão ativa. Existe uma transcrição do promotor com status `finalizada`.

**Pós-condições:** existe uma sessão de chat em memória no Backend, vinculada ao promotor, com a transcrição carregada como contexto. A interação em si ocorre em RQF10.

**Telas de referência:** T4 Transcrição e T5 Chat com a I.A.

![T4 Transcrição](../../resources/mockups/T4-transcricao--RQF7.png)

![T5 Chat com a I.A](../../resources/mockups/T5-chat--RQF7.png)

![RQF7](../../resources/diagramas/RQF7.png)

<details>
<summary>Código mermaid</summary>

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

</details>

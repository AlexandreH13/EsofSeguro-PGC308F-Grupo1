### RQF10: Interação com a I.A

Convenções, participantes e premissas estão no [README](README.md) desta pasta.

**Pré-condições:** existe uma sessão de chat criada em RQF7.

**Pós-condições:** após o encerramento do chat, não resta contexto nem histórico no Backend (RQNF3.3).

**Tela de referência:** T5 Chat com a I.A.

![T5 Chat com a I.A](../resources/mockups/T5-chat--RQF10.png)

![RQF10](../resources/diagramas/RQF10.png)

<details>
<summary>Código mermaid</summary>

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

</details>

**Notas:** a transcrição inteira e as perguntas do promotor trafegam para a I.A externa a cada mensagem. É o principal ponto de exposição do ativo A6.

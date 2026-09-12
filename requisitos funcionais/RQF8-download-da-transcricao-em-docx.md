### RQF8: Download da transcrição em .docx

Convenções, participantes e premissas estão no [README](README.md) desta pasta.

**Pré-condições:** sessão ativa. Existe uma transcrição do promotor com status `finalizada`.

**Pós-condições:** o promotor recebe um arquivo .docx com o conteúdo da transcrição.

**Tela de referência:** T4 Transcrição.

![T4 Transcrição](../resources/mockups/T4-transcricao--RQF8.png)

![RQF8](../resources/diagramas/RQF8.png)

<details>
<summary>Código mermaid</summary>

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

</details>

**Notas:** o download é o ponto em que dados dos ativos A2, A3 e A5 saem do controle do sistema.

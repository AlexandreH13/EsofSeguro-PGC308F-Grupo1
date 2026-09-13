### RQF6: Acesso a transcrição finalizada

Convenções, participantes e premissas estão no [README](README.md) desta pasta.

**Pré-condições:** sessão ativa. Existe uma transcrição do promotor com status `finalizada`.

**Pós-condições:** o promotor visualiza o conteúdo da transcrição.

**Tela de referência:** T4 Transcrição.

![T4 Transcrição](../resources/mockups/T4-transcricao--RQF6.png)

![RQF6](../resources/diagramas/RQF6.png)

<details>
<summary>Código mermaid</summary>

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
            B->>B: Confere o hash do texto com o registrado (RQNF6.3)
            alt Hash confere
                B-->>P: Exibe transcrição
            else Hash diverge
                B->>B: Atualiza status "falha" com motivo "conteúdo alterado" e gera evento de auditoria
                B-->>P: Erro, transcrição indisponível
            end
        else Arquivo ausente
            S-->>B: Não encontrado
            B->>B: Atualiza status "falha" com motivo "arquivo ausente" e gera evento de auditoria
            B-->>P: Erro, transcrição indisponível
        end
    end
```

</details>

**Notas:** um arquivo ausente para uma transcrição com status `finalizada` indica perda ou remoção indevida no Storage e deve gerar evento de auditoria.

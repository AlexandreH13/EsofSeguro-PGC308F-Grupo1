### RQF1: Acesso com credenciais

Convenções, participantes e premissas estão no [README](README.md) desta pasta.

**Ator:** Promotor.

**Pré-condições:** o promotor está cadastrado no provedor de identidade institucional (premissa 6).

**Pós-condições:** em caso de sucesso, existe uma sessão ativa no Backend vinculada ao promotor, que recebeu o token de sessão.

**Tela de referência:** T1 Entrada.

![T1 Entrada](../resources/mockups/T1-entrada--RQF1.png)

![RQF1](../resources/diagramas/RQF1.png)

<details>
<summary>Código mermaid</summary>

```mermaid
sequenceDiagram
    actor P as Promotor
    participant B as Backend
    participant IdP as Provedor de Identidade
    P->>B: Acessa o sistema
    B-->>P: Redireciona para o Provedor de Identidade
    P->>IdP: Envia credenciais institucionais
    IdP->>IdP: Valida credenciais
    alt Credenciais válidas
        IdP-->>P: Redireciona para o Backend com código de autorização
        P->>B: Entrega código de autorização
        B->>IdP: Troca código por token de identidade
        IdP-->>B: Token de identidade assinado
        B->>B: Valida assinatura, emissor e validade do token
        B->>B: Cria sessão vinculada ao promotor
        B-->>P: Retorna token de sessão
    else Credenciais inválidas
        IdP-->>P: Erro de autenticação com mensagem genérica
    else Conta bloqueada ou promotor sem acesso a este serviço
        IdP-->>P: Erro de acesso
    end
```

</details>

**Notas:** as credenciais nunca passam pelo Backend. Bloqueio por tentativas, política de senha e segundo fator são responsabilidade do provedor de identidade. O Backend confia apenas em tokens assinados pelo provedor e valida a assinatura a cada login. A mensagem genérica evita revelar se o login existe.

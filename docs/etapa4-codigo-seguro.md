## Código Seguro e Testes de Segurança

### Descrição

Este documento mostra como duas decisões da [arquitetura segura](etapa3-arquitetura-segura.md) viram código. As práticas escolhidas são a proteção da sessão pela validação do token de identidade com segundo fator, que cumpre o RS02, e o controle de autorização por propriedade do recurso, que cumpre o RS01. Para cada prática o documento traz o risco e o requisito de origem, os testes de segurança escritos antes da implementação, a implementação, o resultado esperado e o obtido, e as referências da OWASP. O código executável fica em [prototipo](../prototipo): um backend Java com Spring Boot, os testes das duas práticas e uma interface estática derivada dos mockups. O [README do protótipo](../prototipo/README.md) explica como executar, o que é real e o que é simulado.

---

### 1. Escopo do protótipo

O protótipo implementa só o necessário para exercitar e testar as duas práticas: criação e encerramento de sessão (RQF1, RQF11), listagem (RQF5), abertura (RQF6) e exclusão (RQF9) de transcrições. O provedor de identidade é simulado por um emissor de tokens dentro da própria aplicação, que gera um par de chaves RSA a cada início e assina tokens com ou sem o segundo fator. O registro de transcrições é um banco H2 em memória com dados de dois promotores, e o Storage é uma coluna com o texto da transcrição. Fila, Módulo I.A, I.A e Sistema Processos ficam fora. A sessão é um token opaco guardado em memória no Backend e enviado pela interface no cabeçalho `Authorization`. O protótipo sobe com Docker Compose, e a construção da imagem só termina se os testes passarem; os comandos estão no README do protótipo.

Os testes usam JUnit 5 com MockMvc. Cada teste sobe a aplicação inteira, inclusive o filtro de sessão, e chama as rotas HTTP como um cliente faria, por isso o resultado reflete o comportamento real do Backend, não de uma classe isolada. A ordem de trabalho segue o item 21.1 do enunciado: a tabela de testes entra primeiro no documento, depois a classe de teste, que falha, depois a implementação, que a faz passar. Os commits do repositório seguem essa ordem para cada prática.

---

### 2. Prática 1: proteção da sessão com validação do token de identidade e segundo fator

#### 2.1 Risco e requisito

| Item | Referência |
| --- | --- |
| Risco | RI13 (AME13), ALTO: atacante com senha roubada age como promotor em todos os processos dele |
| Requisito de segurança | RS02: o Backend cria a sessão somente a partir de token de identidade que comprove segundo fator, pelo claim `amr` contendo `mfa` (RFC 8176), e recusa qualquer outro token mesmo com assinatura, emissor, audiência e prazo válidos |
| Vulnerabilidade | V02: CWE-308 (autenticação de fator único) |
| Decisão de arquitetura | DA02, seção 4 da Etapa 3 |
| Caso de uso e tela | RQF1, tela T1 |

A conta do promotor é institucional e a senha pode vazar por phishing ou reutilização. Se o Backend aceita qualquer token assinado pelo provedor, uma senha roubada vira sessão válida. A prática escolhida faz o Backend verificar a cadeia inteira do token e, além dela, exigir a prova do segundo fator, para que um token emitido só com senha não abra sessão nenhuma.

#### 2.2 Testes definidos antes da implementação

Pendente.

#### 2.3 Implementação

Pendente.

#### 2.4 Resultado esperado e obtido

Pendente.

#### 2.5 Referências OWASP

- OWASP Multifactor Authentication Cheat Sheet: <https://cheatsheetseries.owasp.org/cheatsheets/Multifactor_Authentication_Cheat_Sheet.html>
- OWASP Session Management Cheat Sheet, sobre entropia e geração do identificador de sessão: <https://cheatsheetseries.owasp.org/cheatsheets/Session_Management_Cheat_Sheet.html>
- OWASP JSON Web Token Cheat Sheet for Java, sobre a conferência do algoritmo e a validação do token: <https://cheatsheetseries.owasp.org/cheatsheets/JSON_Web_Token_for_Java_Cheat_Sheet.html>
- OWASP ASVS 5.0, V6 Authentication (6.3.3) e V10 OAuth and OIDC (10.3.4): <https://github.com/OWASP/ASVS/blob/master/5.0/en/0x15-V6-Authentication.md> e <https://github.com/OWASP/ASVS/blob/master/5.0/en/0x19-V10-OAuth-and-OIDC.md>
- RFC 8176, Authentication Method Reference Values: <https://www.rfc-editor.org/rfc/rfc8176>
- CWE-308, já citado na Etapa 3

---

### 3. Prática 2: controle de autorização por propriedade do recurso

#### 3.1 Risco e requisito

| Item | Referência |
| --- | --- |
| Risco | RI01 (AME1), MUITO ALTO: promotor troca o id e lê ou apaga transcrição de colega |
| Requisito de segurança | RS01: toda rota que recebe um id de transcrição confere que o recurso pertence ao promotor da sessão antes de executar a operação, e os ids são UUID v4 |
| Vulnerabilidade | V01: CWE-639 (autorização contornada por chave controlada pelo usuário) e CWE-340 (ids previsíveis) |
| Decisão de arquitetura | DA01, seção 4 da Etapa 3 |
| Casos de uso e telas | RQF6 (abrir) e RQF9 (excluir), telas T3 e T4 |

O problema típico está em carregar o registro pelo id e só depois, em cada rota, lembrar de comparar o dono com a sessão. Basta uma rota sem a comparação para o id de um colega abrir, baixar ou apagar a transcrição dele. A prática escolhida tira a decisão do controller: a consulta ao registro já recebe o promotor da sessão, então não existe caminho de código que carregue um registro alheio.

#### 3.2 Testes definidos antes da implementação

Cenário comum: promotor A tem sessão válida; o registro contém uma transcrição de A e uma de B, ambas finalizadas.

| Teste | Entrada ou ação | Resultado esperado |
| --- | --- | --- |
| TS04 (malicioso) | A chama `GET` e `DELETE` em `/api/transcricoes/{id}` com o id da transcrição de B. A também chama `GET` com um id inexistente | As três chamadas respondem 403 com o corpo `{"erro":"acesso negado"}`, idêntico para id alheio e id inexistente. O corpo não traz mídia, processo nem texto de B. O registro de B continua existindo. Cada tentativa gera um alerta `ACESSO_NEGADO` com o promotor da sessão e o id, sem nenhum conteúdo da transcrição |
| TS05 (válido) | A chama `GET` e `DELETE` com o id da própria transcrição | `GET` responde 200 com o texto. `DELETE` responde 204 e o registro deixa de existir. Nenhum alerta é gerado |
| TS06 (ids) | Duas transcrições são criadas em sequência | Os dois ids são UUID versão 4 e diferentes entre si |

Os testes estão em [AutorizacaoTranscricaoTest.java](../prototipo/src/test/java/br/grupo1/transcricao/pratica2/AutorizacaoTranscricaoTest.java), um método por linha da tabela e por operação. A classe entrou no repositório antes do controller e chama as rotas por URL, por isso compila e falha enquanto a implementação não existe.

#### 3.3 Implementação

Pendente.

#### 3.4 Resultado esperado e obtido

Pendente.

#### 3.5 Referências OWASP

- OWASP Authorization Cheat Sheet, itens "Enforce Authorization Checks on Static Resources" e "Ensure Lookup IDs are Not Accessible Even When Guessed or Cannot Be Tampered With": <https://cheatsheetseries.owasp.org/cheatsheets/Authorization_Cheat_Sheet.html>
- OWASP Insecure Direct Object Reference Prevention Cheat Sheet: <https://cheatsheetseries.owasp.org/cheatsheets/Insecure_Direct_Object_Reference_Prevention_Cheat_Sheet.html>
- OWASP API Security Top 10 2023, API1 Broken Object Level Authorization: <https://owasp.org/API-Security/editions/2023/en/0xa1-broken-object-level-authorization/>
- OWASP ASVS 5.0, V8 Authorization, 8.2.2 e 8.3.1: <https://github.com/OWASP/ASVS/blob/master/5.0/en/0x17-V8-Authorization.md>
- OWASP Logging Cheat Sheet, sobre não registrar dados sensíveis nos eventos: <https://cheatsheetseries.owasp.org/cheatsheets/Logging_Cheat_Sheet.html>
- CWE-639 e CWE-340, já citados na Etapa 3

---

### 4. Rastreabilidade

| Prática | Risco | Requisito | Vulnerabilidade | Decisão | Testes | Código principal |
| --- | --- | --- | --- | --- | --- | --- |
| 1. Sessão só com segundo fator | RI13 | RS02 | V02 | DA02 | TS01 a TS03 | `TokenIdentidadeValidador`, `SessaoController`, `SessaoStore`, `IdpConfig` |
| 2. Autorização por propriedade do recurso | RI01 | RS01 | V01 | DA01 | TS04 a TS06 | `TranscricaoRepository`, `TranscricaoController`, `Auditoria`, `SessaoFilter` |

Para executar os testes e a aplicação com Docker Compose, ver o [README do protótipo](../prototipo/README.md). O item 21.2 do enunciado permite pseudocódigo, mas o grupo optou por código executável para que a Etapa 5 tenha um alvo real para a verificação de vulnerabilidades.

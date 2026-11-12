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

Cenário comum: o teste gera o par de chaves do provedor de identidade, entrega a chave pública ao Backend e assina os tokens com a privada. Todos os tokens têm emissor e sujeito válidos.

| Teste | Entrada ou ação | Resultado esperado |
| --- | --- | --- |
| TS01 (malicioso) | Token assinado com a chave do provedor, com audiência e prazo válidos e `amr` igual a `["pwd"]`, enviado a `POST /api/sessao` | 401 com o corpo `{"erro":"erro de autenticação"}` e sem `tokenSessao`. O número de sessões ativas não muda. Uma rota de dados sem sessão continua respondendo 401 |
| TS02 (válido) | O mesmo token com `amr` igual a `["pwd","mfa"]` | 200 com `tokenSessao` e `promotorId`. `GET /api/transcricoes` com esse token de sessão responde 200 |
| TS03 (contrato) | Três tokens com `amr` contendo `mfa`: um assinado por outra chave, um com audiência de outro serviço e um expirado | 401 nos três, sem `tokenSessao` |

Os testes estão em [SessaoSegundoFatorTest.java](../prototipo/src/test/java/br/grupo1/transcricao/pratica1/SessaoSegundoFatorTest.java). A classe entrou no repositório antes do validador e do controller de sessão.

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

A implementação tem quatro partes, todas em `prototipo/src/main/java/br/grupo1/transcricao`.

O filtro `sessao/SessaoFilter.java` exige sessão válida em toda rota `/api`, exceto a criação da sessão, e entrega o promotor autenticado aos controllers como atributo da requisição. Nenhuma rota de dados executa sem um promotor identificado.

O repositório `transcricao/TranscricaoRepository.java` expõe a consulta `findByIdAndPromotorId(id, promotorId)`. O Spring Data gera o SQL com os dois critérios na cláusula `WHERE`, então o registro de outro promotor nunca é carregado, e o resultado para id alheio é o mesmo de id inexistente: vazio.

O controller `transcricao/TranscricaoController.java` usa essa consulta nas duas rotas que recebem id e trata o vazio de um único jeito:

```java
@GetMapping("/{id}")
public ResponseEntity<?> abrir(@PathVariable("id") UUID id,
                               @RequestAttribute(SessaoFilter.ATRIBUTO_SESSAO) Sessao sessao) {
    return repositorio.findByIdAndPromotorId(id, sessao.promotorId())
            .<ResponseEntity<?>>map(t -> ResponseEntity.ok(Detalhe.de(t)))
            .orElseGet(() -> acessoNegado(sessao, id, "abrir"));
}

private ResponseEntity<Erro> acessoNegado(Sessao sessao, UUID id, String operacao) {
    // Só o id entra no alerta. O conteúdo da transcrição nunca vai para o log.
    auditoria.alerta("ACESSO_NEGADO", sessao.promotorId(), "operação " + operacao + " na transcrição " + id);
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Erro("acesso negado"));
}
```

A resposta é a mesma para id alheio e id inexistente, para a rota não revelar quais ids existem. O alerta vai para `auditoria/Auditoria.java`, que registra no log e guarda em memória para os testes conferirem; entram só identificadores, nunca o conteúdo. A exclusão segue o mesmo caminho e ainda recusa, com 409, transcrições na fila ou em processamento (RQF9).

A entidade `transcricao/Transcricao.java` gera o próprio id com `UUID.randomUUID()`, que produz UUID versão 4 a partir de um gerador criptográfico. Não há sequência no banco, então um id não dá pista sobre os vizinhos.

#### 3.4 Resultado esperado e obtido

O critério de verificação do RS01 pede que, com as sessões de dois promotores, cada rota chamada por um com o id do outro responda acesso negado, não devolva nenhum dado, não altere nem exclua nada e gere um alerta, e que os ids sejam UUID v4 sem relação entre si. TS04 a TS06 cobrem cada um desses pontos.

Antes da implementação, com a classe de teste no repositório e sem o controller, cinco dos seis testes falham porque as rotas não existem. TS06 já passa porque a geração do id está na entidade, que entrou com o modelo:

```
Tests run: 6, Failures: 5, Errors: 0, Skipped: 0 <<< FAILURE! -- in br.grupo1.transcricao.pratica2.AutorizacaoTranscricaoTest
AutorizacaoTranscricaoTest.ts04_abrirTranscricaoDeOutroPromotorEhNegadoSemDevolverDados:67 Status expected:<403> but was:<404>
AutorizacaoTranscricaoTest.ts04_excluirTranscricaoDeOutroPromotorEhNegadoENaoAlteraNada:78 Status expected:<403> but was:<404>
AutorizacaoTranscricaoTest.ts04_idInexistenteRecebeAMesmaRespostaDoIdAlheio:88 Status expected:<403> but was:<404>
AutorizacaoTranscricaoTest.ts05_abrirPropriaTranscricaoDevolveOConteudo:96 Status expected:<200> but was:<404>
AutorizacaoTranscricaoTest.ts05_excluirPropriaTranscricaoRemoveORegistro:105 Status expected:<204> but was:<404>
```

Depois da implementação, os seis passam:

```
Tests run: 6, Failures: 0, Errors: 0, Skipped: 0 -- in br.grupo1.transcricao.pratica2.AutorizacaoTranscricaoTest
```

Na interface, o roteiro do README do protótipo reproduz o TS04 com dois promotores em abas diferentes: a página mostra acesso negado sem nenhum dado e o log do contêiner registra `ALERTA ACESSO_NEGADO promotor=promotor-b operação abrir na transcrição <id>`.

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

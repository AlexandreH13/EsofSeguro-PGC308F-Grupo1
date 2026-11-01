# Protótipo da Etapa 4

Backend em Java 21 com Spring Boot que implementa as duas práticas de código seguro descritas em [docs/etapa4-codigo-seguro.md](../docs/etapa4-codigo-seguro.md). A interface é um conjunto de páginas estáticas, derivadas dos mockups, servidas pelo próprio backend. Nada aqui é o sistema completo: só o suficiente para exercitar e testar as práticas.

## Executar

Precisa só de Docker. Na pasta `prototipo`:

```
docker compose up --build
```

A construção da imagem compila o código e roda os testes; um teste reprovado derruba a construção e nada sobe. A aplicação fica em <http://localhost:8080>, porta do host definida em `compose.yaml`. Se ela estiver ocupada, troque com `PORTA=8081 docker compose up --build`. O log sai em `docker compose logs -f transcricao` e `docker compose down` derruba tudo.

Para rodar só os testes e ver a saída completa:

```
docker compose run --rm testes
```

Uma classe só: `docker compose run --rm testes mvn -B test -Dtest=AutorizacaoTranscricaoTest`. Os testes das práticas ficam em `src/test/java/br/grupo1/transcricao/pratica1` e `pratica2`.

Sem Docker, com Java 21, o Maven Wrapper faz o mesmo: `./mvnw test` e `./mvnw spring-boot:run`. É o caminho para quem usa a IDE.

## O que é real e o que é simulado

| Componente do HLD | No protótipo |
| --- | --- |
| Backend | Spring Boot, com o filtro de sessão e as rotas das práticas |
| Provedor de Identidade | Chega com a prática 1: um provedor simulado, ativo só na demonstração, e a validação do token de identidade |
| Registro de transcrições | H2 em memória, com dados iniciais de dois promotores em `DadosIniciais` |
| Storage | O texto da transcrição fica numa coluna do registro |
| Sistema Processos, Fila, Módulo I.A, I.A | Fora do protótipo. Os status das transcrições são fixos nos dados iniciais |
| Interface | Chega depois das práticas: páginas estáticas derivadas dos mockups, servidas pelo próprio backend |

A sessão é um token opaco de 256 bits guardado em memória no Backend e enviado no cabeçalho `Authorization: Bearer`. Toda rota `/api` sem sessão válida responde 401 com `{"erro":"sessão inválida"}`.

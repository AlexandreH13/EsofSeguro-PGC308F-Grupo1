# Etapa de build: compila e roda os testes. Um teste reprovado derruba a imagem.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B package

# Etapa final: só o JRE e o jar, rodando sem privilégios.
FROM eclipse-temurin:21-jre
RUN useradd --system --uid 1001 --create-home app
WORKDIR /app
COPY --from=build /app/target/transcricao-*.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

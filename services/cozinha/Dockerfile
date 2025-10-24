# Estágio de build
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copiar o pom.xml e baixar dependências (cache layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar o código-fonte e compilar
COPY src ./src
RUN mvn package -DskipTests

# Estágio de execução
FROM eclipse-temurin:17-jre
WORKDIR /app

# Criar usuário não-root
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Copiar o JAR do estágio de build
COPY --from=build /app/target/*.jar app.jar

# Mudar para usuário não-root
USER appuser

# Expor porta
EXPOSE 8080

# Comando de inicialização
ENTRYPOINT ["java", "-jar", "app.jar"]

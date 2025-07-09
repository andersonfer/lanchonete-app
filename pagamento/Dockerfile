# Estágio de build
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copiar o arquivo pom.xml e baixar dependências para aproveitar o cache do Docker
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar o código-fonte e compilar
COPY src ./src
RUN mvn package -DskipTests

# Estágio de execução
FROM eclipse-temurin:17-jre
WORKDIR /app

# Criar um usuário não-root
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Copiar o JAR compilado do estágio de build
COPY --from=build /app/target/*.jar app.jar

# Configurar permissões
RUN chown -R appuser:appuser /app
USER appuser

# Expor a porta da aplicação
EXPOSE 8081

# Configurar variáveis de ambiente para a JVM
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseContainerSupport"

# Executar a aplicação
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
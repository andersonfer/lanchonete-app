FROM eclipse-temurin:17-jre
WORKDIR /app

# Criar um usuário não-root
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Copiar o JAR compilado do host (já compilado pelo script com mvn)
COPY target/*.jar app.jar

# Configurar permissões
RUN chown -R appuser:appuser /app
USER appuser

# Expor a porta da aplicação
EXPOSE 8080

# Configurar variáveis de ambiente para a JVM
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseContainerSupport"

# Executar a aplicação
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

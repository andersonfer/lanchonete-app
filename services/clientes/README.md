# 🧑 Serviço de Clientes

Microserviço responsável pela identificação e cadastro de clientes do sistema de lanchonete.

---

## 📋 Responsabilidades

- Cadastro de novos clientes
- Identificação de clientes por CPF
- Busca de dados de clientes
- Validação de CPF e Email

---

## 🏗️ Arquitetura

### Clean Architecture + Hexagonal Architecture

```
src/main/java/br/com/lanchonete/clientes/
│
├── domain/                          # 🎯 Núcleo - Regras de Negócio
│   ├── model/
│   │   ├── Cliente.java            # Entity com regras de negócio
│   │   ├── Cpf.java                # Value Object
│   │   └── Email.java              # Value Object
│   └── exceptions/
│       ├── ValidacaoException.java
│       └── ClienteNaoEncontradoException.java
│
├── application/                     # 🔧 Casos de Uso
│   ├── usecases/
│   │   ├── CadastrarCliente.java
│   │   ├── IdentificarCliente.java
│   │   └── BuscarClientePorCpf.java
│   └── gateways/
│       └── ClienteGateway.java     # Interface (Port)
│
├── adapters/                        # 🔌 Adaptadores
│   ├── persistence/
│   │   └── ClienteGatewayJDBC.java # Adapter Out (Banco)
│   └── web/
│       ├── controller/
│       │   ├── ClienteController.java
│       │   └── ExceptionHandlerController.java
│       ├── service/
│       │   └── ClienteService.java # Orquestra Use Cases
│       └── dto/
│           ├── ClienteRequest.java
│           ├── ClienteResponse.java
│           ├── IdentificarClienteRequest.java
│           └── ErrorResponse.java
│
└── infrastructure/                  # ⚙️ Configuração
    └── config/
        ├── JdbcConfig.java         # DataSource eager
        └── UseCaseConfig.java      # Beans Use Cases
```

---

## 🚀 Endpoints

### 1. Cadastrar Cliente
```bash
POST /clientes
Content-Type: application/json

{
  "nome": "Maria Silva",
  "cpf": "12345678901",
  "email": "maria.silva@email.com"
}

# Response: 201 Created
{
  "id": 3,
  "nome": "Maria Silva",
  "cpf": "12345678901",
  "email": "maria.silva@email.com"
}
```

### 2. Identificar Cliente por CPF
```bash
POST /clientes/identificar
Content-Type: application/json

{
  "cpf": "12345678901"
}

# Response: 200 OK
{
  "id": 3,
  "nome": "Maria Silva",
  "cpf": "12345678901",
  "email": "maria.silva@email.com"
}
```

### 3. Buscar Cliente por CPF
```bash
GET /clientes/cpf/12345678901

# Response: 200 OK
{
  "id": 3,
  "nome": "Maria Silva",
  "cpf": "12345678901",
  "email": "maria.silva@email.com"
}
```

---

## 💾 Banco de Dados

### MySQL StatefulSet

**Database:** `clientes_db`

**Schema:**
```sql
CREATE TABLE cliente (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    cpf VARCHAR(11) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL,
    INDEX idx_cliente_cpf (cpf)
);
```

**Dados Iniciais:**
```sql
INSERT INTO cliente (nome, cpf, email)
VALUES ('João da Silva', '55555555555', 'joao.silva@lanchonete.com')
ON DUPLICATE KEY UPDATE
    nome = VALUES(nome),
    email = VALUES(email);
```

---

## 🧪 Testes

### Cobertura: 95% ✅

```bash
mvn clean test jacoco:report
```

### Testes Implementados

#### Domain (31 testes)
- ✅ `CpfTest` (9 testes) - Validação de CPF
- ✅ `EmailTest` (11 testes) - Validação de Email
- ✅ `ClienteTest` (11 testes) - Regras de negócio

#### Use Cases (11 testes)
- ✅ `CadastrarClienteTest` (7 testes)
- ✅ `IdentificarClienteTest` (2 testes)
- ✅ `BuscarClientePorCpfTest` (2 testes)

#### Repositories (6 testes)
- ✅ `ClienteRepositoryJdbcTest` (6 testes) - Integração com BD

#### Web Layer (10 testes)
- ✅ `ClienteServiceTest` (3 testes) - Orquestração
- ✅ `ClienteControllerTest` (3 testes) - Endpoints
- ✅ `ExceptionHandlerControllerTest` (3 testes) - Error handling
- ✅ `UseCaseConfigTest` (1 teste) - Configuração Spring

**Total: 58 testes**

---

## 🐳 Docker

### Build
```bash
docker build -t lanchonete-clientes:latest .
```

### Dockerfile (Multi-stage)
- **Stage 1:** Maven build com cache de dependências
- **Stage 2:** Runtime com JRE 17 (sem Maven)
- **User:** appuser (não-root)
- **Port:** 8080
- **Resources:** 256Mi RAM / 250m CPU

---

## ☸️ Kubernetes

### Deploy Local (Minikube)

```bash
# 1. Build e load da imagem
docker build -t lanchonete-clientes:latest .
minikube image load lanchonete-clientes:latest

# 2. Aplicar manifests
kubectl apply -f k8s_manifests/clientes/

# 3. Aguardar pods prontos
kubectl wait --for=condition=ready pod -l app=clientes --timeout=180s

# 4. Port-forward
kubectl port-forward service/clientes-service 8081:8080 &

# 5. Testar endpoints
curl -X POST http://localhost:8081/clientes \
  -H "Content-Type: application/json" \
  -d '{"nome":"Maria Silva","cpf":"12345678901","email":"maria@email.com"}'
```

### Manifests

#### ConfigMap
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: clientes-config
data:
  SPRING_SQL_INIT_MODE: "always"        # ⚠️ Obrigatório
  SPRING_SQL_INIT_PLATFORM: "mysql"     # ⚠️ Obrigatório
  SPRING_PROFILES_ACTIVE: "prod"
```

#### Deployment
- **Replicas:** 2
- **Image:** `lanchonete-clientes:latest`
- **ImagePullPolicy:** `Never` (Minikube) / `Always` (EKS)
- **Resources:**
  - Requests: 250Mi RAM / 100m CPU
  - Limits: 350Mi RAM / 300m CPU
- **Health Checks:** Startup, Readiness, Liveness

#### Service
- **Type:** ClusterIP
- **Port:** 8080

#### HPA (Horizontal Pod Autoscaler)
- **Min:** 2 réplicas
- **Max:** 4 réplicas
- **Target:** 60% CPU

---

## 🔧 Configuração

### application.yml

```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:mysql-clientes-service}:${DB_PORT:3306}/${DB_NAME:clientes}
    driverClassName: com.mysql.cj.jdbc.Driver
    username: ${DB_USERNAME:clientes}
    password: ${DB_PASSWORD}

  sql:
    init:
      mode: always              # Scripts rodam sempre
      platform: mysql           # Prefixo schema-mysql.sql
      schema-locations: classpath:schema-mysql.sql
      data-locations: classpath:data-mysql.sql

management:
  endpoints:
    web:
      exposure:
        include: "health,info,metrics"
  health:
    readinessstate:
      enabled: true
    livenessstate:
      enabled: true
```

### JdbcConfig.java ⚠️ OBRIGATÓRIO

```java
@Configuration
public class JdbcConfig {
    @Bean
    JdbcTemplate jdbcTemplate(final DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
```

**Por que é necessário?**
- Força inicialização **eager** do DataSource
- Sem isso, DataSource só inicializa na primeira requisição HTTP
- Scripts SQL (`schema-mysql.sql`, `data-mysql.sql`) só rodam se DataSource inicializar no startup

---

## 📊 Dependências

```xml
<dependencies>
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jdbc</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- MySQL -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
    </dependency>

    <!-- Actuator (health checks) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>

    <!-- Testes -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>

    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- JaCoCo (cobertura) -->
    <plugin>
        <groupId>org.jacoco</groupId>
        <artifactId>jacoco-maven-plugin</artifactId>
    </plugin>
</dependencies>
```

---

## ⚠️ Troubleshooting

### Scripts SQL não executam

**Problema:** Tabela `cliente` não existe no banco

**Solução:**
1. ✅ Verificar `application.yml` tem `spring.sql.init.mode: always`
2. ✅ Verificar `JdbcConfig.java` existe
3. ✅ Verificar ConfigMap tem `SPRING_SQL_INIT_MODE: always`
4. ✅ Reiniciar pods: `kubectl rollout restart deployment clientes-deployment`

### DataSource inicializa lazy

**Problema:** Scripts SQL só rodam na primeira requisição HTTP

**Causa:** Falta `JdbcConfig.java` com bean `JdbcTemplate`

**Solução:** Criar `JdbcConfig.java` que injeta `DataSource` (força eager initialization)

### Pods com erro CreateContainerConfigError

**Problema:** Secret não encontrado ou chave errada

**Solução:**
```bash
# Verificar secret
kubectl get secret mysql-clientes-secret

# Ver chaves
kubectl get secret mysql-clientes-secret -o json | jq -r '.data | keys'

# Deployment deve usar as chaves corretas:
# MYSQL_USER, MYSQL_PASSWORD, MYSQL_DATABASE
```

---

## 📚 Referências

- [Clean Architecture - Uncle Bob](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
- [Spring Boot Data JDBC](https://spring.io/projects/spring-data-jdbc)
- [Spring Boot SQL Initialization](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.data-initialization)
- [Kubernetes Best Practices](https://kubernetes.io/docs/concepts/configuration/overview/)

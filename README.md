# Lanchonete App - Sistema de Autoatendimento

Sistema de autoatendimento para lanchonete implementado com arquitetura de microserviços, orquestrado em Kubernetes (EKS) com mensageria assíncrona via RabbitMQ.

## Arquitetura

```
                                    Internet
                                        │
                                        ▼
                            ┌───────────────────────┐
                            │   AWS API Gateway     │
                            │   (Cognito Auth)      │
                            └───────────┬───────────┘
                                        │
                    ┌───────────────────┼───────────────────┐
                    │                   │                   │
                    ▼                   ▼                   ▼
            ┌───────────────┐   ┌───────────────┐   ┌───────────────┐
            │    Lambda     │   │  AWS EKS      │   │  AWS Cognito  │
            │  (Auth CPF)   │   │  Cluster      │   │  (User Pool)  │
            └───────────────┘   └───────┬───────┘   └───────────────┘
                                        │
        ┌───────────────────────────────┼───────────────────────────────┐
        │                               │              AWS EKS          │
        │   ┌───────────────────────────┼───────────────────────────┐   │
        │   │                    ALB Ingress                        │   │
        │   │  /clientes/* → Clientes Service (8080)                │   │
        │   │  /pedidos/*  → Pedidos Service (8080)                 │   │
        │   │  /cozinha/*  → Cozinha Service (8080)                 │   │
        │   │  /pagamento/* → Pagamento Service (8081)              │   │
        │   └───────────────────────────┬───────────────────────────┘   │
        │                               │                               │
        │   ┌───────────────────────────┴───────────────────────────┐   │
        │   │                    Microserviços                      │   │
        │   │                                                       │   │
        │   │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐   │   │
        │   │  │  Clientes   │  │   Pedidos   │  │   Cozinha   │   │   │
        │   │  │  (Java 17)  │  │  (Java 17)  │  │  (Java 17)  │   │   │
        │   │  │             │  │             │  │             │   │   │
        │   │  │    RDS      │  │    RDS      │  │    RDS      │   │   │
        │   │  └─────────────┘  └──────┬──────┘  └──────┬──────┘   │   │
        │   │                          │                │          │   │
        │   │                     Feign│           Feign│          │   │
        │   │  ┌─────────────┐        │                │          │   │
        │   │  │  Pagamento  │◄───────┴────────────────┘          │   │
        │   │  │  (Java 17)  │                                    │   │
        │   │  │             │                                    │   │
        │   │  │  MongoDB    │                                    │   │
        │   │  │ (StatefulSet)                                    │   │
        │   │  └──────┬──────┘                                    │   │
        │   │         │                                           │   │
        │   │         ▼                                           │   │
        │   │  ┌─────────────────────────────────────────────┐   │   │
        │   │  │          RabbitMQ (StatefulSet)             │   │   │
        │   │  │                                             │   │   │
        │   │  │  Exchanges:                                 │   │   │
        │   │  │  • pedido.events   (PedidoCriado)           │   │   │
        │   │  │  • pagamento.events (Aprovado/Rejeitado)    │   │   │
        │   │  │  • cozinha.events  (PedidoPronto)           │   │   │
        │   │  └─────────────────────────────────────────────┘   │   │
        │   └───────────────────────────────────────────────────┘   │
        └───────────────────────────────────────────────────────────┘
                                        │
                                        ▼
        ┌───────────────────────────────────────────────────────────┐
        │                       AWS RDS MySQL                       │
        │                                                           │
        │  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────┐ │
        │  │ lanchonete-     │ │ lanchonete-     │ │ lanchonete- │ │
        │  │ clientes-db     │ │ pedidos-db      │ │ cozinha-db  │ │
        │  └─────────────────┘ └─────────────────┘ └─────────────┘ │
        └───────────────────────────────────────────────────────────┘
```

## Microserviços

O sistema é composto por 4 microserviços independentes:

| Serviço | Tecnologia | Banco de Dados | Porta | Responsabilidade |
|---------|------------|----------------|-------|------------------|
| **Clientes** | Spring Boot 3 + Java 17 | MySQL (RDS) | 8080 | Cadastro e identificação de clientes por CPF |
| **Pedidos** | Spring Boot 3 + Java 17 | MySQL (RDS) | 8080 | Checkout, gestão de pedidos e produtos |
| **Cozinha** | Spring Boot 3 + Java 17 | MySQL (RDS) | 8080 | Fila de produção e status de preparo |
| **Pagamento** | Spring Boot 3 + Java 17 | MongoDB (StatefulSet) | 8081 | Processamento de pagamentos (mock 80% aprovação) |

### Comunicação entre Serviços

**REST (Síncrono via OpenFeign):**
- Pedidos → Clientes: Valida cliente por CPF ao criar pedido
- Cozinha → Pedidos: Consulta detalhes do pedido

**RabbitMQ (Assíncrono):**
```
Pedidos                         Pagamento                       Cozinha
   │                               │                               │
   │──── PedidoCriado ────────────►│                               │
   │                               │──── PagamentoAprovado ───────►│
   │◄─── PagamentoAprovado ────────│                               │
   │◄─── PagamentoRejeitado ───────│                               │
   │                               │                               │
   │◄───────────────────────────── PedidoPronto ──────────────────│
   │                               │                               │
   │──── PedidoRetirado ──────────────────────────────────────────►│
```

## Estrutura do Projeto

```
lanchonete-app/
├── services/                    # Microserviços
│   ├── clientes/               # Serviço de clientes
│   │   ├── src/
│   │   ├── Dockerfile
│   │   └── pom.xml
│   ├── pedidos/                # Serviço de pedidos
│   ├── cozinha/                # Serviço de cozinha
│   └── pagamento/              # Serviço de pagamento
│
├── k8s/                        # Manifests Kubernetes
│   ├── base/                   # ConfigMaps e Services
│   │   ├── configmaps/
│   │   └── services/
│   ├── aws/                    # Deployments e StatefulSets
│   │   ├── deployments/
│   │   └── statefulsets/       # MongoDB, RabbitMQ
│   ├── ingress/aws/            # ALB Ingress
│   ├── hpa/                    # Horizontal Pod Autoscaler
│   └── secrets/                # Templates de secrets
│
├── infra/                      # Terraform (IaC)
│   ├── backend/                # S3 + DynamoDB (state)
│   ├── ecr/                    # Container Registry
│   ├── kubernetes/             # EKS Cluster
│   ├── database/               # RDS MySQL
│   ├── auth/                   # Cognito User Pool
│   ├── lambda/                 # Lambda de autenticação
│   └── api-gateway/            # API Gateway
│
├── .github/workflows/          # CI/CD Pipelines
│   ├── ci-*.yml                # Continuous Integration
│   └── cd-*.yml                # Continuous Deployment
│
├── deploy_scripts/aws/         # Scripts de deploy
└── test_scripts/aws/           # Scripts de teste
```

## CI/CD

O projeto utiliza GitHub Actions com pipelines separados para cada microserviço.

### Continuous Integration (CI)

Executado em **Pull Requests** para a branch `main`:

```
Trigger: PR em services/{service}/**

1. Checkout código
2. Setup Java 17
3. Executar testes (mvn test)
4. Análise SonarCloud
5. Verificar Quality Gate
```

### Continuous Deployment (CD)

Executado em **Push** para a branch `main`:

```
Trigger: Push em services/{service}/**

1. Checkout código
2. Setup Java 17
3. Build aplicação (mvn package -DskipTests)
4. Configurar AWS Credentials
5. Login no Amazon ECR
6. Build e Push Docker (tag: SHA + latest)
7. Configurar kubectl (EKS)
8. Deploy Kubernetes
   - ConfigMap
   - Service
   - Deployment (com imagem SHA)
   - Ingress
9. Aguardar rollout
10. Smoke Tests (health checks)
11. Verificar ALB
```

### Pipelines por Serviço

| Serviço | CI Pipeline | CD Pipeline | SonarCloud Project |
|---------|-------------|-------------|---------------------|
| Clientes | `ci-clientes.yml` | `cd-clientes.yml` | andersonfer_lanchonete-clientes |
| Pedidos | `ci-pedidos.yml` | `cd-pedidos.yml` | andersonfer_lanchonete-pedidos |
| Cozinha | `ci-cozinha.yml` | `cd-cozinha.yml` | andersonfer_lanchonete-cozinha |
| Pagamento | `ci-pagamento.yml` | `cd-pagamento.yml` | andersonfer_lanchonete-pagamento |

## Infraestrutura AWS

### Componentes Provisionados via Terraform

| Módulo | Recursos | Descrição |
|--------|----------|-----------|
| `backend/` | S3 Bucket + DynamoDB | Armazenamento do Terraform state |
| `ecr/` | 4 repositórios ECR | Container registry para as imagens |
| `kubernetes/` | EKS Cluster + Node Group | Cluster Kubernetes (t3.medium) |
| `database/` | 3 instâncias RDS MySQL | Bancos para Clientes, Pedidos e Cozinha |
| `auth/` | Cognito User Pool | Autenticação de usuários por CPF |
| `lambda/` | Lambda Java 17 | Handler de autenticação |
| `api-gateway/` | REST API + Authorizer | Gateway com integração Cognito |

### Diagrama de Infraestrutura

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              AWS Cloud (us-east-1)                       │
│                                                                          │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │                        API Gateway (REST)                          │ │
│  │                                                                    │ │
│  │  POST /auth/identificar → Lambda (sem auth)                        │ │
│  │  ANY  /clientes/**     → ALB Clientes (Cognito auth)               │ │
│  │  ANY  /pedidos/**      → ALB Pedidos (Cognito auth)                │ │
│  │  ANY  /cozinha/**      → ALB Cozinha (Cognito auth)                │ │
│  │  ANY  /pagamento/**    → ALB Pagamento (Cognito auth)              │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                                    │                                     │
│  ┌─────────────────┐    ┌─────────┴─────────┐    ┌─────────────────┐   │
│  │  Cognito        │    │  Lambda           │    │  ECR            │   │
│  │  User Pool      │    │  (Auth Handler)   │    │  (4 repos)      │   │
│  │                 │    │                   │    │                 │   │
│  │  - CPF auth     │    │  - Java 17        │    │  - clientes     │   │
│  │  - JWT tokens   │    │  - Valida CPF     │    │  - pedidos      │   │
│  └─────────────────┘    │  - Cria usuário   │    │  - cozinha      │   │
│                         │  - Retorna token  │    │  - pagamento    │   │
│                         └───────────────────┘    └─────────────────┘   │
│                                                                          │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │                    EKS Cluster (lanchonete-cluster)                │ │
│  │                                                                    │ │
│  │  ┌──────────────────────────────────────────────────────────────┐ │ │
│  │  │                    Node Group (t3.medium x 2-3)              │ │ │
│  │  │                                                              │ │ │
│  │  │  ┌────────────┐ ┌────────────┐ ┌────────────┐ ┌───────────┐ │ │ │
│  │  │  │ Clientes   │ │ Pedidos    │ │ Cozinha    │ │ Pagamento │ │ │ │
│  │  │  │ Deployment │ │ Deployment │ │ Deployment │ │ Deployment│ │ │ │
│  │  │  └─────┬──────┘ └─────┬──────┘ └─────┬──────┘ └─────┬─────┘ │ │ │
│  │  │        │              │              │              │       │ │ │
│  │  │        └──────────────┼──────────────┼──────────────┘       │ │ │
│  │  │                       │              │                      │ │ │
│  │  │              ┌────────┴────────┐     │                      │ │ │
│  │  │              │    RabbitMQ     │     │                      │ │ │
│  │  │              │   StatefulSet   │◄────┘                      │ │ │
│  │  │              └─────────────────┘                            │ │ │
│  │  │                                                              │ │ │
│  │  │              ┌─────────────────┐                            │ │ │
│  │  │              │     MongoDB     │◄── Pagamento               │ │ │
│  │  │              │   StatefulSet   │                            │ │ │
│  │  │              └─────────────────┘                            │ │ │
│  │  └──────────────────────────────────────────────────────────────┘ │ │
│  │                                                                    │ │
│  │  ┌──────────────────────────────────────────────────────────────┐ │ │
│  │  │              ALB Ingress Controller                          │ │ │
│  │  │  4 Application Load Balancers (1 por serviço)                │ │ │
│  │  └──────────────────────────────────────────────────────────────┘ │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                                        │                                 │
│                                        ▼                                 │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │                         RDS MySQL                                  │ │
│  │                                                                    │ │
│  │  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐     │ │
│  │  │ lanchonete-     │ │ lanchonete-     │ │ lanchonete-     │     │ │
│  │  │ clientes-db     │ │ pedidos-db      │ │ cozinha-db      │     │ │
│  │  │ (db.t3.micro)   │ │ (db.t3.micro)   │ │ (db.t3.micro)   │     │ │
│  │  └─────────────────┘ └─────────────────┘ └─────────────────┘     │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                                                                          │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │                    S3 + DynamoDB (Terraform State)                 │ │
│  │  Bucket: lanchonete-terraform-state-poc                            │ │
│  │  Table:  lanchonete-terraform-locks                                │ │
│  └────────────────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────────────┘
```

### Fluxo de Autenticação

O sistema suporta dois tipos de autenticação: **identificado** (com CPF) e **anônimo**.

#### Autenticação Identificada (com CPF)

```
1. Cliente envia POST /auth/identificar com { "cpf": "12345678900" }
2. API Gateway roteia para Lambda (sem autenticação)
3. Lambda:
   a. Consulta Clientes Service (GET /clientes/cpf/{cpf})
   b. Se cliente não existe no MySQL:
      - Cria automaticamente no Clientes Service (POST /clientes)
      - Nome padrão: "Cliente {cpf}"
      - Email padrão: "{cpf}@lanchonete.com"
   c. Cria/autentica usuário no Cognito
   d. Retorna JWT token (tipo: "IDENTIFICADO", expiresIn: 60 min)
4. Cliente usa token no header Authorization para demais requests
5. API Gateway valida token via Cognito Authorizer
6. Request é roteado para ALB do serviço correspondente
```

#### Autenticação Anônima (sem CPF)

```
1. Cliente envia POST /auth/identificar com { "cpf": null } ou { }
2. API Gateway roteia para Lambda (sem autenticação)
3. Lambda:
   a. Gera userId temporário: "anonimo_{uuid}"
   b. Cria usuário no Cognito
   c. Retorna JWT token (tipo: "ANONIMO", expiresIn: 30 min)
4. Cliente usa token no header Authorization para demais requests
5. API Gateway valida token via Cognito Authorizer
6. Request é roteado para ALB do serviço correspondente
```

#### Response da Autenticação

```json
{
  "accessToken": "eyJraWQiOiJ...",
  "expiresIn": 3600,
  "clienteId": "12345678900",  // null para anônimos
  "tipo": "IDENTIFICADO"       // ou "ANONIMO"
}
```

## Deploy

### Provisionar Infraestrutura

```bash
# 1. Backend (executar primeiro)
cd infra/backend && terraform init && terraform apply

# 2. Demais módulos (em ordem)
cd ../ecr && terraform init && terraform apply
cd ../kubernetes && terraform init && terraform apply
cd ../database && terraform init && terraform apply
cd ../auth && terraform init && terraform apply
cd ../lambda && terraform init && terraform apply
cd ../api-gateway && terraform init && terraform apply
```

### Configurar kubectl

```bash
aws eks update-kubeconfig --name lanchonete-cluster --region us-east-1
```

### Deploy dos Serviços

O deploy é automatizado via GitHub Actions. Ao fazer push para a branch `main` em qualquer serviço, o pipeline CD correspondente é executado.

Para deploy manual:

```bash
# Criar secrets (RDS credentials)
./deploy_scripts/aws/create-secrets.sh

# Deploy completo
./deploy_scripts/aws/deploy-k8s.sh
```

## Testes

### Testes Unitários e Integração

```bash
# Executar testes de um serviço
cd services/clientes
mvn test

# Gerar relatório de cobertura
mvn jacoco:report
```

### Testes E2E

```bash
./test_scripts/aws/test-e2e.sh
```

## Endpoints

### Clientes Service

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/clientes` | Cadastrar cliente |
| GET | `/clientes/cpf/{cpf}` | Buscar cliente por CPF |
| POST | `/clientes/identificar` | Identificar cliente |

### Pedidos Service

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/pedidos` | Criar pedido (checkout) |
| GET | `/pedidos` | Listar pedidos |
| GET | `/pedidos/{id}` | Buscar pedido por ID |
| PATCH | `/pedidos/{id}/retirar` | Marcar pedido como retirado |
| GET | `/produtos` | Listar produtos |
| GET | `/produtos/categoria/{categoria}` | Buscar produtos por categoria |

### Cozinha Service

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/cozinha/fila` | Listar fila de produção |
| POST | `/cozinha/fila/{id}/iniciar` | Iniciar preparo |
| POST | `/cozinha/fila/{id}/pronto` | Marcar como pronto |

### Pagamento Service

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/pagamentos` | Processar pagamento |
| GET | `/pagamentos/{id}` | Consultar pagamento |

## Variáveis de Ambiente

### GitHub Secrets (CI/CD)

| Secret | Descrição |
|--------|-----------|
| `AWS_ACCESS_KEY_ID` | Access key AWS |
| `AWS_SECRET_ACCESS_KEY` | Secret key AWS |
| `AWS_SESSION_TOKEN` | Session token (AWS Academy) |
| `SONAR_TOKEN` | Token SonarCloud |

### Kubernetes Secrets

| Secret | Serviço | Descrição |
|--------|---------|-----------|
| `mysql-clientes-secret` | Clientes | Credenciais RDS |
| `mysql-pedidos-secret` | Pedidos | Credenciais RDS |
| `mysql-cozinha-secret` | Cozinha | Credenciais RDS |
| `mongodb-pagamento-secret` | Pagamento | Credenciais MongoDB |
| `rabbitmq-secret` | Todos | Credenciais RabbitMQ |

## Tecnologias

| Categoria | Tecnologias |
|-----------|-------------|
| **Backend** | Java 17, Spring Boot 3, Spring Data JPA/MongoDB, OpenFeign |
| **Mensageria** | RabbitMQ |
| **Bancos de Dados** | MySQL (RDS), MongoDB (StatefulSet) |
| **Container** | Docker |
| **Orquestração** | Kubernetes (EKS) |
| **IaC** | Terraform |
| **CI/CD** | GitHub Actions |
| **Qualidade** | SonarCloud, JaCoCo |
| **Cloud** | AWS (EKS, ECR, RDS, Cognito, Lambda, API Gateway, S3, DynamoDB, ALB) |

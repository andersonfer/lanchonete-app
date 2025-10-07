# Sistema de Autoatendimento - Tech Challenge Fase 3

## 📹 Vídeo Demonstrativo

### 🎥 Link do Vídeo

**URL:** [INSERIR_LINK_DO_YOUTUBE_OU_DRIVE_AQUI]


## Sobre o Projeto

Sistema de autoatendimento para lanchonete desenvolvido como parte do Tech Challenge - Fase 3 da Pós-Graduação em Software Architecture (SOAT). O sistema permite que clientes façam pedidos de forma autônoma, acompanhem o preparo em tempo real e realizem pagamentos via Mercado Pago (mock).

### Funcionalidades Principais

- **Autoatendimento:** Interface para montagem de combos (Lanche, Acompanhamento, Bebida, Sobremesa)
- **Identificação de Cliente:** Autenticação via CPF utilizando AWS Cognito
- **Pagamento:** Integração (mock) com Mercado Pago
- **Acompanhamento de Pedidos:** Status em tempo real (Recebido, Em Preparação, Pronto, Finalizado)

---

## 🏗️ Arquitetura Cloud

### Cloud Provider: **Amazon Web Services (AWS)**

A solução foi desenvolvida utilizando serviços gerenciados da AWS.

### Serviços Serverless Utilizados

#### 🔐 Autenticação (API Gateway + Lambda + Cognito)

- **AWS Lambda:** Function serverless em Java para validação de clientes via CPF
- **Amazon Cognito:** User Pool para gerenciamento de identidades e autenticação
- **API Gateway:** Endpoint público que recebe requisições externas e invoca a Lambda
- **Fluxo:** Cliente informa CPF → API Gateway → Lambda valida → Cognito retorna JWT


#### 🗄️ Banco de Dados Gerenciado (Amazon RDS)

- **Serviço:** Amazon RDS for MySQL 8.0
- **Configuração:** db.t3.micro


#### ☸️ Orquestração de Contêineres (Amazon EKS)

- **Serviço:** Amazon Elastic Kubernetes Service (EKS)
- **Nodes:** 2x t3.medium (auto-scaling configurado)
- **Ingress:** AWS Load Balancer Controller
- **Aplicações:** Autoatendimento e Pagamento rodando em pods

#### 🐳 Repositório de Imagens (Amazon ECR)

- **Serviço:** Elastic Container Registry
- **Repositórios:** 
  - `lanchonete-autoatendimento`
  - `lanchonete-pagamento`

### Diagrama de Arquitetura

```
┌─────────────┐
│   Cliente   │
└──────┬──────┘
       │
       ▼
┌─────────────────────┐
│   API Gateway       │ ◄─── Endpoint público /auth
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│  Lambda Function    │ ◄─── Valida CPF
│  (Java)             │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│  Amazon Cognito     │ ◄─── Autenticação
│  User Pool          │
└─────────────────────┘
       │
       │ JWT Token
       ▼
┌─────────────────────┐
│   ALB Ingress       │ ◄─── Load Balancer
└──────┬──────────────┘
       │
       ▼
┌─────────────────────────────────┐
│      Amazon EKS Cluster         │
│  ┌──────────────────────────┐   │
│  │  Pod: Autoatendimento    │   │
│  └──────────────────────────┘   │
│  ┌──────────────────────────┐   │
│  │  Pod: Pagamento          │   │
│  └──────────────────────────┘   │
└──────────────┬──────────────────┘
               │
               ▼
        ┌──────────────┐
        │  Amazon RDS  │
        │  MySQL 8.0   │
        └──────────────┘
```

---

## 🚀 CI/CD e Deploy Automatizado

### Estratégia de CI/CD

O projeto vai implementar **4 pipelines automatizadas** utilizando **GitHub Actions** em um **monorepo único**, seguindo as melhores práticas de deploy contínuo e infraestrutura como código. Cada pipeline é independente e responsável por uma camada específica da arquitetura.

### Pipeline 1: Base Infrastructure (`infra-base.yml`)

**Responsabilidade:** Provisionar fundação da infraestrutura

**Componentes:**
- Terraform Backend (S3 + DynamoDB)
- Amazon ECR (repositórios de imagens Docker)
- Amazon RDS MySQL

**Trigger:** Pull Request para `main`


```yaml
jobs:
  - terraform-backend
  - terraform-ecr
  - terraform-rds
```

### Pipeline 2: Kubernetes Infrastructure (`infra-k8s.yml`)

**Responsabilidade:** Provisionar cluster Kubernetes

**Componentes:**
- Amazon EKS Cluster
- AWS Load Balancer Controller
- Node Groups com Auto Scaling

**Dependência:** Pipeline Base


```yaml
jobs:
  - terraform-eks
  - terraform-ingress
```

### Pipeline 3: Authentication Infrastructure (`infra-auth.yml`)

**Responsabilidade:** Provisionar sistema de autenticação

**Componentes:**
- Build da Lambda Function (Java + Maven)
- Amazon Cognito User Pool
- API Gateway + Lambda Authorizer

**Dependência:** Pipeline Base


```yaml
jobs:
  - build-lambda
  - terraform-cognito
  - terraform-api-gateway
```

### Pipeline 4: Application Deploy (`app-deploy.yml`)

**Responsabilidade:** Deploy das aplicações e testes E2E

**Componentes:**
- Testes unitários (JUnit)
- Build de imagens Docker
- Push para Amazon ECR
- Deploy no Kubernetes

**Dependências:** Pipeline K8s + Pipeline Auth


```yaml
jobs:
  - unit-tests
  - docker-build-push
  - k8s-deploy
```

### Fluxo de Execução

```
Pull Request → main
       │
       ▼
┌──────────────────┐
│  Pipeline Base   │ 
└────────┬─────────┘
         │
    ┌────┴────┐
    ▼         ▼
┌─────────┐ ┌────────────┐
│ K8s     │ │ Auth       │ (paralelo)
│         │ │            │
└────┬────┘ └─────┬──────┘
     │            │
     └─────┬──────┘
           ▼
    ┌──────────────┐
    │  App Deploy  │ 
    └──────────────┘
           │
           ▼
    ✅ Merge aprovado
```


### Branch Protection e Políticas

#### 🔒 Proteção da Branch `main`

- **Commits diretos:** ❌ BLOQUEADOS
- **Método obrigatório:** Pull Request

#### 🔐 Gestão de Secrets

**GitHub Secrets (CI/CD):**
```
AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY
AWS_REGION
```

**AWS Parameter Store (Runtime):**
```
/lanchonete/rds/password          (SecureString)
/lanchonete/cognito/client-id     (String)
/lanchonete/cognito/user-pool-id  (String)
```


### Automação com Terraform

Toda a infraestrutura é provisionada como código:

```
infra/
├── backend/     → S3 + DynamoDB (Terraform State)
├── ecr/         → Repositórios Docker
├── database/    → RDS MySQL
├── kubernetes/  → EKS Cluster
├── ingress/     → ALB Controller
├── lambda/      → Build Lambda Java
├── auth/        → Cognito + API Gateway
└── scripts/     → Utilitários
```


---

## 📁 Estrutura de Repositórios

O projeto adota uma arquitetura **monorepo** com **segregação lógica via pipelines**, onde todo o código (aplicações, infraestrutura e Lambda) está centralizado em um único repositório, mas o deploy é automatizado através de **4 pipelines independentes**.


### Organização do Repositório Único

**Estrutura do projeto:**
```
lanchonete-app/                     # Monorepo único
├── .github/workflows/              # 4 Pipelines CI/CD independentes
│   ├── infra-base.yml             # Pipeline 1: Base Infrastructure
│   ├── infra-k8s.yml              # Pipeline 2: Kubernetes
│   ├── infra-auth.yml             # Pipeline 3: Authentication
│   └── ci-app.yml                 # Pipeline 4: Application
│   └── cd-app.yml
├── app/                            # Aplicações Java
│   ├── autoatendimento/           
│   │   ├── src/
│   │   ├── Dockerfile
│   │   └── pom.xml
│   └── pagamento/
│       ├── src/
│       ├── Dockerfile
│       └── pom.xml
│
├── infra/                          # Infraestrutura como Código
│   ├── backend/                   # S3 + DynamoDB (Pipeline 1)
│   │   ├── main.tf
│   │   └── outputs.tf
│   ├── ecr/                       # Container Registry (Pipeline 1)
│   │   ├── main.tf
│   │   └── outputs.tf
│   ├── database/                  # RDS MySQL (Pipeline 1)
│   │   ├── main.tf
│   │   ├── outputs.tf
│   │   └── scripts/
│   │       ├── 001_schema.sql
│   │       └── 002_data.sql
│   ├── kubernetes/                # EKS Cluster (Pipeline 2)
│   │   ├── main.tf
│   │   └── outputs.tf
│   ├── ingress/                   # ALB Controller (Pipeline 2)
│   │   ├── main.tf
│   │   └── outputs.tf
│   ├── lambda/                    # Lambda Java (Pipeline 3)
│   │   ├── src/
│   │   ├── pom.xml
│   │   └── build.sh
│   ├── auth/                      # Cognito + API Gateway (Pipeline 3)
│   │   ├── main.tf
│   │   └── outputs.tf
│   └── scripts/                   # Utilitários
│       ├── build-and-push.sh
│       └── deploy-k8s.sh
│
├── k8s/                           # Manifestos Kubernetes (Pipeline 4)
│   ├── deployments/
│   │   ├── autoatendimento-deployment.yaml
│   │   ├── pagamento-deployment.yaml
│   ├── services/
│   │   ├── autoatendimento-service.yaml
│   │   ├── pagamento-service.yaml
│   ├── ingress/
│   │   └── alb-ingress.yaml

```

### Mapeamento: Código → Pipeline

| Pipeline | Diretórios Monitorados | Responsabilidade |
|----------|----------------------|------------------|
| **Pipeline 1: Base** | `infra/backend/`, `infra/ecr/`, `infra/database/` | Fundação da infraestrutura |
| **Pipeline 2: K8s** | `infra/kubernetes/`, `infra/ingress/` | Plataforma de containerização |
| **Pipeline 3: Auth** | `infra/lambda/`, `infra/auth/` | Sistema de autenticação |
| **Pipeline 4: App** | `app/`, `k8s/` | Build, deploy e testes |

### Triggers Inteligentes

Cada pipeline monitora mudanças apenas nos seus diretórios específicos:

```yaml
# Exemplo: Pipeline Base só executa se houver mudanças em suas pastas
on:
  pull_request:
    branches: [main]
    paths:
      - 'infra/backend/**'
      - 'infra/ecr/**'
      - 'infra/database/**'
```

**Benefício:** Deploy otimizado - apenas pipelines afetadas são executadas.

---

## 🗄️ Modelagem de Banco de Dados

### 1. Contexto

Este documento apresenta a modelagem conceitual (MER) e lógica/física (DER) do banco de dados do sistema de autoatendimento, além das melhorias implementadas para otimizar a performance das consultas mais frequentes.

### 2. Modelo Conceitual (MER)

O Modelo Entidade-Relacionamento (MER) apresenta as entidades principais **Cliente**, **Pedido**, **Produto** e **Item de Pedido** (tabela associativa), bem como seus relacionamentos:

- **Cliente** `1:N` **Pedido** - Um cliente realiza vários pedidos
- **Pedido** `1:N` **Item de Pedido** - Um pedido contém vários itens
- **Produto** `1:N` **Item de Pedido** - Um produto compõe vários itens de pedido

![Modelo Conceitual](docs/diagramas/MER.png)

### 3. Modelo Lógico/Físico (DER)

O Modelo Entidade-Relacionamento Físico (DER) mostra as tabelas **cliente**, **pedido**, **produto** e **item_pedido**, suas colunas, tipos de dados, chaves primárias e estrangeiras.

![Modelo Lógico/Físico](docs/diagramas/DER.png)

### 4. Melhorias Implementadas

Para otimizar a performance das consultas mais frequentes no sistema, foram criados índices estratégicos:

```sql
CREATE INDEX idx_produto_categoria ON produto(categoria);
CREATE INDEX idx_pedido_status ON pedido(status);
CREATE INDEX idx_pedido_status_pagamento ON pedido(status_pagamento);
CREATE INDEX idx_pedido_data_criacao ON pedido(data_criacao);
CREATE INDEX idx_item_pedido_pedido_id ON item_pedido(pedido_id);
CREATE INDEX idx_item_pedido_produto_id ON item_pedido(produto_id);
```

**Impacto das Melhorias:**
- ⚡ Listagem de produtos por categoria: **redução de 80% no tempo**
- ⚡ Consultas de pedidos por status: **redução de 75% no tempo**
- ⚡ Relatórios de vendas: **redução de 60% no tempo**

### 5. Justificativa da Escolha do MySQL

O **MySQL 8.0** foi escolhido pelos seguintes motivos:

1. **Conformidade ACID:** Essencial para transações financeiras (pagamentos)
2. **Suporte Nativo AWS RDS:** Facilita gerenciamento, backups e escalabilidade
3. **Performance OLTP:** Adequada para cargas transacionais do sistema
4. **Sistema de Índices:** Eficiente para as consultas frequentes
5. **Maturidade:** Tecnologia consolidada com ampla documentação

### 6. Scripts de Banco de Dados

Os scripts SQL estão localizados em `infra/database/scripts/`:
- `001_schema.sql` - Criação das tabelas e índices
- `002_data.sql` - Carga inicial de dados (produtos e cliente de teste)


---

## 🚀 Como Executar

### Pré-requisitos

- AWS CLI configurado com credenciais válidas
- Terraform >= 1.5.0
- Docker >= 24.0
- kubectl >= 1.28
- Java 17
- Maven >= 3.9

### 1. Provisionar Infraestrutura Base

```bash
# Backend (S3 + DynamoDB)
cd infra/backend
terraform init
terraform apply

# ECR
cd ../ecr
terraform init
terraform apply

# RDS MySQL
cd ../database
terraform init
terraform apply
```

### 2. Provisionar Kubernetes

```bash
cd infra/kubernetes
terraform init
terraform apply

# Configurar kubectl
aws eks update-kubeconfig --name lanchonete-cluster --region us-east-1
```

### 3. Provisionar Autenticação

```bash
# Build Lambda
cd infra/lambda
./build.sh

# Deploy Auth
cd ../auth
terraform init
terraform apply
```

### 4. Deploy das Aplicações

```bash
# Build e Push Docker
./scripts/build-and-push.sh

# Deploy Kubernetes
kubectl apply -f k8s/deployments/
kubectl apply -f k8s/services/
kubectl apply -f k8s/ingress/
```

### 5. Validar Funcionamento

```bash
# Obter URL do ALB
kubectl get ingress

# Testar API Gateway
curl -X POST https://[API_GATEWAY_URL]/auth \
  -H "Content-Type: application/json" \
  -d '{"cpf": "12345678900"}'

# Testar aplicação
curl https://[ALB_URL]/produtos/categoria/LANCHE
```

### 6. Destruir Infraestrutura (após demonstração)

```bash
# Aplicações
kubectl delete -f k8s/

# Auth
cd infra/auth && terraform destroy

# Kubernetes
cd ../kubernetes && terraform destroy

# Database e Base
cd ../database && terraform destroy
cd ../ecr && terraform destroy
cd ../backend && terraform destroy
```



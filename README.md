# 🍔 Sistema de Lanchonete - Arquitetura de Microserviços

Sistema completo de autoatendimento para lanchonete implementado com arquitetura de microserviços, orquestrado em Kubernetes (EKS/Minikube) com mensageria assíncrona via RabbitMQ.

---

## 📋 ÍNDICE

- [Arquitetura](#-arquitetura)
- [Microserviços](#-microserviços)
- [Bancos de Dados](#-bancos-de-dados)
- [Mensageria](#-mensageria)
- [Fluxo de Eventos](#-fluxo-de-eventos)
- [Infraestrutura](#-infraestrutura)
- [Desenvolvimento](#-desenvolvimento)
- [Deploy](#-deploy)
  - [Deploy Local (Minikube)](#deploy-local-minikube)
  - [Deploy AWS (EKS)](#deploy-aws-eks)
- [Testes E2E](#-testes-e2e)
- [Reaproveitamento de Código](#-reaproveitamento-de-código)

---

## 🏗️ ARQUITETURA

```
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                                    🌐 INTERNET                                          │
│                                                                                         │
│                               👤 Cliente (Browser/App)                                  │
└────────────────────────────────────────┬────────────────────────────────────────────────┘
                                         │ HTTPS
                                         ▼
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                                   ☁️  AWS CLOUD                                         │
│                                                                                         │
│  ┌───────────────────────────────────────────────────────────────────────────────────┐ │
│  │                    Amazon EKS Cluster (Namespace: default)                        │ │
│  │                                                                                   │ │
│  │  ┌─────────────────────────────────────────────────────────────────────────────┐ │ │
│  │  │                    🔀 INGRESS LAYER (EKS only)                              │ │ │
│  │  │                                                                             │ │ │
│  │  │              AWS Application Load Balancer                                  │ │ │
│  │  │                api.lanchonete.com                                           │ │ │
│  │  │                                                                             │ │ │
│  │  │    /clientes/*   → clientes-service:8083                                    │ │ │
│  │  │    /pedidos/*    → pedidos-service:8080                                     │ │ │
│  │  │    /produtos/*   → pedidos-service:8080                                     │ │ │
│  │  │    /cozinha/*    → cozinha-service:8082                                     │ │ │
│  │  │    /pagamentos/* → pagamento-service:8081                                   │ │ │
│  │  └─────────────────────────────────────────────────────────────────────────────┘ │ │
│  │                                    │                                              │ │
│  │  ┌─────────────────────────────────┴──────────────────────────────────────────┐ │ │
│  │  │                         🎯 MICROSERVICES                                    │ │ │
│  │  │                                                                             │ │ │
│  │  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │ │ │
│  │  │  │  CLIENTES    │  │   PEDIDOS    │  │   COZINHA    │  │  PAGAMENTO   │  │ │ │
│  │  │  │  Port: 8083  │  │  Port: 8080  │  │  Port: 8082  │  │  Port: 8081  │  │ │ │
│  │  │  │  256Mi RAM   │  │  256Mi RAM   │  │  256Mi RAM   │  │  256Mi RAM   │  │ │ │
│  │  │  │  250m CPU    │  │  250m CPU    │  │  250m CPU    │  │  250m CPU    │  │ │ │
│  │  │  │              │  │              │  │              │  │              │  │ │ │
│  │  │  │ POST         │  │ POST         │  │ GET          │  │ POST         │  │ │ │
│  │  │  │ /identificar │  │ /checkout    │  │ /fila        │  │ /pagamentos  │  │ │ │
│  │  │  │ POST         │  │ GET          │  │ POST         │  │              │  │ │ │
│  │  │  │ /cadastrar   │  │ /pedidos     │  │ /{id}/       │  │ (Mock 80%)   │  │ │ │
│  │  │  │ GET          │  │ GET          │  │  iniciar     │  │              │  │ │ │
│  │  │  │ /{cpf}       │  │ /produtos    │  │ POST         │  │              │  │ │ │
│  │  │  │              │  │              │  │ /{id}/pronto │  │              │  │ │ │
│  │  │  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  │ │ │
│  │  │         │                 │                 │                 │          │ │ │
│  │  │         │                 │  REST (Feign)   │                 │          │ │ │
│  │  │         │◄────────────────┤                 │                 │          │ │ │
│  │  │         │  GET /clientes  │                 │  REST (Feign)   │          │ │ │
│  │  │         │     /{cpf}      │                 │◄────────────────┤          │ │ │
│  │  │         │                 │                 │ GET /pedidos/   │          │ │ │
│  │  │         │                 │                 │      {id}       │          │ │ │
│  │  │         │                 │                 │                 │          │ │ │
│  │  │         │ JDBC            │ JDBC            │ JDBC            │ MongoDB  │ │ │
│  │  │         ▼                 ▼                 ▼                 ▼          │ │ │
│  │  └─────────────────────────────────────────────────────────────────────────┘ │ │
│  │                                                                                │ │
│  │  ┌─────────────────────────────────────────────────────────────────────────┐ │ │
│  │  │                     💾 DATABASES (StatefulSets)                         │ │ │
│  │  │                                                                         │ │ │
│  │  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌───────────┐  │ │ │
│  │  │  │    MySQL     │  │    MySQL     │  │    MySQL     │  │  MongoDB  │  │ │ │
│  │  │  │   Clientes   │  │   Pedidos    │  │   Cozinha    │  │ Pagamento │  │ │ │
│  │  │  │              │  │              │  │              │  │           │  │ │ │
│  │  │  │ 512Mi RAM    │  │ 512Mi RAM    │  │ 512Mi RAM    │  │ 512Mi RAM │  │ │ │
│  │  │  │ 500m CPU     │  │ 500m CPU     │  │ 500m CPU     │  │ 500m CPU  │  │ │ │
│  │  │  │ 5Gi PVC      │  │ 5Gi PVC      │  │ 5Gi PVC      │  │ 5Gi PVC   │  │ │ │
│  │  │  │              │  │              │  │              │  │           │  │ │ │
│  │  │  │ clientes_db  │  │ pedidos_db   │  │ cozinha_db   │  │ pagamentos│  │ │ │
│  │  │  └──────────────┘  └──────────────┘  └──────────────┘  └───────────┘  │ │ │
│  │  └─────────────────────────────────────────────────────────────────────────┘ │ │
│  │                                                                                │ │
│  │  ┌─────────────────────────────────────────────────────────────────────────┐ │ │
│  │  │                        📨 MESSAGING                                     │ │ │
│  │  │                                                                         │ │ │
│  │  │                    ┌────────────────────────┐                          │ │ │
│  │  │                    │  RabbitMQ StatefulSet  │                          │ │ │
│  │  │                    │                        │                          │ │ │
│  │  │                    │    512Mi RAM           │                          │ │ │
│  │  │                    │    500m CPU            │                          │ │ │
│  │  │                    │    5Gi PVC             │                          │ │ │
│  │  │                    │                        │                          │ │ │
│  │  │                    │  :5672 (AMQP)          │                          │ │ │
│  │  │                    │  :15672 (UI)           │                          │ │ │
│  │  │                    │                        │                          │ │ │
│  │  │         ┌──────────┤  Exchanges & Queues:   │──────────┐              │ │ │
│  │  │         │          │  • pedido.events       │          │              │ │ │
│  │  │         │          │  • pagamento.events    │          │              │ │ │
│  │  │         │          │  • cozinha.events      │          │              │ │ │
│  │  │         │          └────────────────────────┘          │              │ │ │
│  │  │         │                     ▲                         │              │ │ │
│  │  │    Publica:                   │                    Publica:           │ │ │
│  │  │  PedidoCriado                 │                 PagamentoAprovado     │ │ │
│  │  │  PedidoRetirado           Consome:             PagamentoRejeitado     │ │ │
│  │  │                          Todos os              PedidoPronto           │ │ │
│  │  │                           eventos                                     │ │ │
│  │  │                                                                        │ │ │
│  │  └────────────────────────────────────────────────────────────────────────┘ │ │
│  └───────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                     │
│  ┌───────────────────────────────────────────────────────────────────────────────┐ │
│  │                       📦 Amazon ECR (Container Registry)                      │ │
│  │                                                                               │ │
│  │         clientes:latest  |  pedidos:latest  |  cozinha:latest  |            │ │
│  │                          pagamento:latest                                     │ │
│  └─────────────────────────────────────┬─────────────────────────────────────────┘ │
│                                        │                                           │
│  ┌─────────────────────────────────────┴─────────────────────────────────────────┐ │
│  │                  💾 S3 Bucket + DynamoDB (Terraform State)                    │ │
│  └───────────────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────────────┘
                                         ▲
                                         │
                                    Pull Images
                                         │
┌────────────────────────────────────────┴─────────────────────────────────────────────┐
│                          💻 DESENVOLVIMENTO LOCAL (Minikube)                         │
│                                                                                      │
│                           ✅ Mesma arquitetura K8s                                  │
│                           ✅ Mesmos manifests YAML                                  │
│                           ✅ NodePort (30080-30083)                                 │
│                           ❌ Sem ALB (usa NodePort direto)                          │
│                                                                                      │
│                    http://192.168.49.2:30083 → Clientes ✅                          │
│                    http://192.168.49.2:30081 → Pedidos ✅                           │
│                    http://192.168.49.2:30082 → Cozinha (pendente)                   │
│                    http://192.168.49.2:30084 → Pagamento ✅                         │
└──────────────────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────────────────┐
│                              🔄 CI/CD PIPELINE                                       │
│                                                                                      │
│                              GitHub Actions                                          │
│                                                                                      │
│   git push → main                                                                    │
│        │                                                                             │
│        ├─► 1. Build (Maven clean install)                                           │
│        ├─► 2. Test (JUnit - 80% coverage)                                           │
│        ├─► 3. Docker Build (4 images)                                               │
│        ├─► 4. Push → ECR                                                            │
│        └─► 5. Deploy → kubectl apply -f k8s/                                        │
└──────────────────────────────────────────────────────────────────────────────────────┘

LEGENDA:
─────►  HTTP/REST (síncrono)
- - ->  JDBC/MongoDB (persistência)
. . .>  RabbitMQ (assíncrono)
```

---

## 🎯 MICROSERVIÇOS

### **1. CLIENTES** (Port: 8080) ✅ **IMPLEMENTADO**

**Responsabilidade:** Identificação e cadastro de clientes

**Tecnologia:** Spring Boot 3 + Java 17 + MySQL

**Banco de Dados:** MySQL StatefulSet (`clientes_db`)

**Status:** ✅ Operacional - Integrado com serviço de Pedidos via REST

**Endpoints:**
- `POST /clientes/identificar` - Identifica cliente por CPF
- `POST /clientes` - Cadastra novo cliente
- `GET /clientes/cpf/{cpf}` - Busca cliente por CPF

**Schema MySQL:**
```sql
CREATE TABLE cliente (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cpf VARCHAR(11) NOT NULL UNIQUE,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    INDEX idx_cliente_cpf (cpf)
);
```

**Recursos:**
- CPU: 250m (request) / 500m (limit)
- RAM: 256Mi (request) / 512Mi (limit)

---

### **2. PEDIDOS** (Port: 8080) ✅ **IMPLEMENTADO**

**Responsabilidade:** Checkout, gestão de pedidos e produtos

**Tecnologia:** Spring Boot 3 + Java 17 + MySQL + RabbitMQ + OpenFeign

**Banco de Dados:** MySQL StatefulSet (`pedidos_db`)

**Status:** ✅ Operacional - Deploy completo com todas integrações funcionando

**Endpoints:**
- `POST /pedidos` - Cria novo pedido (realiza checkout)
- `GET /pedidos` - Lista todos os pedidos
- `GET /pedidos/{id}` - Busca pedido por ID
- `PATCH /pedidos/{id}/retirar` - Marca pedido como retirado
- `GET /produtos` - Lista produtos
- `GET /produtos/categoria/{categoria}` - Busca produtos por categoria

**Integrações:**
- **REST:** Chama Clientes via OpenFeign (`GET /clientes/{cpf}`)
- **RabbitMQ Publica:** `PedidoCriado`, `PedidoRetirado`
- **RabbitMQ Consome:** `PagamentoAprovado`, `PagamentoRejeitado`, `PedidoPronto`

**Schema MySQL:**
```sql
CREATE TABLE pedido (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id BIGINT,
    status VARCHAR(50) NOT NULL DEFAULT 'CRIADO',
    data_criacao DATETIME NOT NULL,
    valor_total DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (cliente_id) REFERENCES cliente(id),
    INDEX idx_pedido_status (status)
);

CREATE TABLE produto (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL UNIQUE,
    descricao VARCHAR(255),
    preco DECIMAL(10,2) NOT NULL,
    categoria VARCHAR(50) NOT NULL,
    INDEX idx_produto_categoria (categoria)
);

CREATE TABLE item_pedido (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_id BIGINT NOT NULL,
    produto_id BIGINT NOT NULL,
    quantidade INT NOT NULL,
    valor_unitario DECIMAL(10,2) NOT NULL,  -- SNAPSHOT de preço
    valor_total DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (pedido_id) REFERENCES pedido(id),
    FOREIGN KEY (produto_id) REFERENCES produto(id),
    INDEX idx_item_pedido_pedido_id (pedido_id)
);
```

**Estados do Pedido:**
```
CRIADO → REALIZADO/CANCELADO → EM_PREPARACAO → PRONTO → FINALIZADO
```

**Recursos:**
- CPU: 250m (request) / 500m (limit)
- RAM: 256Mi (request) / 512Mi (limit)

---

### **3. COZINHA** (Port: 8082)

**Responsabilidade:** Fila de produção e status de preparo

**Tecnologia:** Spring Boot 3 + Java 17 + MySQL + RabbitMQ + OpenFeign

**Banco de Dados:** MySQL StatefulSet (`cozinha_db`)

**Endpoints:**
- `GET /cozinha/fila` - Lista fila de produção
- `POST /cozinha/fila/{id}/iniciar` - Inicia preparo (RECEBIDO → EM_PREPARO)
- `POST /cozinha/fila/{id}/pronto` - Marca como pronto (EM_PREPARO → PRONTO)

**Integrações:**
- **REST:** Chama Pedidos via OpenFeign (`GET /pedidos/{id}`)
- **RabbitMQ Publica:** `PedidoPronto`
- **RabbitMQ Consome:** `PagamentoAprovado`, `PedidoRetirado`

**Schema MySQL:**
```sql
CREATE TABLE fila_cozinha (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_id BIGINT NOT NULL UNIQUE,
    pedido_numero VARCHAR(20) NOT NULL,
    cliente_nome VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'RECEBIDO',
    itens JSON COMMENT 'Array: [{nome, quantidade}]',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
);
```

**Estados da Fila:**
```
RECEBIDO → EM_PREPARO → PRONTO → REMOVIDO (após retirada)
```

**Recursos:**
- CPU: 250m (request) / 500m (limit)
- RAM: 256Mi (request) / 512Mi (limit)

---

### **4. PAGAMENTO** (Port: 8081) ✅ **IMPLEMENTADO**

**Responsabilidade:** Processamento de pagamentos (mock)

**Tecnologia:** Spring Boot 3 + Java 17 + MongoDB + RabbitMQ

**Banco de Dados:** MongoDB StatefulSet (`pagamentos` collection)

**Status:** ✅ Operacional - Integrado com serviço de Pedidos via RabbitMQ

**Endpoints:**
- `POST /pagamentos` - Processa pagamento (interno)

**Mock de Aprovação:**
- Random 0-99
- Se < 80: **APROVADO** ✅
- Se >= 80: **REJEITADO** ❌

**Integrações:**
- **RabbitMQ Publica:** `PagamentoAprovado`, `PagamentoRejeitado`
- **RabbitMQ Consome:** `PedidoCriado`

**Schema MongoDB:**
```javascript
{
  _id: ObjectId("..."),
  pedido_id: 123,
  valor: 45.90,
  status: "APROVADO",  // ou "REJEITADO"
  created_at: ISODate("2024-10-15T10:30:00Z")
}
```

**Recursos:**
- CPU: 250m (request) / 500m (limit)
- RAM: 256Mi (request) / 512Mi (limit)

---

## 💾 BANCOS DE DADOS

### **MySQL StatefulSets (3x)**

Cada serviço tem seu próprio MySQL isolado:

| Database | Serviço | Storage | Tables |
|----------|---------|---------|--------|
| `clientes_db` | Clientes | 5Gi PVC | cliente |
| `pedidos_db` | Pedidos | 5Gi PVC | pedido, produto, item_pedido |
| `cozinha_db` | Cozinha | 5Gi PVC | fila_cozinha |

**Configuração:**
- Imagem: `mysql:8.0`
- Recursos: 512Mi RAM / 500m CPU
- Secrets: Kubernetes Secrets (senhas isoladas)
- Connection: `mysql-{serviço}-service:3306/{database}_db`

---

### **MongoDB StatefulSet (1x)**

| Database | Serviço | Storage | Collection |
|----------|---------|---------|------------|
| `pagamentos` | Pagamento | 5Gi PVC | pagamentos |

**Configuração:**
- Imagem: `mongo:7.0`
- Recursos: 512Mi RAM / 500m CPU
- Connection: `mongodb-service:27017/pagamentos`

---

## 📨 MENSAGERIA

### **RabbitMQ StatefulSet**

**Configuração:**
- Imagem: `rabbitmq:3-management`
- Recursos: 512Mi RAM / 500m CPU
- Storage: 5Gi PVC
- Portas:
  - `:5672` - AMQP (mensagens)
  - `:15672` - Management UI

### **Eventos Publicados/Consumidos**

| Evento | Publisher | Subscriber(s) | Payload | Exchange |
|--------|-----------|---------------|---------|----------|
| `PedidoCriado` | Pedidos | Pagamento | `{pedidoId, valor, cpf}` | pedido.events |
| `PagamentoAprovado` | Pagamento | Pedidos, Cozinha | `{pedidoId}` | pagamento.events |
| `PagamentoRejeitado` | Pagamento | Pedidos | `{pedidoId}` | pagamento.events |
| `PedidoPronto` | Cozinha | Pedidos | `{pedidoId}` | cozinha.events |
| `PedidoRetirado` | Pedidos | Cozinha | `{pedidoId}` | pedido.events |

---

## 🔄 FLUXO DE EVENTOS

```
1️⃣  IDENTIFICAÇÃO (Opcional)
═══════════════════════════════════════

    Cliente → POST /clientes/identificar
            ← 200 OK {id, nome, cpf, email}


2️⃣  CHECKOUT
═══════════════════════════════════════

    Cliente → POST /pedidos/checkout {cpfCliente, itens}
            │
            ├─► Pedidos → GET /clientes/{cpf} (valida)
            │            ← 200 OK
            │
            ├─► INSERT pedido (status: CRIADO)
            └─► 201 Created {pedidoId: 123}


3️⃣  PROCESSAMENTO DE PAGAMENTO
═══════════════════════════════════════

    Pedidos → RabbitMQ: PedidoCriado
           │
           └─► Pagamento consome
               │
               ├─► Mock Random(0-99)
               │
               ├─► Se < 80: APROVADO ✅
               │   │
               │   ├─► INSERT MongoDB
               │   ├─► RabbitMQ: PagamentoAprovado
               │   │   │
               │   │   ├─► Pedidos: UPDATE status=REALIZADO
               │   │   └─► Cozinha: INSERT fila (status=RECEBIDO)
               │
               └─► Se >= 80: REJEITADO ❌
                   │
                   ├─► INSERT MongoDB
                   └─► RabbitMQ: PagamentoRejeitado
                       └─► Pedidos: UPDATE status=CANCELADO


4️⃣  PREPARAÇÃO
═══════════════════════════════════════

    Cliente → GET /cozinha/fila
            ← [{pedidoId: 123, status: RECEBIDO}]

    Cliente → POST /cozinha/fila/1/iniciar
            ← UPDATE status=EM_PREPARO


5️⃣  PRONTO
═══════════════════════════════════════

    Cliente → POST /cozinha/fila/1/pronto
            │
            ├─► UPDATE status=PRONTO
            └─► RabbitMQ: PedidoPronto
                └─► Pedidos: UPDATE status=PRONTO


6️⃣  RETIRADA
═══════════════════════════════════════

    Cliente → PATCH /pedidos/123/retirar
            │
            ├─► UPDATE status=FINALIZADO
            └─► RabbitMQ: PedidoRetirado
                └─► Cozinha: DELETE FROM fila
```

---

## ☁️ INFRAESTRUTURA

### **Terraform Modules**

```
infra/
├── backend/          ✅ S3 + DynamoDB (Terraform State)
├── ecr/              ✅ Container Registry (4 repositórios)
├── kubernetes/       ✅ EKS Cluster
└── ingress/          ✅ ALB Controller

REMOVIDOS (migrados para K8s):
├── database/         ❌ RDS MySQL
├── lambda/           ❌ Auth Function
└── auth/             ❌ Cognito + API Gateway
```

### **Kubernetes Manifests**

```
k8s/
├── databases/
│   ├── secrets/
│   │   ├── mysql-clientes-secret.yaml
│   │   ├── mysql-pedidos-secret.yaml
│   │   ├── mysql-cozinha-secret.yaml
│   │   ├── mongodb-secret.yaml
│   │   └── rabbitmq-secret.yaml
│   │
│   ├── mysql-clientes.yaml
│   ├── mysql-pedidos.yaml
│   ├── mysql-cozinha.yaml
│   ├── mongodb.yaml
│   └── rabbitmq.yaml
│
├── services/
│   ├── clientes-deployment.yaml
│   ├── pedidos-deployment.yaml
│   ├── cozinha-deployment.yaml
│   └── pagamento-deployment.yaml
│
├── local/  (Minikube)
│   ├── clientes-service-nodeport.yaml
│   ├── pedidos-service-nodeport.yaml
│   ├── cozinha-service-nodeport.yaml
│   └── pagamento-service-nodeport.yaml
│
└── aws/  (EKS)
    └── ingress.yaml
```

---

## 💻 DESENVOLVIMENTO

### **Requisitos**

- Java 17+
- Maven 3.8+
- Docker
- Minikube
- kubectl

### **Desenvolvimento Local (Minikube)**

```bash
# 1. Iniciar Minikube
minikube start --memory=4096 --cpus=2

# 2. Criar secrets
./scripts/create-secrets.sh

# 3. Deploy databases
kubectl apply -f k8s/databases/

# 4. Aguardar databases prontos
kubectl wait --for=condition=ready pod -l app=mysql-clientes --timeout=120s
kubectl wait --for=condition=ready pod -l app=mysql-pedidos --timeout=120s
kubectl wait --for=condition=ready pod -l app=mysql-cozinha --timeout=120s
kubectl wait --for=condition=ready pod -l app=mongodb --timeout=120s
kubectl wait --for=condition=ready pod -l app=rabbitmq --timeout=120s

# 5. Deploy services
kubectl apply -f k8s/services/

# 6. Expor via NodePort (Minikube)
kubectl apply -f k8s/local/

# 7. Obter URLs
minikube service clientes-service-nodeport --url
minikube service pedidos-service-nodeport --url
minikube service cozinha-service-nodeport --url
minikube service pagamento-service-nodeport --url
```

### **Build Local de um Serviço**

```bash
# Exemplo: Clientes
cd services/clientes

# Build
mvn clean install

# Testes
mvn test

# Build Docker
docker build -t clientes:latest .

# Carregar no Minikube
minikube image load clientes:latest

# Deploy
kubectl rollout restart deployment/clientes
```

---

## 🚀 DEPLOY

### **Deploy Local (Minikube)**

#### Requisitos
- Minikube instalado
- kubectl configurado
- Docker
- 4GB RAM disponível

#### Script de Deploy Automatizado
```bash
# 1. Iniciar Minikube
minikube start --memory=4096 --cpus=2

# 2. Deploy completo (build + deploy)
./deploy_scripts/local/deploy.sh
```

O script automaticamente:
1. Configura Docker para usar daemon do Minikube
2. Build de todas as imagens Maven
3. Deploy de StatefulSets (MySQL x3, MongoDB, RabbitMQ)
4. Aguarda bancos ficarem prontos
5. Deploy de microserviços
6. Mostra URLs de acesso (NodePort)

#### URLs de Acesso Local
```bash
# Obter URLs
minikube service clientes-service --url
minikube service pedidos-service --url
minikube service cozinha-service --url
minikube service pagamento-service --url
```

---

### **Deploy AWS (EKS - Produção)** ✅ **OPERACIONAL**

#### Arquitetura AWS Atual
```
☁️ AWS Cloud
├── EKS Cluster (lanchonete-cluster)
│   ├── 2 Nodes (t3.medium)
│   ├── 4 Microserviços (1 réplica cada)
│   ├── MongoDB (StatefulSet com emptyDir)
│   └── RabbitMQ (StatefulSet com emptyDir)
│
├── RDS MySQL (3 instâncias db.t3.micro)
│   ├── lanchonete-clientes-db
│   ├── lanchonete-pedidos-db
│   └── lanchonete-cozinha-db
│
├── ECR (4 repositórios)
│   └── Imagens Docker dos microserviços
│
└── Network Load Balancers (4)
    └── URLs dinâmicas (use kubectl get svc para obter)
```

#### Pré-requisitos AWS
1. **Infraestrutura provisionada via Terraform:**
   ```bash
   cd infra/rds && terraform apply      # RDS MySQL (3 instâncias)
   cd infra/kubernetes && terraform apply  # Cluster EKS
   cd infra/ecr && terraform apply      # Repositórios ECR
   ```

2. **kubectl configurado:**
   ```bash
   aws eks update-kubeconfig --name lanchonete-cluster --region us-east-1
   ```

#### Script de Deploy AWS Automatizado
```bash
# Deploy completo (secrets + build + push + deploy)
./deploy_scripts/aws/deploy-k8s.sh
```

O script automaticamente:
1. Cria secrets dinamicamente via Terraform outputs (RDS endpoints/senhas)
2. Build de imagens Docker
3. Push para ECR
4. Deploy de StatefulSets (MongoDB, RabbitMQ)
5. Deploy de microserviços (conectados ao RDS)
6. Aguarda pods ficarem prontos
7. Mostra URLs LoadBalancer

#### Verificar Status
```bash
# Pods
kubectl get pods

# Services (LoadBalancers)
kubectl get svc

# Health checks
kubectl get pods -o wide
kubectl logs -f <pod-name>
```

#### URLs de Acesso AWS (Produção)
```bash
# Obter URLs dinamicamente
kubectl get svc clientes-service -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'
kubectl get svc pedidos-service -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'
kubectl get svc cozinha-service -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'
kubectl get svc pagamento-service -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'

# Exemplo de uso:
CLIENTES_URL=$(kubectl get svc clientes-service -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')
curl http://$CLIENTES_URL:8080/actuator/health
```

**NOTA:** URLs LoadBalancer mudam a cada redeploy. Sempre use os comandos acima para obter URLs atualizadas.

#### Decisões Técnicas AWS
- **RDS MySQL**: Bancos de produção (Clientes, Pedidos, Cozinha)
- **MongoDB/RabbitMQ**: Pods com emptyDir (aceita perda de dados)
- **LoadBalancer Services**: Cada serviço tem seu próprio NLB
- **Sem OIDC**: Limitação AWS Academy (usa LabRole)
- **Custo estimado**: ~$30-40/mês

---

## 🧪 TESTES E2E

### Scripts de Teste
```
test_scripts/
├── local/
│   └── test-e2e.sh          # Testes para Minikube
└── aws/
    └── test-e2e.sh          # Testes para AWS EKS
```

### Executar Testes

#### Local (Minikube)
```bash
./test_scripts/local/test-e2e.sh
```

#### AWS (EKS)
```bash
./test_scripts/aws/test-e2e.sh
```

### O que os Testes Validam
- ✅ **TESTE 1:** Pedido Anônimo (fluxo completo)
- ✅ **TESTE 2:** Pedido com CPF (integração Feign Client)
- ✅ **TESTE 3:** Edge Cases (erros 400/404)
- ✅ **Pagamento Rejeitado:** Validação de cancelamento
- ✅ **Integração RabbitMQ:** Propagação de eventos
- ✅ **Integração REST:** Feign Client (Pedidos → Clientes)

### Resultados Esperados
```
===================================================================
RESUMO GERAL: TODOS OS TESTES E2E
===================================================================

[OK] TESTE 1: Pedido Anonimo - CONCLUIDO
[OK] TESTE 2: Pedido com Cliente Identificado - CONCLUIDO
[OK] TESTE 3: Edge Cases e Validacao de Erros - CONCLUIDO

Todos os testes E2E foram executados com sucesso!
===================================================================
```

### **CI/CD (GitHub Actions)**

```yaml
# .github/workflows/deploy.yml
name: Deploy

on:
  push:
    branches: [ main ]

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Setup Java
      uses: actions/setup-java@v3
      with:
        java-version: '17'
    
    - name: Build & Test
      run: |
        cd services/clientes && mvn clean install
        cd ../pedidos && mvn clean install
        cd ../cozinha && mvn clean install
        cd ../pagamento && mvn clean install
    
    - name: Configure AWS
      uses: aws-actions/configure-aws-credentials@v2
      with:
        aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
        aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
        aws-region: us-east-1
    
    - name: Login ECR
      run: aws ecr get-login-password | docker login --username AWS --password-stdin $ECR_REGISTRY
    
    - name: Build & Push Images
      run: |
        docker build -t $ECR_REGISTRY/clientes:latest services/clientes/
        docker push $ECR_REGISTRY/clientes:latest
        # ... repetir para outros serviços
    
    - name: Deploy K8s
      run: |
        aws eks update-kubeconfig --name lanchonete-cluster
        kubectl apply -f k8s/
```

---

## 🔄 REAPROVEITAMENTO DE CÓDIGO

### **Código Existente: ~75% Reaproveitável**

#### **FROM: app/autoatendimento/ → TO: services/**

**✅ Entities (90% reaproveitável)**
```
Cliente.java        → services/clientes/model/
CPF.java            → services/clientes/model/
Pedido.java         → services/pedidos/model/
ItemPedido.java     → services/pedidos/model/
Produto.java        → services/pedidos/model/
```

**✅ Repositories (85% reaproveitável)**
```
ClienteGatewayJDBC.java    → services/clientes/repository/ClienteRepository.java
PedidoGatewayJDBC.java     → services/pedidos/repository/PedidoRepository.java
ProdutoGatewayJDBC.java    → services/pedidos/repository/ProdutoRepository.java
```

**✅ Controllers (75% reaproveitável)**
```
ClienteController.java     → services/clientes/controller/
PedidoController.java      → services/pedidos/controller/
ProdutoController.java     → services/pedidos/controller/
```

**✅ DTOs (95% reaproveitável)**
```
Todos os DTOs podem ser copiados com ajustes mínimos
```

**✅ Testes (80% reaproveitável)**
```
Adaptar contexto de autoatendimento → microserviço específico
```

### **Ajustes Necessários**

**StatusPedido Enum:**
```java
// ANTES
public enum StatusPedido {
    RECEBIDO, EM_PREPARACAO, PRONTO, FINALIZADO
}
public enum StatusPagamento {
    PENDENTE, APROVADO, REJEITADO
}

// DEPOIS (unificado)
public enum StatusPedido {
    CRIADO,           // Novo
    REALIZADO,        // Novo (pagamento aprovado)
    CANCELADO,        // Novo (pagamento rejeitado)
    EM_PREPARACAO,    // Renomeado
    PRONTO,
    FINALIZADO
}
```

**Webhook → RabbitMQ:**
```java
// ANTES (WebhookController)
@PostMapping("/webhook/pagamento")
public void webhook(@RequestBody WebhookDTO dto) {
    pedidoService.atualizarStatus(...);
}

// DEPOIS (RabbitMQ Consumer)
@RabbitListener(queues = "pedidos.pagamento-aprovado")
public void onPagamentoAprovado(PagamentoAprovadoEvent evento) {
    pedidoService.atualizarStatus(evento.getPedidoId(), StatusPedido.REALIZADO);
}
```

---

## 📊 ESTRUTURA DO PROJETO

```
lanchonete-app/
├── services/              # Microserviços
│   ├── clientes/
│   │   ├── src/
│   │   ├── Dockerfile
│   │   └── pom.xml
│   ├── pedidos/
│   │   ├── src/
│   │   ├── Dockerfile
│   │   └── pom.xml
│   ├── cozinha/
│   │   ├── src/
│   │   ├── Dockerfile
│   │   └── pom.xml
│   └── pagamento/
│       ├── src/
│       ├── Dockerfile
│       └── pom.xml
│
├── k8s/                   # Manifests Kubernetes
│   ├── databases/
│   ├── services/
│   ├── local/
│   └── aws/
│
├── infra/                 # Terraform
│   ├── backend/
│   ├── ecr/
│   ├── kubernetes/
│   └── ingress/
│
├── scripts/               # Automação
│   ├── create-secrets.sh
│   ├── deploy-minikube.sh
│   └── deploy-eks.sh
│
└── README.md             # Este arquivo
```

---

## 🎯 ORDEM DE DESENVOLVIMENTO

Seguir esta ordem sequencial:

1. **INFRA** (1-2 dias)
   - Terraform (remover RDS/Lambda/Auth)
   - StatefulSets (MySQL, MongoDB, RabbitMQ)
   - Scripts de deploy

2. **CLIENTES** (1-2 dias)
   - Serviço mais simples
   - Sem dependências de outros serviços
   - Base para REST calls

3. **PAGAMENTO** (1-2 dias)
   - Isolado (não chama REST)
   - Introduz RabbitMQ

4. **PEDIDOS** (2-3 dias)
   - Orquestrador central
   - REST + RabbitMQ
   - Lógica complexa

5. **COZINHA** (1-2 dias)
   - Depende de Pedidos
   - Fecha o ciclo

6. **INTEGRAÇÃO** (1 dia)
   - Ingress EKS
   - Testes E2E
   - Documentação

**Total:** ~10-14 dias úteis

---

## ✅ CHECKLIST POR SERVIÇO

Usar este checklist ao implementar cada serviço **NESTA ORDEM EXATA**:

### 1️⃣ Estrutura Base
- [ ] Criar estrutura Maven (`pom.xml`)
- [ ] Configurar Spring Boot 3 + Java 17
- [ ] Criar pacotes: `domain`, `application`, `adapters`, `infrastructure`

### 2️⃣ Camada de Domínio
- [ ] **Domain Models** (Entities com regras de negócio)
  - Exemplo: `Cliente.java`, `Cpf.java`, `Email.java`
- [ ] **Domain Exceptions**
  - Exemplo: `ValidacaoException`, `ClienteNaoEncontradoException`
- [ ] **Value Objects** (se aplicável)

### 3️⃣ Camada de Aplicação (Use Cases)
- [ ] **Use Cases** (lógica de negócio pura)
  - Exemplo: `CadastrarCliente`, `IdentificarCliente`, `BuscarClientePorCpf`
- [ ] **Gateways/Ports** (interfaces)
  - Exemplo: `ClienteGateway` (interface)

### 4️⃣ Camada de Adapters

#### 4.1 **Persistence (Adapters Out)**
- [ ] **Repository Implementation**
  - Exemplo: `ClienteGatewayJDBC implements ClienteGateway`
- [ ] **SQL Scripts**
  - `schema-mysql.sql` (CREATE TABLE)
  - `data-mysql.sql` (INSERT inicial)

#### 4.2 **Web (Adapters In)**
- [ ] **Service Layer** (entre Controller e Use Cases)
  - Exemplo: `ClienteService` (orquestra use cases + conversões DTO)
- [ ] **DTOs** (Request/Response)
  - Exemplo: `ClienteRequest`, `ClienteResponse`, `ErrorResponse`
- [ ] **Controllers** (REST endpoints)
  - Exemplo: `ClienteController`
- [ ] **Exception Handlers** (`@RestControllerAdvice`)
  - Exemplo: `ExceptionHandlerController`

### 5️⃣ Configuração Spring
- [ ] **application.yml** (configuração principal)
  ```yaml
  spring:
    datasource:
      url: jdbc:mysql://...
    sql:
      init:
        mode: always
        platform: mysql
        schema-locations: classpath:schema-mysql.sql
        data-locations: classpath:data-mysql.sql
  ```
- [ ] **application-prod.yml** (perfil produção)
- [ ] **JdbcConfig.java** (força inicialização eager do DataSource)
  ```java
  @Configuration
  public class JdbcConfig {
      @Bean
      JdbcTemplate jdbcTemplate(DataSource dataSource) {
          return new JdbcTemplate(dataSource);
      }
  }
  ```
- [ ] **UseCaseConfig.java** (beans dos use cases)

### 6️⃣ Testes ⚠️ **ANTES DO DOCKERFILE**

**⚠️ REGRA FUNDAMENTAL:** NUNCA criar Dockerfile antes de garantir 80% de cobertura!

#### 6.1 **Testes Unitários de Domínio**
- [ ] Testar Value Objects (Cpf, Email)
- [ ] Testar Entities (Cliente)
- [ ] Testar Domain Exceptions

#### 6.2 **Testes Unitários de Use Cases**
- [ ] Testar cada Use Case isoladamente
- [ ] Mockar gateways com `@Mock`

#### 6.3 **Testes de Integração (Repository)**
- [ ] Usar `@DataJdbcTest` ou `@SpringBootTest`
- [ ] Testar SQL queries reais

#### 6.4 **Testes Unitários de Service**
- [ ] Mockar Use Cases com `@Mock`
- [ ] Testar conversões DTO

#### 6.5 **Testes Unitários de Controller**
- [ ] Mockar Service com `@Mock`
- [ ] Testar HTTP status codes

#### 6.6 **Testes de Exception Handler**
- [ ] Testar todos os `@ExceptionHandler`
- [ ] Validar `ErrorResponse` correto

#### 6.7 **Testes de Configuração**
- [ ] Testar `@Configuration` classes
- [ ] Validar beans não nulos

#### 6.8 **Validação de Cobertura**
```bash
mvn clean test jacoco:report
# Verificar target/site/jacoco/index.html
# ⚠️ MÍNIMO 80% POR MICROSERVIÇO
```

### 7️⃣ Docker & Kubernetes

#### 7.1 **Dockerfile** (só após 80% cobertura!)
- [ ] Multi-stage build (Maven + JRE)
- [ ] Usuário não-root (`appuser`)
- [ ] EXPOSE 8080
- [ ] ENTRYPOINT com JAVA_OPTS

#### 7.2 **Manifests Kubernetes**
- [ ] **ConfigMap** (`{service}-configmap.yaml`)
  ```yaml
  SPRING_SQL_INIT_MODE: "always"
  SPRING_SQL_INIT_PLATFORM: "mysql"
  ```
- [ ] **Deployment** (`{service}-deployment.yaml`)
  - imagePullPolicy: `Never` (minikube)
  - env: variáveis do banco via Secret
- [ ] **Service ClusterIP** (`{service}-service.yaml`)
- [ ] **HPA** (`{service}-hpa.yaml`)

### 8️⃣ Deploy Local (Minikube) ⚠️ **OBRIGATÓRIO**

```bash
# 1. Build da imagem
docker build -t lanchonete-{service}:latest .

# 2. Carregar no minikube
minikube image load lanchonete-{service}:latest

# 3. Aplicar manifests
kubectl apply -f k8s_manifests/{service}/

# 4. Aguardar pods prontos
kubectl wait --for=condition=ready pod -l app={service} --timeout=180s

# 5. Port-forward
kubectl port-forward service/{service}-service 8081:8080 &
```

### 9️⃣ Testes de Endpoints ⚠️ **VIA CURL**

**⚠️ NUNCA criar nada manualmente no banco! Scripts SQL devem rodar automaticamente.**

```bash
# Testar TODOS os endpoints:
curl -X POST http://localhost:8081/endpoint1 -H "Content-Type: application/json" -d '{...}'
curl -X GET http://localhost:8081/endpoint2
# ... etc

# Validar:
# ✅ Status code correto (200, 201, 404, etc)
# ✅ Response JSON correto
# ✅ Dados persistidos no banco
```

### 🔟 Limpeza e Documentação
- [ ] Remover código comentado
- [ ] Atualizar README do serviço
- [ ] **Você** faz commit: `git add . && git commit -m "feat: implementa serviço X"`
- [ ] **Você** faz push: `git push origin branch-name`

**⚠️ IMPORTANTE:** Operações de Git (`git add`, `git commit`, `git push`) são de responsabilidade do usuário!

---

## 📐 ARQUITETURA DE CÓDIGO (Clean Architecture)

### **Estrutura de Pacotes**

```
src/main/java/br/com/lanchonete/{service}/
│
├── domain/                          # 🎯 Núcleo - Regras de Negócio
│   ├── model/                       # Entities e Value Objects
│   │   ├── Cliente.java
│   │   ├── Cpf.java
│   │   └── Email.java
│   └── exceptions/                  # Domain Exceptions
│       ├── ValidacaoException.java
│       └── ClienteNaoEncontradoException.java
│
├── application/                     # 🔧 Casos de Uso
│   ├── usecases/                    # Use Cases (lógica de negócio)
│   │   ├── CadastrarCliente.java
│   │   ├── IdentificarCliente.java
│   │   └── BuscarClientePorCpf.java
│   └── gateways/                    # Interfaces (Ports)
│       └── ClienteGateway.java
│
├── adapters/                        # 🔌 Adaptadores
│   ├── persistence/                 # Adapter Out (BD)
│   │   └── ClienteGatewayJDBC.java
│   └── web/                         # Adapter In (HTTP)
│       ├── controller/
│       │   ├── ClienteController.java
│       │   └── ExceptionHandlerController.java
│       ├── service/
│       │   └── ClienteService.java  # ⚠️ Orquestra Use Cases + DTO
│       └── dto/
│           ├── ClienteRequest.java
│           ├── ClienteResponse.java
│           └── ErrorResponse.java
│
└── infrastructure/                  # ⚙️ Configuração
    └── config/
        ├── JdbcConfig.java          # DataSource eager initialization
        └── UseCaseConfig.java       # Beans dos Use Cases
```

### **Fluxo de Dados (Request → Response)**

```
HTTP Request
    ↓
ClienteController        # 1. Recebe ClienteRequest (DTO)
    ↓
ClienteService           # 2. Converte DTO → Domain Model
    ↓                    # 3. Chama Use Case
CadastrarCliente         # 4. Executa lógica de negócio
    ↓                    # 5. Chama Gateway (interface)
ClienteGateway
    ↓
ClienteGatewayJDBC       # 6. Persiste no banco
    ↓
Database
    ↓
Cliente (Domain Model)   # 7. Retorna Entity
    ↓
ClienteService           # 8. Converte Domain → DTO
    ↓
ClienteResponse (DTO)    # 9. Retorna para Controller
    ↓
HTTP Response (JSON)
```

### **⚠️ CAMADA SERVICE: Por que existe?**

A camada `Service` **não faz parte do Clean Architecture tradicional**, mas foi adicionada para:

1. **Orquestrar múltiplos Use Cases**
   - Exemplo: Checkout pode precisar validar cliente + criar pedido
2. **Converter DTOs ↔ Domain Models**
   - Isola Controllers dos detalhes do domínio
3. **Simplificar Controllers**
   - Controller apenas recebe/retorna JSON
4. **Transações declarativas**
   - `@Transactional` no Service

**Regra de Ouro:** Service **NÃO** contém lógica de negócio! Apenas orquestra Use Cases.

---

## 📝 PADRÕES E CONVENÇÕES

### **Nomenclatura**

| Tipo | Padrão | Exemplo |
|------|--------|---------|
| Use Case | Verbo no infinitivo | `CadastrarCliente` |
| Service | Substantivo + Service | `ClienteService` |
| Controller | Substantivo + Controller | `ClienteController` |
| Gateway | Substantivo + Gateway | `ClienteGateway` |
| DTO Request | Substantivo + Request | `ClienteRequest` |
| DTO Response | Substantivo + Response | `ClienteResponse` |
| Exception | Descrição + Exception | `ClienteNaoEncontradoException` |

### **Testes**

| Tipo | Padrão | Exemplo |
|------|--------|---------|
| Método de teste | `t1()`, `t2()`, etc | `void t1() { ... }` |
| DisplayName | Descrição em português | `@DisplayName("Deve cadastrar cliente com sucesso")` |
| Mocks | `@Mock` + `@ExtendWith(MockitoExtension.class)` | - |
| Config tests | `@ContextConfiguration` + Spring | - |

### **application.yml**

```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}

  sql:
    init:
      mode: always              # ⚠️ OBRIGATÓRIO
      platform: mysql           # ⚠️ OBRIGATÓRIO
      schema-locations: classpath:schema-mysql.sql
      data-locations: classpath:data-mysql.sql
```

### **ConfigMap Kubernetes**

```yaml
data:
  SPRING_SQL_INIT_MODE: "always"        # ⚠️ OBRIGATÓRIO
  SPRING_SQL_INIT_PLATFORM: "mysql"     # ⚠️ OBRIGATÓRIO
  SPRING_PROFILES_ACTIVE: "prod"
```

---

## ⚠️ REGRAS FUNDAMENTAIS

### ❌ **NUNCA FAÇA ISSO:**

1. ❌ Criar Dockerfile antes de 80% cobertura
2. ❌ Criar dados manualmente no banco via `kubectl exec`
3. ❌ Pular testes unitários ("testo depois")
4. ❌ Colocar lógica de negócio no Controller
5. ❌ Colocar lógica de negócio no Service
6. ❌ Esquecer `JdbcConfig.java` (DataSource não inicializa!)
7. ❌ Esquecer `SPRING_SQL_INIT_*` no ConfigMap
8. ❌ Usar `imagePullPolicy: Always` no Minikube

### ✅ **SEMPRE FAÇA ISSO:**

1. ✅ Testes ANTES de Docker/K8s
2. ✅ Scripts SQL devem rodar automaticamente no startup
3. ✅ Testar todos os endpoints via curl após deploy
4. ✅ Seguir o fluxo: Código → Testes (80%) → Docker → K8s → Curl
5. ✅ Verificar logs: `kubectl logs -l app={service}`
6. ✅ Validar tabelas: `kubectl exec mysql-{service}-0 -- mysql ...`
7. ✅ Usar `@DisplayName` em todos os testes
8. ✅ Mockar dependências com `@Mock`
9. ✅ **Você** controla Git: `git add`, `git commit`, `git push` (nunca automatizado)

---

## 📝 OBSERVAÇÕES IMPORTANTES

### **Autenticação Simplificada**
- ✅ Identificação via CPF (sem JWT/tokens)
- ✅ Pedidos podem ser anônimos (cpfCliente = null)
- ✅ Validação REST ao serviço Clientes

### **Snapshot de Preços**
- ✅ Já implementado em `app/autoatendimento/`
- ✅ Preço do produto é copiado para `item_pedido.valor_unitario`
- ✅ Pedidos antigos mantêm preço original se produto mudar

### **Secrets**
- ✅ Nunca commitar secrets no Git
- ✅ Usar `./scripts/create-secrets.sh` para criar
- ✅ Cada banco tem seu secret isolado

### **Logs**
- ✅ Observabilidade via `kubectl logs`
- ✅ Sem Prometheus/Grafana para simplicidade
- ✅ CloudWatch automático no EKS

### **Controle de Versão (Git)** ⚠️

**Operações de Git são de RESPONSABILIDADE DO USUÁRIO:**

```bash
# Após implementar um serviço completo:

# 1. Verificar mudanças
git status

# 2. Adicionar arquivos
git add services/clientes/
git add k8s_manifests/clientes/
git add README.md

# 3. Commit com mensagem descritiva
git commit -m "feat(clientes): implementa microserviço de clientes

- Implementa Clean Architecture
- Adiciona 58 testes (95% cobertura)
- Cria Dockerfile e manifests K8s
- Testa todos endpoints via curl"

# 4. Push para repositório
git push origin feature/migracao-microservicos
```

**Regras:**
- ❌ Assistente NUNCA executa `git add`, `git commit` ou `git push`
- ✅ Usuário controla quando e o que commitar
- ✅ Usuário escreve mensagens de commit
- ✅ Usuário decide quando fazer push

---

## 📊 STATUS DE IMPLEMENTAÇÃO

### **Microserviços Implementados**

| Serviço | Status | Porta | NodePort | Integrações | Testes |
|---------|--------|-------|----------|-------------|--------|
| **Clientes** | ✅ Operacional | 8080 | 30083 | REST (consumido por Pedidos) | ✅ Validado |
| **Pedidos** | ✅ Operacional | 8080 | 30081 | REST → Clientes<br>RabbitMQ ↔ Pagamento | ✅ Validado |
| **Pagamento** | ✅ Operacional | 8081 | 30084 | RabbitMQ ↔ Pedidos | ✅ Validado |
| **Cozinha** | ⏳ Pendente | 8082 | 30082 | - | - |

### **Validações Realizadas**

#### ✅ **Integração REST (Pedidos → Clientes)**
```bash
# Teste: Criar pedido COM CPF para validar Feign Client
curl -X POST http://192.168.49.2:30081/pedidos \
  -H "Content-Type: application/json" \
  -d '{"cpfCliente":"12345678900","itens":[{"produtoId":1,"quantidade":2}]}'

# Resultado: ✅ clienteNome preenchido via Feign
{
  "id": 9,
  "numeroPedido": "PED-000009",
  "cpfCliente": "12345678900",
  "clienteNome": "Teste Cliente",  ← Buscado do serviço de Clientes!
  "status": "CRIADO",
  "valorTotal": 41.80
}
```

#### ✅ **Integração RabbitMQ (Pedidos ↔ Pagamento)**
```bash
# Teste: Criar pedido e aguardar atualização de status via eventos
curl -s -X POST http://192.168.49.2:30081/pedidos \
  -H "Content-Type: application/json" \
  -d '{"cpfCliente":null,"itens":[{"produtoId":1,"quantidade":1}]}' \
  | jq -r '.id,.status'
# Output: 9
#         CRIADO

# Aguardar 5 segundos (processamento assíncrono)
sleep 5

# Verificar status atualizado
curl -s http://192.168.49.2:30081/pedidos/9 | jq -r '.status'
# Output: REALIZADO  ← Mudou de CRIADO para REALIZADO via RabbitMQ!
```

**Fluxo de Eventos Validado:**
1. Pedidos publica `PedidoCriado` → Exchange `pedido.events`
2. Pagamento consome evento da fila `pagamentos.pedido-criado`
3. Pagamento processa (mock 80% aprovação)
4. Pagamento publica `PagamentoAprovado` → Exchange `pagamento.events`
5. Pedidos consome evento da fila `pedidos.pagamento-aprovado`
6. Pedidos atualiza status: `CRIADO` → `REALIZADO`

#### ✅ **RabbitMQ Exchanges e Bindings**
```bash
# Verificar exchanges criados
kubectl exec rabbitmq-0 -- rabbitmqadmin -u admin -p rabbitmq123 list exchanges \
  | grep -E "pedido|pagamento"

# Resultado:
| pedido.events      | direct  |
| pagamento.events   | direct  |

# Verificar bindings
kubectl exec rabbitmq-0 -- rabbitmqadmin -u admin -p rabbitmq123 list bindings \
  | grep -E "pedido|pagamento"

# Resultado:
pedido.events → pagamentos.pedido-criado → pedido.criado
pagamento.events → pedidos.pagamento-aprovado → pagamento.aprovado
pagamento.events → pedidos.pagamento-rejeitado → pagamento.rejeitado
```

### **Problemas Resolvidos**

Durante a implementação do serviço de Pedidos, foram identificados e corrigidos 7 problemas críticos:

1. ✅ **MySQL Secret Incorreto** - Deployment referenciando secret genérico ao invés de `mysql-pedidos-secret`
2. ✅ **RabbitMQ Exchange Type Mismatch** - Pedidos usando `TopicExchange` enquanto Pagamento usa `DirectExchange`
3. ✅ **Feign Client - Porta Errada** - URL do Clientes configurada com porta 8083 (deveria ser 8080)
4. ✅ **Feign Client - Endpoint Errado** - Endpoint `/clientes/{cpf}` ao invés de `/clientes/cpf/{cpf}`
5. ✅ **NodePort Conflict** - Porta 30080 já alocada pelo autoatendimento
6. ✅ **Minikube Stopped** - Cluster parado durante deploy
7. ✅ **RabbitMQ Bindings** - Bindings não criados automaticamente (service restart necessário)

**Documentação detalhada:** Consulte [TROUBLESHOOTING.md](./TROUBLESHOOTING.md) para detalhes completos sobre cada problema e solução.

### **Mapa de Portas (Atualizado)**

| Serviço | Porta Interna | NodePort | URL Minikube |
|---------|--------------|----------|--------------|
| Autoatendimento | 8080 | 30080 | http://192.168.49.2:30080 |
| **Pedidos** | 8080 | **30081** | http://192.168.49.2:30081 |
| Cozinha | 8082 | 30082 | http://192.168.49.2:30082 |
| **Clientes** | 8080 | 30083 | http://192.168.49.2:30083 |
| **Pagamento** | 8081 | 30084 | http://192.168.49.2:30084 |

### **Próximos Passos**

1. ⏳ Implementar microserviço de **Cozinha**
2. ⏳ Remover aplicação monolítica **Autoatendimento**
3. ⏳ Configurar **Ingress** para AWS EKS
4. ⏳ Implementar testes E2E completos
5. ⏳ Configurar CI/CD no GitHub Actions

---

## 🆘 TROUBLESHOOTING

Para documentação completa de problemas e soluções, consulte **[TROUBLESHOOTING.md](./TROUBLESHOOTING.md)**.

Este documento contém:
- 📋 Todos os problemas encontrados durante a implementação
- 🔧 Soluções passo-a-passo com exemplos de código
- ✅ Testes de validação para cada integração
- 🧪 Comandos úteis de debug (logs, RabbitMQ, MySQL, secrets, pods)
- 📊 Checklist de validação completo

### **Referência Rápida**

#### **Pod não inicia**
```bash
kubectl describe pod <pod-name>
kubectl logs <pod-name>
```

#### **Banco não conecta**
```bash
# Verificar se StatefulSet está pronto
kubectl get statefulset

# Testar conexão
kubectl exec -it mysql-clientes-0 -- mysql -u root -p
```

#### **RabbitMQ não recebe mensagens**
```bash
# Acessar Management UI
kubectl port-forward svc/rabbitmq-service 15672:15672
# Abrir: http://localhost:15672 (admin/rabbitmq123)

# Verificar exchanges
kubectl exec rabbitmq-0 -- rabbitmqadmin -u admin -p rabbitmq123 list exchanges

# Verificar bindings
kubectl exec rabbitmq-0 -- rabbitmqadmin -u admin -p rabbitmq123 list bindings
```

#### **Minikube service não responde**
```bash
minikube service <service-name> --url
curl $(minikube service <service-name> --url)/actuator/health
```


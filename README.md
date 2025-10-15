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
│                    http://192.168.49.2:30083 → Clientes                             │
│                    http://192.168.49.2:30080 → Pedidos                              │
│                    http://192.168.49.2:30082 → Cozinha                              │
│                    http://192.168.49.2:30081 → Pagamento                            │
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

### **1. CLIENTES** (Port: 8083)

**Responsabilidade:** Identificação e cadastro de clientes

**Tecnologia:** Spring Boot 3 + Java 17 + MySQL

**Banco de Dados:** MySQL StatefulSet (`clientes_db`)

**Endpoints:**
- `POST /clientes/identificar` - Identifica cliente por CPF
- `POST /clientes` - Cadastra novo cliente
- `GET /clientes/{cpf}` - Busca cliente por CPF

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

### **2. PEDIDOS** (Port: 8080)

**Responsabilidade:** Checkout, gestão de pedidos e produtos

**Tecnologia:** Spring Boot 3 + Java 17 + MySQL + RabbitMQ + OpenFeign

**Banco de Dados:** MySQL StatefulSet (`pedidos_db`)

**Endpoints:**
- `POST /pedidos/checkout` - Cria novo pedido
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

### **4. PAGAMENTO** (Port: 8081)

**Responsabilidade:** Processamento de pagamentos (mock)

**Tecnologia:** Spring Boot 3 + Java 17 + MongoDB + RabbitMQ

**Banco de Dados:** MongoDB StatefulSet (`pagamentos` collection)

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

### **Deploy EKS (Produção)**

```bash
# 1. Configurar kubectl
aws eks update-kubeconfig --name lanchonete-cluster --region us-east-1

# 2. Criar secrets
./scripts/create-secrets.sh

# 3. Deploy databases
kubectl apply -f k8s/databases/

# 4. Aguardar databases
kubectl wait --for=condition=ready pod -l app=mysql-clientes --timeout=300s
kubectl wait --for=condition=ready pod -l app=mysql-pedidos --timeout=300s
kubectl wait --for=condition=ready pod -l app=mysql-cozinha --timeout=300s
kubectl wait --for=condition=ready pod -l app=mongodb --timeout=300s
kubectl wait --for=condition=ready pod -l app=rabbitmq --timeout=300s

# 5. Deploy services
kubectl apply -f k8s/services/

# 6. Deploy Ingress (ALB)
kubectl apply -f k8s/aws/ingress.yaml

# 7. Aguardar ALB provisionar
kubectl wait --for=condition=available --timeout=300s ingress/lanchonete-ingress

# 8. Obter URL do ALB
kubectl get ingress lanchonete-ingress -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'
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

Usar este checklist ao implementar cada serviço:

### Código
- [ ] Spring Boot configurado
- [ ] application.yml (DB + RabbitMQ)
- [ ] Entities/Models
- [ ] Repository
- [ ] Service
- [ ] Controller
- [ ] DTOs
- [ ] Feign Clients (se REST)
- [ ] RabbitMQ Config (se eventos)
- [ ] Exception handlers

### Testes
- [ ] Testes unitários (Service)
- [ ] Testes de integração (Controller)
- [ ] Coverage > 80%

### Docker/K8s
- [ ] Dockerfile
- [ ] Deployment.yaml
- [ ] Service.yaml (ClusterIP)
- [ ] NodePort.yaml (Minikube)
- [ ] ConfigMap (se necessário)

### Validação
- [ ] Build passa (`mvn clean install`)
- [ ] Roda local (docker-compose)
- [ ] Roda Minikube (`kubectl apply`)
- [ ] Endpoints testados (curl/Postman)

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

---

## 🆘 TROUBLESHOOTING

### **Pod não inicia**
```bash
kubectl describe pod <pod-name>
kubectl logs <pod-name>
```

### **Banco não conecta**
```bash
# Verificar se StatefulSet está pronto
kubectl get statefulset

# Testar conexão
kubectl exec -it mysql-clientes-0 -- mysql -u root -p
```

### **RabbitMQ não recebe mensagens**
```bash
# Acessar Management UI
kubectl port-forward svc/rabbitmq-service 15672:15672
# Abrir: http://localhost:15672 (guest/guest)
```

### **Minikube service não responde**
```bash
minikube service <service-name> --url
curl $(minikube service <service-name> --url)/actuator/health
```


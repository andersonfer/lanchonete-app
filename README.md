## 📖 Sobre o Projeto

Este projeto implementa um sistema de autoatendimento para lanchonetes, seguindo os princípios de **Clean Architecture** e **Domain-Driven Design (DDD)**.


### Arquitetura Geral

```
                                           ┌─────────────────────┐
                                           │   Amazon Cognito    │
                                           │   (User Pool)       │
                                           │                     │
                                           └──────────▲──────────┘
                                                      │
┌──────────┐         ┌──────────────────────┐         │
│  Cliente │────────▶│   API Gateway        │─────────┘
└──────────┘         │   (REST API)         │
                     │                      │
                     └──────────┬───────────┘
                                │
                     ┌──────────▼───────────┐
                     │  Lambda Function     │
                     │  (Autenticação)      │
                     └──────────┬───────────┘
                                │
                     ┌──────────▼───────────────────────────┐
                     │   Application Load Balancers         │
                     │   (ALB Controller)                   │
                     └──────────┬───────────────────────────┘
                                │
           ┌────────────────────┼────────────────────┐
           │                    │                    │
    ┌──────▼──────┐      ┌──────▼──────┐     ┌──────▼──────┐
    │ Auto-       │      │  Pagamento  │     │   Amazon    │
    │ atendimento │─────▶│  (Callback) │     │   RDS       │
    │   (EKS)     │      │    (EKS)    │     │  (MySQL)    │
    └─────────────┘      └─────────────┘     └─────────────┘
```

### Infraestrutura

#### Gerenciamento de identidade
**Amazon Cognito User Pool** 
- Armazena usuários cadastrados (CPFs)
- Gera e valida tokens JWT
- Autenticação via CPF (sem senha)
- Suporte a usuários anônimos
- **Localização**: `infra/auth/`

#### API Gateway
**Amazon API Gateway**
- Authorizer: Cognito (validação automática de tokens)
- Endpoints protegidos via JWT
- **Localização**: `infra/api-gateway/`

#### Função de Autenticação
**AWS Lambda**
- Recebe CPF do cliente
- Consulta/cria usuário no Cognito (auto-cadastro)
- Gera tokens JWT válidos
- Retorna token para o cliente
- **Localização**: `infra/lambda/`

#### Balanceamento de Carga
**AWS Load Balancer Controller**
- Cria Application Load Balancers via Kubernetes Ingress
- Distribui tráfego entre pods das aplicações
- Health checks automáticos
- Integração nativa com EKS
- **Localização**: `infra/ingress/`

#### Aplicações
**Amazon EKS (Elastic Kubernetes Service)** 
- Orquestração de contêineres
- **Aplicações**:
  - **Autoatendimento**: Aplicação principal (gerencia pedidos, produtos, clientes)
  - **Pagamento**: Processamento de pagamento e callback
- **Localização**: `infra/kubernetes/` e `k8s_manifests/`
**Amazon ECR** 
- Registro de contêineres
- Repositórios privados para imagens Docker:
  - `lanchonete-autoatendimento`
  - `lanchonete-pagamento`
- **Localização**: `infra/ecr/`

#### Banco de dados gerenciado
**Amazon RDS MySQL 8.0** - Banco de dados gerenciado
- Instância: `db.t3.micro`
- Storage: 20GB (gp2)
- Acesso exclusivo do serviço Autoatendimento
- **Localização**: `infra/database/`

#### Gerenciamento de Estado Terraform
- **S3 Bucket**: Armazena estado centralizado
- **DynamoDB**: Controla locks para prevenir conflitos
- **Localização**: `infra/backend/`



### Deploy da Infraestrutura

A infraestrutura é provisionada via **Terraform** de forma modular e sequencial:

```bash
# 1. Backend (S3 + DynamoDB)
cd infra/backend && terraform apply

# 2. Repositórios ECR
cd infra/ecr && terraform apply

# 3. Banco de Dados RDS
cd infra/database && terraform apply

# 4. Cluster EKS
cd infra/kubernetes && terraform apply

# 5. ALB Controller
cd infra/ingress && terraform apply

# 6. Sistema de Autenticação
cd infra/lambda && ./build.sh
cd infra/auth && terraform apply
cd infra/lambda && terraform apply
cd infra/api-gateway && terraform apply
```

### Deploy das Aplicações

Após a infraestrutura provisionada, as aplicações Java são deployadas no Kubernetes:

```bash
# 1. Configurar kubectl para acessar o cluster EKS
aws eks update-kubeconfig --region us-east-1 --name lanchonete-cluster

# 2. Atualizar manifestos Kubernetes com URLs dinâmicas
./scripts/update-manifests.sh

# 3. Criar secrets do RDS no cluster
./scripts/create-secrets.sh

# 4. Build e push das imagens Docker para ECR
./scripts/build-and-push.sh

# 5. Deploy das aplicações no Kubernetes
./scripts/deploy-k8s.sh
```

## 🔄 CI/CD

### Estrutura de Repositório (Monorepo)

Este projeto utiliza uma abordagem de **monorepo**, onde toda a infraestrutura e código da aplicação estão centralizados em um único repositório.

A estrutura está organizada de forma modular, simulando a separação lógica que existiria em múltiplos repositórios:

```
lanchonete-app/
├── infra/
│   ├── backend/          # Estado do Terraform (S3 + DynamoDB)
│   ├── ecr/              # Repositórios Docker
│   ├── database/         # RDS MySQL
│   ├── kubernetes/       # Cluster EKS
│   ├── ingress/          # ALB Controller
│   ├── auth/             # Cognito User Pool
│   ├── lambda/           # Lambda de Autenticação
│   └── api-gateway/      # API Gateway + Authorizer
├── app/
│   ├── autoatendimento/  # Aplicação principal (Spring Boot)
│   └── pagamento/        # Serviço de pagamento (Spring Boot)
├── k8s_manifests/        # Manifestos Kubernetes
└── scripts/              # Scripts de automação
```

### Pipelines CI/CD

O projeto foi estruturado para suportar 4 pipelines independentes via GitHub Actions, cada um responsável por uma parte específica da infraestrutura e aplicação:

#### Pipeline 1: Infraestrutura Base
**Trigger**: Pull Request → main (paths: `infra/backend/**`, `infra/ecr/**`, `infra/database/**`)

**Responsabilidades**:
- Provisionar backend Terraform (S3 + DynamoDB)
- Criar repositórios ECR
- Provisionar banco de dados RDS MySQL

**Diretórios envolvidos**:
- `infra/backend/`
- `infra/ecr/`
- `infra/database/`

**Testes**:
- Validação de sintaxe Terraform
- Teste de conectividade com RDS
- Verificação de autenticação AWS


#### Pipeline 2: Infraestrutura Kubernetes
**Trigger**: Pull Request → main (paths: `infra/kubernetes/**`, `infra/ingress/**`)

**Responsabilidades**:
- Provisionar cluster EKS
- Configurar AWS Load Balancer Controller
- Configurar kubectl

**Diretórios envolvidos**:
- `infra/kubernetes/`
- `infra/ingress/`

**Dependências**: Pipeline 1 (Base)

**Testes**:
- Verificar nodes do cluster ativos
- Validar ALB Controller instalado
- Health check dos componentes do EKS


#### Pipeline 3: Sistema de Autenticação
**Trigger**: Pull Request → main (paths: `infra/auth/**`, `infra/lambda/**`, `infra/api-gateway/**`)

**Responsabilidades**:
- Build da Lambda Function (Java)
- Provisionar Cognito User Pool
- Configurar API Gateway com Authorizer

**Diretórios envolvidos**:
- `infra/auth/`
- `infra/lambda/`
- `infra/api-gateway/`

**Dependências**: Pipeline 1 (Base)

**Testes**:
- Validar build da Lambda
- Testar geração de tokens
- Health check do API Gateway


#### Pipeline 4: Deploy da Aplicação
**Trigger**: Pull Request → main (paths: `app/**`, `k8s_manifests/**`, `scripts/**`)

**Responsabilidades**:
- Executar testes unitários (JUnit)
- Build das imagens Docker
- Push para ECR
- Deploy no Kubernetes

**Diretórios envolvidos**:
- `app/autoatendimento/`
- `app/pagamento/`
- `k8s_manifests/`
- `scripts/`

**Dependências**: Pipeline 2 (Kubernetes) + Pipeline 3 (Autenticação)

**Testes**:
- Testes unitários Java (Maven)
- Validação de build Docker
- Verificação de pods healthy


### Secrets Necessários

Configure os seguintes secrets no GitHub (Settings → Secrets and Variables → Actions):

```yaml
AWS_ACCESS_KEY_ID: <sua-access-key>
AWS_SECRET_ACCESS_KEY: <sua-secret-key>
AWS_SESSION_TOKEN: <seu-session-token> 
AWS_DEFAULT_REGION: us-east-1
```

### Comunicação Entre Pipelines

Os pipelines compartilham informações através de:

**Terraform State (S3)**:
- URLs dos repositórios ECR
- Endpoint do RDS
- Nome do cluster EKS
- URL do API Gateway

**AWS Systems Manager Parameter Store**:
- Senha do banco RDS (SecureString)

**Kubernetes API**:
- URLs dos Application Load Balancers
- Status dos pods e serviços


# Sistema de Autoatendimento - Modelagem e Estrutura de Banco de Dados

## 1. Contexto

Este documento apresenta a modelagem conceitual (MER) e lógica/física (DER) do banco de dados do sistema de autoatendimento para lanchonete, além das melhorias implementadas para otimizar a performance das consultas mais frequentes sem alterar o funcionamento da aplicação.

## 2. Modelo Conceitual (MER)

O Modelo Entidade-Relacionamento (MER) apresenta as entidades principais **Cliente**, **Pedido**, **Produto** e **Item de Pedido** (tabela associativa), bem como seus relacionamentos:

- **Cliente** `1:N` **Pedido** - Um cliente realiza vários pedidos
- **Pedido** `1:N` **Item de Pedido** - Um pedido contém vários itens
- **Produto** `1:N` **Item de Pedido** - Um produto compõe vários itens de pedido

![Modelo Conceitual](diagramas/MER.png)

## 3. Modelo Lógico/Físico (DER)

O Modelo Entidade-Relacionamento Físico (DER) mostra as tabelas **cliente**, **pedido**, **produto** e **item_pedido**, suas colunas, tipos de dados, chaves primárias e estrangeiras.

![Modelo Lógico/Físico](diagramas/DER.png)

## 4. Melhorias Implementadas

Para otimizar a performance das consultas mais frequentes no sistema, foram criados índices estratégicos nas seguintes colunas:

### Índices Criados

```sql
CREATE INDEX idx_produto_categoria ON produto(categoria);
CREATE INDEX idx_pedido_status ON pedido(status);
CREATE INDEX idx_pedido_status_pagamento ON pedido(status_pagamento);
CREATE INDEX idx_pedido_data_criacao ON pedido(data_criacao);
CREATE INDEX idx_item_pedido_pedido_id ON item_pedido(pedido_id);
CREATE INDEX idx_item_pedido_produto_id ON item_pedido(produto_id);
```

### Impacto das Melhorias

Os índices foram implementados para melhorar a performance das seguintes operações:
- Listagem de produtos filtrados por categoria (LANCHE, BEBIDA, ACOMPANHAMENTO, SOBREMESA)
- Consultas de pedidos por status no painel operacional da cozinha
- Verificação de status de pagamento dos pedidos
- Ordenação cronológica de pedidos
- Consulta de itens de um pedido específico
- Geração de relatórios de vendas por produto

## 5. Justificativa da Escolha do MySQL

O MySQL foi escolhido por sua conformidade ACID essencial para transações financeiras, suporte nativo no AWS RDS que facilita gerenciamento e escalabilidade em cloud, e performance adequada para cargas OLTP.
## 6. Scripts de Banco de Dados

Os scripts SQL estão localizados em `infra/database/scripts/`:
- `001_schema.sql` - Criação das tabelas e índices
- `002_data.sql` - Carga inicial de dados (produtos e cliente de teste)
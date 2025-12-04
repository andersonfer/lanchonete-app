# Lanchonete - Sistema de Gestão com Microsserviços

## Tech Challenge FIAP - Fase 4

Sistema de autoatendimento para lanchonete, desenvolvido com arquitetura de microsserviços, contemplando todo o fluxo desde o cadastro do cliente até a entrega do pedido.

### Visão Geral

O sistema é composto por **4 microsserviços** independentes:

| Serviço | Repositório | Descrição |
|---------|-------------|-----------|
| **Clientes** | [lanchonete-clientes](https://github.com/andersonfer/lanchonete-clientes) | Cadastro e identificação de clientes por CPF |
| **Pedidos** | [lanchonete-pedidos](https://github.com/andersonfer/lanchonete-pedidos) | Registro de pedidos e catálogo de produtos |
| **Pagamento** | [lanchonete-pagamento](https://github.com/andersonfer/lanchonete-pagamento) | Processamento de pagamentos via integração externa |
| **Cozinha** | [lanchonete-cozinha](https://github.com/andersonfer/lanchonete-cozinha) | Gestão da fila de preparo e atualização de status |
| **Infraestrutura** | [lanchonete-infra](https://github.com/andersonfer/lanchonete-infra) | Terraform, Kubernetes manifests e scripts de deploy |

### Integrante

| Nome | RM | Discord |
|------|----|---------|
| Anderson Fér | rm363691 | anderson_rm363691 |

---

## Arquitetura

### Diagrama

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              AWS Cloud                                       │
│                                                                              │
│  ┌──────────────┐     ┌──────────────┐                                      │
│  │   Cognito    │◀────│ API Gateway  │                                      │
│  │   (Auth)     │     │   + Lambda   │                                      │
│  └──────────────┘     └──────┬───────┘                                      │
│                              │                                               │
│                              ▼                                               │
│  ┌───────────────────────────────────────────────────────────────────────┐  │
│  │                        Amazon EKS                                      │  │
│  │                                                                        │  │
│  │   ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐       │  │
│  │   │ Clientes │    │ Pedidos  │    │Pagamento │    │ Cozinha  │       │  │
│  │   │ Service  │◀──▶│ Service  │    │ Service  │    │ Service  │       │  │
│  │   └────┬─────┘    └────┬─────┘    └────┬─────┘    └────┬─────┘       │  │
│  │        │               │               │               │              │  │
│  │        │          ┌────┴───────────────┴───────────────┴────┐        │  │
│  │        │          │            RabbitMQ                      │        │  │
│  │        │          │  (pedido.criado, pagamento.aprovado,     │        │  │
│  │        │          │   cozinha.pedido-pronto)                 │        │  │
│  │        │          └──────────────────────────────────────────┘        │  │
│  └────────┼──────────────────────────────────────────────────────────────┘  │
│           │                    │               │                            │
│           ▼                    ▼               ▼                            │
│  ┌──────────────┐     ┌──────────────┐  ┌──────────────┐                   │
│  │  Amazon RDS  │     │  Amazon RDS  │  │MongoDB Atlas │                   │
│  │   (MySQL)    │     │   (MySQL)    │  │   (NoSQL)    │                   │
│  │  Clientes    │     │Pedidos/Cozinha│  │  Pagamento   │                   │
│  └──────────────┘     └──────────────┘  └──────────────┘                   │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Bancos de Dados

| Serviço | Tipo | Tecnologia |
|---------|------|------------|
| Clientes | SQL | MySQL (RDS) |
| Pedidos | SQL | MySQL (RDS) |
| Cozinha | SQL | MySQL (RDS) |
| Pagamento | NoSQL | MongoDB Atlas |

### Comunicação entre Serviços

| Origem | Destino | Método | Evento/Endpoint |
|--------|---------|--------|-----------------|
| Pedidos | Clientes | HTTP REST | Validação de cliente |
| Pedidos | Pagamento | RabbitMQ | `pedido.criado` |
| Pagamento | Pedidos | RabbitMQ | `pagamento.aprovado` / `pagamento.rejeitado` |
| Pagamento | Cozinha | RabbitMQ | `pagamento.aprovado` |
| Cozinha | Pedidos | RabbitMQ | `cozinha.pedido-pronto` |

### Infraestrutura

| Serviço | Função |
|---------|--------|
| EKS | Gerenciamento dos microsserviços |
| ECR | Armazenamento das imagens Docker |
| RDS MySQL | Banco de dados relacional |
| MongoDB Atlas | Banco de dados de documentos |
| Cognito | Autenticação de usuários |
| API Gateway | Roteamento e segurança das APIs |
| Lambda | Autorização customizada |
| RabbitMQ (EKS) | Comunicação assíncrona |

---

## Fluxo E2E da Aplicação

O sistema implementa o fluxo completo de autoatendimento de uma lanchonete:

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│  1. Cliente │    │  2. Criar   │    │ 3. Processar│    │  4. Fila    │    │ 5. Pedido   │
│ Identifica  │───▶│   Pedido    │───▶│  Pagamento  │───▶│   Cozinha   │───▶│   Pronto    │
│   (CPF)     │    │             │    │             │    │             │    │             │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
     │                   │                  │                  │                  │
     ▼                   ▼                  ▼                  ▼                  ▼
 [Clientes]          [Pedidos]         [Pagamento]        [Cozinha]          [Pedidos]
```

### Etapas do Fluxo

| Etapa | Serviço | Descrição |
|-------|---------|-----------|
| 1 | Clientes | Cliente se identifica pelo CPF ou realiza cadastro |
| 2 | Pedidos | Cliente seleciona produtos e cria o pedido |
| 3 | Pagamento | Sistema processa o pagamento (integração externa) |
| 4 | Cozinha | Pedido entra na fila de preparo |
| 5 | Cozinha | Atualização de status: Recebido → Em Preparo → Pronto → Finalizado |

### Status do Pedido

| Status | Descrição |
|--------|-----------|
| `RECEBIDO` | Pedido criado, aguardando pagamento |
| `EM_PREPARACAO` | Pagamento confirmado, pedido na cozinha |
| `PRONTO` | Pedido finalizado, aguardando retirada |
| `FINALIZADO` | Pedido entregue ao cliente |

---

## CI/CD

Cada microsserviço possui pipelines independentes de integração e entrega contínua.

### Pipeline de CI (Pull Request)

Executado automaticamente em cada Pull Request:

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Build     │───▶│   Testes    │───▶│ SonarCloud  │
│   (Maven)   │    │   (JUnit)   │    │  (Quality)  │
└─────────────┘    └─────────────┘    └─────────────┘
```

- **Build**: Compilação com Maven
- **Testes**: Execução de testes unitários e BDD
- **SonarCloud**: Análise de qualidade e cobertura de código

### Pipeline de CD (Merge na Main)

Executado automaticamente após merge na branch `main`:

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Build     │───▶│   Docker    │───▶│  Push ECR   │───▶│ Deploy EKS  │
│   (Maven)   │    │   Build     │    │             │    │             │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
```

- **Build**: Compilação do artefato
- **Docker Build**: Construção da imagem
- **Push ECR**: Publicação no Amazon ECR
- **Deploy EKS**: Atualização do deployment no Kubernetes

### Branch Protection

Todas as branches `main` estão protegidas com:

- Status check obrigatório: **Testes e Análise SonarCloud**
- Merge apenas via Pull Request

---

## Testes

### Cobertura de Código

Todos os microsserviços possuem cobertura de testes monitorada pelo **SonarCloud**:

| Serviço | SonarCloud |
|---------|------------|
| Clientes | [![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=andersonfer_lanchonete-clientes&metric=coverage)](https://sonarcloud.io/project/overview?id=andersonfer_lanchonete-clientes) |
| Pedidos | [![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=andersonfer_lanchonete-pedidos&metric=coverage)](https://sonarcloud.io/project/overview?id=andersonfer_lanchonete-pedidos) |
| Pagamento | [![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=andersonfer_lanchonete-pagamento&metric=coverage)](https://sonarcloud.io/project/overview?id=andersonfer_lanchonete-pagamento) |
| Cozinha | [![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=andersonfer_lanchonete-cozinha&metric=coverage)](https://sonarcloud.io/project/overview?id=andersonfer_lanchonete-cozinha) |

### BDD (Behavior Driven Development)

Todos os serviços implementam testes BDD com **Cucumber**, seguindo a especificação Gherkin:

| Serviço | Feature Files |
|---------|---------------|
| Clientes | [identificacao-cliente.feature](https://github.com/andersonfer/lanchonete-clientes/blob/main/src/test/resources/features/identificacao-cliente.feature), [cadastro-cliente.feature](https://github.com/andersonfer/lanchonete-clientes/blob/main/src/test/resources/features/cadastro-cliente.feature) |
| Pedidos | [criar-pedido.feature](https://github.com/andersonfer/lanchonete-pedidos/blob/main/src/test/resources/features/criar-pedido.feature), [consultar-produtos.feature](https://github.com/andersonfer/lanchonete-pedidos/blob/main/src/test/resources/features/consultar-produtos.feature) |
| Pagamento | [processar-pagamento.feature](https://github.com/andersonfer/lanchonete-pagamento/blob/main/src/test/resources/features/processar-pagamento.feature) |
| Cozinha | [consultar-fila.feature](https://github.com/andersonfer/lanchonete-cozinha/blob/main/src/test/resources/features/consultar-fila.feature), [gerenciar-preparo.feature](https://github.com/andersonfer/lanchonete-cozinha/blob/main/src/test/resources/features/gerenciar-preparo.feature) |

---

## Como Executar

### Pré-requisitos

- AWS CLI configurado
- Terraform >= 1.0
- kubectl
- Docker
- Java 17
- Maven

### Deploy da Infraestrutura

```bash
# Clonar repositório de infraestrutura
git clone https://github.com/andersonfer/lanchonete-infra.git
cd lanchonete-infra

# 1. Provisionar infraestrutura AWS (EKS, RDS, ECR, Cognito, etc.)
./scripts/01-deploy-infra.sh

# 2. Build e push das imagens Docker para ECR
./scripts/02-build-and-push.sh

# 3. Deploy dos microsserviços no Kubernetes
./scripts/03-deploy-k8s.sh

# 4. Configurar API Gateway
./scripts/04-apply-api-gateway.sh

# 5. Atualizar URL do serviço de clientes na Lambda
./scripts/05-update-lambda-url.sh
```

### Testes E2E

```bash
# Teste do fluxo completo com cliente anônimo
./test_scripts/test-e2e-anonimo.sh

# Teste do fluxo completo com cliente identificado (CPF)
./test_scripts/test-e2e-identificado.sh
```

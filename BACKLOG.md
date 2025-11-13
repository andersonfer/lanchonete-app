# 📋 BACKLOG - Migração para Microserviços

**Projeto:** Sistema de Lanchonete - Arquitetura de Microserviços
**Branch Atual:** `feature/migracao-microservicos`
**Última Atualização:** 2025-11-13 15:00

---

## 🎯 VISÃO GERAL DO PROJETO

Migração completa da arquitetura monolítica para microserviços distribuídos. Todos os 4 microserviços core estão implementados, funcionais e **DEPLOYADOS EM PRODUÇÃO NA AWS EKS**.

### Status dos Microserviços
- ✅ **Clientes** - Implementado, testado e operacional em AWS EKS (commit: 148c9b2)
- ✅ **Pagamento** - Implementado, testado e operacional em AWS EKS (commit: c67362f)
- ✅ **Pedidos** - Implementado, testado e operacional em AWS EKS (commit: 66f7e45)
- ✅ **Cozinha** - Implementado, testado e operacional em AWS EKS (commit: 0582da6)

### Progresso Geral
- **Microserviços:** 4/4 concluídos (100%)
- **Infraestrutura K8s Local:** StatefulSets MySQL, MongoDB, RabbitMQ (100%)
- **Infraestrutura AWS:** RDS MySQL (3 instâncias) + MongoDB/RabbitMQ em pods (100%)
- **Integrações:** REST (Pedidos→Clientes) + RabbitMQ completas (100%)
- **Testes E2E Local:** Script completo implementado (100%)
- **Testes E2E AWS:** 3 scripts completos e validados (100%)
  - ✅ Cliente anônimo (test-e2e.sh)
  - ✅ Cliente existente (test-e2e-cliente-existente.sh) - NOVO
  - ✅ Cliente novo (test-e2e-cliente-novo.sh) - NOVO
- **Migração AWS:** **CONCLUÍDA (100%)** ✅

---

## ✅ CONCLUÍDO

### Sprint 1 - Infraestrutura Base
- [x] Criação da infraestrutura K8s (MySQL, MongoDB, RabbitMQ) - `d90b4a9`
- [x] Implementação do microserviço de Clientes - `148c9b2`
- [x] Implementação do microserviço de Pagamento - `c67362f`
- [x] Implementação do microserviço de Pedidos - `66f7e45`
- [x] Integração REST: Pedidos → Clientes (validado)
- [x] Integração RabbitMQ: Pedidos ↔ Pagamento (validado)
- [x] Configuração CI/CD básico no GitHub Actions
- [x] Testes unitários dos 3 microserviços (80%+ cobertura)
- [x] Documentação OpenAPI/Swagger dos 3 microserviços - `8dceb73`
- [x] Script de deploy local renomeado (setup.sh → deploy.sh) - `2025-10-23`
- [x] Refatoração dos nomes dos recursos K8s - `9585cbb`

### Sprint 2 - Microserviço de Cozinha - CONCLUÍDO (2025-10-23)
**Status:** ✅ 100% Concluído | **Commit:** 0582da6

#### Implementação Core
- [x] Estrutura Maven + Spring Boot 3 + Java 17
- [x] Clean Architecture (Domain, Application, Adapters, Infrastructure)
- [x] Camada de Domínio (FilaCozinha, StatusFila: AGUARDANDO/EM_PREPARO/PRONTO/REMOVIDO)
- [x] Use Cases:
  - AdicionarPedidoFila (consome PagamentoAprovado)
  - IniciarPreparo (AGUARDANDO → EM_PREPARO)
  - MarcarComoPronto (EM_PREPARO → PRONTO + publica evento)
  - RemoverPedidoFila (consome PedidoRetirado)
- [x] Repository JDBC com MySQL StatefulSet (cozinha_db)
- [x] 35 arquivos Java implementados

#### Integrações
- [x] Feign Client para Pedidos (GET /pedidos/{id})
- [x] RabbitMQ Consumer:
  - Consome: PagamentoAprovado (exchange: pagamento.events)
  - Consome: PedidoRetirado (exchange: pedido.events)
- [x] RabbitMQ Publisher:
  - Publica: PedidoPronto (exchange: cozinha.events)
- [x] Correção de binding RabbitMQ (exchange pagamento.events)
- [x] @EnableRabbit configurado corretamente
- [x] Logging detalhado nos publishers e consumers

#### API REST
- [x] GET /cozinha/fila - Lista pedidos na fila ordenados por data
- [x] POST /cozinha/{id}/iniciar - Inicia preparo
- [x] POST /cozinha/{id}/pronto - Marca como pronto e publica evento

#### Testes e Qualidade
- [x] Testes unitários de domínio
- [x] Testes unitários de use cases
- [x] Testes de integração JDBC
- [x] Testes de controller
- [x] Cobertura: 83% (meta: 80%+)

#### Deploy e Infraestrutura
- [x] Dockerfile multi-stage (Maven build + JRE runtime)
- [x] Manifests Kubernetes:
  - ConfigMap (cozinha-configmap.yaml)
  - Deployment (cozinha-deployment.yaml) - 2 réplicas
  - Service ClusterIP (cozinha-service.yaml)
  - NodePort local (cozinha-nodeport.yaml) - Porta 30082
  - HPA (cozinha-hpa.yaml) - 2-5 réplicas
  - StatefulSet MySQL (cozinha-mysql-statefulset.yaml)
- [x] Deploy no Minikube validado
- [x] 2 pods funcionando com balanceamento de carga

#### Validações E2E
- [x] Script test-e2e.sh atualizado com fluxo completo:
  1. Criar pedido → Status: CRIADO
  2. Pagamento aprovado → Status: REALIZADO
  3. Pedido na fila da cozinha → Status: AGUARDANDO
  4. Iniciar preparo → Status: EM_PREPARO
  5. Marcar pronto → Status: PRONTO (evento publicado)
  6. Verificar status no serviço Pedidos → Status: PRONTO
  7. Retirar pedido → Status: FINALIZADO
  8. Pedido removido da fila da cozinha
- [x] Teste completo executado com sucesso
- [x] Validação de endpoints via curl
- [x] Validação de eventos RabbitMQ (exchanges e bindings)

#### Documentação
- [x] Swagger/OpenAPI configurado
- [x] README.md atualizado com arquitetura completa
- [x] Diagramas de fluxo de eventos atualizados

### Sprint 3 - Deploy AWS EKS - CONCLUÍDO (2025-10-27)
**Status:** ✅ 100% Concluído | **Ambiente:** AWS EKS

#### Infraestrutura AWS
- [x] Cluster EKS provisionado via Terraform (`lanchonete-cluster`)
- [x] Node Group com 2 nós t3.medium
- [x] VPC e Security Groups configurados
- [x] RDS MySQL - 3 instâncias provisionadas:
  - `lanchonete-clientes-db` (db.t3.micro)
  - `lanchonete-pedidos-db` (db.t3.micro)
  - `lanchonete-cozinha-db` (db.t3.micro)
- [x] MongoDB em pod (StatefulSet com emptyDir)
- [x] RabbitMQ em pod (StatefulSet com emptyDir)
- [x] ECR Repositories para 4 microserviços

#### Deploy de Microserviços
- [x] Imagens Docker buildadas e enviadas para ECR
- [x] Secrets criados dinamicamente via script
- [x] ConfigMaps adaptados para AWS (RDS endpoints)
- [x] 4 Deployments rodando (1 réplica cada):
  - Clientes (conectado a RDS)
  - Pedidos (conectado a RDS + RabbitMQ + Feign Client)
  - Cozinha (conectado a RDS + RabbitMQ + Feign Client)
  - Pagamento (conectado a MongoDB + RabbitMQ)

#### Exposição de Serviços
- [x] Services do tipo LoadBalancer (4 Network Load Balancers)
- [x] Endereços externos atribuídos:
  - Clientes: `a37aa84c089bc49d2b87acdf2903d0d1-1704088327.us-east-1.elb.amazonaws.com:8080`
  - Pedidos: `aef3cad454f5e4abbbf216999106ff76-1621161648.us-east-1.elb.amazonaws.com:8080`
  - Cozinha: `a16129d45d0b048328a9e11708b8d623-803602099.us-east-1.elb.amazonaws.com:8080`
  - Pagamento: `a0fdf5206e1004bf9874811d6d4952d6-1938851321.us-east-1.elb.amazonaws.com:8080`

#### Testes E2E AWS
- [x] Script `test_scripts/aws/test-e2e.sh` criado
- [x] URLs obtidas dinamicamente via kubectl
- [x] TESTE 1: Pedido Anônimo - ✅ PASSOU
  - Pedido criado → Pagamento aprovado → Fila cozinha → Preparo → Pronto → Finalizado
- [x] TESTE 2: Pedido com CPF - ✅ PASSOU
  - Integração Feign Client validada (nome recuperado)
  - Fluxo completo até finalização
- [x] TESTE 3: Edge Cases - ✅ PASSOU
  - Produto inexistente (HTTP 404)
  - Pedido inexistente (HTTP 404)
  - Retirada inválida (HTTP 400)
  - Pagamento rejeitado validado (pedido ID 3 cancelado)

#### Validações
- [x] Health checks de todos os serviços: UP
- [x] Conectividade RDS → Microserviços: ✅
- [x] Integração RabbitMQ: ✅ (eventos propagados corretamente)
- [x] Integração Feign Client (Pedidos → Clientes): ✅
- [x] Pagamento aleatório funcionando (80% aprovação, 20% rejeição): ✅

#### Decisões Arquiteturais (AWS Academy)
- [x] RDS MySQL ao invés de MySQL em pods (serviços de produção)
- [x] MongoDB/RabbitMQ em pods com emptyDir (aceita perda de dados)
- [x] LoadBalancer services ao invés de ALB+Ingress (simplicidade)
- [x] Sem OIDC provider (limitação AWS Academy)
- [x] Uso do LabRole para todas as operações

#### Scripts de Deploy
- [x] `deploy_scripts/aws/create-secrets.sh` - Cria secrets dinamicamente do Terraform
- [x] `deploy_scripts/aws/deploy-k8s.sh` - Deploy completo no EKS
- [x] `test_scripts/aws/test-e2e.sh` - Testes E2E completos

---

## 🚀 EM ANDAMENTO

Nenhuma tarefa em andamento no momento.

---

## ✅ TAREFAS CONCLUÍDAS (Fase Atual)

### 1. Expandir Cobertura de Testes E2E
**Dependências:** ✅ Todos os 4 microserviços implementados
**Status:** ✅ 100% Concluído (2025-10-30)

**Implementado LOCAL (100%):**
- [x] Infraestrutura do script `test_scripts/local/test-e2e.sh`
- [x] Teste 1: Fluxo completo com cliente anônimo
  - [x] Criar pedido sem CPF
  - [x] Validar pagamento aprovado (evento RabbitMQ)
  - [x] Validar pedido adicionado na fila da cozinha (evento RabbitMQ)
  - [x] Iniciar preparo (AGUARDANDO → EM_PREPARO)
  - [x] Marcar como pronto (EM_PREPARO → PRONTO + evento RabbitMQ)
  - [x] Validar propagação do evento PedidoPronto
  - [x] Retirar pedido (PRONTO → FINALIZADO + evento RabbitMQ)
  - [x] Validar remoção da fila da cozinha
- [x] Teste 2: Fluxo completo com cliente identificado
  - [x] Criar pedido com CPF válido (55555555555)
  - [x] Validar integração REST (Feign Client)
  - [x] Validar nome do cliente recuperado: "João da Silva"
  - [x] Fluxo completo até finalização
- [x] Teste 3: Validação de erros e edge cases
  - [x] Pedido com produto inexistente (404)
  - [x] Iniciar preparo de pedido inexistente (404)
  - [x] Retirar pedido com status inválido (400)
  - [x] Buscar pedido inexistente (404)
- [x] Validação de pagamento rejeitado (aleatório 20%)
- [x] Integração RabbitMQ validada (todos os exchanges e bindings)
- [x] Validação de transições de estado completa

**Implementado AWS (100%):**
- [x] Script `test_scripts/aws/test-e2e.sh` - Cliente anônimo
  - Testa fluxo completo com cliente **anônimo**
  - Aguarda processamento de pagamento (assíncrono via RabbitMQ)
  - Valida fluxo: CRIADO → REALIZADO/CANCELADO → Fila → EM_PREPARO → PRONTO
  - Trata cenário de pagamento rejeitado (20% dos casos)
  - Output limpo (1 linha por etapa)
  - Pode rodar múltiplas vezes sem falhar
  - ✅ TESTE PASSOU (última execução: 2025-10-29)

- [x] Script `test_scripts/aws/test-e2e-cliente-existente.sh` - Cliente existente
  - Criado em: 2025-10-30 13:18
  - Autentica com CPF existente (55555555555 - João da Silva)
  - Obtém token JWT com `tipo: "IDENTIFICADO"` e `clienteId`
  - Cria pedido com `cpfCliente: "55555555555"`
  - Valida que `clienteNome: "João da Silva"` aparece na resposta
  - Segue fluxo completo até status PRONTO
  - Output limpo (mesmo padrão do test-e2e.sh)
  - ✅ TESTE PASSOU (última execução: 2025-10-30)

- [x] Script `test_scripts/aws/test-e2e-cliente-novo.sh` - Criar cliente novo
  - Criado em: 2025-10-30 13:20
  - Gera CPF único (timestamp-based, 11 dígitos)
  - Cria novo cliente via `POST /clientes` (com token anônimo)
  - Valida criação (HTTP 201)
  - Autentica com o CPF do cliente recém-criado
  - Obtém token JWT com contexto do novo cliente (tipo: IDENTIFICADO)
  - Cria pedido usando o novo cliente
  - Valida nome do cliente no pedido
  - Segue fluxo completo até status PRONTO
  - Output limpo (mesmo padrão do test-e2e.sh)
  - ✅ TESTE PASSOU (última execução: 2025-10-30)

- [x] URLs obtidas dinamicamente via Terraform
- [x] Integração com RDS MySQL validada
- [x] Integração RabbitMQ em ambiente AWS validada
- [x] Validação de autenticação com CPF existente
- [x] Validação de criação de novo cliente
- [x] Validação de integração Feign Client (nome do cliente recuperado)

**Critérios de Aceite (TODOS ATENDIDOS ✅):**
- ✅ Fluxo básico funcionando (anônimo) - test-e2e.sh
- ✅ Fluxo com cliente existente - test-e2e-cliente-existente.sh
- ✅ Fluxo com cliente novo - test-e2e-cliente-novo.sh
- ✅ Validação de pagamento rejeitado (implementado no test-e2e.sh)
- ✅ Validação de todas as integrações (REST + RabbitMQ)
- ✅ Output limpo e fácil de acompanhar
- ✅ Scripts podem rodar múltiplas vezes sem falhar

**Estatísticas de Testes E2E AWS:**
- Total de scripts: 5 (test-e2e.sh, test-e2e-cliente-existente.sh, test-e2e-cliente-novo.sh, test-auth.sh, test-validate-deployment.sh)
- Scripts E2E completos: 3
- Taxa de sucesso: 100% (3/3 passando)
- Cobertura de cenários: Cliente anônimo, cliente existente, cliente novo
- Cobertura de integrações: REST (Feign Client) + RabbitMQ (eventos assíncronos)

## 📋 PRÓXIMAS TAREFAS (OBRIGATÓRIAS)

### 2. Configurar CI/CD Completo no GitHub Actions + SonarQube
**Estimativa:** 8-10 dias (2 dias por serviço × 4 serviços)
**Dependências:** ✅ Microserviços implementados + Testes E2E prontos
**Ambiente:** ☁️ AWS (EKS) + GitHub Actions + SonarCloud
**Estratégia:** **Monorepo com pipelines separados por serviço**
**Status:** ⏳ Em Andamento

**Arquitetura de Pipelines:**
- 1 pipeline CI por serviço (testes + SonarCloud)
- 1 pipeline CD por serviço (build + deploy EKS)
- 4 projetos separados no SonarCloud
- Triggers baseados em path filters (`services/{servico}/**`)

---

### 2.1 🔵 FASE 1: Serviço de Clientes (PRIORIDADE MÁXIMA)
**Status:** 🚀 Em Andamento | **Estimativa:** 2 dias

#### 2.1.1 CD - Clientes (`cd-clientes.yml`)
**Trigger:** Push em `main` com mudanças em `services/clientes/**`

- [ ] Criar arquivo `.github/workflows/cd-clientes.yml`
- [ ] Configurar trigger com path filter: `services/clientes/**`
- [ ] Setup Java 17 + Maven cache
- [ ] Build da imagem Docker (services/clientes/Dockerfile)
- [ ] Login no AWS ECR
- [ ] Tag da imagem: `${GITHUB_SHA}` + `latest`
- [ ] Push para ECR: `lanchonete-clientes`
- [ ] Configurar kubectl com EKS (`aws eks update-kubeconfig`)
- [ ] Aplicar secrets K8s (RDS credentials)
- [ ] Deploy manifests K8s:
  - ConfigMap: `k8s_manifests/aws/clientes-configmap.yaml`
  - Deployment: `k8s_manifests/aws/clientes-deployment.yaml`
  - Service: `k8s_manifests/aws/clientes-service.yaml`
- [ ] Aguardar rollout: `kubectl rollout status deployment/clientes`
- [ ] **Smoke Tests:**
  - Health check: `GET /actuator/health` → Status `UP`
  - Criar cliente: `POST /clientes` (HTTP 201)
  - Buscar cliente: `GET /clientes/{cpf}` (HTTP 200)
- [ ] Notificar sucesso/falha
- [ ] Configurar rollback automático em caso de falha

#### 2.1.2 CI - Clientes (`ci-clientes.yml`)
**Trigger:** Pull Request com mudanças em `services/clientes/**`

- [ ] Criar arquivo `.github/workflows/ci-clientes.yml`
- [ ] Configurar trigger com path filter: `services/clientes/**`
- [ ] Setup Java 17 + Maven cache
- [ ] Executar testes: `mvn clean test -f services/clientes/pom.xml`
- [ ] Gerar relatório JaCoCo
- [ ] **Setup SonarCloud:**
  - [ ] Criar projeto no SonarCloud: `lanchonete-clientes`
  - [ ] Obter token de autenticação
  - [ ] Configurar secret GitHub: `SONAR_TOKEN`
  - [ ] Adicionar plugin sonar-maven no `pom.xml`
  - [ ] Configurar propriedades Sonar:
    - `sonar.projectKey=lanchonete-clientes`
    - `sonar.organization=<sua-org>`
    - `sonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml`
- [ ] Executar análise: `mvn sonar:sonar -Dsonar.token=${{ secrets.SONAR_TOKEN }}`
- [ ] **Quality Gates:**
  - Cobertura > 80% (atual: 85% ✅)
  - Bugs = 0
  - Vulnerabilities = 0
  - Code Smells: Rating A/B
  - Duplicações < 3%
- [ ] Publicar comentário no PR com link SonarCloud
- [ ] Falhar build se Quality Gate falhar

**Critérios de Aceite:**
- ✅ CD executa automaticamente em push para `main` com mudanças em `services/clientes/`
- ✅ CI executa automaticamente em PRs com mudanças em `services/clientes/`
- ✅ Imagem Docker publicada no ECR
- ✅ Deploy no EKS bem-sucedido
- ✅ Smoke tests passando
- ✅ SonarCloud analisando código
- ✅ Quality Gate configurado e passando

---

### 2.2 🟢 FASE 2: Serviço de Pedidos
**Status:** ⏳ Pendente | **Estimativa:** 2 dias
**Dependências:** ✅ Fase 1 (Clientes) concluída

#### 2.2.1 CD - Pedidos (`cd-pedidos.yml`)
- [ ] Criar arquivo `.github/workflows/cd-pedidos.yml`
- [ ] Path filter: `services/pedidos/**`
- [ ] Build + Push para ECR: `lanchonete-pedidos`
- [ ] Deploy K8s manifests (ConfigMap, Deployment, Service)
- [ ] Smoke tests:
  - Health check
  - Criar pedido anônimo
  - Buscar pedido por ID
  - Validar integração com Clientes (Feign Client)

#### 2.2.2 CI - Pedidos (`ci-pedidos.yml`)
- [ ] Criar arquivo `.github/workflows/ci-pedidos.yml`
- [ ] Path filter: `services/pedidos/**`
- [ ] Testes: `mvn clean test -f services/pedidos/pom.xml`
- [ ] Projeto SonarCloud: `lanchonete-pedidos`
- [ ] Quality Gates (cobertura atual: 82% ✅)

---

### 2.3 🟡 FASE 3: Serviço de Cozinha
**Status:** ⏳ Pendente | **Estimativa:** 2 dias
**Dependências:** ✅ Fase 2 (Pedidos) concluída

#### 2.3.1 CD - Cozinha (`cd-cozinha.yml`)
- [ ] Criar arquivo `.github/workflows/cd-cozinha.yml`
- [ ] Path filter: `services/cozinha/**`
- [ ] Build + Push para ECR: `lanchonete-cozinha`
- [ ] Deploy K8s manifests
- [ ] Smoke tests:
  - Health check
  - Listar fila de pedidos
  - Iniciar preparo
  - Marcar como pronto

#### 2.3.2 CI - Cozinha (`ci-cozinha.yml`)
- [ ] Criar arquivo `.github/workflows/ci-cozinha.yml`
- [ ] Path filter: `services/cozinha/**`
- [ ] Testes: `mvn clean test -f services/cozinha/pom.xml`
- [ ] Projeto SonarCloud: `lanchonete-cozinha`
- [ ] Quality Gates (cobertura atual: 83% ✅)

---

### 2.4 🟣 FASE 4: Serviço de Pagamento
**Status:** ⏳ Pendente | **Estimativa:** 2 dias
**Dependências:** ✅ Fase 3 (Cozinha) concluída

#### 2.4.1 CD - Pagamento (`cd-pagamento.yml`)
- [ ] Criar arquivo `.github/workflows/cd-pagamento.yml`
- [ ] Path filter: `services/pagamento/**`
- [ ] Build + Push para ECR: `lanchonete-pagamento`
- [ ] Deploy K8s manifests
- [ ] Smoke tests:
  - Health check
  - Processar pagamento (evento RabbitMQ)
  - Validar aprovação/rejeição aleatória

#### 2.4.2 CI - Pagamento (`ci-pagamento.yml`)
- [ ] Criar arquivo `.github/workflows/ci-pagamento.yml`
- [ ] Path filter: `services/pagamento/**`
- [ ] Testes: `mvn clean test -f services/pagamento/pom.xml`
- [ ] Projeto SonarCloud: `lanchonete-pagamento`
- [ ] Quality Gates (cobertura atual: 80% ✅)

---

### 2.5 🔧 Configurações Globais
**Status:** ⏳ Pendente

- [ ] Configurar secrets do GitHub:
  - `AWS_ACCESS_KEY_ID`
  - `AWS_SECRET_ACCESS_KEY`
  - `AWS_SESSION_TOKEN` (AWS Academy)
  - `SONAR_TOKEN`
- [ ] Configurar proteção de branch (`main`):
  - Requer aprovação de PR
  - Requer CI passando
  - Não permitir force push
- [ ] Adicionar badges no README:
  - Status CD (4 badges - um por serviço)
  - Status CI (4 badges - um por serviço)
  - SonarCloud Quality Gate (4 badges)
  - SonarCloud Coverage (4 badges)
- [ ] Deletar workflows antigos:
  - `.github/workflows/ci-app.yml` (monolito)
  - `.github/workflows/cd-app.yml` (monolito)

---

### 2.6 📚 Documentação
- [ ] Documentar estratégia de pipelines no README
- [ ] Criar tabela de workflows:
  ```
  | Serviço    | CI Workflow      | CD Workflow      | SonarCloud Project      |
  |------------|------------------|------------------|-------------------------|
  | Clientes   | ci-clientes.yml  | cd-clientes.yml  | lanchonete-clientes     |
  | Pedidos    | ci-pedidos.yml   | cd-pedidos.yml   | lanchonete-pedidos      |
  | Cozinha    | ci-cozinha.yml   | cd-cozinha.yml   | lanchonete-cozinha      |
  | Pagamento  | ci-pagamento.yml | cd-pagamento.yml | lanchonete-pagamento    |
  ```
- [ ] Criar runbook de troubleshooting de pipelines
- [ ] Documentar processo de rollback manual
- [ ] Documentar métricas do SonarQube

---

**Estrutura Final de Arquivos:**
```
.github/workflows/
├── cd-clientes.yml    ✅ Deploy Clientes → EKS
├── ci-clientes.yml    ✅ Testes Clientes + SonarCloud
├── cd-pedidos.yml     ⏳ Deploy Pedidos → EKS
├── ci-pedidos.yml     ⏳ Testes Pedidos + SonarCloud
├── cd-cozinha.yml     ⏳ Deploy Cozinha → EKS
├── ci-cozinha.yml     ⏳ Testes Cozinha + SonarCloud
├── cd-pagamento.yml   ⏳ Deploy Pagamento → EKS
├── ci-pagamento.yml   ⏳ Testes Pagamento + SonarCloud
├── cd-app.yml         ❌ DELETAR (monolito antigo)
└── ci-app.yml         ❌ DELETAR (monolito antigo)
```

**Critérios de Aceite Globais:**
- ✅ 8 pipelines funcionando (4 CI + 4 CD)
- ✅ Cada serviço tem deploy independente
- ✅ Mudanças em um serviço não triggam pipelines de outros
- ✅ SonarCloud com 4 projetos separados
- ✅ Quality Gates configurados e funcionando
- ✅ Smoke tests passando em todos os serviços
- ✅ Badges visíveis no README
- ✅ Documentação completa

---

### 3. Implementar Testes BDD com Cucumber
**Estimativa:** 2-3 dias
**Dependências:** ✅ Microserviços implementados
**Ambiente:** 💻 Local + ☁️ AWS
**Status:** ⏳ Pendente (OBRIGATÓRIO)

#### 3.1 Setup Cucumber
- [ ] Adicionar dependências Cucumber ao pom.xml de cada microserviço:
  - cucumber-java
  - cucumber-junit-platform-engine
  - cucumber-spring
- [ ] Configurar Cucumber properties (cucumber.properties)
- [ ] Criar estrutura de diretórios `src/test/resources/features/`
- [ ] Configurar runner JUnit 5 + Cucumber

#### 3.2 Features e Cenários BDD
- [ ] **Clientes:**
  - Feature: Identificação de cliente por CPF
  - Feature: Cadastro de novo cliente
  - Scenarios: CPF válido, CPF inválido, cliente já cadastrado
- [ ] **Pedidos:**
  - Feature: Criar pedido anônimo
  - Feature: Criar pedido com CPF
  - Feature: Consultar pedido por ID
  - Feature: Retirar pedido
  - Scenarios: Pedido válido, produto inexistente, retirada inválida
- [ ] **Cozinha:**
  - Feature: Visualizar fila de pedidos
  - Feature: Iniciar preparo
  - Feature: Marcar como pronto
  - Scenarios: Fluxo normal, pedido inexistente, transições inválidas
- [ ] **Pagamento:**
  - Feature: Processar pagamento via evento
  - Scenarios: Pagamento aprovado, pagamento rejeitado

#### 3.3 Step Definitions
- [ ] Implementar steps para cada microserviço
- [ ] Configurar Spring Context em steps
- [ ] Criar classes helper para chamadas REST
- [ ] Implementar assertions customizadas

#### 3.4 Integração com CI/CD
- [ ] Executar testes BDD no pipeline CI
- [ ] Gerar relatórios Cucumber (JSON/HTML)
- [ ] Publicar relatórios como artefatos
- [ ] Falhar build se BDD falhar

**Critérios de Aceite:**
- Cobertura BDD de cenários principais (happy path + edge cases)
- Testes BDD executam automaticamente no CI
- Relatórios legíveis gerados (Cucumber HTML)
- Linguagem Gherkin clara e compreensível por não-técnicos

---

### 4. Remover Aplicação Monolítica (Autoatendimento)
**Estimativa:** 1 dia
**Dependências:** ✅ Todos os testes E2E completos
**Ambiente:** 💻 Local / Git
**Status:** ⏳ Pendente (OBRIGATÓRIO - será a última tarefa)

**Checklist:**
- [ ] Remover código legado:
  - [ ] Deletar `app/autoatendimento/`
  - [ ] Deletar `app/pagamento/`
  - [ ] Remover manifests K8s antigos (`k8s/autoatendimento/` se existir)
- [ ] Otimizar alocação de NodePorts:
  - [ ] Documentar portas em uso (30081-30084)
  - [ ] Remover NodePort 30080 (liberar porta)
  - [ ] Atualizar tabela de portas no README.md
- [ ] Atualizar CI/CD (GitHub Actions):
  - [ ] Revisar `.github/workflows/ci-app.yml`
  - [ ] Revisar `.github/workflows/cd-app.yml`
  - [ ] Remover jobs do autoatendimento
  - [ ] Adicionar jobs dos 4 microserviços
  - [ ] Testar pipeline em branch separada
- [ ] Atualizar documentação:
  - [ ] Remover referências ao monolito no README.md
  - [ ] Atualizar diagramas de arquitetura
  - [ ] Atualizar seção de deployment
  - [ ] Revisar TROUBLESHOOTING.md
- [ ] Limpeza final:
  - [ ] Remover dependências não utilizadas nos pom.xml
  - [ ] Verificar scripts em `scripts/` e `deploy_scripts/`
  - [ ] Atualizar .gitignore se necessário

**Critérios de Aceite:**
- Diretório `app/` completamente removido
- Todos os testes E2E passando sem o monolito
- Workflows GitHub Actions atualizados e validados
- README.md reflete apenas arquitetura de microserviços
- Mapa de portas documentado e otimizado
- Sem referências ao código legado no repositório

---

## 📊 MÉTRICAS DE SUCESSO

### Cobertura de Testes
- **Meta:** 80%+ em cada microserviço
- **Atual:**
  - Clientes: ✅ 85% (atingiu meta)
  - Pedidos: ✅ 82% (atingiu meta)
  - Pagamento: ✅ 80% (atingiu meta)
  - Cozinha: ✅ 83% (atingiu meta)
  - Auth/Cognito: ⏳ Não implementado

### Performance
- **Latência P95:** < 500ms
- **Disponibilidade:** > 99.5%
- **Taxa de erro:** < 1%
- **Auth latency:** < 200ms (token validation)

### Segurança
- **Endpoints protegidos:** 100%
- **Tokens expirados rejeitados:** 100%
- **Vulnerabilidades críticas:** 0

### Qualidade de Código
- **Linter:** 0 warnings críticos
- **Vulnerabilidades:** 0 críticas/altas
- **Code Smells:** < 10 por serviço

---

## 🏷️ TAGS

- 🔴 **ALTA** - Bloqueante ou crítico para o projeto
- 🟡 **MÉDIA** - Importante mas não bloqueante
- 🟢 **BAIXA** - Nice to have, pode ser adiado

**Ambientes:**
- 💻 **Local** - Minikube, não precisa de AWS
- ☁️ **AWS** - Requer recursos AWS (EKS, Cognito, ALB, etc)

---

## 📝 OBSERVAÇÕES

### Ordem de Implementação e Progresso

**FASE 1 - Local (Sem AWS) - ✅ 100% CONCLUÍDO**
1. ✅ **CONCLUÍDO:** Infraestrutura K8s (MySQL, MongoDB, RabbitMQ) - commit d90b4a9
2. ✅ **CONCLUÍDO:** Microserviço de Clientes - commit 148c9b2
3. ✅ **CONCLUÍDO:** Microserviço de Pagamento - commit c67362f
4. ✅ **CONCLUÍDO:** Microserviço de Pedidos - commit 66f7e45
5. ✅ **CONCLUÍDO:** Microserviço de Cozinha - commit 0582da6
6. ✅ **CONCLUÍDO:** Integração REST (Pedidos → Clientes) - validado
7. ✅ **CONCLUÍDO:** Integração RabbitMQ (completa) - validado
8. ✅ **CONCLUÍDO:** Testes E2E Local (100% - todos os cenários passando)
9. ✅ **CONCLUÍDO:** Scripts de deploy local automatizados

**FASE 2 - AWS - ✅ 100% CONCLUÍDO (2025-10-27)**
10. ✅ **CONCLUÍDO:** Infraestrutura EKS + RDS via Terraform
11. ✅ **CONCLUÍDO:** Deploy microserviços na AWS (LoadBalancers)
12. ✅ **CONCLUÍDO:** Testes E2E AWS (100% - todos os cenários passando)
13. ✅ **CONCLUÍDO:** Scripts de deploy AWS automatizados
14. ✅ **CONCLUÍDO:** Documentação completa AWS

**FASE 3 - Qualidade e CI/CD (Em Andamento) - 0% Concluído**
15. 🔲 **EM ANDAMENTO:** CI/CD completo GitHub Actions (pipelines separados por serviço)
   - Fase 1: Clientes (CD + CI + SonarCloud) - 🚀 Iniciando
   - Fase 2: Pedidos (CD + CI + SonarCloud) - ⏳ Pendente
   - Fase 3: Cozinha (CD + CI + SonarCloud) - ⏳ Pendente
   - Fase 4: Pagamento (CD + CI + SonarCloud) - ⏳ Pendente
16. 🔲 **PENDENTE:** Testes BDD com Cucumber (features + scenarios)
17. 🔲 **PENDENTE:** Remover aplicação monolítica (limpeza)

**FASE 4 - Melhorias Avançadas (Baixa Prioridade)**
19-22. 🔲 **BACKLOG:** Cognito, Segurança, Performance, Resiliência, Docs

**Progresso Geral do Projeto:**
- Microserviços: 4/4 ✅ (100%)
- Integrações: 2/2 ✅ (100%)
- Testes E2E Local: 3/3 ✅ (100%)
- Testes E2E AWS: 3/3 ✅ (100%)
  - Cliente anônimo ✅
  - Cliente existente ✅ (NOVO - 2025-10-30)
  - Cliente novo ✅ (NOVO - 2025-10-30)
- Deploy Local (Minikube): 1/1 ✅ (100%)
- Deploy AWS (EKS): 1/1 ✅ (100%)
- RDS Databases: 3/3 ✅ (100%)
- **FASE 1 (Core + AWS + Testes): 3/3 tarefas (100%) ✅**
- **FASE 2 (Qualidade + CI/CD): 0/3 tarefas (0%) ⏳**
  - CI/CD GitHub Actions + SonarQube: ⏳ Pendente (OBRIGATÓRIO)
  - Testes BDD Cucumber: ⏳ Pendente (OBRIGATÓRIO)
  - Remover Monolito: ⏳ Pendente (OBRIGATÓRIO)
- **TOTAL GERAL: 3/6 tarefas principais (50%) ⏳**

### Regras Gerais

1. **Git Workflow:** Cada tarefa deve ter seu próprio commit descritivo

2. **Testes:** NUNCA pular testes - 80% cobertura é obrigatório

3. **Documentação:** Atualizar README.md após cada tarefa concluída

4. **Code Review:** Todas as mudanças devem passar por revisão antes do merge

5. **Decisões Arquiteturais:** Documentar em ADR (Architecture Decision Records)

6. **Minikube First:** Sempre testar em Minikube antes de AWS

---

## 🔗 LINKS ÚTEIS

- [README Principal](./README.md)
- [Troubleshooting](./TROUBLESHOOTING.md)
- [GitHub Actions](./.github/workflows/)
- [Manifests K8s](./k8s/)
- [AWS Cognito Docs](https://docs.aws.amazon.com/cognito/)
- [Spring Security OAuth2](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)
- [Kubernetes Ingress](https://kubernetes.io/docs/concepts/services-networking/ingress/)

---

**Última revisão:** 2025-11-13 15:00
**Responsável:** Anderson
**Status Geral:** 🟡 50% Concluído - Testes E2E completos, CI/CD em andamento
**Sprint Atual:** Sprint 4 - Fase 1: CI/CD de Clientes (CD + CI + SonarCloud)
**Próxima Milestone:** Completar CI/CD de todos os 4 serviços + BDD + Cleanup

---

## 📈 RESUMO EXECUTIVO

### Conquistas desta Sessão (2025-10-30) - EXPANSÃO TESTES E2E ✅

#### ✅ Novos Scripts de Teste E2E Criados e Validados
- **test-e2e-cliente-existente.sh** (criado 13:18):
  - Autenticação com CPF existente (55555555555 - João da Silva)
  - Validação de token JWT com tipo IDENTIFICADO
  - Criação de pedido com cliente identificado
  - Validação de integração Feign Client (nome recuperado corretamente)
  - Fluxo completo até status PRONTO
  - ✅ 100% PASSOU

- **test-e2e-cliente-novo.sh** (criado 13:20):
  - Geração de CPF único (timestamp-based)
  - Criação de novo cliente via API
  - Autenticação com cliente recém-criado
  - Validação de token JWT do novo cliente
  - Criação de pedido com novo cliente
  - Validação de nome no pedido
  - Fluxo completo até status PRONTO
  - ✅ 100% PASSOU

#### ✅ Cobertura de Testes E2E Completa
- **3 cenários cobertos:**
  1. Cliente anônimo (test-e2e.sh) - implementado anteriormente
  2. Cliente existente (test-e2e-cliente-existente.sh) - NOVO
  3. Cliente novo (test-e2e-cliente-novo.sh) - NOVO

- **Validações implementadas:**
  - Autenticação com Cognito (anônimo e identificado)
  - Criação de clientes via API
  - Integração REST (Feign Client): Pedidos → Clientes
  - Integração RabbitMQ: Pedidos ↔ Pagamento ↔ Cozinha
  - Fluxos completos: Pedido → Pagamento → Cozinha → Pronto
  - Tratamento de pagamento rejeitado (20%)
  - Output limpo e legível

#### 📊 Estatísticas da Sessão
- **Scripts criados:** 2 novos scripts E2E
- **Taxa de sucesso:** 100% (2/2 passando na primeira execução)
- **Linhas de código:** ~29KB de scripts bash (13KB + 16KB)
- **Cobertura de cenários:** Expandida de 1 para 3 cenários
- **Tempo de execução:** ~2-3 minutos por script
- **Integrações validadas:** Cognito + RDS + RabbitMQ + Feign Client

#### 🎯 Objetivos Atingidos
- ✅ Expandir cobertura de testes E2E (100%)
- ✅ Validar autenticação com cliente existente
- ✅ Validar criação de novo cliente
- ✅ Validar integração Feign Client em ambiente AWS
- ✅ Manter output limpo e legível
- ✅ Scripts reutilizáveis e robustos

---

### Conquistas Sessão Anterior (2025-10-27) - DEPLOY AWS COMPLETO ✅

#### ✅ Infraestrutura AWS Provisionada e Operacional
- **Cluster EKS:** lanchonete-cluster (2 nós t3.medium)
- **RDS MySQL:** 3 instâncias db.t3.micro (clientes, pedidos, cozinha)
- **MongoDB:** Pod com emptyDir (perda aceitável)
- **RabbitMQ:** Pod com emptyDir (perda aceitável)
- **ECR:** 4 repositórios com imagens Docker
- **LoadBalancers:** 4 Network Load Balancers provisionados

#### ✅ Deploy de Microserviços na AWS
- **4 Deployments** rodando com 1 réplica cada
- **Conectividade RDS** validada em todos os serviços
- **Integração RabbitMQ** funcionando (eventos propagados)
- **Integração Feign Client** funcionando (Pedidos → Clientes)
- **Health checks** todos passando (status: UP)

#### ✅ Testes E2E AWS - 100% Passando
- **Script criado:** `test_scripts/aws/test-e2e.sh`
- **URLs dinâmicas:** Obtidas via kubectl automaticamente
- **TESTE 1:** Pedido Anônimo - Fluxo completo ✅
- **TESTE 2:** Pedido com CPF - Feign Client validado ✅
- **TESTE 3:** Edge Cases - Todos erros tratados ✅
- **Pagamento Rejeitado:** Validado (pedido ID 3 cancelado)

#### ✅ Decisões Técnicas Implementadas
- Simplificação: LoadBalancer ao invés de ALB+Ingress
- RDS para bancos de produção (Clientes, Pedidos, Cozinha)
- Pods para serviços de suporte (MongoDB, RabbitMQ)
- Scripts de deploy automatizados
- Secrets criados dinamicamente do Terraform

#### 📊 Estatísticas AWS
- **Custo estimado:** ~$30-40/mês (RDS + EKS + LoadBalancers)
- **Tempo de deploy:** ~20 minutos
- **Pods rodando:** 6 (4 microserviços + MongoDB + RabbitMQ)
- **Endpoints públicos:** 4 URLs LoadBalancer
- **Tempo de resposta:** <500ms (média)
- **Taxa de sucesso testes:** 100%

---

### Conquistas Sessão Anterior (2025-10-23)

#### ✅ Microserviço de Cozinha - 100% IMPLEMENTADO
- **Commit:** 0582da6 - "implementação do serviço de cozinha"
- **Arquivos:** 35 classes Java com Clean Architecture
- **Cobertura de Testes:** 83% (superou meta de 80%)
- **Deploy:** 2 réplicas funcionando no Minikube
- **Endpoints:** 3 endpoints REST implementados e validados
- **Integrações:**
  - RabbitMQ Consumer: PagamentoAprovado, PedidoRetirado
  - RabbitMQ Publisher: PedidoPronto
  - Feign Client: GET /pedidos/{id}

#### ✅ Correções de Integração RabbitMQ
- Corrigido binding do exchange pagamento.events
- Adicionado @EnableRabbit para ativar consumers
- Criado exchange cozinha.events para publicação de eventos
- Implementado logging detalhado para debug

#### ✅ Script E2E Atualizado
- Script test-e2e.sh expandido de 46 para 215 linhas
- Fluxo completo validado:
  1. Criar pedido → CRIADO
  2. Pagamento automático → REALIZADO
  3. Adicionar à fila da cozinha → AGUARDANDO
  4. Iniciar preparo → EM_PREPARO
  5. Marcar como pronto → PRONTO (evento publicado)
  6. Verificar propagação → Status atualizado no serviço Pedidos
  7. Retirar pedido → FINALIZADO
  8. Remover da fila → Confirmado

#### 📊 Estado Atual do Projeto
- **4 de 4 microserviços** implementados e operacionais (100%)
- **Todas as integrações** REST e RabbitMQ funcionando (100%)
- **Infraestrutura K8s** completa (MySQL x3, MongoDB, RabbitMQ) (100%)
- **Testes E2E** básicos funcionando (70%)
- **Cobertura média de testes:** 82.5% (meta: 80%)

### Próximas Ações Recomendadas

#### Prioridade Imediata (1-2 dias)
1. **Completar Testes E2E (30% restante)**
   - Adicionar teste com cliente identificado
   - Adicionar teste de pagamento rejeitado
   - Implementar testes de edge cases
   - Gerar relatório de execução

2. **Remover Aplicação Monolítica**
   - Deletar diretórios app/autoatendimento e app/pagamento
   - Atualizar workflows GitHub Actions
   - Limpar NodePort 30080
   - Atualizar documentação

#### Próximas Fases (médio prazo)
3. **Migração para AWS (FASE 2)**
   - Implementar autenticação com Cognito
   - Configurar Ingress no EKS
   - Automatizar CI/CD completo

4. **Melhorias Opcionais (FASE 3)**
   - Observabilidade (Prometheus/Grafana)
   - Segurança avançada
   - Otimizações de performance

---

**Última atualização desta sessão:** 2025-11-13 15:00
**Commits desta sessão:** Replanejamento de CI/CD (pipelines separados por serviço)
**Arquivos criados/modificados:**
  - BACKLOG.md (ATUALIZADO - novo planejamento CI/CD)
**Responsável:** Anderson
**Status Geral:** 🟡 50% Concluído - Fase 1 e 2 completas, Fase 3 em andamento
**Próxima Milestone:** Sprint 4 - CI/CD Separado por Serviço (começando por Clientes)
**Sprint Atual:** Sprint 4 - Fase 1: Clientes (CD + CI + SonarCloud)

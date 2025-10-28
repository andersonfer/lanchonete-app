# 📋 BACKLOG - Migração para Microserviços

**Projeto:** Sistema de Lanchonete - Arquitetura de Microserviços
**Branch Atual:** `feature/migracao-microservicos`
**Última Atualização:** 2025-10-27 20:30

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
- **Testes E2E AWS:** Script completo e validado (100%)
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

### 1. Testes E2E Automatizados - CONCLUÍDO ✅
**Prioridade:** 🔴 ALTA
**Dependências:** ✅ Todos os 4 microserviços implementados
**Status:** ✅ 100% Concluído (Local + AWS)

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
- [x] Script `test_scripts/aws/test-e2e.sh` criado
- [x] URLs obtidas dinamicamente via kubectl (LoadBalancer)
- [x] Teste 1: Pedido Anônimo ✅ (todos os passos passaram)
- [x] Teste 2: Pedido com CPF ✅ (Feign Client validado)
- [x] Teste 3: Edge Cases ✅ (todos os erros tratados corretamente)
- [x] Validação de pagamento rejeitado (pedido ID 3 cancelado)
- [x] Integração com RDS MySQL validada
- [x] Integração RabbitMQ em ambiente AWS validada

**Critérios de Aceite:**
- ✅ Fluxo básico funcionando (anônimo)
- ✅ Fluxo com cliente identificado
- ✅ Validação de erros implementada
- ✅ 100% dos testes passando automaticamente (Local + AWS)
- ✅ Documentação de execução atualizada
- ✅ Validação de todas as integrações (REST + RabbitMQ)
- ✅ Cobertura de cenários de erro
- ✅ Suporte a ambos os ambientes (Local Minikube + AWS EKS)

## 📋 PRÓXIMAS TAREFAS - FASE LOCAL

### 2. Remover Aplicação Monolítica (Autoatendimento)
**Prioridade:** 🔴 ALTA
**Estimativa:** 1 dia
**Dependências:** ✅ Todos os 4 microserviços funcionando | ⏳ Testes E2E completos
**Ambiente:** 💻 Local / Git
**Status:** Bloqueada (aguardando testes E2E 100%)

**Análise Atual:**
- Monolito presente em `app/autoatendimento/` e `app/pagamento/`
- NodePort 30080 alocado para autoatendimento (conflita com pedidos)
- Workflows GitHub Actions ainda referenciam monolito
- README.md contém diagramas com arquitetura antiga

**Checklist:**
- [x] ✅ Validar que todos os 4 microserviços estão funcionando
- [ ] ⏳ Executar testes E2E completos e validar 100% sucesso
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
- [ ] Diretório `app/` completamente removido
- [ ] Todos os testes E2E passando sem o monolito
- [ ] Workflows GitHub Actions atualizados e validados
- [ ] README.md reflete apenas arquitetura de microserviços
- [ ] Mapa de portas documentado e otimizado
- [ ] Sem referências ao código legado no repositório

**Bloqueadores:**
- ⏳ Aguardando conclusão dos Testes E2E (tarefa 1)

---

## ☁️ PRÓXIMAS TAREFAS - FASE AWS (Com dependência AWS)

### 3. Implementar Autenticação com AWS Cognito
**Prioridade:** 🔴 ALTA
**Estimativa:** 3-4 dias
**Dependências:** Todos os microserviços implementados
**Ambiente:** ☁️ AWS (EKS)

**Descrição:**
Implementar autenticação e identificação de clientes utilizando AWS Cognito, substituindo o modelo atual de identificação simples por CPF.

**Checklist:**

#### 4.1 Infraestrutura Cognito (Terraform)
- [ ] Criar módulo `infra/cognito/`
- [ ] Configurar User Pool do Cognito
- [ ] Configurar App Client (com refresh token)
- [ ] Definir atributos customizados (CPF, nome, email)
- [ ] Configurar políticas de senha
- [ ] Configurar MFA (opcional)
- [ ] Configurar triggers Lambda (pre-signup, post-confirmation)
- [ ] Aplicar Terraform e validar recursos criados

#### 4.2 API Gateway ou ALB com autenticação
- [ ] Decisão arquitetural: API Gateway vs ALB Cognito Integration
- [ ] Se API Gateway:
  - Criar API Gateway REST
  - Configurar Cognito Authorizer
  - Configurar rotas para cada microserviço
- [ ] Se ALB:
  - Configurar ALB Authentication com Cognito
  - Atualizar Ingress rules

#### 4.3 Serviço de Autenticação
- [ ] Decisão: novo serviço ou adaptar Clientes
- [ ] Endpoints:
  - `POST /auth/signup` - Cadastro de novo cliente
  - `POST /auth/signin` - Login (retorna JWT)
  - `POST /auth/refresh` - Refresh token
  - `POST /auth/signout` - Logout
  - `GET /auth/me` - Dados do usuário autenticado
- [ ] Integração com AWS Cognito SDK
- [ ] Validação de CPF único no signup
- [ ] Sincronização: Cognito User → Tabela Cliente

#### 4.4 Atualização dos Microserviços
- [ ] **Clientes:**
  - Adicionar campo `cognito_user_id` na tabela
  - Criar endpoint `GET /clientes/me` (requer token)
  - Manter endpoint `POST /clientes/identificar` para retrocompatibilidade
- [ ] **Pedidos:**
  - Adicionar middleware JWT validation
  - Extrair `sub` (user_id) do token JWT
  - Buscar cliente via `cognito_user_id` ou CPF (fallback)
  - Atualizar endpoint `POST /pedidos` para aceitar token
- [ ] **Cozinha:**
  - Proteger endpoints administrativos com JWT
  - Validar roles/grupos do Cognito (ex: grupo "cozinha")
- [ ] **Pagamento:**
  - Manter interno (sem autenticação externa)

#### 4.5 Segurança e Validação
- [ ] Implementar JWT validation em todos os microserviços
  - Biblioteca: `spring-boot-starter-oauth2-resource-server`
  - Validar signature usando Cognito JWKS
  - Validar claims (iss, aud, exp)
- [ ] Configurar CORS adequadamente
- [ ] Implementar rate limiting
- [ ] Adicionar logs de auditoria (login, signup, falhas)

#### 4.6 Testes
- [ ] Testes unitários dos novos endpoints de auth
- [ ] Testes de integração com Cognito (LocalStack ou conta AWS dev)
- [ ] Testes E2E do fluxo completo:
  - Signup → Signin → Criar Pedido → Logout
- [ ] Testes de validação de token expirado
- [ ] Testes de refresh token
- [ ] Testes de tentativa de acesso sem token (401)

#### 4.7 Documentação
- [ ] Atualizar diagramas de arquitetura com Cognito
- [ ] Documentar fluxo de autenticação no README
- [ ] Criar guia de uso da API com autenticação
- [ ] Documentar formato do token JWT e claims
- [ ] Atualizar collection Postman/Insomnia com auth

**Critérios de Aceite:**
- User Pool criado e configurado no Cognito
- Clientes conseguem se cadastrar via `/auth/signup`
- Login retorna token JWT válido
- Todos os endpoints protegidos validam JWT corretamente
- Pedidos são criados usando o contexto do usuário autenticado
- Tokens expirados retornam 401
- Refresh token funciona corretamente
- Sincronização Cognito ↔ Tabela Cliente funcionando
- Testes E2E passando com autenticação

**Decisões Arquiteturais a Documentar:**
1. API Gateway ou ALB com Cognito Authentication?
2. Criar novo microserviço "Auth" ou adaptar "Clientes"?
3. Usar Cognito Hosted UI ou endpoints customizados?
4. Implementar grupos/roles no Cognito (admin, cliente, cozinha)?

---

### 4. Configurar Ingress para AWS EKS
**Prioridade:** 🔴 ALTA
**Estimativa:** 1-2 dias
**Dependências:** Cognito implementado
**Ambiente:** ☁️ AWS (EKS)

**Checklist:**
- [ ] Verificar/Instalar AWS Load Balancer Controller no EKS
- [ ] Criar manifesto `k8s/aws/ingress.yaml`
- [ ] Definir routing rules:
  - `/auth/*` → auth-service ou clientes-service
  - `/clientes/*` → clientes-service:8080
  - `/pedidos/*` → pedidos-service:8080
  - `/produtos/*` → pedidos-service:8080
  - `/cozinha/*` → cozinha-service:8082
  - (sem rota pública para pagamentos - apenas interno)
- [ ] Configurar Cognito Authentication no ALB (se não usar API Gateway)
- [ ] Configurar Health Checks para cada serviço
- [ ] Configurar HTTPS/TLS com Certificate Manager
- [ ] Aplicar Ingress no EKS
- [ ] Aguardar provisionamento do ALB
- [ ] Testar todos os endpoints via ALB
- [ ] Configurar DNS (opcional): api.lanchonete.com
- [ ] Atualizar documentação com URLs do ALB

**Critérios de Aceite:**
- ALB provisionado e respondendo
- Routing funcionando para todos os microserviços
- Autenticação Cognito funcionando via ALB
- Health checks reportando status correto
- HTTPS configurado e certificado válido
- URLs públicas acessíveis e documentadas

---

### 5. Configurar CI/CD Completo no GitHub Actions
**Prioridade:** 🟡 MÉDIA
**Estimativa:** 2-3 dias
**Dependências:** Ingress EKS configurado + Testes E2E locais prontos
**Ambiente:** ☁️ AWS (EKS) + GitHub Actions

**Checklist:**

#### 6.1 Workflow CI (Pull Requests)
- [ ] Atualizar `ci-app.yml`:
  - Remover testes do autoatendimento
  - Adicionar testes dos 4 microserviços (Clientes, Pedidos, Pagamento, Cozinha)
  - Executar testes E2E locais (com LocalStack para Cognito mock)
  - Verificar cobertura de código (80%+ mínimo)
  - Lint/SonarQube (opcional)
- [ ] Configurar cache de dependências Maven
- [ ] Configurar matriz de testes (paralelo)

#### 6.2 Workflow CD (Deploy para EKS)
- [ ] Atualizar `cd-app.yml`:
  - Build das 4 imagens Docker (Clientes, Pedidos, Pagamento, Cozinha)
  - Login no ECR
  - Tag com SHA do commit + latest
  - Push para ECR (4 repositórios)
  - Configurar kubectl com EKS
  - Aplicar secrets
  - Deploy databases (se necessário)
  - Deploy dos 4 microserviços
  - Aplicar Ingress
  - Aguardar rollout completo
- [ ] Smoke Tests:
  - Health check de cada microserviço via ALB
  - Teste de autenticação (signup/signin)
  - Teste básico de criação de pedido
- [ ] Rollback automático em caso de falha
- [ ] Notificação de sucesso/falha

#### 6.3 Segurança e Configuração
- [ ] Configurar secrets do GitHub:
  - AWS_ACCESS_KEY_ID
  - AWS_SECRET_ACCESS_KEY
  - AWS_SESSION_TOKEN (se necessário)
  - Secrets adicionais do Cognito
- [ ] Configurar proteção de branch (main):
  - Requer aprovação de PR
  - Requer CI passando
  - Não permitir force push
- [ ] Configurar CODEOWNERS (opcional)

#### 6.4 Notificações e Monitoramento
- [ ] Configurar notificações Slack/Email em caso de falha
- [ ] Adicionar badge de status do CI/CD no README
- [ ] Configurar deploy manual (workflow_dispatch) para ambientes

#### 6.5 Documentação
- [ ] Documentar processo de CI/CD no README
- [ ] Criar runbook de troubleshooting de pipeline
- [ ] Documentar processo de rollback manual

**Critérios de Aceite:**
- CI executa automaticamente em todos os PRs
- CD executa automaticamente em push para main
- Pipeline completo: Build → Test → Push ECR → Deploy EKS → Smoke Test
- Rollback automático funciona em caso de falha
- Notificações funcionando
- Badge de status visível no README
- Deploy manual disponível via workflow_dispatch

---

## 🔮 BACKLOG FUTURO (Baixa Prioridade)

### 6. Implementar Testes BDD com Cucumber
**Prioridade:** 🟡 MÉDIA
**Estimativa:** 2-3 dias
**Ambiente:** 💻 Local + ☁️ AWS

#### 6.1 Setup Cucumber
- [ ] Adicionar dependências Cucumber ao pom.xml de cada microserviço:
  - cucumber-java
  - cucumber-junit-platform-engine
  - cucumber-spring
- [ ] Configurar Cucumber properties (cucumber.properties)
- [ ] Criar estrutura de diretórios `src/test/resources/features/`
- [ ] Configurar runner JUnit 5 + Cucumber

#### 6.2 Features e Cenários BDD
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

#### 6.3 Step Definitions
- [ ] Implementar steps para cada microserviço
- [ ] Configurar Spring Context em steps
- [ ] Criar classes helper para chamadas REST
- [ ] Implementar assertions customizadas

#### 6.4 Integração com CI/CD
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

### 7. Integração SonarQube no CI/CD
**Prioridade:** 🟡 MÉDIA
**Estimativa:** 1-2 dias
**Ambiente:** 💻 Local + ☁️ AWS + GitHub Actions

#### 7.1 Setup SonarCloud/SonarQube
- [ ] Opção A: Usar SonarCloud (cloud, grátis para open source)
  - Criar conta SonarCloud
  - Conectar com repositório GitHub
  - Obter token de autenticação
- [ ] Opção B: Self-hosted SonarQube (Docker local)
  - Deploy SonarQube via Docker Compose
  - Configurar admin/senha
  - Criar projeto e token

#### 7.2 Configuração Maven
- [ ] Adicionar plugin SonarQube aos 4 microserviços:
  ```xml
  <plugin>
    <groupId>org.sonarsource.scanner.maven</groupId>
    <artifactId>sonar-maven-plugin</artifactId>
    <version>3.10.0.2594</version>
  </plugin>
  ```
- [ ] Configurar propriedades Sonar (sonar-project.properties)
- [ ] Configurar exclusões (testes, DTOs, configs)

#### 7.3 Integração CI (GitHub Actions)
- [ ] Adicionar step Sonar no workflow CI:
  ```yaml
  - name: SonarQube Analysis
    env:
      SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
    run: mvn sonar:sonar
  ```
- [ ] Configurar Quality Gate
- [ ] Falhar build se Quality Gate falhar
- [ ] Publicar link do Sonar no PR

#### 7.4 Métricas e Qualidade
- [ ] Configurar thresholds:
  - Code Coverage > 80%
  - Duplicações < 3%
  - Bugs: 0
  - Vulnerabilities: 0
  - Code Smells: Rating A ou B
- [ ] Configurar análise de branches
- [ ] Configurar análise de Pull Requests

**Critérios de Aceite:**
- SonarQube executando em todos os builds
- Quality Gate configurado e funcionando
- Badge do SonarQube no README
- Análise de PRs funcionando
- Equipe consegue visualizar métricas de código

---

### 8. Melhorias de Segurança Avançadas
**Estimativa:** 2-3 dias
**Ambiente:** ☁️ AWS

- [ ] Implementar grupos/roles no Cognito (admin, cliente, cozinha)
- [ ] Network Policies no Kubernetes
- [ ] AWS Secrets Manager para secrets sensíveis
- [ ] Scan de vulnerabilidades nas imagens Docker (Trivy/Snyk)
- [ ] WAF no ALB
- [ ] Rotação automática de secrets
- [ ] Audit logging completo

### 9. Otimizações de Performance
**Estimativa:** 2-3 dias
**Ambiente:** ☁️ AWS + Local

- [ ] Implementar cache (Redis/ElastiCache) para tokens JWT
- [ ] Otimizar queries SQL (índices, explain plan)
- [ ] Configurar Connection Pool adequadamente
- [ ] Implementar rate limiting por usuário
- [ ] Configurar HPA (Horizontal Pod Autoscaler) para todos os serviços
- [ ] Configurar PDB (Pod Disruption Budget)

### 10. Resiliência e Tolerância a Falhas
**Estimativa:** 2-3 dias
**Ambiente:** Local + AWS

- [ ] Implementar Circuit Breaker (Resilience4j)
- [ ] Configurar Retry policies com backoff exponencial
- [ ] Implementar fallback para chamadas REST
- [ ] Dead Letter Queue para RabbitMQ
- [ ] Health checks avançados (readiness vs liveness)
- [ ] Graceful shutdown
- [ ] Chaos Engineering (testes de resiliência)

### 11. Documentação e Governança
**Estimativa:** 2 dias
**Ambiente:** Local

- [x] Gerar documentação OpenAPI/Swagger para todos os microserviços - `2025-10-23`
- [x] Criar diagramas de arquitetura AWS atualizados - `2025-10-27`
- [ ] Criar diagramas C4 Model completos
- [ ] Documentar contratos de eventos (AsyncAPI)
- [ ] Guia de contribuição (CONTRIBUTING.md)
- [ ] ADRs (Architecture Decision Records)
- [ ] Documentar políticas de segurança e compliance
- [ ] Vídeo de demonstração do sistema

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

**FASE 3 - Qualidade e CI/CD (Opcional) - 0% Concluído**
15. 🔲 **PENDENTE:** Testes BDD com Cucumber (features + scenarios)
16. 🔲 **PENDENTE:** Integração SonarQube no CI/CD (quality gates)
17. 🔲 **PENDENTE:** CI/CD completo GitHub Actions (build + test + deploy)
18. 🔲 **PENDENTE:** Remover aplicação monolítica (limpeza)

**FASE 4 - Melhorias Avançadas (Baixa Prioridade)**
19-22. 🔲 **BACKLOG:** Cognito, Segurança, Performance, Resiliência, Docs

**Progresso Geral do Projeto:**
- Microserviços: 4/4 ✅ (100%)
- Integrações: 2/2 ✅ (100%)
- Testes E2E Local: 1/1 ✅ (100%)
- Testes E2E AWS: 1/1 ✅ (100%)
- Deploy Local (Minikube): 1/1 ✅ (100%)
- Deploy AWS (EKS): 1/1 ✅ (100%)
- RDS Databases: 3/3 ✅ (100%)
- **TOTAL FASE A: 14/14 tarefas (100%) ✅**

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

**Última revisão:** 2025-10-23 16:45
**Responsável:** Anderson
**Status Geral:** 🟢 65% Concluído - No prazo
**Sprint Atual:** Sprint 2 - Concluído com sucesso
**Próxima Milestone:** Completar Testes E2E + Remover Monolito

---

## 📈 RESUMO EXECUTIVO

### Conquistas desta Sessão (2025-10-27) - DEPLOY AWS COMPLETO ✅

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

**Última atualização desta sessão:** 2025-10-27 20:30
**Commits desta sessão:** Múltiplos (deploy AWS, RDS, LoadBalancers, test scripts)
**Responsável:** Anderson
**Status Geral:** 🟢 100% Concluído - FASE A COMPLETA ✅
**Próxima Milestone:** Melhorias opcionais (Cognito, Observabilidade, CI/CD) ou conclusão

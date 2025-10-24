# 📋 BACKLOG - Migração para Microserviços

**Projeto:** Sistema de Lanchonete - Arquitetura de Microserviços
**Branch Atual:** `feature/migracao-microservicos`
**Última Atualização:** 2025-10-23 16:45

---

## 🎯 VISÃO GERAL DO PROJETO

Migração completa da arquitetura monolítica para microserviços distribuídos. Todos os 4 microserviços core estão implementados e funcionais.

### Status dos Microserviços
- ✅ **Clientes** - Implementado, testado e operacional (commit: 148c9b2)
- ✅ **Pagamento** - Implementado, testado e operacional (commit: c67362f)
- ✅ **Pedidos** - Implementado, testado e operacional (commit: 66f7e45)
- ✅ **Cozinha** - Implementado, testado e operacional (commit: 0582da6)

### Progresso Geral
- **Microserviços:** 4/4 concluídos (100%)
- **Infraestrutura K8s:** StatefulSets MySQL, MongoDB, RabbitMQ (100%)
- **Integrações:** REST (Pedidos→Clientes) + RabbitMQ completas (100%)
- **Testes E2E:** Script básico implementado (70%)
- **Migração AWS:** Pendente (0%)

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

---

## 🚀 EM ANDAMENTO

### 1. Implementar Testes E2E Automatizados Completos
**Prioridade:** 🔴 ALTA
**Estimativa:** 1-2 dias
**Dependências:** ✅ Todos os 4 microserviços implementados
**Ambiente:** 💻 Minikube (Local)
**Status:** 70% Concluído

**Já Implementado (70%):**
- [x] Infraestrutura do script test-e2e.sh
- [x] Teste 1: Fluxo completo com cliente anônimo
  - [x] Criar pedido sem CPF
  - [x] Validar pagamento aprovado (evento RabbitMQ)
  - [x] Validar pedido adicionado na fila da cozinha (evento RabbitMQ)
  - [x] Iniciar preparo (AGUARDANDO → EM_PREPARO)
  - [x] Marcar como pronto (EM_PREPARO → PRONTO + evento RabbitMQ)
  - [x] Validar propagação do evento PedidoPronto
  - [x] Retirar pedido (PRONTO → FINALIZADO + evento RabbitMQ)
  - [x] Validar remoção da fila da cozinha
- [x] Integração RabbitMQ validada (todos os exchanges e bindings)
- [x] Validação de transições de estado completa

**Próximas Implementações (30%):**
- [ ] Teste 2: Fluxo completo com cliente identificado
  - [ ] Cadastrar cliente via POST /clientes
  - [ ] Criar pedido com CPF válido
  - [ ] Validar integração REST (Feign Client)
  - [ ] Validar nome do cliente no pedido (snapshot)
- [ ] Teste 3: Fluxo com pagamento rejeitado
  - [ ] Criar múltiplos pedidos até obter rejeição (mock 20%)
  - [ ] Validar status CANCELADO após rejeição
  - [ ] Validar que pedido NÃO é adicionado à fila da cozinha
- [ ] Teste 4: Validar snapshot de preços
  - [ ] Criar pedido com produtos
  - [ ] Alterar preço de produto no banco
  - [ ] Verificar que pedido mantém preço original
- [ ] Teste 5: Consulta de produtos por categoria
  - [ ] GET /produtos
  - [ ] GET /produtos/categoria/LANCHE
  - [ ] GET /produtos/categoria/BEBIDA
  - [ ] GET /produtos/categoria/ACOMPANHAMENTO
  - [ ] GET /produtos/categoria/SOBREMESA
- [ ] Teste 6: Validação de erros e edge cases
  - [ ] Pedido com produto inexistente (404)
  - [ ] Pedido com quantidade inválida (400)
  - [ ] Cliente com CPF inválido (400)
  - [ ] Iniciar preparo de pedido inexistente (404)
- [ ] Gerar relatório de testes consolidado (JSON/HTML)
- [ ] Adicionar métricas de tempo de execução

**Critérios de Aceite:**
- ✅ Fluxo básico funcionando (anônimo)
- [ ] Todos os 6 cenários de teste implementados
- [ ] 100% dos testes passando automaticamente
- [ ] Relatório de execução gerado (sucesso/falha/tempo)
- [ ] Documentação de execução no README.md
- [ ] Validação de todas as integrações (REST + RabbitMQ)
- [ ] Cobertura de cenários de erro

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

### 6. Melhorias de Observabilidade
**Estimativa:** 3-5 dias
**Ambiente:** ☁️ AWS

- [ ] Configurar Prometheus para métricas
- [ ] Configurar Grafana para dashboards
- [ ] Adicionar distributed tracing (AWS X-Ray)
- [ ] Configurar alertas CloudWatch (CPU, memória, latência, erros)
- [ ] Logs centralizados (CloudWatch Logs Insights)
- [ ] Dashboard de métricas de negócio (pedidos/hora, taxa de aprovação, etc)

### 7. Melhorias de Segurança Avançadas
**Estimativa:** 2-3 dias
**Ambiente:** ☁️ AWS

- [ ] Implementar grupos/roles no Cognito (admin, cliente, cozinha)
- [ ] Network Policies no Kubernetes
- [ ] AWS Secrets Manager para secrets sensíveis
- [ ] Scan de vulnerabilidades nas imagens Docker (Trivy/Snyk)
- [ ] WAF no ALB
- [ ] Rotação automática de secrets
- [ ] Audit logging completo

### 8. Otimizações de Performance
**Estimativa:** 2-3 dias
**Ambiente:** ☁️ AWS + Local

- [ ] Implementar cache (Redis/ElastiCache) para tokens JWT
- [ ] Otimizar queries SQL (índices, explain plan)
- [ ] Configurar Connection Pool adequadamente
- [ ] Implementar rate limiting por usuário
- [ ] Configurar HPA (Horizontal Pod Autoscaler) para todos os serviços
- [ ] Configurar PDB (Pod Disruption Budget)

### 9. Resiliência e Tolerância a Falhas
**Estimativa:** 2-3 dias
**Ambiente:** Local + AWS

- [ ] Implementar Circuit Breaker (Resilience4j)
- [ ] Configurar Retry policies com backoff exponencial
- [ ] Implementar fallback para chamadas REST
- [ ] Dead Letter Queue para RabbitMQ
- [ ] Health checks avançados (readiness vs liveness)
- [ ] Graceful shutdown
- [ ] Chaos Engineering (testes de resiliência)

### 10. Documentação e Governança
**Estimativa:** 2 dias
**Ambiente:** Local

- [x] Gerar documentação OpenAPI/Swagger para todos os microserviços - `2025-10-23`
- [ ] Criar diagramas de arquitetura atualizados com Cognito (C4 Model)
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

**FASE 1 - Local (Sem AWS) - 75% Concluído**
1. ✅ **CONCLUÍDO:** Infraestrutura K8s (MySQL, MongoDB, RabbitMQ) - commit d90b4a9
2. ✅ **CONCLUÍDO:** Microserviço de Clientes - commit 148c9b2
3. ✅ **CONCLUÍDO:** Microserviço de Pagamento - commit c67362f
4. ✅ **CONCLUÍDO:** Microserviço de Pedidos - commit 66f7e45
5. ✅ **CONCLUÍDO:** Microserviço de Cozinha - commit 0582da6
6. ✅ **CONCLUÍDO:** Integração REST (Pedidos → Clientes) - validado
7. ✅ **CONCLUÍDO:** Integração RabbitMQ (completa) - validado
8. ⏳ **EM PROGRESSO:** Testes E2E Local (70% - fluxo básico funcionando)
9. 🔲 **BLOQUEADO:** Remover Monolito (aguardando testes E2E 100%)

**FASE 2 - AWS (Requer AWS) - 0% Concluído**
10. 🔲 **PENDENTE:** Cognito (implementar autenticação)
11. 🔲 **PENDENTE:** Ingress EKS (expor serviços na AWS)
12. 🔲 **PENDENTE:** CI/CD Completo (automatizar deploy)

**FASE 3 - Melhorias (Opcional) - 0% Concluído**
13-17. 🔲 **BACKLOG:** Observabilidade, Segurança, Performance, Resiliência, Docs

**Progresso Geral do Projeto:**
- Microserviços: 4/4 ✅ (100%)
- Integrações: 2/2 ✅ (100%)
- Testes E2E: 7/10 ⏳ (70%)
- Limpeza: 0/1 🔲 (0%)
- AWS: 0/3 🔲 (0%)
- **TOTAL: 13/20 tarefas (65%)**

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

### Conquistas desta Sessão (2025-10-23)

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

**Última atualização desta sessão:** 2025-10-23 16:45
**Commits desta sessão:** 0582da6 (Cozinha) + mudanças não commitadas (BACKLOG.md, RabbitMQ fixes)
**Responsável:** Anderson
**Status Geral:** 🟢 65% Concluído - Fase 1 quase finalizada
**Próxima Milestone:** Completar Testes E2E (30% restante) + Remover Monolito

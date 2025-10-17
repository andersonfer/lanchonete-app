# 📋 BACKLOG - Migração para Microserviços

**Projeto:** Sistema de Lanchonete - Arquitetura de Microserviços
**Branch Atual:** `feature/migracao-microservicos`
**Última Atualização:** 2025-10-17

---

## 🎯 VISÃO GERAL

Migração da arquitetura monolítica para microserviços com os seguintes serviços:
- ✅ Clientes (implementado)
- ✅ Pedidos (implementado)
- ✅ Pagamento (implementado)
- ⏳ Cozinha (próximo)

---

## ✅ CONCLUÍDO

### Sprint 1 - Infraestrutura Base
- [x] Criação da infraestrutura K8s (MySQL, MongoDB, RabbitMQ) - `d90b4a9`
- [x] Implementação do microserviço de Clientes - `148c9b2`
- [x] Implementação do microserviço de Pagamento - `c67362f`
- [x] Implementação do microserviço de Pedidos - `main`
- [x] Integração REST: Pedidos → Clientes (validado)
- [x] Integração RabbitMQ: Pedidos ↔ Pagamento (validado)
- [x] Configuração CI/CD básico no GitHub Actions
- [x] Testes unitários dos 3 microserviços (80%+ cobertura)

---

## 🚀 PRÓXIMAS TAREFAS - FASE LOCAL (Sem dependência AWS)

### 1. Implementar Microserviço de Cozinha
**Prioridade:** 🔴 ALTA
**Estimativa:** 2-3 dias
**Dependências:** Pedidos e Pagamento funcionando
**Ambiente:** 💻 Minikube (Local)

**Checklist:**
- [ ] Estrutura Maven + Spring Boot 3
- [ ] Camada de Domínio (FilaCozinha, StatusFila)
- [ ] Use Cases (IniciarPreparo, MarcarComoPronto)
- [ ] Repository JDBC (MySQL)
- [ ] Feign Client para Pedidos (GET /pedidos/{id})
- [ ] RabbitMQ Consumer (PagamentoAprovado, PedidoRetirado)
- [ ] RabbitMQ Publisher (PedidoPronto)
- [ ] Controller REST (GET /fila, POST /{id}/iniciar, POST /{id}/pronto)
- [ ] Testes unitários (80%+ cobertura)
- [ ] Dockerfile multi-stage
- [ ] Manifests K8s (Deployment, Service, ConfigMap)
- [ ] Deploy e testes no Minikube
- [ ] Validação de endpoints via curl
- [ ] Testes de integração E2E completos

**Critérios de Aceite:**
- Serviço responde em http://192.168.49.2:30082
- Recebe eventos do Pagamento via RabbitMQ
- Publica evento PedidoPronto quando marcar como pronto
- Integração REST com Pedidos funcionando
- Fila de cozinha atualiza status corretamente

---

### 2. Implementar Testes E2E Automatizados (Local)
**Prioridade:** 🔴 ALTA
**Estimativa:** 2-3 dias
**Dependências:** Todos os 4 microserviços implementados
**Ambiente:** 💻 Minikube (Local)

**Checklist:**
- [ ] Criar script `scripts/e2e-tests-local.sh`
- [ ] Teste 1: Fluxo completo com cliente identificado
  - Cadastrar cliente
  - Criar pedido com CPF
  - Validar pagamento aprovado
  - Validar pedido na fila da cozinha
  - Iniciar preparo
  - Marcar como pronto
  - Retirar pedido
- [ ] Teste 2: Fluxo com cliente anônimo
- [ ] Teste 3: Fluxo com pagamento rejeitado
- [ ] Teste 4: Validar snapshot de preços
- [ ] Teste 5: Consulta de produtos por categoria
- [ ] Teste 6: Validar integrações REST (Pedidos → Clientes)
- [ ] Teste 7: Validar eventos RabbitMQ (todos os fluxos)
- [ ] Gerar relatório de testes

**Critérios de Aceite:**
- Script executa todos os cenários de forma automatizada no Minikube
- Todos os testes passam sem intervenção manual
- Relatório claro de sucesso/falha
- Documentação de como executar os testes

---

### 3. Remover Aplicação Monolítica (Autoatendimento)
**Prioridade:** 🔴 ALTA
**Estimativa:** 1 dia
**Dependências:** Cozinha implementado + Testes E2E passando
**Ambiente:** 💻 Local / Git

**Checklist:**
- [ ] Validar que todos os 4 microserviços estão funcionando
- [ ] Executar testes E2E completos e validar 100% sucesso
- [ ] Remover diretório `app/autoatendimento/`
- [ ] Remover diretório `app/pagamento/` (monolito)
- [ ] Remover manifests K8s do autoatendimento
- [ ] Liberar NodePort 30080
- [ ] Atualizar workflows do GitHub Actions:
  - Remover testes do autoatendimento de `ci-app.yml`
  - Remover build do autoatendimento de `cd-app.yml`
- [ ] Atualizar README.md (remover referências ao monolito)
- [ ] Limpar dependências não utilizadas

**Critérios de Aceite:**
- Aplicação monolítica completamente removida
- Workflows atualizados e validados
- Fluxo completo funciona apenas com microserviços
- Documentação atualizada

---

## ☁️ PRÓXIMAS TAREFAS - FASE AWS (Com dependência AWS)

### 4. Implementar Autenticação com AWS Cognito
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

### 5. Configurar Ingress para AWS EKS
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

### 6. Configurar CI/CD Completo no GitHub Actions
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

### 7. Melhorias de Observabilidade
**Estimativa:** 3-5 dias
**Ambiente:** ☁️ AWS

- [ ] Configurar Prometheus para métricas
- [ ] Configurar Grafana para dashboards
- [ ] Adicionar distributed tracing (AWS X-Ray)
- [ ] Configurar alertas CloudWatch (CPU, memória, latência, erros)
- [ ] Logs centralizados (CloudWatch Logs Insights)
- [ ] Dashboard de métricas de negócio (pedidos/hora, taxa de aprovação, etc)

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

- [ ] Gerar documentação OpenAPI/Swagger para todos os microserviços
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
  - Clientes: ✅ ~85%
  - Pedidos: ✅ ~82%
  - Pagamento: ✅ ~80%
  - Cozinha: ⏳ Pendente
  - Auth/Cognito: ⏳ Pendente

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

### Ordem de Implementação

**FASE 1 - Local (Sem AWS):**
1. ✅ Cozinha (tarefa 1) - Completar stack de microserviços
2. ✅ Testes E2E Local (tarefa 2) - Validar integração completa
3. ✅ Remover Monolito (tarefa 3) - Limpar código legado

**FASE 2 - AWS (Requer AWS):**
4. ☁️ Cognito (tarefa 4) - Implementar autenticação
5. ☁️ Ingress EKS (tarefa 5) - Expor serviços na AWS
6. ☁️ CI/CD Completo (tarefa 6) - Automatizar deploy

**FASE 3 - Melhorias (Opcional):**
7-11. Observabilidade, Segurança, Performance, Resiliência, Docs

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

**Última revisão:** 2025-10-17
**Responsável:** Anderson
**Status Geral:** 🟢 No prazo
**Próxima Milestone:** Fase 1 - Implementar Cozinha + Testes E2E Local + Remover Monolito

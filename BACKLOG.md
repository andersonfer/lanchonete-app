# BACKLOG - Sistema Lanchonete (Fase 4)

**Projeto:** Sistema de Lanchonete - Arquitetura de Microserviços
**Branch Atual:** feature/atualizacao_backlog
**Última Atualizacao:** 2025-11-18 08:30
**Fase Atual:** Fase 4 - CI/CD e Qualidade de Codigo (FASE FINAL)

---

## VISAO GERAL DO PROJETO

### Status da Migracao para Microservicos
- 4 microservicos implementados e operacionais (100%)
- Arquitetura distribuida com comunicacao via REST e RabbitMQ
- Deploy em AWS EKS com infraestrutura completa
- Testes E2E validados em ambiente local e AWS

### Microservicos Implementados
1. **Clientes** - Gestao de clientes e identificacao (MySQL)
2. **Pedidos** - Gestao de pedidos e produtos (MySQL)
3. **Cozinha** - Gestao da fila de producao (MySQL)
4. **Pagamento** - Processamento de pagamentos (MongoDB)

### Progresso Geral Fase 4
- **Migracao Microservicos:** 100% CONCLUIDO
- **Bancos de Dados:** 100% CONCLUIDO (3x MySQL + 1x MongoDB)
- **Comunicacao entre Servicos:** 100% CONCLUIDO (REST + RabbitMQ)
- **Testes Unitarios:** 100% CONCLUIDO (86.5% cobertura media)
- **CI com SonarCloud:** 100% CONCLUIDO
- **Testes BDD:** 100% CONCLUIDO (19 cenários em 4 serviços)
- **CD para AWS EKS:** 100% CONCLUIDO (SHA versioning implementado)
- **Seguranca:** 100% CONCLUIDO (Log Injection vulnerabilidade corrigida)
- **Branch Protection:** 90% CONCLUIDO (checks configurados, regras pendentes)
- **Documentacao:** 60% CONCLUIDO (badges e evidencias pendentes)
- **Video Demo:** 0% PENDENTE

---

## SCRUM MASTER SESSION REPORT

### Recent Progress Analysis (Ultimas 24h - 2025-11-18)

**Commits Analisados (desde 2025-11-17 08:00):**
- `8b5dd2b` - SECURITY FIX: Remove entrada do usuario do log (Log Injection)
- `8c96e55` - Merge branch 'main' into test/cd-pedidos
- `a53b4c3` - Ajustes no pipeline de pedidos (SHA versioning)
- `696dd1b` - Merge PR #19: test/cd-pagamento (validated)
- `62ed446` - Ajustes no pipeline de pagamentos (SHA versioning)
- `74ae056` - Merge PR #18: test/cd-cozinha (validated)
- `3ddb53c` - Ajustes no pipeline da cozinha (SHA versioning)
- `d0e8478` - Merge PR #16: feature/cd-microservicos (SHA implementation)
- `3d6d701` - Corrige imagens para adicionar SHA
- `8650a2d` - Merge PR #15: melhorias-cd-clientes
- `ada6639` - Criacao da tag versionada das imagens docker
- `aaf2384` - Teste do pipeline de clientes (20251117)
- `69de214` - Adiciona mensagem de log para testar pipeline

**Principais Mudancas:**
- ✅ CRITICO: SHA-based versioning implementado em todos os 4 CD pipelines
- ✅ SECURITY: Vulnerabilidade de Log Injection corrigida no servico Pedidos
- ✅ Branch protection ajustada para usar check generico "Testes e Análise SonarCloud"
- ✅ 4 PRs de teste mergeados com sucesso (test/cd-*)
- ✅ Deploy automatico funcionando sem necessidade de kubectl rollout restart manual
- ✅ Todas as imagens Docker agora tagueadas com :latest e :sha

### Completed Items (Ultima Sessao - 2025-11-18)

**ENTREGAVEL 3c: CD com SHA Versioning** ✅ CONCLUIDO (NOVA FEATURE!)
- ✅ CD Clientes: SHA versioning + automatic deployment (PRs #15, #16)
- ✅ CD Pedidos: SHA versioning + automatic deployment (PR #16, commit a53b4c3)
- ✅ CD Cozinha: SHA versioning + automatic deployment (PRs #16, #18)
- ✅ CD Pagamento: SHA versioning + automatic deployment (PRs #16, #19)
- ✅ Todas imagens Docker tagueadas com: `:latest` e `:${{ github.sha }}`
- ✅ Deploy usa tag especifica SHA para confiabilidade
- ✅ Rollout automatico sem necessidade de kubectl restart manual
- ✅ Smoke tests validando health, readiness, liveness em todos os servicos

**SECURITY: Log Injection Vulnerability** ✅ CORRIGIDO
- ✅ SonarCloud detectou vulnerabilidade no servico Pedidos (commit 8b5dd2b)
- ✅ Entrada do usuario removida dos logs
- ✅ Quality Gate passou apos correcao
- ✅ Todos os 4 servicos agora sem vulnerabilidades criticas

**BRANCH PROTECTION: Checks Unificados** ✅ MELHORADO
- ✅ Branch protection usando check generico "Testes e Análise SonarCloud"
- ✅ Nao precisa mais de 4 checks individuais por servico
- ✅ PRs bloqueados ate CI passar

**HISTORICO DE ENTREGAS ANTERIORES:**

**FASE 4 - ENTREGAVEL 1: Refatoracao para Microservicos** ✅ CONCLUIDO
- ✅ 4 microservicos (Clientes, Pedidos, Cozinha, Pagamento)
- ✅ 3x MySQL + 1x MongoDB
- ✅ REST (Feign Client) + RabbitMQ
- ✅ Isolamento completo de bancos de dados

**FASE 4 - ENTREGAVEL 2a: Testes Unitarios 80%+** ✅ CONCLUIDO
- ✅ Clientes: 85%, Pedidos: 83%, Cozinha: 84%, Pagamento: 94%
- ✅ Media: 86.5% (supera meta de 80%)

**FASE 4 - ENTREGAVEL 2b: Testes BDD** ✅ CONCLUIDO
- ✅ 19 cenarios BDD em 4 servicos (Cucumber + Gherkin)

**FASE 4 - ENTREGAVEL 3b: CI com SonarCloud** ✅ CONCLUIDO
- ✅ 4 workflows CI com Quality Gates > 70%
- ✅ Path filters configurados
- ✅ Trigger em PRs para main

### Current Status

**Total de Entregaveis Fase 4:** 9 principais
- **Concluidos:** 8/9 (88.9%) ⬆️ +22.2% desde ultima sessao
- **Em Progresso:** 0/9
- **Pendentes:** 1/9 (11.1%)

**Quality Metrics (EXCELENTES):**
- Cobertura Media de Testes: 86.5% (meta: 80%) ✅
- Testes BDD: 19 cenários (100% passing) ✅
- Testes Totais: 393 (374 unitários + 19 BDD) ✅
- Pipelines CI Funcionando: 4/4 (100%) ✅
- Pipelines CD Funcionando: 4/4 (100%) ✅
- SonarCloud Quality Gates: 4/4 PASSED ✅
- Vulnerabilidades Criticas: 0/4 (100% seguro) ✅

**GitHub Actions Status (Ultimos PRs #15-#19):**
- ✅ CI - Clientes: SUCCESS (com SonarCloud)
- ✅ CI - Pedidos: SUCCESS (vulnerabilidade corrigida)
- ✅ CI - Cozinha: SUCCESS (com SonarCloud)
- ✅ CI - Pagamento: SUCCESS (com SonarCloud)
- ✅ CD - Clientes: SUCCESS (SHA: ada6639)
- ✅ CD - Pedidos: SUCCESS (SHA: a53b4c3)
- ✅ CD - Cozinha: SUCCESS (SHA: 3ddb53c)
- ✅ CD - Pagamento: SUCCESS (SHA: 62ed446)

**AWS EKS Deployment Status:**
- Cluster: lanchonete-cluster (us-east-1) ✅
- Todos os 4 servicos deployed com SHA versioning ✅
- ALBs provisionados e respondendo ✅
- Smoke tests passando (health/readiness/liveness) ✅

### Blockers & Risks

**SEM BLOQUEADORES CRITICOS!** 🎉 Todos os problemas tecnicos resolvidos.

**PENDENCIAS (NAO-BLOQUEADORAS):**

1. ⚠️ **Monorepo vs Multi-Repo** (Entregavel 3a - Decisao Arquitetural)
   - Situacao: Usando monorepo com path filters (padrao da industria)
   - Impacto: BAIXO - Monorepo e usado por Google, Microsoft, Uber para microservicos
   - Mitigacao: Documentar decisao e justificar no video
   - Path filters garantem isolamento: cada servico tem CI/CD independente
   - **Recomendacao:** Manter monorepo + criar documento de justificativa

2. ⚠️ **Branch Protection Rules** (Entregavel 3a - Parcialmente Completo)
   - Status: Checks configurados, regras de protecao pendentes
   - Impacto: BAIXO - PRs ja estao sendo revisados e checks obrigatorios funcionam
   - Acao: Configurar regras formais no GitHub (30 minutos)

3. ⚠️ **Documentacao e Badges** (Entregavel 4b - 60% Completo)
   - Faltam: Badges SonarCloud, badges GitHub Actions, links para dashboards
   - Impacto: BAIXO - Documentacao existe, falta apenas visibilidade
   - Acao: Adicionar badges e links (2-3 horas)

4. ⚠️ **Video de Demonstracao** (Entregavel 4a - Obrigatorio)
   - Status: Nao iniciado
   - Impacto: ALTO - Requisito obrigatorio para entrega
   - Acao: Gravar video de 15-20 minutos (4-6 horas)
   - Prioridade: ALTA

---

## ENTREGAVEIS FASE 4 - RASTREABILIDADE

### ENTREGAVEL 1: Refatoracao para Microservicos ✅ CONCLUIDO

**Requisitos:**
- [x] Ao menos 3 microservicos (implementados 4)
- [x] Banco SQL e NoSQL (MySQL + MongoDB)
- [x] Comunicacao entre servicos (REST + RabbitMQ)
- [x] Isolamento de bancos de dados

**Evidencias:**
- Commit: 72bb829 (merge PR #7)
- Estrutura: /services/{clientes,pedidos,cozinha,pagamento}
- Arquitetura documentada em README.md

---

### ENTREGAVEL 2: Testes Unitarios e BDD

#### 2a. Testes Unitarios com 80% Cobertura ✅ CONCLUIDO

**Status por Servico:**
- ✅ Clientes: 85% (target/site/jacoco/index.html)
- ✅ Pedidos: 83% (target/site/jacoco/index.html)
- ✅ Cozinha: 84% (target/site/jacoco/index.html)
- ✅ Pagamento: 94% (target/site/jacoco/index.html)

**Evidencias:**
- Plugin JaCoCo configurado em todos os pom.xml
- Relatorios gerados automaticamente no CI
- SonarCloud validando cobertura

#### 2b. Testes BDD (Ao menos um caminho) ✅ CONCLUIDO

**Status:** IMPLEMENTADO EM 4 SERVICOS

**Requisitos:**
- [x] Implementar BDD em ao menos um servico
- [x] Usar Cucumber ou framework similar
- [x] Escrever features em Gherkin
- [x] Integrar com CI/CD

**Implementação:**
- ✅ **Clientes:** 6 cenários BDD (2 features)
  - Feature: Cadastro de Cliente (4 cenários)
  - Feature: Identificação Cliente (2 cenários)
- ✅ **Pagamento:** 5 cenários BDD (1 feature)
  - Feature: Validação de Valor de Pagamento (5 cenários)
- ✅ **Pedidos:** 4 cenários BDD (2 features)
  - Feature: Consultar Produtos (1 cenário)
  - Feature: Criar Pedido (3 cenários)
- ✅ **Cozinha:** 4 cenários BDD (2 features)
  - Feature: Consultar Fila da Cozinha (2 cenários)
  - Feature: Gerenciar Preparo de Pedidos (2 cenários)

**Total:** 19 cenários BDD implementados e passando
**Framework:** Cucumber 7.18.0 + JUnit Platform
**Linguagem:** Gherkin em Português
**Padrão:** Given-When-Then
**Relatórios:** HTML + JSON gerados automaticamente

**Evidências:**
- Commit: feature/testes-bdd merged to main
- Tests run: 393 (374 unitários + 19 BDD)
- Success rate: 100%
- Relatórios: target/cucumber-reports/ em cada serviço

---

### ENTREGAVEL 3: Repositorios e CI/CD

#### 3a. Branch Protection ❌ BLOQUEADOR

**Requisitos:**
- [ ] Branches main/master protegidas
- [ ] Commits diretos bloqueados
- [ ] Repositorios separados por aplicacao

**Status Atual:**
- ❌ Repositorio unico (monorepo)
- ❌ Branch protection nao configurada
- ❌ Necessario separar em 4 repositorios

**Acao Necessaria:** DECISAO ARQUITETURAL

**Opcao A: Manter Monorepo (Requer Justificativa)**
- Configurar branch protection no repo atual
- Justificar escolha no video de entrega
- Manter path filters nos workflows

**Opcao B: Separar Repositorios (Segue Requisito)**
- Criar 4 repositorios novos
- Migrar historico git de cada servico
- Configurar CI/CD em cada repo
- Adicionar usuario soat-architecture em todos

**Recomendacao:** Opcao A (monorepo com justificativa)
- Menos complexidade de gerenciamento
- Path filters ja implementados
- CI/CD ja funcional
- Justificavel por ser projeto academico

#### 3b. CI com SonarCloud ✅ CONCLUIDO

**Status:**
- ✅ 4 workflows de CI criados
- ✅ SonarCloud configurado (organization: andersonfer)
- ✅ Quality Gates funcionando
- ✅ Coverage > 70% em todos os servicos

**Evidencias:**
- .github/workflows/ci-clientes.yml
- .github/workflows/ci-pedidos.yml
- .github/workflows/ci-cozinha.yml
- .github/workflows/ci-pagamento.yml

**Projects SonarCloud:**
- andersonfer_lanchonete-clientes
- andersonfer_lanchonete-pedidos
- andersonfer_lanchonete-cozinha
- andersonfer_lanchonete-pagamento

#### 3c. CD para Deploy Automatico ✅ CONCLUIDO

**Requisitos:**
- [x] Deploy automatico no merge para main
- [x] Todos os microservicos devem ser deployados
- [x] Validacao de testes antes do deploy

**Status:** CONCLUIDO (2025-11-17)

**Implementação:**
- ✅ Criado cd-clientes.yml (1m27s de execução)
- ✅ Criado cd-pedidos.yml (51s de execução)
- ✅ Criado cd-cozinha.yml (1m1s de execução)
- ✅ Criado cd-pagamento.yml (49s de execução)
- ✅ AWS credentials configurados nos GitHub Secrets
- ✅ Deploy validado em EKS (todos os 4 serviços)
- ✅ Smoke tests implementados (health, readiness, liveness)
- ✅ ALB validação não-bloqueante

**Detalhes dos Pipelines:**

Cada pipeline CD possui 12 passos:
1. Checkout código
2. Setup Java 17 com Maven cache
3. Build aplicação (skip tests)
4. Configurar AWS Credentials
5. Login no Amazon ECR
6. Build & Push Docker (:latest tag)
7. Configurar kubectl
8. Deploy no EKS (ConfigMap, Service, Deployment, Ingress)
9. Aguardar rollout (timeout 5min)
10. Smoke Tests (health, readiness, liveness)
11. Verificar ALB (não-bloqueante)
12. Resumo do deploy

**Evidências:**
- PR #9: cd-clientes.yml (Run ID: 19440589914)
- PR #10: cd-pagamento.yml (Run ID: 19440881080)
- PR #11: cd-cozinha.yml (Run ID: 19441051052)
- PR #12: cd-pedidos.yml (Run ID: 19441342925)

**ALBs Provisionados:**
- Clientes: lanchonete-clientes-alb-199660999.us-east-1.elb.amazonaws.com
- Pagamento: lanchonete-pagamento-alb-1162736294.us-east-1.elb.amazonaws.com
- Cozinha: lanchonete-cozinha-alb-1426986788.us-east-1.elb.amazonaws.com
- Pedidos: lanchonete-pedidos-alb-2100039014.us-east-1.elb.amazonaws.com

**Prioridade:** P0 - CRITICA (obrigatorio para entrega) - ✅ CONCLUIDO

#### 3d. Teste Integrado CI+CD ❌ PENDENTE

**Objetivo:** Validar que todo o fluxo CI+CD está funcionando end-to-end antes de dar a tarefa por concluída.

**Requisitos:**
- [ ] Fazer alteração em cada serviço (ex: adicionar log, comentário)
- [ ] Criar branch feature para cada serviço
- [ ] Abrir PR e validar que CI executa automaticamente
- [ ] Validar Quality Gate do SonarCloud (deve passar)
- [ ] Fazer merge para main
- [ ] Validar que CD executa automaticamente
- [ ] Validar que deploy foi realizado com sucesso no EKS
- [ ] Validar que smoke tests passaram
- [ ] Validar que ALB está respondendo
- [ ] Validar que nova versão está no ar

**Cenários de Teste:**

1. **Teste Clientes:**
   - Adicionar comentário em ClienteController
   - Branch: test/ci-cd-clientes
   - Validar CI → CD → Deploy

2. **Teste Pedidos:**
   - Adicionar comentário em PedidoController
   - Branch: test/ci-cd-pedidos
   - Validar CI → CD → Deploy

3. **Teste Cozinha:**
   - Adicionar comentário em CozinhaController
   - Branch: test/ci-cd-cozinha
   - Validar CI → CD → Deploy

4. **Teste Pagamento:**
   - Adicionar comentário em PagamentoController
   - Branch: test/ci-cd-pagamento
   - Validar CI → CD → Deploy

**Critérios de Aceitação:**
- ✅ CI executa em todos os PRs
- ✅ Quality Gate passa em todos os serviços
- ✅ CD executa após merge para main
- ✅ Deploy realizado com sucesso (4/4 serviços)
- ✅ Smoke tests passam (4/4 serviços)
- ✅ ALBs respondendo corretamente (4/4 serviços)
- ✅ Pods em estado Running no EKS (4/4 serviços)

**Estimativa:** 2-3 horas
**Prioridade:** P0 - CRITICA (validação final antes de considerar CD completo)
**Status:** PENDENTE (próxima sessão)

---

### ENTREGAVEL 4: Artefatos de Entrega ❌ PENDENTE

#### 4a. Video Demonstracao (OBRIGATORIO)

**Requisitos:**
- [ ] Demonstrar funcionamento da aplicacao
- [ ] Mostrar atualizacoes na arquitetura
- [ ] Mostrar processo de deploy dos microservicos
- [ ] Mostrar testes funcionando
- [ ] Mostrar checks verdes (nao precisa mostrar pipelines rodando)

**Status:** NAO INICIADO
**Estimativa:** 1 dia de gravacao + edicao
**Prioridade:** P0 - CRITICA

#### 4b. Links e Evidencias no README (OBRIGATORIO)

**Requisitos:**
- [ ] Links para todos os repositorios
- [ ] Evidencia de cobertura por microservico
- [ ] Screenshots ou links do SonarCloud
- [ ] Adicionar usuario soat-architecture aos repos

**Status:** PARCIALMENTE CONCLUIDO
- ✅ README.md existe e esta documentado
- ❌ Falta adicionar badges do SonarCloud
- ❌ Falta adicionar badges de CI/CD
- ❌ Falta adicionar evidencias de cobertura

**Estimativa:** 2-3 horas
**Prioridade:** P1 - ALTA

---

## PROXIMAS TAREFAS RECOMENDADAS

### SPRINT ATUAL: Finalizacao Fase 4 (2-3 dias restantes)

**STATUS ATUAL (2025-11-18 08:30):**
- ✅ Microservicos implementados (4 servicos, 100%)
- ✅ Testes unitarios (86.5% cobertura, supera 80%)
- ✅ Testes BDD (19 cenarios, 4 servicos)
- ✅ Pipelines CI (4 workflows, SonarCloud integrado)
- ✅ Pipelines CD (4 workflows, SHA versioning)
- ✅ Seguranca (0 vulnerabilidades criticas)
- ⏳ Documentacao (badges e evidencias)
- ⏳ Branch protection (regras formais)
- ⏳ Video de demonstracao (OBRIGATORIO)

**PROXIMA SESSAO (PRIORIDADE MAXIMA): DOCUMENTACAO + VIDEO**

### Immediate Priority (Start Now)

**0. [CLEANUP] Remover Código Legado do Monolito** 🧹 LIMPEZA TÉCNICA
   - **Why:** Remover código antigo não utilizado, organizar repositório
   - **Depends on:** Nothing
   - **Effort:** 30 minutos
   - **Priority:** P1 - MEDIA
   - **Acceptance Criteria:**
     - [ ] Deletar diretório `app/` (código monolito antigo)
     - [ ] Deletar diretório `k8s_manifests/` (manifestos k8s antigos)
     - [ ] Deletar diretório `scripts_k8s/` (scripts antigos)
     - [ ] Verificar ausência de referências aos diretórios deletados
     - [ ] Commit: "chore: remove legacy monolith code and old k8s manifests"

**1. [DOCS] Update README with Badges and Evidence** ⭐ COMECE AQUI
   - **Why:** Visibilidade do progresso, cumpre entregavel 4b
   - **Depends on:** Nothing - pode iniciar imediatamente
   - **Effort:** 2-3 horas
   - **Priority:** P0 - ALTA
   - **Acceptance Criteria:**
     - [ ] Adicionar 4 badges SonarCloud Quality Gate
     - [ ] Adicionar 4 badges SonarCloud Coverage
     - [ ] Adicionar 8 badges GitHub Actions (4 CI + 4 CD)
     - [ ] Adicionar links para dashboards SonarCloud
     - [ ] Documentar ALBs e endpoints AWS
     - [ ] Explicar SHA versioning strategy
     - [ ] Adicionar secao "What's New in Phase 4"

**2. [REPO] Configure Branch Protection Rules** ✅ CONCLUIDO
   - **Status:** Branch protection configurada com sucesso
   - **Verificado em:** 2025-11-18
   - **Configuracao:**
     - ✅ PRs obrigatorios antes de merge
     - ✅ Check "Testes e Análise SonarCloud" obrigatorio
     - ✅ Enforce admins ativado
     - ✅ Force pushes bloqueados
     - ✅ Branch deletions bloqueados

**3. [DOCS] Create Monorepo Architecture Decision Document** ❌ NAO NECESSARIO
   - **Status:** Validado com professor - nao e necessario
   - **Razao:** Monorepo e aceitavel para o projeto
   - **Data:** 2025-11-18

### Following Priority (Next 2-3 Days)

**4. [VIDEO] Record Demo Video** 🎬 OBRIGATORIO
   - **Why:** Entregavel obrigatorio 4a
   - **Depends on:** Badges e documentacao completos (para referencia)
   - **Effort:** 4-6 horas (roteiro + gravacao + edicao)
   - **Priority:** P0 - CRITICA
   - **Acceptance Criteria:**
     - [ ] Roteiro completo (15-20 minutos)
     - [ ] Secao 1: Arquitetura (4 min) - microservicos, bancos, comunicacao
     - [ ] Secao 2: Aplicacao funcionando (5 min) - fluxo completo E2E
     - [ ] Secao 3: Testes (4 min) - cobertura, BDD, SonarCloud
     - [ ] Secao 4: CI/CD (4 min) - pipelines, SHA versioning, deploy AWS
     - [ ] Secao 5: Monorepo decision (2 min) - justificativa
     - [ ] Video editado e publicado (YouTube unlisted)
     - [ ] Link testado e funcionando

**5. [DOCS] Add Evaluator Access** ✅ CONCLUIDO
   - **Status:** Usuario @soat-architecture adicionado como collaborator
   - **Verificado em:** 2025-11-18
   - **Permissao:** Read access concedido
   - **Documentacao:** Pendente no README

**6. [ENTREGA] Prepare Final Submission**
   - **Why:** Submissao final do projeto
   - **Depends on:** Todos os itens anteriores
   - **Effort:** 1 hora
   - **Priority:** P0 - CRITICA
   - **Acceptance Criteria:**
     - [ ] Documento com nomes dos integrantes + Discord IDs
     - [ ] Links para repositorio(s)
     - [ ] Link do video YouTube
     - [ ] Link SonarCloud organization
     - [ ] Validacao final de todos os requisitos
     - [ ] Submeter no prazo

---

## TAREFAS DETALHADAS (SPRINT ATUAL)

### TAREFA 1: Implementar Testes BDD (P0 - CRITICA)

**Servico Escolhido:** Clientes (mais simples)

**Subtarefas:**
- [ ] Adicionar dependencias Cucumber ao pom.xml
  ```xml
  <dependency>
    <groupId>io.cucumber</groupId>
    <artifactId>cucumber-java</artifactId>
    <version>7.14.0</version>
    <scope>test</scope>
  </dependency>
  <dependency>
    <groupId>io.cucumber</groupId>
    <artifactId>cucumber-spring</artifactId>
    <version>7.14.0</version>
    <scope>test</scope>
  </dependency>
  <dependency>
    <groupId>io.cucumber</groupId>
    <artifactId>cucumber-junit-platform-engine</artifactId>
    <version>7.14.0</version>
    <scope>test</scope>
  </dependency>
  ```

- [ ] Criar estrutura de diretórios
  ```
  src/test/resources/features/
  src/test/java/br/com/lanchonete/clientes/bdd/
  ```

- [ ] Escrever features (3 cenarios minimo)
  - Feature: Cadastrar Cliente
    - Cenario: Cadastrar cliente com sucesso
    - Cenario: Cadastrar cliente com CPF invalido
    - Cenario: Cadastrar cliente duplicado

  - Feature: Identificar Cliente
    - Cenario: Identificar cliente existente
    - Cenario: Identificar cliente inexistente

- [ ] Implementar Step Definitions
  - CadastrarClienteSteps.java
  - IdentificarClienteSteps.java

- [ ] Configurar Cucumber no CI
  - Atualizar ci-clientes.yml
  - Executar testes BDD junto com unitarios
  - Gerar relatorio HTML

- [ ] Validar execucao local e no CI

**Criterios de Aceite:**
- Ao menos 3 features escritas em Gherkin
- Step definitions implementados
- Testes BDD executam no CI
- Relatorio gerado e acessivel

**Estimativa:** 8-10 horas
**Prioridade:** P0 - CRITICA

---

### TAREFA 2: Implementar Pipelines de CD (P0 - CRITICA)

**Estrutura dos Workflows:**

Cada servico tera um workflow de CD com os seguintes passos:
1. Trigger: Push em main com mudancas em services/{servico}/**
2. Checkout codigo
3. Setup Java 17
4. Build com Maven
5. Build imagem Docker
6. Login no AWS ECR
7. Push imagem para ECR
8. Configurar kubectl com EKS
9. Aplicar secrets K8s
10. Deploy manifests K8s
11. Aguardar rollout
12. Smoke tests
13. Notificar status

**Subtarefas:**

#### 2.1 CD - Clientes
- [ ] Criar .github/workflows/cd-clientes.yml
- [ ] Configurar path filter: services/clientes/**
- [ ] Implementar build e push para ECR
- [ ] Implementar deploy para EKS
- [ ] Implementar smoke tests:
  - GET /actuator/health → 200 OK
  - POST /clientes → 201 Created
  - GET /clientes/{cpf} → 200 OK
- [ ] Testar workflow

#### 2.2 CD - Pedidos
- [ ] Criar .github/workflows/cd-pedidos.yml
- [ ] Configurar path filter: services/pedidos/**
- [ ] Implementar build e push para ECR
- [ ] Implementar deploy para EKS
- [ ] Implementar smoke tests:
  - GET /actuator/health → 200 OK
  - GET /produtos → 200 OK
  - POST /pedidos → 201 Created
- [ ] Testar workflow

#### 2.3 CD - Cozinha
- [ ] Criar .github/workflows/cd-cozinha.yml
- [ ] Configurar path filter: services/cozinha/**
- [ ] Implementar build e push para ECR
- [ ] Implementar deploy para EKS
- [ ] Implementar smoke tests:
  - GET /actuator/health → 200 OK
  - GET /cozinha/fila → 200 OK
- [ ] Testar workflow

#### 2.4 CD - Pagamento
- [ ] Criar .github/workflows/cd-pagamento.yml
- [ ] Configurar path filter: services/pagamento/**
- [ ] Implementar build e push para ECR
- [ ] Implementar deploy para EKS
- [ ] Implementar smoke tests:
  - GET /actuator/health → 200 OK
- [ ] Testar workflow

#### 2.5 Configuracoes Globais
- [ ] Configurar secrets GitHub:
  - AWS_ACCESS_KEY_ID
  - AWS_SECRET_ACCESS_KEY
  - AWS_SESSION_TOKEN (se AWS Academy)
  - AWS_REGION (us-east-1)
  - ECR_REGISTRY (obter do Terraform)
- [ ] Documentar processo de CD no README
- [ ] Criar runbook de troubleshooting

**Criterios de Aceite:**
- 4 workflows de CD criados e funcionais
- Deploy automatico no merge para main
- Smoke tests validando deploy
- Documentacao completa

**Estimativa:** 3-4 dias
**Prioridade:** P0 - CRITICA

---

### TAREFA 3: Branch Protection e Documentacao (P1 - ALTA)

#### 3.1 Branch Protection
- [ ] Acessar Settings → Branches no GitHub
- [ ] Adicionar rule para branch main
- [ ] Configuracoes:
  - [x] Require pull request before merging
  - [x] Require approvals: 1
  - [x] Require status checks to pass
    - CI - Clientes
    - CI - Pedidos
    - CI - Cozinha
    - CI - Pagamento
  - [x] Require branches to be up to date
  - [x] Do not allow bypassing the above settings
- [ ] Salvar configuracao
- [ ] Testar com PR de teste

#### 3.2 README Atualizado
- [ ] Adicionar secao "Quality & CI/CD"
- [ ] Badges SonarCloud (4):
  ```markdown
  [![Quality Gate - Clientes](https://sonarcloud.io/api/project_badges/measure?project=andersonfer_lanchonete-clientes&metric=alert_status)](https://sonarcloud.io/dashboard?id=andersonfer_lanchonete-clientes)
  [![Coverage - Clientes](https://sonarcloud.io/api/project_badges/measure?project=andersonfer_lanchonete-clientes&metric=coverage)](https://sonarcloud.io/dashboard?id=andersonfer_lanchonete-clientes)
  ```
- [ ] Badges GitHub Actions (8):
  - CI Clientes, Pedidos, Cozinha, Pagamento
  - CD Clientes, Pedidos, Cozinha, Pagamento
- [ ] Secao de evidencias de cobertura
  - Link SonarCloud por servico
  - Screenshot ou tabela com percentuais
- [ ] Adicionar usuario soat-architecture como colaborador

**Estimativa:** 2-3 horas
**Prioridade:** P1 - ALTA

---

### TAREFA 4: Video de Demonstracao (P0 - CRITICA)

**Roteiro Proposto (15-20 minutos):**

1. **Introducao (2 min)**
   - Apresentacao do grupo
   - Visao geral do projeto
   - Objetivos da Fase 4

2. **Arquitetura (4 min)**
   - Mostrar diagrama de arquitetura
   - Explicar separacao em microservicos
   - Mostrar bancos de dados (MySQL + MongoDB)
   - Explicar comunicacao (REST + RabbitMQ)

3. **Demonstracao da Aplicacao (5 min)**
   - Criar pedido via API
   - Mostrar fluxo completo:
     - Pedido criado
     - Pagamento processado
     - Fila da cozinha
     - Pedido pronto
     - Retirada
   - Mostrar logs e eventos RabbitMQ

4. **Testes e Qualidade (4 min)**
   - Mostrar cobertura de testes (80%+)
   - Executar testes unitarios
   - Mostrar testes BDD (Cucumber)
   - Mostrar relatorios SonarCloud

5. **CI/CD (4 min)**
   - Mostrar workflows GitHub Actions
   - Mostrar checks verdes em PR
   - Explicar processo de CI (testes + SonarCloud)
   - Explicar processo de CD (deploy automatico)
   - Mostrar deploy no EKS (kubectl get pods)

6. **Conclusao (1 min)**
   - Resumo dos entregaveis
   - Agradecimentos

**Subtarefas:**
- [ ] Preparar ambiente (limpar logs, preparar dados de teste)
- [ ] Gravar video (OBS Studio ou similar)
- [ ] Editar video (cortes, legendas se necessario)
- [ ] Upload para YouTube (nao listado ou publico)
- [ ] Testar link do video
- [ ] Adicionar link ao documento de entrega

**Estimativa:** 4-6 horas
**Prioridade:** P0 - CRITICA

---

## STATUS GERAL DA FASE 4

### Checklist de Entregaveis

**OBRIGATORIOS:**
- [x] 1. Refatoracao em ao menos 3 microservicos (4 implementados)
- [x] 2a. Testes unitarios com 80% cobertura (media 86.5%)
- [x] 2b. Ao menos um caminho com BDD (19 cenários implementados!)
- [ ] 3a. Repositorios separados OU justificativa (DECISAO PENDENTE)
- [x] 3b. CI com SonarCloud 70%+ coverage (4 workflows funcionando)
- [x] 3c. CD com deploy automatico (4 workflows funcionando)
- [ ] 3d. Teste integrado CI+CD (PENDENTE - validação final)
- [ ] 4a. Video demonstracao (PENDENTE)
- [ ] 4b. Links e evidencias no README (PARCIAL)

**PERCENTUAL DE CONCLUSAO:** 55.5% (5/9 entregaveis + 1 validação pendente)

### Metricas de Qualidade

**Cobertura de Testes:**
- Clientes: 85% ✅ (meta: 80%)
- Pedidos: 83% ✅ (meta: 80%)
- Cozinha: 84% ✅ (meta: 80%)
- Pagamento: 94% ✅ (meta: 80%)
- **Media:** 86.5% ✅

**SonarCloud Quality Gates:**
- Clientes: PASSED ✅
- Pedidos: PASSED ✅
- Cozinha: PASSED ✅
- Pagamento: PASSED ✅

**Pipelines GitHub Actions:**
- CI Clientes: ✅ SUCCESS
- CI Pedidos: ✅ SUCCESS
- CI Cozinha: ✅ SUCCESS
- CI Pagamento: ✅ SUCCESS
- CD Clientes: ✅ SUCCESS (1m27s - Run #19440589914)
- CD Pedidos: ✅ SUCCESS (51s - Run #19441342925)
- CD Cozinha: ✅ SUCCESS (1m1s - Run #19441051052)
- CD Pagamento: ✅ SUCCESS (49s - Run #19440881080)

---

## RISCOS E MITIGACOES

### RISCO 1: Tempo Insuficiente para CD
**Probabilidade:** MEDIA
**Impacto:** ALTO (entregavel obrigatorio)
**Mitigacao:**
- Priorizar implementacao de CD imediatamente apos BDD
- Usar template de workflow para acelerar
- Focar em smoke tests simples (health check + 1 endpoint)

### RISCO 2: Repositorios Separados
**Probabilidade:** BAIXA (se escolher monorepo)
**Impacto:** ALTO (pode reprovar se exigido)
**Mitigacao:**
- Documentar decisao de manter monorepo
- Justificar no video (path filters, gerenciamento simplificado)
- Alternativa: separar repos em ultimo caso

### RISCO 3: BDD Complexo
**Probabilidade:** MEDIA
**Impacto:** MEDIO
**Mitigacao:**
- Escolher servico mais simples (Clientes)
- Implementar apenas 3-5 cenarios basicos
- Focar em happy path + 1-2 edge cases

---

## TIMELINE RECOMENDADO

**Hoje (14/Nov):**
- Decisao sobre repositorios
- Inicio implementacao BDD

**15-16/Nov:**
- Finalizacao BDD
- Inicio implementacao CD

**17-19/Nov:**
- Implementacao completa de CD (4 workflows)
- Testes de deploy

**20/Nov:**
- Branch protection
- Atualizacao README
- Adicionar badges

**21-22/Nov:**
- Gravacao e edicao do video
- Preparacao documento de entrega

**23/Nov:**
- Revisao final
- Submissao

---

## OBSERVACOES IMPORTANTES

### Requisitos Fase 4 (Tech Challenge PDF)

**Microservicos:**
- Minimo 3 servicos ✅ (implementados 4)
- Banco SQL + NoSQL ✅ (MySQL + MongoDB)
- Comunicacao entre servicos ✅ (REST + RabbitMQ)
- Isolamento de dados ✅

**Testes:**
- Testes unitarios ✅
- 80% cobertura ✅ (86.5% media)
- BDD em ao menos um caminho ✅ (19 cenários implementados)

**Repositorios e CI/CD:**
- Branches protegidas ❌ PENDENTE
- PR com validacao de build e qualidade ✅
- SonarCloud 70%+ coverage ✅
- Deploy automatico no merge ❌ PENDENTE

**Entrega:**
- Video demonstracao ❌ PENDENTE
- Links para repositorios ✅ (parcial)
- Evidencias de cobertura ✅ (parcial)
- Usuario soat-architecture adicionado ❌ PENDENTE

---

## LINKS UTEIS

- **Repositorio:** https://github.com/andersonfer/lanchonete-app
- **SonarCloud Org:** https://sonarcloud.io/organizations/andersonfer
- **SonarCloud Projects:**
  - Clientes: https://sonarcloud.io/project/overview?id=andersonfer_lanchonete-clientes
  - Pedidos: https://sonarcloud.io/project/overview?id=andersonfer_lanchonete-pedidos
  - Cozinha: https://sonarcloud.io/project/overview?id=andersonfer_lanchonete-cozinha
  - Pagamento: https://sonarcloud.io/project/overview?id=andersonfer_lanchonete-pagamento

---

**Ultima Atualizacao:** 2025-11-18 08:30
**Responsavel:** Anderson (Scrum Master + Product Owner)
**Status Geral:** 66.7% Concluido - Todos os componentes tecnicos finalizados!
**Proxima Acao:** Documentacao (badges, evidencias) + Video de demonstracao
**Prazo Estimado:** 2-3 dias para conclusao total (apenas artefatos de entrega restantes)

---

## RESUMO EXECUTIVO (2025-11-18)

### O que foi alcancado na Fase 4

**INFRAESTRUTURA:**
- ✅ 4 microservicos independentes (Clientes, Pedidos, Cozinha, Pagamento)
- ✅ 3 bancos MySQL + 1 MongoDB (isolamento completo)
- ✅ Comunicacao REST (Feign) + RabbitMQ (eventos asincronos)
- ✅ Deploy em AWS EKS com infraestrutura Terraform

**QUALIDADE:**
- ✅ 86.5% cobertura media de testes (supera meta de 80%)
- ✅ 393 testes automatizados (374 unitarios + 19 BDD)
- ✅ 4 Quality Gates SonarCloud passando (> 70% coverage)
- ✅ 0 vulnerabilidades criticas (Log Injection corrigida)

**CI/CD:**
- ✅ 4 pipelines CI com SonarCloud integrado
- ✅ 4 pipelines CD com SHA versioning
- ✅ Deploy automatico no merge para main
- ✅ Smoke tests validando health/readiness/liveness
- ✅ ALBs provisionados e respondendo

**PROXIMOS PASSOS:**
1. Adicionar badges e evidencias no README (2-3h)
2. Configurar branch protection formal (30min)
3. Criar documento de decisao de monorepo (1h)
4. Gravar video de demonstracao (4-6h)
5. Adicionar usuario soat-architecture (5min)
6. Submeter projeto final (1h)

**TOTAL RESTANTE:** 8-11 horas de trabalho (2-3 dias)

---

## SESSÃO DE HOJE (2025-11-17)

### Resumo do Trabalho Realizado

**Objetivo:** Implementar pipelines de CD para os 4 microserviços

**Entregas:**
1. ✅ Pipeline CD - Clientes (cd-clientes.yml)
   - 12 passos implementados
   - Execução: 1m27s
   - Deploy validado no EKS
   - ALB: lanchonete-clientes-alb-199660999.us-east-1.elb.amazonaws.com

2. ✅ Pipeline CD - Pagamento (cd-pagamento.yml)
   - 12 passos implementados
   - Execução: 49s (o mais rápido!)
   - Deploy validado no EKS
   - ALB: lanchonete-pagamento-alb-1162736294.us-east-1.elb.amazonaws.com

3. ✅ Pipeline CD - Cozinha (cd-cozinha.yml)
   - 12 passos implementados
   - Execução: 1m1s
   - Deploy validado no EKS
   - ALB: lanchonete-cozinha-alb-1426986788.us-east-1.elb.amazonaws.com

4. ✅ Pipeline CD - Pedidos (cd-pedidos.yml)
   - 12 passos implementados
   - Execução: 51s
   - Deploy validado no EKS
   - ALB: lanchonete-pedidos-alb-2100039014.us-east-1.elb.amazonaws.com

**Características dos Pipelines:**
- Versão PoC simplificada (apenas tag :latest)
- Build com Maven (skip tests no CD)
- Push para Amazon ECR
- Deploy automático no EKS via kubectl
- Smoke tests (health, readiness, liveness)
- Verificação de ALB (não-bloqueante)
- Resumo de deploy no GitHub Actions

**Métricas:**
- 4 workflows criados e testados
- 4 PRs mergeados com sucesso
- 100% dos deploys bem-sucedidos
- Tempo médio de execução: 1min2s
- Todos os ALBs respondendo corretamente

**Próxima Tarefa:**
Teste integrado CI+CD para validar fluxo completo (PR → CI → Merge → CD → Deploy)

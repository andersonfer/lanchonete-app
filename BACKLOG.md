# BACKLOG - Sistema Lanchonete (Fase 4)

**Projeto:** Sistema de Lanchonete - Arquitetura de Microserviços
**Branch Atual:** main
**Última Atualizacao:** 2025-11-14 18:30
**Fase Atual:** Fase 4 - CI/CD e Qualidade de Codigo

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
- **Testes Unitarios:** 100% CONCLUIDO (80%+ cobertura em todos)
- **CI com SonarCloud:** 100% CONCLUIDO
- **CD para AWS EKS:** 0% PENDENTE
- **Testes BDD:** 0% PENDENTE
- **Repositorios Separados:** 0% PENDENTE

---

## SCRUM MASTER SESSION REPORT

### Recent Progress Analysis (Ultimas 72h)

**Commits Analisados (desde 2025-11-11):**
- `72bb829` - Merge PR #7: Refatoracao completa para microservicos
- `1e0ea5b` - Criacao do CI de Pagamento
- `602d824` - Refatoracao PedidoCozinhaResponse para record
- `8bacc67` - Criacao do CI da Cozinha
- `1b164ab` - Remocao dos pipelines antigos (ci-app.yml, cd-app.yml)
- `8ed679e` - Remocao de testes duplicados
- `031fca7` - Testes unitarios do servico de Pedidos
- `03812bf` - Criacao do CI de Pedidos
- `305e6e4` - Criacao do workflow de CI de Clientes

**Principais Mudancas:**
- 4 workflows de CI implementados (.github/workflows/ci-*.yml)
- Todos os servicos com configuracao SonarCloud no pom.xml
- Remocao dos pipelines monoliticos (ci-app.yml e cd-app.yml)
- Adicao de testes unitarios no servico de Pedidos
- PR #7 mergeado para main com todas as mudancas

### Completed Items (Desde Ultima Sessao)

**FASE 4 - ENTREGAVEL 1: Refatoracao para Microservicos** ✅ CONCLUIDO
- ✅ Separacao em 4 microservicos (Clientes, Pedidos, Cozinha, Pagamento)
- ✅ Banco MySQL para Clientes, Pedidos e Cozinha
- ✅ Banco MongoDB para Pagamento (NoSQL obrigatorio)
- ✅ Comunicacao via REST (Feign Client)
- ✅ Comunicacao via RabbitMQ (mensageria assincrona)
- ✅ Isolamento de bancos de dados (sem acesso cross-service)

**FASE 4 - ENTREGAVEL 2a: Testes Unitarios com 80% Cobertura** ✅ CONCLUIDO
- ✅ Clientes: 85% cobertura (superou meta)
- ✅ Pedidos: 83% cobertura (superou meta)
- ✅ Cozinha: 84% cobertura (superou meta)
- ✅ Pagamento: 94% cobertura (superou meta)

**FASE 4 - ENTREGAVEL 3b: CI com SonarCloud (70% coverage minimo)** ✅ CONCLUIDO
- ✅ CI Clientes: Testes + SonarCloud + Quality Gate
- ✅ CI Pedidos: Testes + SonarCloud + Quality Gate
- ✅ CI Cozinha: Testes + SonarCloud + Quality Gate
- ✅ CI Pagamento: Testes + SonarCloud + Quality Gate
- ✅ Path filters configurados (services/{servico}/**)
- ✅ Trigger em Pull Requests para branch main
- ✅ Todos os CIs executados com sucesso no PR #7

### Current Status

**Total de Entregaveis Fase 4:** 8 principais
- **Concluidos:** 3/8 (37.5%)
- **Em Progresso:** 0/8
- **Pendentes:** 5/8 (62.5%)

**Quality Metrics:**
- Cobertura Media de Testes: 86.5% (meta: 80%)
- Pipelines CI Funcionando: 4/4 (100%)
- Pipelines CD Funcionando: 0/4 (0%)
- SonarCloud Projects: 4/4 configurados

**GitHub Actions Status (Ultimo PR #7):**
- ✅ CI - Clientes: SUCCESS
- ✅ CI - Pedidos: SUCCESS
- ✅ CI - Cozinha: SUCCESS
- ✅ CI - Pagamento: SUCCESS

### Blockers & Risks

**BLOQUEADOR CRITICO:**
- ❌ **Repositorios nao estao separados** (Entregavel 3a)
  - Requisito: "Seus repositorios devem ser separados para cada aplicacao"
  - Situacao Atual: Monorepo unico com todos os servicos
  - Impacto: Viola requisito obrigatorio da Fase 4
  - Acao Necessaria: Criar 4 repositorios separados + configurar CI/CD em cada

**PENDENCIAS CRITICAS:**
- ❌ Pipelines de CD nao implementados (Entregavel 3c)
- ❌ Testes BDD nao implementados (Entregavel 2a)
- ❌ Branch protection nao configurada (Entregavel 3a)

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

#### 2b. Testes BDD (Ao menos um caminho) ❌ PENDENTE

**Status:** NAO INICIADO

**Requisitos:**
- [ ] Implementar BDD em ao menos um servico
- [ ] Usar Cucumber ou framework similar
- [ ] Escrever features em Gherkin
- [ ] Integrar com CI/CD

**Estimativa:** 3-4 dias
**Prioridade:** P0 - CRITICA (obrigatorio para entrega)

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

#### 3c. CD para Deploy Automatico ❌ PENDENTE

**Requisitos:**
- [ ] Deploy automatico no merge para main
- [ ] Todos os microservicos devem ser deployados
- [ ] Validacao de testes antes do deploy

**Status:** NAO INICIADO

**Tarefas:**
- [ ] Criar cd-clientes.yml
- [ ] Criar cd-pedidos.yml
- [ ] Criar cd-cozinha.yml
- [ ] Criar cd-pagamento.yml
- [ ] Configurar AWS credentials nos secrets
- [ ] Testar deploy em ambiente staging
- [ ] Validar deploy em producao (EKS)

**Estimativa:** 5-6 dias
**Prioridade:** P0 - CRITICA (obrigatorio para entrega)

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

### SPRINT ATUAL: Finalizacao Fase 4 (6-8 dias restantes)

**DIA 1-2: DECISAO ARQUITETURAL + BDD**
1. **[DECISAO]** Definir estrategia de repositorios
   - Manter monorepo OU separar repositorios
   - Documentar decisao e justificativa
   - Atualizar BACKLOG.md com a escolha

2. **[BDD]** Implementar testes BDD em 1 servico
   - Escolher servico mais simples (Clientes ou Pagamento)
   - Adicionar dependencias Cucumber
   - Escrever 3-5 features em Gherkin
   - Implementar step definitions
   - Integrar com CI
   - **Estimativa:** 8-10 horas

**DIA 3-5: PIPELINES DE CD**
3. **[CD]** Implementar workflows de CD
   - Criar cd-clientes.yml
   - Criar cd-pedidos.yml
   - Criar cd-cozinha.yml
   - Criar cd-pagamento.yml
   - Configurar secrets AWS
   - Testar deploy automatico
   - **Estimativa:** 3-4 dias

**DIA 6: BRANCH PROTECTION + DOCUMENTACAO**
4. **[REPO]** Configurar branch protection
   - Proteger branch main
   - Bloquear commits diretos
   - Requer PR reviews
   - Requer CI passando

5. **[DOCS]** Atualizar README com evidencias
   - Adicionar badges SonarCloud (4 badges)
   - Adicionar badges CI/CD (8 badges)
   - Screenshots de cobertura
   - Links para SonarCloud projects
   - Adicionar usuario soat-architecture

**DIA 7-8: VIDEO + ENTREGA**
6. **[VIDEO]** Gravar video de demonstracao
   - Roteiro: Arquitetura → Testes → CI/CD → Deploy
   - Mostrar aplicacao funcionando
   - Mostrar checks verdes
   - Edicao e upload
   - **Estimativa:** 4-6 horas

7. **[ENTREGA]** Preparar artefatos finais
   - Documento com nomes + Discord IDs
   - Links para repositorios
   - Link do video
   - Validacao final

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
- [ ] 2b. Ao menos um caminho com BDD (PENDENTE)
- [ ] 3a. Repositorios separados OU justificativa (DECISAO PENDENTE)
- [x] 3b. CI com SonarCloud 70%+ coverage (4 workflows funcionando)
- [ ] 3c. CD com deploy automatico (PENDENTE)
- [ ] 4a. Video demonstracao (PENDENTE)
- [ ] 4b. Links e evidencias no README (PARCIAL)

**PERCENTUAL DE CONCLUSAO:** 50% (4/8 entregaveis)

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
- CD Clientes: ❌ NAO IMPLEMENTADO
- CD Pedidos: ❌ NAO IMPLEMENTADO
- CD Cozinha: ❌ NAO IMPLEMENTADO
- CD Pagamento: ❌ NAO IMPLEMENTADO

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
- BDD em ao menos um caminho ❌ PENDENTE

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

**Ultima Atualizacao:** 2025-11-14 18:30
**Responsavel:** Anderson
**Status Geral:** 50% Concluido - CI implementado, CD e BDD pendentes
**Proxima Acao:** Implementar BDD no servico de Clientes
**Prazo Estimado:** 9 dias para conclusao total

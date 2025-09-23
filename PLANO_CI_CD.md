# 🚀 Plano de Implementação CI/CD - Lanchonete App

## 📋 Arquitetura Definida

### **Requisitos Capturados:**
- **Triggers**: Pull Requests → main (branch protegida)
- **Strategy**: GitHub Flow
- **Scope**: Apenas produção
- **Secrets**: GitHub Repository Secrets (AWS credentials)
- **Tests**: Unit tests + smoke tests por pipeline
- **Failure Policy**: Pipeline falha se tests falham
- **Notifications**: Logs do GitHub Actions
- **Performance**: Pipelines rápidos
- **Goal**: Demonstração POC
- **Usage**: Apenas você, em várias máquinas

## 🏗️ Estrutura dos 4 Pipelines

### **Pipeline 1 - Base (infra-base)**
- **Responsabilidade**: Fundação da infraestrutura
- **Componentes**:
  - `infra/backend/` - S3 + DynamoDB
  - `infra/ecr/` - Repositórios Docker
  - `infra/database/` - RDS MySQL
- **Dados exportados**:
  - ECR URLs (Terraform State)
  - RDS Endpoint (Terraform State)
  - RDS Password (Parameter Store)

### **Pipeline 2 - Kubernetes (infra-k8s)**
- **Responsabilidade**: Plataforma de containerização
- **Componentes**:
  - `infra/kubernetes/` - EKS Cluster
  - `infra/ingress/` - ALB Controller
- **Dependência**: Pipeline Base
- **Dados exportados**:
  - Cluster Name (Terraform State)

### **Pipeline 3 - Autenticação (infra-auth)**
- **Responsabilidade**: Sistema de autenticação
- **Componentes**:
  - `infra/lambda/` - Build da Lambda Java
  - `infra/auth/` - Cognito User Pool
  - `infra/api-gateway/` - API Gateway + Authorizer
- **Dependência**: Pipeline Base
- **Dados exportados**:
  - API Gateway URL (Terraform State)

### **Pipeline 4 - Aplicação (app-deploy)**
- **Responsabilidade**: Deploy das aplicações + testes
- **Componentes**:
  - Unit tests Java
  - Build & Push Docker images
  - Deploy Kubernetes
  - Testes E2E integrados
- **Dependências**: Pipeline Base, Pipeline K8s, Pipeline Auth

## 🔄 Ordem de Execução

```
PR → main triggers:

1. Pipeline Base (obrigatório primeiro)
   ↓
2. Pipeline K8s + Pipeline Auth (paralelos)
   ↓
3. Pipeline App (aguarda ambos completarem)
```

## 🔐 Estratégia de Secrets

### **GitHub Repository Secrets:**
```yaml
AWS_ACCESS_KEY_ID: [suas credenciais AWS]
AWS_SECRET_ACCESS_KEY: [suas credenciais AWS]
AWS_DEFAULT_REGION: us-east-1
```

### **Uso nos workflows:**
```yaml
- name: Configure AWS credentials
  uses: aws-actions/configure-aws-credentials@v2
  with:
    aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
    aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
    aws-region: us-east-1
```

## 🧪 Testes por Pipeline

### **Pipeline Base - Smoke Tests:**
- `terraform validate` (sintaxe)
- `terraform plan` (preview)
- `aws sts get-caller-identity` (autenticação)
- MySQL connection test (pós-RDS)

### **Pipeline K8s - Smoke Tests:**
- `kubectl get nodes` (cluster funcionando)
- `kubectl get pods -n kube-system` (ALB controller)

### **Pipeline Auth - Smoke Tests:**
- `curl` API Gateway health endpoint
- Token generation test básico

### **Pipeline App - Testes Completos:**
- `mvn test` (unit tests Java)
- `docker build` validation
- `kubectl get pods` (deploy successful)
- `curl` health endpoints das aplicações
- **Cliente anônimo E2E test** (smoke test integrado)

## 📋 Plano de Implementação - 3 Fases

### **🔧 FASE 1: Setup Inicial (30 min)**

#### 1. Branch Protection Setup
```yaml
Settings → Branches → Add rule:
- Branch name pattern: main
- Require pull request reviews: false (é só você)
- Require status checks: true
- Required status checks:
  - pipeline-base
  - pipeline-k8s
  - pipeline-auth
  - pipeline-app
```

#### 2. Repository Secrets
```yaml
Settings → Secrets and variables → Actions → New repository secret:
- AWS_ACCESS_KEY_ID: [sua access key]
- AWS_SECRET_ACCESS_KEY: [sua secret key]
- AWS_DEFAULT_REGION: us-east-1
```

#### 3. Criar estrutura de workflows
```bash
mkdir -p .github/workflows
touch .github/workflows/pipeline-base.yml
touch .github/workflows/pipeline-k8s.yml
touch .github/workflows/pipeline-auth.yml
touch .github/workflows/pipeline-app.yml
```

### **⚙️ FASE 2: Implementação dos Workflows (2-3 horas)**

#### Ordem de implementação:

**1. Pipeline Base (40 min)**
```yaml
# .github/workflows/pipeline-base.yml
name: "Pipeline 1 - Base Infrastructure"
on:
  pull_request:
    branches: [main]
  workflow_dispatch:

jobs:
  base-infra:
    runs-on: ubuntu-latest
    steps:
      # Configure AWS credentials
      # Terraform backend
      # Terraform ecr
      # Terraform database
      # Smoke tests
```

**2. Pipeline K8s (30 min)**
```yaml
# .github/workflows/pipeline-k8s.yml
name: "Pipeline 2 - Kubernetes"
needs: [base-infra] # Aguarda Pipeline Base
# Terraform kubernetes + ingress
# kubectl smoke tests
```

**3. Pipeline Auth (30 min)**
```yaml
# .github/workflows/pipeline-auth.yml
name: "Pipeline 3 - Authentication"
needs: [base-infra] # Aguarda Pipeline Base
# Lambda build + Terraform auth + api-gateway
# API Gateway smoke tests
```

**4. Pipeline App (45 min)**
```yaml
# .github/workflows/pipeline-app.yml
name: "Pipeline 4 - Application"
needs: [k8s-infra, auth-infra] # Aguarda ambos
# Unit tests + Docker build + K8s deploy + E2E tests
```

### **🧪 FASE 3: Validação e Refinamento (1 hora)**

#### 1. Teste completo via PR
```bash
# Criar branch de teste
git checkout -b feature/test-ci-cd

# Fazer mudança simples (ex: comentário no README)
echo "# Teste CI/CD" >> README.md
git add . && git commit -m "test: CI/CD pipeline validation"
git push origin feature/test-ci-cd

# Abrir PR via GitHub UI
# Verificar execução sequencial dos 4 pipelines
```

#### 2. Ajustes finais
- Configurar timeouts apropriados (Base: 15min, K8s: 25min, Auth: 10min, App: 15min)
- Error handling e cleanup em caso de falha
- Logs informativos para debugging
- Documentação inline nos workflows

#### 3. Demo preparation
- README com badges dos workflows
- Workflow history limpa (sem execuções de teste)
- Screenshots dos pipelines para apresentação

## 📊 Comunicação Entre Pipelines

### **Dados Compartilhados:**

```yaml
Terraform State (S3) → Fonte primária:
  - ECR URLs
  - RDS Endpoint
  - Cluster Name
  - API Gateway URL

Parameter Store → Apenas:
  - RDS Password (SecureString)

kubectl → Conforme necessário:
  - ALB URLs (para testes)
```

### **Implementação no workflow:**
```yaml
# Ler do Terraform State
- name: Get ECR URL
  run: |
    cd infra/ecr
    ECR_URL=$(terraform output -raw ecr_autoatendimento_url)
    echo "ECR_URL=$ECR_URL" >> $GITHUB_ENV

# Ler do Parameter Store
- name: Get RDS Password
  run: |
    RDS_PASSWORD=$(aws ssm get-parameter \
      --name "/lanchonete/rds/password" \
      --with-decryption \
      --query 'Parameter.Value' \
      --output text)
    echo "::add-mask::$RDS_PASSWORD"
    echo "RDS_PASSWORD=$RDS_PASSWORD" >> $GITHUB_ENV
```

## 🚨 Casos de Falha e Recuperação

### **Pipeline Base falha:**
- **Impacto**: Crítico - outros pipelines não rodam
- **Ação**: Cleanup manual no AWS Console + debug + restart

### **Pipeline K8s falha:**
- **Impacto**: App pipeline não roda
- **Ação**: Pipeline Auth pode continuar, depois fix K8s

### **Pipeline Auth falha:**
- **Impacto**: Testes E2E vão falhar
- **Ação**: App pipeline roda mas testes quebram

### **Pipeline App falha:**
- **Impacto**: Sistema não funcional
- **Ação**: Infra OK, debug aplicação

### **Estratégia para POC:**
**"Falhou = Destroi tudo e recomeça"** - Mais rápido que debug complexo

## ⚡ Tempos Estimados

```yaml
Pipeline Base: ~8 min (RDS: 6min)
Pipeline K8s: ~18 min (EKS: 16min)
Pipeline Auth: ~5 min (Lambda build: 2min)
Pipeline App: ~8 min (Docker build: 3min, Deploy: 3min, Tests: 2min)

Total sequencial: ~35-40 min
Total paralelo (K8s+Auth): ~25-30 min
```

## 🎯 Objetivo Final

**Resultado esperado:**
- CI/CD completamente automatizado via PR
- Demonstração robusta para POC
- Deploy zero-touch da infraestrutura completa
- Validação automática de funcionamento
- Logs detalhados para troubleshooting
- Reutilizável em diferentes máquinas

**Success criteria:**
- ✅ PR → main dispara todos os 4 pipelines
- ✅ Execução sequencial respeitando dependências
- ✅ Falha de qualquer pipeline bloqueia merge
- ✅ Smoke tests validam cada componente
- ✅ E2E test confirma sistema funcionando
- ✅ Logs permitem debug rápido de falhas

---

## ✅ Status de Implementação (22/09/2025)

### **FASE 1: Setup Inicial** ✅ CONCLUÍDA
- ✅ Estrutura de workflows criada
- ✅ Secrets do GitHub configurados (requer atualização por sessão)
- ⏳ Branch Protection Rules (configurar via UI do GitHub)

### **FASE 2: Implementação dos Workflows** ✅ CONCLUÍDA
- ✅ **Pipeline 1 - Base** (`pipeline-base.yml`) - Implementado
- ✅ **Pipeline 2 - Kubernetes** (`pipeline-k8s.yml`) - Implementado
- ✅ **Pipeline 3 - Authentication** (`pipeline-auth.yml`) - Implementado
- ✅ **Pipeline 4 - Application** (`pipeline-app.yml`) - Implementado

### **FASE 3: Validação e Refinamento** 🔄 EM ANDAMENTO
- ⏳ Teste completo via PR
- ⏳ Ajustes finais baseados em execução real
- ⏳ Demo preparation

**💡 Próximos passos:**
1. ✅ ~~Executar FASE 1 (Setup)~~
2. ✅ ~~Implementar workflows na FASE 2~~
3. 🔄 **Validar tudo na FASE 3** ← ESTAMOS AQUI
   - Configurar Branch Protection Rules no GitHub
   - Criar PR de teste para validação
   - Ajustar baseado nos resultados
4. Demonstrar POC funcionando! 🚀
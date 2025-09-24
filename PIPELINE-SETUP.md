# 🚀 Setup de Pipelines CI/CD - Lanchonete App

## 📋 Status dos Pipelines

### ✅ Pipeline 1: Database (RDS)
- **Arquivo**: `.github/workflows/01-infra-database.yml`
- **Trigger**: Changes em `infra/01-database/**`
- **Tempo**: ~10-15 minutos
- **Outputs**: RDS_ENDPOINT, DATABASE_USERNAME, JDBC_URL

### 🔄 Pipeline 2: Kubernetes (Em desenvolvimento)
- **Arquivo**: `.github/workflows/02-infra-kubernetes.yml`
- **Trigger**: Changes em `infra/02-kubernetes/**` ou `k8s/global/**`

### 🔄 Pipeline 3: Auth (Em desenvolvimento)
- **Arquivo**: `.github/workflows/03-infra-auth.yml`
- **Trigger**: Changes em `infra/03-auth/**`

### 🔄 Pipeline 4: Application (Em desenvolvimento)
- **Arquivo**: `.github/workflows/04-deploy-apps.yml`
- **Trigger**: Changes em `app/**` ou `k8s/autoatendimento/**` ou `k8s/pagamento/**`

---

## 🤖 Workflow de Teste Autônomo

### **Claude fará automaticamente**:

#### **Pré-requisitos (usuário)**:
1. ✅ Configurar secrets do GitHub:
   - `AWS_ACCESS_KEY_ID`
   - `AWS_SECRET_ACCESS_KEY`
   - `AWS_SESSION_TOKEN`
2. ✅ Criar backend S3 manualmente (`terraform apply` em `infra/backend/`)
3. ✅ Configurar branch protection na `main`

#### **Teste Automático (Claude)**:
1. 🤖 Criar branch de teste: `test-pipeline-[nome]`
2. 🤖 Fazer alteração mínima no módulo
3. 🤖 Commit e push: `git add . && git commit && git push`
4. 🤖 Criar Pull Request via `gh pr create`
5. 🤖 Monitorar pipeline com `gh run watch`
6. 🤖 Analisar logs se falhar: `gh run view --log`
7. 🤖 Corrigir erros e fazer novos commits se necessário
8. 🤖 Mergear PR se sucesso: `gh pr merge`
9. 🤖 Limpar branch: `git branch -D test-pipeline-[nome]`

#### **Verificações Automáticas**:
- ✅ Pipeline executou sem timeout
- ✅ Health check passou (RDS "available")
- ✅ Terraform outputs disponíveis
- ✅ Limpeza automática da branch

#### **Troubleshooting Autônomo**:
- 🔍 Analisar falhas via GitHub CLI
- 🔧 Ajustar configurações se necessário
- 🔄 Re-executar testes até sucesso
- 📝 Documentar correções aplicadas

---

## 📁 Nova Estrutura do Projeto

```
lanchonete-app/
├── .github/workflows/           # Pipelines CI/CD
│   └── 01-infra-database.yml   ✅
├── infra/
│   ├── 01-database/            ✅ (RDS)
│   ├── 02-kubernetes/          🔄 (EKS + ECR + ALB)
│   ├── 03-auth/               🔄 (Cognito + Lambda + API Gateway)
│   └── backend/               ✅ (S3 + DynamoDB - manual)
├── k8s/
│   ├── global/                🔄 (Namespaces, etc)
│   ├── autoatendimento/       🔄 (Manifests + Dockerfile)
│   └── pagamento/             🔄 (Manifests + Dockerfile)
└── app/
    ├── autoatendimento/       ✅ (Código Java)
    └── pagamento/             ✅ (Código Java)
```

---

## 🎯 Próximos Passos

1. **Testar Pipeline 1**: Validar Database deployment
2. **Criar Pipeline 2**: Kubernetes (EKS + ECR + manifests globais)
3. **Criar Pipeline 3**: Auth (Lambda + Cognito + API Gateway)
4. **Criar Pipeline 4**: Applications (Build + Deploy)
5. **Validação end-to-end**: Todos os pipelines funcionando

**Implementação incremental**: Um pipeline por vez, testando antes de seguir para o próximo.

---

## 🔧 Comandos de Teste que Claude Usará

### **Ciclo de Teste Completo**:

```bash
# 1. Criar branch de teste
git checkout -b test-pipeline-database
git push -u origin test-pipeline-database

# 2. Fazer alteração mínima
echo "# Pipeline test $(date)" >> infra/01-database/main.tf
git add .
git commit -m "test: pipeline database - $(date +%H%M)"
git push

# 3. Criar PR e monitorar
gh pr create --title "test: Pipeline Database" --body "Teste automático do pipeline de database"
gh run watch  # Monitorar em tempo real

# 4. Analisar resultado
gh run list --limit 1
gh run view --log  # Se falhar

# 5. Mergear se sucesso
gh pr merge --auto --squash

# 6. Limpeza
git checkout main
git pull origin main
git branch -D test-pipeline-database
```

### **Verificações de Sucesso**:
- ✅ `gh run list` mostra status "completed"
- ✅ `gh pr status` mostra checks passando
- ✅ RDS visível no console AWS
- ✅ Terraform state no S3 atualizado

### **Comandos de Debug**:
```bash
# Ver logs detalhados
gh run view [RUN_ID] --log-failed

# Status do PR
gh pr status

# Re-executar workflow se necessário
gh workflow run "01 - Deploy Database (RDS)"
```
# 🚀 Guia de Deploy Lanchonete - Infraestrutura Completa

## Visão Geral

Este guia permite subir a infraestrutura completa do projeto Lanchonete do zero, resolvendo definitivamente o "dependency hell".

### Arquitetura Final
```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   DATABASE      │    │   KUBERNETES     │    │     LAMBDA      │
│                 │    │                  │    │                 │
│ • RDS MySQL     │    │ • EKS Cluster    │    │ • API Gateway   │
│ • Security Grps │───▶│ • ALB/NLB        │◀───│ • Auth/JWT      │
│ • VPC Endpoint  │    │ • VPC Link       │    │ • Business APIs │
│ • Migrations    │    │ • Node Groups    │    │ • Lambda Layers │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

## ⚠️ Pré-requisitos OBRIGATÓRIOS

### 1. Scripts Auxiliares
- ✅ `aws-cleanup.sh` - Script de limpeza robusto
- ✅ `run-migrations.sh` - Script de migrations externo
- ✅ `aws-inventory.sh` - Script de inventário

### 2. Arquivos JARs Buildados
```bash
# SEMPRE buildar JARs ANTES do terraform!
cd lambda-connectivity-test && mvn clean package
cd ../lambda-auth && mvn clean package
```

### 3. Terraform tfvars
```bash
# Verificar se existe: terraform/shared/terraform.tfvars
cat terraform/shared/terraform.tfvars
```

### 4. Backend S3
```bash
# Backend deve existir (NÃO deletar!)
aws s3 ls s3://lanchonete-tfstate
```

## 🧹 Passo 1: Limpeza Prévia (SEMPRE)

### 1.1 Executar Limpeza Completa
```bash
cd /home/ubuntu/lanchonete-app
./aws-cleanup.sh
```

### 1.2 Verificar Estado Limpo
```bash
./aws-inventory.sh
```

**Estado esperado:** Apenas recursos padrão AWS + bucket backend

### 1.3 Verificar Pré-requisitos
```bash
# Verificar JARs
ls -la lambda-*/target/*.jar

# Verificar tfvars
cat terraform/shared/terraform.tfvars

# Verificar backend
aws s3 ls s3://lanchonete-tfstate
```

## 🗄️ Passo 2: Deploy Database Module

### 2.1 Inicializar Database Module
```bash
cd terraform/database
terraform init
```

### 2.2 Planejar Database Module
```bash
terraform plan -var-file="../shared/terraform.tfvars"
```

**Verificar se o plan mostra:**
- ✅ RDS MySQL instance
- ✅ Security Groups (Lambda + RDS)  
- ✅ Lambda functions (migration + connectivity-test)
- ✅ S3 migrations bucket **com nome fixo** (`lanchonete-migrations`)
- ✅ S3 objects (schema.sql + seed.sql)
- ✅ VPC endpoint para S3
- ✅ DB Subnet Group

**Nota:** Se aparecer "destroy random_string.bucket_suffix" é **normal e esperado**.

### 2.3 Aplicar Database Module
```bash
terraform apply -var-file="../shared/terraform.tfvars"
```

**Confirmar com:** `yes`

**Aguardar:** ~5-8 minutos (RDS demora para criar)

### 2.4 Verificar Outputs Database
```bash
terraform output
```

**Esperado:**
```
database_name = "lanchonete"
lambda_security_group_id = "sg-xxxxxxxxx"
migrations_bucket = "lanchonete-migrations"
rds_endpoint = "lanchonete-mysql.xxxxx.rds.amazonaws.com:3306"
rds_port = 3306
rds_security_group_id = "sg-xxxxxxxxx"
```

## 🔄 Passo 3: Executar Migrations

### 3.1 Voltar ao Diretório Raiz
```bash
cd /home/ubuntu/lanchonete-app
```

### 3.2 Executar Migrations
```bash
./run-migrations.sh
```

**Aguardar:** ~10-30 segundos

**Verificar logs esperados:**
```
✅ Pré-requisitos verificados
✅ Outputs obtidos
✅ Lambda lanchonete-migration encontrada
✅ RDS lanchonete-mysql está disponível
✅ Migrations executadas com sucesso!
```

## ☸️ Passo 4: Deploy Kubernetes Module

### 4.1 Inicializar Kubernetes Module
```bash
cd terraform/kubernetes
terraform init
```

### 4.2 Planejar Kubernetes Module
```bash
terraform plan -var-file="../shared/terraform.tfvars"
```

**Verificar se o plan mostra:**
- ✅ EKS Cluster
- ✅ EKS Node Groups
- ✅ Application Load Balancer (ALB)
- ✅ Network Load Balancer (NLB)
- ✅ VPC Link para API Gateway
- ✅ IAM roles e políticas

### 4.3 Aplicar Kubernetes Module
```bash
terraform apply -var-file="../shared/terraform.tfvars"
```

**Confirmar com:** `yes`

**Aguardar:** ~15-20 minutos (EKS é lento para criar)

### 4.4 Verificar Outputs Kubernetes
```bash
terraform output
```

## 🔗 Passo 5: Deploy Lambda Module

### 5.1 Inicializar Lambda Module
```bash
cd terraform/lambda
terraform init
```

### 5.2 Planejar Lambda Module
```bash
terraform plan -var-file="../shared/terraform.tfvars"
```

**Verificar se o plan mostra:**
- ✅ API Gateway REST API
- ✅ Lambda functions (auth, business logic)
- ✅ Lambda authorizers (JWT)
- ✅ API Gateway integrations
- ✅ VPC Link connections

### 5.3 Aplicar Lambda Module
```bash
terraform apply -var-file="../shared/terraform.tfvars"
```

**Confirmar com:** `yes`

**Aguardar:** ~5-10 minutos

### 5.4 Verificar Outputs Lambda
```bash
terraform output
```

## ✅ Passo 6: Verificação Final

### 6.1 Inventário Completo
```bash
cd /home/ubuntu/lanchonete-app
./aws-inventory.sh
```

**Devem existir:**
- ✅ RDS Instance (available)
- ✅ EKS Cluster (active)
- ✅ Lambda Functions (ativas)
- ✅ ALB/NLB (active)
- ✅ API Gateway (deployed)
- ✅ Security Groups (configurados)

### 6.2 Teste de conectividade
```bash
# Testar Lambda de conectividade
aws lambda invoke --function-name lanchonete-connectivity-test response.json
cat response.json

# Verificar bucket S3 migrations (nome fixo)
aws s3 ls s3://lanchonete-migrations/
```

### 6.3 Configurar kubectl (se necessário)
```bash
aws eks update-kubeconfig --region us-east-1 --name lanchonete-cluster
kubectl get nodes
```

## 🚨 Troubleshooting

### Erro: "Bucket lanchonete-migrations já existe"
```bash
# Limpar recursos órfãos
./aws-cleanup.sh

# Ou deletar manualmente
aws s3 rb s3://lanchonete-migrations --force
```

### Erro: "DB Subnet Group já existe"  
```bash
# Verificar recursos órfãos
./aws-inventory.sh
# Executar limpeza
./aws-cleanup.sh
```

### Erro: "Lambda JAR não encontrado"
```bash
# Rebuildar JARs
cd lambda-connectivity-test && mvn clean package
cd ../lambda-auth && mvn clean package
```

### Erro: "VPC Link dependency"
```bash
# Seguir ordem EXATA dos módulos
# 1. database
# 2. kubernetes  
# 3. lambda
```

### Timeout no Terraform
```bash
# EKS demora ~15-20min
# RDS demora ~5-8min
# Aguardar pacientemente
```

## 🔄 Comandos de Emergência

### Limpeza Completa (Começar do Zero)
```bash
cd /home/ubuntu/lanchonete-app
./aws-cleanup.sh
./aws-inventory.sh  # Verificar limpeza
# Depois seguir o guia do início
```

### Rebuild JARs
```bash
cd lambda-connectivity-test
mvn clean package
cd ../lambda-auth  
mvn clean package
cd ..
```

### Verificar Estado de Cada Módulo
```bash
# Database
cd terraform/database && terraform show

# Kubernetes
cd terraform/kubernetes && terraform show

# Lambda
cd terraform/lambda && terraform show
```

## 📝 Checklist Final

- [ ] Scripts auxiliares criados e executáveis
- [ ] JARs buildados recentemente  
- [ ] Backend S3 existente e acessível
- [ ] Limpeza completa executada
- [ ] Database module deployado com sucesso
- [ ] Migrations executadas com sucesso
- [ ] Kubernetes module deployado com sucesso
- [ ] Lambda module deployado com sucesso
- [ ] Inventário final confirmado
- [ ] Testes de conectividade OK

## 🎯 Ordem Crítica (NUNCA ALTERAR)

1. **PRÉ**: Limpeza + Pré-requisitos
2. **DATABASE**: terraform/database
3. **MIGRATIONS**: run-migrations.sh
4. **KUBERNETES**: terraform/kubernetes  
5. **LAMBDA**: terraform/lambda
6. **VERIFICAÇÃO**: aws-inventory.sh

**Esta ordem resolve definitivamente o dependency hell!**

## 📝 Changelog

### v2.1 (2025-09-07)
- ✅ **S3 bucket com nome fixo**: `lanchonete-migrations` (sem random suffix)
- ✅ **Cleanup S3 melhorado**: Remove buckets com objetos automaticamente
- ✅ **Scripts robustos**: Timeouts e tratamento de erros
- ✅ **Migration externa**: Script `run-migrations.sh` para controle manual

### v2.0 (2025-09-07)
- ✅ **Dependency hell resolvido**: Ordem crítica definida
- ✅ **Cleanup automatizado**: Script `aws-cleanup.sh` robusto
- ✅ **Troubleshooting completo**: Cenários comuns documentados

---
*Guia criado após análise completa do dependency hell e testes extensivos*
*Versão: 2.1 - 2025-09-07*
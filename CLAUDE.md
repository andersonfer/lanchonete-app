# Terraform Infrastructure - Lanchonete Project

## Scripts de Automação Criados

### Scripts Disponíveis
```bash
# Limpeza automatizada (aguardar 20min para ENIs Lambda)
./aws-cleanup.sh

# Migrations controladas 
./run-migrations.sh

# Inventário completo
./aws-inventory.sh
```

## Progresso do Deploy

### ✅ Database Module (CONCLUÍDO)
```bash
cd terraform/database
terraform init
terraform plan -var-file="../shared/terraform.tfvars"
terraform apply -var-file="../shared/terraform.tfvars"
```
**Status**: Deploy OK, migrations executadas, limpeza testada

### ✅ Migrations (CONCLUÍDO)  
```bash
./run-migrations.sh
```
**Status**: Script funcional, tabelas criadas com sucesso

### ✅ Kubernetes Module (CONCLUÍDO)
```bash
cd terraform/kubernetes
terraform init
terraform plan -var-file="../shared/terraform.tfvars" 
terraform apply -var-file="../shared/terraform.tfvars"
```
**Status**: Deploy OK - 13 recursos criados em ~15min
- EKS Cluster v1.30 (lanchonete-cluster) - ACTIVE
- Node Group (lanchonete-nodes) com t3.small - ACTIVE  
- Network Load Balancer (lanchonete-nlb) - active
- VPC Link (jyqa2y) - AVAILABLE
- ECR repositories + Docker builds OK

### ✅ Lambda Module (CONCLUÍDO)
```bash
cd terraform/lambda
terraform init
terraform plan -var-file="../shared/terraform.tfvars"
terraform apply -var-file="../shared/terraform.tfvars"
```
**Status**: Deploy OK - 37 recursos criados em ~30s
- API Gateway REST API - DEPLOYED
- Lambda functions (auth + JWT authorizer) - ACTIVE  
- VPC Link integrations - CONNECTED TO EKS
- API endpoints funcionais

## Melhorias Implementadas

### S3 Bucket Nome Fixo
- **Antes**: `lanchonete-migrations-xxxxxxxx` (random suffix)
- **Depois**: `lanchonete-migrations` (nome fixo)
- **Benefício**: Cleanup mais limpo, debugging mais fácil

### Cleanup Automatizado
- **Problema**: Recursos órfãos após destroy
- **Solução**: Script robusto com timeouts
- **Limitação**: ENIs Lambda levam ~20min para liberação (AWS behavior)

### Migration Externa
- **Problema**: local-exec não é confiável
- **Solução**: Script externo com validações

## Ordem Crítica dos Módulos
1. **database** (RDS, Security Groups, Lambdas)
2. **kubernetes** (EKS, NLB, VPC Link) 
3. **lambda** (API Gateway, conecta com EKS via VPC Link)

## Status Atual
- [x] Backend S3 funcionando
- [x] Database module testado e funcionando (2025-09-07)
- [x] Kubernetes module testado e funcionando (2025-09-07)
- [x] Scripts de automação criados e testados
- [x] Cleanup 100% automatizado
- [ ] Lambda module (próximo passo)
- [ ] Dependency hell resolvido (final)
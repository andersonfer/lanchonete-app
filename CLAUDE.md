# Terraform Infrastructure - Lanchonete Project

## Problemas Identificados e Soluções

### Estrutura Atual
```
terraform/
├── database/     # RDS, Security Groups, Migration Lambdas
├── lambda/       # API Gateway, Auth/JWT Lambdas  
├── kubernetes/   # EKS, ALB, VPC Link
└── shared/       # Variables compartilhadas
```

### Problemas Críticos Detectados

#### 1. Backend S3 não existe
- **Problema**: Todos os módulos referenciam bucket `lanchonete-tfstate` que não existe
- **Sintoma**: `terraform init` falha em todos os módulos
- **Solução**: Criar bucket S3 e tabela DynamoDB antes de inicializar

#### 2. Dependências entre módulos não gerenciadas
- **Problema**: Módulos usam `data sources` para referenciar recursos de outros módulos
- **Sintoma**: Ordem de execução manual necessária, recursos órfãos na destruição
- **Solução**: Usar outputs explícitos e remote state

#### 3. Variables sensíveis não padronizadas
- **Problema**: `db_password` e `jwt_secret` precisam ser fornecidas manualmente
- **Sintoma**: Automação quebra por falta de variáveis
- **Solução**: Usar terraform.tfvars ou AWS Secrets Manager

#### 4. VPC Link/NLB dependency hell
- **Problema**: VPC Link referencia NLB, mas dependency não é explícita
- **Sintoma**: Não consegue destruir NLB (como vimos)
- **Solução**: Definir depends_on explícito

## Ordem de Execução Correta

### Pré-requisitos (executar primeiro)
1. Criar backend S3
2. Criar terraform.tfvars com variables sensíveis

### Ordem dos módulos
1. `shared/` (se existir estado compartilhado)
2. `database/` (RDS, Security Groups)
3. `kubernetes/` (EKS, NLB)  
4. `lambda/` (API Gateway, Lambdas)

## Comandos Padronizados

### Inicialização
```bash
# Para cada módulo, na ordem:
cd terraform/[module]
terraform init
terraform plan -var-file="../shared/terraform.tfvars"
terraform apply -var-file="../shared/terraform.tfvars"
```

### Destruição (ordem inversa)
```bash
# lambda/ primeiro, depois kubernetes/, depois database/
terraform destroy -var-file="../shared/terraform.tfvars"
```

## Status dos Testes
- [ ] Backend S3 criado
- [ ] Módulo database testado
- [ ] Módulo kubernetes testado  
- [ ] Módulo lambda testado
- [ ] Teste de destruição completo
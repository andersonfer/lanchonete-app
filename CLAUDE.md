# Projeto AWS POC - Lanchonete App

## 🚀 ROTEIRO COMPLETO - Deploy da Infraestrutura

**Este roteiro deve ser seguido a cada nova sessão para subir todo o ambiente:**

### Criar Backend S3 + DynamoDB
```bash
cd infra/backend
terraform init
terraform apply -auto-approve
```

### Criar Repositórios ECR
```bash
cd ../ecr
terraform init
terraform apply -auto-approve
```

### Criar Banco RDS MySQL
```bash
cd ../database
terraform init
terraform apply -auto-approve
```

### Criar Cluster EKS (8-12 minutos)
```bash
cd ../kubernetes
terraform init
terraform apply -auto-approve
```

### Configurar ALB Controller (5-8 minutos)
```bash
cd ../ingress
terraform init
terraform apply -auto-approve
```

### Configurar kubectl
```bash
cd ../..
aws eks update-kubeconfig --region us-east-1 --name lanchonete-cluster
kubectl get nodes
```

### Atualizar Manifests com URLs Dinâmicas
```bash
./scripts/update-manifests.sh
```

### Criar Secrets do RDS
```bash
./scripts/create-secrets.sh
```

### Build e Push das Imagens Docker
```bash
./scripts/build-and-push.sh
```

### Deploy no Kubernetes (inclui Ingresses ALB)
```bash
./scripts/deploy-k8s.sh
```

### Aguardar ALBs ficarem ativos (5-10 minutos)
```bash
# Monitorar status dos Ingresses
watch -n 30 'kubectl get ingress -o wide'

# Verificar quando os ALBs estiverem com ADDRESS
kubectl get ingress
```

### Verificar Funcionamento e Testar Integração
```bash
# Verificar pods e ingresses
kubectl get pods
kubectl get ingress -o wide

# Testar health checks dos ALBs
curl http://[AUTOATENDIMENTO-ALB-URL]/actuator/health
curl http://[PAGAMENTO-ALB-URL]/actuator/health

# Teste completo de integração
curl -X POST http://[AUTOATENDIMENTO-ALB-URL]/pedidos/checkout \
  -H "Content-Type: application/json" \
  -d '{"cpfCliente": null, "itens": [{"produtoId": 1, "quantidade": 1}]}'
```

---

## ⚡ Scripts Automatizados Criados

- `scripts/update-manifests.sh` - Atualiza manifests com URLs ECR/RDS dinâmicas
- `scripts/create-secrets.sh` - Cria Secrets do RDS automaticamente
- `scripts/build-and-push.sh` - Build e push das imagens para ECR
- `scripts/deploy-k8s.sh` - Deploy completo no Kubernetes

## 📊 Status da Última Sessão (11/09/2025)

**🎉 INFRAESTRUTURA COMPLETA E 100% TESTADA:**
- **Backend S3 + DynamoDB**: Funcionando ✅
- **ECR Repositories**: Imagens buildadas e enviadas ✅
- **RDS MySQL 8.0**: Conectado às aplicações ✅
- **EKS Cluster**: 2 nodes ativos ✅
- **AWS Load Balancer Controller**: Instalado via Terraform ✅
- **Application Load Balancers**: Ambos funcionando perfeitamente ✅
  - Autoatendimento: `lanchonete-autoatendimento-alb-1781225815.us-east-1.elb.amazonaws.com`
  - Pagamento: `lanchonete-pagamento-alb-786070014.us-east-1.elb.amazonaws.com`

**🔧 MIGRAÇÃO PARA ALB COMPLETA:**
- ✅ Migração de Classic LoadBalancer para Application Load Balancer
- ✅ Ingresses ALB configurados para ambos os serviços
- ✅ Webhook automático entre serviços funcionando
- ✅ Integração completa testada com 3 fluxos diferentes
- ✅ Scripts atualizados para incluir deploy dos Ingresses

**📊 TESTES REALIZADOS COM SUCESSO:**
| **Teste** | **Pedido** | **Valor** | **Status** | **Webhook** |
|-----------|------------|-----------|------------|-------------|
| 1 | PED000002 (ID: 2) | R$ 40,70 | ✅ APROVADO | ✅ Automático |
| 2 | PED000003 (ID: 3) | R$ 56,70 | ✅ APROVADO | ✅ Automático |
| 3 | PED000004 (ID: 4) | R$ 43,70 | ✅ APROVADO | ✅ Automático |

**📁 ESTRUTURA ATUALIZADA:**
- `infra/backend/` - S3 + DynamoDB (✅ aplicado)
- `infra/ecr/` - Repositórios de imagem (✅ aplicado)  
- `infra/database/` - RDS MySQL (✅ aplicado)
- `infra/kubernetes/` - EKS cluster (✅ aplicado)
- `infra/ingress/` - ALB Controller (✅ aplicado)
- `k8s_manifests/` - manifests com Ingresses ALB (✅ aplicado)
- `scripts/` - scripts atualizados com ALB deploy (✅ aplicado)

---

## Especificações do Projeto

1. **Infraestrutura**
   - Banco MySQL migrado para **AWS RDS** ✅
   - Cluster **EKS** para rodar a aplicação Kubernetes ⏳
   - Sempre usar **LabRole** para todas as operações AWS ✅
   - Apenas um ambiente, não é necessário nomear dev/prod ✅
   - Estrutura mínima, simples e funcional para POC ✅

2. **Aplicação**
   - Dois serviços:
     a) **autoatendimento**: acessa o banco MySQL
     b) **pagamento**: callback simples para autoatendimento, não acessa o banco
   - Ambos rodando no **mesmo namespace**
   - Integrar os **manifests Kubernetes existentes**, organizando por serviço:
     - `k8s_manifests/autoatendimento`
     - `k8s_manifests/pagamento`
   - **Remover todos os manifests relacionados a MySQL ou storage** (StatefulSets, PVC, PV, ConfigMaps ou Secrets antigos do banco)
   - Adicionar apenas arquivos novos quando necessário

3. **Gerenciamento Manual**
   - **Abandonamos GitHub Actions** devido a múltiplos erros
   - Toda infraestrutura é criada manualmente via Terraform
   - Backend S3 configurado em todos os módulos
   - Estado centralizado: `s3://lanchonete-terraform-state-poc/`

4. **Estrutura de diretórios atual**
   - `infra/backend/` → S3 bucket + DynamoDB para Terraform state
   - `infra/ecr/` → Repositórios Docker
   - `infra/database/` → RDS MySQL
   - `infra/kubernetes/` → EKS cluster + node group
   - `k8s_manifests/` → manifests reorganizados por serviço

5. **Segurança**
   - Apenas autoatendimento terá Secret (DB credentials)
   - Pagamento não precisa de Secret
   - Secrets criados manualmente, nunca versionados no Git
---

## Configurações Importantes para Próximas Sessões

**🌐 Comunicação:**
- Sempre falar em português brasileiro
- Código em português brasileiro sempre que possível

**🔧 Terraform - Boas Práticas:**
- Usar **LabRole** para todas operações AWS: `data.aws_iam_role.lab_role.arn`
- Backend S3 fixo: `bucket = "lanchonete-terraform-state-poc"`
- Não anotar tipos de ambiente (dev, prod, etc.)
- Usar `locals.common_tags` para tags
- Obter subnets dinamicamente da VPC padrão
- Variável `nome_projeto = "lanchonete"` para consistência

**⚙️ Comandos Terraform:**
- Executar apenas quando solicitado pelo usuário
- Backend já configurado em todos os módulos
- State centralizado no S3

**🚫 O que NÃO fazer:**
- Não criar workflows GitHub Actions (abandonado)
- Não hardcoded senhas/secrets no Git
- Não filtrar AZs específicas para subnets
- os comandos de terraform apply sou eu quem executa e te copio a saída, para evitar timeout
- sem comentarios sobre algo que foi removido
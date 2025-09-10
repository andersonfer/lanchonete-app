# Projeto AWS POC - Lanchonete App

## 🚀 ROTEIRO COMPLETO - Deploy da Infraestrutura

**Este roteiro deve ser seguido a cada nova sessão para subir todo o ambiente:**

### PASSO 1: Criar Backend S3 + DynamoDB
```bash
cd infra/backend
terraform init
terraform apply -auto-approve
```

### PASSO 2: Criar Repositórios ECR
```bash
cd ../ecr
terraform init
terraform apply -auto-approve
```

### PASSO 3: Criar Banco RDS MySQL
```bash
cd ../database
terraform init
terraform apply -auto-approve
```

### PASSO 4: Criar Cluster EKS (8-12 minutos)
```bash
cd ../kubernetes
terraform init
terraform apply -auto-approve
```

### PASSO 5: Configurar kubectl
```bash
cd ../..
aws eks update-kubeconfig --region us-east-1 --name lanchonete-cluster
kubectl get nodes
```

### PASSO 6: Atualizar Manifests com URLs Dinâmicas
```bash
./scripts/update-manifests.sh
```

### PASSO 7: Criar Secrets do RDS
```bash
./scripts/create-secrets.sh
```

### PASSO 8: Build e Push das Imagens Docker
```bash
./scripts/build-and-push.sh
```

### PASSO 9: Deploy no Kubernetes
```bash
./scripts/deploy-k8s.sh
```

### PASSO 10: Verificar Funcionamento
```bash
kubectl get pods
kubectl get services
```

---

## ⚡ Scripts Automatizados Criados

- `scripts/update-manifests.sh` - Atualiza manifests com URLs ECR/RDS dinâmicas
- `scripts/create-secrets.sh` - Cria Secrets do RDS automaticamente
- `scripts/build-and-push.sh` - Build e push das imagens para ECR
- `scripts/deploy-k8s.sh` - Deploy completo no Kubernetes

## 📊 Status da Última Sessão (10/09/2025)

**✅ INFRAESTRUTURA TESTADA E FUNCIONANDO:**
- Backend S3 + DynamoDB: Funcionando
- ECR Repositories: Imagens buildadas e enviadas
- RDS MySQL 8.0: Conectado às aplicações
- EKS Cluster: 2 nodes ativos
- Security Groups: Configurados via Terraform
- **Autoatendimento**: LoadBalancer funcionando e testado ✅
- **Pagamento**: LoadBalancer criado mas curl ainda não retorna (⏳ aguardando)

**🔧 MELHORIAS IMPLEMENTADAS:**
- Scripts de automação criados e testados
- Manifests configurados para LoadBalancer (ao invés de NodePort)
- Security Groups configurados via Terraform para persistir
- Autoatendimento conectando corretamente ao RDS

**⚠️ PRÓXIMOS PASSOS NA NOVA SESSÃO:**
1. Verificar se LoadBalancer do pagamento está respondendo
2. Testar ambos os serviços completamente
3. LoadBalancers podem demorar 5-10 minutos para ficarem ativos

**📁 ESTRUTURA FINAL:**
- `infra/backend/` - S3 + DynamoDB (✅ aplicado)
- `infra/ecr/` - Repositórios de imagem (✅ aplicado)  
- `infra/database/` - RDS MySQL (✅ aplicado)
- `infra/kubernetes/` - EKS cluster (✅ aplicado)
- `k8s_manifests/` - manifests organizados por serviço (✅ aplicado)
- `scripts/` - scripts de automação (✅ criados)

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
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

### Aguardar ALBs ficarem ativos (3-5 minutos)
```bash
# Verificar quando os ALBs estiverem com ADDRESS
kubectl get ingress

# Para monitorar continuamente (opcional)
watch -n 30 'kubectl get ingress -o wide'
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
# 1. Criar pedido
curl -X POST http://[AUTOATENDIMENTO-ALB-URL]/pedidos/checkout \
  -H "Content-Type: application/json" \
  -d '{"cpfCliente": null, "itens": [{"produtoId": 1, "quantidade": 1}]}'

# 2. Processar pagamento (usar o ID retornado do pedido)
curl -X POST http://[PAGAMENTO-ALB-URL]/pagamentos \
  -H "Content-Type: application/json" \
  -d '{"pedidoId": 1, "valor": 18.90}'

# 3. Verificar status do pagamento (aguardar ~10s para processamento)
curl http://[AUTOATENDIMENTO-ALB-URL]/pedidos/1/pagamento/status
```

---

## ⚡ Scripts Automatizados Criados

- `scripts/update-manifests.sh` - Atualiza manifests substituindo qualquer ID de conta AWS pelo correto (resiliente a mudanças)
- `scripts/create-secrets.sh` - Cria Secrets do RDS automaticamente
- `scripts/build-and-push.sh` - Build e push das imagens para ECR
- `scripts/deploy-k8s.sh` - Deploy completo no Kubernetes

## 📊 Status da Última Sessão (15/09/2025)

**🎉 INFRAESTRUTURA COMPLETA E 100% TESTADA:**
- **Backend S3 + DynamoDB**: Funcionando ✅
- **ECR Repositories**: Imagens buildadas e enviadas ✅
- **RDS MySQL 8.0**: Conectado às aplicações ✅
- **EKS Cluster**: 2 nodes ativos ✅
- **AWS Load Balancer Controller**: Instalado via Terraform ✅
- **Application Load Balancers**: Ambos funcionando perfeitamente ✅
  - Os endereços dos ALBs são gerados dinamicamente a cada deploy
  - Verificar com: `kubectl get ingress`

**🔧 MIGRAÇÃO PARA ALB COMPLETA:**
- ✅ Migração de Classic LoadBalancer para Application Load Balancer
- ✅ Ingresses ALB configurados para ambos os serviços
- ✅ Webhook automático entre serviços funcionando
- ✅ Integração completa testada com 3 fluxos diferentes
- ✅ Scripts atualizados para incluir deploy dos Ingresses

**📊 TESTES REALIZADOS COM SUCESSO:**
- ✅ Integração completa testada entre serviços
- ✅ Webhook automático funcionando
- ✅ Aprovações e rejeições de pagamento simuladas (aleatórias)
- ✅ Script `update-manifests.sh` melhorado para substituir qualquer ID de conta AWS

**📁 ESTRUTURA ATUALIZADA:**
- `infra/backend/` - S3 + DynamoDB (✅ aplicado)
- `infra/ecr/` - Repositórios de imagem (✅ aplicado)  
- `infra/database/` - RDS MySQL (✅ aplicado)
- `infra/kubernetes/` - EKS cluster (✅ aplicado)
- `infra/ingress/` - ALB Controller (✅ aplicado)
- `infra/auth/` - Cognito User Pool para autenticação (🚧 em desenvolvimento)
- `infra/lambda/` - Lambda de autenticação em Java (🚧 em desenvolvimento)
- `infra/api-gateway/` - API Gateway com integração Cognito (🚧 em desenvolvimento)
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
   - Três serviços principais:
     a) **autoatendimento**: acessa o banco MySQL
     b) **pagamento**: callback simples para autoatendimento, não acessa o banco
     c) **autenticação**: Lambda em Java + Cognito para identificação via CPF
   - Serviços Kubernetes rodando no **mesmo namespace**
   - **API Gateway** protegendo todos os endpoints com autenticação Cognito
   - Integrar os **manifests Kubernetes existentes**, organizando por serviço:
     - `k8s_manifests/autoatendimento`
     - `k8s_manifests/pagamento`

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
   - `infra/auth/` → Cognito User Pool para autenticação via CPF
   - `infra/lambda/` → Lambda Function em Java para fluxo de autenticação
   - `infra/api-gateway/` → API Gateway com authorizer Cognito
   - `k8s_manifests/` → manifests reorganizados por serviço

5. **Segurança**
   - Apenas autoatendimento terá Secret (DB credentials)
   - Pagamento não precisa de Secret
   - Secrets criados manualmente, nunca versionados no Git
---

## 🔐 **NOVA FEATURE: Autenticação via Cognito + Lambda**

### **Arquitetura de Autenticação:**
```
Cliente → API Gateway → Lambda (Java) → Cognito User Pool → JWT Token
                ↓
         [Token válido]
                ↓
    Serviços protegidos (Autoatendimento/Pagamento)
```

### **Fluxo de Identificação:**
1. **Endpoint de autenticação:** `POST /auth/identificar`
2. **Payload simples:** `{"cpf": "12345678900"}` (ou null para anônimo)
3. **Auto-cadastro transparente:** CPF novo é cadastrado automaticamente
4. **Suporte a anônimos:** Tokens temporários sem necessidade de CPF
5. **Tokens JWT:** Duração de 1h para identificados, 30min para anônimos

### **Integração com Serviços Existentes:**
- API Gateway protege ALBs com authorizer Cognito
- Tokens validados automaticamente pela AWS
- Headers contêm dados do cliente para os serviços

### **Próximos Passos:**
1. Criar Cognito User Pool (`infra/auth/`)
2. Desenvolver Lambda de autenticação em Java (`infra/lambda/`)
3. Configurar API Gateway com authorizer (`infra/api-gateway/`)
4. Integrar com ALBs existentes
5. Testar fluxo completo de autenticação

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
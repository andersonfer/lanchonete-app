# Deploy AWS (EKS)

Scripts para deploy em produção usando AWS EKS e ECR.

## 🚀 Quick Start

```bash
# 1. Provisiona infraestrutura (primeira vez)
cd infra/ecr && terraform apply
cd infra/kubernetes && terraform apply

# 2. Configura kubectl
aws eks update-kubeconfig --region us-east-1 --name lanchonete-cluster

# 3. Deploy completo
./deploy_scripts/aws/deploy.sh
```

## 📋 Scripts Disponíveis

### `build-and-push.sh`
Build e push das imagens para ECR.

```bash
./deploy_scripts/aws/build-and-push.sh
```

**O que faz:**
- Pega URLs dos repositórios ECR do Terraform
- Login no ECR
- Build Maven de cada microserviço
- Build Docker de cada imagem
- Tag com URL ECR completa
- Push para ECR

### `deploy.sh`
Deploy completo no EKS.

```bash
./deploy_scripts/aws/deploy.sh
```

**O que faz:**
1. Executa `build-and-push.sh`
2. Pega URLs ECR do Terraform
3. Aplica StatefulSets (MySQL, MongoDB, RabbitMQ)
4. Aguarda bancos ficarem prontos
5. Aplica Deployments com **substituição inline** dos placeholders
6. Aplica Services (ClusterIP)
7. Aplica Ingress (ALB)
8. Mostra URL do ALB

**Substituição inline:**
```bash
# Placeholder no arquivo: {{ECR_CLIENTES}}
# Substituído em tempo real por: <account>.dkr.ecr.us-east-1.amazonaws.com/lanchonete-clientes:latest
sed "s|{{ECR_CLIENTES}}|${ECR_URL}:latest|g" \
    k8s/aws/deployments/clientes-deployment.yaml | kubectl apply -f -
```

### `cleanup.sh`
Remove todos os recursos do EKS.

```bash
./deploy_scripts/aws/cleanup.sh
```

**Atenção:** Não remove a infraestrutura Terraform (cluster, ECR). Para isso, use `terraform destroy`.

## 🌐 Acesso aos Serviços

Após o deploy, os serviços ficam disponíveis via Application Load Balancer:

```bash
# Pega a URL do ALB (pode levar 3-5 min para provisionar)
ALB_URL=$(kubectl get ingress -o jsonpath='{.items[0].status.loadBalancer.ingress[0].hostname}')

# Health checks
curl http://$ALB_URL/clientes/actuator/health
curl http://$ALB_URL/pedidos/actuator/health
curl http://$ALB_URL/cozinha/actuator/health
curl http://$ALB_URL/pagamento/actuator/health

# Swagger UIs
open http://$ALB_URL/clientes/swagger-ui.html
open http://$ALB_URL/pedidos/swagger-ui.html
open http://$ALB_URL/cozinha/swagger-ui.html
open http://$ALB_URL/pagamento/swagger-ui.html
```

## 🔑 Pré-requisitos

### 1. AWS CLI Configurado

```bash
aws sts get-caller-identity
# Deve retornar info do usuário AWS
```

**AWS Academy:** Renove credenciais periodicamente (expiram em ~4h).

### 2. Terraform Outputs Disponíveis

Os scripts dependem dos outputs do Terraform ECR:

```bash
cd infra/ecr
terraform output repositorios_ecr
# Deve mostrar URLs dos 4 repositórios
```

### 3. kubectl Configurado para EKS

```bash
aws eks update-kubeconfig --region us-east-1 --name lanchonete-cluster
kubectl config current-context
# Deve mostrar algo com "eks" ou "lanchonete"
```

### 4. jq Instalado

```bash
jq --version
# Necessário para processar JSON do Terraform
```

Instale se necessário:
```bash
# Ubuntu/Debian
sudo apt install jq

# macOS
brew install jq
```

## 🔍 Debug

```bash
# Ver pods
kubectl get pods

# Ver logs de um pod
kubectl logs <pod-name>

# Ver status do Ingress
kubectl get ingress -o wide

# Ver eventos
kubectl get events --sort-by='.lastTimestamp'

# Listar imagens no ECR
for repo in clientes pedidos cozinha pagamento; do
  aws ecr list-images --repository-name lanchonete-$repo --region us-east-1
done

# Rollback de um deployment
kubectl rollout undo deployment clientes-deployment
```

## 📝 Configuração

As imagens usam:
- **ImagePullPolicy:** `Always` (sempre faz pull do ECR)
- **Resources:** Alto (512Mi RAM / 500m CPU)
- **Replicas:** 2

Os manifestos estão em: `k8s/aws/`

### Placeholders Usados

Os deployments AWS usam placeholders que são substituídos dinamicamente:

| Placeholder | Descrição |
|------------|-----------|
| `{{ECR_CLIENTES}}` | URL ECR do serviço clientes |
| `{{ECR_PEDIDOS}}` | URL ECR do serviço pedidos |
| `{{ECR_COZINHA}}` | URL ECR do serviço cozinha |
| `{{ECR_PAGAMENTO}}` | URL ECR do serviço pagamento |

## ⚠️ Troubleshooting

### Erro: "The security token included in the request is invalid"

Credenciais AWS Academy expiraram. Renove:

1. Acesse AWS Academy → Learner Lab
2. Start Lab (se parado)
3. AWS Details → Show → Copie credenciais
4. Cole em `~/.aws/credentials`
5. Reconfigure kubectl:
   ```bash
   aws eks update-kubeconfig --region us-east-1 --name lanchonete-cluster
   ```

### Erro: "error: You must be logged in to the server (Unauthorized)"

```bash
aws eks update-kubeconfig --region us-east-1 --name lanchonete-cluster
```

### ALB não provisiona

```bash
# Verifica controller do ALB
kubectl get pods -n kube-system | grep aws-load-balancer

# Se não existir, aplica Terraform do Ingress
cd infra/ingress
terraform apply
```

### Imagens não fazem pull

```bash
# Verifica se o node tem permissão para acessar ECR
kubectl describe pod <pod-name> | grep -A 10 Events

# Verifica se a imagem existe no ECR
aws ecr describe-images --repository-name lanchonete-clientes --region us-east-1
```

## 💰 Custo

**Atenção:** Recursos AWS têm custo! Não esqueça de destruir após uso:

```bash
# Remove aplicação
./deploy_scripts/aws/cleanup.sh

# Remove infraestrutura
cd infra/kubernetes && terraform destroy
cd infra/ecr && terraform destroy
```

---

Para mais detalhes, veja: `../README.md` ou `DEPLOY_STRATEGY.md` (raiz do projeto)

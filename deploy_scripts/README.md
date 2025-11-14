# Scripts de Deploy - Lanchonete App

Este diretório contém scripts para deploy da aplicação em diferentes ambientes.

## 📁 Estrutura

```
deploy_scripts/
├── local/              # Deploy em Minikube (desenvolvimento)
│   ├── build.sh        # Build imagens localmente
│   ├── deploy.sh       # Deploy completo
│   └── cleanup.sh      # Remove recursos
│
└── aws/                # Deploy em AWS EKS (produção)
    ├── build-and-push.sh  # Build e push para ECR
    ├── deploy.sh          # Deploy completo
    └── cleanup.sh         # Remove recursos
```

## 🏠 Ambiente LOCAL (Minikube)

### Pré-requisitos
- Minikube instalado e rodando
- kubectl configurado para Minikube
- Maven 3.9+
- Docker

### Uso

```bash
# Inicia Minikube (se não estiver rodando)
minikube start --memory=4096 --cpus=2

# Deploy completo (build + apply)
./deploy_scripts/local/deploy.sh

# Apenas build das imagens
./deploy_scripts/local/build.sh

# Limpar recursos
./deploy_scripts/local/cleanup.sh
```

### Acesso aos Serviços

Após o deploy, acesse via NodePort:

```bash
# Pega o IP do Minikube
minikube ip

# Acesse os serviços
# - Clientes:  http://<MINIKUBE_IP>:30083
# - Pedidos:   http://<MINIKUBE_IP>:30081
# - Cozinha:   http://<MINIKUBE_IP>:30082
# - Pagamento: http://<MINIKUBE_IP>:30084
```

## ☁️ Ambiente AWS (EKS)

### Pré-requisitos

1. **AWS CLI** configurado com credenciais válidas
2. **Cluster EKS** provisionado:
   ```bash
   cd infra/kubernetes
   terraform apply
   ```
3. **Repositórios ECR** criados:
   ```bash
   cd infra/ecr
   terraform apply
   ```
4. **kubectl** configurado para EKS:
   ```bash
   aws eks update-kubeconfig --region us-east-1 --name lanchonete-cluster
   ```
5. **jq** instalado (para processar outputs JSON do Terraform)

### Uso

```bash
# Deploy completo (build + push + apply)
./deploy_scripts/aws/deploy.sh

# Apenas build e push para ECR
./deploy_scripts/aws/build-and-push.sh

# Limpar recursos
./deploy_scripts/aws/cleanup.sh
```

### Acesso aos Serviços

Após o deploy, acesse via Application Load Balancer:

```bash
# Pega a URL do ALB (pode levar 3-5 min para provisionar)
kubectl get ingress -o wide
```

## 🔄 Workflow Típico

### Desenvolvimento (LOCAL)

```bash
# 1. Desenvolve código
vim services/clientes/...

# 2. Rebuild e redeploy
./deploy_scripts/local/deploy.sh

# 3. Testa
curl http://$(minikube ip):30083/clientes/actuator/health
```

### Produção (AWS)

```bash
# 1. Garante que a infraestrutura está provisionada
cd infra/ecr && terraform apply
cd infra/kubernetes && terraform apply

# 2. Configura kubectl
aws eks update-kubeconfig --region us-east-1 --name lanchonete-cluster

# 3. Deploy
./deploy_scripts/aws/deploy.sh

# 4. Verifica
kubectl get pods
kubectl get ingress
```

## 🎯 Diferenças entre Ambientes

| Aspecto | LOCAL | AWS |
|---------|-------|-----|
| **Images** | `lanchonete-*:latest` (local) | ECR URLs dinâmicas |
| **Registry** | Docker local do Minikube | AWS ECR |
| **Services** | NodePort | ClusterIP + ALB |
| **Replicas** | 1 | 2 |
| **Resources** | Baixo (256Mi/250m) | Alto (512Mi/500m) |
| **Custo** | Grátis | Pago |

## 📝 Notas Importantes

### URLs ECR Dinâmicas (AWS)

Os scripts AWS pegam as URLs dos repositórios ECR automaticamente do Terraform:

```bash
ECR_URLS[clientes]=$(cd infra/ecr && terraform output -json repositorios_ecr | jq -r '.clientes')
```

Isso garante que funciona mesmo quando o Account ID muda (AWS Academy).

### Substituição Inline (AWS)

Os manifestos AWS contêm placeholders que são substituídos **inline** durante o deploy:

```bash
# Placeholder no arquivo: {{ECR_CLIENTES}}
# Substituído por: 266504741899.dkr.ecr.us-east-1.amazonaws.com/lanchonete-clientes:latest

sed "s|{{ECR_CLIENTES}}|${ECR_URL}:latest|g" \
    k8s/aws/deployments/clientes-deployment.yaml | kubectl apply -f -
```

Não são criados arquivos intermediários.

### Secrets

Secrets devem ser criados manualmente antes do primeiro deploy. Veja instruções em `DEPLOY_STRATEGY.md`.

## 🐛 Troubleshooting

### LOCAL: "docker: command not found" no build

```bash
# Certifique-se de que está usando o Docker do Minikube
eval $(minikube docker-env)
```

### LOCAL: Pods em CrashLoopBackOff

```bash
# Verifica logs
kubectl logs <pod-name>

# Verifica se as imagens estão disponíveis localmente
docker images | grep lanchonete
```

### AWS: "Error from server (NotFound): the server could not find the requested resource"

```bash
# Verifica se kubectl está configurado para o cluster correto
kubectl config current-context

# Deve mostrar algo com "eks" ou "lanchonete"
```

### AWS: "The security token included in the request is invalid"

```bash
# Credenciais AWS Academy expiraram
# Renove as credenciais e reconfigure kubectl
aws eks update-kubeconfig --region us-east-1 --name lanchonete-cluster
```

## 📚 Documentação Adicional

Para mais detalhes sobre a estratégia de deploy multi-ambiente, veja:
- `DEPLOY_STRATEGY.md` - Documento completo da estratégia
- `k8s/local/` - Manifestos específicos do ambiente local
- `k8s/aws/` - Manifestos específicos do ambiente AWS
- `k8s/base/` - Recursos compartilhados (StatefulSets)

---

**Última atualização:** 2025-10-27

# Estratégia de Deploy Multi-Ambiente

## 📋 Visão Geral

Este projeto suporta deploy em **dois ambientes distintos**:
- 🏠 **LOCAL**: Minikube (desenvolvimento)
- ☁️ **AWS**: EKS (produção)

Cada ambiente tem suas próprias configurações, scripts e manifestos Kubernetes otimizados.

---

## 🗂️ Estrutura de Diretórios

```
lanchonete-app/
├── deploy_scripts/
│   ├── local/                 # Scripts para ambiente LOCAL
│   │   ├── build.sh           # Build imagens no Docker do Minikube
│   │   ├── deploy.sh          # Deploy completo no Minikube
│   │   └── cleanup.sh         # Remove recursos do Minikube
│   │
│   └── aws/                   # Scripts para ambiente AWS
│       ├── build-and-push.sh  # Build e push para ECR
│       ├── deploy.sh          # Deploy completo no EKS (com substituição inline)
│       └── cleanup.sh         # Remove recursos do EKS
│
└── k8s/
    ├── base/                  # Recursos compartilhados entre ambientes
    │   ├── statefulsets/      # MySQL, MongoDB, RabbitMQ
    │   └── secrets/           # Secrets (criar manualmente)
    │
    ├── local/                 # Manifestos específicos LOCAL
    │   ├── deployments/       # image: lanchonete-*:latest
    │   │                      # imagePullPolicy: Never
    │   │                      # resources: baixos (256Mi/250m)
    │   └── services/          # type: NodePort (30081-30084)
    │
    └── aws/                   # Manifestos específicos AWS
        ├── deployments/       # image: {{ECR_SERVICE}}:latest (placeholder)
        │                      # imagePullPolicy: Always
        │                      # resources: altos (512Mi/500m)
        ├── services/          # type: ClusterIP
        └── ingress/           # AWS ALB Ingress Controller
```

---

## 🔑 Conceitos-Chave

### 1. **Separação Total de Ambientes**

Cada ambiente tem seus próprios manifestos K8s:
- **LOCAL** usa imagens locais (`lanchonete-clientes:latest`)
- **AWS** usa placeholders que são substituídos dinamicamente (`{{ECR_CLIENTES}}`)

### 2. **Substituição Inline (Sem Templates)**

No ambiente AWS, os scripts fazem substituição **inline** dos placeholders:

```bash
sed "s|{{ECR_CLIENTES}}|266504741899.dkr.ecr.us-east-1.amazonaws.com/lanchonete-clientes:latest|g" \
    k8s/aws/deployments/clientes-deployment.yaml | kubectl apply -f -
```

**Vantagens:**
- ✅ Sem arquivos intermediários
- ✅ Sem diretório `generated/`
- ✅ URLs ECR sempre dinâmicas
- ✅ Funciona mesmo quando Account ID muda (AWS Academy)

### 3. **Scripts Independentes**

Cada ambiente tem seus próprios scripts:
- `deploy_scripts/local/deploy.sh` → Deploy no Minikube
- `deploy_scripts/aws/deploy.sh` → Deploy no EKS

**Não há lógica condicional** - cada script faz uma coisa só.

---

## 🏠 Ambiente LOCAL (Minikube)

### Características

| Recurso | Configuração |
|---------|-------------|
| **Images** | `lanchonete-*:latest` (local) |
| **ImagePullPolicy** | `Never` (não faz pull) |
| **Services** | `NodePort` (30081-30084) |
| **Replicas** | 1 |
| **Resources** | Baixo (256Mi/250m) |
| **Acesso** | `http://$(minikube ip):300XX` |

### Como Usar

```bash
# 1. Inicia Minikube (se não estiver rodando)
minikube start --memory=4096 --cpus=2

# 2. Deploy completo
./deploy_scripts/local/deploy.sh
```

### O que o Script Faz

1. Configura Docker para usar o daemon do Minikube
2. Build das 4 imagens Maven + Docker
3. Aplica StatefulSets (MySQL, MongoDB, RabbitMQ)
4. Aguarda bancos ficarem prontos
5. Aplica Deployments e Services dos microserviços
6. Mostra URLs de acesso

### Exemplo de Manifesto LOCAL

```yaml
# k8s/local/deployments/clientes-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: clientes-deployment
spec:
  replicas: 1
  template:
    spec:
      containers:
      - name: clientes
        image: lanchonete-clientes:latest  # Imagem local
        imagePullPolicy: Never             # Não faz pull
        resources:
          requests:
            memory: "256Mi"
            cpu: "250m"
```

---

## ☁️ Ambiente AWS (EKS)

### Características

| Recurso | Configuração |
|---------|-------------|
| **Images** | ECR URLs dinâmicas |
| **ImagePullPolicy** | `Always` (sempre faz pull) |
| **Services** | `ClusterIP` |
| **Ingress** | AWS ALB Controller |
| **Replicas** | 2 |
| **Resources** | Alto (512Mi/500m) |
| **Acesso** | Via ALB URL |

### Pré-requisitos

1. Cluster EKS provisionado:
   ```bash
   cd infra/kubernetes
   terraform apply
   ```

2. Repositórios ECR criados:
   ```bash
   cd infra/ecr
   terraform apply
   ```

3. kubectl configurado para EKS:
   ```bash
   aws eks update-kubeconfig --region us-east-1 --name lanchonete-cluster
   ```

### Como Usar

```bash
./deploy_scripts/aws/deploy.sh
```

### O que o Script Faz

1. **Build e Push** → Executa `build-and-push.sh`:
   - Login no ECR
   - Build das 4 imagens Maven + Docker
   - Tag com URLs ECR
   - Push para ECR

2. **Obtem URLs ECR** do Terraform:
   ```bash
   ECR_URLS[clientes]=$(cd infra/ecr && terraform output -json repositorios_ecr | jq -r '.clientes')
   ```

3. **Aplica StatefulSets** (sem substituição):
   - MySQL (3 instâncias)
   - MongoDB
   - RabbitMQ

4. **Aplica Deployments COM substituição inline**:
   ```bash
   sed "s|{{ECR_CLIENTES}}|${ECR_URLS[clientes]}:latest|g" \
       k8s/aws/deployments/clientes-deployment.yaml | kubectl apply -f -
   ```

5. **Aplica Services e Ingress**

6. **Mostra ALB URL** para acesso

### Exemplo de Manifesto AWS

```yaml
# k8s/aws/deployments/clientes-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: clientes-deployment
spec:
  replicas: 2
  template:
    spec:
      containers:
      - name: clientes
        image: {{ECR_CLIENTES}}:latest  # ⚠️ Placeholder (substituído no deploy)
        imagePullPolicy: Always          # Sempre faz pull do ECR
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
```

---

## 🔄 Fluxo de Deploy

### LOCAL (Desenvolvimento)
```
┌─────────────────────┐
│ minikube start      │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────────────────┐
│ ./deploy_scripts/local/deploy.sh│
└──────────┬──────────────────────┘
           │
           ├─► eval $(minikube docker-env)
           │
           ├─► mvn clean package (x4)
           │
           ├─► docker build (x4)
           │
           ├─► kubectl apply -f k8s/base/statefulsets/
           │
           ├─► kubectl apply -f k8s/local/deployments/
           │
           └─► kubectl apply -f k8s/local/services/
```

### AWS (Produção)
```
┌──────────────────────────────┐
│ terraform apply (ECR + EKS)  │
└──────────┬───────────────────┘
           │
           ▼
┌─────────────────────────────────┐
│ ./deploy_scripts/aws/deploy.sh  │
└──────────┬──────────────────────┘
           │
           ├─► aws ecr get-login-password
           │
           ├─► mvn clean package + docker build (x4)
           │
           ├─► docker push ECR (x4)
           │
           ├─► terraform output (pega URLs ECR)
           │
           ├─► kubectl apply -f k8s/base/statefulsets/
           │
           ├─► sed "s|{{ECR_*}}|URL|g" ... | kubectl apply -f -
           │
           ├─► kubectl apply -f k8s/aws/services/
           │
           └─► kubectl apply -f k8s/aws/ingress/
```

---

## 🎯 Placeholders AWS

Os seguintes placeholders são usados nos manifestos `k8s/aws/deployments/`:

| Placeholder | Substituído por |
|------------|-----------------|
| `{{ECR_CLIENTES}}` | URL completa do ECR para clientes |
| `{{ECR_PEDIDOS}}` | URL completa do ECR para pedidos |
| `{{ECR_COZINHA}}` | URL completa do ECR para cozinha |
| `{{ECR_PAGAMENTO}}` | URL completa do ECR para pagamento |

**Exemplo de substituição:**
```yaml
# ANTES (no arquivo)
image: {{ECR_CLIENTES}}:latest

# DEPOIS (aplicado ao cluster)
image: 266504741899.dkr.ecr.us-east-1.amazonaws.com/lanchonete-clientes:latest
```

---

## 🔒 Secrets

Secrets devem ser criados manualmente em ambos ambientes:

```bash
# MySQL Clientes
kubectl create secret generic mysql-clientes-secret \
  --from-literal=root-password=root123 \
  --from-literal=database=clientes_db \
  --from-literal=username=clientes_user \
  --from-literal=password=clientes123

# MySQL Pedidos
kubectl create secret generic mysql-pedidos-secret \
  --from-literal=root-password=root123 \
  --from-literal=database=pedidos_db \
  --from-literal=username=pedidos_user \
  --from-literal=password=pedidos123

# MySQL Cozinha
kubectl create secret generic mysql-cozinha-secret \
  --from-literal=root-password=root123 \
  --from-literal=database=cozinha_db \
  --from-literal=username=cozinha_user \
  --from-literal=password=cozinha123

# MongoDB Pagamento
kubectl create secret generic mongodb-secret \
  --from-literal=root-username=admin \
  --from-literal=root-password=admin123 \
  --from-literal=database=pagamentos

# RabbitMQ
kubectl create secret generic rabbitmq-secret \
  --from-literal=username=admin \
  --from-literal=password=admin123
```

---

## 🧹 Limpeza

### LOCAL
```bash
./deploy_scripts/local/cleanup.sh
```

### AWS
```bash
./deploy_scripts/aws/cleanup.sh
```

---

## 📊 Comparação Detalhada

| Aspecto | LOCAL | AWS |
|---------|-------|-----|
| **Cluster** | Minikube | EKS |
| **Image Registry** | Local | ECR |
| **Image URLs** | `lanchonete-*:latest` | `<account>.dkr.ecr.<region>.amazonaws.com/lanchonete-*:latest` |
| **Image Pull Policy** | `Never` | `Always` |
| **Service Type** | `NodePort` | `ClusterIP` + ALB |
| **Ingress** | Opcional | ALB Controller (obrigatório) |
| **Replicas** | 1 | 2-3 |
| **Memory Request** | 256Mi | 512Mi |
| **CPU Request** | 250m | 500m |
| **StatefulSets** | Mesmos | Mesmos |
| **Custo** | Grátis | Pago (AWS) |
| **Propósito** | Desenvolvimento | Produção |

---

## ✅ Status Atual (2025-10-27)

### LOCAL (Minikube) - ✅ OPERACIONAL
- ✅ Scripts de deploy completos em `deploy_scripts/local/`
- ✅ Manifestos K8s em `k8s/local/`
- ✅ Testes E2E passando 100%
- ✅ StatefulSets MySQL, MongoDB, RabbitMQ rodando
- ✅ 4 microserviços deployados via NodePort

### AWS (EKS) - ✅ OPERACIONAL
- ✅ Cluster EKS provisionado (2 nós t3.medium)
- ✅ RDS MySQL (3 instâncias db.t3.micro)
- ✅ MongoDB/RabbitMQ em pods (emptyDir)
- ✅ 4 microserviços deployados
- ✅ LoadBalancer Services (4 NLBs)
- ✅ Testes E2E passando 100%
- ✅ Scripts de deploy completos em `deploy_scripts/aws/`
- ✅ Secrets criados dinamicamente via Terraform

### Decisão Final de Arquitetura AWS
Após testes, optou-se por **LoadBalancer Services** ao invés de ALB+Ingress por:
1. **Simplicidade**: Cada serviço tem seu próprio endpoint
2. **Confiabilidade**: Menos pontos de falha
3. **Debugging**: Mais fácil isolar problemas
4. **Custo aceitável**: ~$64/mês (4 NLBs) vs ~$16/mês (1 ALB)

## 🚀 Conclusão

Ambos os ambientes (Local e AWS) estão **100% operacionais** com:
- ✅ Repositórios ECR criados
- ✅ Imagens buildadas e pushed
- ✅ Estrutura `deploy_scripts/` completa
- ✅ Scripts de deploy automatizados
- ✅ Manifestos K8s separados por ambiente
- ✅ Cluster EKS provisionado
- ✅ Deploy testado e validado em ambos ambientes
- ✅ Testes E2E completos (local + aws)

---

## 📝 Notas Importantes

1. **URLs ECR dinâmicas**: O Account ID da AWS Academy muda a cada sessão. Os scripts pegam as URLs do Terraform automaticamente.

2. **Secrets dinâmicos AWS**: Script `create-secrets.sh` extrai endpoints e senhas RDS do Terraform e cria secrets no Kubernetes.

3. **Ambientes isolados**: Mudar algo no ambiente LOCAL não afeta AWS e vice-versa.

4. **RDS vs StatefulSets**: AWS usa RDS MySQL (gerenciado), Local usa StatefulSets (pods).

5. **MongoDB/RabbitMQ com emptyDir**: Aceita perda de dados em reinicializações (trade-off AWS Academy).

6. **LoadBalancer URLs**: Mudam a cada redeploy dos Services. Use `kubectl get svc` para obter URLs atualizadas.

---

**Última atualização:** 2025-10-27 20:30
**Status:** ✅ Ambos ambientes operacionais e validados

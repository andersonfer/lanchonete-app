# Manifests Kubernetes - Sistema Lanchonete

Este diretório contém todos os manifests Kubernetes para deploy do sistema de microserviços.

## Estrutura de Diretórios

```
k8s/
├── databases/          # StatefulSets para bancos de dados e mensageria
│   ├── secrets/       # Secrets (gerados via script, não commitados)
│   ├── mysql-clientes.yaml
│   ├── mysql-pedidos.yaml
│   ├── mysql-cozinha.yaml
│   ├── mongodb.yaml
│   └── rabbitmq.yaml
├── services/          # Deployments dos microserviços
│   ├── clientes-deployment.yaml
│   ├── pedidos-deployment.yaml
│   ├── cozinha-deployment.yaml
│   └── pagamento-deployment.yaml
├── local/             # Services NodePort para Minikube
│   ├── clientes-service-nodeport.yaml
│   ├── pedidos-service-nodeport.yaml
│   ├── cozinha-service-nodeport.yaml
│   └── pagamento-service-nodeport.yaml
└── aws/               # Ingress para EKS (ALB)
    └── ingress.yaml
```

---

## Deploy Rápido

### Minikube (Desenvolvimento Local)

```bash
# 1. Iniciar Minikube
minikube start --memory=4096 --cpus=4

# 2. Criar secrets
./scripts/create-secrets.sh

# 3. Deploy databases
kubectl apply -f k8s/databases/

# 4. Aguardar databases prontos (2-3 minutos)
kubectl wait --for=condition=ready pod -l app=mysql-clientes --timeout=180s
kubectl wait --for=condition=ready pod -l app=mysql-pedidos --timeout=180s
kubectl wait --for=condition=ready pod -l app=mysql-cozinha --timeout=180s
kubectl wait --for=condition=ready pod -l app=mongodb --timeout=180s
kubectl wait --for=condition=ready pod -l app=rabbitmq --timeout=180s

# 5. Deploy microserviços (quando prontos)
kubectl apply -f k8s/services/

# 6. Expor via NodePort
kubectl apply -f k8s/local/

# 7. Obter URLs
echo "Clientes:  http://$(minikube ip):30083"
echo "Pedidos:   http://$(minikube ip):30080"
echo "Cozinha:   http://$(minikube ip):30082"
echo "Pagamento: http://$(minikube ip):30081"
echo "RabbitMQ:  http://$(minikube ip):30672"
```

### EKS (Demonstração AWS)

```bash
# 1. Configurar kubectl
aws eks update-kubeconfig --name lanchonete-cluster --region us-east-1

# 2. Criar secrets
./scripts/create-secrets.sh

# 3. Deploy databases
kubectl apply -f k8s/databases/

# 4. Aguardar databases
kubectl wait --for=condition=ready pod -l app=mysql-clientes --timeout=300s
kubectl wait --for=condition=ready pod -l app=mysql-pedidos --timeout=300s
kubectl wait --for=condition=ready pod -l app=mysql-cozinha --timeout=300s
kubectl wait --for=condition=ready pod -l app=mongodb --timeout=300s
kubectl wait --for=condition=ready pod -l app=rabbitmq --timeout=300s

# 5. Deploy microserviços
kubectl apply -f k8s/services/

# 6. Deploy Ingress (ALB)
kubectl apply -f k8s/aws/ingress.yaml

# 7. Aguardar ALB provisionar (~3 minutos)
kubectl wait --for=condition=available --timeout=300s ingress/lanchonete-ingress

# 8. Obter URL do ALB
ALB_URL=$(kubectl get ingress lanchonete-ingress -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')
echo "Aplicação disponível em: http://$ALB_URL"
```

---

## Databases

### MySQL StatefulSets (3x)

Cada microserviço tem seu próprio banco MySQL isolado:

| StatefulSet | Service | Database | Porta | Storage |
|-------------|---------|----------|-------|---------|
| `mysql-clientes` | `mysql-clientes-service` | `clientes_db` | 3306 | 5Gi |
| `mysql-pedidos` | `mysql-pedidos-service` | `pedidos_db` | 3306 | 5Gi |
| `mysql-cozinha` | `mysql-cozinha-service` | `cozinha_db` | 3306 | 5Gi |

**Recursos:**
- CPU: 500m (request) / 1000m (limit)
- RAM: 512Mi (request) / 1Gi (limit)

**Conexão interna (de outros pods):**
```
jdbc:mysql://mysql-clientes-service:3306/clientes_db
jdbc:mysql://mysql-pedidos-service:3306/pedidos_db
jdbc:mysql://mysql-cozinha-service:3306/cozinha_db
```

**Schema:** Gerenciado pelo Spring Boot (`schema.sql` + `data.sql` em cada microserviço)

---

### MongoDB StatefulSet (1x)

| StatefulSet | Service | Database | Porta | Storage |
|-------------|---------|----------|-------|---------|
| `mongodb` | `mongodb-service` | `pagamentos` | 27017 | 5Gi |

**Recursos:**
- CPU: 500m (request) / 1000m (limit)
- RAM: 512Mi (request) / 1Gi (limit)

**Conexão interna:**
```
mongodb://admin:${MONGO_PASSWORD}@mongodb-service:27017/pagamentos?authSource=admin
```

**Schema:** Gerenciado pelo Spring Boot (Spring Data MongoDB)

---

### RabbitMQ StatefulSet (1x)

| StatefulSet | Service | Portas | Storage |
|-------------|---------|--------|---------|
| `rabbitmq` | `rabbitmq-service` | 5672 (AMQP), 15672 (Management) | 5Gi |

**Recursos:**
- CPU: 500m (request) / 1000m (limit)
- RAM: 512Mi (request) / 1Gi (limit)

**Conexão interna (AMQP):**
```
amqp://admin:${RABBITMQ_PASSWORD}@rabbitmq-service:5672
```

**Management UI:**
- **Minikube:** `http://<minikube-ip>:30672` (user: admin / senha: ver secret)
- **EKS:** Port-forward: `kubectl port-forward svc/rabbitmq-service 15672:15672`

**Exchanges criados automaticamente pelos microserviços:**
- `pedido.events` (topic)
- `pagamento.events` (topic)
- `cozinha.events` (topic)

---

## Secrets

Secrets são criados via script `scripts/create-secrets.sh` e **NÃO são commitados no Git**.

**Secrets criados:**
- `mysql-clientes-secret`
- `mysql-pedidos-secret`
- `mysql-cozinha-secret`
- `mongodb-secret`
- `rabbitmq-secret`

Ver [docs/SECRETS-MANAGEMENT.md](../docs/SECRETS-MANAGEMENT.md) para detalhes.

---

## Comandos Úteis

### Verificar Status

```bash
# Todos os recursos
kubectl get all

# Apenas databases
kubectl get statefulsets
kubectl get pods -l 'app in (mysql-clientes,mysql-pedidos,mysql-cozinha,mongodb,rabbitmq)'

# Microserviços
kubectl get deployments
kubectl get pods -l 'app in (clientes,pedidos,cozinha,pagamento)'

# PVCs
kubectl get pvc
```

### Logs

```bash
# Database
kubectl logs mysql-clientes-0
kubectl logs mongodb-0
kubectl logs rabbitmq-0

# Microserviço
kubectl logs deployment/clientes
kubectl logs -f deployment/pedidos  # follow
```

### Acessar Container

```bash
# MySQL
kubectl exec -it mysql-clientes-0 -- mysql -u root -p

# MongoDB
kubectl exec -it mongodb-0 -- mongosh -u admin -p

# RabbitMQ
kubectl exec -it rabbitmq-0 -- rabbitmqctl status
```

### Reiniciar Pods

```bash
# StatefulSet (databases)
kubectl rollout restart statefulset/mysql-clientes
kubectl delete pod mysql-clientes-0  # força recriação

# Deployment (microserviços)
kubectl rollout restart deployment/clientes
```

### Deletar Tudo

```bash
# Microserviços
kubectl delete -f k8s/services/
kubectl delete -f k8s/local/

# Databases (CUIDADO: perde dados!)
kubectl delete -f k8s/databases/

# Secrets
kubectl delete secret mysql-clientes-secret mysql-pedidos-secret \
  mysql-cozinha-secret mongodb-secret rabbitmq-secret

# PVCs (CUIDADO: perde dados permanentemente!)
kubectl delete pvc mysql-clientes-pvc mysql-pedidos-pvc \
  mysql-cozinha-pvc mongodb-pvc rabbitmq-pvc
```

---

## Troubleshooting

### Pod não inicia

```bash
# Ver eventos
kubectl describe pod <pod-name>

# Ver logs
kubectl logs <pod-name>

# Verificar resources
kubectl top pods
```

### Database não conecta

```bash
# Verificar se secret existe
kubectl get secret mysql-clientes-secret

# Verificar se pod está ready
kubectl get pod mysql-clientes-0

# Testar conexão
kubectl exec -it mysql-clientes-0 -- mysql -u root -p -e "SELECT 1"
```

### RabbitMQ não recebe mensagens

```bash
# Acessar Management UI
kubectl port-forward svc/rabbitmq-service 15672:15672
# Abrir: http://localhost:15672

# Verificar exchanges
kubectl exec -it rabbitmq-0 -- rabbitmqctl list_exchanges

# Verificar filas
kubectl exec -it rabbitmq-0 -- rabbitmqctl list_queues
```

### Minikube sem espaço

```bash
# Limpar imagens antigas
minikube ssh docker system prune -a

# Aumentar disk
minikube delete
minikube start --disk-size=20g
```

---

## Storage Classes

### Minikube
- **StorageClass:** `standard` (padrão)
- **Provisioner:** `k8s.io/minikube-hostpath`
- **Tipo:** Hostpath (diretório local)

### EKS
- **StorageClass:** `gp2` (padrão)
- **Provisioner:** `kubernetes.io/aws-ebs`
- **Tipo:** EBS (Elastic Block Store)

**Nota:** Os manifests usam `gp2`, mas Minikube ignora e usa `standard` automaticamente.

---

## Próximos Passos

1. Implementar microserviços em `services/`
2. Criar Deployments em `k8s/services/`
3. Configurar Ingress para EKS
4. Testar fluxo completo E2E

Ver [docs/MIGRATION-GUIDE.md](../docs/MIGRATION-GUIDE.md) para o plano de migração completo.

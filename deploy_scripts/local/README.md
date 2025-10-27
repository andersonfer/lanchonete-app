# Deploy Local (Minikube)

Scripts para desenvolvimento local usando Minikube.

## 🚀 Quick Start

```bash
# 1. Inicia Minikube
minikube start --memory=4096 --cpus=2

# 2. Deploy completo
./deploy_scripts/local/deploy.sh
```

## 📋 Scripts Disponíveis

### `build.sh`
Build das imagens Docker localmente usando o daemon do Minikube.

```bash
./deploy_scripts/local/build.sh
```

**O que faz:**
- Configura Docker para usar daemon do Minikube
- Build Maven de cada microserviço
- Build Docker de cada imagem (`lanchonete-*:latest`)

### `deploy.sh`
Deploy completo no Minikube.

```bash
./deploy_scripts/local/deploy.sh
```

**O que faz:**
1. Executa `build.sh`
2. Aplica StatefulSets (MySQL, MongoDB, RabbitMQ)
3. Aguarda bancos ficarem prontos
4. Aplica Deployments dos microserviços
5. Aplica Services (NodePort)
6. Mostra URLs de acesso

### `cleanup.sh`
Remove todos os recursos do Minikube.

```bash
./deploy_scripts/local/cleanup.sh
```

## 🌐 Acesso aos Serviços

Após o deploy, os serviços ficam disponíveis via NodePort:

```bash
MINIKUBE_IP=$(minikube ip)

# Health checks
curl http://$MINIKUBE_IP:30083/clientes/actuator/health
curl http://$MINIKUBE_IP:30081/pedidos/actuator/health
curl http://$MINIKUBE_IP:30082/cozinha/actuator/health
curl http://$MINIKUBE_IP:30084/pagamento/actuator/health

# Swagger UIs
open http://$MINIKUBE_IP:30083/swagger-ui.html  # Clientes
open http://$MINIKUBE_IP:30081/swagger-ui.html  # Pedidos
open http://$MINIKUBE_IP:30082/swagger-ui.html  # Cozinha
open http://$MINIKUBE_IP:30084/swagger-ui.html  # Pagamento
```

## 🔍 Debug

```bash
# Ver pods
kubectl get pods

# Ver logs de um pod
kubectl logs <pod-name>

# Ver todas as imagens no Minikube
eval $(minikube docker-env)
docker images | grep lanchonete

# Restart de um deployment
kubectl rollout restart deployment clientes-deployment
```

## 📝 Configuração

As imagens usam:
- **ImagePullPolicy:** `Never` (não tenta pull de registry)
- **Resources:** Baixo (256Mi RAM / 250m CPU)
- **Replicas:** 1

Os manifestos estão em: `k8s/local/`

---

Para mais detalhes, veja: `../README.md` ou `DEPLOY_STRATEGY.md` (raiz do projeto)

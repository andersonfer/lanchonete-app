# Guia de Deployment Completo - AWS EKS com Autenticação

Este guia garante um deployment 100% repetível da infraestrutura e aplicação.

## 📋 Pré-requisitos

1. **AWS CLI** configurado com credenciais válidas
2. **Terraform** >= 1.0
3. **kubectl** instalado
4. **Docker** instalado
5. **JDK 17** (para build da Lambda)
6. **Maven** (para build dos serviços Java)
7. **jq** (para parsing de JSON)

## 🏗️ Arquitetura Provisionada

```
Cliente
   ↓
API Gateway (JWT Cognito Authorizer)
   ↓
ALB (Application Load Balancer) - 4 ALBs (um por serviço)
   ↓
ClusterIP Services (privados)
   ↓
Pods (clientes, pedidos, cozinha, pagamento)
```

### Recursos Criados:

- **Backend**: S3 bucket + DynamoDB table (Terraform state)
- **ECR**: 4 repositórios (imagens Docker)
- **EKS Cluster**: Cluster Kubernetes gerenciado
- **RDS MySQL**: 3 instâncias (clientes, pedidos, cozinha)
- **DocumentDB**: 1 instância (pagamento)
- **AWS Load Balancer Controller**: Helm chart instalado no cluster
- **Ingress Resources**: 4 ALBs provisionados automaticamente
- **Cognito User Pool**: Autenticação e emissão de JWT
- **Lambda AuthHandler**: Função Java 17 para autenticação customizada
- **API Gateway**: REST API com Cognito Authorizer

## 🚀 Processo de Deployment (Ordem Correta)

### Passo 1: Limpar Deploy Anterior (se existir)

```bash
cd /path/to/lanchonete-app
./deploy_scripts/aws/00-cleanup.sh
```

**O que faz:**
- Destroy de todos os recursos Terraform (em ordem reversa)
- Remove imagens Docker do ECR
- Limpa recursos Kubernetes

**Tempo estimado:** 15-20 minutos

---

### Passo 2: Provisionar Infraestrutura Base

```bash
./deploy_scripts/aws/01-provision-infrastructure.sh
```

**O que faz:**
1. Provisiona Backend (S3 + DynamoDB)
2. Cria repositórios ECR
3. Provisiona Cluster EKS (10-15 min)
4. Provisiona bancos de dados RDS/DocumentDB
5. Configura kubectl
6. **Instala AWS Load Balancer Controller** (via Terraform/Helm)

**Tempo estimado:** 20-25 minutos

**Validação:**
```bash
kubectl get nodes
kubectl get deployment -n kube-system aws-load-balancer-controller
aws eks describe-cluster --name lanchonete-cluster
```

---

### Passo 3: Deploy dos Microserviços

```bash
./deploy_scripts/aws/02-deploy.sh
```

**O que faz:**
1. Build das imagens Docker localmente
2. Push para ECR
3. Aplica ConfigMaps (banco de dados, RabbitMQ, etc.)
4. Aplica Secrets (credenciais)
5. Aplica StatefulSets (bancos de dados)
6. Aplica Deployments (microserviços)
7. Aplica Services (ClusterIP - privados)
8. **Aplica Ingress Resources** → AWS LB Controller provisiona 4 ALBs automaticamente

**Tempo estimado:** 10-15 minutos (incluindo tempo de provisionamento dos ALBs)

**Validação:**
```bash
kubectl get pods
kubectl get svc
kubectl get ingress
aws elbv2 describe-load-balancers | grep lanchonete
```

**⚠️ IMPORTANTE:** Aguarde 3-5 minutos após o deployment para os ALBs ficarem ativos.

---

### Passo 4: Provisionar Autenticação

```bash
./deploy_scripts/aws/03-provision-auth.sh
```

**O que faz:**
1. **Verifica se ALBs estão ativos** (obtém URLs via AWS CLI)
2. Provisiona Cognito User Pool
3. Build e deploy da Lambda AuthHandler (Java 17)
4. Provisiona API Gateway com Cognito Authorizer
5. **Configura integrações HTTP_PROXY para os 4 ALBs:**
   - `/clientes/{proxy+}` → `http://ALB_CLIENTES/clientes/{proxy}`
   - `/pedidos/{proxy+}` → `http://ALB_PEDIDOS/pedidos/{proxy}`
   - `/cozinha/{proxy+}` → `http://ALB_COZINHA/cozinha/{proxy}`
   - `/pagamento/{proxy+}` → `http://ALB_PAGAMENTO/pagamento/{proxy}`

**Tempo estimado:** 5-8 minutos

**Validação:**
```bash
cd infra/api-gateway
terraform output api_gateway_url
```

---

### Passo 5: Testes de Autenticação

```bash
./deploy_scripts/aws/04-test-auth.sh
```

**O que faz:**
- Testa 5 cenários de autenticação
- Valida emissão de tokens JWT
- Valida autorização nos endpoints

**Resultado esperado:** 10/10 testes passando

---

### Passo 6: Validação End-to-End

```bash
./deploy_scripts/aws/05-validate-deployment.sh
```

**O que faz:**
- Verifica todos os módulos Terraform
- Verifica status dos ALBs
- Testa API Gateway
- Testa autenticação JWT
- Testa endpoints de negócio (POST, GET)
- Testa segurança (acesso sem token deve ser bloqueado)
- Testa health checks

**Resultado esperado:** 0 erros

---

## 🔧 Configuração Final do API Gateway (IMPORTANTE)

### Problema Resolvido: Roteamento Correto

**Configuração correta** (implementada):

```terraform
# /clientes/{proxy+} → http://ALB/clientes/{proxy}
resource "aws_api_gateway_integration" "clientes_integration" {
  uri = "${var.clientes_service_url}/clientes/{proxy}"
  # ...
}
```

**Por que funciona:**
- Cliente chama: `POST https://API-GW/v1/clientes`
- API Gateway encaminha: `POST http://ALB/clientes/`
- Controller Spring Boot: `@RequestMapping("/clientes")` → ✅ Match!

**❌ Erro comum (NÃO fazer):**
- ~~Adicionar recursos raiz `/clientes` além do `{proxy+}`~~ → causa duplicação de path
- ~~Usar `http://ALB/{proxy}` sem incluir o nome do serviço~~ → paths não fazem match

---

## 📊 Endpoints Disponíveis

### Autenticação (sem JWT)
```bash
POST https://API-GW/v1/auth/identificar
Body: {"cpf": "12345678901"}  # ou {"cpf": null} para anônimo
Response: {"accessToken": "eyJ...", "tipo": "CPF" | "ANONIMO"}
```

### Clientes (requer JWT)
```bash
# Criar cliente
POST https://API-GW/v1/clientes
Headers: Authorization: Bearer {token}
Body: {"nome": "...", "cpf": "...", "email": "..."}

# Buscar por CPF
GET https://API-GW/v1/clientes/cpf/{cpf}
Headers: Authorization: Bearer {token}

# Identificar
POST https://API-GW/v1/clientes/identificar
Headers: Authorization: Bearer {token}
Body: {"cpf": "..."}
```

### Pedidos, Cozinha, Pagamento
Seguem o mesmo padrão: `/pedidos/...`, `/cozinha/...`, `/pagamento/...`

---

## 🔒 Segurança Implementada

### 1. Autenticação JWT via Cognito
- API Gateway valida JWT em todos os endpoints (exceto `/auth/identificar`)
- Tokens são assinados pelo Cognito User Pool
- Tokens expiram em 30 minutos

### 2. Serviços Privados
- Todos os Services são `type: ClusterIP` (não acessíveis externamente)
- Apenas ALBs são públicos (roteiam tráfego do API Gateway)

### 3. Defesa em Profundidade
```
✅ API Gateway → valida JWT
✅ ALB → roteia apenas tráfego autorizado
✅ ClusterIP Service → privado, só acessível dentro do cluster
✅ Pod → recebe apenas requisições autorizadas
```

### 4. Recomendação Adicional
Para ambiente de produção, configurar:
- **Security Groups dos ALBs** para aceitar apenas tráfego do API Gateway
- Ou usar **VPC Link** para conexão privada API Gateway ↔ ALB

---

## 🧪 Testes Manuais

### 1. Obter Token
```bash
API_URL="https://{api-id}.execute-api.us-east-1.amazonaws.com/v1"

TOKEN=$(curl -s -X POST "$API_URL/auth/identificar" \
  -H "Content-Type: application/json" \
  -d '{"cpf": null}' | jq -r '.accessToken')

echo $TOKEN
```

### 2. Criar Cliente
```bash
curl -X POST "$API_URL/clientes" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"nome": "Test", "cpf": "12345678901", "email": "test@example.com"}'
```

### 3. Buscar Cliente
```bash
curl "$API_URL/clientes/cpf/12345678901" \
  -H "Authorization: Bearer $TOKEN"
```

### 4. Testar Segurança (deve retornar 401)
```bash
curl "$API_URL/clientes"
# Expected: {"message":"Unauthorized"}
```

---

## 🐛 Troubleshooting

### ALBs não foram criados
```bash
# Verificar se AWS Load Balancer Controller está rodando
kubectl get deployment -n kube-system aws-load-balancer-controller

# Ver logs do controller
kubectl logs -n kube-system deployment/aws-load-balancer-controller

# Verificar Ingress resources
kubectl get ingress
kubectl describe ingress clientes-ingress
```

### Endpoints retornam 500
```bash
# Verificar logs dos pods
kubectl logs -l app=clientes

# Verificar se ALB consegue alcançar os pods
kubectl get pods -o wide
kubectl describe ingress clientes-ingress
```

### Script 03 falha ao obter URLs
```bash
# Verificar manualmente se ALBs existem
aws elbv2 describe-load-balancers | grep lanchonete

# ALBs levam 3-5 minutos para ficarem ativos após aplicar Ingress
# Aguarde e tente novamente
```

---

## 📝 Checklist de Deployment

- [ ] Credenciais AWS configuradas
- [ ] Cleanup de deployment anterior (se existir)
- [ ] Passo 1: Infraestrutura provisionada
- [ ] AWS Load Balancer Controller instalado
- [ ] Passo 2: Microserviços deployados
- [ ] Ingress resources aplicados (ALBs criados)
- [ ] **Aguardar 3-5 min para ALBs ficarem ativos**
- [ ] Passo 3: Autenticação provisionada
- [ ] Passo 4: Testes de autenticação passando (10/10)
- [ ] Passo 5: Validação E2E passando (0 erros)
- [ ] Testar manualmente alguns endpoints

---

## 🎯 Pontos Críticos para Deployment Repetível

### 1. AWS Load Balancer Controller DEVE ser provisionado ANTES do step 7 do script 02

Isso está garantido agora no script 01-provision-infrastructure.sh (Passo 6).

### 2. Script 03 DEVE obter URLs dos ALBs (não LoadBalancers)

Corrigido para usar `aws elbv2 describe-load-balancers` ao invés de `kubectl get svc`.

### 3. URIs do API Gateway DEVEM incluir o nome do serviço

Configurado em `infra/api-gateway/main.tf`:
```terraform
uri = "${var.clientes_service_url}/clientes/{proxy}"
```

### 4. Aguardar ALBs ficarem ativos

O script 02 já exibe mensagem de aguardo. Se necessário, executar:
```bash
watch -n 10 'kubectl get ingress'
```

Aguardar até todos mostrarem ADDRESS (DNS do ALB).

---

## ✅ Garantia de Sucesso

Se seguir exatamente esses passos na ordem:

1. 00-cleanup.sh (se necessário)
2. 01-provision-infrastructure.sh
3. 02-deploy.sh
4. **Aguardar 3-5 min**
5. 03-provision-auth.sh
6. 04-test-auth.sh → Deve passar 10/10
7. 05-validate-deployment.sh → Deve ter 0 erros

O deployment será **100% funcional e repetível**.

---

## 📞 Suporte

Em caso de problemas:
1. Consulte a seção Troubleshooting
2. Verifique logs: `kubectl logs -l app={service-name}`
3. Verifique eventos: `kubectl get events --sort-by='.lastTimestamp'`
4. Verifique estado Terraform: `terraform show` em cada módulo

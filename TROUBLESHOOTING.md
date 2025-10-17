# 🔧 Troubleshooting - Sistema de Lanchonete

Este documento contém soluções para problemas comuns encontrados durante o desenvolvimento e deploy dos microserviços.

---

## 📋 ÍNDICE

- [Problemas de Deploy](#problemas-de-deploy)
- [Problemas de Configuração](#problemas-de-configuração)
- [Problemas de Comunicação](#problemas-de-comunicação)
- [Correções Aplicadas](#correções-aplicadas)

---

## 🚀 PROBLEMAS DE DEPLOY

### **1. MySQL - Access Denied (Usuário não existe)**

**Sintoma:**
```
Access denied for user 'lanchonete'@'10.244.0.73' (using password: YES)
```

**Causa Raiz:**
- Deployment estava referenciando secret genérico (`mysql-secret`)
- Cada microserviço tem seu próprio secret MySQL específico
- O usuário `lanchonete` não existe no banco de dados

**Solução:**

1. **Arquivo**: `k8s/services/pedidos-deployment.yaml`
   ```yaml
   # ❌ ANTES (ERRADO)
   - name: MYSQL_USER
     valueFrom:
       secretKeyRef:
         name: mysql-secret      # Secret genérico
         key: username

   # ✅ DEPOIS (CORRETO)
   - name: MYSQL_USER
     valueFrom:
       secretKeyRef:
         name: mysql-pedidos-secret  # Secret específico
         key: MYSQL_USER
   ```

2. **Estrutura de Secrets MySQL:**
   ```
   mysql-clientes-secret:
     - MYSQL_USER: clientes_user
     - MYSQL_PASSWORD: clientes123
     - MYSQL_DATABASE: clientes_db

   mysql-pedidos-secret:
     - MYSQL_USER: pedidos_user
     - MYSQL_PASSWORD: pedidos123
     - MYSQL_DATABASE: pedidos_db

   mysql-cozinha-secret:
     - MYSQL_USER: cozinha_user
     - MYSQL_PASSWORD: cozinha123
     - MYSQL_DATABASE: cozinha_db
   ```

3. **Verificar Secret Correto:**
   ```bash
   # Listar secrets
   kubectl get secrets | grep mysql

   # Ver conteúdo do secret
   kubectl get secret mysql-pedidos-secret -o yaml

   # Decodificar valores
   echo "cGVkaWRvc191c2Vy" | base64 -d  # pedidos_user
   ```

4. **Aplicar Correção:**
   ```bash
   kubectl apply -f k8s/services/pedidos-deployment.yaml
   kubectl rollout restart deployment pedidos-deployment
   ```

**Resultado:**
- ✅ Pods iniciam sem erros de autenticação
- ✅ Nenhuma configuração manual necessária

---

### **2. RabbitMQ - PRECONDITION_FAILED (Exchange Type Mismatch)**

**Sintoma:**
```
Shutdown Signal: channel error; protocol method: #method<channel.close>(
  reply-code=406,
  reply-text=PRECONDITION_FAILED - inequivalent arg 'type' for exchange 'pedido.events' in vhost '/':
  received 'topic' but current is 'direct',
  class-id=40, method-id=10
)
```

**Causa Raiz:**
- Serviço de **Pagamento** usa `DirectExchange`
- Serviço de **Pedidos** estava usando `TopicExchange`
- RabbitMQ não permite recriar exchange com tipo diferente

**Solução:**

1. **Arquivo**: `services/pedidos/src/main/java/.../RabbitMQConfig.java`
   ```java
   // ❌ ANTES (ERRADO)
   @Bean
   public TopicExchange pedidoExchange() {
       return new TopicExchange(pedidoExchange);
   }

   @Bean
   public TopicExchange pagamentoExchange() {
       return new TopicExchange(pagamentoExchange);
   }

   @Bean
   public TopicExchange cozinhaExchange() {
       return new TopicExchange(cozinhaExchange);
   }

   // ✅ DEPOIS (CORRETO)
   @Bean
   public DirectExchange pedidoExchange() {
       return new DirectExchange(pedidoExchange);
   }

   @Bean
   public DirectExchange pagamentoExchange() {
       return new DirectExchange(pagamentoExchange);
   }

   @Bean
   public DirectExchange cozinhaExchange() {
       return new DirectExchange(cozinhaExchange);
   }
   ```

2. **Rebuild da Imagem:**
   ```bash
   cd services/pedidos
   docker build -t pedidos:latest .
   minikube image load pedidos:latest
   ```

3. **Redeploy:**
   ```bash
   kubectl delete deployment pedidos-deployment
   kubectl apply -f k8s/services/pedidos-deployment.yaml
   ```

4. **Verificar Exchanges:**
   ```bash
   kubectl exec rabbitmq-0 -- rabbitmqadmin -u admin -p rabbitmq123 list exchanges | grep -E "pedido|pagamento|cozinha"

   # Resultado esperado:
   | pedido.events      | direct  |
   | pagamento.events   | direct  |
   | cozinha.events     | direct  |
   ```

**Resultado:**
- ✅ Exchanges criados corretamente como `direct`
- ✅ Nenhum erro PRECONDITION_FAILED nos logs

---

### **3. Feign Client - URL e Endpoint Incorretos**

**Sintoma:**
```
Erro ao validar cliente: Connect timed out executing GET http://clientes-service:8083/clientes/12345678900
```

**Causa Raiz:**
- Serviço de Clientes está na porta **8080**, não 8083
- Endpoint correto é `/clientes/cpf/{cpf}`, não `/clientes/{cpf}`

**Solução:**

1. **Arquivo**: `services/pedidos/src/main/resources/application.yml`
   ```yaml
   # ❌ ANTES (ERRADO)
   clientes:
     service:
       url: ${CLIENTES_SERVICE_URL:http://clientes-service:8083}

   # ✅ DEPOIS (CORRETO)
   clientes:
     service:
       url: ${CLIENTES_SERVICE_URL:http://clientes-service:8080}
   ```

2. **Arquivo**: `services/pedidos/src/.../ClienteFeignClient.java`
   ```java
   // ❌ ANTES (ERRADO)
   @GetMapping("/clientes/{cpf}")
   ClienteResponse buscarPorCpf(@PathVariable String cpf);

   // ✅ DEPOIS (CORRETO)
   @GetMapping("/clientes/cpf/{cpf}")
   ClienteResponse buscarPorCpf(@PathVariable String cpf);
   ```

3. **Arquivo**: `k8s/services/pedidos-deployment.yaml`
   ```yaml
   # ✅ SEMPRE usar porta 8080
   - name: CLIENTES_SERVICE_URL
     value: "http://clientes-service:8080"
   ```

4. **Verificar Porta do Serviço:**
   ```bash
   kubectl get svc clientes-service -o yaml | grep port:
   # Resultado: port: 8080
   ```

5. **Testar Endpoint:**
   ```bash
   # Criar pod temporário para teste
   kubectl run curl-pod --image=curlimages/curl:latest --rm -it -- sh

   # Dentro do pod:
   curl http://clientes-service:8080/clientes/cpf/12345678900
   ```

**Resultado:**
- ✅ Integração Feign funcionando
- ✅ Cliente retornado com sucesso
- ✅ Pedido criado com `clienteNome` preenchido

---

## ⚙️ PROBLEMAS DE CONFIGURAÇÃO

### **4. RabbitMQ - Binding Não Criado Automaticamente**

**Sintoma:**
- Exchange `pedido.events` existe
- Queue `pagamentos.pedido-criado` existe
- Mas não há binding entre eles
- Mensagens não chegam ao Pagamento

**Causa Raiz:**
- Serviço de Pagamento não foi reiniciado após correções do RabbitMQ
- Bindings são criados apenas no startup do serviço

**Solução:**

1. **Verificar Bindings:**
   ```bash
   kubectl exec rabbitmq-0 -- rabbitmqadmin -u admin -p rabbitmq123 list bindings -f tsv | grep pedido

   # Se vazio, bindings não foram criados
   ```

2. **Reiniciar Serviço:**
   ```bash
   kubectl rollout restart deployment pagamento-deployment
   sleep 60  # Aguardar pods subirem
   ```

3. **Verificar Novamente:**
   ```bash
   kubectl exec rabbitmq-0 -- rabbitmqadmin -u admin -p rabbitmq123 list bindings -f tsv | grep pedido

   # Resultado esperado:
   pedido.events	pagamentos.pedido-criado	pedido.criado
   ```

4. **Estrutura Completa de Bindings:**
   ```
   Exchange             → Queue                          → Routing Key
   ─────────────────────────────────────────────────────────────────────
   pedido.events       → pagamentos.pedido-criado       → pedido.criado
   pagamento.events    → pedidos.pagamento-aprovado     → pagamento.aprovado
   pagamento.events    → pedidos.pagamento-rejeitado    → pagamento.rejeitado
   cozinha.events      → pedidos.pedido-pronto          → cozinha.pedido-pronto
   ```

**Resultado:**
- ✅ Bindings criados automaticamente no startup
- ✅ Mensagens roteadas corretamente

---

## 📡 PROBLEMAS DE COMUNICAÇÃO

### **5. NodePort - Conflito de Porta**

**Sintoma:**
```
The Service "pedidos-service-nodeport" is invalid: spec.ports[0].nodePort:
Invalid value: 30080: provided port is already allocated
```

**Causa Raiz:**
- Porta 30080 já estava em uso pelo serviço de `autoatendimento`

**Solução:**

1. **Verificar Portas em Uso:**
   ```bash
   kubectl get svc --all-namespaces | grep NodePort
   ```

2. **Mapa de Portas Atual:**
   ```
   Serviço          | NodePort | Porta Interna
   ─────────────────────────────────────────────
   autoatendimento  | 30080    | 8080
   pedidos          | 30081    | 8080
   cozinha          | 30082    | 8082
   clientes         | 30083    | 8080
   pagamento        | 30084    | 8081
   ```

3. **Atualizar NodePort:**
   ```yaml
   # k8s/local/pedidos-service-nodeport.yaml
   spec:
     type: NodePort
     ports:
     - port: 8080
       targetPort: 8080
       nodePort: 30081  # Porta livre
   ```

4. **Aplicar:**
   ```bash
   kubectl apply -f k8s/local/pedidos-service-nodeport.yaml
   ```

**Resultado:**
- ✅ Serviço acessível via `http://192.168.49.2:30081`

---

## ✅ CORREÇÕES APLICADAS

### **Resumo de Todas as Correções**

| # | Problema | Arquivo | Correção |
|---|----------|---------|----------|
| 1 | MySQL Secret | `k8s/services/pedidos-deployment.yaml` | `mysql-secret` → `mysql-pedidos-secret` |
| 2 | RabbitMQ Exchange Type | `services/pedidos/.../RabbitMQConfig.java` | `TopicExchange` → `DirectExchange` |
| 3 | URL Clientes - Porta | `services/pedidos/.../application.yml` | `:8083` → `:8080` |
| 4 | URL Clientes - Endpoint | `services/pedidos/.../ClienteFeignClient.java` | `/clientes/{cpf}` → `/clientes/cpf/{cpf}` |
| 5 | NodePort Conflito | `k8s/local/pedidos-service-nodeport.yaml` | `30080` → `30081` |

---

## 🧪 TESTES DE VALIDAÇÃO

### **1. Testar Integração REST (Pedidos → Clientes)**

```bash
# Criar pedido COM CPF (testa Feign)
curl -X POST http://192.168.49.2:30081/pedidos \
  -H "Content-Type: application/json" \
  -d '{
    "cpfCliente": "12345678900",
    "itens": [
      {"produtoId": 1, "quantidade": 2}
    ]
  }'

# Resposta esperada:
{
  "id": 4,
  "numeroPedido": "PED-000004",
  "cpfCliente": "12345678900",
  "clienteNome": "Teste Cliente",  ← Buscado via Feign!
  "status": "CRIADO",
  ...
}
```

### **2. Testar Integração RabbitMQ (Pedidos ↔ Pagamento)**

```bash
# 1. Criar pedido
curl -s -X POST http://192.168.49.2:30081/pedidos \
  -H "Content-Type: application/json" \
  -d '{"cpfCliente":null,"itens":[{"produtoId":1,"quantidade":1}]}' \
  | jq -r '.id,.status'
# Output: 9
#         CRIADO

# 2. Aguardar processamento (5s)
sleep 5

# 3. Verificar status atualizado
curl -s http://192.168.49.2:30081/pedidos/9 | jq -r '.status'
# Output: REALIZADO  ← Mudou via RabbitMQ!
```

### **3. Verificar Health Checks**

```bash
# Pedidos
curl http://192.168.49.2:30081/actuator/health | jq '.status,.components.db.status,.components.rabbit.status'
# Output: "UP"
#         "UP"
#         "UP"

# Clientes
curl http://192.168.49.2:30083/actuator/health | jq '.status,.components.db.status'
# Output: "UP"
#         "UP"

# Pagamento
kubectl run curl-test --image=curlimages/curl:latest --rm -it -- \
  curl -s http://pagamento-service:8081/actuator/health | jq '.status'
# Output: "UP"
```

---

## 🔍 COMANDOS ÚTEIS DE DEBUG

### **Logs**

```bash
# Ver logs de um serviço
kubectl logs -l app=pedidos --tail=100

# Ver logs de um pod específico
kubectl logs pedidos-deployment-79ddbcbf74-4695v

# Seguir logs em tempo real
kubectl logs -f -l app=pedidos

# Ver eventos do pod
kubectl describe pod <pod-name>
```

### **RabbitMQ**

```bash
# Listar exchanges
kubectl exec rabbitmq-0 -- rabbitmqadmin -u admin -p rabbitmq123 list exchanges

# Listar queues
kubectl exec rabbitmq-0 -- rabbitmqadmin -u admin -p rabbitmq123 list queues

# Listar bindings
kubectl exec rabbitmq-0 -- rabbitmqadmin -u admin -p rabbitmq123 list bindings

# Verificar mensagens em uma fila
kubectl exec rabbitmq-0 -- rabbitmqadmin -u admin -p rabbitmq123 get queue=pagamentos.pedido-criado count=5
```

### **MySQL**

```bash
# Conectar ao MySQL
kubectl exec -it mysql-pedidos-0 -- mysql -u root -p

# Verificar usuários
kubectl exec mysql-pedidos-0 -- mysql -u root -proot123 -e "SELECT User, Host FROM mysql.user;"

# Verificar databases
kubectl exec mysql-pedidos-0 -- mysql -u root -proot123 -e "SHOW DATABASES;"

# Ver tabelas
kubectl exec mysql-pedidos-0 -- mysql -u root -proot123 pedidos_db -e "SHOW TABLES;"
```

### **Secrets**

```bash
# Listar secrets
kubectl get secrets

# Ver conteúdo de um secret
kubectl get secret mysql-pedidos-secret -o yaml

# Decodificar valor
kubectl get secret mysql-pedidos-secret -o jsonpath='{.data.MYSQL_USER}' | base64 -d
```

### **Pods e Services**

```bash
# Listar todos os pods
kubectl get pods

# Ver status detalhado
kubectl get pods -o wide

# Listar services
kubectl get svc

# Porta forward para teste local
kubectl port-forward svc/pedidos-service 8080:8080
```

---

## 📊 CHECKLIST DE VALIDAÇÃO

Após aplicar correções, validar:

- [ ] ✅ Pods todos em status `Running` (1/1 Ready)
- [ ] ✅ Logs sem erros de `PRECONDITION_FAILED`
- [ ] ✅ Logs sem erros de `Access denied`
- [ ] ✅ Health checks retornando `UP`
- [ ] ✅ Exchanges criados como `direct`
- [ ] ✅ Bindings criados corretamente
- [ ] ✅ Integração Feign funcionando (clienteNome preenchido)
- [ ] ✅ Integração RabbitMQ funcionando (status muda para REALIZADO)
- [ ] ✅ Endpoints acessíveis via NodePort

---

## 🎯 RESULTADO FINAL

Após aplicar todas as correções:

```
✅ Clientes (2 pods)   - Porta 8080 - NodePort 30083
✅ Pagamento (2 pods)  - Porta 8081 - NodePort 30084
✅ Pedidos (2 pods)    - Porta 8080 - NodePort 30081
✅ RabbitMQ (1 pod)    - Exchanges direct
✅ MySQL Clientes      - Usuário: clientes_user
✅ MySQL Pedidos       - Usuário: pedidos_user

TODAS AS INTEGRAÇÕES FUNCIONANDO:
✅ REST: Pedidos → Clientes (via Feign)
✅ Eventos: Pedidos ↔ Pagamento (via RabbitMQ)
✅ Deploy: Automático sem configuração manual
```

---

**Última atualização:** 2025-10-17

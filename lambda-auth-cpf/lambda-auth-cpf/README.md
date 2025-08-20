# Lambda Auth CPF - Tech Challenge Fase 3

Lambda function para autenticação por CPF (com suporte a cliente anônimo) usando AWS Academy.

## 🎯 Funcionalidades

- ✅ **Login com CPF**: Identifica cliente existente e gera JWT
- ✅ **Login anônimo**: Permite acesso sem CPF (CPF vazio/null)
- ✅ **Validação de formato**: CPF deve ter 11 dígitos numéricos
- ✅ **JWT personalizado**: Diferentes claims para cliente identificado vs anônimo
- ✅ **Clean Architecture**: Domain, Application, Adapters
- ✅ **Testes completos**: 11 testes unitários com padrão do projeto

## 🏗️ Arquitetura

```
API Gateway → Lambda Handler → AuthService → IdentificarCliente → ClienteMockGateway
                                     ↓
                               JwtService
```

## 📋 Pré-requisitos

- AWS Academy com LabRole ativa
- Java 17
- Maven 3.8+
- Terraform 1.0+ (para deploy)

## 🚀 Deploy

### 1. Build do projeto
```bash
mvn clean package
```

### 2. Configurar credenciais AWS Academy
```bash
# No AWS Academy, clicar em "AWS Details" e configurar:
aws configure set aws_access_key_id AKIA...
aws configure set aws_secret_access_key wJal...
aws configure set aws_session_token IQoJ...
aws configure set region us-east-1

# Verificar configuração
aws sts get-caller-identity
aws iam get-role --role-name LabRole
```

### 3. Deploy com Terraform
```bash
cd terraform
terraform init
terraform plan
terraform apply
```

### 4. Obter URL da API
```bash
terraform output api_gateway_url
```

## 🧪 Testes

### Executar testes unitários
```bash
mvn test
```

### Testes da API (após deploy)

```bash
API_URL="https://your-api-id.execute-api.us-east-1.amazonaws.com/dev/auth/cpf"

# Teste 1: Login anônimo (CPF vazio)
curl -X POST $API_URL \
  -H "Content-Type: application/json" \
  -d '{"cpf": ""}'

# Teste 2: Cliente existente
curl -X POST $API_URL \
  -H "Content-Type: application/json" \
  -d '{"cpf": "12345678901"}'

# Teste 3: CPF não encontrado
curl -X POST $API_URL \
  -H "Content-Type: application/json" \
  -d '{"cpf": "99999999999"}'

# Teste 4: Erro - sem campo CPF
curl -X POST $API_URL \
  -H "Content-Type: application/json" \
  -d '{}'

# Teste 5: Erro - CPF formato inválido
curl -X POST $API_URL \
  -H "Content-Type: application/json" \
  -d '{"cpf": "123"}'
```

## 📊 Respostas Esperadas

### ✅ Sucesso - Cliente Anônimo
```json
{
  "success": true,
  "cliente": {
    "id": null,
    "cpf": null,
    "nome": "Cliente Anônimo",
    "email": null
  },
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "expiresIn": 3600
}
```

### ✅ Sucesso - Cliente Identificado
```json
{
  "success": true,
  "cliente": {
    "id": 1,
    "cpf": "12345678901",
    "nome": "Maria Oliveira",
    "email": "maria.oliveira@email.com"
  },
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "expiresIn": 3600
}
```

### ❌ Erro - CPF não encontrado
```json
{
  "success": false,
  "error": "CPF não encontrado"
}
```

## 🗃️ Dados Mock

O sistema vem com 5 clientes pré-cadastrados para teste:

- `12345678901` - Maria Oliveira
- `11144477735` - João Silva  
- `98765432100` - Pedro Costa
- `11111111111` - Ana Santos
- `22222222222` - Carlos Ferreira

## 🔧 Configurações AWS Academy

### Recursos criados:
- **Lambda Function**: `lanchonete-auth-cpf-dev`
- **API Gateway**: `lanchonete-auth-api`  
- **CloudWatch Logs**: `/aws/lambda/lanchonete-auth-cpf-dev`

### Limites Academy respeitados:
- Memory: 512MB (≤ 1GB)
- Timeout: 30s (≤ 60s)
- IAM: Usa LabRole existente

## 🧹 Cleanup

```bash
cd terraform
terraform destroy
```

## 📈 Monitoramento

```bash
# Ver logs da Lambda
aws logs tail "/aws/lambda/lanchonete-auth-cpf-dev" --follow

# Métricas da Lambda
aws lambda get-function --function-name lanchonete-auth-cpf-dev
```

## 🎓 Tech Challenge - Requisitos Atendidos

- ✅ **API Gateway** para receber solicitações externas
- ✅ **Function serverless** para autenticar por CPF  
- ✅ **Identificação apenas por CPF** (sem senha)
- ✅ **JWT** para fluxo de integração
- ✅ **Infraestrutura Terraform**
- ✅ **Clean Architecture** preservada
- ✅ **Testes unitários** completos

---

**Desenvolvido para:** Tech Challenge SOAT - Fase 3  
**Arquitetura:** AWS Lambda + API Gateway + Terraform  
**Ambiente:** AWS Academy com LabRole
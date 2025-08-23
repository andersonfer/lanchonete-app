# 🗄️ FASE 3: RDS Setup - Tech Challenge

## 📋 Estrutura do Projeto

```
terraform-rds/
├── main.tf                 # Provider + configuração Academy
├── variables.tf           # Variáveis com validações Academy  
├── rds.tf                # Instância RDS + Security Groups
├── outputs.tf            # Endpoints e informações de conexão
├── terraform.tfvars      # Configurações específicas
├── migrations/
│   ├── 001_create_tables.sql  # Estrutura das tabelas
│   └── 002_insert_data.sql    # Dados iniciais (10 produtos)
└── README.md             # Este arquivo
```

## 🚀 Deploy da Infraestrutura

### **1. Configurar Credenciais AWS Academy**
```bash
# Configurar credenciais temporárias do Academy
aws configure set aws_access_key_id AKIA...
aws configure set aws_secret_access_key wJal...  
aws configure set aws_session_token IQoJ...
aws configure set region us-east-1

# Verificar configuração
aws sts get-caller-identity
```

### **2. Deploy do RDS**
```bash
# Navegar para o diretório
cd terraform-rds

# Inicializar Terraform
terraform init

# Planejar deployment
terraform plan

# Aplicar recursos (RDS leva ~5-10 minutos para criar)
terraform apply -auto-approve

# Pegar informações de conexão
terraform output
```

## 🔧 Executar Scripts de Migração

### **3. Aguardar RDS Disponível**
```bash
# Verificar status do RDS
aws rds describe-db-instances \
  --db-instance-identifier lanchonete-dev \
  --query 'DBInstances[0].DBInstanceStatus'

# Deve retornar "available"
```

### **4. Executar Scripts MySQL**
```bash
# Pegar endpoint do RDS
RDS_ENDPOINT=$(terraform output -raw rds_endpoint)
echo "RDS Endpoint: $RDS_ENDPOINT"

# Executar criação de tabelas
mysql -h $RDS_ENDPOINT -P 3306 -u admin -p lanchonete < migrations/001_create_tables.sql

# Executar inserção de dados  
mysql -h $RDS_ENDPOINT -P 3306 -u admin -p lanchonete < migrations/002_insert_data.sql

# Senha: LanchoneteDB123!
```

## 🧪 Validar Dados

### **5. Testar Conexão e Dados**
```bash
# Conectar ao banco
mysql -h $RDS_ENDPOINT -P 3306 -u admin -p lanchonete

# Dentro do MySQL:
SHOW TABLES;
SELECT COUNT(*) FROM produto;  -- Deve retornar 10
SELECT * FROM produto WHERE categoria = 'LANCHE';
SELECT * FROM produto WHERE categoria = 'BEBIDA';
```

## 📊 Configurações Aplicadas

### **Instância RDS:**
- **Engine:** MySQL 8.0
- **Classe:** db.t3.micro (Free Tier Academy)
- **Storage:** 20GB GP2 com criptografia
- **Backup:** 1 dia de retenção
- **Multi-AZ:** Desabilitado (Academy)
- **Acesso público:** Não

### **Security Group:**
- **Entrada:** Porta 3306 de qualquer IP (0.0.0.0/0)
- **Saída:** Todo tráfego liberado

### **Dados Inseridos:**
- **10 produtos** distribuídos em 4 categorias
- **LANCHE:** X-Burger (R$ 18,90)
- **ACOMPANHAMENTO:** Batata Frita P/G, Onion Rings
- **BEBIDA:** Refrigerante, Suco Natural, Água Mineral  
- **SOBREMESA:** Pudim, Sorvete, Brownie

## ⚠️ Notas Importantes

1. **Credenciais Academy:** Renovar a cada 4-12 horas
2. **Tempo de criação:** RDS leva ~5-10 minutos para ficar disponível
3. **Senha padrão:** `LanchoneteDB123!` (alterar em produção)
4. **Cleanup:** Executar `terraform destroy` ao final dos testes
5. **Custos:** $0 no Academy (Free Tier)

## 🔄 Próximos Passos

Após validar o RDS:
1. **OPÇÃO A:** Integrar Lambda Produtos com RDS (substituir mocks)
2. **OPÇÃO B:** Continuar para FASE 4 (Gestão de Pedidos)
3. **OPÇÃO C:** Implementar FASE 5 (Sistema de Pagamento)
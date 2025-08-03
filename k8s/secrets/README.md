# 🔐 Gestão de Secrets - Tech Challenge Fase 2

Este diretório contém scripts para gerenciar secrets do Kubernetes de forma segura, **sem senhas hardcoded no código**.

## 📋 Arquivos

- `create_secrets.sh` - Script principal (usa variáveis de ambiente)
- `create_secrets.sh.example` - Template para novos desenvolvedores
- `README.md` - Este arquivo

## 🚀 Setup Rápido

### 1. Primeira vez (desenvolvedor principal)

```bash
# 1. Definir variáveis de ambiente
export MYSQL_ROOT_PASSWORD="sua_senha_root_aqui"
export MYSQL_USER_PASSWORD="sua_senha_user_aqui"

# 2. Executar script
./create_secrets.sh
```

### 2. Novos desenvolvedores

```bash
# 1. Copiar template (se necessário)
cp create_secrets.sh.example create_secrets.sh

# 2. Configurar variáveis (ver seção "Variáveis Necessárias")
export MYSQL_ROOT_PASSWORD="mesma_senha_da_equipe"
export MYSQL_USER_PASSWORD="mesma_senha_da_equipe"

# 3. Executar
./create_secrets.sh
```

## 🔧 Variáveis Necessárias

| Variável | Obrigatória | Padrão | Descrição |
|----------|-------------|---------|-----------|
| `MYSQL_ROOT_PASSWORD` | ✅ | - | Senha do usuário root do MySQL |
| `MYSQL_USER_PASSWORD` | ✅ | - | Senha do usuário da aplicação |
| `MYSQL_USER` | ❌ | `lanchonete` | Nome do usuário da aplicação |
| `DB_USERNAME` | ❌ | `lanchonete` | Username para apps se conectarem |
| `DB_PASSWORD` | ❌ | `$MYSQL_USER_PASSWORD` | Password para apps se conectarem |

## 📊 Comandos Úteis

### Verificar secrets existentes
```bash
# Listar todos os secrets
kubectl get secrets

# Detalhes do secret MySQL (não mostra valores)
kubectl describe secret mysql-secret

# Ver chaves disponíveis
kubectl get secret mysql-secret -o jsonpath='{.data}' | jq keys
```

### Testar conectividade
```bash
# Encontrar pod do MySQL
kubectl get pods -l app=mysql

# Testar conexão root
kubectl exec -it mysql-statefulset-0 -- mysql -u root -p$MYSQL_ROOT_PASSWORD -e "SELECT 'Root OK';"

# Testar conexão usuário app
kubectl exec -it mysql-statefulset-0 -- mysql -u lanchonete -p$MYSQL_USER_PASSWORD -e "SELECT 'App User OK';"
```

### Recriar secrets (se necessário)
```bash
# Deletar secret existente
kubectl delete secret mysql-secret

# Recriar com script
./create_secrets.sh
```

## 🛡️ Segurança

### ✅ Boas Práticas
- ✅ Senhas vêm de variáveis de ambiente
- ✅ Nenhuma senha commitada no Git
- ✅ Script valida variáveis antes de executar
- ✅ Values sensíveis não aparecem em logs

### ⚠️ Cuidados
- **NUNCA** commite o arquivo `create_secrets.sh` com senhas reais
- **SEMPRE** use o template `.example` para compartilhar
- **COMPARTILHE** senhas por canais seguros (Slack DM, etc.)
- **ROTACIONE** senhas periodicamente

## 🐛 Troubleshooting

### Erro: "Variável não está definida"
```bash
# Verificar se variáveis estão carregadas
echo $MYSQL_ROOT_PASSWORD

# Se vazio, definir novamente
export MYSQL_ROOT_PASSWORD="sua_senha"
```

### Secret já existe
```bash
# O script recria automaticamente, mas se der erro:
kubectl delete secret mysql-secret
./create_secrets.sh
```

### MySQL não conecta
```bash
# Verificar se pod está rodando
kubectl get pods -l app=mysql

# Verificar logs do MySQL
kubectl logs mysql-statefulset-0

# Verificar se secret tem as chaves corretas
kubectl describe secret mysql-secret
```

### Apps não conectam no MySQL
```bash
# Verificar logs das aplicações
kubectl logs deployment/autoatendimento-deployment
kubectl logs deployment/pagamento-deployment

# Verificar se services estão funcionando
kubectl get services -l app=mysql
```

## 🔄 Workflow da Equipe

1. **Primeiro dev** define senhas e cria secrets
2. **Compartilha** senhas com equipe (canal seguro)
3. **Outros devs** usam mesmas senhas localmente
4. **Todos** conseguem recriar secrets idênticos
5. **Deploy** funciona para qualquer membro da equipe

---

**💡 Dica:** Para setup completo do projeto, veja `docs/setup-secrets.md`

# BACKLOG - Migração para Repositórios Separados

## Contexto

Migração do monorepo `lanchonete-app` para 5 repositórios independentes, separando os 4 microserviços e a infraestrutura compartilhada.

### Repositórios a Criar

| Repositório | Conteúdo |
|-------------|----------|
| `lanchonete-clientes` | Serviço de clientes + k8s + CI/CD |
| `lanchonete-pedidos` | Serviço de pedidos + k8s + CI/CD |
| `lanchonete-cozinha` | Serviço de cozinha + k8s + CI/CD |
| `lanchonete-pagamento` | Serviço de pagamento + k8s + CI/CD |
| `lanchonete-infra` | Terraform + k8s compartilhado (RabbitMQ, MongoDB, secrets) |

---

## Backlog

### Criar repositório lanchonete-infra
- Criar repositório no GitHub
- Copiar módulos Terraform (backend, ecr, kubernetes, database, auth, lambda, api-gateway)
- Copiar manifests k8s compartilhados (RabbitMQ, MongoDB, secrets)
- Copiar scripts de deploy e documentação
- Configurar secrets do GitHub (AWS credentials)
- Validar que Terraform continua funcional

### Criar repositório lanchonete-clientes
- Criar repositório no GitHub
- Copiar código do serviço (src, pom.xml, Dockerfile)
- Copiar manifests k8s específicos (deployment, service, configmap, ingress, hpa)
- Adaptar workflows CI/CD (remover path filters)
- Configurar secrets do GitHub (AWS, SonarCloud)
- Validar CI (criar PR de teste)
- Validar CD (merge e verificar deploy)

### Criar repositório lanchonete-pagamento
- Criar repositório no GitHub
- Copiar código do serviço (src, pom.xml, Dockerfile)
- Copiar manifests k8s específicos (deployment, service, configmap, ingress, hpa)
- Adaptar workflows CI/CD (remover path filters)
- Configurar secrets do GitHub (AWS, SonarCloud)
- Validar CI (criar PR de teste)
- Validar CD (merge e verificar deploy)

### Criar repositório lanchonete-pedidos
- Criar repositório no GitHub
- Copiar código do serviço (src, pom.xml, Dockerfile)
- Copiar manifests k8s específicos (deployment, service, configmap, ingress, hpa)
- Adaptar workflows CI/CD (remover path filters)
- Configurar secrets do GitHub (AWS, SonarCloud)
- Validar CI (criar PR de teste)
- Validar CD (merge e verificar deploy)

### Criar repositório lanchonete-cozinha
- Criar repositório no GitHub
- Copiar código do serviço (src, pom.xml, Dockerfile)
- Copiar manifests k8s específicos (deployment, service, configmap, ingress, hpa)
- Adaptar workflows CI/CD (remover path filters)
- Configurar secrets do GitHub (AWS, SonarCloud)
- Validar CI (criar PR de teste)
- Validar CD (merge e verificar deploy)

### Validação final e documentação
- Testar comunicação entre serviços no ambiente
- Atualizar README de cada repositório
- Adicionar usuário soat-architecture como colaborador em todos os repos
- Arquivar/deprecar monorepo original

---

## Secrets do GitHub (replicar em cada repo de serviço)

| Secret | Descrição |
|--------|-----------|
| `AWS_ACCESS_KEY_ID` | Credencial AWS |
| `AWS_SECRET_ACCESS_KEY` | Credencial AWS |
| `AWS_SESSION_TOKEN` | Token AWS Academy |
| `SONAR_TOKEN` | Token SonarCloud |

---

## Estrutura Final dos Repositórios

### Repositório de Serviço (padrão)

```
lanchonete-{service}/
├── src/
├── pom.xml
├── Dockerfile
├── k8s/
│   ├── deployment.yaml
│   ├── service.yaml
│   ├── configmap.yaml
│   ├── ingress.yaml
│   └── hpa.yaml
├── .github/workflows/
│   ├── ci.yml
│   └── cd.yml
└── README.md
```

### Repositório de Infraestrutura

```
lanchonete-infra/
├── terraform/
│   ├── backend/
│   ├── ecr/
│   ├── kubernetes/
│   ├── database/
│   ├── auth/
│   ├── lambda/
│   └── api-gateway/
├── k8s/
│   ├── rabbitmq-statefulset.yaml
│   ├── mongodb-statefulset.yaml
│   └── create-secrets.sh
└── README.md
```

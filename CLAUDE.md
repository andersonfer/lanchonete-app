Crie um projeto AWS POC com as seguintes especificações:

1. **Infraestrutura**
   - Banco MySQL migrado para **AWS RDS**.
   - Cluster **EKS** para rodar a aplicação Kubernetes.
   - Sempre usar **LabRole** para todas as operações AWS.
   - Apenas um ambiente, não é necessário nomear dev/prod.
   - Estrutura mínima, simples e funcional para POC.

2. **Aplicação**
   - Dois serviços:
     a) **autoatendimento**: acessa o banco MySQL.
     b) **pagamento**: callback simples para autoatendimento, não acessa o banco.
   - Ambos rodando no **mesmo namespace**.
   - Integrar os **manifests Kubernetes existentes**, organizando por serviço:
     - `k8s-manifests/autoatendimento`
     - `k8s-manifests/pagamento`
   - **Remover todos os manifests relacionados a MySQL ou storage** (StatefulSets, PVC, PV, ConfigMaps ou Secrets antigos do banco).
   - Adicionar apenas arquivos novos quando necessário, por exemplo:
     - `secret.yaml` para autoatendimento (placeholder para pipeline injetar credenciais do RDS).
   
3. **CI/CD (pré-configuração)**
   - Pipelines separados (infra Kubernetes, infra banco, aplicação), mas apenas estruturar.
   - Coordenação via **outputs/artefatos**:
     - RDS gera endpoint que pipeline da aplicação consome para criar Secret Kubernetes.
   - Pipeline de aplicação deve criar Secret Kubernetes para autoatendimento, mas **não versionar senhas no Git**.

4. **Estrutura de diretórios sugerida**
   - `app/` → código da aplicação (`autoatendimento`, `pagamento`)
   - `infra/` → Terraform (`kubernetes/`, `database/`)
   - `k8s-manifests/` → organizar manifests existentes por serviço, remover manifests de MySQL/storage, adicionar placeholders novos se necessário
   - `.github/workflows/` → pipelines separados (infra EKS, infra RDS, app autoatendimento, app pagamento)

5. **Segurança**
   - Apenas autoatendimento terá Secret (DB credentials).
   - Pagamento não precisa de Secret.
   - Secrets devem ser injetados pelo pipeline, nunca hardcoded ou versionados no Git.

6. **Outputs esperados**
   - Estrutura inicial de pastas e arquivos, integrando manifests existentes.
   - Manifests de MySQL/storage removidos.
   - Exemplos de `main.tf` Terraform para EKS e RDS.
   - Exemplo de `secret.yaml` para autoatendimento (placeholder para pipeline injetar credenciais).
   - Comentários indicando onde o pipeline deve injetar outputs do Terraform.
- sempre fale em portugues brasileiro
- o codigo deve estar em portugues brasileiro sempre que possivel
- Garantir que todas as operações AWS sejam feitas usando **LabRole**.
- Adicionalmente, para simplificação e boas práticas do Terraform:

1. **Variável `nome_projeto`**
   - Evitar repetição excessiva em nomes de recursos (cluster, node group, security group).
   - Criar **uma única variável ou local** que contenha o prefixo padrão, e reutilizar em todos os recursos.
   - Por exemplo, usar `local.prefix = var.nome_projeto` ou similar, concatenando apenas quando necessário.

2. **LabRole**
   - Garantir que todos os recursos AWS (EKS cluster, Node Group) usem `data.aws_iam_role.lab_role.arn` corretamente.

3. **Tags**
   - Continuar usando `locals.common_tags` para todas as tags.

4. **Subnets**
   - Obter todas as subnets da VPC dinamicamente, **sem filtrar AZs específicas**.

5. **Ambiente**
   - Não anotar em nenhum lugar o tipo de ambiente (dev, prod, etc.).
- os comandos terraform apply sou eu que executo e te copio a saída
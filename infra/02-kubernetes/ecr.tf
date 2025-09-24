# ==============================================================================
# REPOSITÓRIOS ECR - Container Registry
# ==============================================================================

# Repositórios ECR para cada serviço
resource "aws_ecr_repository" "repos" {
  count = length(local.servicos)

  name                 = "${local.prefix}-${local.servicos[count.index]}"
  image_tag_mutability = "MUTABLE" # Permite sobrescrever tags (bom para POC)

  # Configuração de scanning de imagens
  image_scanning_configuration {
    scan_on_push = false # Desabilitado para POC (economiza custo e tempo)
  }

  tags = merge(
    local.common_tags,
    {
      Name    = "${local.prefix}-${local.servicos[count.index]}"
      Servico = local.servicos[count.index]
    }
  )
}

# Política de ciclo de vida para limpar imagens antigas (economiza custo)
resource "aws_ecr_lifecycle_policy" "cleanup" {
  count      = length(local.servicos)
  repository = aws_ecr_repository.repos[count.index].name

  policy = jsonencode({
    rules = [
      {
        rulePriority = 1
        description  = "Manter apenas as 10 imagens mais recentes"
        selection = {
          tagStatus   = "untagged"
          countType   = "imageCountMoreThan"
          countNumber = 1
        }
        action = {
          type = "expire"
        }
      },
      {
        rulePriority = 2
        description  = "Manter apenas as 10 tags mais recentes"
        selection = {
          tagStatus     = "tagged"
          tagPrefixList = ["v", "latest", "main", "feature"]
          countType     = "imageCountMoreThan"
          countNumber   = 10
        }
        action = {
          type = "expire"
        }
      }
    ]
  })
}

# Outputs do ECR movidos para outputs.tf
# ECR Repositories
resource "aws_ecr_repository" "autoatendimento" {
  name = "lanchonete-autoatendimento"
  
  image_scanning_configuration {
    scan_on_push = false
  }

  tags = {
    Name = "lanchonete-autoatendimento"
  }
}

resource "aws_ecr_repository" "pagamento" {
  name = "lanchonete-pagamento"
  
  image_scanning_configuration {
    scan_on_push = false
  }

  tags = {
    Name = "lanchonete-pagamento"
  }
}

# Docker Provider Configuration (declarado no main.tf)

provider "docker" {
  registry_auth {
    address  = "${data.aws_caller_identity.current.account_id}.dkr.ecr.us-east-1.amazonaws.com"
    username = "AWS"
    password = data.aws_ecr_authorization_token.token.password
  }
}

data "aws_ecr_authorization_token" "token" {
  registry_id = data.aws_caller_identity.current.account_id
}

data "aws_caller_identity" "current" {}

# Build e Push Autoatendimento
resource "docker_image" "autoatendimento" {
  name = "${aws_ecr_repository.autoatendimento.repository_url}:latest"
  
  build {
    context    = "../../applications/autoatendimento"
    dockerfile = "Dockerfile"
  }
  
  triggers = {
    dir_sha1 = sha1(join("", [for f in fileset("../../applications/autoatendimento", "**") : filesha1("../../applications/autoatendimento/${f}")]))
  }

  depends_on = [aws_ecr_repository.autoatendimento]
}

resource "docker_registry_image" "autoatendimento" {
  name = docker_image.autoatendimento.name
  
  depends_on = [docker_image.autoatendimento]
}

# Build e Push Pagamento
resource "docker_image" "pagamento" {
  name = "${aws_ecr_repository.pagamento.repository_url}:latest"
  
  build {
    context    = "../../applications/pagamento"
    dockerfile = "Dockerfile"
  }
  
  triggers = {
    dir_sha1 = sha1(join("", [for f in fileset("../../applications/pagamento", "**") : filesha1("../../applications/pagamento/${f}")]))
  }

  depends_on = [aws_ecr_repository.pagamento]
}

resource "docker_registry_image" "pagamento" {
  name = docker_image.pagamento.name
  
  depends_on = [docker_image.pagamento]
}
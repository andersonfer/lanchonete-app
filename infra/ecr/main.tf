# Configuração do Terraform
terraform {
  required_version = ">= 1.0"

  backend "s3" {
    bucket         = "lanchonete-terraform-state-poc"
    key            = "ecr/terraform.tfstate"
    region         = "us-east-1"
    dynamodb_table = "lanchonete-terraform-locks"
  }

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

# Provider AWS - usa LabRole automaticamente via AWS CLI/credentials
provider "aws" {
  region = var.regiao_aws
}

# Variáveis essenciais
variable "regiao_aws" {
  description = "Região AWS para deploy"
  type        = string
  default     = "us-east-1"
}

variable "nome_projeto" {
  description = "Nome do projeto"
  type        = string
  default     = "lanchonete"
}

# Data sources para repositórios ECR existentes - não gerenciamos, apenas consultamos
data "aws_ecr_repository" "autoatendimento" {
  name = "lanchonete-autoatendimento"
}

data "aws_ecr_repository" "pagamento" {
  name = "lanchonete-pagamento"
}

# Registry URL para facilitar build
data "aws_caller_identity" "current" {}

locals {
  registry_url = "${data.aws_caller_identity.current.account_id}.dkr.ecr.${var.regiao_aws}.amazonaws.com"
}

# Outputs consolidados
output "registry_url" {
  description = "URL base do registry ECR"
  value       = local.registry_url
}

output "repositorios_ecr" {
  description = "URLs dos repositórios ECR"
  value = {
    autoatendimento = data.aws_ecr_repository.autoatendimento.repository_url
    pagamento       = data.aws_ecr_repository.pagamento.repository_url
  }
}

output "repositorios_nomes" {
  description = "Nomes dos repositórios ECR"
  value = {
    autoatendimento = data.aws_ecr_repository.autoatendimento.name
    pagamento       = data.aws_ecr_repository.pagamento.name
  }
}

# Outputs específicos para o pipeline CI/CD
output "ecr_autoatendimento_url" {
  description = "URL do repositório ECR do autoatendimento"
  value       = data.aws_ecr_repository.autoatendimento.repository_url
}

output "ecr_pagamento_url" {
  description = "URL do repositório ECR do pagamento"
  value       = data.aws_ecr_repository.pagamento.repository_url
}
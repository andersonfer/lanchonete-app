# ==============================================================================
# KUBERNETES INFRASTRUCTURE - EKS + ECR + ALB Controller
# Módulo consolidado para toda infraestrutura Kubernetes
# ==============================================================================

# Configuração do Terraform
terraform {
  required_version = ">= 1.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.0"
    }
    helm = {
      source  = "hashicorp/helm"
      version = "~> 2.0"
    }
  }
}

# Provider AWS
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

variable "cluster_name" {
  description = "Nome do cluster EKS"
  type        = string
  default     = "lanchonete-cluster"
}

variable "servicos" {
  description = "Lista dos serviços que precisam de repositórios ECR"
  type        = list(string)
  default     = ["autoatendimento", "pagamento"]
}

# Configurações locais consolidadas
locals {
  prefix   = var.nome_projeto
  servicos = var.servicos

  common_tags = {
    Projeto   = var.nome_projeto
    ManagedBy = "terraform"
  }
}# Pipeline 2 test trigger Wed Sep 24 12:28:33 PM -03 2025

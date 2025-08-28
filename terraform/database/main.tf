# =============================================================================
# PROVIDER E BACKEND - MÓDULO DATABASE
# =============================================================================
# Configurações base para o módulo de banco de dados
# Importa configurações do módulo shared para reutilização

# -----------------------------------------------------------------------------
# TERRAFORM CONFIGURATION
# -----------------------------------------------------------------------------

terraform {
  required_version = ">= 1.0"
  
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
  
  # Backend local para desenvolvimento
  # Em produção, usar S3 + DynamoDB
  backend "local" {
    path = "./terraform.tfstate"
  }
}

# -----------------------------------------------------------------------------
# PROVIDER AWS
# -----------------------------------------------------------------------------

provider "aws" {
  region = "us-east-1"
}
terraform {
  required_version = ">= 1.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
  
  default_tags {
    tags = merge(var.default_tags, {
      Environment = "aws-academy"
      ManagedBy   = "terraform"
    })
  }
}

# Dados da conta Academy
data "aws_caller_identity" "current" {}
data "aws_region" "current" {}

# LabRole - obrigatória no Academy
data "aws_iam_role" "lab_role" {
  name = "LabRole"
}

# Outputs informativos sobre Academy
output "academy_info" {
  description = "Informações do ambiente Academy"
  value = {
    account_id = data.aws_caller_identity.current.account_id
    region     = data.aws_region.current.name
    lab_role   = data.aws_iam_role.lab_role.arn
    using_academy = true
  }
}
# Data sources compartilhados para AWS Academy
# Lanchonete Tech Challenge Fase 3

# LabRole - única role disponível no AWS Academy
data "aws_iam_role" "lab_role" {
  name = "LabRole"
}

# VPC padrão obrigatória no Academy
data "aws_vpc" "default" {
  default = true
}

# Subnets da VPC padrão
data "aws_subnets" "default" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }
}

# Availability Zones disponíveis
data "aws_availability_zones" "available" {
  state = "available"
}

# Informações da conta AWS atual
data "aws_caller_identity" "current" {}

# Informações da região atual
data "aws_region" "current" {}
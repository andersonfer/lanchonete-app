# =============================================================================
# DATA SOURCES - RECURSOS AWS EXISTENTES
# =============================================================================
# Este arquivo busca informações de recursos que já existem na AWS Academy
# e que não podem ser criados pelo Terraform (LabRole, VPC default, etc)

# -----------------------------------------------------------------------------
# IAM ROLE (AWS ACADEMY)
# -----------------------------------------------------------------------------

# LabRole: Única role disponível no AWS Academy
# Usada por Lambda, EKS, RDS e outros serviços AWS
data "aws_iam_role" "lab_role" {
  name = "LabRole"
}

# -----------------------------------------------------------------------------
# NETWORKING (VPC DEFAULT)
# -----------------------------------------------------------------------------

# VPC Default: AWS Academy não permite criar VPCs customizadas
# Precisamos usar a VPC padrão que já existe
data "aws_vpc" "default" {
  default = true
}

# Subnets da VPC default
# Automaticamente descobre todas as subnets disponíveis
data "aws_subnets" "default" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }
  
  filter {
    name   = "state"
    values = ["available"]
  }
}

# Informações detalhadas das subnets (para verificar AZs)
data "aws_subnet" "default" {
  for_each = toset(data.aws_subnets.default.ids)
  id       = each.value
}

# -----------------------------------------------------------------------------
# AVAILABILITY ZONES
# -----------------------------------------------------------------------------

# Lista todas as AZs disponíveis na região us-east-1
# Usado para distribuir recursos (RDS, EKS nodes)
data "aws_availability_zones" "available" {
  state = "available"
}

# -----------------------------------------------------------------------------
# ACCOUNT E REGION INFO
# -----------------------------------------------------------------------------

# Informações da conta AWS atual
# Útil para logs e debugging
data "aws_caller_identity" "current" {}

# Região atual (sempre us-east-1 no Academy)
data "aws_region" "current" {}
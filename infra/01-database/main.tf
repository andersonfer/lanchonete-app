# Configuração do Terraform
terraform {
  required_version = ">= 1.0"


  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.0"
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

variable "database_name" {
  description = "Nome do banco de dados"
  type        = string
  default     = "lanchonetedb"
}

variable "master_username" {
  description = "Usuário master do banco"
  type        = string
  default     = "admin"
}

# Configurações locais
locals {
  prefix = var.nome_projeto

  common_tags = {
    Projeto   = var.nome_projeto
    ManagedBy = "terraform"
  }
}

# Gera senha aleatória para o banco (sem caracteres problemáticos)
resource "random_password" "rds_password" {
  length  = 16
  special = true
  # Exclui caracteres não permitidos pelo RDS: '/', '@', '"', ' '
  override_special = "!#$%&*()_+=<>?{}|~"
}

# Busca a VPC padrão
data "aws_vpc" "padrao" {
  default = true
}

# Busca todas as subnets da VPC
data "aws_subnets" "disponiveis" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.padrao.id]
  }
}

# Security Group para RDS
resource "aws_security_group" "rds" {
  name_prefix = "${local.prefix}-rds-"
  description = "Security group para RDS MySQL"
  vpc_id      = data.aws_vpc.padrao.id

  # Permite conexões MySQL de dentro da VPC
  ingress {
    from_port   = 3306
    to_port     = 3306
    protocol    = "tcp"
    cidr_blocks = [data.aws_vpc.padrao.cidr_block]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(
    local.common_tags,
    {
      Name = "${local.prefix}-rds-sg"
    }
  )
}

# Subnet Group para RDS
resource "aws_db_subnet_group" "principal" {
  name       = "${local.prefix}-db-subnet-group"
  subnet_ids = data.aws_subnets.disponiveis.ids

  tags = merge(
    local.common_tags,
    {
      Name = "${local.prefix}-db-subnet-group"
    }
  )
}

# Instância RDS MySQL
resource "aws_db_instance" "mysql" {
  identifier = "${local.prefix}-mysql"

  # Configurações do banco
  engine         = "mysql"
  engine_version = "8.0"
  instance_class = "db.t3.micro" # Menor instância para POC

  # Armazenamento
  allocated_storage = 20
  storage_type      = "gp2"
  storage_encrypted = false # POC não precisa criptografia

  # Credenciais
  db_name  = var.database_name
  username = var.master_username
  password = random_password.rds_password.result

  # Rede
  db_subnet_group_name   = aws_db_subnet_group.principal.name
  vpc_security_group_ids = [aws_security_group.rds.id]
  publicly_accessible    = false

  # Configurações de manutenção
  skip_final_snapshot     = true  # POC não precisa snapshot final
  deletion_protection     = false # POC permite deletar facilmente
  backup_retention_period = 1     # Backup mínimo para POC

  # Performance e disponibilidade
  multi_az                   = false # POC não precisa multi-AZ
  auto_minor_version_upgrade = true

  tags = merge(
    local.common_tags,
    {
      Name = "${local.prefix}-mysql"
    }
  )
}

# ===== OUTPUTS PARA PIPELINE CI/CD =====

output "rds_endpoint" {
  description = "Endpoint do RDS MySQL"
  value       = aws_db_instance.mysql.endpoint
}

output "rds_address" {
  description = "Endereço do RDS MySQL (sem porta)"
  value       = aws_db_instance.mysql.address
}

output "rds_port" {
  description = "Porta do RDS MySQL"
  value       = aws_db_instance.mysql.port
}

output "database_name" {
  description = "Nome do banco de dados"
  value       = aws_db_instance.mysql.db_name
}

output "database_username" {
  description = "Usuário do banco de dados"
  value       = var.master_username
}

output "database_password" {
  description = "Senha do banco de dados (sensível)"
  value       = random_password.rds_password.result
  sensitive   = true
}

output "jdbc_url" {
  description = "URL JDBC para conexão (sem credenciais)"
  value       = "jdbc:mysql://${aws_db_instance.mysql.endpoint}/${aws_db_instance.mysql.db_name}"
}

output "security_group_id" {
  description = "ID do security group do RDS"
  value       = aws_security_group.rds.id
}# Test trigger Wed Sep 24 11:22:01 AM -03 2025

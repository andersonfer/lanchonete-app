# =============================================================================
# VARIÁVEIS E DATA SOURCES - MÓDULO DATABASE
# =============================================================================

# Variáveis necessárias para o módulo
variable "project_name" {
  description = "Nome do projeto"
  type        = string
  default     = "lanchonete-app"
}

variable "db_name" {
  description = "Nome do banco de dados"
  type        = string
  default     = "lanchonete"
}

variable "db_username" {
  description = "Username do banco de dados"
  type        = string
  sensitive   = true
}

variable "db_password" {
  description = "Password do banco de dados"
  type        = string
  sensitive   = true
}

variable "common_tags" {
  description = "Tags comuns"
  type        = map(string)
  default = {
    Project     = "lanchonete-app"
    Environment = "dev"
    ManagedBy   = "terraform"
  }
}

# Data sources
data "aws_vpc" "default" {
  default = true
}

data "aws_subnets" "default" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }
}

# IP público atual (dinâmico)
data "http" "my_ip" {
  url = "https://ipv4.icanhazip.com"
  request_headers = {
    Accept = "text/plain"
  }
}
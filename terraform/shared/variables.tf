# =============================================================================
# VARIÁVEIS GLOBAIS - PROJETO LANCHONETE FASE 3
# =============================================================================
# Este arquivo centraliza todas as variáveis que serão reutilizadas 
# entre os diferentes módulos Terraform (database, lambda, kubernetes)

# -----------------------------------------------------------------------------
# CONFIGURAÇÕES AWS ACADEMY
# -----------------------------------------------------------------------------

variable "aws_region" {
  description = "Região AWS (fixa para Academy)"
  type        = string
  default     = "us-east-1"
  
  validation {
    condition     = var.aws_region == "us-east-1"
    error_message = "AWS Academy só permite região us-east-1."
  }
}

# -----------------------------------------------------------------------------
# CONFIGURAÇÕES DE PROJETO
# -----------------------------------------------------------------------------

variable "project_name" {
  description = "Nome do projeto (usado em tags e naming)"
  type        = string
  default     = "lanchonete-app"
}

variable "environment" {
  description = "Ambiente (dev ou prod)"
  type        = string
  default     = "prod"
  
  validation {
    condition     = contains(["dev", "prod"], var.environment)
    error_message = "Environment deve ser dev ou prod."
  }
}

# -----------------------------------------------------------------------------
# CONFIGURAÇÕES DE BANCO DE DADOS
# -----------------------------------------------------------------------------

variable "db_name" {
  description = "Nome do banco de dados"
  type        = string
  default     = "lanchonete"
}

variable "db_username" {
  description = "Username do banco de dados"
  type        = string
  default     = "lanchonete_admin"
  sensitive   = true
}

variable "db_password" {
  description = "Password do banco de dados"
  type        = string
  sensitive   = true
  
  validation {
    condition     = length(var.db_password) >= 8
    error_message = "Password deve ter pelo menos 8 caracteres."
  }
}

# -----------------------------------------------------------------------------
# CONFIGURAÇÕES DE SEGURANÇA
# -----------------------------------------------------------------------------

variable "jwt_secret" {
  description = "Chave secreta para assinar JWTs"
  type        = string
  sensitive   = true
  
  validation {
    condition     = length(var.jwt_secret) >= 32
    error_message = "JWT secret deve ter pelo menos 32 caracteres para HMAC256."
  }
}

# -----------------------------------------------------------------------------
# TAGS PADRÃO
# -----------------------------------------------------------------------------

variable "common_tags" {
  description = "Tags comuns aplicadas a todos os recursos"
  type        = map(string)
  default = {
    Project     = "lanchonete-app"
    Environment = "prod"
    ManagedBy   = "terraform"
  }
}
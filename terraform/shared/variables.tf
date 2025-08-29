# Variables compartilhadas para todos os módulos Terraform
# AWS Academy Lanchonete Tech Challenge Fase 3

# Configurações AWS Academy
variable "aws_region" {
  description = "Região AWS (fixo para Academy)"
  type        = string
  default     = "us-east-1"
}

variable "project_name" {
  description = "Nome do projeto"
  type        = string
  default     = "lanchonete"
}

# Configurações Database
variable "db_username" {
  description = "Username do banco RDS MySQL"
  type        = string
  default     = "lanchonete"
}

variable "db_password" {
  description = "Password do banco RDS MySQL"
  type        = string
  sensitive   = true
}

variable "db_name" {
  description = "Nome do banco de dados"
  type        = string
  default     = "lanchonete"
}

variable "db_instance_class" {
  description = "Classe da instância RDS"
  type        = string
  default     = "db.t3.micro"
}

variable "db_allocated_storage" {
  description = "Armazenamento alocado para RDS (GB)"
  type        = number
  default     = 20
}

# Configurações Lambda
variable "jwt_secret" {
  description = "Chave secreta para assinar JWTs (mínimo 256 bits)"
  type        = string
  sensitive   = true
  
  validation {
    condition     = length(var.jwt_secret) >= 32
    error_message = "JWT secret deve ter pelo menos 32 caracteres para HMAC256."
  }
}

variable "lambda_memory" {
  description = "Memória alocada para Lambda (MB)"
  type        = number
  default     = 512
}

variable "lambda_timeout" {
  description = "Timeout da função Lambda (segundos)"
  type        = number
  default     = 30
}

# Configurações EKS
variable "cluster_version" {
  description = "Versão do cluster EKS"
  type        = string
  default     = "1.28"
}

variable "node_instance_type" {
  description = "Tipo de instância para nodes EKS"
  type        = string
  default     = "t3.medium"
}

variable "node_min_size" {
  description = "Número mínimo de nodes EKS"
  type        = number
  default     = 1
}

variable "node_max_size" {
  description = "Número máximo de nodes EKS"
  type        = number
  default     = 3
}

variable "node_desired_size" {
  description = "Número desejado de nodes EKS"
  type        = number
  default     = 2
}
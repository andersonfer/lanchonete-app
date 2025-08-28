# Variables for Lambda Auth CPF - AWS Academy optimized
variable "aws_region" {
  description = "Região AWS"
  type        = string
  default     = "us-east-1"
}

variable "environment" {
  description = "Ambiente de deployment"
  type        = string
  default     = "dev"
}

variable "project_name" {
  description = "Nome do projeto"
  type        = string
  default     = "lanchonete"
}

variable "lambda_memory" {
  description = "Memória da Lambda"
  type        = number
  default     = 512
}

variable "lambda_timeout" {
  description = "Timeout da Lambda"
  type        = number
  default     = 30
}

variable "jwt_secret" {
  description = "Secret para geração JWT"
  type        = string
  default     = "lanchonete-auth-secret-academy"
  sensitive   = true
}

variable "db_host" {
  description = "Hostname do RDS MySQL"
  type        = string
}

variable "db_port" {
  description = "Porta do RDS MySQL"
  type        = string
  default     = "3306"
}

variable "db_name" {
  description = "Nome do banco de dados"
  type        = string
  default     = "lanchonete"
}

variable "db_username" {
  description = "Usuário do banco de dados"
  type        = string
  default     = "admin"
}

variable "db_password" {
  description = "Senha do banco de dados"
  type        = string
  sensitive   = true
}
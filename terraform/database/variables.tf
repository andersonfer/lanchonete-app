# Variables for database module
# Usa as mesmas variables definidas em ../shared/variables.tf

variable "aws_region" {
  description = "Região AWS"
  type        = string
  default     = "us-east-1"
}

variable "project_name" {
  description = "Nome do projeto"
  type        = string
  default     = "lanchonete"
}

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
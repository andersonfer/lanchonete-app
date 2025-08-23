variable "aws_region" {
  description = "Região AWS (limitada no Academy)"
  type        = string
  default     = "us-east-1"
  
  validation {
    condition = contains(["us-east-1", "us-west-2"], var.aws_region)
    error_message = "Academy normalmente disponibiliza apenas us-east-1 ou us-west-2."
  }
}

variable "environment" {
  description = "Environment name"
  type        = string
  default     = "dev"
}

variable "project_name" {
  description = "Nome do projeto"
  type        = string
  default     = "lanchonete"
}

variable "db_password" {
  description = "Senha do banco RDS MySQL"
  type        = string
  sensitive   = true
}

variable "db_username" {
  description = "Usuário master do banco RDS"
  type        = string
  default     = "admin"
}

variable "db_name" {
  description = "Nome do banco de dados inicial"
  type        = string
  default     = "lanchonete"
}

variable "db_instance_class" {
  description = "Classe da instância RDS (limitada no Academy)"
  type        = string
  default     = "db.t3.micro"
  
  validation {
    condition = contains(["db.t3.micro", "db.t2.micro"], var.db_instance_class)
    error_message = "No Academy, use apenas instâncias Free Tier: db.t3.micro ou db.t2.micro."
  }
}

variable "db_allocated_storage" {
  description = "Storage alocado para RDS (GB)"
  type        = number
  default     = 20
  
  validation {
    condition     = var.db_allocated_storage >= 20 && var.db_allocated_storage <= 100
    error_message = "Storage deve estar entre 20GB e 100GB para Academy."
  }
}

variable "backup_retention_period" {
  description = "Período de retenção de backup (dias)"
  type        = number
  default     = 1
  
  validation {
    condition     = var.backup_retention_period >= 1 && var.backup_retention_period <= 7
    error_message = "No Academy, use retenção entre 1 e 7 dias para otimizar recursos."
  }
}

variable "default_tags" {
  description = "Tags padrão para todos os recursos"
  type        = map(string)
  default = {
    Project     = "tech-challenge-fase3"
    Environment = "aws-academy"
    Course      = "soat-pos-tech"
    ManagedBy   = "terraform"
  }
}
# Security Group para RDS MySQL
resource "aws_security_group" "rds" {
  name_prefix = "${var.project_name}-rds-"
  description = "Security group para RDS MySQL - ${var.project_name}"
  
  # Porta MySQL para conexões internas
  ingress {
    from_port   = 3306
    to_port     = 3306
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]  # Simplificado para Academy (não recomendado para produção)
    description = "MySQL port para Lambda functions"
  }
  
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Outbound traffic"
  }
  
  tags = merge(var.default_tags, {
    Name = "${var.project_name}-rds-sg"
    Type = "database-security-group"
  })
}

# Subnet Group para RDS (usar subnets padrão do Academy)
resource "aws_db_subnet_group" "rds" {
  name       = "${var.project_name}-${var.environment}-subnet-group"
  subnet_ids = data.aws_subnets.available.ids
  
  tags = merge(var.default_tags, {
    Name = "${var.project_name}-${var.environment}-subnet-group"
    Type = "database-subnet-group"
  })
}

# Instância RDS MySQL configurada para AWS Academy
resource "aws_db_instance" "lanchonete_db" {
  identifier = "${var.project_name}-${var.environment}"
  
  # Engine Configuration
  engine         = "mysql"
  engine_version = "8.0"
  instance_class = var.db_instance_class
  
  # Storage Configuration (otimizado para Academy)
  allocated_storage     = var.db_allocated_storage
  max_allocated_storage = var.db_allocated_storage + 10  # Auto-scaling limitado
  storage_type         = "gp2"
  storage_encrypted    = true
  
  # Database Configuration
  db_name  = var.db_name
  username = var.db_username
  password = var.db_password
  port     = 3306
  
  # Network Configuration (simplificado para Academy)
  db_subnet_group_name   = aws_db_subnet_group.rds.name
  vpc_security_group_ids = [aws_security_group.rds.id]
  publicly_accessible    = true
  
  # Backup Configuration (conservador para Academy)
  backup_retention_period = var.backup_retention_period
  backup_window          = "03:00-04:00"
  maintenance_window     = "sun:04:00-sun:05:00"
  
  # Operacional Configuration
  skip_final_snapshot = true
  deletion_protection = false
  
  # Monitoring desabilitado para economia no Academy
  monitoring_interval = 0
  
  # Parameter group padrão
  parameter_group_name = "default.mysql8.0"
  
  tags = merge(var.default_tags, {
    Name = "${var.project_name}-${var.environment}-db"
    Type = "database-instance"
  })
}

# Buscar subnets disponíveis na VPC padrão
data "aws_vpc" "default" {
  default = true
}

data "aws_subnets" "available" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }
  
  filter {
    name   = "availability-zone"
    values = ["${var.aws_region}a", "${var.aws_region}b", "${var.aws_region}c"]
  }
}
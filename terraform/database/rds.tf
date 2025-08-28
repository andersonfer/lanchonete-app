# =============================================================================
# RDS MYSQL - INSTÂNCIA PRINCIPAL
# =============================================================================

# -----------------------------------------------------------------------------
# DB SUBNET GROUP
# -----------------------------------------------------------------------------

resource "aws_db_subnet_group" "main" {
  name       = "${var.project_name}-db-subnet-group"
  subnet_ids = data.aws_subnets.default.ids

  tags = var.common_tags
}

# -----------------------------------------------------------------------------
# RDS MYSQL INSTANCE
# -----------------------------------------------------------------------------

resource "aws_db_instance" "mysql" {
  identifier = "${var.project_name}-mysql"
  db_name    = var.db_name
  
  engine         = "mysql"
  engine_version = "8.0"
  instance_class = "db.t3.micro"
  
  allocated_storage = 20
  storage_type     = "gp2"
  storage_encrypted = false
  
  username = var.db_username
  password = var.db_password
  
  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = [aws_security_group.rds.id]
  publicly_accessible    = true
  
  skip_final_snapshot     = true
  deletion_protection     = false
  backup_retention_period = 0
  
  tags = var.common_tags
}
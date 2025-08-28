# =============================================================================
# OUTPUTS - MÓDULO DATABASE
# =============================================================================

output "rds_endpoint" {
  description = "Endpoint do RDS MySQL"
  value       = aws_db_instance.mysql.endpoint
}

output "rds_port" {
  description = "Porta do RDS MySQL"
  value       = aws_db_instance.mysql.port
}

output "database_name" {
  description = "Nome do banco de dados"
  value       = aws_db_instance.mysql.db_name
}

output "security_group_id" {
  description = "ID do security group do RDS"
  value       = aws_security_group.rds.id
}
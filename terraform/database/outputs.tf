# Outputs do módulo database para uso em outros módulos

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

output "rds_security_group_id" {
  description = "ID do security group do RDS"
  value       = aws_security_group.rds.id
}

output "lambda_security_group_id" {
  description = "ID do security group da Lambda"
  value       = aws_security_group.lambda.id
}

output "migrations_bucket" {
  description = "Nome do bucket S3 das migrations"
  value       = aws_s3_bucket.migrations.id
}
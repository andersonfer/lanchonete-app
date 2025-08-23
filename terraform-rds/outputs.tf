# Outputs essenciais para conexão com RDS
output "rds_endpoint" {
  description = "Endpoint da instância RDS (apenas hostname)"
  value       = split(":", aws_db_instance.lanchonete_db.endpoint)[0]
  sensitive   = false
}

output "rds_port" {
  description = "Porta da instância RDS"
  value       = aws_db_instance.lanchonete_db.port
}

output "database_name" {
  description = "Nome do banco de dados"
  value       = aws_db_instance.lanchonete_db.db_name
}

output "database_username" {
  description = "Usuário master do banco"
  value       = aws_db_instance.lanchonete_db.username
  sensitive   = true
}

output "rds_instance_id" {
  description = "ID da instância RDS"
  value       = aws_db_instance.lanchonete_db.id
}

output "rds_arn" {
  description = "ARN da instância RDS"
  value       = aws_db_instance.lanchonete_db.arn
}

output "security_group_id" {
  description = "ID do Security Group do RDS"
  value       = aws_security_group.rds.id
}

output "subnet_group_name" {
  description = "Nome do DB Subnet Group"
  value       = aws_db_subnet_group.rds.name
}

# Informações para conexão das Lambdas
output "connection_info" {
  description = "Informações de conexão para usar nas Lambdas"
  value = {
    host     = split(":", aws_db_instance.lanchonete_db.endpoint)[0]
    port     = aws_db_instance.lanchonete_db.port
    database = aws_db_instance.lanchonete_db.db_name
    username = aws_db_instance.lanchonete_db.username
  }
  sensitive = false
}

# String de conexão JDBC (sem senha por segurança)
output "jdbc_url" {
  description = "URL JDBC para conexão (sem senha)"
  value       = "jdbc:mysql://${split(":", aws_db_instance.lanchonete_db.endpoint)[0]}:${aws_db_instance.lanchonete_db.port}/${aws_db_instance.lanchonete_db.db_name}?useSSL=true&requireSSL=false&serverTimezone=UTC"
}

# Comandos para testar conexão
output "mysql_connection_command" {
  description = "Comando para conectar via mysql client"
  value       = "mysql -h ${split(":", aws_db_instance.lanchonete_db.endpoint)[0]} -P ${aws_db_instance.lanchonete_db.port} -u ${aws_db_instance.lanchonete_db.username} -p ${aws_db_instance.lanchonete_db.db_name}"
}

# Status e informações operacionais
output "rds_status" {
  description = "Status atual da instância RDS"
  value       = aws_db_instance.lanchonete_db.status
}

output "rds_availability_zone" {
  description = "AZ onde a instância está rodando"
  value       = aws_db_instance.lanchonete_db.availability_zone
}
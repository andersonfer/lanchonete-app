# Execução automática da Lambda de migration após criação do RDS
# Tech Challenge Fase 3 - Lanchonete

resource "null_resource" "auto_migrate" {
  depends_on = [
    aws_db_instance.mysql,
    aws_lambda_function.migration,
    aws_s3_object.schema_sql,
    aws_s3_object.seed_sql
  ]

  provisioner "local-exec" {
    command = <<-EOF
      echo "🚀 Executando migrations automaticamente..."
      aws lambda invoke --function-name ${aws_lambda_function.migration.function_name} response.json
      
      echo "📄 Resposta da migration:"
      cat response.json
      
      # Verificar se migration foi bem-sucedida (sem jq)
      if grep -q '"status":"success"' response.json; then
        echo "✅ Migrations executadas com sucesso!"
      else
        echo "❌ Migration falhou - verifique os logs"
        exit 1
      fi
    EOF
  }
  
  provisioner "local-exec" {
    when    = destroy
    command = "rm -f response.json"
  }

  # Re-executar migrations quando necessário
  triggers = {
    db_id       = aws_db_instance.mysql.id
    files_hash  = md5("${file("${path.module}/migrations/001_create_schema.sql")}${file("${path.module}/migrations/002_seed_data.sql")}")
    lambda_hash = aws_lambda_function.migration.source_code_hash
  }
}
# Placeholder para controle de dependências
# As migrations são executadas via script externo (run-migrations.sh)
# Tech Challenge Fase 3 - Lanchonete

resource "null_resource" "migration_ready" {
  depends_on = [
    aws_db_instance.mysql,
    aws_lambda_function.migration,
    aws_s3_object.schema_sql,
    aws_s3_object.seed_sql
  ]

  # Apenas garantir que todos os recursos estão prontos
  # A migration será executada via script externo
  triggers = {
    db_id       = aws_db_instance.mysql.id
    files_hash  = md5("${file("${path.module}/migrations/001_create_schema.sql")}${file("${path.module}/migrations/002_seed_data.sql")}")
    lambda_hash = aws_lambda_function.migration.source_code_hash
  }
}
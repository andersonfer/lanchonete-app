# Lambda simples para executar migrations sem S3

resource "aws_lambda_function" "simple_migration" {
  filename         = "../../lambda-connectivity-test/target/connectivity-test-1.0.0.jar"
  function_name    = "${var.project_name}-simple-migration"
  role            = data.aws_iam_role.lab_role.arn
  handler         = "br.com.lanchonete.test.SimpleMigrationHandler::handleRequest"
  runtime         = "java17"
  memory_size     = 512
  timeout         = 60
  
  vpc_config {
    subnet_ids         = data.aws_subnets.default.ids
    security_group_ids = [aws_security_group.lambda.id]
  }
  
  environment {
    variables = {
      DATABASE_URL = aws_db_instance.mysql.endpoint
      DB_NAME     = aws_db_instance.mysql.db_name
      DB_USERNAME = var.db_username
      DB_PASSWORD = var.db_password
    }
  }
  
  depends_on = [aws_db_instance.mysql]
}
# Lambda function configuration
resource "aws_lambda_function" "auth_cpf" {
  filename         = "../target/lambda-auth-cpf-1.0.0.jar"
  function_name    = "${var.project_name}-auth-cpf-${var.environment}"
  role            = data.aws_iam_role.lab_role.arn
  handler         = "br.com.lanchonete.auth.AuthCpfHandler::handleRequest"
  runtime         = "java17"
  timeout         = var.lambda_timeout
  memory_size     = var.lambda_memory
  
  source_code_hash = filebase64sha256("../target/lambda-auth-cpf-1.0.0.jar")
  
  environment {
    variables = {
      ENVIRONMENT = var.environment
    }
  }
}

# CloudWatch Log Group
resource "aws_cloudwatch_log_group" "lambda_logs" {
  name              = "/aws/lambda/${aws_lambda_function.auth_cpf.function_name}"
  retention_in_days = 7
}
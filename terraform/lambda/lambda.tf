# Usar security group criado pelo módulo database
data "aws_security_group" "lambda_sg" {
  filter {
    name   = "tag:Name"
    values = ["lanchonete-lambda-sg"]
  }
  vpc_id = data.aws_vpc.default.id
}

resource "aws_lambda_function" "auth_lambda" {
  filename         = var.lambda_jar_path
  function_name    = "lanchonete-auth"
  role            = data.aws_iam_role.lab_role.arn
  handler         = "br.com.lanchonete.auth.AuthHandler::handleRequest"
  runtime         = "java17"
  memory_size     = 512
  timeout         = 30

  vpc_config {
    subnet_ids         = data.aws_subnets.default.ids
    security_group_ids = [data.aws_security_group.lambda_sg.id]
  }

  environment {
    variables = {
      DATABASE_URL = "jdbc:mysql://${data.aws_db_instance.mysql.endpoint}/${data.aws_db_instance.mysql.db_name}?useSSL=false&serverTimezone=UTC"
      DB_USERNAME  = var.db_username
      DB_PASSWORD  = var.db_password
      JWT_SECRET   = var.jwt_secret
    }
  }

  tags = {
    Name = "lanchonete-auth"
  }
}

resource "aws_lambda_permission" "allow_api_gateway" {
  statement_id  = "AllowExecutionFromAPIGateway"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.auth_lambda.function_name
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${aws_api_gateway_rest_api.lanchonete_api.execution_arn}/*/*"
}
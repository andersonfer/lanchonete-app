# JWT Custom Authorizer para API Gateway

# Lambda function para JWT Authorizer
resource "aws_lambda_function" "jwt_authorizer" {
  filename         = var.lambda_jar_path
  function_name    = "lanchonete-jwt-authorizer"
  role            = data.aws_iam_role.lab_role.arn
  handler         = "br.com.lanchonete.auth.util.JwtAuthorizerHandler::handleRequest"
  runtime         = "java17"
  memory_size     = 256
  timeout         = 10

  environment {
    variables = {
      JWT_SECRET = var.jwt_secret
    }
  }

  tags = {
    Name = "lanchonete-jwt-authorizer"
  }
}

# Custom Authorizer no API Gateway
resource "aws_api_gateway_authorizer" "jwt_authorizer" {
  name                             = "lanchonete-jwt-authorizer"
  rest_api_id                     = aws_api_gateway_rest_api.lanchonete_api.id
  authorizer_uri                  = aws_lambda_function.jwt_authorizer.invoke_arn
  authorizer_credentials          = data.aws_iam_role.lab_role.arn
  type                           = "TOKEN"
  identity_source                = "method.request.header.Authorization"
  authorizer_result_ttl_in_seconds = 300  # Cache por 5 minutos
}

# Permissão para API Gateway invocar o JWT Authorizer
resource "aws_lambda_permission" "allow_api_gateway_authorizer" {
  statement_id  = "AllowAPIGatewayInvokeAuthorizer"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.jwt_authorizer.function_name
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${aws_api_gateway_rest_api.lanchonete_api.execution_arn}/authorizers/${aws_api_gateway_authorizer.jwt_authorizer.id}"
}
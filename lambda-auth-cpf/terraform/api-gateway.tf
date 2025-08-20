# API Gateway REST API
resource "aws_api_gateway_rest_api" "auth_api" {
  name = "${var.project_name}-auth-api"
}

# Resource /auth/cpf
resource "aws_api_gateway_resource" "auth" {
  rest_api_id = aws_api_gateway_rest_api.auth_api.id
  parent_id   = aws_api_gateway_rest_api.auth_api.root_resource_id
  path_part   = "auth"
}

resource "aws_api_gateway_resource" "cpf" {
  rest_api_id = aws_api_gateway_rest_api.auth_api.id
  parent_id   = aws_api_gateway_resource.auth.id
  path_part   = "cpf"
}

# POST method
resource "aws_api_gateway_method" "post_cpf" {
  rest_api_id   = aws_api_gateway_rest_api.auth_api.id
  resource_id   = aws_api_gateway_resource.cpf.id
  http_method   = "POST"
  authorization = "NONE"
}

# Lambda integration
resource "aws_api_gateway_integration" "lambda_integration" {
  rest_api_id = aws_api_gateway_rest_api.auth_api.id
  resource_id = aws_api_gateway_resource.cpf.id
  http_method = aws_api_gateway_method.post_cpf.http_method

  integration_http_method = "POST"
  type                    = "AWS_PROXY"
  uri                     = aws_lambda_function.auth_cpf.invoke_arn
}

# Lambda permission
resource "aws_lambda_permission" "api_gateway" {
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.auth_cpf.function_name
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${aws_api_gateway_rest_api.auth_api.execution_arn}/*/*"
}

# Deploy
resource "aws_api_gateway_deployment" "auth_deployment" {
  depends_on = [
    aws_api_gateway_method.post_cpf,
    aws_api_gateway_integration.lambda_integration
  ]

  rest_api_id = aws_api_gateway_rest_api.auth_api.id
  stage_name  = "dev"
}
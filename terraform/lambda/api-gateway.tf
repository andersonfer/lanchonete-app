resource "aws_api_gateway_rest_api" "lanchonete_api" {
  name        = "lanchonete-api"
  description = "API Gateway para sistema de lanchonete"
  
  endpoint_configuration {
    types = ["REGIONAL"]
  }

  tags = {
    Name = "lanchonete-api"
  }
}

# Resource /auth
resource "aws_api_gateway_resource" "auth" {
  rest_api_id = aws_api_gateway_rest_api.lanchonete_api.id
  parent_id   = aws_api_gateway_rest_api.lanchonete_api.root_resource_id
  path_part   = "auth"
}

# Method POST /auth
resource "aws_api_gateway_method" "auth_post" {
  rest_api_id   = aws_api_gateway_rest_api.lanchonete_api.id
  resource_id   = aws_api_gateway_resource.auth.id
  http_method   = "POST"
  authorization = "NONE"
}

# Integration POST /auth -> Lambda
resource "aws_api_gateway_integration" "auth_integration" {
  rest_api_id             = aws_api_gateway_rest_api.lanchonete_api.id
  resource_id             = aws_api_gateway_resource.auth.id
  http_method             = aws_api_gateway_method.auth_post.http_method
  integration_http_method = "POST"
  type                   = "AWS_PROXY"
  uri                    = aws_lambda_function.auth_lambda.invoke_arn
}

# Method OPTIONS /auth (CORS)
resource "aws_api_gateway_method" "auth_options" {
  rest_api_id   = aws_api_gateway_rest_api.lanchonete_api.id
  resource_id   = aws_api_gateway_resource.auth.id
  http_method   = "OPTIONS"
  authorization = "NONE"
}

# Integration OPTIONS /auth
resource "aws_api_gateway_integration" "auth_options_integration" {
  rest_api_id = aws_api_gateway_rest_api.lanchonete_api.id
  resource_id = aws_api_gateway_resource.auth.id
  http_method = aws_api_gateway_method.auth_options.http_method
  type        = "MOCK"
  
  request_templates = {
    "application/json" = jsonencode({
      statusCode = 200
    })
  }
}

# Method Response OPTIONS /auth
resource "aws_api_gateway_method_response" "auth_options_200" {
  rest_api_id = aws_api_gateway_rest_api.lanchonete_api.id
  resource_id = aws_api_gateway_resource.auth.id
  http_method = aws_api_gateway_method.auth_options.http_method
  status_code = "200"
  
  response_parameters = {
    "method.response.header.Access-Control-Allow-Origin"  = true
    "method.response.header.Access-Control-Allow-Methods" = true
    "method.response.header.Access-Control-Allow-Headers" = true
  }
  
  response_models = {
    "application/json" = "Empty"
  }
}

# Integration Response OPTIONS /auth
resource "aws_api_gateway_integration_response" "auth_options_integration_response" {
  rest_api_id = aws_api_gateway_rest_api.lanchonete_api.id
  resource_id = aws_api_gateway_resource.auth.id
  http_method = aws_api_gateway_method.auth_options.http_method
  status_code = aws_api_gateway_method_response.auth_options_200.status_code
  
  response_parameters = {
    "method.response.header.Access-Control-Allow-Origin"  = "'*'"
    "method.response.header.Access-Control-Allow-Methods" = "'POST,OPTIONS'"
    "method.response.header.Access-Control-Allow-Headers" = "'Content-Type,Authorization'"
  }
  
  depends_on = [aws_api_gateway_integration.auth_options_integration]
}

# Deployment
resource "aws_api_gateway_deployment" "api_deployment" {
  depends_on = [
    aws_api_gateway_integration.auth_integration,
    aws_api_gateway_integration.auth_options_integration,
    aws_api_gateway_integration.produtos_categoria_integration,
    aws_api_gateway_integration.clientes_integration,
    aws_api_gateway_integration.pedidos_get_integration,
    aws_api_gateway_integration.pedidos_post_integration,
    aws_api_gateway_integration.pagamentos_post_integration,
    aws_api_gateway_integration.pagamentos_get_integration
  ]
  
  rest_api_id = aws_api_gateway_rest_api.lanchonete_api.id
  
  triggers = {
    redeployment = sha1(jsonencode([
      aws_api_gateway_resource.produtos.id,
      aws_api_gateway_resource.clientes.id,
      aws_api_gateway_resource.pedidos.id,
      aws_api_gateway_resource.pagamentos.id,
      aws_api_gateway_method.produtos_categoria_get.id,
      aws_api_gateway_method.clientes_get.id,
      aws_api_gateway_method.pedidos_get.id,
      aws_api_gateway_method.pedidos_post.id,
      aws_api_gateway_method.pagamentos_get.id,
      aws_api_gateway_method.pagamentos_post.id,
    ]))
  }
  
  lifecycle {
    create_before_destroy = true
  }
}

# Stage
resource "aws_api_gateway_stage" "api_stage" {
  deployment_id = aws_api_gateway_deployment.api_deployment.id
  rest_api_id   = aws_api_gateway_rest_api.lanchonete_api.id
  stage_name    = "v1"
}
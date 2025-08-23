# API Gateway REST API
resource "aws_api_gateway_rest_api" "produtos_api" {
  name = "${var.project_name}-api"
}

# Resource /produtos
resource "aws_api_gateway_resource" "produtos" {
  rest_api_id = aws_api_gateway_rest_api.produtos_api.id
  parent_id   = aws_api_gateway_rest_api.produtos_api.root_resource_id
  path_part   = "produtos"
}

# Resource /produtos/{id}
resource "aws_api_gateway_resource" "produto_id" {
  rest_api_id = aws_api_gateway_rest_api.produtos_api.id
  parent_id   = aws_api_gateway_resource.produtos.id
  path_part   = "{id}"
}

# Resource /produtos/categoria
resource "aws_api_gateway_resource" "categoria" {
  rest_api_id = aws_api_gateway_rest_api.produtos_api.id
  parent_id   = aws_api_gateway_resource.produtos.id
  path_part   = "categoria"
}

# Resource /produtos/categoria/{categoria}
resource "aws_api_gateway_resource" "categoria_param" {
  rest_api_id = aws_api_gateway_rest_api.produtos_api.id
  parent_id   = aws_api_gateway_resource.categoria.id
  path_part   = "{categoria}"
}

# POST /produtos (criar produto)
resource "aws_api_gateway_method" "post_produtos" {
  rest_api_id   = aws_api_gateway_rest_api.produtos_api.id
  resource_id   = aws_api_gateway_resource.produtos.id
  http_method   = "POST"
  authorization = "NONE"
}

# PUT /produtos/{id} (editar produto)
resource "aws_api_gateway_method" "put_produto" {
  rest_api_id   = aws_api_gateway_rest_api.produtos_api.id
  resource_id   = aws_api_gateway_resource.produto_id.id
  http_method   = "PUT"
  authorization = "NONE"
}

# DELETE /produtos/{id} (remover produto)
resource "aws_api_gateway_method" "delete_produto" {
  rest_api_id   = aws_api_gateway_rest_api.produtos_api.id
  resource_id   = aws_api_gateway_resource.produto_id.id
  http_method   = "DELETE"
  authorization = "NONE"
}

# GET /produtos/categoria/{categoria} (buscar por categoria)
resource "aws_api_gateway_method" "get_categoria" {
  rest_api_id   = aws_api_gateway_rest_api.produtos_api.id
  resource_id   = aws_api_gateway_resource.categoria_param.id
  http_method   = "GET"
  authorization = "NONE"
}

# Integrations - POST /produtos
resource "aws_api_gateway_integration" "post_integration" {
  rest_api_id = aws_api_gateway_rest_api.produtos_api.id
  resource_id = aws_api_gateway_resource.produtos.id
  http_method = aws_api_gateway_method.post_produtos.http_method

  integration_http_method = "POST"
  type                    = "AWS_PROXY"
  uri                     = aws_lambda_function.produtos_crud.invoke_arn
}

# Integrations - PUT /produtos/{id}
resource "aws_api_gateway_integration" "put_integration" {
  rest_api_id = aws_api_gateway_rest_api.produtos_api.id
  resource_id = aws_api_gateway_resource.produto_id.id
  http_method = aws_api_gateway_method.put_produto.http_method

  integration_http_method = "POST"
  type                    = "AWS_PROXY"
  uri                     = aws_lambda_function.produtos_crud.invoke_arn
}

# Integrations - DELETE /produtos/{id}
resource "aws_api_gateway_integration" "delete_integration" {
  rest_api_id = aws_api_gateway_rest_api.produtos_api.id
  resource_id = aws_api_gateway_resource.produto_id.id
  http_method = aws_api_gateway_method.delete_produto.http_method

  integration_http_method = "POST"
  type                    = "AWS_PROXY"
  uri                     = aws_lambda_function.produtos_crud.invoke_arn
}

# Integrations - GET /produtos/categoria/{categoria}
resource "aws_api_gateway_integration" "get_categoria_integration" {
  rest_api_id = aws_api_gateway_rest_api.produtos_api.id
  resource_id = aws_api_gateway_resource.categoria_param.id
  http_method = aws_api_gateway_method.get_categoria.http_method

  integration_http_method = "POST"
  type                    = "AWS_PROXY"
  uri                     = aws_lambda_function.produtos_crud.invoke_arn
}

# Lambda permission
resource "aws_lambda_permission" "api_gateway" {
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.produtos_crud.function_name
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${aws_api_gateway_rest_api.produtos_api.execution_arn}/*/*"
}

# Deploy
resource "aws_api_gateway_deployment" "produtos_deployment" {
  depends_on = [
    aws_api_gateway_method.post_produtos,
    aws_api_gateway_method.put_produto,
    aws_api_gateway_method.delete_produto,
    aws_api_gateway_method.get_categoria,
    aws_api_gateway_integration.post_integration,
    aws_api_gateway_integration.put_integration,
    aws_api_gateway_integration.delete_integration,
    aws_api_gateway_integration.get_categoria_integration
  ]

  rest_api_id = aws_api_gateway_rest_api.produtos_api.id
  stage_name  = "dev"
}
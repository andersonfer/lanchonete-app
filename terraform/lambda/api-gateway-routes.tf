# Routes protegidas do API Gateway com Context Injection

# ============================================
# RECURSOS (Resources)  
# ============================================

# /produtos
resource "aws_api_gateway_resource" "produtos" {
  rest_api_id = aws_api_gateway_rest_api.lanchonete_api.id
  parent_id   = aws_api_gateway_rest_api.lanchonete_api.root_resource_id
  path_part   = "produtos"
}

# /produtos/categoria
resource "aws_api_gateway_resource" "produtos_categoria" {
  rest_api_id = aws_api_gateway_rest_api.lanchonete_api.id
  parent_id   = aws_api_gateway_resource.produtos.id
  path_part   = "categoria"
}

# /produtos/categoria/{categoria}
resource "aws_api_gateway_resource" "produtos_categoria_param" {
  rest_api_id = aws_api_gateway_rest_api.lanchonete_api.id
  parent_id   = aws_api_gateway_resource.produtos_categoria.id
  path_part   = "{categoria}"
}

# /clientes  
resource "aws_api_gateway_resource" "clientes" {
  rest_api_id = aws_api_gateway_rest_api.lanchonete_api.id
  parent_id   = aws_api_gateway_rest_api.lanchonete_api.root_resource_id
  path_part   = "clientes"
}

# /pedidos
resource "aws_api_gateway_resource" "pedidos" {
  rest_api_id = aws_api_gateway_rest_api.lanchonete_api.id
  parent_id   = aws_api_gateway_rest_api.lanchonete_api.root_resource_id
  path_part   = "pedidos"
}

# ============================================
# MÉTODOS COM JWT AUTHORIZER
# ============================================

# GET /produtos/categoria/{categoria}
resource "aws_api_gateway_method" "produtos_categoria_get" {
  rest_api_id      = aws_api_gateway_rest_api.lanchonete_api.id
  resource_id      = aws_api_gateway_resource.produtos_categoria_param.id
  http_method      = "GET"
  authorization    = "CUSTOM"
  authorizer_id    = aws_api_gateway_authorizer.jwt_authorizer.id
  api_key_required = false

  request_parameters = {
    "method.request.path.categoria" = true
  }
}

# GET /clientes (buscar por CPF via query param)
resource "aws_api_gateway_method" "clientes_get" {
  rest_api_id      = aws_api_gateway_rest_api.lanchonete_api.id
  resource_id      = aws_api_gateway_resource.clientes.id
  http_method      = "GET"
  authorization    = "CUSTOM"
  authorizer_id    = aws_api_gateway_authorizer.jwt_authorizer.id
  api_key_required = false

  request_parameters = {
    "method.request.querystring.cpf" = false
  }
}

# GET /pedidos
resource "aws_api_gateway_method" "pedidos_get" {
  rest_api_id      = aws_api_gateway_rest_api.lanchonete_api.id
  resource_id      = aws_api_gateway_resource.pedidos.id
  http_method      = "GET"
  authorization    = "CUSTOM"
  authorizer_id    = aws_api_gateway_authorizer.jwt_authorizer.id
  api_key_required = false
}

# POST /pedidos (checkout)
resource "aws_api_gateway_method" "pedidos_post" {
  rest_api_id      = aws_api_gateway_rest_api.lanchonete_api.id
  resource_id      = aws_api_gateway_resource.pedidos.id
  http_method      = "POST"
  authorization    = "CUSTOM"
  authorizer_id    = aws_api_gateway_authorizer.jwt_authorizer.id
  api_key_required = false
}

# ============================================
# INTEGRAÇÕES MOCK (para testar context injection)
# ============================================

# Mock integration para produtos
resource "aws_api_gateway_integration" "produtos_categoria_mock" {
  rest_api_id = aws_api_gateway_rest_api.lanchonete_api.id
  resource_id = aws_api_gateway_resource.produtos_categoria_param.id
  http_method = aws_api_gateway_method.produtos_categoria_get.http_method
  type        = "MOCK"

  request_templates = {
    "application/json" = jsonencode({
      statusCode = 200
    })
  }
}

# Mock integration response para produtos
resource "aws_api_gateway_integration_response" "produtos_categoria_mock_response" {
  rest_api_id = aws_api_gateway_rest_api.lanchonete_api.id
  resource_id = aws_api_gateway_resource.produtos_categoria_param.id
  http_method = aws_api_gateway_method.produtos_categoria_get.http_method
  status_code = "200"

  response_templates = {
    "application/json" = jsonencode({
      message = "Context injection test - EKS não implementado ainda"
      categoria = "$input.params('categoria')"
      headers = {
        clienteId = "$context.authorizer.clienteId"
        cpf = "$context.authorizer.cpf"
        nome = "$context.authorizer.nome"
        authType = "$context.authorizer.authType"
        sessionId = "$context.authorizer.sessionId"
      }
    })
  }

  depends_on = [aws_api_gateway_integration.produtos_categoria_mock]
}

# Method response para produtos
resource "aws_api_gateway_method_response" "produtos_categoria_200" {
  rest_api_id = aws_api_gateway_rest_api.lanchonete_api.id
  resource_id = aws_api_gateway_resource.produtos_categoria_param.id
  http_method = aws_api_gateway_method.produtos_categoria_get.http_method
  status_code = "200"

  response_models = {
    "application/json" = "Empty"
  }

  response_parameters = {
    "method.response.header.Access-Control-Allow-Origin" = true
  }
}

# Mock integration para clientes
resource "aws_api_gateway_integration" "clientes_mock" {
  rest_api_id = aws_api_gateway_rest_api.lanchonete_api.id
  resource_id = aws_api_gateway_resource.clientes.id
  http_method = aws_api_gateway_method.clientes_get.http_method
  type        = "MOCK"

  request_templates = {
    "application/json" = jsonencode({
      statusCode = 200
    })
  }
}

# Mock integration response para clientes  
resource "aws_api_gateway_integration_response" "clientes_mock_response" {
  rest_api_id = aws_api_gateway_rest_api.lanchonete_api.id
  resource_id = aws_api_gateway_resource.clientes.id
  http_method = aws_api_gateway_method.clientes_get.http_method
  status_code = "200"

  response_templates = {
    "application/json" = jsonencode({
      message = "Context injection test - EKS não implementado ainda"
      headers = {
        clienteId = "$context.authorizer.clienteId"
        cpf = "$context.authorizer.cpf"
        nome = "$context.authorizer.nome"
        authType = "$context.authorizer.authType"
        sessionId = "$context.authorizer.sessionId"
      }
    })
  }

  depends_on = [aws_api_gateway_integration.clientes_mock]
}

# Method response para clientes
resource "aws_api_gateway_method_response" "clientes_200" {
  rest_api_id = aws_api_gateway_rest_api.lanchonete_api.id
  resource_id = aws_api_gateway_resource.clientes.id
  http_method = aws_api_gateway_method.clientes_get.http_method
  status_code = "200"

  response_models = {
    "application/json" = "Empty"
  }

  response_parameters = {
    "method.response.header.Access-Control-Allow-Origin" = true
  }
}

# Mock integration para pedidos GET
resource "aws_api_gateway_integration" "pedidos_get_mock" {
  rest_api_id = aws_api_gateway_rest_api.lanchonete_api.id
  resource_id = aws_api_gateway_resource.pedidos.id
  http_method = aws_api_gateway_method.pedidos_get.http_method
  type        = "MOCK"

  request_templates = {
    "application/json" = jsonencode({
      statusCode = 200
    })
  }
}

# Mock integration response para pedidos GET
resource "aws_api_gateway_integration_response" "pedidos_get_mock_response" {
  rest_api_id = aws_api_gateway_rest_api.lanchonete_api.id
  resource_id = aws_api_gateway_resource.pedidos.id
  http_method = aws_api_gateway_method.pedidos_get.http_method
  status_code = "200"

  response_templates = {
    "application/json" = jsonencode({
      message = "Context injection test - EKS não implementado ainda"
      headers = {
        clienteId = "$context.authorizer.clienteId"
        cpf = "$context.authorizer.cpf"
        nome = "$context.authorizer.nome"
        authType = "$context.authorizer.authType"
        sessionId = "$context.authorizer.sessionId"
      }
    })
  }

  depends_on = [aws_api_gateway_integration.pedidos_get_mock]
}

# Method response para pedidos GET
resource "aws_api_gateway_method_response" "pedidos_get_200" {
  rest_api_id = aws_api_gateway_rest_api.lanchonete_api.id
  resource_id = aws_api_gateway_resource.pedidos.id
  http_method = aws_api_gateway_method.pedidos_get.http_method
  status_code = "200"

  response_models = {
    "application/json" = "Empty"
  }

  response_parameters = {
    "method.response.header.Access-Control-Allow-Origin" = true
  }
}

# Mock integration para pedidos POST
resource "aws_api_gateway_integration" "pedidos_post_mock" {
  rest_api_id = aws_api_gateway_rest_api.lanchonete_api.id
  resource_id = aws_api_gateway_resource.pedidos.id
  http_method = aws_api_gateway_method.pedidos_post.http_method
  type        = "MOCK"

  request_templates = {
    "application/json" = jsonencode({
      statusCode = 201
    })
  }
}

# Mock integration response para pedidos POST
resource "aws_api_gateway_integration_response" "pedidos_post_mock_response" {
  rest_api_id = aws_api_gateway_rest_api.lanchonete_api.id
  resource_id = aws_api_gateway_resource.pedidos.id
  http_method = aws_api_gateway_method.pedidos_post.http_method
  status_code = "201"

  response_templates = {
    "application/json" = jsonencode({
      message = "Context injection test - EKS não implementado ainda"
      headers = {
        clienteId = "$context.authorizer.clienteId"
        cpf = "$context.authorizer.cpf"
        nome = "$context.authorizer.nome"
        authType = "$context.authorizer.authType"
        sessionId = "$context.authorizer.sessionId"
      }
    })
  }

  depends_on = [aws_api_gateway_integration.pedidos_post_mock]
}

# Method response para pedidos POST
resource "aws_api_gateway_method_response" "pedidos_post_201" {
  rest_api_id = aws_api_gateway_rest_api.lanchonete_api.id
  resource_id = aws_api_gateway_resource.pedidos.id
  http_method = aws_api_gateway_method.pedidos_post.http_method
  status_code = "201"

  response_models = {
    "application/json" = "Empty"
  }

  response_parameters = {
    "method.response.header.Access-Control-Allow-Origin" = true
  }
}
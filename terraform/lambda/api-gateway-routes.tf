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

# /pagamentos
resource "aws_api_gateway_resource" "pagamentos" {
  rest_api_id = aws_api_gateway_rest_api.lanchonete_api.id
  parent_id   = aws_api_gateway_rest_api.lanchonete_api.root_resource_id
  path_part   = "pagamentos"
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

# POST /pagamentos (processar pagamento)
resource "aws_api_gateway_method" "pagamentos_post" {
  rest_api_id      = aws_api_gateway_rest_api.lanchonete_api.id
  resource_id      = aws_api_gateway_resource.pagamentos.id
  http_method      = "POST"
  authorization    = "CUSTOM"
  authorizer_id    = aws_api_gateway_authorizer.jwt_authorizer.id
  api_key_required = false
}

# GET /pagamentos (consultar status pagamento - via query param)
resource "aws_api_gateway_method" "pagamentos_get" {
  rest_api_id      = aws_api_gateway_rest_api.lanchonete_api.id
  resource_id      = aws_api_gateway_resource.pagamentos.id
  http_method      = "GET"
  authorization    = "CUSTOM"
  authorizer_id    = aws_api_gateway_authorizer.jwt_authorizer.id
  api_key_required = false

  request_parameters = {
    "method.request.querystring.pedidoId" = false
  }
}

# ============================================
# INTEGRAÇÕES HTTP_PROXY PARA EKS VIA NLB
# ============================================

# HTTP_PROXY integration para produtos (autoatendimento via NLB)
resource "aws_api_gateway_integration" "produtos_categoria_integration" {
  rest_api_id = aws_api_gateway_rest_api.lanchonete_api.id
  resource_id = aws_api_gateway_resource.produtos_categoria_param.id
  http_method = aws_api_gateway_method.produtos_categoria_get.http_method
  type        = "HTTP_PROXY"

  integration_http_method = "GET"
  uri = "http://${data.aws_lb.nlb.dns_name}:30080/produtos/categoria/{categoria}"
  
  connection_type = "VPC_LINK"
  connection_id   = data.aws_api_gateway_vpc_link.eks_vpc_link.id

  request_parameters = {
    "integration.request.path.categoria" = "method.request.path.categoria"
    # CONTEXT INJECTION - Headers injetados para aplicação
    "integration.request.header.X-Cliente-ID"   = "context.authorizer.clienteId"
    "integration.request.header.X-CPF"          = "context.authorizer.cpf"
    "integration.request.header.X-Nome"         = "context.authorizer.nome"
    "integration.request.header.X-Auth-Type"    = "context.authorizer.authType"
    "integration.request.header.X-Session-ID"   = "context.authorizer.sessionId"
  }
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

# HTTP_PROXY integration para clientes (autoatendimento via NLB)
resource "aws_api_gateway_integration" "clientes_integration" {
  rest_api_id = aws_api_gateway_rest_api.lanchonete_api.id
  resource_id = aws_api_gateway_resource.clientes.id
  http_method = aws_api_gateway_method.clientes_get.http_method
  type        = "HTTP_PROXY"

  integration_http_method = "GET"
  uri = "http://${data.aws_lb.nlb.dns_name}:30080/clientes"
  
  connection_type = "VPC_LINK"
  connection_id   = data.aws_api_gateway_vpc_link.eks_vpc_link.id

  request_parameters = {
    # Query parameters passthrough
    "integration.request.querystring.cpf" = "method.request.querystring.cpf"
    # CONTEXT INJECTION - Headers injetados para aplicação
    "integration.request.header.X-Cliente-ID"   = "context.authorizer.clienteId"
    "integration.request.header.X-CPF"          = "context.authorizer.cpf"
    "integration.request.header.X-Nome"         = "context.authorizer.nome"
    "integration.request.header.X-Auth-Type"    = "context.authorizer.authType"
    "integration.request.header.X-Session-ID"   = "context.authorizer.sessionId"
  }
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

# HTTP_PROXY integration para pedidos GET (autoatendimento via NLB)
resource "aws_api_gateway_integration" "pedidos_get_integration" {
  rest_api_id = aws_api_gateway_rest_api.lanchonete_api.id
  resource_id = aws_api_gateway_resource.pedidos.id
  http_method = aws_api_gateway_method.pedidos_get.http_method
  type        = "HTTP_PROXY"

  integration_http_method = "GET"
  uri = "http://${data.aws_lb.nlb.dns_name}:30080/pedidos"
  
  connection_type = "VPC_LINK"
  connection_id   = data.aws_api_gateway_vpc_link.eks_vpc_link.id

  request_parameters = {
    # CONTEXT INJECTION - Headers injetados para aplicação
    "integration.request.header.X-Cliente-ID"   = "context.authorizer.clienteId"
    "integration.request.header.X-CPF"          = "context.authorizer.cpf"
    "integration.request.header.X-Nome"         = "context.authorizer.nome"
    "integration.request.header.X-Auth-Type"    = "context.authorizer.authType"
    "integration.request.header.X-Session-ID"   = "context.authorizer.sessionId"
  }
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

# HTTP_PROXY integration para pedidos POST (autoatendimento via NLB)
resource "aws_api_gateway_integration" "pedidos_post_integration" {
  rest_api_id = aws_api_gateway_rest_api.lanchonete_api.id
  resource_id = aws_api_gateway_resource.pedidos.id
  http_method = aws_api_gateway_method.pedidos_post.http_method
  type        = "HTTP_PROXY"

  integration_http_method = "POST"
  uri = "http://${data.aws_lb.nlb.dns_name}:30080/pedidos"
  
  connection_type = "VPC_LINK"
  connection_id   = data.aws_api_gateway_vpc_link.eks_vpc_link.id

  request_parameters = {
    # CONTEXT INJECTION - Headers injetados para aplicação
    "integration.request.header.X-Cliente-ID"   = "context.authorizer.clienteId"
    "integration.request.header.X-CPF"          = "context.authorizer.cpf"
    "integration.request.header.X-Nome"         = "context.authorizer.nome"
    "integration.request.header.X-Auth-Type"    = "context.authorizer.authType"
    "integration.request.header.X-Session-ID"   = "context.authorizer.sessionId"
  }
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

# ============================================
# INTEGRAÇÕES HTTP_PROXY PAGAMENTOS (porta 30081)
# ============================================

# HTTP_PROXY integration para pagamentos POST (pagamento via NLB)
resource "aws_api_gateway_integration" "pagamentos_post_integration" {
  rest_api_id = aws_api_gateway_rest_api.lanchonete_api.id
  resource_id = aws_api_gateway_resource.pagamentos.id
  http_method = aws_api_gateway_method.pagamentos_post.http_method
  type        = "HTTP_PROXY"

  integration_http_method = "POST"
  uri = "http://${data.aws_lb.nlb.dns_name}:30081/pagamentos"
  
  connection_type = "VPC_LINK"
  connection_id   = data.aws_api_gateway_vpc_link.eks_vpc_link.id

  request_parameters = {
    # CONTEXT INJECTION - Headers injetados para aplicação
    "integration.request.header.X-Cliente-ID"   = "context.authorizer.clienteId"
    "integration.request.header.X-CPF"          = "context.authorizer.cpf"
    "integration.request.header.X-Nome"         = "context.authorizer.nome"
    "integration.request.header.X-Auth-Type"    = "context.authorizer.authType"
    "integration.request.header.X-Session-ID"   = "context.authorizer.sessionId"
  }
}

# HTTP_PROXY integration para pagamentos GET (consultar status via NLB)
resource "aws_api_gateway_integration" "pagamentos_get_integration" {
  rest_api_id = aws_api_gateway_rest_api.lanchonete_api.id
  resource_id = aws_api_gateway_resource.pagamentos.id
  http_method = aws_api_gateway_method.pagamentos_get.http_method
  type        = "HTTP_PROXY"

  integration_http_method = "GET"
  uri = "http://${data.aws_lb.nlb.dns_name}:30081/pagamentos"
  
  connection_type = "VPC_LINK"
  connection_id   = data.aws_api_gateway_vpc_link.eks_vpc_link.id

  request_parameters = {
    # Query parameters passthrough
    "integration.request.querystring.pedidoId" = "method.request.querystring.pedidoId"
    # CONTEXT INJECTION - Headers injetados para aplicação
    "integration.request.header.X-Cliente-ID"   = "context.authorizer.clienteId"
    "integration.request.header.X-CPF"          = "context.authorizer.cpf"
    "integration.request.header.X-Nome"         = "context.authorizer.nome"
    "integration.request.header.X-Auth-Type"    = "context.authorizer.authType"
    "integration.request.header.X-Session-ID"   = "context.authorizer.sessionId"
  }
}

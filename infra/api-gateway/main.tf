terraform {
  required_version = ">= 1.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  backend "s3" {
    bucket         = "lanchonete-terraform-state-poc"
    key            = "api-gateway/terraform.tfstate"
    region         = "us-east-1"
    dynamodb_table = "lanchonete-terraform-locks"
    encrypt        = true
  }
}

provider "aws" {
  region = var.regiao
}

# Buscar outputs do módulo auth (Cognito)
data "terraform_remote_state" "auth" {
  backend = "s3"
  config = {
    bucket = "lanchonete-terraform-state-poc"
    key    = "auth/terraform.tfstate"
    region = "us-east-1"
  }
}

# Buscar outputs do módulo lambda
data "terraform_remote_state" "lambda" {
  backend = "s3"
  config = {
    bucket = "lanchonete-terraform-state-poc"
    key    = "lambda/terraform.tfstate"
    region = "us-east-1"
  }
}

# Buscar ALB do autoatendimento por tags
data "aws_lb" "autoatendimento" {
  tags = {
    "ingress.k8s.aws/stack" = "lanchonete-autoatendimento"
  }
}

# Buscar ALB do pagamento por tags
data "aws_lb" "pagamento" {
  tags = {
    "ingress.k8s.aws/stack" = "lanchonete"
  }
}

# API Gateway REST API
resource "aws_api_gateway_rest_api" "lanchonete_api" {
  name        = "${var.nome_projeto}-api"
  description = "API Gateway para lanchonete com autenticação Cognito"

  endpoint_configuration {
    types = ["REGIONAL"]
  }

  tags = local.common_tags
}

# Authorizer do Cognito
resource "aws_api_gateway_authorizer" "cognito_authorizer" {
  name                   = "${var.nome_projeto}-cognito-authorizer"
  rest_api_id            = aws_api_gateway_rest_api.lanchonete_api.id
  type                   = "COGNITO_USER_POOLS"
  provider_arns          = [data.terraform_remote_state.auth.outputs.user_pool_arn]
  identity_source        = "method.request.header.Authorization"
  authorizer_credentials = ""
}

# ================================
# RECURSOS DE AUTENTICAÇÃO
# ================================

# Resource /auth
resource "aws_api_gateway_resource" "auth_resource" {
  rest_api_id = aws_api_gateway_rest_api.lanchonete_api.id
  parent_id   = aws_api_gateway_rest_api.lanchonete_api.root_resource_id
  path_part   = "auth"
}

# Resource /auth/identificar
resource "aws_api_gateway_resource" "identificar_resource" {
  rest_api_id = aws_api_gateway_rest_api.lanchonete_api.id
  parent_id   = aws_api_gateway_resource.auth_resource.id
  path_part   = "identificar"
}

# Method POST /auth/identificar (sem autorização)
resource "aws_api_gateway_method" "identificar_post" {
  rest_api_id   = aws_api_gateway_rest_api.lanchonete_api.id
  resource_id   = aws_api_gateway_resource.identificar_resource.id
  http_method   = "POST"
  authorization = "NONE"

  request_models = {
    "application/json" = "Empty"
  }
}

# Integration com Lambda
resource "aws_api_gateway_integration" "identificar_lambda_integration" {
  rest_api_id = aws_api_gateway_rest_api.lanchonete_api.id
  resource_id = aws_api_gateway_resource.identificar_resource.id
  http_method = aws_api_gateway_method.identificar_post.http_method

  integration_http_method = "POST"
  type                    = "AWS_PROXY"
  uri                     = data.terraform_remote_state.lambda.outputs.lambda_invoke_arn
}

# ================================
# RECURSOS PROTEGIDOS - AUTOATENDIMENTO
# ================================

# Resource /autoatendimento
resource "aws_api_gateway_resource" "autoatendimento_resource" {
  rest_api_id = aws_api_gateway_rest_api.lanchonete_api.id
  parent_id   = aws_api_gateway_rest_api.lanchonete_api.root_resource_id
  path_part   = "autoatendimento"
}

# Resource /autoatendimento/{proxy+}
resource "aws_api_gateway_resource" "autoatendimento_proxy" {
  rest_api_id = aws_api_gateway_rest_api.lanchonete_api.id
  parent_id   = aws_api_gateway_resource.autoatendimento_resource.id
  path_part   = "{proxy+}"
}

# Method ANY /autoatendimento/{proxy+} (com autorização Cognito)
resource "aws_api_gateway_method" "autoatendimento_any" {
  rest_api_id   = aws_api_gateway_rest_api.lanchonete_api.id
  resource_id   = aws_api_gateway_resource.autoatendimento_proxy.id
  http_method   = "ANY"
  authorization = "COGNITO_USER_POOLS"
  authorizer_id = aws_api_gateway_authorizer.cognito_authorizer.id

  request_parameters = {
    "method.request.path.proxy" = true
  }
}

# Integration com ALB Autoatendimento
resource "aws_api_gateway_integration" "autoatendimento_alb_integration" {
  rest_api_id = aws_api_gateway_rest_api.lanchonete_api.id
  resource_id = aws_api_gateway_resource.autoatendimento_proxy.id
  http_method = aws_api_gateway_method.autoatendimento_any.http_method

  type                    = "HTTP_PROXY"
  integration_http_method = "ANY"
  uri                     = "http://${data.aws_lb.autoatendimento.dns_name}/{proxy}"

  request_parameters = {
    "integration.request.path.proxy" = "method.request.path.proxy"
  }
}

# ================================
# RECURSOS PROTEGIDOS - PAGAMENTO
# ================================

# Resource /pagamento
resource "aws_api_gateway_resource" "pagamento_resource" {
  rest_api_id = aws_api_gateway_rest_api.lanchonete_api.id
  parent_id   = aws_api_gateway_rest_api.lanchonete_api.root_resource_id
  path_part   = "pagamento"
}

# Resource /pagamento/{proxy+}
resource "aws_api_gateway_resource" "pagamento_proxy" {
  rest_api_id = aws_api_gateway_rest_api.lanchonete_api.id
  parent_id   = aws_api_gateway_resource.pagamento_resource.id
  path_part   = "{proxy+}"
}

# Method ANY /pagamento/{proxy+} (com autorização Cognito)
resource "aws_api_gateway_method" "pagamento_any" {
  rest_api_id   = aws_api_gateway_rest_api.lanchonete_api.id
  resource_id   = aws_api_gateway_resource.pagamento_proxy.id
  http_method   = "ANY"
  authorization = "COGNITO_USER_POOLS"
  authorizer_id = aws_api_gateway_authorizer.cognito_authorizer.id

  request_parameters = {
    "method.request.path.proxy" = true
  }
}

# Integration com ALB Pagamento
resource "aws_api_gateway_integration" "pagamento_alb_integration" {
  rest_api_id = aws_api_gateway_rest_api.lanchonete_api.id
  resource_id = aws_api_gateway_resource.pagamento_proxy.id
  http_method = aws_api_gateway_method.pagamento_any.http_method

  type                    = "HTTP_PROXY"
  integration_http_method = "ANY"
  uri                     = "http://${data.aws_lb.pagamento.dns_name}/{proxy}"

  request_parameters = {
    "integration.request.path.proxy" = "method.request.path.proxy"
  }
}

# ================================
# CORS E DEPLOYMENT
# ================================

# Deployment
resource "aws_api_gateway_deployment" "lanchonete_deployment" {
  depends_on = [
    aws_api_gateway_integration.identificar_lambda_integration,
    aws_api_gateway_integration.autoatendimento_alb_integration,
    aws_api_gateway_integration.pagamento_alb_integration,
  ]

  rest_api_id = aws_api_gateway_rest_api.lanchonete_api.id

  triggers = {
    redeployment = sha1(jsonencode([
      aws_api_gateway_resource.auth_resource.id,
      aws_api_gateway_resource.identificar_resource.id,
      aws_api_gateway_method.identificar_post.id,
      aws_api_gateway_integration.identificar_lambda_integration.id,
      aws_api_gateway_resource.autoatendimento_resource.id,
      aws_api_gateway_resource.autoatendimento_proxy.id,
      aws_api_gateway_method.autoatendimento_any.id,
      aws_api_gateway_integration.autoatendimento_alb_integration.id,
      aws_api_gateway_resource.pagamento_resource.id,
      aws_api_gateway_resource.pagamento_proxy.id,
      aws_api_gateway_method.pagamento_any.id,
      aws_api_gateway_integration.pagamento_alb_integration.id,
    ]))
  }

  lifecycle {
    create_before_destroy = true
  }
}

# Stage
resource "aws_api_gateway_stage" "lanchonete_stage" {
  deployment_id = aws_api_gateway_deployment.lanchonete_deployment.id
  rest_api_id   = aws_api_gateway_rest_api.lanchonete_api.id
  stage_name    = "v1"

  tags = local.common_tags
}
# Output values for testing and reference
output "api_gateway_url" {
  description = "URL da API Gateway para testes"
  value       = aws_api_gateway_deployment.produtos_deployment.invoke_url
}

output "lambda_function_name" {
  description = "Nome da função Lambda"
  value       = aws_lambda_function.produtos_crud.function_name
}

output "lambda_function_arn" {
  description = "ARN da função Lambda"
  value       = aws_lambda_function.produtos_crud.arn
}

# Endpoints disponíveis
output "endpoints" {
  description = "Endpoints CRUD disponíveis"
  value = {
    "POST criar"     = "${aws_api_gateway_deployment.produtos_deployment.invoke_url}/produtos"
    "PUT editar"     = "${aws_api_gateway_deployment.produtos_deployment.invoke_url}/produtos/{id}"
    "DELETE remover" = "${aws_api_gateway_deployment.produtos_deployment.invoke_url}/produtos/{id}"
    "GET categoria"  = "${aws_api_gateway_deployment.produtos_deployment.invoke_url}/produtos/categoria/{categoria}"
  }
}
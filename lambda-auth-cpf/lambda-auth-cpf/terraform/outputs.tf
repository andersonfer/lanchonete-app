# Output values for testing and reference
output "api_gateway_url" {
  description = "URL da API Gateway para testes"
  value       = "${aws_api_gateway_deployment.auth_deployment.invoke_url}/auth/cpf"
}

output "lambda_function_name" {
  description = "Nome da função Lambda"
  value       = aws_lambda_function.auth_cpf.function_name
}

output "lambda_function_arn" {
  description = "ARN da função Lambda"
  value       = aws_lambda_function.auth_cpf.arn
}
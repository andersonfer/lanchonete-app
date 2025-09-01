output "api_gateway_url" {
  description = "URL base do API Gateway"
  value       = "https://${aws_api_gateway_rest_api.lanchonete_api.id}.execute-api.${var.aws_region}.amazonaws.com/${aws_api_gateway_stage.api_stage.stage_name}"
}

output "lambda_function_name" {
  description = "Nome da função Lambda"
  value       = aws_lambda_function.auth_lambda.function_name
}

output "lambda_function_arn" {
  description = "ARN da função Lambda"
  value       = aws_lambda_function.auth_lambda.arn
}
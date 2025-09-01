variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "us-east-1"
}

variable "db_username" {
  description = "Database username"
  type        = string
  sensitive   = true
}

variable "db_password" {
  description = "Database password" 
  type        = string
  sensitive   = true
}

variable "jwt_secret" {
  description = "JWT signing secret"
  type        = string
  sensitive   = true
}

variable "lambda_jar_path" {
  description = "Path to Lambda JAR file"
  type        = string
  default     = "../../lambda-auth/target/lambda-auth-1.0.0.jar"
}
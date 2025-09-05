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

variable "project_name" {
  description = "Project name"
  type        = string
  default     = "lanchonete"
}

variable "db_name" {
  description = "Database name"
  type        = string
  default     = "lanchonete"
}

variable "db_instance_class" {
  description = "Database instance class"
  type        = string
  default     = "db.t3.micro"
}

variable "db_allocated_storage" {
  description = "Database allocated storage in GB"
  type        = number
  default     = 20
}

variable "lambda_memory" {
  description = "Lambda memory in MB"
  type        = number
  default     = 512
}

variable "lambda_timeout" {
  description = "Lambda timeout in seconds"
  type        = number
  default     = 30
}

variable "cluster_version" {
  description = "EKS cluster version"
  type        = string
  default     = "1.28"
}

variable "node_instance_type" {
  description = "EKS node instance type"
  type        = string
  default     = "t3.medium"
}

variable "node_min_size" {
  description = "EKS node group min size"
  type        = number
  default     = 1
}

variable "node_max_size" {
  description = "EKS node group max size"
  type        = number
  default     = 3
}

variable "node_desired_size" {
  description = "EKS node group desired size"
  type        = number
  default     = 2
}
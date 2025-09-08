# Backend remoto S3 para estado do Terraform
terraform {
  backend "s3" {
    bucket         = "lanchonete-terraform-state"
    key            = "eks/terraform.tfstate"
    region         = "us-east-1"
    dynamodb_table = "lanchonete-terraform-locks"
    encrypt        = true
  }
}
terraform {
  backend "s3" {
    bucket         = "lanchonete-terraform-state-poc"
    key            = "database/terraform.tfstate"
    region         = "us-east-1"
    dynamodb_table = "lanchonete-terraform-locks"
    encrypt        = true
  }
}
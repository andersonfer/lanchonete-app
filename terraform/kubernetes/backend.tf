terraform {
  backend "s3" {
    bucket         = "lanchonete-tfstate"
    key            = "kubernetes/terraform.tfstate"
    region         = "us-east-1"
    dynamodb_table = "lanchonete-terraform-locks"
    encrypt        = true
  }
}
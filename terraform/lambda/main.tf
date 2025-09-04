terraform {
  required_version = ">= 1.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

data "aws_iam_role" "lab_role" {
  name = "LabRole"
}

data "aws_vpc" "default" {
  default = true
}

data "aws_subnets" "default" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }
}


data "aws_db_instance" "mysql" {
  db_instance_identifier = "lanchonete-mysql"
}

# Data source para obter o NLB criado no módulo kubernetes
data "aws_lb" "nlb" {
  name = "lanchonete-nlb"
}

# Data source para obter o VPC Link criado no módulo kubernetes
data "aws_api_gateway_vpc_link" "eks_vpc_link" {
  name = "lanchonete-eks-vpc-link"
}
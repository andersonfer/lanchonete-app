# ==============================================================================
# APPLICATION INFRASTRUCTURE - ALB Controller + Load Balancers
# ==============================================================================

terraform {
  required_version = ">= 1.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    helm = {
      source  = "hashicorp/helm"
      version = "~> 2.0"
    }
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.0"
    }
  }

  backend "s3" {
    bucket         = "lanchonete-terraform-state-poc"
    key            = "04-applications/terraform.tfstate"
    region         = "us-east-1"
    dynamodb_table = "lanchonete-terraform-locks"
    encrypt        = true
  }
}

# Variables
variable "regiao_aws" {
  description = "Região AWS para deploy"
  type        = string
  default     = "us-east-1"
}

variable "nome_projeto" {
  description = "Nome do projeto"
  type        = string
  default     = "lanchonete"
}

# Buscar dados do cluster EKS
data "terraform_remote_state" "kubernetes" {
  backend = "s3"
  config = {
    bucket = "lanchonete-terraform-state-poc"
    key    = "02-kubernetes/terraform.tfstate"
    region = "us-east-1"
  }
}

locals {
  cluster_name = data.terraform_remote_state.kubernetes.outputs.cluster_name

  common_tags = {
    Projeto   = var.nome_projeto
    ManagedBy = "terraform"
  }
}

# AWS Provider
provider "aws" {
  region = var.regiao_aws
}

# EKS cluster data
data "aws_eks_cluster" "cluster" {
  name = local.cluster_name
}

data "aws_eks_cluster_auth" "cluster" {
  name = local.cluster_name
}

data "aws_vpc" "default" {
  default = true
}

# Kubernetes Provider
provider "kubernetes" {
  host                   = data.aws_eks_cluster.cluster.endpoint
  cluster_ca_certificate = base64decode(data.aws_eks_cluster.cluster.certificate_authority[0].data)
  token                  = data.aws_eks_cluster_auth.cluster.token
}

# Helm Provider
provider "helm" {
  kubernetes {
    host                   = data.aws_eks_cluster.cluster.endpoint
    cluster_ca_certificate = base64decode(data.aws_eks_cluster.cluster.certificate_authority[0].data)
    token                  = data.aws_eks_cluster_auth.cluster.token
  }
}

# Obter LabRole
data "aws_iam_role" "lab_role" {
  name = "LabRole"
}

# ==============================================================================
# SERVICE ACCOUNT para AWS Load Balancer Controller
# ==============================================================================

resource "kubernetes_service_account" "aws_load_balancer_controller" {
  metadata {
    name      = "aws-load-balancer-controller"
    namespace = "kube-system"

    labels = {
      "app.kubernetes.io/name"       = "aws-load-balancer-controller"
      "app.kubernetes.io/component"  = "controller"
    }

    annotations = {
      "eks.amazonaws.com/role-arn" = data.aws_iam_role.lab_role.arn
    }
  }
}

# ==============================================================================
# AWS LOAD BALANCER CONTROLLER via Helm
# ==============================================================================

resource "helm_release" "aws_load_balancer_controller" {
  name       = "aws-load-balancer-controller"
  repository = "https://aws.github.io/eks-charts"
  chart      = "aws-load-balancer-controller"
  namespace  = "kube-system"
  version    = "1.6.2"

  set {
    name  = "clusterName"
    value = local.cluster_name
  }

  set {
    name  = "serviceAccount.create"
    value = "false"
  }

  set {
    name  = "serviceAccount.name"
    value = kubernetes_service_account.aws_load_balancer_controller.metadata[0].name
  }

  set {
    name  = "region"
    value = var.regiao_aws
  }

  set {
    name  = "vpcId"
    value = data.aws_vpc.default.id
  }

  depends_on = [
    kubernetes_service_account.aws_load_balancer_controller
  ]

  tags = local.common_tags
}
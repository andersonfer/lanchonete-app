# ==============================================================================
# AWS LOAD BALANCER CONTROLLER - Application Load Balancer para EKS
# ==============================================================================

# Data sources para cluster EKS existente
data "aws_eks_cluster" "cluster" {
  name = var.cluster_name
}

data "aws_eks_cluster_auth" "cluster" {
  name = var.cluster_name
}

# Provider configurations usando cluster existente
provider "kubernetes" {
  host                   = data.aws_eks_cluster.cluster.endpoint
  cluster_ca_certificate = base64decode(data.aws_eks_cluster.cluster.certificate_authority[0].data)
  token                  = data.aws_eks_cluster_auth.cluster.token
}

provider "helm" {
  kubernetes {
    host                   = data.aws_eks_cluster.cluster.endpoint
    cluster_ca_certificate = base64decode(data.aws_eks_cluster.cluster.certificate_authority[0].data)
    token                  = data.aws_eks_cluster_auth.cluster.token
  }
}


# ==============================================================================
# SERVICE ACCOUNT
# ==============================================================================

resource "kubernetes_service_account" "aws_load_balancer_controller" {
  metadata {
    name      = "aws-load-balancer-controller"
    namespace = "kube-system"
    
    labels = {
      "app.kubernetes.io/name"       = "aws-load-balancer-controller"
      "app.kubernetes.io/component"  = "controller"
      "app.kubernetes.io/managed-by" = "terraform"
    }
  }
}

# ==============================================================================
# AWS LOAD BALANCER CONTROLLER
# ==============================================================================

resource "helm_release" "aws_load_balancer_controller" {
  name       = "aws-load-balancer-controller"
  repository = "https://aws.github.io/eks-charts"
  chart      = "aws-load-balancer-controller"
  namespace  = "kube-system"
  version    = "1.6.2"

  set {
    name  = "clusterName"
    value = var.cluster_name
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

  # Aguardar criação do ServiceAccount
  depends_on = [kubernetes_service_account.aws_load_balancer_controller]
}

# ==============================================================================
# DADOS DA VPC E CONFIGURAÇÃO DAS SUBNETS
# ==============================================================================

data "aws_vpc" "default" {
  default = true
}

data "aws_subnets" "publicas" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }
  
  filter {
    name   = "default-for-az"
    values = ["true"]
  }
}

# Adicionar tags necessárias para o AWS Load Balancer Controller
resource "aws_ec2_tag" "subnet_elb_public" {
  count       = length(data.aws_subnets.publicas.ids)
  resource_id = data.aws_subnets.publicas.ids[count.index]
  key         = "kubernetes.io/role/elb"
  value       = "1"
}

resource "aws_ec2_tag" "subnet_cluster" {
  count       = length(data.aws_subnets.publicas.ids)
  resource_id = data.aws_subnets.publicas.ids[count.index]
  key         = "kubernetes.io/cluster/${var.cluster_name}"
  value       = "shared"
}
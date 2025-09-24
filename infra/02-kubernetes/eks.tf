# ==============================================================================
# EKS CLUSTER - Elastic Kubernetes Service
# ==============================================================================

# Busca o LabRole existente no ambiente AWS
data "aws_iam_role" "lab_role" {
  name = "LabRole"
}

# Busca a VPC padrão
data "aws_vpc" "padrao" {
  default = true
}

# Busca subnets em zonas suportadas pelo EKS
data "aws_subnets" "disponiveis" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.padrao.id]
  }

  filter {
    name   = "availability-zone"
    values = ["us-east-1a", "us-east-1b", "us-east-1c", "us-east-1d", "us-east-1f"]
  }
}

# Security Group para o cluster EKS
resource "aws_security_group" "eks_cluster" {
  name_prefix = "${local.prefix}-eks-"
  description = "Security group para cluster EKS"
  vpc_id      = data.aws_vpc.padrao.id

  # Permite todo tráfego de saída
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(
    local.common_tags,
    {
      Name = "${local.prefix}-eks-sg"
    }
  )
}

# Cluster EKS usando LabRole
resource "aws_eks_cluster" "principal" {
  name     = "${local.prefix}-cluster"
  role_arn = data.aws_iam_role.lab_role.arn
  version  = "1.28"

  vpc_config {
    subnet_ids              = data.aws_subnets.disponiveis.ids
    security_group_ids      = [aws_security_group.eks_cluster.id]
    endpoint_private_access = true
    endpoint_public_access  = true
    public_access_cidrs     = ["0.0.0.0/0"]
  }

  tags = merge(
    local.common_tags,
    {
      Name = "${local.prefix}-cluster"
    }
  )
}

# Node Group usando LabRole
resource "aws_eks_node_group" "aplicacao" {
  cluster_name    = aws_eks_cluster.principal.name
  node_group_name = "${local.prefix}-nodes"
  node_role_arn   = data.aws_iam_role.lab_role.arn
  subnet_ids      = data.aws_subnets.disponiveis.ids

  # Configuração mínima
  scaling_config {
    desired_size = 2
    max_size     = 3
    min_size     = 1
  }

  instance_types = ["t3.medium"]
  disk_size      = 20

  tags = merge(
    local.common_tags,
    {
      Name = "${local.prefix}-nodes"
    }
  )
}

# ===== OUTPUTS PARA PIPELINE CI/CD =====

# Outputs específicos do EKS movidos para outputs.tf
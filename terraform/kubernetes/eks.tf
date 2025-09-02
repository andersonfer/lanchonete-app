# Security Group para o cluster EKS
resource "aws_security_group" "eks_cluster" {
  name_prefix = "lanchonete-eks-cluster-"
  description = "Security group para cluster EKS"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "HTTPS"
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Todo trafego de saida"
  }

  tags = {
    Name = "lanchonete-eks-cluster-sg"
  }
}

# Security Group para os nós do EKS
resource "aws_security_group" "eks_nodes" {
  name_prefix = "lanchonete-eks-nodes-"
  description = "Security group para nos worker do EKS"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    from_port       = 0
    to_port         = 65535
    protocol        = "tcp"
    security_groups = [aws_security_group.eks_cluster.id]
    description     = "Permite cluster se comunicar com nos"
  }

  ingress {
    from_port = 0
    to_port   = 65535
    protocol  = "tcp"
    self      = true
    description = "Permite nos se comunicarem entre si"
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Todo trafego de saida"
  }

  tags = {
    Name = "lanchonete-eks-nodes-sg"
  }
}

# Cluster EKS
resource "aws_eks_cluster" "lanchonete_cluster" {
  name     = var.cluster_name
  version  = var.kubernetes_version
  role_arn = data.aws_iam_role.lab_role.arn

  vpc_config {
    subnet_ids              = data.aws_subnets.default.ids
    endpoint_private_access = true
    endpoint_public_access  = true
    security_group_ids      = [aws_security_group.eks_cluster.id]
  }

  enabled_cluster_log_types = ["api", "audit", "authenticator", "controllerManager", "scheduler"]

  depends_on = [
    aws_security_group.eks_cluster
  ]

  tags = {
    Name = var.cluster_name
  }
}

# Node Group
resource "aws_eks_node_group" "lanchonete_nodes" {
  cluster_name    = aws_eks_cluster.lanchonete_cluster.name
  node_group_name = var.node_group_name
  node_role_arn   = data.aws_iam_role.lab_role.arn
  subnet_ids      = data.aws_subnets.default.ids

  capacity_type  = var.node_capacity_type
  instance_types = var.node_instance_types
  disk_size      = var.node_disk_size

  scaling_config {
    desired_size = var.node_desired_size
    max_size     = var.node_max_size
    min_size     = var.node_min_size
  }

  update_config {
    max_unavailable = 1
  }


  depends_on = [
    aws_eks_cluster.lanchonete_cluster,
    aws_security_group.eks_nodes
  ]

  tags = {
    Name = "${var.cluster_name}-${var.node_group_name}"
  }
}
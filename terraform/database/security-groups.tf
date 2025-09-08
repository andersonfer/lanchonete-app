# Security Groups para RDS MySQL

# Data source para acessar outputs do módulo kubernetes
data "terraform_remote_state" "kubernetes" {
  backend = "s3"
  config = {
    bucket = "${var.project_name}-tfstate"
    key    = "kubernetes/terraform.tfstate"
    region = var.aws_region
  }
}

resource "aws_security_group" "lambda" {
  name_prefix = "${var.project_name}-lambda-"
  description = "Security group para Lambda functions"
  vpc_id      = data.aws_vpc.default.id

  # Egress completo (como no exemplo que funciona)
  egress {
    description = "All outbound traffic"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.project_name}-lambda-sg"
  }
}

resource "aws_security_group" "rds" {
  name_prefix = "${var.project_name}-rds-"
  description = "Security group para RDS MySQL"
  vpc_id      = data.aws_vpc.default.id

  # Tráfego de saída (necessário para patches e updates)
  egress {
    description = "All outbound traffic"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.project_name}-rds-sg"
  }
}

# Security group rule: RDS aceita conexões apenas da Lambda

resource "aws_security_group_rule" "rds_from_lambda" {
  type                     = "ingress"
  from_port                = 3306
  to_port                  = 3306
  protocol                 = "tcp"
  source_security_group_id = aws_security_group.lambda.id
  security_group_id        = aws_security_group.rds.id
  description              = "MySQL access from Lambda"
}

# Security group rule: RDS aceita conexões dos nodes EKS (SG gerenciado pelo EKS)
resource "aws_security_group_rule" "rds_from_eks_managed_nodes" {
  count = try(data.terraform_remote_state.kubernetes.outputs.cluster_name, null) != null ? 1 : 0
  
  type                     = "ingress"
  from_port                = 3306
  to_port                  = 3306
  protocol                 = "tcp"
  source_security_group_id = try(data.terraform_remote_state.kubernetes.outputs.cluster_security_group_id, "")
  security_group_id        = aws_security_group.rds.id
  description              = "MySQL access from EKS managed nodes"
}

# Security group rule adicional: usar o SG real dos nodes 
resource "aws_security_group_rule" "rds_from_real_eks_nodes" {
  type              = "ingress"
  from_port         = 3306
  to_port           = 3306
  protocol          = "tcp"
  cidr_blocks       = ["172.31.0.0/16"]  # VPC CIDR - permite toda VPC temporariamente
  security_group_id = aws_security_group.rds.id
  description       = "MySQL access from EKS nodes (VPC wide)"
}
# =============================================================================
# SECURITY GROUPS - RDS MYSQL
# =============================================================================

resource "aws_security_group" "rds" {
  name_prefix = "${var.project_name}-rds-"
  description = "Security group para RDS MySQL"
  vpc_id      = data.aws_vpc.default.id

  # Acesso da VPC interna (para Lambda e EKS futuros)
  ingress {
    from_port   = 3306
    to_port     = 3306
    protocol    = "tcp"
    cidr_blocks = [data.aws_vpc.default.cidr_block]
  }
  
  # Acesso do IP atual (dinâmico)
  ingress {
    from_port   = 3306
    to_port     = 3306
    protocol    = "tcp"
    cidr_blocks = ["${chomp(data.http.my_ip.response_body)}/32"]
    description = "Acesso do IP atual"
  }
  
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
  
  tags = merge(var.common_tags, {
    Name = "${var.project_name}-rds-sg"
  })
}
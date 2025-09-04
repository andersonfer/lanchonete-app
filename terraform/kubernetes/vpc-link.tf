# VPC Link para conectar API Gateway ao EKS via NLB
resource "aws_api_gateway_vpc_link" "eks_vpc_link" {
  name        = "lanchonete-eks-vpc-link"
  description = "VPC Link para conectar API Gateway ao cluster EKS via NLB"
  target_arns = [aws_lb.lanchonete_nlb.arn]

  tags = {
    Name = "lanchonete-eks-vpc-link"
  }
}
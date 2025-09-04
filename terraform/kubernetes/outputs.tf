output "cluster_endpoint" {
  description = "Endpoint do cluster EKS"
  value       = aws_eks_cluster.lanchonete_cluster.endpoint
}

output "cluster_security_group_id" {
  description = "ID do security group do cluster EKS"
  value       = aws_security_group.eks_cluster.id
}

output "cluster_name" {
  description = "Nome do cluster EKS"
  value       = aws_eks_cluster.lanchonete_cluster.name
}

output "cluster_arn" {
  description = "ARN do cluster EKS"
  value       = aws_eks_cluster.lanchonete_cluster.arn
}

output "cluster_certificate_authority_data" {
  description = "Dados do certificado de autoridade do cluster"
  value       = aws_eks_cluster.lanchonete_cluster.certificate_authority[0].data
}

output "cluster_version" {
  description = "Versão do Kubernetes do cluster"
  value       = aws_eks_cluster.lanchonete_cluster.version
}

output "node_group_arn" {
  description = "ARN do node group"
  value       = aws_eks_node_group.lanchonete_nodes.arn
}

output "node_group_status" {
  description = "Status do node group"
  value       = aws_eks_node_group.lanchonete_nodes.status
}

output "nlb_arn" {
  description = "ARN do Network Load Balancer"
  value       = aws_lb.lanchonete_nlb.arn
}

output "nlb_dns_name" {
  description = "DNS name do Network Load Balancer"
  value       = aws_lb.lanchonete_nlb.dns_name
}

output "nlb_zone_id" {
  description = "Zone ID do Network Load Balancer"
  value       = aws_lb.lanchonete_nlb.zone_id
}

output "vpc_link_id" {
  description = "ID do VPC Link para API Gateway"
  value       = aws_api_gateway_vpc_link.eks_vpc_link.id
}

# ECR Outputs
output "ecr_autoatendimento_url" {
  description = "URL do repositório ECR para autoatendimento"
  value       = aws_ecr_repository.autoatendimento.repository_url
}

output "ecr_pagamento_url" {
  description = "URL do repositório ECR para pagamento"
  value       = aws_ecr_repository.pagamento.repository_url
}

output "account_id" {
  description = "AWS Account ID"
  value       = data.aws_caller_identity.current.account_id
}
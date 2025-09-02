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

output "alb_arn" {
  description = "ARN do Application Load Balancer"
  value       = aws_lb.lanchonete_alb.arn
}

output "alb_dns_name" {
  description = "DNS name do Application Load Balancer"
  value       = aws_lb.lanchonete_alb.dns_name
}

output "alb_zone_id" {
  description = "Zone ID do Application Load Balancer"
  value       = aws_lb.lanchonete_alb.zone_id
}

output "vpc_link_id" {
  description = "ID do VPC Link para API Gateway"
  value       = aws_api_gateway_vpc_link.eks_vpc_link.id
}
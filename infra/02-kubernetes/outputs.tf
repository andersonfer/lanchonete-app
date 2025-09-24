# ==============================================================================
# OUTPUTS - KUBERNETES INFRASTRUCTURE
# ==============================================================================

# Cluster EKS
output "cluster_name" {
  description = "Nome do cluster EKS"
  value       = aws_eks_cluster.principal.name
}

output "cluster_endpoint" {
  description = "Endpoint do cluster EKS"
  value       = aws_eks_cluster.principal.endpoint
}

output "cluster_status" {
  description = "Status do cluster EKS"
  value       = aws_eks_cluster.principal.status
}

# ECR Repositories
output "registry_url" {
  description = "URL base do registry ECR"
  value       = split("/", aws_ecr_repository.repos[0].repository_url)[0]
}

output "repositorio_autoatendimento" {
  description = "URL do repositório ECR autoatendimento"
  value       = aws_ecr_repository.repos[0].repository_url
}

output "repositorio_pagamento" {
  description = "URL do repositório ECR pagamento"
  value       = aws_ecr_repository.repos[1].repository_url
}

output "repositorios_nomes" {
  description = "Nomes dos repositórios ECR"
  value = {
    for idx, servico in local.servicos : servico => aws_ecr_repository.repos[idx].name
  }
}

output "repositorios_ecr" {
  description = "URLs de todos os repositórios ECR"
  value = {
    for idx, servico in local.servicos : servico => aws_ecr_repository.repos[idx].repository_url
  }
}

# Outputs adicionais do EKS
output "cluster_security_group_id" {
  description = "ID do security group do cluster"
  value       = aws_security_group.eks_cluster.id
}

output "vpc_id" {
  description = "ID da VPC utilizada"
  value       = data.aws_vpc.padrao.id
}

# ALB Controller
output "controller_service_account" {
  description = "Nome do ServiceAccount do AWS Load Balancer Controller"
  value       = kubernetes_service_account.aws_load_balancer_controller.metadata[0].name
}

output "controller_namespace" {
  description = "Namespace do AWS Load Balancer Controller"
  value       = kubernetes_service_account.aws_load_balancer_controller.metadata[0].namespace
}

output "helm_release_status" {
  description = "Status da instalação do Helm"
  value       = helm_release.aws_load_balancer_controller.status
}


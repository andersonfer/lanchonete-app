output "alb_controller_status" {
  description = "Status do AWS Load Balancer Controller"
  value       = helm_release.aws_load_balancer_controller.status
}

output "alb_controller_version" {
  description = "Versão do AWS Load Balancer Controller"
  value       = helm_release.aws_load_balancer_controller.version
}

output "cluster_name" {
  description = "Nome do cluster EKS"
  value       = local.cluster_name
}
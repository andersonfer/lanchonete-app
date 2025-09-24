output "alb_controller_status" {
  description = "Status do AWS Load Balancer Controller"
  value       = helm_release.aws_load_balancer_controller.status
}

output "cluster_name" {
  description = "Nome do cluster EKS"
  value       = local.cluster_name
}
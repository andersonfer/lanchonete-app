variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "us-east-1"
}

variable "cluster_name" {
  description = "Nome do cluster EKS"
  type        = string
  default     = "lanchonete-cluster"
}

variable "kubernetes_version" {
  description = "Versão do Kubernetes para o cluster EKS"
  type        = string
  default     = "1.30"
}

variable "node_group_name" {
  description = "Nome do node group"
  type        = string
  default     = "lanchonete-nodes"
}

variable "node_instance_types" {
  description = "Tipos de instância para os nós do cluster"
  type        = list(string)
  default     = ["t3.small"]
}

variable "node_capacity_type" {
  description = "Tipo de capacidade dos nós (ON_DEMAND ou SPOT)"
  type        = string
  default     = "ON_DEMAND"
}

variable "node_desired_size" {
  description = "Número desejado de nós"
  type        = number
  default     = 2
}

variable "node_min_size" {
  description = "Número mínimo de nós"
  type        = number
  default     = 1
}

variable "node_max_size" {
  description = "Número máximo de nós"
  type        = number
  default     = 4
}

variable "node_disk_size" {
  description = "Tamanho do disco dos nós em GB"
  type        = number
  default     = 20
}
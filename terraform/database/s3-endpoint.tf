# VPC Endpoint para S3 (Gateway) - permite Lambda acessar S3 sem internet

# Route tables para associar ao Gateway Endpoint
data "aws_route_tables" "default" {
  vpc_id = data.aws_vpc.default.id
}

# VPC Endpoint Gateway para S3
resource "aws_vpc_endpoint" "s3" {
  vpc_id            = data.aws_vpc.default.id
  service_name      = "com.amazonaws.${var.aws_region}.s3"
  vpc_endpoint_type = "Gateway"
  
  route_table_ids = data.aws_route_tables.default.ids
  
  tags = {
    Name = "${var.project_name}-s3-endpoint"
  }
}
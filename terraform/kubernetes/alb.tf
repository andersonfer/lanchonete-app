# Security Group para o ALB
resource "aws_security_group" "alb" {
  name_prefix = "lanchonete-alb-"
  description = "Security group para Application Load Balancer"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "HTTP"
  }

  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "HTTPS"
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Todo trafego de saida"
  }

  tags = {
    Name = "lanchonete-alb-sg"
  }
}

# Network Load Balancer (necessário para VPC Link)
resource "aws_lb" "lanchonete_nlb" {
  name               = "lanchonete-nlb"
  internal           = true
  load_balancer_type = "network"
  subnets            = data.aws_subnets.default.ids

  enable_deletion_protection = false

  tags = {
    Name = "lanchonete-nlb"
  }
}

# Target Group padrão para o NLB
resource "aws_lb_target_group" "default" {
  name     = "lanchonete-default-tg"
  port     = 80
  protocol = "TCP"
  vpc_id   = data.aws_vpc.default.id
  target_type = "ip"

  health_check {
    enabled             = true
    healthy_threshold   = 2
    unhealthy_threshold = 2
    interval            = 30
    port                = "traffic-port"
    protocol            = "TCP"
  }

  tags = {
    Name = "lanchonete-default-tg"
  }
}

# Listener padrão para o NLB
resource "aws_lb_listener" "default" {
  load_balancer_arn = aws_lb.lanchonete_nlb.arn
  port              = "80"
  protocol          = "TCP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.default.arn
  }

  tags = {
    Name = "lanchonete-nlb-listener"
  }
}
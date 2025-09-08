# Bucket S3 para armazenar migrations SQL

resource "aws_s3_bucket" "migrations" {
  bucket = "${var.project_name}-migrations"
}

resource "aws_s3_bucket_public_access_block" "migrations" {
  bucket = aws_s3_bucket.migrations.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# Upload dos arquivos SQL
resource "aws_s3_object" "schema_sql" {
  bucket = aws_s3_bucket.migrations.id
  key    = "001_create_schema.sql"
  source = "${path.module}/migrations/001_create_schema.sql"
  etag   = filemd5("${path.module}/migrations/001_create_schema.sql")
}

resource "aws_s3_object" "seed_sql" {
  bucket = aws_s3_bucket.migrations.id
  key    = "002_seed_data.sql"
  source = "${path.module}/migrations/002_seed_data.sql"
  etag   = filemd5("${path.module}/migrations/002_seed_data.sql")
}
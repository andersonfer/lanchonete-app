#!/bin/bash

set -e

PROJECT_PREFIX="lanchonete"
LOG_FILE="/tmp/aws-cleanup-$(date +%Y%m%d_%H%M%S).log"

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

check_resource_exists() {
    local resource_type="$1"
    local identifier="$2"
    
    case "$resource_type" in
        "rds")
            aws rds describe-db-instances --db-instance-identifier "$identifier" &>/dev/null
            ;;
        "sg")
            aws ec2 describe-security-groups --group-ids "$identifier" &>/dev/null
            ;;
        "log-group")
            aws logs describe-log-groups --log-group-name-prefix "$identifier" --query 'logGroups[0]' --output text &>/dev/null
            ;;
        "lambda")
            aws lambda get-function --function-name "$identifier" &>/dev/null
            ;;
        "eks")
            aws eks describe-cluster --name "$identifier" &>/dev/null
            ;;
        "nlb"|"alb")
            aws elbv2 describe-load-balancers --names "$identifier" &>/dev/null
            ;;
        "api-gateway")
            aws apigateway get-rest-api --rest-api-id "$identifier" &>/dev/null
            ;;
        "db-subnet-group")
            aws rds describe-db-subnet-groups --db-subnet-group-name "$identifier" &>/dev/null
            ;;
        "vpc-endpoint")
            aws ec2 describe-vpc-endpoints --vpc-endpoint-ids "$identifier" &>/dev/null
            ;;
    esac
}

wait_for_resource_deletion() {
    local resource_type="$1"
    local identifier="$2"
    local max_wait="$3"
    local waited=0
    
    log "Waiting for $resource_type ($identifier) to be fully deleted..."
    
    while [ $waited -lt $max_wait ]; do
        if ! check_resource_exists "$resource_type" "$identifier"; then
            log "$resource_type ($identifier) successfully deleted"
            return 0
        fi
        sleep 10
        waited=$((waited + 10))
        log "Still waiting... ($waited/${max_wait}s)"
    done
    
    log "WARNING: $resource_type ($identifier) deletion timeout after ${max_wait}s"
    return 1
}

cleanup_lambda_functions() {
    log "=== CLEANING UP LAMBDA FUNCTIONS ==="
    
    local functions=$(aws lambda list-functions --query "Functions[?starts_with(FunctionName, '$PROJECT_PREFIX')].FunctionName" --output text)
    
    if [ -n "$functions" ]; then
        for func in $functions; do
            log "Deleting Lambda function: $func"
            if aws lambda delete-function --function-name "$func" 2>/dev/null; then
                log "Lambda function $func deleted successfully"
            else
                log "WARNING: Failed to delete Lambda function $func"
            fi
        done
        sleep 5
    else
        log "No Lambda functions found with prefix $PROJECT_PREFIX"
    fi
}

cleanup_api_gateway() {
    log "=== CLEANING UP API GATEWAY ==="
    
    local apis=$(aws apigateway get-rest-apis --query "items[?contains(name, '$PROJECT_PREFIX')].id" --output text)
    
    if [ -n "$apis" ]; then
        for api in $apis; do
            log "Deleting API Gateway: $api"
            if aws apigateway delete-rest-api --rest-api-id "$api" 2>/dev/null; then
                log "API Gateway $api deleted successfully"
            else
                log "WARNING: Failed to delete API Gateway $api"
            fi
        done
        sleep 5
    else
        log "No API Gateway found with name containing $PROJECT_PREFIX"
    fi
}

cleanup_load_balancers() {
    log "=== CLEANING UP LOAD BALANCERS ==="
    
    local lbs=$(aws elbv2 describe-load-balancers --query "LoadBalancers[?contains(LoadBalancerName, '$PROJECT_PREFIX')].LoadBalancerArn" --output text)
    
    if [ -n "$lbs" ]; then
        for lb in $lbs; do
            local lb_name=$(aws elbv2 describe-load-balancers --load-balancer-arns "$lb" --query 'LoadBalancers[0].LoadBalancerName' --output text)
            log "Deleting Load Balancer: $lb_name"
            
            if aws elbv2 delete-load-balancer --load-balancer-arn "$lb" 2>/dev/null; then
                log "Load Balancer $lb_name deletion initiated"
                wait_for_resource_deletion "nlb" "$lb_name" 300
            else
                log "WARNING: Failed to delete Load Balancer $lb_name"
            fi
        done
    else
        log "No Load Balancers found with name containing $PROJECT_PREFIX"
    fi
}

cleanup_eks_clusters() {
    log "=== CLEANING UP EKS CLUSTERS ==="
    
    local clusters=$(aws eks list-clusters --query "clusters[?contains(@, '$PROJECT_PREFIX')]" --output text)
    
    if [ -n "$clusters" ]; then
        for cluster in $clusters; do
            log "Deleting EKS cluster: $cluster"
            
            # First delete node groups
            local nodegroups=$(aws eks list-nodegroups --cluster-name "$cluster" --query 'nodegroups' --output text 2>/dev/null || true)
            if [ -n "$nodegroups" ]; then
                for ng in $nodegroups; do
                    log "Deleting node group: $ng"
                    aws eks delete-nodegroup --cluster-name "$cluster" --nodegroup-name "$ng" &>/dev/null || true
                done
                sleep 30
            fi
            
            # Then delete cluster
            if aws eks delete-cluster --name "$cluster" 2>/dev/null; then
                log "EKS cluster $cluster deletion initiated"
                wait_for_resource_deletion "eks" "$cluster" 600
            else
                log "WARNING: Failed to delete EKS cluster $cluster"
            fi
        done
    else
        log "No EKS clusters found with name containing $PROJECT_PREFIX"
    fi
}

cleanup_rds_instances() {
    log "=== CLEANING UP RDS INSTANCES ==="
    
    local instances=$(aws rds describe-db-instances --query "DBInstances[?contains(DBInstanceIdentifier, '$PROJECT_PREFIX')].DBInstanceIdentifier" --output text)
    
    if [ -n "$instances" ]; then
        for instance in $instances; do
            log "Deleting RDS instance: $instance"
            
            if aws rds delete-db-instance --db-instance-identifier "$instance" --skip-final-snapshot --delete-automated-backups 2>/dev/null; then
                log "RDS instance $instance deletion initiated"
                wait_for_resource_deletion "rds" "$instance" 900
            else
                log "WARNING: Failed to delete RDS instance $instance"
            fi
        done
    else
        log "No RDS instances found with identifier containing $PROJECT_PREFIX"
    fi
}

cleanup_security_groups() {
    log "=== CLEANING UP SECURITY GROUPS ==="
    
    local sgs=$(aws ec2 describe-security-groups --query "SecurityGroups[?contains(GroupName, '$PROJECT_PREFIX') && GroupName != 'default'].GroupId" --output text)
    
    if [ -n "$sgs" ]; then
        for sg in $sgs; do
            local sg_name=$(aws ec2 describe-security-groups --group-ids "$sg" --query 'SecurityGroups[0].GroupName' --output text 2>/dev/null || echo "unknown")
            log "Deleting Security Group: $sg ($sg_name)"
            
            # Remove all rules first
            aws ec2 describe-security-groups --group-ids "$sg" --query 'SecurityGroups[0].IpPermissions' --output json > "/tmp/sg_rules_$sg.json" 2>/dev/null || true
            if [ -s "/tmp/sg_rules_$sg.json" ] && [ "$(cat /tmp/sg_rules_$sg.json)" != "[]" ]; then
                aws ec2 revoke-security-group-ingress --group-id "$sg" --ip-permissions file:///tmp/sg_rules_$sg.json &>/dev/null || true
            fi
            
            aws ec2 describe-security-groups --group-ids "$sg" --query 'SecurityGroups[0].IpPermissionsEgress' --output json > "/tmp/sg_egress_$sg.json" 2>/dev/null || true
            if [ -s "/tmp/sg_egress_$sg.json" ] && [ "$(cat /tmp/sg_egress_$sg.json)" != "[]" ]; then
                aws ec2 revoke-security-group-egress --group-id "$sg" --ip-permissions file:///tmp/sg_egress_$sg.json &>/dev/null || true
            fi
            
            sleep 2
            
            if aws ec2 delete-security-group --group-id "$sg" 2>/dev/null; then
                log "Security Group $sg deleted successfully"
            else
                log "WARNING: Failed to delete Security Group $sg (may have dependencies)"
            fi
            
            rm -f "/tmp/sg_rules_$sg.json" "/tmp/sg_egress_$sg.json"
        done
    else
        log "No Security Groups found with name containing $PROJECT_PREFIX"
    fi
}

cleanup_db_subnet_groups() {
    log "=== CLEANING UP DB SUBNET GROUPS ==="
    
    # Usar timeout para evitar travamento
    local subnet_groups
    if subnet_groups=$(timeout 30 aws rds describe-db-subnet-groups --query "DBSubnetGroups[?contains(DBSubnetGroupName, '$PROJECT_PREFIX')].DBSubnetGroupName" --output text 2>/dev/null); then
        if [ -n "$subnet_groups" ] && [ "$subnet_groups" != "None" ]; then
            for sg in $subnet_groups; do
                log "Deleting DB Subnet Group: $sg"
                if timeout 30 aws rds delete-db-subnet-group --db-subnet-group-name "$sg" 2>/dev/null; then
                    log "DB Subnet Group $sg deleted successfully"
                else
                    log "WARNING: Failed to delete DB Subnet Group $sg (may have dependencies)"
                fi
            done
        else
            log "No DB Subnet Groups found with name containing $PROJECT_PREFIX"
        fi
    else
        log "WARNING: Timeout or error querying DB Subnet Groups"
    fi
}

cleanup_vpc_endpoints() {
    log "=== CLEANING UP VPC ENDPOINTS ==="
    
    # Get VPC endpoints with project prefix in tags
    local endpoints=$(aws ec2 describe-vpc-endpoints --query "VpcEndpoints[?Tags[?Key=='Name' && contains(Value, '$PROJECT_PREFIX')]].VpcEndpointId" --output text)
    
    if [ -n "$endpoints" ]; then
        for endpoint in $endpoints; do
            local endpoint_name=$(aws ec2 describe-vpc-endpoints --vpc-endpoint-ids "$endpoint" --query 'VpcEndpoints[0].Tags[?Key==`Name`].Value | [0]' --output text 2>/dev/null || echo "unknown")
            log "Deleting VPC Endpoint: $endpoint ($endpoint_name)"
            
            if aws ec2 delete-vpc-endpoints --vpc-endpoint-ids "$endpoint" 2>/dev/null; then
                log "VPC Endpoint $endpoint deletion initiated"
                wait_for_resource_deletion "vpc-endpoint" "$endpoint" 300
            else
                log "WARNING: Failed to delete VPC Endpoint $endpoint"
            fi
        done
    else
        log "No VPC Endpoints found with tags containing $PROJECT_PREFIX"
    fi
}

cleanup_log_groups() {
    log "=== CLEANING UP CLOUDWATCH LOG GROUPS ==="
    
    local log_groups=$(aws logs describe-log-groups --query "logGroups[?contains(logGroupName, '$PROJECT_PREFIX')].logGroupName" --output text)
    
    if [ -n "$log_groups" ]; then
        for lg in $log_groups; do
            log "Deleting Log Group: $lg"
            if aws logs delete-log-group --log-group-name "$lg" 2>/dev/null; then
                log "Log Group $lg deleted successfully"
            else
                log "WARNING: Failed to delete Log Group $lg"
            fi
        done
    else
        log "No Log Groups found with name containing $PROJECT_PREFIX"
    fi
}

cleanup_s3_buckets() {
    log "=== CLEANING UP S3 BUCKETS ==="
    
    # Get S3 buckets with project prefix, EXCLUDING backend bucket
    local buckets
    if buckets=$(timeout 30 aws s3api list-buckets --query "Buckets[?starts_with(Name, '$PROJECT_PREFIX') && Name != '${PROJECT_PREFIX}-tfstate'].Name" --output text 2>/dev/null); then
        if [ -n "$buckets" ] && [ "$buckets" != "None" ]; then
            for bucket in $buckets; do
                log "Checking S3 bucket: $bucket"
                
                # Check if bucket contains objects
                local object_count
                if object_count=$(timeout 30 aws s3api list-objects-v2 --bucket "$bucket" --query 'Contents | length(@)' --output text 2>/dev/null); then
                    if [ "$object_count" = "None" ] || [ "$object_count" = "0" ] || [ -z "$object_count" ]; then
                        log "Deleting empty S3 bucket: $bucket"
                        if timeout 30 aws s3api delete-bucket --bucket "$bucket" 2>/dev/null; then
                            log "S3 bucket $bucket deleted successfully"
                        else
                            log "WARNING: Failed to delete empty S3 bucket $bucket"
                        fi
                    else
                        log "Deleting S3 bucket with objects: $bucket (objects: $object_count)"
                        # Delete all objects first, then bucket
                        if timeout 60 aws s3 rm "s3://$bucket" --recursive 2>/dev/null; then
                            log "Objects deleted from $bucket"
                            if timeout 30 aws s3api delete-bucket --bucket "$bucket" 2>/dev/null; then
                                log "S3 bucket $bucket deleted successfully"
                            else
                                log "WARNING: Failed to delete S3 bucket $bucket after emptying"
                            fi
                        else
                            log "WARNING: Failed to delete objects from S3 bucket $bucket"
                        fi
                    fi
                else
                    log "WARNING: Failed to check objects in S3 bucket $bucket"
                fi
            done
        else
            log "No S3 buckets found with prefix $PROJECT_PREFIX (excluding backend bucket)"
        fi
    else
        log "WARNING: Timeout or error querying S3 buckets"
    fi
}

cleanup_network_interfaces() {
    log "=== CLEANING UP ORPHANED NETWORK INTERFACES ==="
    
    # Only delete available (unattached) network interfaces
    local enis=$(aws ec2 describe-network-interfaces --filters "Name=status,Values=available" --query 'NetworkInterfaces[?contains(Description, `'$PROJECT_PREFIX'`)].NetworkInterfaceId' --output text)
    
    if [ -n "$enis" ]; then
        for eni in $enis; do
            log "Deleting orphaned Network Interface: $eni"
            if aws ec2 delete-network-interface --network-interface-id "$eni" 2>/dev/null; then
                log "Network Interface $eni deleted successfully"
            else
                log "WARNING: Failed to delete Network Interface $eni"
            fi
        done
    else
        log "No orphaned Network Interfaces found"
    fi
}

main() {
    log "Starting AWS cleanup for project: $PROJECT_PREFIX"
    log "Cleanup log: $LOG_FILE"
    
    # Order matters - delete in reverse dependency order
    cleanup_lambda_functions
    cleanup_api_gateway
    cleanup_load_balancers
    cleanup_eks_clusters
    cleanup_rds_instances
    cleanup_db_subnet_groups
    cleanup_vpc_endpoints
    cleanup_security_groups
    cleanup_log_groups
    cleanup_s3_buckets
    cleanup_network_interfaces
    
    log "AWS cleanup completed"
    log "Log file: $LOG_FILE"
}

# Show help
if [[ "$1" == "--help" || "$1" == "-h" ]]; then
    echo "Usage: $0 [--dry-run] [--project-prefix PREFIX]"
    echo ""
    echo "Options:"
    echo "  --dry-run              Show what would be deleted without actually deleting"
    echo "  --project-prefix PREFIX Use custom project prefix (default: $PROJECT_PREFIX)"
    echo "  --help, -h             Show this help message"
    exit 0
fi

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --dry-run)
            echo "DRY RUN MODE - No resources will be deleted"
            # Override AWS commands to just echo
            aws() { echo "DRY RUN: aws $@"; }
            export -f aws
            shift
            ;;
        --project-prefix)
            PROJECT_PREFIX="$2"
            shift 2
            ;;
        *)
            echo "Unknown option: $1"
            exit 1
            ;;
    esac
done

main
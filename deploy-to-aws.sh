#!/bin/bash

# Hotel Booking System - AWS Deployment Script
# This script builds Docker images and pushes them to Amazon ECR

set -e

AWS_REGION=${AWS_REGION:-us-east-1}
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
ECR_REGISTRY="$AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com"

echo "🚀 Starting Hotel Booking System deployment to AWS..."
echo "AWS Account: $AWS_ACCOUNT_ID"
echo "AWS Region: $AWS_REGION"
echo "ECR Registry: $ECR_REGISTRY"

# Function to create ECR repository if it doesn't exist
create_ecr_repo() {
    local repo_name=$1
    echo "📦 Creating ECR repository: $repo_name"

    if ! aws ecr describe-repositories --repository-names $repo_name --region $AWS_REGION >/dev/null 2>&1; then
        aws ecr create-repository --repository-name $repo_name --region $AWS_REGION
        echo "✅ Created ECR repository: $repo_name"
    else
        echo "✅ ECR repository already exists: $repo_name"
    fi
}

# Function to build and push Docker image
build_and_push() {
    local service_name=$1
    local service_dir=$2
    local repo_name="hrs-$service_name"

    echo "🔨 Building $service_name..."

    # Build the service
    cd $service_dir
    ../gradlew build -x test

    # Create ECR repository
    create_ecr_repo $repo_name

    # Login to ECR
    aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $ECR_REGISTRY

    # Build Docker image
    docker build -t $repo_name .
    docker tag $repo_name:latest $ECR_REGISTRY/$repo_name:latest

    # Push to ECR
    echo "📤 Pushing $service_name to ECR..."
    docker push $ECR_REGISTRY/$repo_name:latest

    echo "✅ Successfully deployed $service_name"
    cd ..
}

# Build all services
echo "🏗️ Building all services..."
./gradlew build -x test

# Build and push each service
build_and_push "user-service" "user-service"
build_and_push "hotel-service" "hotel-service"
build_and_push "booking-service" "booking-service"
build_and_push "api-gateway" "api-gateway"

echo "🎉 All services built and pushed to ECR successfully!"
echo ""
echo "Next steps:"
echo "1. Update the CDK stack with your ECR image URIs"
echo "2. Deploy the infrastructure: cd infrastructure && npm run deploy"
echo ""
echo "ECR Image URIs:"
echo "- User Service: $ECR_REGISTRY/hrs-user-service:latest"
echo "- Hotel Service: $ECR_REGISTRY/hrs-hotel-service:latest"
echo "- Booking Service: $ECR_REGISTRY/hrs-booking-service:latest"
echo "- API Gateway: $ECR_REGISTRY/hrs-api-gateway:latest"

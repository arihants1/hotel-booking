#!/bin/bash

# Hotel Booking System - Infrastructure Deployment Script
# This script deploys the AWS infrastructure using CDK

set -e

echo "🚀 Deploying Hotel Booking System Infrastructure to AWS..."

# Check if AWS CLI is configured
if ! aws sts get-caller-identity >/dev/null 2>&1; then
    echo "❌ AWS CLI not configured. Please run 'aws configure' first."
    exit 1
fi

# Check if CDK is installed
if ! command -v cdk &> /dev/null; then
    echo "📦 Installing AWS CDK..."
    npm install -g aws-cdk
fi

# Navigate to infrastructure directory
cd infrastructure

# Install dependencies
echo "📦 Installing dependencies..."
npm install

# Bootstrap CDK (if not already done)
echo "🏗️ Bootstrapping CDK..."
npx cdk bootstrap

# Build TypeScript
echo "🔨 Building TypeScript..."
npm run build

# Deploy the stack
echo "🚀 Deploying infrastructure..."
npx cdk deploy --require-approval never

echo "✅ Infrastructure deployment completed!"
echo ""
echo "Next steps:"
echo "1. Update the ECR image URIs in the CDK stack"
echo "2. Redeploy with: npx cdk deploy"
echo "3. Test your services using the output URLs"

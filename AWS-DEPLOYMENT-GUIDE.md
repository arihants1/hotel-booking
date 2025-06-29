# Hotel Booking System - AWS Deployment Guide

## Overview
This guide explains how to deploy the Hotel Booking System microservices to AWS using App Runner with supporting infrastructure including RDS PostgreSQL, ElastiCache Redis, and VPC networking.

## Architecture
- **AWS App Runner**: Container hosting for microservices
- **Amazon RDS**: PostgreSQL database
- **Amazon ElastiCache**: Redis caching
- **Amazon VPC**: Network isolation and security
- **AWS CDK**: Infrastructure as Code

## Prerequisites

1. **AWS CLI Configuration**
   ```bash
   aws configure
   # Enter your AWS Access Key ID, Secret Access Key, and preferred region
   ```

2. **Required Tools**
   - AWS CLI v2+
   - Docker
   - Node.js 18+
   - npm or yarn
   - Java 17+
   - Gradle

3. **AWS Permissions**
   Your AWS user/role needs permissions for:
   - App Runner
   - RDS
   - ElastiCache
   - VPC
   - ECR
   - IAM
   - CloudFormation
   - Systems Manager Parameter Store

## Deployment Steps

### Step 1: Build and Push Docker Images

1. **Make the deployment script executable:**
   ```bash
   chmod +x deploy-to-aws.sh
   ```

2. **Run the deployment script:**
   ```bash
   ./deploy-to-aws.sh
   ```
   
   This will:
   - Build all Java services
   - Create ECR repositories
   - Build and push Docker images to ECR

### Step 2: Update CDK Stack with ECR Image URIs

After running the deployment script, update the CDK stack to use your ECR images instead of public images:

1. **Edit `infrastructure/lib/hotel-booking-stack.ts`**
2. **Replace the placeholder image identifiers** with your ECR URIs:
   ```typescript
   // Replace this:
   imageIdentifier: 'public.ecr.aws/docker/library/openjdk:17-jdk-slim'
   
   // With your ECR URI:
   imageIdentifier: '123456789012.dkr.ecr.us-east-1.amazonaws.com/hrs-user-service:latest'
   ```

### Step 3: Deploy Infrastructure

1. **Navigate to infrastructure directory:**
   ```bash
   cd infrastructure
   ```

2. **Install dependencies:**
   ```bash
   npm install
   ```

3. **Bootstrap CDK (first time only):**
   ```bash
   npx cdk bootstrap
   ```

4. **Deploy the stack:**
   ```bash
   npm run deploy
   ```

### Step 4: Verify Deployment

1. **Check App Runner services:**
   ```bash
   aws apprunner list-services
   ```

2. **Get service URLs from CloudFormation outputs:**
   ```bash
   aws cloudformation describe-stacks --stack-name HotelBookingStack --query 'Stacks[0].Outputs'
   ```

3. **Test the API Gateway:**
   ```bash
   curl https://your-api-gateway-url.us-east-1.awsapprunner.com/actuator/health
   ```

## Configuration

### Environment Variables
The following environment variables are automatically configured:
- `DB_HOST`: RDS PostgreSQL endpoint
- `DB_PORT`: Database port (5432)
- `DB_NAME`: Database name (hrs_db)
- `REDIS_HOST`: ElastiCache Redis endpoint
- `REDIS_PORT`: Redis port (6379)
- `SPRING_PROFILES_ACTIVE`: Set to 'aws'

### Database Schema
The database schema will be automatically created using Flyway migrations or JPA DDL when the services start.

## Monitoring and Logging

### CloudWatch Logs
- Each service has its own log group in CloudWatch
- Logs are retained for 7 days (configurable)

### Health Checks
- Each service exposes `/actuator/health` endpoint
- App Runner performs health checks every 20 seconds

### Metrics
- Prometheus metrics are exposed at `/actuator/prometheus`
- Can be integrated with CloudWatch Container Insights

## Security

### Network Security
- Services run in private subnets with VPC connectivity
- Database and Redis are in isolated subnets
- Security groups restrict access between components

### Database Security
- RDS uses encrypted storage
- Database credentials are managed by AWS Secrets Manager
- Access restricted to App Runner services only

## Scaling

### App Runner Auto Scaling
- Minimum instances: 1
- Maximum instances: 10 (default)
- Auto-scaling based on CPU and memory usage

### Database Scaling
- RDS instance can be scaled vertically
- Read replicas can be added for read scaling

## Cost Optimization

### Resource Sizing
- App Runner: 0.25 vCPU, 0.5 GB RAM (can be adjusted)
- RDS: t3.micro instance (suitable for development/testing)
- ElastiCache: cache.t3.micro (suitable for development/testing)

### Production Recommendations
- Enable RDS Multi-AZ for high availability
- Use larger instance types for production workloads
- Enable RDS automated backups
- Consider reserved instances for cost savings

## Troubleshooting

### Common Issues

1. **Service fails to start:**
   - Check CloudWatch logs for error messages
   - Verify environment variables are set correctly
   - Ensure database connectivity

2. **Database connection issues:**
   - Verify VPC connector configuration
   - Check security group rules
   - Ensure database is in correct subnet group

3. **Build failures:**
   - Ensure Java 17 is installed
   - Check Gradle build configuration
   - Verify Docker is running

### Debugging Commands

```bash
# Check App Runner service status
aws apprunner describe-service --service-arn <service-arn>

# View CloudWatch logs
aws logs describe-log-streams --log-group-name /aws/apprunner/hrs-api-gateway

# Test database connectivity
aws rds describe-db-instances --db-instance-identifier <db-instance-id>
```

## Cleanup

To remove all resources and avoid charges:

```bash
cd infrastructure
npm run destroy
```

**Warning:** This will delete all data including databases. Ensure you have backups if needed.

## Support

For issues with this deployment:
1. Check CloudWatch logs for service errors
2. Verify AWS permissions and quotas
3. Review CDK stack events in CloudFormation console
4. Check App Runner service configuration

## Next Steps

1. **Set up CI/CD pipeline** for automated deployments
2. **Configure custom domain** for the API Gateway
3. **Set up monitoring and alerting**
4. **Implement backup strategies**
5. **Configure SSL/TLS certificates**

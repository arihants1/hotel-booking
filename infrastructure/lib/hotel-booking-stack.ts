import * as cdk from 'aws-cdk-lib';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import * as rds from 'aws-cdk-lib/aws-rds';
import * as elasticache from 'aws-cdk-lib/aws-elasticache';
import * as apprunner from 'aws-cdk-lib/aws-apprunner';
import * as iam from 'aws-cdk-lib/aws-iam';
import * as ssm from 'aws-cdk-lib/aws-ssm';
import * as logs from 'aws-cdk-lib/aws-logs';
import { Construct } from 'constructs';

export class HotelBookingStack extends cdk.Stack {
  constructor(scope: Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    // VPC for database and cache
    const vpc = new ec2.Vpc(this, 'HotelBookingVPC', {
      maxAzs: 2,
      natGateways: 1,
      subnetConfiguration: [
        {
          cidrMask: 24,
          name: 'public',
          subnetType: ec2.SubnetType.PUBLIC,
        },
        {
          cidrMask: 24,
          name: 'private',
          subnetType: ec2.SubnetType.PRIVATE_WITH_EGRESS,
        },
        {
          cidrMask: 28,
          name: 'database',
          subnetType: ec2.SubnetType.PRIVATE_ISOLATED,
        },
      ],
    });

    // Security groups
    const dbSecurityGroup = new ec2.SecurityGroup(this, 'DatabaseSecurityGroup', {
      vpc,
      description: 'Security group for RDS databases',
      allowAllOutbound: false,
    });

    const cacheSecurityGroup = new ec2.SecurityGroup(this, 'CacheSecurityGroup', {
      vpc,
      description: 'Security group for ElastiCache Redis',
      allowAllOutbound: false,
    });

    const appRunnerSecurityGroup = new ec2.SecurityGroup(this, 'AppRunnerSecurityGroup', {
      vpc,
      description: 'Security group for App Runner services',
    });

    // Allow App Runner to access databases and cache
    dbSecurityGroup.addIngressRule(
      appRunnerSecurityGroup,
      ec2.Port.tcp(5432),
      'Allow App Runner to access PostgreSQL'
    );

    cacheSecurityGroup.addIngressRule(
      appRunnerSecurityGroup,
      ec2.Port.tcp(6379),
      'Allow App Runner to access Redis'
    );

    // RDS Subnet Group
    const dbSubnetGroup = new rds.SubnetGroup(this, 'DatabaseSubnetGroup', {
      vpc,
      description: 'Subnet group for RDS databases',
      vpcSubnets: {
        subnetType: ec2.SubnetType.PRIVATE_ISOLATED,
      },
    });

    // PostgreSQL RDS Instance for all services
    const database = new rds.DatabaseInstance(this, 'HotelBookingDatabase', {
      engine: rds.DatabaseInstanceEngine.postgres({
        version: rds.PostgresEngineVersion.VER_15_4,
      }),
      instanceType: ec2.InstanceType.of(ec2.InstanceClass.T3, ec2.InstanceSize.MICRO),
      credentials: rds.Credentials.fromGeneratedSecret('hrs_admin', {
        excludeCharacters: '"@/\\',
      }),
      vpc,
      vpcSubnets: {
        subnetType: ec2.SubnetType.PRIVATE_ISOLATED,
      },
      securityGroups: [dbSecurityGroup],
      subnetGroup: dbSubnetGroup,
      allowMajorVersionUpgrade: false,
      autoMinorVersionUpgrade: true,
      backupRetention: cdk.Duration.days(7),
      deletionProtection: false, // Set to true for production
      storageEncrypted: true,
      multiAz: false, // Set to true for production
    });

    // ElastiCache Redis Subnet Group
    const cacheSubnetGroup = new elasticache.CfnSubnetGroup(this, 'CacheSubnetGroup', {
      description: 'Subnet group for ElastiCache Redis',
      subnetIds: vpc.selectSubnets({
        subnetType: ec2.SubnetType.PRIVATE_WITH_EGRESS,
      }).subnetIds,
    });

    // ElastiCache Redis Cluster
    const redisCluster = new elasticache.CfnCacheCluster(this, 'RedisCluster', {
      cacheNodeType: 'cache.t3.micro',
      engine: 'redis',
      numCacheNodes: 1,
      cacheSubnetGroupName: cacheSubnetGroup.ref,
      vpcSecurityGroupIds: [cacheSecurityGroup.securityGroupId],
    });

    // IAM Role for App Runner
    const appRunnerRole = new iam.Role(this, 'AppRunnerRole', {
      assumedBy: new iam.ServicePrincipal('tasks.apprunner.amazonaws.com'),
      managedPolicies: [
        iam.ManagedPolicy.fromAwsManagedPolicyName('AmazonRDSDataFullAccess'),
        iam.ManagedPolicy.fromAwsManagedPolicyName('CloudWatchLogsFullAccess'),
      ],
    });

    // Store database and cache endpoints in Parameter Store
    new ssm.StringParameter(this, 'DatabaseEndpoint', {
      parameterName: '/hrs/database/endpoint',
      stringValue: database.instanceEndpoint.hostname,
    });

    new ssm.StringParameter(this, 'DatabasePort', {
      parameterName: '/hrs/database/port',
      stringValue: database.instanceEndpoint.port.toString(),
    });

    new ssm.StringParameter(this, 'RedisEndpoint', {
      parameterName: '/hrs/redis/endpoint',
      stringValue: redisCluster.attrRedisEndpointAddress,
    });

    new ssm.StringParameter(this, 'RedisPort', {
      parameterName: '/hrs/redis/port',
      stringValue: redisCluster.attrRedisEndpointPort,
    });

    // App Runner VPC Connector
    const vpcConnector = new apprunner.CfnVpcConnector(this, 'AppRunnerVpcConnector', {
      subnets: vpc.selectSubnets({
        subnetType: ec2.SubnetType.PRIVATE_WITH_EGRESS,
      }).subnetIds,
      securityGroups: [appRunnerSecurityGroup.securityGroupId],
    });

    // Common environment variables for all services
    const commonEnvironmentVariables = {
      'SPRING_PROFILES_ACTIVE': 'aws',
      'DB_HOST': database.instanceEndpoint.hostname,
      'DB_PORT': database.instanceEndpoint.port.toString(),
      'DB_NAME': 'hrs_db',
      'REDIS_HOST': redisCluster.attrRedisEndpointAddress,
      'REDIS_PORT': redisCluster.attrRedisEndpointPort,
      'AWS_REGION': this.region,
    };

    // User Service
    const userService = new apprunner.CfnService(this, 'UserService', {
      serviceName: 'hrs-user-service',
      sourceConfiguration: {
        imageRepository: {
          imageIdentifier: 'public.ecr.aws/docker/library/openjdk:17-jdk-slim', // Replace with your ECR image
          imageConfiguration: {
            port: '8083',
            startCommand: 'java -jar /app/user-service.jar',
            runtimeEnvironmentVariables: {
              ...commonEnvironmentVariables,
              'SERVER_PORT': '8083',
              'SPRING_APPLICATION_NAME': 'user-service',
            },
          },
          imageRepositoryType: 'ECR_PUBLIC', // Change to ECR for private repositories
        },
        autoDeploymentsEnabled: false,
      },
      instanceConfiguration: {
        cpu: '0.25 vCPU',
        memory: '0.5 GB',
        instanceRoleArn: appRunnerRole.roleArn,
      },
      networkConfiguration: {
        egressConfiguration: {
          egressType: 'VPC',
          vpcConnectorArn: vpcConnector.attrVpcConnectorArn,
        },
      },
      healthCheckConfiguration: {
        protocol: 'HTTP',
        path: '/actuator/health',
        interval: 20,
        timeout: 10,
        healthyThreshold: 3,
        unhealthyThreshold: 5,
      },
    });

    // Hotel Service
    const hotelService = new apprunner.CfnService(this, 'HotelService', {
      serviceName: 'hrs-hotel-service',
      sourceConfiguration: {
        imageRepository: {
          imageIdentifier: 'public.ecr.aws/docker/library/openjdk:17-jdk-slim', // Replace with your ECR image
          imageConfiguration: {
            port: '8082',
            startCommand: 'java -jar /app/hotel-service.jar',
            runtimeEnvironmentVariables: {
              ...commonEnvironmentVariables,
              'SERVER_PORT': '8082',
              'SPRING_APPLICATION_NAME': 'hotel-service',
            },
          },
          imageRepositoryType: 'ECR_PUBLIC',
        },
        autoDeploymentsEnabled: false,
      },
      instanceConfiguration: {
        cpu: '0.25 vCPU',
        memory: '0.5 GB',
        instanceRoleArn: appRunnerRole.roleArn,
      },
      networkConfiguration: {
        egressConfiguration: {
          egressType: 'VPC',
          vpcConnectorArn: vpcConnector.attrVpcConnectorArn,
        },
      },
      healthCheckConfiguration: {
        protocol: 'HTTP',
        path: '/actuator/health',
        interval: 20,
        timeout: 10,
        healthyThreshold: 3,
        unhealthyThreshold: 5,
      },
    });

    // Booking Service
    const bookingService = new apprunner.CfnService(this, 'BookingService', {
      serviceName: 'hrs-booking-service',
      sourceConfiguration: {
        imageRepository: {
          imageIdentifier: 'public.ecr.aws/docker/library/openjdk:17-jdk-slim', // Replace with your ECR image
          imageConfiguration: {
            port: '8081',
            startCommand: 'java -jar /app/booking-service.jar',
            runtimeEnvironmentVariables: {
              ...commonEnvironmentVariables,
              'SERVER_PORT': '8081',
              'SPRING_APPLICATION_NAME': 'booking-service',
              'USER_SERVICE_URL': `https://${userService.attrServiceUrl}`,
              'HOTEL_SERVICE_URL': `https://${hotelService.attrServiceUrl}`,
            },
          },
          imageRepositoryType: 'ECR_PUBLIC',
        },
        autoDeploymentsEnabled: false,
      },
      instanceConfiguration: {
        cpu: '0.25 vCPU',
        memory: '0.5 GB',
        instanceRoleArn: appRunnerRole.roleArn,
      },
      networkConfiguration: {
        egressConfiguration: {
          egressType: 'VPC',
          vpcConnectorArn: vpcConnector.attrVpcConnectorArn,
        },
      },
      healthCheckConfiguration: {
        protocol: 'HTTP',
        path: '/actuator/health',
        interval: 20,
        timeout: 10,
        healthyThreshold: 3,
        unhealthyThreshold: 5,
      },
    });

    // API Gateway Service
    const apiGateway = new apprunner.CfnService(this, 'ApiGateway', {
      serviceName: 'hrs-api-gateway',
      sourceConfiguration: {
        imageRepository: {
          imageIdentifier: 'public.ecr.aws/docker/library/openjdk:17-jdk-slim', // Replace with your ECR image
          imageConfiguration: {
            port: '8080',
            startCommand: 'java -jar /app/api-gateway.jar',
            runtimeEnvironmentVariables: {
              ...commonEnvironmentVariables,
              'SERVER_PORT': '8080',
              'SPRING_APPLICATION_NAME': 'api-gateway',
              'USER_SERVICE_URL': `https://${userService.attrServiceUrl}`,
              'HOTEL_SERVICE_URL': `https://${hotelService.attrServiceUrl}`,
              'BOOKING_SERVICE_URL': `https://${bookingService.attrServiceUrl}`,
            },
          },
          imageRepositoryType: 'ECR_PUBLIC',
        },
        autoDeploymentsEnabled: false,
      },
      instanceConfiguration: {
        cpu: '0.5 vCPU',
        memory: '1 GB',
        instanceRoleArn: appRunnerRole.roleArn,
      },
      networkConfiguration: {
        egressConfiguration: {
          egressType: 'VPC',
          vpcConnectorArn: vpcConnector.attrVpcConnectorArn,
        },
      },
      healthCheckConfiguration: {
        protocol: 'HTTP',
        path: '/actuator/health',
        interval: 20,
        timeout: 10,
        healthyThreshold: 3,
        unhealthyThreshold: 5,
      },
    });

    // CloudWatch Log Groups
    new logs.LogGroup(this, 'UserServiceLogGroup', {
      logGroupName: '/aws/apprunner/hrs-user-service',
      retention: logs.RetentionDays.ONE_WEEK,
    });

    new logs.LogGroup(this, 'HotelServiceLogGroup', {
      logGroupName: '/aws/apprunner/hrs-hotel-service',
      retention: logs.RetentionDays.ONE_WEEK,
    });

    new logs.LogGroup(this, 'BookingServiceLogGroup', {
      logGroupName: '/aws/apprunner/hrs-booking-service',
      retention: logs.RetentionDays.ONE_WEEK,
    });

    new logs.LogGroup(this, 'ApiGatewayLogGroup', {
      logGroupName: '/aws/apprunner/hrs-api-gateway',
      retention: logs.RetentionDays.ONE_WEEK,
    });

    // Outputs
    new cdk.CfnOutput(this, 'ApiGatewayUrl', {
      value: `https://${apiGateway.attrServiceUrl}`,
      description: 'API Gateway URL',
    });

    new cdk.CfnOutput(this, 'UserServiceUrl', {
      value: `https://${userService.attrServiceUrl}`,
      description: 'User Service URL',
    });

    new cdk.CfnOutput(this, 'HotelServiceUrl', {
      value: `https://${hotelService.attrServiceUrl}`,
      description: 'Hotel Service URL',
    });

    new cdk.CfnOutput(this, 'BookingServiceUrl', {
      value: `https://${bookingService.attrServiceUrl}`,
      description: 'Booking Service URL',
    });

    new cdk.CfnOutput(this, 'DatabaseEndpoint', {
      value: database.instanceEndpoint.hostname,
      description: 'RDS PostgreSQL Endpoint',
    });

    new cdk.CfnOutput(this, 'RedisEndpoint', {
      value: redisCluster.attrRedisEndpointAddress,
      description: 'ElastiCache Redis Endpoint',
    });
  }
}

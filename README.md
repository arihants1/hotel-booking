# HRS Hotel Booking System

A comprehensive microservices-based hotel booking platform built with Spring Boot 3.x, featuring distributed architecture, API gateway with circuit breakers, Redis-based rate limiting, and Elasticsearch-powered search capabilities.

## 🏗️ Architecture Overview

The HRS (Hotel Reservation System) follows a microservices architecture pattern with the following components:

```
┌─────────────────────────────────────────────────────────────────┐
│                    API Gateway (Port: 8080)                     │
│           ┌─────────────────────────────────────┐               │
│           │  Rate Limiting (Redis-based)        │               │
│           │  Circuit Breakers (Resilience4j)    │               │
│           │  Request Logging & Health Monitoring │               │
│           │  CORS & Security Headers            │               │
│           └─────────────────────────────────────┘               │
└─────────────────────┬───────────────────────────────────────────┘
                     │
        ┌────────────┼────────────┬────────────────────────┐
        │            │            │                        │
        ▼            ▼            ▼                        ▼
┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────────┐
│User Service │ │Hotel Service│ │Booking Svc  │ │ Shared Library  │
│(Port: 8083) │ │(Port: 8081) │ │(Port: 8082) │ │                 │
│             │ │             │ │             │ │ - UserDTO       │
│- User CRUD  │ │- Hotel CRUD │ │- Booking Ops│ │ - HotelDTO      │
│- Email      │ │- ES Search  │ │- ES Search  │ │ - BookingDTO    │
│- Validation │ │- Amenities  │ │- Analytics  │ │ - ApiResponse   │
│- Profiles   │ │- Pricing    │ │- Status Mgmt│ │ - Exceptions    │
└──────┬──────┘ └──────┬──────┘ └──────┬──────┘ └─────────────────┘
       │               │               │
       ▼               ▼               ▼
┌─────────────┐ ┌─────────────┐ ┌─────────────┐
│ PostgreSQL  │ │ PostgreSQL  │ │ PostgreSQL  │
│   + Redis   │ │ + Redis     │ │ + Redis     │
│             │ │+ Elasticsearch│ │+ Elasticsearch│
│(Port: 5433) │ │(Port: 5431) │ │(Port: 5432) │
│hrs_user_db  │ │hrs_hotel_db │ │hrs_booking_db│
└─────────────┘ └─────────────┘ └─────────────┘
```

## 🎯 Key Features

### 🚪 API Gateway (Spring Cloud Gateway)
- **High-Performance Routing**: Reactive WebFlux-based routing to all microservices
- **Redis Rate Limiting**: IP-based rate limiting with burst capacity
- **Circuit Breaker Pattern**: Service-specific circuit breakers with fallback URIs
- **Request Enhancement**: Automatic headers (X-Gateway-Timestamp, X-HRS-Service)
- **Health Monitoring**: Custom health indicators with Redis connectivity checks
- **CORS Support**: Cross-origin resource sharing for web applications

### 🏨 Hotel Service  
- **Hotel Management**: Complete CRUD operations for hotels with location indexing
- **Elasticsearch Search**: Full-text search across hotel names, cities, and countries
- **Dual Storage Strategy**: PostgreSQL for transactional data + Elasticsearch for search
- **Auto-Indexing**: Automatic synchronization from PostgreSQL to Elasticsearch on startup
- **Search Features**: City-based search, name containment search, active status filtering
- **Batch Processing**: Efficient pagination-based indexing for large datasets
- **Amenities Support**: JSON-based amenities storage with flexible schema
- **Price Management**: Base pricing with star rating correlation
- **Cache Layer**: Redis caching for frequently accessed hotel data

### 📋 Booking Service
- **Booking Lifecycle**: Complete booking management from creation to completion
- **Elasticsearch Search**: Advanced booking search with multiple criteria
- **Reference System**: Unique booking reference generation and lookup
- **Overlap Detection**: Prevents double bookings with date range validation
- **Status Management**: Comprehensive booking status tracking (PENDING, CONFIRMED, CANCELLED, COMPLETED)
- **Analytics Support**: Booking statistics and reporting capabilities
- **Date Validation**: Future date validation for check-in/check-out

### 👤 User Service
- **User Profile Management**: Complete user CRUD with email uniqueness
- **Data Validation**: Comprehensive field validation (email, phone patterns)
- **Active Status Tracking**: Soft delete functionality with active/inactive states
- **Email-based Lookup**: User retrieval by email for authentication
- **Profile Completion**: Tracks profile completeness for user experience
- **Audit Trail**: Creation and update timestamp tracking

### 📚 Shared Library
- **Standardized DTOs**: UserDTO, HotelDTO, BookingDTO with validation annotations
- **Response Wrapper**: ApiResponse with success/error status and timestamps
- **Exception Handling**: GlobalExceptionHandler with business validation exceptions
- **Status Enums**: BookingStatus enum for type safety
- **Swagger Integration**: OpenAPI documentation annotations

## 🛠️ Technology Stack

### Backend Framework
- **Java 17** with modern features
- **Spring Boot 3.1.5** with WebFlux reactive programming
- **Spring Cloud Gateway** for API routing
- **Spring Data JPA** with Hibernate
- **Spring Data Elasticsearch** for search functionality
- **Resilience4j** for circuit breaker pattern

### Databases & Search
- **PostgreSQL** (separate database per service)
  - User DB: `hrs_user_db` on port 5433
  - Hotel DB: `hrs_hotel_db` on port 5431  
  - Booking DB: `hrs_booking_db` on port 5432
- **Redis** for caching and rate limiting (port 6379)
- **Elasticsearch** for hotel and booking search

### Build & Deployment
- **Gradle 8+** with multi-module build
- **Docker & Docker Compose** for containerization
- **AWS CDK** for Infrastructure as Code
- **Prometheus** for metrics collection

### Validation & Documentation
- **Jakarta Validation** with custom constraints
- **Swagger/OpenAPI 3** for API documentation
- **Lombok** for reducing boilerplate code
- **MapStruct** for entity-DTO mapping

## 🚀 Getting Started

### Prerequisites
- **Java 17+**
- **Docker & Docker Compose**
- **Gradle 8+**
- **Git**

### Quick Start

1. **Clone Repository**
   ```bash
   git clone <repository-url>
   cd hotel-booking
   ```

2. **Start Infrastructure Dependencies**
   ```bash
   # Start databases for each service
   cd user-service && docker-compose up -d
   cd ../hotel-service && docker-compose up -d  
   cd ../booking-service && docker-compose up -d
   cd ../api-gateway && docker-compose up -d
   cd ..
   ```

3. **Build All Services**
   ```bash
   ./gradlew clean build
   ```

4. **Start Services** (in separate terminals)
   ```bash
   # Terminal 1 - User Service
   cd user-service && ./gradlew bootRun
   
   # Terminal 2 - Hotel Service  
   cd hotel-service && ./gradlew bootRun
   
   # Terminal 3 - Booking Service
   cd booking-service && ./gradlew bootRun
   
   # Terminal 4 - API Gateway
   cd api-gateway && ./gradlew bootRun
   ```

5. **Verify Setup**
   ```bash
   # Check gateway health
   curl http://localhost:8080/actuator/health
   
   # Test hotel service through gateway
   curl http://localhost:8080/api/v1/hotels
   ```

## 📡 API Endpoints

### Through API Gateway (Port: 8080)

#### User Management
```
POST   /api/v1/users                    # Create user
GET    /api/v1/users/{id}               # Get user by ID
GET    /api/v1/users/email/{email}      # Get user by email
PUT    /api/v1/users/{id}               # Update user
DELETE /api/v1/users/{id}               # Delete user
GET    /api/v1/users                    # List users (paginated)
```

#### Hotel Management
```
POST   /api/v1/hotels                   # Create hotel
GET    /api/v1/hotels/{id}              # Get hotel details
PUT    /api/v1/hotels/{id}              # Update hotel
DELETE /api/v1/hotels/{id}              # Delete hotel
GET    /api/v1/hotels                   # Search hotels
GET    /api/v1/hotels/search            # Advanced hotel search
```

#### Booking Operations
```
POST   /api/v1/bookings                 # Create booking
GET    /api/v1/bookings/{id}            # Get booking details
PUT    /api/v1/bookings/{id}            # Update booking
DELETE /api/v1/bookings/{id}            # Cancel booking
GET    /api/v1/bookings                 # List user bookings
GET    /api/v1/bookings/search          # Search bookings (Elasticsearch)
GET    /api/v1/bookings/reference/{ref} # Find by reference number
```

## 💾 Database Configuration

### Database Connections
| Service | Database | Port | Username | Password | Database |
|---------|----------|------|----------|----------|----------|
| User Service | PostgreSQL | 5433 | hrs_user | hrs_password_2025 | hrs_user_db |
| Hotel Service | PostgreSQL | 5431 | hrs_hotel | hrs_password_2025 | hrs_hotel_db |
| Booking Service | PostgreSQL | 5432 | hrs_booking | hrs_password_2025 | hrs_booking_db |
| All Services | Redis | 6379 | - | hrs_redis_2025 | - |

### pgAdmin Access
Each service includes pgAdmin for database management:
- **User Service**: http://localhost:8083/pgadmin
- **Hotel Service**: http://localhost:8081/pgadmin  
- **Booking Service**: http://localhost:8082/pgadmin
- **Credentials**: admin@hrs.com / admin_password_2025

## 🔧 Configuration

### Environment Variables
Create `.env` files in each service directory for custom configuration:

```bash
# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=hrs_booking_db
DB_USERNAME=hrs_booking
DB_PASSWORD=hrs_password_2025

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=hrs_redis_2025

# Elasticsearch Configuration
ELASTICSEARCH_HOST=localhost
ELASTICSEARCH_PORT=9200

# Gateway Configuration
GATEWAY_URL=http://localhost:8080
```

### Circuit Breaker Configuration
```yaml
resilience4j:
  circuitbreaker:
    instances:
      hotel-service-cb:
        sliding-window-size: 15
        failure-rate-threshold: 40
        wait-duration-in-open-state: 20s
      booking-service-cb:
        sliding-window-size: 30
        failure-rate-threshold: 60
        wait-duration-in-open-state: 45s
      user-service-cb:
        sliding-window-size: 20
        failure-rate-threshold: 50
        wait-duration-in-open-state: 30s
```

## 🧪 Testing

### Running Tests
```bash
# Run all tests
./gradlew test

# Service-specific tests
./gradlew :user-service:test
./gradlew :hotel-service:test
./gradlew :booking-service:test
./gradlew :api-gateway:test
./gradlew :shared:test

# Generate coverage reports
./gradlew jacocoTestReport
```

### Test Reports
Test reports are generated in each service's `build/reports/tests/test/index.html`

### Postman Collections
Import the provided Postman collections for API testing:
- `booking-service/postman/HRS-Booking-Service.postman_collection.json`
- `booking-service/postman/HRS-Booking-Service-Elasticsearch.postman_collection.json`
- `hotel-service/postman/HRS-Hotel-Service.postman_collection.json`

## ☁️ AWS Deployment

### Infrastructure as Code
The project includes AWS CDK scripts for cloud deployment:

```bash
# Deploy infrastructure
cd infrastructure
npm install
npx cdk deploy

# Deploy application services
./deploy-to-aws.sh
```

### AWS Services Used
- **ECS Fargate**: Containerized service deployment
- **Application Load Balancer**: Traffic distribution
- **RDS PostgreSQL**: Managed database instances
- **ElastiCache Redis**: Managed caching layer
- **Elasticsearch Service**: Managed search engine
- **CloudWatch**: Monitoring and logging

See `AWS-DEPLOYMENT-GUIDE.md` for detailed deployment instructions.

## 📈 Performance & Monitoring

### Performance Features
- **Elasticsearch Integration**: Fast search for hotels and bookings
- **Redis Caching**: Performance optimization for frequently accessed data
- **Database Indexing**: Strategic indexes for common query patterns
- **Reactive Programming**: Non-blocking I/O with Spring WebFlux
- **Connection Pooling**: Optimized database connections
- **Pagination Support**: Efficient large dataset handling

### Health Monitoring
```bash
# Gateway health
GET http://localhost:8080/actuator/health

# Service health (direct access)
GET http://localhost:8081/actuator/health  # Hotel Service
GET http://localhost:8082/actuator/health  # Booking Service  
GET http://localhost:8083/actuator/health  # User Service

# Prometheus metrics
GET http://localhost:8080/actuator/prometheus
```

## 🔒 Security Features

### Data Validation
- **Jakarta Validation**: Comprehensive input validation
- **Email Format Validation**: RFC-compliant email validation
- **Phone Number Patterns**: International phone number support
- **Date Validation**: Future date validation for bookings
- **Business Rules**: Custom validation for booking logic

### API Security
- **CORS Configuration**: Proper cross-origin resource sharing
- **Request Headers**: Security headers added by gateway
- **Input Sanitization**: Protection against injection attacks
- **Rate Limiting**: API abuse prevention

## 🗂️ Project Structure

```
hotel-booking/
├── api-gateway/                 # Spring Cloud Gateway
│   ├── src/main/java/          # Gateway filters, config, fallbacks
│   ├── docker-compose.yml      # Redis for rate limiting
│   └── database/init.sql       # Gateway-specific setup
├── user-service/               # User management service
│   ├── src/main/java/          # User CRUD, validation
│   ├── docker-compose.yml      # PostgreSQL + pgAdmin
│   └── database/init.sql       # User schema
├── hotel-service/              # Hotel management service
│   ├── src/main/java/          # Hotel CRUD, Elasticsearch
│   ├── docker-compose.yml      # PostgreSQL + Elasticsearch
│   ├── database/init.sql       # Hotel schema
│   └── postman/                # API test collections
├── booking-service/            # Booking management service
│   ├── src/main/java/          # Booking CRUD, search
│   ├── docker-compose.yml      # PostgreSQL + Elasticsearch
│   ├── database/init.sql       # Booking schema
│   └── postman/                # API test collections
├── shared/                     # Common libraries
│   └── src/main/java/          # DTOs, exceptions, responses
├── infrastructure/             # AWS CDK deployment
│   ├── lib/                    # CDK stack definitions
│   └── bin/                    # CDK entry point
├── build.gradle               # Multi-module build configuration
├── settings.gradle             # Project modules
├── deploy-to-aws.sh           # AWS deployment script
└── README.md                  # This file
```

## 🚀 Development Guidelines

### Code Standards
- **Java 17** with modern features
- **Spring Boot 3.x** best practices
- **Clean Architecture** principles
- **RESTful API** design
- **Test-Driven Development**

### Git Workflow
1. Create feature branch from `main`
2. Implement feature with tests
3. Ensure all tests pass
4. Create pull request
5. Code review and merge

## 🛠️ Troubleshooting

### Common Issues

#### Port Conflicts
```bash
# Check port usage (Windows)
netstat -an | findstr :8080

# Kill process using port
taskkill /PID <process-id> /F
```

#### Database Connection Issues
```bash
# Check Docker containers
docker ps -a

# Restart database containers
docker-compose down && docker-compose up -d

# Check logs
docker-compose logs postgres
```

#### Service Communication Problems
```bash
# Check service health
curl http://localhost:8080/actuator/health

# Test direct service access
curl http://localhost:8083/api/v1/users
curl http://localhost:8081/api/v1/hotels
curl http://localhost:8082/api/v1/bookings
```

### Debug Mode
Enable debug logging in `application.yml`:
```yaml
logging:
  level:
    com.hrs.hotelbooking: DEBUG
    org.springframework.cloud.gateway: DEBUG
    org.springframework.web: DEBUG
```

## 📋 Quick Reference

### Service Ports
| Service | Port | Health Check |
|---------|------|--------------|
| API Gateway | 8080 | http://localhost:8080/actuator/health |
| Hotel Service | 8081 | http://localhost:8081/actuator/health |
| Booking Service | 8082 | http://localhost:8082/actuator/health |
| User Service | 8083 | http://localhost:8083/actuator/health |

### Database Ports
| Database | Port | Service |
|----------|------|---------|
| User PostgreSQL | 5433 | User Service |
| Hotel PostgreSQL | 5431 | Hotel Service |
| Booking PostgreSQL | 5432 | Booking Service |
| Redis | 6379 | All Services |
| Elasticsearch | 9200 | Hotel & Booking Services |

### Test Commands
```bash
# Through API Gateway
curl http://localhost:8080/api/v1/users
curl http://localhost:8080/api/v1/hotels  
curl http://localhost:8080/api/v1/bookings

# Direct to Services
curl http://localhost:8083/api/v1/users
curl http://localhost:8081/api/v1/hotels
curl http://localhost:8082/api/v1/bookings
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 📞 Support

For support and questions:
- **Issues**: Create GitHub issues for bugs and feature requests
- **Documentation**: Check this README and inline code documentation
- **Health Checks**: Use the provided health endpoints for monitoring

---

**Last Updated**: June 29, 2025  
**Version**: 1.0.0  
**Architecture**: Microservices with Spring Boot 3.x, Spring Cloud Gateway, PostgreSQL, Redis, Elasticsearch

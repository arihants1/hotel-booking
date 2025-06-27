# HRS Hotel Booking System

This repository contains a microservices-based hotel booking system, including services for booking, user management, hotel management, and an API gateway. Each service is containerized and can be run locally using Docker Compose.

## Services

- **api-gateway**: Central entry point for all APIs, handles routing and fallback.
- **booking-service**: Manages hotel bookings, availability, and booking lifecycle.
- **user-service**: Manages user profiles and authentication.
- **hotel-service**: Manages hotel data, rooms, and amenities.
- **shared**: Shared libraries and DTOs used across services.

## Prerequisites
- Java 17+
- Gradle
- Docker Compose

## Running Locally

1. **Build all services:**
   ```sh
   ./gradlew build
   ```

2. **Start required databases and dependencies:**
   - Each service has its own `docker-compose.yml` for PostgreSQL, Redis, and pgAdmin.
   - Example for user-service:
     ```sh
     cd user-service
     docker-compose up -d
     ```
   - Repeat for `booking-service` and `hotel-service` as needed.

3. **Run a service:**
   ```sh
   cd user-service
   ./gradlew bootRun
   ```
   Or run the JAR from `build/libs`.

4. **Database Management:**
   - pgAdmin is available at `http://localhost:8080` (booking), `8081` (user), etc.
   - Default credentials: `admin@hrs.com` / `admin_password_2025`

## Example Postman Requests

### User Service
- **Create User:**
  POST `http://localhost:8083/api/v1/users`
- **Get User by ID:**
  GET `http://localhost:8083/api/v1/users/1`

### Booking Service
- **Create Booking:**
  POST `http://localhost:8082/api/v1/bookings`
- **Get Booking by ID:**
  GET `http://localhost:8082/api/v1/bookings/1`

## Environment Variables
- Database and Redis credentials can be overridden in each service's `application.yml`.

## Troubleshooting
- Ensure Docker containers are running and healthy.
- Check that the correct database and port are set in `application.yml`.
- If you see connection errors, verify that the database exists and is initialized (see `database/init.sql`).

## Authors
- arihants1



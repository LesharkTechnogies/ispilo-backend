# Ispilo Backend

Ispilo is a social commerce platform that combines social media features with a marketplace. This repository contains the Spring Boot backend service.

## Features

- **User Authentication**: JWT-based login, registration, and profile management.
- **Social Media**: Create posts with multiple media files, like, comment, and view counts.
- **Marketplace**: Seller registration, product listing, and category-based search.
- **Real-time Messaging**: WebSocket-based chat with support for direct and group conversations.
- **Search**: Search for users, posts, and products.

## Tech Stack

- **Java 17**
- **Spring Boot 3.x**
- **Spring Security** (JWT Authentication)
- **Spring Data JPA** (PostgreSQL)
- **Spring WebSocket** (Real-time messaging)
- **Lombok** (Boilerplate reduction)
- **Jakarta Persistence**

## Getting Started

### Prerequisites

- JDK 17 or higher
- Maven 3.6+
- PostgreSQL

### Configuration

1. Create a PostgreSQL database named `ispilo`.
2. Update `src/main/resources/application.properties` with your database credentials and JWT secret:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/ispilo
spring.datasource.username=your_username
spring.datasource.password=your_password

app.jwt.secret=your_very_long_and_secure_jwt_secret_key
app.jwt.expiration=86400000
```

### Running the Application

```bash
mvn clean install
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`.

## API Documentation

- **Auth**: `/api/v1/auth/**`
- **Users**: `/api/v1/users/**`
- **Posts**: `/api/v1/posts/**`
- **Sellers**: `/api/v1/sellers/**`
- **Products**: `/api/v1/products/**`
- **Messages**: `/api/v1/messages/**`

## Database Schema

See [DATABASE.md](./DATABASE.md) for detailed schema information.

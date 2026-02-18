# Ispilo Backend - Quick Start Guide

## ⚡ 5-Minute Setup

### Prerequisites
- Java 17+ installed
- Maven 3.8+ installed
- MySQL 8.0+ running
- 8GB RAM minimum

### Step 1: Start MySQL (if not running)
```bash
# Windows
net start MySQL80

# macOS
brew services start mysql-server

# Linux
sudo service mysql start
```

### Step 2: Create Database
```bash
mysql -u root -p

# In MySQL prompt:
CREATE DATABASE ispilo_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EXIT;
```

### Step 3: Clone & Configure
```bash
cd ispilo-backend
```

Create `.env` file in project root:
```properties
DB_PASSWORD=your_mysql_password
JWT_SECRET=MjU2Yml0c2VjcmV0a2V5Zm9yaXNwaWxvYXBwbGljYXRpb25jaGFuZ2VpbnByb2R1Y3Rpb24=
```

### Step 4: Build & Run
```bash
# Clean build
mvn clean install

# Run
mvn spring-boot:run
```

### Step 5: Verify
```bash
# In another terminal
curl http://localhost:8080/actuator/health

# Expected response
{"status":"UP"}
```

### Step 6: Access API Documentation
Open in browser: **http://localhost:8080/swagger-ui.html**

## 🚀 Next Steps

### Test the API
```bash
# 1. Register a user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123@",
    "firstName": "John",
    "lastName": "Doe"
  }'

# 2. Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123@"
  }'

# 3. Get your token from response and use it
export TOKEN="your_access_token_here"

# 4. Get all products
curl http://localhost:8080/api/products \
  -H "Authorization: Bearer $TOKEN"

# 5. Get education videos
curl http://localhost:8080/api/education/videos \
  -H "Authorization: Bearer $TOKEN"
```

### Configure Flutter App
In `ispilo-main/lib/core/services/api_service.dart`:
```dart
static const String baseUrl = 'http://localhost:8080/api';
```

### Update Database Credentials
In `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    password: your_mysql_password
```

## 📚 Documentation

| Document | Purpose |
|----------|---------|
| [DATABASE_SETUP.md](DATABASE_SETUP.md) | Database configuration & schema |
| [IMPLEMENTATION_GUIDE.md](IMPLEMENTATION_GUIDE.md) | Detailed implementation guide |
| [FLUTTER_MIGRATION_GUIDE.md](FLUTTER_MIGRATION_GUIDE.md) | Replace mock data with API calls |
| [README_IMPLEMENTATION.md](README_IMPLEMENTATION.md) | Project overview |
| [COMPLETION_SUMMARY.md](COMPLETION_SUMMARY.md) | What's been implemented |

## 🔑 Key API Endpoints

### Authentication
- `POST /api/auth/register` - Create account
- `POST /api/auth/login` - Login
- `POST /api/auth/refresh` - Refresh token

### Products
- `GET /api/products` - List products
- `GET /api/products/search?keyword=...` - Search
- `GET /api/products/category/{category}` - Filter by category
- `POST /api/products` - Create product (seller only)

### Education
- `GET /api/education/videos` - Videos list
- `GET /api/education/courses` - Courses list
- `POST /api/education/courses/{id}/enroll` - Enroll in course
- `GET /api/education/my-courses` - My courses

### Messaging
- `GET /api/conversations` - List conversations
- `WS /ws/chat` - WebSocket for real-time messaging
- `POST /api/conversations/{id}/messages` - Send message

### Social
- `GET /api/posts` - Feed posts
- `POST /api/posts` - Create post
- `POST /api/posts/{id}/like` - Like post

## 🔐 Security

### Tokens
- **Access Token**: Valid for 24 hours
- **Refresh Token**: Valid for 7 days
- **Encryption**: JWT with HS256

### Messaging
- All private messages: **AES-256-GCM encrypted**
- Unique key per conversation
- 12-byte random IV per message
- 128-bit authentication tag

## 🛠️ Troubleshooting

### MySQL Connection Failed
```bash
# Check MySQL is running
mysql -u root -p

# Create database if missing
CREATE DATABASE ispilo_db;

# Update application.yml with correct password
```

### Port 8080 Already in Use
```bash
# Find process using port 8080
lsof -i :8080

# Kill process
kill -9 <PID>
```

### JWT Token Expired
```bash
# Use refresh endpoint to get new token
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "your_refresh_token"}'
```

### WebSocket Connection Failed
- Ensure application is running
- Check WebSocket URL: `ws://localhost:8080/ws/chat`
- Include JWT token in URL query parameter

## 📊 Useful Commands

```bash
# Start MySQL
mysql -u root -p

# Stop MySQL
mysql -u root -p -e "SHUTDOWN"

# Check Maven version
mvn --version

# Check Java version
java -version

# Build JAR
mvn clean package -DskipTests

# Run specific test
mvn test -Dtest=UserControllerTest

# View application logs
tail -f target/spring.log

# Check running Spring Boot processes
jps -l | grep spring
```

## 🚀 Production Deployment

### Using Docker
```bash
# Build Docker image
docker build -t ispilo-backend .

# Run with Docker Compose
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop services
docker-compose down
```

### Manual Deployment
```bash
# Build JAR
mvn clean package -DskipTests

# Copy to server
scp target/ispilo-backend-1.0-SNAPSHOT.jar user@server:/app/

# Run on server
java -Dspring.profiles.active=prod \
  -Dspring.datasource.password=$DB_PASSWORD \
  -jar /app/ispilo-backend-1.0-SNAPSHOT.jar
```

## 📈 Performance Monitoring

```bash
# Health check
curl http://localhost:8080/actuator/health

# Application metrics
curl http://localhost:8080/actuator/metrics

# Specific metric (e.g., requests)
curl http://localhost:8080/actuator/metrics/http.server.requests
```

## 🧪 Testing

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ProductControllerTest

# Run tests with coverage
mvn test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

## 💡 Tips & Tricks

### Generate JWT Token for Testing
```bash
# Use login endpoint or create in Spring Boot:
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@ispilo.com","password":"admin123"}'
```

### Test WebSocket
```bash
# Using websocat (install: brew install websocat)
websocat "ws://localhost:8080/ws/chat?token=YOUR_TOKEN"

# Or use Postman's WebSocket feature
```

### Quick Database Reset
```bash
# Connect to MySQL
mysql -u root -p ispilo_db

# Clear all data
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE messages;
TRUNCATE TABLE conversations;
TRUNCATE TABLE posts;
TRUNCATE TABLE comments;
TRUNCATE TABLE products;
TRUNCATE TABLE sellers;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;
```

## 🔗 Important Links

- **API Docs**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:8080/actuator/metrics
- **GitHub**: https://github.com/yourusername/ispilo-backend
- **Issues**: https://github.com/yourusername/ispilo-backend/issues

## 📞 Need Help?

1. Check the [IMPLEMENTATION_GUIDE.md](IMPLEMENTATION_GUIDE.md)
2. Review [DATABASE_SETUP.md](DATABASE_SETUP.md)
3. Check application logs: `tail -f target/spring.log`
4. Test endpoint in Postman/Insomnia
5. Review Swagger UI: http://localhost:8080/swagger-ui.html

## ✅ Checklist for First Run

- [ ] MySQL is running
- [ ] Database `ispilo_db` created
- [ ] `.env` file configured
- [ ] Maven build successful
- [ ] Application starts without errors
- [ ] Health check returns "UP"
- [ ] Swagger UI accessible
- [ ] Can login and get JWT token
- [ ] Can fetch products
- [ ] Can fetch education videos

## 🎉 You're Ready!

The backend is now running and ready for:
- ✅ API testing with Postman
- ✅ Flutter app integration
- ✅ Database operations
- ✅ Real-time messaging tests
- ✅ User authentication flow

---

**Questions?** Check the comprehensive documentation files or review the Swagger UI at http://localhost:8080/swagger-ui.html

**Happy Coding! 🚀**

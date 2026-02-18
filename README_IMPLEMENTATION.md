# Ispilo Backend API

A comprehensive Spring Boot 3.2.3 REST API backend for the Ispilo Flutter mobile application. Provides authentication, encrypted real-time messaging, marketplace functionality, education hub, and social feed features.

## 🚀 Features

### Core Features
- ✅ **JWT Authentication** - Secure token-based authentication with refresh tokens
- ✅ **End-to-End Encrypted Messaging** - AES-256-GCM encryption for all private messages
- ✅ **Real-Time WebSocket** - STOMP protocol for live messaging with typing indicators
- ✅ **Marketplace** - Product listings, search, filtering, seller management
- ✅ **Education Hub** - Educational videos, courses, enrollment tracking
- ✅ **Social Feed** - Posts, comments, likes, sharing with verification badges
- ✅ **User Management** - Profiles, followers, user verification
- ✅ **Search** - Global search across products, users, and content

### Advanced Features
- 🔐 Message encryption with AES-256-GCM
- 📱 Real-time typing indicators and read receipts
- 🎓 Course progress tracking with completion analytics
- 📊 User audit logging and activity tracking
- ☁️ AWS S3 integration for media uploads
- 💾 Redis caching for performance
- 📖 Comprehensive API documentation with Swagger/OpenAPI

## 📋 Prerequisites

- **Java 17+**
- **Maven 3.8+**
- **MySQL 8.0+**
- **Redis 6.0+** (optional, for caching)
- **AWS S3 Credentials** (optional, for file uploads)

## 🛠️ Installation

### 1. Clone Repository
```bash
git clone https://github.com/yourusername/ispilo-backend.git
cd ispilo-backend
```

### 2. Configure Environment Variables
Create `.env` file in project root:
```properties
# Database
DB_PASSWORD=your_secure_password

# JWT
JWT_SECRET=your_base64_encoded_secret_key
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000

# AWS S3 (optional)
AWS_ACCESS_KEY=your_aws_access_key
AWS_SECRET_KEY=your_aws_secret_key
AWS_REGION=us-east-1
S3_BUCKET=ispilo-media

# Redis (optional)
REDIS_HOST=localhost
REDIS_PORT=6379

# Application
BASE_URL=http://localhost:8080
CDN_URL=http://localhost:8080/media
ENCRYPTION_MASTER_KEY=your_encryption_master_key
```

### 3. Setup Database
```bash
# Create database and tables
mysql -u root -p < DATABASE_SETUP.sql

# Or let Spring Boot create tables automatically
# Set spring.jpa.hibernate.ddl-auto=update in application.yml
```

### 4. Build and Run
```bash
# Build with Maven
mvn clean install

# Run Spring Boot application
mvn spring-boot:run

# Or run the JAR directly
java -jar target/ispilo-backend-1.0-SNAPSHOT.jar
```

### 5. Verify Installation
```bash
# Health check
curl http://localhost:8080/actuator/health

# Swagger UI
open http://localhost:8080/swagger-ui.html
```

## 📚 API Documentation

### Authentication Endpoints

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}

Response:
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "user": {
    "id": "user-123",
    "email": "user@example.com",
    "name": "John Doe"
  }
}
```

#### Register
```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "newuser@example.com",
  "password": "securePassword123",
  "firstName": "John",
  "lastName": "Doe"
}
```

#### Refresh Token
```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

### Product Endpoints

#### Get All Products
```http
GET /api/products?page=0&size=20

Response:
{
  "content": [...],
  "pageNumber": 0,
  "pageSize": 20,
  "totalElements": 256,
  "totalPages": 13,
  "isLast": false
}
```

#### Search Products
```http
GET /api/products/search?keyword=switch&page=0&size=20
```

#### Get Product by ID
```http
GET /api/products/{productId}
```

#### Create Product (Seller Only)
```http
POST /api/products
Authorization: Bearer {token}
Content-Type: application/json

{
  "title": "TP-Link Switch",
  "description": "5-port network switch",
  "price": 800,
  "category": "Hardware",
  "condition": "New",
  "mainImage": "https://...",
  "images": ["https://..."],
  "location": "Nairobi",
  "stockQuantity": 10
}
```

### Education Endpoints

#### Get Videos
```http
GET /api/education/videos?page=0&size=20
```

#### Get Trending Videos
```http
GET /api/education/videos/trending?page=0&size=10
```

#### Get Courses
```http
GET /api/education/courses?page=0&size=20
```

#### Enroll in Course
```http
POST /api/education/courses/{courseId}/enroll
Authorization: Bearer {token}
```

#### Get My Courses
```http
GET /api/education/my-courses
Authorization: Bearer {token}
```

#### Update Course Progress
```http
PUT /api/education/enrollments/{enrollmentId}/progress?progress=65.5&completedLessons=10
Authorization: Bearer {token}
```

### Messaging Endpoints

#### Get Conversations
```http
GET /api/conversations?page=0&size=10
Authorization: Bearer {token}
```

#### Get Conversation Messages
```http
GET /api/conversations/{conversationId}/messages?page=0&size=20
Authorization: Bearer {token}
```

#### Create Conversation
```http
POST /api/conversations
Authorization: Bearer {token}
Content-Type: application/json

{
  "participantIds": ["user-123", "user-456"],
  "name": "Network Team"
}
```

### WebSocket Endpoints

#### Connect
```
ws://localhost:8080/ws/chat?token={authToken}
```

#### Send Message
```javascript
// Subscribe to messages
stompClient.subscribe('/topic/conversation/{conversationId}', 
  (message) => {
    const msg = JSON.parse(message.body);
    // Handle encrypted message
  }
);

// Send message
stompClient.send('/app/chat.send', {}, JSON.stringify({
  conversationId: 'conv-123',
  content: 'Hello',
  encryptedContent: 'encrypted...',
  encryptionKey: 'key...',
  encryptionIv: 'iv...',
  type: 'TEXT',
  clientMsgId: 'msg-client-id'
}));
###

## 🔐 Security

### Message Encryption

All private messages are encrypted using **AES-256-GCM**:

1. **Key Generation**: Each conversation gets a unique 256-bit key
2. **IV Generation**: 12-byte random IV generated per message
3. **Authentication**: 128-bit authentication tag included
4. **Storage**: Only encrypted content stored in database
5. **Decryption**: Client-side only using conversation key

### JWT Token Security

- **Algorithm**: HS256 (HMAC with SHA-256)
- **Access Token Expiry**: 24 hours (configurable)
- **Refresh Token Expiry**: 7 days (configurable)
- **Secret Key**: Base64-encoded, minimum 256 bits
- **Token Blacklisting**: Implement on logout (optional)

### Best Practices

- ✅ All passwords hashed with bcrypt
- ✅ HTTPS/TLS enforced in production
- ✅ CORS restricted to trusted domains
- ✅ Rate limiting on authentication endpoints
- ✅ CSRF protection enabled
- ✅ Input validation on all endpoints
- ✅ SQL injection prevention via parameterized queries

## 📱 Flutter Integration

### Install Dependencies
```yaml
dependencies:
  http: ^1.1.0
  shared_preferences: ^2.2.0
  web_socket_channel: ^2.4.0
  crypto: ^3.0.0
  pointycastle: ^3.7.0
```

### Initialize API Service
```dart
// Already configured in lib/core/services/api_service.dart
final products = await ApiService.get('/products?page=0&size=20');
```

### Setup WebSocket
```dart
final wsService = WebSocketService();
await wsService.initialize(
  conversationId: 'conv-123',
  userId: userId,
  encryptionKey: key,
  authToken: token,
);

// Send encrypted message
await wsService.sendMessage('Hello', 'msg-client-id');

// Listen to messages
wsService.messageNotifier.addListener(() {
  print('New messages: ${wsService.messageNotifier.value}');
});
```

## 🚀 Deployment

### Docker Deployment
```bash
# Build Docker image
docker build -t ispilo-backend .

# Run with Docker Compose
docker-compose up -d

# Verify
docker-compose logs -f app
```

### Production Checklist
- [ ] Update application.yml with production URLs
- [ ] Configure HTTPS/TLS certificates
- [ ] Set strong JWT secret key
- [ ] Configure MySQL with password authentication
- [ ] Enable Redis for caching
- [ ] Setup AWS S3 bucket
- [ ] Configure CORS for production domain
- [ ] Enable rate limiting
- [ ] Setup monitoring and logging
- [ ] Configure backup strategy

## 📊 Monitoring

### Health Check
```bash
curl http://localhost:8080/actuator/health
```

### Application Metrics
```bash
curl http://localhost:8080/actuator/metrics
```

### View Logs
```bash
tail -f logs/ispilo-backend.log
```

## 🧪 Testing

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=UserControllerTest

# Run with coverage
mvn test jacoco:report
```

## 📖 Documentation Files

- [DATABASE_SETUP.md](DATABASE_SETUP.md) - Complete database setup guide
- [IMPLEMENTATION_GUIDE.md](IMPLEMENTATION_GUIDE.md) - Detailed implementation guide
- [API_SPECIFICATION.md](BACKEND_API_SPECIFICATION.md) - Complete API documentation
- [SPRING_BOOT_IMPLEMENTATION.md](SPRING_BOOT_IMPLEMENTATION.md) - Spring Boot specifics

## 🤝 Contributing

1. Create feature branch: `git checkout -b feature/amazing-feature`
2. Commit changes: `git commit -m 'Add amazing feature'`
3. Push to branch: `git push origin feature/amazing-feature`
4. Open Pull Request

## 📝 License

This project is licensed under the MIT License - see LICENSE file for details.

## 🆘 Support

For issues and questions:
1. Check [GitHub Issues](https://github.com/yourusername/ispilo-backend/issues)
2. Review documentation files
3. Check Swagger UI at `/swagger-ui.html`

## 🔄 Version History

### v1.0.0 (2026-01-16)
- Initial release
- Core authentication
- Product marketplace
- Education hub
- Real-time messaging with encryption
- Social feed

## 📞 Contact

- **Project Lead**: Your Name
- **Email**: your.email@example.com
- **Issues**: GitHub Issues
- **Discussions**: GitHub Discussions

---

**Built with ❤️ for the Ispilo Community**

Last Updated: January 16, 2026
Spring Boot Version: 3.2.3
Java Version: 17
MySQL Version: 8.0+

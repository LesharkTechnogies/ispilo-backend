# Ispilo Backend Implementation Guide

## Project Structure Overview

The Ispilo backend is a comprehensive Spring Boot 3.2.3 REST API designed to serve the Flutter mobile application with full authentication, real-time messaging with encryption, marketplace functionality, and educational content management.

### Directory Structure
```
ispilo-backend/
├── src/main/java/com/ispilo/
│   ├── controller/
│   │   ├── AuthController.java          # Authentication endpoints
│   │   ├── UserController.java          # User profile management
│   │   ├── ProductController.java       # Marketplace products
│   │   ├── EducationController.java     # Education hub (videos, courses)
│   │   ├── ConversationController.java  # Messaging conversations
│   │   ├── PostController.java          # Social feed posts
│   │   ├── SearchController.java        # Global search
│   │   ├── MessageController.java       # REST message endpoints
│   │   └── WebSocketController.java     # Real-time WebSocket messaging
│   ├── service/
│   │   ├── AuthService.java             # Authentication logic
│   │   ├── UserService.java             # User management
│   │   ├── ProductService.java          # Product operations
│   │   ├── EducationService.java        # Education content
│   │   ├── ConversationService.java     # Conversation management
│   │   ├── MessageService.java          # Message handling
│   │   ├── EncryptionService.java       # AES-256-GCM encryption
│   │   ├── FeedService.java             # Social feed
│   │   ├── SearchService.java           # Search functionality
│   │   ├── MediaService.java            # File uploads to S3
│   │   └── AuditService.java            # Audit logging
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── ProductRepository.java
│   │   ├── SellerRepository.java
│   │   ├── ConversationRepository.java
│   │   ├── MessageRepository.java
│   │   ├── PostRepository.java
│   │   ├── CommentRepository.java
│   │   ├── EducationVideoRepository.java
│   │   ├── EducationCourseRepository.java
│   │   ├── CourseEnrollmentRepository.java
│   │   └── AuditLogRepository.java
│   ├── model/
│   │   ├── entity/               # JPA entities
│   │   ├── dto/
│   │   │   ├── request/          # Request DTOs
│   │   │   └── response/         # Response DTOs
│   │   └── enums/                # Enumerations
│   ├── security/
│   │   ├── JwtUtil.java                 # JWT token generation/validation
│   │   ├── JwtAuthenticationFilter.java  # JWT filter
│   │   ├── SecurityConfig.java          # Security configuration
│   │   ├── UserDetailsServiceImpl.java   # User details loading
│   │   ├── UserPrincipal.java           # Principal wrapper
│   │   └── WebSocketAuthInterceptor.java # WebSocket authentication
│   ├── config/
│   │   ├── WebSocketConfig.java         # WebSocket configuration
│   │   ├── S3Config.java                # AWS S3 configuration
│   │   ├── RedisConfig.java             # Redis caching
│   │   ├── WebConfig.java               # CORS and web config
│   │   └── JpaConfig.java               # JPA auditing
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java  # Global error handling
│   │   ├── NotFoundException.java
│   │   ├── UnauthorizedException.java
│   │   ├── BadRequestException.java
│   │   ├── ConflictException.java
│   │   └── ErrorResponse.java
│   ├── interceptor/
│   │   └── AuditInterceptor.java        # Request auditing
│   ├── websocket/
│   │   └── StompPrincipal.java          # WebSocket principal
│   └── Main.java                        # Spring Boot entry point
├── src/main/resources/
│   ├── application.yml                  # Configuration file
│   └── data.sql                         # Initial data seeding
├── src/test/java/                       # Test classes
├── pom.xml                              # Maven dependencies
├── Dockerfile                           # Docker image
├── docker-compose.yml                   # Docker Compose setup
├── DATABASE_SETUP.md                    # Database documentation
└── README.md                            # Project README
```

## Core Features Implementation

### 1. Authentication & Authorization

#### JWT Token Flow
```
Client Login (username/password)
    ↓
AuthService validates credentials
    ↓
JwtUtil generates access token + refresh token
    ↓
Client stores tokens in SharedPreferences
    ↓
All subsequent requests include Authorization header
    ↓
JwtAuthenticationFilter validates token
    ↓
Request proceeds if token is valid, 401 if invalid
```

#### Token Structure
```yaml
Access Token: 
  - Type: Bearer JWT
  - Expiration: 24 hours (configurable)
  - Claims: userId, email, roles

Refresh Token:
  - Type: Bearer JWT
  - Expiration: 7 days (configurable)
  - Used to obtain new access token
```

### 2. End-to-End Encrypted Messaging

#### Encryption Architecture: AES-256-GCM
```
Message Flow:
1. Client creates message with plaintext content
2. Client generates/uses conversation encryption key
3. Client encrypts message using AES-256-GCM
4. Client sends encrypted message via WebSocket
5. Server receives encrypted message
6. Server stores: encrypted_content, IV, algorithm
7. Server broadcasts to recipient via WebSocket
8. Recipient decrypts using conversation key
9. Only plaintext shown to authorized users
```

#### Key Generation & Exchange
```
Per-Conversation Key Management:
1. User A initiates conversation
2. EncryptionService generates new AES-256 key
3. Key is encrypted for User B using RSA (future: implement)
4. Only conversation participants have the key
5. Key rotation happens every 90 days (configurable)
```

#### Message Entity Fields
```java
- id: UUID
- clientMsgId: Unique client-side identifier
- conversationId: Reference to conversation
- senderId: Who sent the message
- content: Plaintext (for immediate display)
- encryptedContent: AES-256-GCM encrypted content
- isEncrypted: Boolean flag
- encryptionAlgorithm: "AES-256-GCM"
- encryptionIv: Base64 encoded IV (12 bytes)
- isRead: Read receipt tracking
- readAt: Timestamp when read
- createdAt: Message creation time
```

### 3. Real-Time WebSocket Messaging

#### STOMP Protocol Configuration
```yaml
Endpoint: ws://localhost:8080/ws/chat

Messaging Prefixes:
- /app/chat.* → Server endpoints
- /topic/conversation/{id} → Broadcast channel
- /queue/* → Private channels
- /user/* → User-specific messages

Heartbeat: 25000ms (client), 25000ms (server)
```

#### WebSocket Message Routes
```
# Send encrypted message
Client → /app/chat.send (SendMessageRequest with encryptedContent)
Server → /topic/conversation/{id} (MessageResponse)

# Typing indicator (real-time)
Client → /app/chat.typing (TypingNotification)
Server → /topic/conversation/{id}/typing (TypingIndicator)

# Read receipts
Client → /app/chat.read (ReadReceiptRequest)
Server → /topic/conversation/{id}/read (ReadReceipt)

# Error handling
Server → /user/{sessionId}/queue/errors (ErrorMessage)
```

### 4. Product Marketplace

#### Product Catalog Structure
```json
{
  "id": "PRD-001",
  "title": "TP-Link TL-SF1005D 5-Port Switch",
  "seller": {
    "id": "seller-123",
    "businessName": "TechNet Solutions",
    "isVerified": true
  },
  "price": 800,
  "mainImage": "https://cdn.ispilo.co.ke/...",
  "images": ["url1", "url2", "url3"],
  "category": "Hardware",
  "condition": "New",
  "rating": 4.8,
  "reviewCount": 256,
  "location": "Nairobi, Kenya",
  "stockQuantity": 15,
  "isAvailable": true,
  "isFeatured": false,
  "createdAt": "2026-01-16T10:00:00Z"
}
```

#### Product Search Capabilities
```
GET /api/products
  - Full product listing with pagination

GET /api/products/search?keyword=switch
  - Search by title, description, category

GET /api/products/category/{category}
  - Filter by product category

GET /api/products/seller/{sellerId}
  - Products from specific seller

GET /api/products/featured
  - Featured products showcase

GET /api/products/trending
  - High-rated products

GET /api/products/categories
  - Available categories for filtering
```

### 5. Education Hub

#### Education Content Types

##### Videos
```json
{
  "id": "vid-001",
  "title": "Understanding Fiber Optics Basics",
  "channel": "ISPilo Academy",
  "thumbnail": "https://...",
  "videoUrl": "https://...",
  "duration": "12:34",
  "views": 12300,
  "category": "Networking",
  "rating": 4.5
}
```

##### Courses
```json
{
  "id": "course-001",
  "title": "Advanced Network Security Fundamentals",
  "instructor": "Dr. Sarah Chen",
  "thumbnail": "https://...",
  "category": "Security",
  "enrollmentCount": 1250,
  "rating": 4.8,
  "durationHours": 40,
  "totalLessons": 15,
  "topics": ["SSL/TLS", "Firewalls", "IDS/IPS"]
}
```

##### Enrollment
```json
{
  "id": "enroll-001",
  "userId": "user-123",
  "courseId": "course-001",
  "progress": 65.5,
  "completedLessons": 10,
  "isCompleted": false,
  "enrolledAt": "2026-01-01T10:00:00Z"
}
```

#### Education Endpoints
```
Videos:
GET /api/education/videos
GET /api/education/videos/search?keyword=...
GET /api/education/videos/trending
GET /api/education/videos/category/{category}

Courses:
GET /api/education/courses
GET /api/education/courses/popular
GET /api/education/courses/top-rated
POST /api/education/courses/{courseId}/enroll

User Courses:
GET /api/education/my-courses
GET /api/education/my-courses/in-progress
GET /api/education/my-courses/completed
PUT /api/education/enrollments/{enrollmentId}/progress
```

### 6. Social Feed

#### Post Structure
```json
{
  "id": "post-001",
  "userId": "user-123",
  "content": "Just finished setting up a new fiber network...",
  "imageUrl": "https://...",
  "isLiked": false,
  "isSaved": false,
  "isSponsored": false,
  "likesCount": 42,
  "commentsCount": 8,
  "viewCount": 156,
  "createdAt": "2026-01-16T10:00:00Z"
}
```

#### Comment Structure
```json
{
  "id": "comment-001",
  "postId": "post-001",
  "userId": "user-456",
  "content": "Great work!",
  "likesCount": 2,
  "createdAt": "2026-01-16T10:15:00Z"
}
```

## Flutter Integration

### 1. API Service Setup

```dart
// Initialize ApiService with base URL
final apiService = ApiService.baseUrl; // http://localhost:8080/api

// All requests automatically include JWT token from SharedPreferences
// Error handling: 401 clears tokens and navigates to login
```

### 2. User Authentication Flow

```dart
// Login
final response = await ApiService.post('/auth/login', {
  'email': 'user@example.com',
  'password': 'password123',
});
// Response: { token, refreshToken, user {...} }

// Store tokens
await prefs.setString('auth_token', response['token']);
await prefs.setString('refresh_token', response['refreshToken']);

// Subsequent requests automatically include Authorization header
```

### 3. Product Marketplace Integration

```dart
// Replace static marketplace_data.dart with API calls
final products = await ApiService.get('/products?page=0&size=20');

// Search
final results = await ApiService.get('/products/search?keyword=switch');

// Get by category
final networking = await ApiService.get('/products/category/Hardware');
```

### 4. Education Hub Integration

```dart
// Trending videos
final videos = await ApiService.get('/education/videos/trending');

// My enrolled courses
final myCourses = await ApiService.get('/education/my-courses');

// Enroll in course
await ApiService.post('/education/courses/{courseId}/enroll', {});
```

### 5. Real-Time Messaging with WebSocket

```dart
// Initialize WebSocket
final _socketUrl = 'ws://localhost:8080/ws/chat?token=$authToken';

// Connect
_channel = WebSocketChannel.connect(Uri.parse(_socketUrl));

// Listen for messages
_channel.stream.listen((message) {
  final data = jsonDecode(message);
  // Handle encrypted message
  final decrypted = await decryptMessage(data['encryptedContent']);
});

// Send encrypted message
final encrypted = encryptMessage(messageContent);
_channel.sink.add(jsonEncode({
  'conversationId': convId,
  'encryptedContent': encrypted,
  'encryptionKey': key,
}));
```

## Database Schema

### User Tables
- `users`: User accounts and profiles
- `sellers`: Seller shop information
- `audit_logs`: User activity tracking

### Product Tables
- `products`: Product listings
- `product_images`: Product photos
- `categories`: Product categories
- `product_categories`: Product-category mappings (many-to-many)

### Messaging Tables
- `conversations`: Chat conversations
- `messages`: Chat messages with encryption fields

### Education Tables
- `education_videos`: Educational videos
- `education_courses`: Online courses
- `user_course_enrollments`: User course progress

### Social Tables
- `posts`: Social feed posts
- `comments`: Post comments
- `post_views`: Post view tracking
- `follows`: User follow relationships

## API Response Format

### Success Response
```json
{
  "status": "success",
  "code": 200,
  "message": "Operation successful",
  "data": { ... },
  "timestamp": "2026-01-16T10:00:00Z"
}
```

### Error Response
```json
{
  "status": "error",
  "code": 400,
  "message": "Bad request",
  "errors": {
    "field": "Error message"
  },
  "timestamp": "2026-01-16T10:00:00Z"
}
```

### Paginated Response
```json
{
  "content": [ ... ],
  "pageNumber": 0,
  "pageSize": 20,
  "totalElements": 256,
  "totalPages": 13,
  "isLast": false
}
```

## Security Best Practices

1. **JWT Token Management**
   - Store tokens in encrypted SharedPreferences
   - Refresh tokens before expiration
   - Clear tokens on logout

2. **Message Encryption**
   - Use AES-256-GCM for all private messages
   - Generate unique IV for each message
   - Rotate conversation keys every 90 days

3. **HTTPS in Production**
   - All API calls must use HTTPS (https://api.ispilo.co.ke)
   - WebSocket uses WSS (wss://api.ispilo.co.ke/ws/chat)

4. **Input Validation**
   - Server-side validation for all inputs
   - Rate limiting on authentication endpoints
   - CSRF protection enabled

5. **CORS Configuration**
   - Restricted to trusted domains in production
   - Allow credentials with tokens

## Deployment Guide

### Local Development
```bash
# Start MySQL
mysql -u root -p

# Create database
CREATE DATABASE ispilo_db;

# Build and run
mvn clean install
mvn spring-boot:run

# Access Swagger UI
http://localhost:8080/swagger-ui.html
```

### Production Deployment
```bash
# Build JAR
mvn clean package -DskipTests

# Use Docker Compose
docker-compose up -d

# Or deploy to cloud (AWS ECS, Google Cloud Run, etc.)
```

## Monitoring & Logging

### Health Check
```
GET /actuator/health
```

### Metrics
```
GET /actuator/metrics
```

### Application Logs
- Location: `/logs/ispilo-backend.log`
- Level: INFO for production, DEBUG for development
- Include: Request ID, User ID, operation, timestamp

## Future Enhancements

1. **Search Optimization**: Add Elasticsearch for better search
2. **Video Streaming**: Integrate HLS/DASH for video streaming
3. **Payment Gateway**: Add payment processing (M-Pesa, Stripe)
4. **Notifications**: Push notifications via FCM
5. **Social Analytics**: User analytics and recommendations
6. **Admin Dashboard**: Backend admin panel

---

**Last Updated**: January 16, 2026
**Version**: 1.0.0
**Spring Boot**: 3.2.3
**Java**: 17
**Database**: MySQL 8.0+

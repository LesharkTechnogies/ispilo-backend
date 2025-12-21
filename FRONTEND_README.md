# Ispilo Frontend Integration Guide

This guide provides information for frontend developers to integrate with the Ispilo Backend API.

## Base URL
`http://localhost:8080/api/v1`

## Authentication

Most endpoints require a Bearer Token.

1. **Login**: `POST /auth/login`
2. **Register**: `POST /auth/register`
3. **Refresh Token**: `POST /auth/refresh`

Include the token in the header:
`Authorization: Bearer <your_jwt_token>`

## Key Modules

### 1. Social Feed (Posts)
- `GET /posts`: Get paginated feed.
- `POST /posts`: Create a new post.
- `POST /posts/{id}/like`: Like/Unlike a post.
- `GET /posts/{id}/comments`: Get comments for a post.

### 2. Marketplace (Sellers & Products)
- `POST /sellers`: Register as a seller.
- `GET /products`: List all products.
- `GET /products/category/{category}`: Filter by category.
- `GET /sellers/{id}/products`: Get products by a specific seller.

### 3. Messaging (WebSockets)
The chat system uses WebSockets for real-time updates.

- **Endpoint**: `ws://localhost:8080/ws`
- **Topic for receiving**: `/user/queue/messages`
- **Destination for sending**: `/app/chat`

**Message Object Structure:**
```json
{
  "clientMsgId": "uuid-generated-by-client",
  "conversationId": "uuid",
  "content": "Hello world",
  "type": "TEXT"
}
```

## Common Data Models

### User
```json
{
  "id": "string",
  "email": "string",
  "name": "string",
  "avatar": "url_string",
  "isVerified": boolean
}
```

### Post
```json
{
  "id": "string",
  "description": "string",
  "mediaUrls": ["url1", "url2"],
  "likesCount": 0,
  "commentsCount": 0,
  "user": { ...UserObject },
  "createdAt": "ISO-8601-Timestamp"
}
```

## Error Handling
The API returns standard HTTP status codes:
- `200/201`: Success
- `400`: Bad Request (Validation errors)
- `401`: Unauthorized (Missing or invalid token)
- `403`: Forbidden (Insufficient permissions)
- `404`: Not Found
- `500`: Internal Server Error

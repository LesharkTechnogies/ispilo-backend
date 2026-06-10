# Ispilo API Guide

This document is the integration reference for the Ispilo mobile/web clients. It covers the full application feature set from registration through logout, plus admin-only APIs.

---my app is hosted at https://ispilo.hantardev.tech 
## Base URL & Versioning


- **API Prefix:** `/api/v1`
- Most controllers also accept `/api` and `/api/v2` for backward compatibility.

## Authentication & Headers

- **Bearer token:** `Authorization: Bearer <accessToken>`
- **Device header (required for auth):** `X-Device-ID: <device-id>`
- **Optional app header:** `X-App-ID: <app-id>`
- **Content type:** `application/json`
- **STOMP WebSocket:** uses STOMP over WebSocket (`/ws/chat`) with JWT token as query param.

## Pagination

Many list endpoints accept `page` and `size` query params (0-based). Responses are either Spring `Page` or `PageResponse` objects.

## Standard Error Format

```json
{
  "error": {
    "code": "INVALID_ARGUMENT",
    "message": "Human readable message",
    "details": {}
  }
}
```

---

## 1) Authentication & Account Access

### Register
- **POST** `/api/v1/auth/register`
- **Headers:** `X-Device-ID`
- **Body:**
```json
{
  "email": "user@example.com",
  "password": "secret123",
  "firstName": "Jane",
  "lastName": "Doe",
  "phone": "+254700000000",
  "countryCode": "KE",
  "county": "Nairobi",
  "town": "Westlands"
}
```
- **Response (201):**
```json
{
  "success": true,
  "message": "Registration successful",
  "data": {
    "accessToken": "...",
    "refreshToken": "...",
    "user": { "id": "...", "email": "...", "name": "Jane Doe" }
  }
}
```

### Login
- **POST** `/api/v1/auth/login`
- **Headers:** `X-Device-ID`
- **Body:**
```json
{ "phone": "+254700000000", "password": "secret123" }
```
- **Response (200):** `AuthResponse`

### Refresh token
- **POST** `/api/v1/auth/refresh`
- **Body:**
```json
{ "refreshToken": "..." }
```
- **Response (200):** `RefreshTokenResponse`

### Forgot password (request code)
- **POST** `/api/v1/auth/forgot-password/request-code`
- **Body:**
```json
{ "email": "user@example.com" }
```
- **Response (200):** `{ "success": true, "message": "Verification code sent" }`

### Forgot password (resend code)
- **POST** `/api/v1/auth/forgot-password/resend-code`
- **Body:**
```json
{ "email": "user@example.com" }
```

### Reset password (with code)
- **POST** `/api/v1/auth/forgot-password/reset`
- **Body:**
```json
{ "email": "user@example.com", "code": "123456", "newPassword": "newSecret" }
```
- **Response (200):** `{ "success": true, "message": "Password updated successfully" }`

### Logout (app deactivation)
Server-side token logout is not required; the client should delete tokens locally. If you want to deactivate the app installation, use:
- **POST** `/api/app/deactivate/{appId}`

---

## 2) App Registration & Security

### Register app installation
- **POST** `/api/app/register`
- **Body:**
```json
{ "deviceId": "device-uuid", "platform": "android" }
```
- **Response (201):** `{ appId, appPrivateKey, serverPublicKey, encryptionAlgorithm }`

### Public key (mobile)
- **GET** `/api/app/public-key`
- **Headers:** `X-App-ID`, `X-Device-ID`

### Public key (web)
- **GET** `/api/v1/app/public-key`

### Verify app
- **GET** `/api/app/verify/{appId}`

### Test encryption
- **POST** `/api/app/test-encryption`
- **Headers:** `X-App-ID`, `X-Device-ID`
- **Body:** `{ "encryptedMessage": "..." }`

### Web-friendly app registration
- **POST** `/api/v1/registerApp`

### Update app metadata
- **POST** `/api/v1/app/metadata`

### App version
- **GET** `/api/v1/app/version`

---

## 3) User Profile & Settings

### Current user
- **GET** `/api/v1/users/me`

### Update profile
- **PUT** `/api/v1/users/me`
- **Body:**
```json
{ "name": "Jane Doe", "bio": "About me", "location": "Nairobi", "phone": "+2547...", "phonePrivacyPublic": false }
```

### Update avatar
- **POST** `/api/v1/users/me/avatar` (multipart form-data)
- **Field:** `avatar` (file)

### User stats
- **GET** `/api/v1/users/me/stats`
- **GET** `/api/v1/users/{userId}/stats`

### Preferences / settings
- **GET** `/api/v1/users/me/preferences`
- **PUT** `/api/v1/users/me/preferences`
- **Body:**
```json
{
  "biometricAuth": false,
  "twoFactorAuth": false,
  "accountVisibility": true,
  "phonePrivacyPublic": false,
  "socialNotifications": true,
  "messageNotifications": true,
  "educationNotifications": false,
  "marketplaceNotifications": true,
  "themeMode": "SYSTEM"
}
```

### Profile details
- **GET** `/api/v1/users/{userId}`

### Profile posts
- **GET** `/api/v1/users/{userId}/posts?page=0&size=20`
- **GET** `/api/v1/posts/user/{userId}?page=0&size=20`

### My posts
- **GET** `/api/v1/users/me/posts?page=0&size=20`
- **GET** `/api/v1/posts/me?page=0&size=20`

### Follow/unfollow
- **POST** `/api/v1/users/{userId}/follow`

### Followers / following / connections
- **GET** `/api/v1/users/{userId}/followers`
- **GET** `/api/v1/users/{userId}/following`
- **GET** `/api/v1/users/{userId}/connections`

### Discover people
- **GET** `/api/v1/users/discover?page=0&size=10`

### Update password
- **POST** `/api/v1/users/me/password`
- **Body:** `{ "currentPassword": "...", "newPassword": "..." }`

### Update FCM token
- **POST** `/api/v1/users/fcm-token`
- **Body:** `{ "fcmToken": "..." }`

### Delete account
- **DELETE** `/api/v1/users/me/account`

---

## 4) Posts & Comments

*Architectural Note: Post likes and interaction states (`isLiked`) are powered by an in-memory Redis Set infrastructure to provide `O(1)` read/write speeds, enabling seamless UI syncing and resolving detached-entity database bugs at high scale.*

### Create post
- **POST** `/api/v1/posts`
- **Body:**
```json
{ "content": "Hello world", "mediaUrls": ["https://..."] }
```
- **Validation Rule:** Standard posts only support pictures. Any URL ending in `.mp4`, `.mov`, `.avi`, `.mkv`, `.webm`, or `.wmv` will be rejected with a `400 Bad Request`. Videos must be uploaded via the dedicated Video Module.

### Get post
- **GET** `/api/v1/posts/{postId}`

### Update post
- **PUT** `/api/v1/posts/{postId}`

### Delete post
- **DELETE** `/api/v1/posts/{postId}`

### Feed
- **GET** `/api/v1/posts/feed?page=0&size=20`

### Like / unlike
- **POST** `/api/v1/posts/{postId}/like`

### Share post
- **POST** `/api/v1/posts/{postId}/share`
- **POST** `/api/v1/posts/groups/{groupId}/posts/{postId}/share`

### Comments (nested)
- **GET** `/api/v1/posts/{postId}/comments?page=0&size=20`
- **POST** `/api/v1/posts/{postId}/comments`
- **Body:**
```json
{ "content": "Nice post", "parentCommentId": "optional-parent-id" }
```
- **POST** `/api/v1/posts/comments/{commentId}/like`
- **Response (200):** Toggles like status for the comment.

---

## 5) Groups & Group Posts

### Create group
- **POST** `/api/v1/groups`
- **Body:**
```json
{
  "name": "Flutter Devs",
  "description": "Group for Flutter Developers",
  "isPrivateGroup": false
}
```
- **Response (201):** `GroupResponse`

### Join group
- **POST** `/api/v1/groups/{groupId}/join`

### Promote to admin
- **POST** `/api/v1/groups/{groupId}/members/{memberId}/promote`

### Remove member / leave
- **DELETE** `/api/v1/groups/{groupId}/members/{memberId}`

### Create group post
- **POST** `/api/v1/groups/{groupId}/posts`
- **Body:**
```json
{
  "content": "Hello group, what do you think?",
  "description": "Optional alias for content",
  "imageUrl": "https://...",
  "mediaUrls": ["https://..."],
  "groupId": "...",
  "isAnonymous": false
}
```
- **Response (201):** `GroupPostResponse` (Includes `authorId`, `authorName`, and `authorAvatar` if not anonymous)

### Group feed
- **GET** `/api/v1/groups/{groupId}/posts?page=0&size=20`
- **Response (200):** `PageResponse<GroupPostResponse>`

### Like group post
- **POST** `/api/v1/groups/{groupId}/posts/{postId}/like`
- **Response (200):** `GroupPostResponse`

### Delete group post
- **DELETE** `/api/v1/groups/{groupId}/posts/{postId}`

---

## 6) Stories

### Create story
- **POST** `/api/v1/stories`
- **Body:** `CreateStoryRequest` (text/media)

### Active stories
- **GET** `/api/v1/stories`

### Delete story
- **DELETE** `/api/v1/stories/{storyId}`

### Trigger cleanup
- **POST** `/api/v1/stories/cleanup`

---

## 7) Marketplace (Sellers & Products)

### Seller profile
- **POST** `/api/v1/sellers`
- **Body:**
```json
{
  "businessName": "Shop",
  "businessDescription": "My shop description (10-1000 characters)",
  "businessAddress": "123 Market St"
}
```
- **Response (201):** `SellerResponse`

### Seller verification (KRA PIN)
- **POST** `/api/v1/sellers/verification`
- **Body:**
```json
{
  "nationalIdImage": "https://...",
  "phone": "+2547...",
  "fullName": "Jane Doe",
  "kraPin": "A1234567",
  "requestedLevel": "ID_VERIFIED"
}
```
- **Response (200):** `{ "message": "Verification request submitted" }`

### Seller reviews
- **GET** `/api/v1/sellers/{sellerId}/reviews?page=0&size=20`
- **Response (200):** `PageResponse<SellerReviewResponse>`

- **POST** `/api/v1/sellers/{sellerId}/reviews`
- **Body:**
```json
{
  "rating": 5,
  "comment": "Great seller! (10-500 chars)",
  "title": "Excellent experience",
  "wouldRecommend": true
}
```
- **Response (201):** `SellerReviewResponse`

### Report seller
- **POST** `/api/v1/sellers/{sellerId}/reports`
- **Body:**
```json
{
  "reason": "Inappropriate content",
  "description": "More details... (optional)"
}
```
- **Response (201):** `ReportResponse`

### Product image upload
- **POST** `/api/v1/products/upload` (multipart form-data)
- **Field:** `file`

### List/search products
- **GET** `/api/v1/products?page=0&size=20&category=...&sortBy=asc`
- **GET** `/api/v1/products/search?keyword=...`
- **GET** `/api/v1/products/trending`
- **GET** `/api/v1/products/featured`
- **GET** `/api/v1/products/seller/{sellerId}`
- **GET** `/api/v1/products/category/{category}`
- **GET** `/api/v1/products/seller-level/{level}`

### Product details
- **GET** `/api/v1/products/{productId}`
- **GET** `/api/v1/products/{productId}/with-seller`
- **GET** `/api/v1/products/{productId}/complete`

### Create/update/delete product
- **POST** `/api/v1/products`
- **PUT** `/api/v1/products/{productId}`
- **DELETE** `/api/v1/products/{productId}`

### Favorites
- **POST** `/api/v1/products/{productId}/favorite`
- **DELETE** `/api/v1/products/{productId}/favorite`

### Categories
- **GET** `/api/v1/products/categories`

### Reviews
- **GET** `/api/v1/products/{productId}/reviews?page=0&size=20`
- **POST** `/api/v1/products/{productId}/reviews`
- **POST** `/api/v1/products/reviews/{reviewId}/like`
- **POST** `/api/v1/products/reviews/{reviewId}/dislike`

### Report product
- **POST** `/api/v1/products/{productId}/reports`

---

## 8) Chats (REST)

### Conversations
- **POST** `/api/v1/conversations`
- **Body:**
```json
{
  "type": "DIRECT", // "DIRECT" or "GROUP"
  "name": "Group Name (optional)",
  "participantIds": ["user-id-1", "user-id-2"]
}
```
- **Response (201):** `ConversationResponse` (includes `name` and `avatar` automatically resolved for direct chats)

- **GET** `/api/v1/conversations?page=0&size=20&participantId=...&userId=...&sellerId=...`
- **Response (200):** `PageResponse<ConversationResponse>` (If query params provided, returns direct `ConversationResponse`)

- **GET** `/api/v1/conversations/{conversationId}`
- **Response (200):** `ConversationResponse`

- **GET** `/api/v1/conversations/direct/{otherUserId}`
- **Response (200):** `ConversationResponse`

- **GET** `/api/v1/conversations/direct?participantId=...`
- **Response (200):** `ConversationResponse`

- **DELETE** `/api/v1/conversations/{conversationId}`

### Send message (REST)
- **POST** `/api/v1/conversations/{conversationId}/messages`
- **Body:**
```json
{
  "type": "TEXT", // "TEXT", "IMAGE", "VIDEO", "AUDIO", "FILE"
  "content": "Hello",
  "mediaUrl": "https://...",
  "clientMsgId": "uuid-v4 (auto-generated if missing)",
  "replyToMessageId": "optional-message-id"
}
```
- **Response (201):** `MessageResponse`

### Conversation messages
- **GET** `/api/v1/conversations/{conversationId}/messages?page=0&size=50`
- **Response (200):** `PageResponse<MessageResponse>`

### Mark read
- **PUT** `/api/v1/conversations/{conversationId}/read`

### Delete message (for everyone)
- **DELETE** `/api/v1/conversations/{conversationId}/messages/{messageId}`

### Message endpoints
- **GET** `/api/v1/messages/conversation/{conversationId}?page=0&size=20`
- **POST** `/api/v1/messages/conversation/{conversationId}/read`
- **POST** `/api/v1/messages/{messageId}/react` (body: `{ "emoji": "👍" }`)
- **POST** `/api/v1/messages/{messageId}/delete-for-me`
- **POST** `/api/v1/messages/{messageId}/delete-for-everyone`

---

## 9) Chats (WebSocket - STOMP)

**Protocol:** STOMP over WebSocket

**Socket URL:** `ws://<host>/ws/chat?token=<jwt>`

### Connect headers
Clients should send `Authorization: Bearer <jwt>` when supported by the STOMP client. If headers are not supported, the `token` query param is required.

### Destinations
- **Create conversation:** `/app/conversation.create`
- **Join conversation:** `/app/conversation.join`
- **Send message:** `/app/chat.send`
- **Typing:** `/app/chat.typing`
- **Read receipts:** `/app/chat.read`
- **Delivered receipts:** `/app/chat.delivered`
- **React:** `/app/chat.react`
- **Delete:** `/app/chat.delete`

### Send message (STOMP payload)
```json
{
  "conversationId": "...",
  "type": "TEXT",
  "content": "Hello",
  "mediaUrl": null,
  "clientMsgId": "client-uuid",
  "replyToMessageId": "optional-message-id"
}
```

### Subscriptions
- `/topic/conversation/{conversationId}`
- `/topic/conversation/{conversationId}/typing`
- `/topic/conversation/{conversationId}/read`
- `/user/queue/conversation.created`
- `/user/queue/messages.sync`

---

## 10) Education Hub

### Videos
- **GET** `/api/education/videos?page=0&size=20`
- **GET** `/api/education/videos/search?keyword=...`
- **GET** `/api/education/videos/trending`
- **GET** `/api/education/videos/top-rated`
- **GET** `/api/education/videos/category/{category}`
- **GET** `/api/education/videos/channel/{channel}`
- **GET** `/api/education/videos/categories`
- **GET** `/api/education/videos/channels`

### Courses
- **GET** `/api/education/courses?page=0&size=20`
- **GET** `/api/education/courses/search?keyword=...`
- **GET** `/api/education/courses/popular`
- **GET** `/api/education/courses/top-rated`
- **GET** `/api/education/courses/category/{category}`
- **GET** `/api/education/courses/categories`
- **GET** `/api/education/courses/instructors`

### Enrollments
- **POST** `/api/education/courses/{courseId}/enroll`
- **GET** `/api/education/my-courses?page=0&size=10`
- **GET** `/api/education/my-courses/in-progress?page=0&size=10`
- **GET** `/api/education/my-courses/completed`
- **PUT** `/api/education/enrollments/{enrollmentId}/progress?progress=0.5&completedLessons=3`
- **GET** `/api/education/courses/{courseId}/enrolled-status`

---

## 11) Search

---

## 10.5) Video Module (Short Videos - High Performance)

*Optimized for TikTok-style infinite scrolling, with direct client-to-R2 uploads to handle massive concurrent uploads without stressing the backend.*

### Video Feed (Discover)
- **GET** `/api/v1/videos/feed`
- **Query Params:**
  - `page` (int, default=0): Page number
  - `size` (int, default=10): Items per page
- **Response (200):** `PageResponse<VideoResponse>`

### Following & Trending Feeds
- **GET** `/api/v1/videos/feed/following`
- **GET** `/api/v1/videos/feed/trending`

### Upload Video (Optimized Autonomous Flow)
Handles multiple concurrent uploads efficiently using pre-signed URLs directly to Cloudflare R2.

1. **Initiate Upload:**
   - **POST** `/api/v1/videos/upload/initiate`
   - **Body (JSON):**
     ```json
     { 
       "caption": "Check out my new video! (optional)", 
       "hashtags": ["fun", "viral"], 
       "contentType": "video/mp4",
       "durationSeconds": 15
     }
     ```
   - **Response (200):**
     ```json
     {
       "videoId": "uuid-of-video",
       "uploadUrl": "https://<R2_BUCKET>.r2.cloudflarestorage.com/videos/..."
     }
     ```
     
2. **Client Upload:** 
   - Client directly performs an HTTP `PUT` request to the returned `uploadUrl` with the raw binary video file and `Content-Type: video/mp4` header.

3. **Complete Upload:**
   - **POST** `/api/v1/videos/{videoId}/complete`
   - **Response (200):** Triggers autonomous server-side async processing.

### Get Single Video
- **GET** `/api/v1/videos/{videoId}`
- **Response (200):** `VideoResponse`

### Video Discovery & Hashtags
- **GET** `/api/v1/videos/user/{userId}`
- **GET** `/api/v1/videos/hashtag/{hashtag}`
- **GET** `/api/v1/videos/hashtags/trending`

### Video Interaction
- **POST** `/api/v1/videos/{videoId}/view`
- **POST** `/api/v1/videos/{videoId}/like`
- **POST** `/api/v1/videos/{videoId}/share`
- **DELETE** `/api/v1/videos/{videoId}`
- **GET** `/api/v1/videos/{videoId}/comments`
- **POST** `/api/v1/videos/{videoId}/comments`
- **GET** `/api/v1/videos/comments/{commentId}/replies`
- **POST** `/api/v1/videos/comments/{commentId}/like`
- **DELETE** `/api/v1/videos/comments/{commentId}`

---

## 11) Search

- **GET** `/api/search/posts?q=...&page=0&size=20`
- **GET** `/api/search/people?q=...&page=0&size=20`
- **GET** `/api/search/groups?q=...&page=0&size=20`
- **GET** `/api/search/typeahead?q=...&limit=10`

---

## 12) Media & Public Endpoints

- **GET** `/` (status info)
- **GET** `/health`
- **GET** `/api/v1/cloudinary` (returns Cloudinary config)
- **GET** `/api/v1/app/version`

---

## 13) Admin Privileges API

All admin endpoints require an admin user (checked by `isAdmin`). Base path: `/api/v1/admin`.

### Dashboard & audit
- **GET** `/dashboard/stats`
- **GET** `/audit-logs?page=0&size=50`
- **GET** `/audit-trace?userId=&action=&resourceType=&resourceId=&from=&to=&page=0&size=50`

### Promote admin
- **POST** `/promote`
- **Body:** `{ "email": "admin@example.com", "password": "...", "targetEmail": "user@example.com" }`

### Users
- **GET** `/users?query=&page=0&size=50`
- **GET** `/users/{userId}`
- **POST** `/users`
- **PUT** `/users/{userId}`
- **DELETE** `/users/{userId}`
- **POST** `/users/{userId}/flag` (body: `{ "flagged": true, "reason": "...", "blockHours": 24 }`)

### Sellers
- **GET** `/sellers?query=&page=0&size=50`
- **GET** `/sellers/{sellerId}`
- **PUT** `/sellers/{sellerId}`
- **POST** `/sellers/{sellerId}/flag`

### Products
- **GET** `/products?query=&page=0&size=50`
- **GET** `/products/{productId}`
- **PUT** `/products/{productId}`
- **POST** `/products/{productId}/flag`

### Messages
- **GET** `/messages?conversationId=&senderId=&page=0&size=50`
- **GET** `/messages/{messageId}`
- **DELETE** `/messages/{messageId}`

### Reports moderation
- **POST** `/reports/products/{reportId}/review`
- **POST** `/reports/sellers/{reportId}/review`

### Message restore & export
- **POST** `/messages/{messageId}/restore-everyone`
- **POST** `/messages/{messageId}/restore-for-user/{userId}`
- **POST** `/conversations/{conversationId}/restore-for-user/{userId}`
- **GET** `/conversations/{conversationId}/export`

---

## 14) Example Response Shapes

### `UserResponse`
```json
{
  "id": "...",
  "email": "...",
  "name": "...",
  "avatar": "...",
  "phone": "...",
  "isVerified": true,
  "isAdminUser": false,
  "isFlagged": false,
  "flagReason": null,
  "blockedUntil": null
}
```

### `ProductResponse`
```json
{
  "id": "...",
  "sellerId": "...",
  "title": "...",
  "price": 450.00,
  "images": ["https://..."],
  "isAvailable": true,
  "isFeatured": false,
  "isFlagged": false
}
```

### `MessageResponse`
```json
{
  "id": "...",
  "conversationId": "...",
  "senderId": "...",
  "senderName": "John Doe",
  "senderAvatar": "https://res.cloudinary.com/...",
  "type": "TEXT",
  "content": "hello",
  "status": "SENT",
  "deletedForEveryone": false
}
```

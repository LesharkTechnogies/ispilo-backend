# Admin Dashboard & API Documentation

## 1. Admin Registration Status (Is it implemented?)
There is **no dedicated admin registration endpoint** in the API. Admin status is controlled by the `is_admin` flag on the `users` table.

### How to Register an Admin User
1. Register a normal user via **POST** `/api/v1/auth/register`.
2. Promote that user to admin by updating `users.is_admin` in the database.

```sql
UPDATE users SET is_admin = true WHERE email = 'admin@example.com';
```

### Registration Parameters (same as standard register)
The admin user is created using the same payload as the normal registration endpoint:
```json
{
  "firstName": "Admin",
  "lastName": "User",
  "email": "admin@example.com",
  "password": "StrongPassword123",
  "phone": "+254700000000",
  "countryCode": "KE",
  "county": "Nairobi",
  "town": "Nairobi"
}
```

## 2. How to Login as Admin
Once the user has `is_admin` set to true, they can log in via the standard authentication endpoint.
- **POST** `/api/v1/auth/login`
  - **Payload:**
    ```json
    {
      "email": "admin@example.com",
      "password": "yourpassword"
    }
    ```
  - **Response:** Returns JWT tokens with admin roles/claims.

## 3. Admin Dashboard Capabilities
Admins can access system-wide statistics and audit logs for dashboards and system control.
- **GET** `/api/v1/admin/dashboard/stats`
  - Returns overall platform statistics (users, posts, group posts, groups, products, sellers).
- **GET** `/api/v1/admin/audit-logs`
  - Returns paginated audit logs of system actions.

### Access Control
All admin endpoints require the authenticated user to have `is_admin = true`. Non-admin users receive **401 Unauthorized**.

### Promote User to Admin (API)
Admins can promote another user (or themselves) by validating admin credentials.
- **POST** `/api/v1/admin/promote`
  - **Payload:**
    ```json
    {
      "email": "admin@example.com",
      "password": "AdminPassword123",
      "targetEmail": "user-to-promote@example.com"
    }
    ```
  - **Notes:**
    - `targetEmail` is optional; if omitted, the admin user promotes themselves.
  - **Response:**
    ```json
    {
      "success": true,
      "message": "User promoted to admin",
      "userId": "uuid",
      "email": "user-to-promote@example.com"
    }
    ```

---

## 4. Complete API Reference

Below is the comprehensive list of all API functions across the platform, including their HTTP methods and parameters/payloads.

### Authentication (`/api/v1/auth`)
- **POST** `/register` - Register a new user (`RegisterRequest` payload)
- **POST** `/login` - Authenticate user and return tokens (`LoginRequest` payload)
- **POST** `/refresh` - Refresh access token (`RefreshTokenRequest` payload)
- **POST** `/forgot-password/request-code` - Request a password reset code
- **POST** `/forgot-password/resend-code` - Resend verification code
- **POST** `/forgot-password/reset` - Reset password using the code (`ResetPasswordWithCodeRequest` payload)

### Admin (`/api/v1/admin`)
- **GET** `/dashboard/stats` - Display all overall dashboard data
- **GET** `/audit-logs` - Retrieve system audit logs (supports `page` and `size` query params)
- **POST** `/promote` - Promote a user to admin (validates admin credentials)

### Users (`/api/v1/users`)
- **GET** `/discover` - Discover users. **Params:** `page`, `size`. **Returns:** `Page<UserResponse>`
- **GET** `/me` - Get current authenticated user profile. **Returns:** `UserResponse`
- **PUT** `/me` - Update current user profile. **Payload:** `UpdateProfileRequest`. **Returns:** `UserResponse`
- **POST** `/me/avatar` - Upload/update user avatar. **Params:** `avatar` (MultipartFile). **Returns:** `UserResponse`
- **GET** `/me/stats` - Get current user stats. **Returns:** `Map<String, Object>`
- **GET** `/{userId}/stats` - Get a specific user's stats. **Returns:** `Map<String, Object>`
- **GET** `/me/preferences` - Get current user preferences. **Returns:** `Map<String, Object>`
- **PUT** `/me/preferences` - Update user preferences. **Payload:** `UpdateSettingsRequest`. **Returns:** `Map<String, Object>`
- **GET** `/{userId}` - Get user profile by ID. **Returns:** `UserProfileResponse`
- **POST** `/{userId}/follow` - Toggle follow on a user. **Returns:** `Map<String, Object>`
- **GET** `/{userId}/followers` - Get followers of a user. **Returns:** `List<UserResponse>`
- **GET** `/{userId}/following` - Get users a user is following. **Returns:** `List<UserResponse>`
- **GET** `/{userId}/connections` - Get user connections. **Returns:** `List<UserResponse>`
- **DELETE** `/me/account` - Delete current user account. **Returns:** `Map<String, String>`
- **POST** `/me/password` - Update user password. **Payload:** `UpdatePasswordRequest`. **Returns:** `Map<String, String>`
- **POST** `/fcm-token` - Update FCM token for push notifications. **Payload:** `{"fcmToken": "..."}`. **Returns:** `Map<String, String>`

### Posts & Comments (`/api/v1/posts`)
- **POST** `/` - Create a new post (`CreatePostRequest` payload)
- **GET** `/{postId}` - Get a specific post
- **PUT** `/{postId}` - Update a post
- **DELETE** `/{postId}` - Delete a post
- **GET** `/feed` - Get user feed
- **POST** `/{postId}/like` - Toggle like on a post
- **POST** `/{postId}/comments` - Add a comment to a post (`CreateCommentRequest` payload)
- **GET** `/{postId}/comments` - Get comments of a post
- **POST** `/{postId}/track-view` - Track a view on a post

### Groups (`/api/v1/groups`)
- **POST** `/` - Create a new group (`CreateGroupRequest` payload)
- **POST** `/{groupId}/join` - Join a group
- **POST** `/{groupId}/members/{memberId}/promote` - Promote a group member to Admin
- **DELETE** `/{groupId}/members/{memberId}` - Remove a member from a group

### Group Posts (`/api/v1/groups/{groupId}/posts`)
- **POST** `/` - Create a post in a group (`CreatePostRequest` payload)
- **GET** `/` - Get posts from a group
- **DELETE** `/{postId}` - Delete a group post
- **POST** `/{postId}/like` - Toggle like on a group post

### Products & Marketplace (`/api/v1/products`)
- **POST** `/upload` - Upload a product image. **Params:** `file` (MultipartFile). **Returns:** `MediaUploadResponse`
- **GET** `/` - Get all products with pagination. **Params:** `page`, `size`, `category` (optional), `sortBy` (optional). **Returns:** `PageResponse<?>`
- **GET** `/search` - Search products by keyword. **Params:** `keyword`, `page`, `size`. **Returns:** `PageResponse<?>`
- **GET** `/{productId}` - Get product details by ID. **Returns:** `ProductResponse`
- **GET** `/seller/{sellerId}` - Get all products by a specific seller. **Params:** `page`, `size`. **Returns:** `PageResponse<?>`
- **GET** `/category/{category}` - Get products by category. **Params:** `page`, `size`. **Returns:** `PageResponse<?>`
- **GET** `/featured` - Get featured products. **Params:** `page`, `size`. **Returns:** `PageResponse<?>`
- **GET** `/trending` - Get trending products by rating. **Params:** `page`, `size`. **Returns:** `PageResponse<?>`
- **POST** `/` - Create a new product. **Payload:** `CreateProductRequest`. **Returns:** `ProductResponse`
- **PUT** `/{productId}` - Update product details. **Payload:** `CreateProductRequest`. **Returns:** `ProductResponse`
- **DELETE** `/{productId}` - Delete a product. **Returns:** `204 No Content`
- **POST** `/{productId}/favorite` - Add product to favorites. **Returns:** `{ "message": "..." }`
- **DELETE** `/{productId}/favorite` - Remove product from favorites. **Returns:** `{ "message": "..." }`
- **GET** `/categories` - Get all product categories. **Returns:** `List<?>`
- **GET** `/{productId}/complete` - Get complete product details with seller and reviews.
- **GET** `/{productId}/with-seller` - Get product details with seller information.
- **GET** `/{productId}/reviews` - Get product reviews with pagination. **Params:** `page`, `size`.
- **POST** `/{productId}/reviews` - Add a review to a product. **Payload:** `AddReviewRequest`.

### Sellers (`/api/v1/sellers`)
- **POST** `/` - Create a new seller profile (`CreateSellerRequest` payload)

### Conversations & Direct Messages (`/api/v1/conversations`)
- **POST** `/` - Create a conversation (`CreateConversationRequest` payload)
- **GET** `/` - Get all conversations for current user
- **GET** `/{conversationId}` - Get a specific conversation
- **DELETE** `/{conversationId}` - Delete a conversation
- **POST** `/{conversationId}/messages` - Send a message in a conversation (`SendMessageRequest` payload)
- **GET** `/{conversationId}/messages` - Get messages in a conversation
- **PUT** `/{conversationId}/read` - Mark conversation as read
- **POST** `/{conversationId}/read` - Mark conversation as read
- **DELETE** `/{conversationId}/messages/{messageId}` - Delete a message
- **GET** `/direct/{otherUserId}` - Get or create direct conversation
- **GET** `/direct` - Get or create direct conversation (via param)

### Messaging (`/api/v1/messages`)
- **GET** `/conversation/{conversationId}` - Get messages for a conversation
- **POST** `/conversation/{conversationId}/read` - Mark messages as read
- **POST** `/{messageId}/react` - React to a message

### Search (`/api/v1/search`)
- **GET** `/posts` - Search posts
- **GET** `/people` - Search people
- **GET** `/groups` - Search groups
- **GET** `/typeahead` - Typeahead search suggestions

### Education & Courses (`/api/education`)
**Videos:**
- **GET** `/videos` - Get all education videos. **Params:** `page`, `size`. **Returns:** `PageResponse<?>`
- **GET** `/videos/search` - Search videos by keyword. **Params:** `keyword`, `page`, `size`.
- **GET** `/videos/trending` - Get trending videos. **Params:** `page`, `size`.
- **GET** `/videos/top-rated` - Get top-rated videos. **Params:** `page`, `size`.
- **GET** `/videos/category/{category}` - Get videos by category. **Params:** `page`, `size`.
- **GET** `/videos/channel/{channel}` - Get videos by channel. **Params:** `page`, `size`.
- **GET** `/videos/categories` - Get all video categories.
- **GET** `/videos/channels` - Get all channels.

**Courses:**
- **GET** `/courses` - Get all courses. **Params:** `page`, `size`. **Returns:** `PageResponse<?>`
- **GET** `/courses/search` - Search courses by keyword. **Params:** `keyword`, `page`, `size`.
- **GET** `/courses/popular` - Get popular courses. **Params:** `page`, `size`.
- **GET** `/courses/top-rated` - Get top-rated courses. **Params:** `page`, `size`.
- **GET** `/courses/category/{category}` - Get courses by category. **Params:** `page`, `size`.
- **GET** `/courses/categories` - Get all course categories.
- **GET** `/courses/instructors` - Get all instructors.

**Enrollments:**
- **POST** `/courses/{courseId}/enroll` - Enroll user in a course.
- **GET** `/my-courses` - Get user's enrolled courses. **Params:** `page`, `size`.
- **GET** `/my-courses/in-progress` - Get user's in-progress courses. **Params:** `page`, `size`.
- **GET** `/my-courses/completed` - Get user's completed courses.
- **PUT** `/enrollments/{enrollmentId}/progress` - Update course progress. **Params:** `progress`, `completedLessons` (optional).
- **GET** `/courses/{courseId}/enrolled-status` - Check if user is enrolled. **Returns:** `{"enrolled": boolean}`

### App Security & Aliases (`/api/v1/app-security`)
- **POST** `/register` - Register a client application
- **GET** `/public-key` - Get server public key
- **GET** `/verify/{appId}` - Verify an app
- **POST** `/deactivate/{appId}` - Deactivate an app
- **POST** `/test-encryption` - Test end-to-end encryption

### System Health
- **GET** `/` - API Root mapping
- **GET** `/health` - Health check status

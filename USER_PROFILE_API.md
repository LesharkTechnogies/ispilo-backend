# User Profile + Profile Posts API

This guide documents the profile implementation for:
- Viewing a specific user's profile details
- Loading posts authored by a specific profile (for profile screen)
- Loading the authenticated user's own posts (`my posts`)
- Returning post counts and user stats
- Ensuring avatar URL saved in DB is returned in profile/post payloads

---

## 1) Get user profile details

### Endpoint
`GET /api/users/{userId}`

### Purpose
Use this when opening a profile (for example, **Leshar Technologies**) to get user details and counts.

### Response highlights
- `avatar` (DB-stored URL)
- `username`, `firstName`, `lastName`, `name`, `bio`, `location`, `town`
- `postCount`, `followersCount`, `followingCount`
- `isFollowing` (whether current authenticated user follows this profile)
- `isPublic` and optional `message` for restricted/private profile view

### Example response
```json
{
  "id": "user-uuid",
  "username": "leshar",
  "firstName": "Leshar",
  "lastName": "Technologies",
  "name": "Leshar Technologies",
  "avatar": "https://cdn.example.com/avatars/leshar.png",
  "town": "Nairobi",
  "bio": "Better ISPs",
  "location": "Kenya",
  "createdAt": "2026-04-27T09:15:10",
  "isPublic": true,
  "isFollowing": true,
  "postCount": 25,
  "followersCount": 120,
  "followingCount": 50
}
```

---

## 2) Get posts for a profile (when clicking a user's profile)

### Endpoint (User API style)
`GET /api/users/{userId}/posts?page=0&size=20`

### Endpoint (Posts API style, backward-compatible)
`GET /api/posts/user/{userId}?page=0&size=20`

### Purpose
Returns paginated posts authored by that user profile.

### Post response highlights
- `likesCount`, `commentsCount`, `viewCount`
- `likedByCurrentUser` (viewer-specific like state)
- nested author with `avatar`

### Example response
```json
{
  "content": [
    {
      "id": "post-uuid",
      "user": {
        "id": "user-uuid",
        "username": "leshar",
        "name": "Leshar Technologies",
        "avatar": "https://cdn.example.com/avatars/leshar.png"
      },
      "content": "Better ISPs",
      "description": "Better ISPs",
      "likesCount": 12,
      "commentsCount": 3,
      "likedByCurrentUser": true,
      "viewCount": 100,
      "createdAt": "2026-04-27T10:12:00",
      "updatedAt": "2026-04-27T10:12:00"
    }
  ],
  "number": 0,
  "size": 20,
  "totalElements": 25,
  "totalPages": 2,
  "last": false
}
```

---

## 3) Get my posts (authenticated user)

### Endpoint (User API style)
`GET /api/users/me/posts?page=0&size=20`

### Endpoint (Posts API style)
`GET /api/posts/me?page=0&size=20`

### Purpose
Lets the authenticated user request all posts they have ever posted (paginated).

---

## 4) Likes and comments behavior

- **Like toggle**: `POST /api/posts/{postId}/like`
  - Creates/removes like for current user
  - Updates `likesCount`
  - Returns updated post payload

- **Comment create**: `POST /api/posts/{postId}/comments`
  - Adds comment
  - Increments `commentsCount`
  - Sends notification to post owner (if commenter is different user)

---

## 5) Post count implementation

`postCount` in profile responses is computed from posts table:

- `postRepository.countByUserId(userId)`

This ensures profile stats match authored posts.

---

## 6) Avatar URL DB persistence and exposure

### Update avatar endpoint
`POST /api/users/me/avatar` (multipart form-data field: `avatar`)

### Behavior
1. Upload file to configured media storage
2. Store returned URL in `users.avatar`
3. Return updated user/profile payloads with that same DB URL

Avatar URL is now consistently present in:
- `UserResponse`
- `UserProfileResponse`
- `PostResponse.user.avatar`
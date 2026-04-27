# Group Integration Guide

This document outlines the API endpoints, data models, and integration flow for the **Groups** feature in Ispilo, designed for frontend developers to successfully implement the Group functionalities.

## 1. Overview

The Group feature allows users to create, join, manage, and interact within communities. Each group has its own dedicated feed (Group Posts). Groups can be public or private, and have Admins and Members.

---

## 2. Group Management APIs

### 2.1. Create a Group
**Endpoint:** `POST /api/v1/groups`
**Authentication:** Required (Bearer Token)
**Payload:**
```json
{
  "name": "Flutter Developers",
  "description": "A group for Flutter enthusiasts.",
  "privateGroup": false
}
```
**Response (200 OK):** Returns the created `GroupResponse` object.
**Error (409 Conflict):** Returned if a group with the exact same name already exists. The UI should prompt the user to choose a different name.

### 2.2. Join a Group
**Endpoint:** `POST /api/v1/groups/{groupId}/join`
**Authentication:** Required (Bearer Token)
**Description:** Adds the authenticated user to the group as a regular `MEMBER`.
**Response (200 OK):** Success message or empty 200 OK.

### 2.3. Leave / Remove Member
**Endpoint:** `DELETE /api/v1/groups/{groupId}/members/{memberId}`
**Authentication:** Required (Bearer Token)
**Description:** 
- An `ADMIN` can pass any `memberId` to remove that user from the group.
- A regular user can pass their own ID as `memberId` to leave the group.

### 2.4. Promote to Admin
**Endpoint:** `PUT /api/v1/groups/{groupId}/members/{memberId}/promote`
**Authentication:** Required (Bearer Token)
**Description:** Allows an existing `ADMIN` to promote another member to the `ADMIN` role.

---

## 3. Group Posts & Feed APIs

Group posts are strictly separated from the global user feed. 

### 3.1. Create a Group Post
**Endpoint:** `POST /api/v1/groups/{groupId}/posts`
**Authentication:** Required (Bearer Token) - User MUST be a member of the group.
**Payload:**
```json
{
  "actualContent": "Hello group members! Here's a cool update.",
  "mediaUrls": ["https://res.cloudinary.com/.../image.jpg"],
  "isAnonymous": false
}
```
**Features:**
- Supports anonymous posting. If `isAnonymous` is `true`, the author's identity is hidden from other members.
- Creating a post triggers a push notification to all other group members.

### 3.2. Get Group Feed (List Posts)
**Endpoint:** `GET /api/v1/groups/{groupId}/posts?page=0&size=20`
**Authentication:** Required (Bearer Token) - User MUST be a member of the group.
**Response:** Paginated list of `GroupPostResponse` objects.

### 3.3. Toggle Like on a Group Post
**Endpoint:** `POST /api/v1/groups/{groupId}/posts/{postId}/like`
**Authentication:** Required (Bearer Token)
**Description:** Toggles the like status for the authenticated user on a specific group post.
**Note on Data Integrity:** The backend implements pessimistic locking. Double-clicking or rapidly tapping the like button is safely handled by the server without causing 500 errors or duplicate likes.

### 3.4. Delete a Group Post
**Endpoint:** `DELETE /api/v1/groups/{groupId}/posts/{postId}`
**Authentication:** Required (Bearer Token)
**Permissions:**
- The **Author** of the post can delete it.
- Any **Group Admin** can delete it, allowing for community moderation.

---

## 4. Integration Best Practices & Error Handling

1. **Idempotency & Double Clicks:**
   - **Liking Posts:** Safely handle double-taps on the UI. The backend uses row-level locking to ensure like counts remain accurate even under heavy concurrent requests.
   - **Creating Groups:** The backend enforces unique group names. If a user double-clicks "Create", the second request will gracefully fail with a `409 Conflict`. Handle this by showing a toast or snackbar rather than crashing the app.

2. **Role-Based UI Rendering:**
   - When fetching group details or group posts, check the `role` or `isAdmin` flag in the response.
   - Show "Delete" buttons on other users' posts *only* if the current user is a Group Admin.
   - Show "Promote to Admin" buttons *only* if the current user is a Group Admin.

3. **Anonymous Posts:**
   - If a post has `anonymous: true`, ensure the UI completely masks the user's avatar and name, replacing it with a generic "Anonymous Member" placeholder.

4. **Push Notifications:**
   - The app will receive FCM payloads of type `GROUP_POST`. Use the attached `targetId` (the post ID) and `groupId` to deep-link the user directly into the specific group's feed.

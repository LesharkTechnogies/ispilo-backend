# Posts and Groups Integration Guide

This document outlines the architecture, flow, and integration details for **Posts** (global/user feed) and **Group Posts** (community posts within groups), as well as the **Search** and **Notification** systems connecting them.

---

## 1. Data Models and Separation of Concerns

We maintain strict separation between normal user posts and group posts to prevent overlapping logic and ensure that group privacy or anonymity rules don't accidentally affect the global feed.

### Global / User Posts
- **Entity:** `Post` (Table: `posts`)
- **Likes:** `PostLike` (Table: `post_likes`)
- **Controller:** `PostController` (`/api/v1/posts`)
- **Service:** `PostService` & `FeedService`
- **Use Case:** Posts that show up on a user's profile and on their followers' feeds.

### Group Posts
- **Entity:** `PostEntity` (Table: `group_posts`)
- **Likes:** `GroupPostLike` (Table: `group_post_likes`)
- **Controller:** `GroupPostController` (`/api/v1/groups/{groupId}/posts`)
- **Service:** `GroupPostService`
- **Use Case:** Posts created inside a specific `GroupEntity`. Features include anonymous posting. Only visible to group members (if private).

---

## 2. Notification Flow & Implementation

The backend is configured to push notifications out when a post is created, notifying the relevant audience. 

### Global Post Notifications
When a user creates a normal post (`PostService.createPost`):
1. The backend queries `UserFollowRepository` to get a list of all followers.
2. It loops through these users.
3. It passes the users to `NotificationService.sendPushNotifications()`.
4. **Client Receives:** A push payload of type `NEW_POST` with the new `postId`, allowing the client to navigate directly to the post.

### Group Post Notifications
When a user creates a group post (`GroupPostService.createGroupPost`):
1. The backend queries `GroupMemberRepository` to get all members of the group.
2. It removes the author from the notification list.
3. If the post is marked as `anonymous`, the notification title/body omits the author's name (e.g., "Someone posted in [Group Name]"). Otherwise, it displays "[Author Name] posted in [Group Name]".
4. **Client Receives:** A push payload of type `GROUP_POST` with the new `postId`, allowing the client to navigate to the group's feed.

### How Notifications Reach the Client
All notifications are routed through `NotificationService.sendPushNotification(User, title, body, type, targetId)`.
- It currently logs the intent and retrieves the user's `fcmToken` (Firebase Cloud Messaging Token).
- In production, this service uses the Firebase Admin SDK to push the notification asynchronously payload to iOS, Android, or Web clients.
- The client receives an FCM event containing the data payload `{ "type": "GROUP_POST", "targetId": "uuid..." }` and handles deep linking accordingly.

---

## 3. Creating Posts

### Creating a User Post
**Endpoint:** `POST /api/v1/posts`
**Payload:**
```json
{
  "content": "Hello world!",
  "mediaUrls": ["https://s3.../image.jpg"]
}
```

### Creating a Group Post
**Endpoint:** `POST /api/v1/groups/{groupId}/posts`
**Payload:**
```json
{
  "content": "Hello group members!",
  "mediaUrls": ["https://s3.../image.jpg"],
  "isAnonymous": false
}
```

---

## 4. Searching and Discovery (Like Facebook)

We have implemented a unified Search Controller (`SearchController`) that allows users to discover posts, people, and **groups**. 

### Group Search
Users can search for groups by name or description.
**Endpoint:** `GET /api/search/groups?q=programming`
**Response:**
Returns a paginated list of `GroupResponse` items matching the query. Users can then call `POST /api/v1/groups/{groupId}/join` to join a group they found.

### Typeahead / Omnibox Search
A typeahead search is available for the main search bar to provide instant autocomplete suggestions across Posts, Users, and Groups.
**Endpoint:** `GET /api/search/typeahead?q=java`
**Response:**
```json
{
  "query": "java",
  "suggestions": [
    { "type": "group", "text": "Java Developers" },
    { "type": "user", "text": "Java Expert" },
    { "type": "post", "text": "I love java programming" }
  ]
}
```
 have successfully implemented the requested product posting logic, including role-based access for Sellers, Users, and Admins.

Here is a summary of the updates made to the codebase:

1. Default Product Image URLs
In ProductService.createProduct and ProductService.updateProduct, if the frontend does not provide a mainImage or the images array is empty, the system automatically assigns a fallback URL (https://ispilo.com/default-product-image.png) so that the product image will always exist.
2. Role & Admin Infrastructure
Admin Support Added: I added an isAdmin boolean to the User entity (@Column(name = "is_admin") private Boolean isAdmin = false;).
Dynamic Authorization: Removed the generic @PreAuthorize("hasRole('SELLER')") checks from ProductController because it wasn't accurately distinguishing between an Admin bypassing the role and a Seller. Permissions are now securely handled inside the ProductService.
3. Seller & Admin Posting Flow
Sellers: A normal user creates a product using their own seller profile. The system ensures that sellers can only update or delete their own products. Attempting to modify someone else's post throws a BadRequestException.
Admins: An admin can bypass ownership checks completely. They can:
Update or delete any product on the platform.
Post on behalf of a Seller: I added a sellerId field to the CreateProductRequest. If an Admin passes a sellerId when creating a product, the product is posted under that specific seller's account.
4. Normal Users (View Only)
Normal users can view products through all the GET endpoints (/products/search, /products/trending, /products/{id}, etc.) but if they attempt to hit POST, PUT, or DELETE, the ProductService will reject them if they aren't registered as a Seller or an Admin.
The project compiles perfectly and the database mappings are strictly enforced. Admins now have total control over the marketplace!
I have successfully integrated Cloudinary and updated your environment configurations!

Here is what was accomplished:

.env and Database Configurations:

Saved your Cloudinary credentials and existing database configurations into the .env file at the root of the project.
Using environment variables enables different configurations for deployment environments without altering code.
Mapped these credentials inside application.yml so they are accessible to the Spring environment (e.g., ${CLOUDINARY_API_KEY:...}).
Added .env to .gitignore to prevent any sensitive API keys or database passwords from being accidentally committed to version control.
Cloudinary Implementation:

pom.xml: Added the cloudinary-http44 dependency.
CloudinaryConfig.java: Set up a @Bean returning a Cloudinary instance that safely retrieves the credentials using @Value properties from the environment.
CloudinaryController.java: Created the endpoint GET /api/v1/cloudinary that retrieves the configuration maps (cloudName, apiKey, apiSecret).
Flutter Integration: The Flutter app can now securely fetch these credentials directly by sending a GET request to /api/v1/cloudinary, keeping the keys dynamic and securely fetched from the backend on-demand rather than hardcoding them into the mobile app's repository.
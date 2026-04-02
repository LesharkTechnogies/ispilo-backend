# REST API Documentation

This document provides a summary of the available REST APIs.

## Sellers API

Base path: `/api/sellers`

### Create a Seller Profile

*   **Endpoint**: `POST /`
*   **Description**: Creates a seller profile for the currently authenticated user.
*   **Authentication**: Required (user must be logged in).
*   **Request Body**:
    ```json
    {
      "businessName": "My Awesome Shop",
      "businessDescription": "We sell the most awesome products in the world.",
      "businessAddress": "123 Awesome Street, Awesome City"
    }
    ```
*   **Response**: `201 CREATED` with the created seller profile.

## Products API

Base path: `/api/products`

### Upload a Product Image

*   **Endpoint**: `POST /upload`
*   **Description**: Uploads an image for a product.
*   **Authentication**: Required (user must have `SELLER` role).
*   **Request Body**: `multipart/form-data` with the `file` field containing the image.
*   **Response**: `200 OK` with the media upload details.

### Get All Products

*   **Endpoint**: `GET /`
*   **Description**: Retrieves a paginated list of all products.
*   **Query Parameters**: `page`, `size`, `category`, `sortBy`.
*   **Response**: `200 OK` with a paginated list of products.

### Search Products

*   **Endpoint**: `GET /search`
*   **Description**: Searches for products by a keyword.
*   **Query Parameters**: `keyword`, `page`, `size`.
*   **Response**: `200 OK` with a paginated list of matching products.

### Get Product by ID

*   **Endpoint**: `GET /{productId}`
*   **Description**: Retrieves a single product by its ID.
*   **Response**: `200 OK` with the product details.

### Get Products by Seller

*   **Endpoint**: `GET /seller/{sellerId}`
*   **Description**: Retrieves all products for a specific seller.
*   **Response**: `200 OK` with a paginated list of products.

### Create a Product

*   **Endpoint**: `POST /`
*   **Description**: Creates a new product.
*   **Authentication**: Required (user must have `SELLER` role).
*   **Request Body**: `CreateProductRequest` object.
*   **Response**: `201 CREATED` with the created product.

### Update a Product

*   **Endpoint**: `PUT /{productId}`
*   **Description**: Updates an existing product.
*   **Authentication**: Required (user must have `SELLER` role and own the product).
*   **Request Body**: `CreateProductRequest` object.
*   **Response**: `200 OK` with the updated product.

### Delete a Product

*   **Endpoint**: `DELETE /{productId}`
*   **Description**: Deletes a product.
*   **Authentication**: Required (user must have `SELLER` role and own the product).
*   **Response**: `204 No Content`.

## Users API

Base path: `/api/users`

### Get User Profile by ID

*   **Endpoint**: `GET /{userId}`
*   **Description**: Retrieves a user's profile. If the profile is public, it returns the user's details. If the profile is private, it returns limited information.
*   **Response**: `200 OK` with the user's profile details.

### Get Current User

*   **Endpoint**: `GET /me`
*   **Description**: Retrieves the details of the currently authenticated user.
*   **Authentication**: Required.
*   **Response**: `200 OK` with the user's details.

### Update Profile

*   **Endpoint**: `PUT /me`
*   **Description**: Updates the profile of the currently authenticated user.
*   **Authentication**: Required.
*   **Request Body**:
    ```json
    {
      "name": "New Name",
      "bio": "New bio.",
      "location": "New Location"
    }
    ```
*   **Response**: `200 OK` with the updated user details.

### Update Avatar

*   **Endpoint**: `POST /me/avatar`
*   **Description**: Updates the avatar of the currently authenticated user.
*   **Authentication**: Required.
*   **Request Body**: `multipart/form-data` with the `avatar` field containing the image.
*   **Response**: `200 OK` with the updated user details.

### Follow/Unfollow a User

*   **Endpoint**: `POST /{userId}/follow`
*   **Description**: Toggles the follow status of a user.
*   **Authentication**: Required.
*   **Response**: `200 OK`.

### Delete Account

*   **Endpoint**: `DELETE /me/account`
*   **Description**: Deletes the account of the currently authenticated user.
*   **Authentication**: Required.
*   **Response**: `200 OK`.

## Conversations API

Base path: `/api/conversations`

### Create a Conversation

*   **Endpoint**: `POST /`
*   **Description**: Creates a new conversation (private or group).
*   **Authentication**: Required.
*   **Request Body**:
    ```json
    {
      "type": "PRIVATE",
      "participantIds": ["user_id_1", "user_id_2"]
    }
    ```
*   **Response**: `201 CREATED` with the created conversation.

### Get User Conversations

*   **Endpoint**: `GET /`
*   **Description**: Retrieves all conversations for the currently authenticated user.
*   **Authentication**: Required.
*   **Response**: `200 OK` with a paginated list of conversations.

### Get a Conversation

*   **Endpoint**: `GET /{conversationId}`
*   **Description**: Retrieves a specific conversation by its ID.
*   **Authentication**: Required.
*   **Response**: `200 OK` with the conversation details.

### Get Conversation Messages

*   **Endpoint**: `GET /{conversationId}/messages`
*   **Description**: Retrieves messages in a conversation.
*   **Authentication**: Required.
*   **Response**: `200 OK` with a paginated list of messages.

### Delete a Conversation

*   **Endpoint**: `DELETE /{conversationId}`
*   **Description**: Deletes a conversation.
*   **Authentication**: Required.
*   **Response**: `204 No Content`.

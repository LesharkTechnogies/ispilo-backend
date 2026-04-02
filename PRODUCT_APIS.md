# Product API Endpoints

This document outlines the API endpoints for managing products in the application.

## Base URL

The base URL for all product endpoints is `/api/v1/products`. The API is versioned, and also accessible via `/api/products` and `/api/v2/products`.

---

## Get All Products

-   **Endpoint:** `GET /`
-   **Description:** Retrieves a paginated list of all products.
-   **Query Parameters:**
    -   `page` (optional, default: 0): The page number to retrieve.
    -   `size` (optional, default: 20): The number of products per page.
    -   `category` (optional): Filters products by category.
    -   `sortBy` (optional, e.g., "asc" or "desc" for creation date): Sorts products.
-   **Success Response (200 OK):** `PageResponse<ProductResponse>`

---

## Search Products

-   **Endpoint:** `GET /search`
-   **Description:** Searches for products by a keyword.
-   **Query Parameters:**
    -   `keyword`: The search term.
    -   `page` (optional, default: 0): The page number.
    -   `size` (optional, default: 20): The number of products per page.
-   **Success Response (200 OK):** `PageResponse<ProductResponse>`

---

## Get Product by ID

-   **Endpoint:** `GET /{productId}`
-   **Description:** Retrieves details of a specific product.
-   **Path Parameters:**
    -   `productId`: The ID of the product.
-   **Success Response (200 OK):** `ProductResponse`

---

## Get Products by Seller

-   **Endpoint:** `GET /seller/{sellerId}`
-   **Description:** Retrieves all products listed by a specific seller.
-   **Path Parameters:**
    -   `sellerId`: The ID of the seller.
-   **Query Parameters:**
    -   `page` (optional, default: 0): The page number.
    -   `size` (optional, default: 20): The number of products per page.
-   **Success Response (200 OK):** `PageResponse<ProductResponse>`

---

## Get Products by Category

-   **Endpoint:** `GET /category/{category}`
-   **Description:** Retrieves products belonging to a specific category.
-   **Path Parameters:**
    -   `category`: The name of the category.
-   **Query Parameters:**
    -   `page` (optional, default: 0): The page number.
    -   `size` (optional, default: 20): The number of products per page.
-   **Success Response (200 OK):** `PageResponse<ProductResponse>`

---

## Get Featured Products

-   **Endpoint:** `GET /featured`
-   **Description:** Retrieves a list of featured products.
-   **Query Parameters:**
    -   `page` (optional, default: 0): The page number.
    -   `size` (optional, default: 10): The number of products per page.
-   **Success Response (200 OK):** `PageResponse<ProductResponse>`

---

## Get Trending Products

-   **Endpoint:** `GET /trending`
-   **Description:** Retrieves a list of trending products, sorted by rating.
-   **Query Parameters:**
    -   `page` (optional, default: 0): The page number.
    -   `size` (optional, default: 10): The number of products per page.
-   **Success Response (200 OK):** `PageResponse<ProductResponse>`

---

## Create Product

-   **Endpoint:** `POST /`
-   **Description:** Creates a new product.
-   **Authentication:** `SELLER` role required.
-   **Request Body:** `CreateProductRequest`
-   **Success Response (201 Created):** `ProductResponse`

---

## Update Product

-   **Endpoint:** `PUT /{productId}`
-   **Description:** Updates an existing product.
-   **Authentication:** `SELLER` role required (and must be the owner of the product).
-   **Path Parameters:**
    -   `productId`: The ID of the product to update.
-   **Request Body:** `CreateProductRequest`
-   **Success Response (200 OK):** `ProductResponse`

---

## Delete Product

-   **Endpoint:** `DELETE /{productId}`
-   **Description:** Deletes a product.
-   **Authentication:** `SELLER` role required (and must be the owner of the product).
-   **Path Parameters:**
    -   `productId`: The ID of the product to delete.
-   **Success Response (204 No Content):**

---

## Add to Favorites

-   **Endpoint:** `POST /{productId}/favorite`
-   **Description:** Adds a product to the user's favorites.
-   **Authentication:** `USER` role required.
-   **Path Parameters:**
    -   `productId`: The ID of the product.
-   **Success Response (200 OK):** `MessageResponse`

---

## Remove from Favorites

-   **Endpoint:** `DELETE /{productId}/favorite`
-   **Description:** Removes a product from the user's favorites.
-   **Authentication:** `USER` role required.
-   **Path Parameters:**
    -   `productId`: The ID of the product.
-   **Success Response (200 OK):** `MessageResponse`

---

## Get All Categories

-   **Endpoint:** `GET /categories`
-   **Description:** Retrieves a list of all product categories.
-   **Success Response (200 OK):** A list of category strings.

---

## Get Complete Product Details

-   **Endpoint:** `GET /{productId}/complete`
-   **Description:** Retrieves product details along with seller information and reviews.
-   **Path Parameters:**
    -   `productId`: The ID of the product.
-   **Success Response (200 OK):** A response object containing the product, seller, and reviews.

---

## Get Product with Seller

-   **Endpoint:** `GET /{productId}/with-seller`
-   **Description:** Retrieves product details along with seller information.
-   **Path Parameters:**
    -   `productId`: The ID of the product.
-   **Success Response (200 OK):** A response object containing the product and seller.

---

## Get Product Reviews

-   **Endpoint:** `GET /{productId}/reviews`
-   **Description:** Retrieves paginated reviews for a product.
-   **Path Parameters:**
    -   `productId`: The ID of the product.
-   **Query Parameters:**
    -   `page` (optional, default: 0): The page number.
    -   `size` (optional, default: 20): The number of reviews per page.
-   **Success Response (200 OK):** `PageResponse<ReviewResponse>`

---

## Add Product Review

-   **Endpoint:** `POST /{productId}/reviews`
-   **Description:** Adds a review to a product.
-   **Authentication:** `USER` role required.
-   **Path Parameters:**
    -   `productId`: The ID of the product.
-   **Request Body:** `AddReviewRequest`
-   **Success Response (201 Created):** `ReviewResponse`

---

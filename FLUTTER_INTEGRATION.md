# Flutter Frontend Integration Guide

This guide provides instructions on how to interact with the backend API from your Flutter application, specifically for features related to sellers and products.

## 1. Becoming a Seller

To become a seller, the authenticated user needs to provide their business details.

*   **Endpoint**: `POST /api/sellers`
*   **Authentication**: Bearer Token with user's JWT.
*   **Request Body**:
    ```json
    {
      "businessName": "My Awesome Shop",
      "businessDescription": "We sell the most awesome products in the world.",
      "businessAddress": "123 Awesome Street, Awesome City"
    }
    ```
*   **Response**: The server will create a `Seller` profile linked to the user and grant the `SELLER` role to the user. The response will contain the details of the created seller profile.

## 2. Uploading a Product Image

Before creating a product, you should upload the product images to get their URLs.

*   **Endpoint**: `POST /api/products/upload`
*   **Authentication**: Bearer Token with seller's JWT. The user must have the `SELLER` role.
*   **Request Body**: `multipart/form-data` with the image file. The form field name for the file should be `file`.
*   **Response**:
    ```json
    {
      "mediaUrl": "http://your-server.com/media/products/user_id/uuid.jpg",
      "mediaType": "products",
      "fileName": "original_image_name.jpg",
      "fileSize": 123456,
      "uploadedAt": "2026-04-03T10:00:00.000Z"
    }
    ```
    The `mediaUrl` is the URL of the uploaded image. You will use this URL when creating a product.

### Example using Dio in Flutter:

```dart
import 'package:dio/dio.dart';

Future<String?> uploadProductImage(String filePath, String token) async {
  try {
    Dio dio = Dio();
    dio.options.headers["Authorization"] = "Bearer $token";

    String fileName = filePath.split('/').last;
    FormData formData = FormData.fromMap({
      "file": await MultipartFile.fromFile(filePath, filename: fileName),
    });

    Response response = await dio.post(
      "http://your-server.com/api/products/upload",
      data: formData,
    );

    if (response.statusCode == 200) {
      return response.data["mediaUrl"];
    }
  } catch (e) {
    print("Error uploading image: $e");
  }
  return null;
}
```

## 3. Creating a Product

Once you have the URLs for the product images, you can create a product.

*   **Endpoint**: `POST /api/products`
*   **Authentication**: Bearer Token with seller's JWT.
*   **Request Body**:
    ```json
    {
      "title": "My Awesome Product",
      "description": "This is a detailed description of my awesome product.",
      "price": 99.99,
      "stockQuantity": 100,
      "mainImage": "http://your-server.com/media/products/user_id/main_image_uuid.jpg",
      "images": [
        "http://your-server.com/media/products/user_id/image1_uuid.jpg",
        "http://your-server.com/media/products/user_id/image2_uuid.jpg"
      ],
      "category": "Electronics",
      "condition": "New",
      "location": "Awesome City"
    }
    ```
    *   `mainImage` and `images` should contain the URLs obtained from the image upload step.
*   **Response**: The response will contain the details of the created product.

This covers the main flow for sellers to create products. You can apply the same logic for updating products using the `PUT /api/products/{productId}` endpoint.

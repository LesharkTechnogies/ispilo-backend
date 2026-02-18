import 'package:ispilo_main/core/services/api_service.dart';
import 'product_model.dart';

/// Repository for product marketplace API calls
class ProductRepository {
  static const String _baseEndpoint = '/products';

  /// Get all products with pagination
  static Future<List<ProductModel>> getProducts({
    int page = 0,
    int size = 20,
    String? category,
  }) async {
    try {
      String endpoint = '$_baseEndpoint?page=$page&size=$size';
      if (category != null && category.isNotEmpty && category != 'All Categories') {
        endpoint = '$_baseEndpoint/category/$category?page=$page&size=$size';
      }

      final response = await ApiService.get(endpoint);
      final List<dynamic> content = response['content'] as List? ?? [];
      return content
          .map((json) => ProductModel.fromJson(json as Map<String, dynamic>))
          .toList();
    } catch (e) {
      throw Exception('Failed to fetch products: $e');
    }
  }

  /// Search products by keyword
  static Future<List<ProductModel>> searchProducts(
    String keyword, {
    int page = 0,
    int size = 20,
  }) async {
    try {
      final response = await ApiService.get(
        '$_baseEndpoint/search?keyword=$keyword&page=$page&size=$size',
      );
      final List<dynamic> content = response['content'] as List? ?? [];
      return content
          .map((json) => ProductModel.fromJson(json as Map<String, dynamic>))
          .toList();
    } catch (e) {
      throw Exception('Failed to search products: $e');
    }
  }

  /// Get product by ID
  static Future<ProductModel> getProductById(String productId) async {
    try {
      final response = await ApiService.get('$_baseEndpoint/$productId');
      return ProductModel.fromJson(response as Map<String, dynamic>);
    } catch (e) {
      throw Exception('Failed to fetch product: $e');
    }
  }

  /// Get products by seller
  static Future<List<ProductModel>> getProductsBySeller(
    String sellerId, {
    int page = 0,
    int size = 20,
  }) async {
    try {
      final response = await ApiService.get(
        '$_baseEndpoint/seller/$sellerId?page=$page&size=$size',
      );
      final List<dynamic> content = response['content'] as List? ?? [];
      return content
          .map((json) => ProductModel.fromJson(json as Map<String, dynamic>))
          .toList();
    } catch (e) {
      throw Exception('Failed to fetch seller products: $e');
    }
  }

  /// Get featured products
  static Future<List<ProductModel>> getFeaturedProducts({
    int page = 0,
    int size = 10,
  }) async {
    try {
      final response = await ApiService.get(
        '$_baseEndpoint/featured?page=$page&size=$size',
      );
      final List<dynamic> content = response['content'] as List? ?? [];
      return content
          .map((json) => ProductModel.fromJson(json as Map<String, dynamic>))
          .toList();
    } catch (e) {
      throw Exception('Failed to fetch featured products: $e');
    }
  }

  /// Get trending products
  static Future<List<ProductModel>> getTrendingProducts({
    int page = 0,
    int size = 10,
  }) async {
    try {
      final response = await ApiService.get(
        '$_baseEndpoint/trending?page=$page&size=$size',
      );
      final List<dynamic> content = response['content'] as List? ?? [];
      return content
          .map((json) => ProductModel.fromJson(json as Map<String, dynamic>))
          .toList();
    } catch (e) {
      throw Exception('Failed to fetch trending products: $e');
    }
  }

  /// Get all product categories
  static Future<List<String>> getCategories() async {
    try {
      final response = await ApiService.get('$_baseEndpoint/categories');
      if (response is List) {
        return response.cast<String>();
      }
      return ['All Categories', 'Hardware', 'Software', 'Services', 'Tools'];
    } catch (e) {
      return ['All Categories', 'Hardware', 'Software', 'Services', 'Tools'];
    }
  }

  /// Create a new product
  static Future<ProductModel> createProduct({
    required String title,
    required String description,
    required double price,
    required String category,
    String? mainImage,
    List<String>? images,
    String? condition,
    String? location,
    int? stockQuantity,
  }) async {
    try {
      final payload = {
        'title': title,
        'description': description,
        'price': price,
        'category': category,
        'mainImage': mainImage,
        'images': images ?? [],
        'condition': condition ?? 'New',
        'location': location,
        'stockQuantity': stockQuantity ?? 0,
      };

      final response = await ApiService.post(_baseEndpoint, payload);
      return ProductModel.fromJson(response as Map<String, dynamic>);
    } catch (e) {
      throw Exception('Failed to create product: $e');
    }
  }

  /// Update product
  static Future<ProductModel> updateProduct({
    required String productId,
    required String title,
    required String description,
    required double price,
    required String category,
    String? mainImage,
    List<String>? images,
    String? condition,
    String? location,
    int? stockQuantity,
  }) async {
    try {
      final payload = {
        'title': title,
        'description': description,
        'price': price,
        'category': category,
        'mainImage': mainImage,
        'images': images,
        'condition': condition,
        'location': location,
        'stockQuantity': stockQuantity,
      };

      final response = await ApiService.put('$_baseEndpoint/$productId', payload);
      return ProductModel.fromJson(response as Map<String, dynamic>);
    } catch (e) {
      throw Exception('Failed to update product: $e');
    }
  }

  /// Delete product
  static Future<void> deleteProduct(String productId) async {
    try {
      await ApiService.delete('$_baseEndpoint/$productId');
    } catch (e) {
      throw Exception('Failed to delete product: $e');
    }
  }

  /// Add product to favorites
  static Future<void> addToFavorites(String productId) async {
    try {
      await ApiService.post('$_baseEndpoint/$productId/favorite', {});
    } catch (e) {
      throw Exception('Failed to add to favorites: $e');
    }
  }

  /// Remove product from favorites
  static Future<void> removeFromFavorites(String productId) async {
    try {
      await ApiService.delete('$_baseEndpoint/$productId/favorite');
    } catch (e) {
      throw Exception('Failed to remove from favorites: $e');
    }
  }

  /// Get product reviews
  static Future<List<Map<String, dynamic>>> getProductReviews(
    String productId, {
    int page = 0,
    int size = 20,
  }) async {
    try {
      final response = await ApiService.get(
        '$_baseEndpoint/$productId/reviews?page=$page&size=$size',
      );

      // Handle paginated response
      if (response is Map && response.containsKey('content')) {
        final List<dynamic> content = response['content'] as List? ?? [];
        return content.cast<Map<String, dynamic>>();
      }

      // Handle direct list response
      if (response is List) {
        return response.cast<Map<String, dynamic>>();
      }

      return [];
    } catch (e) {
      throw Exception('Failed to fetch product reviews: $e');
    }
  }

  /// Add product review to Java backend
  static Future<Map<String, dynamic>> addProductReview(
    String productId, {
    required int rating,
    required String comment,
    String? title,
  }) async {
    try {
      final payload = {
        'rating': rating,
        'comment': comment,
        if (title != null) 'title': title,
      };

      final response = await ApiService.post(
        '$_baseEndpoint/$productId/reviews',
        payload,
      );

      return response as Map<String, dynamic>;
    } catch (e) {
      throw Exception('Failed to add review: $e');
    }
  }

  /// Get seller details with product from Java backend
  static Future<Map<String, dynamic>> getProductWithSeller(String productId) async {
    try {
      final response = await ApiService.get(
        '$_baseEndpoint/$productId/with-seller',
      );
      return response as Map<String, dynamic>;
    } catch (e) {
      throw Exception('Failed to fetch product with seller: $e');
    }
  }

  /// Get complete product details (product + seller + reviews + ratings) from Java backend
  static Future<Map<String, dynamic>> getCompleteProductDetails(
    String productId,
  ) async {
    try {
      final response = await ApiService.get(
        '$_baseEndpoint/$productId/complete',
      );
      return response as Map<String, dynamic>;
    } catch (e) {
      throw Exception('Failed to fetch complete product details: $e');
    }
  }
}

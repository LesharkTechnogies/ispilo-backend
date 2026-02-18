/// Product model for marketplace
class ProductModel {
  final String id;
  final String title;
  final String description;
  final double price;
  final String mainImage;
  final List<String> images;
  final String category;
  final String condition;
  final double rating;
  final int reviewCount;
  final String location;
  final int stockQuantity;
  final bool isAvailable;
  final SellerModel seller;
  final DateTime createdAt;

  ProductModel({
    required this.id,
    required this.title,
    required this.description,
    required this.price,
    required this.mainImage,
    required this.images,
    required this.category,
    required this.condition,
    required this.rating,
    required this.reviewCount,
    required this.location,
    required this.stockQuantity,
    required this.isAvailable,
    required this.seller,
    required this.createdAt,
  });

  /// Create from JSON API response
  factory ProductModel.fromJson(Map<String, dynamic> json) {
    return ProductModel(
      id: json['id'] as String,
      title: json['title'] as String,
      description: json['description'] as String? ?? '',
      price: (json['price'] as num).toDouble(),
      mainImage: json['mainImage'] as String? ?? '',
      images: List<String>.from(json['images'] as List? ?? []),
      category: json['category'] as String? ?? '',
      condition: json['condition'] as String? ?? 'New',
      rating: (json['rating'] as num?)?.toDouble() ?? 4.5,
      reviewCount: json['reviewCount'] as int? ?? 0,
      location: json['location'] as String? ?? '',
      stockQuantity: json['stockQuantity'] as int? ?? 0,
      isAvailable: json['isAvailable'] as bool? ?? true,
      seller: SellerModel.fromJson(json['seller'] as Map<String, dynamic>? ?? {}),
      createdAt: json['createdAt'] != null
          ? DateTime.parse(json['createdAt'] as String)
          : DateTime.now(),
    );
  }

  /// Convert to JSON for sending to API
  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'title': title,
      'description': description,
      'price': price,
      'mainImage': mainImage,
      'images': images,
      'category': category,
      'condition': condition,
      'rating': rating,
      'reviewCount': reviewCount,
      'location': location,
      'stockQuantity': stockQuantity,
      'isAvailable': isAvailable,
      'seller': seller.toJson(),
      'createdAt': createdAt.toIso8601String(),
    };
  }
}

/// Seller model
class SellerModel {
  final String id;
  final String businessName;
  final String? businessLogo;
  final bool isVerified;

  SellerModel({
    required this.id,
    required this.businessName,
    this.businessLogo,
    required this.isVerified,
  });

  factory SellerModel.fromJson(Map<String, dynamic> json) {
    return SellerModel(
      id: json['id'] as String? ?? '',
      businessName: json['businessName'] as String? ?? '',
      businessLogo: json['businessLogo'] as String?,
      isVerified: json['isVerified'] as bool? ?? false,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'businessName': businessName,
      'businessLogo': businessLogo,
      'isVerified': isVerified,
    };
  }
}

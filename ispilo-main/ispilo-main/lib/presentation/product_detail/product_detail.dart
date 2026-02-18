import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:url_launcher/url_launcher.dart';
import '../../core/services/seller_service.dart';
import '../../model/repository/product_repository.dart';
import '../../data/marketplace_data.dart';

import '../../core/app_export.dart';
import '../../core/models/seller.dart';
import './widgets/action_buttons_bar.dart';
import './widgets/expandable_description.dart';
import './widgets/product_image_gallery.dart';
import './widgets/product_info_section.dart';
import './widgets/related_products_carousel.dart';
import './widgets/seller_profile_section.dart';
import './widgets/shipping_policy_section.dart';
import './widgets/specifications_section.dart';

class ProductDetail extends StatefulWidget {
  const ProductDetail({super.key});

  @override
  State<ProductDetail> createState() => _ProductDetailState();
}

class _ProductDetailState extends State<ProductDetail> {
  bool _isSaved = false;
  bool _loading = false;
  String? _hudError;
  VoidCallback? _hudRetryAction;
  Map<String, dynamic>? _productData;
  List<Map<String, dynamic>> _relatedProducts = [];
  String? _productId;

  void _setLoading(bool value) {
    if (!mounted) return;
    setState(() {
      _loading = value;
      if (value) _hudError = null;
    });
  }

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _loadProductData();
    });
  }

  /// Load product data from API based on product ID
  Future<void> _loadProductData() async {
    _setLoading(true);
    try {
      // Get product ID from route arguments
      final args = ModalRoute.of(context)?.settings.arguments as Map<String, dynamic>?;
      _productId = args?['productId'] as String?;

      if (_productId == null || _productId!.isEmpty) {
        _setError('No product selected');
        return;
      }

      // Fetch complete product details from API
      final completeData = await ProductRepository.getCompleteProductDetails(_productId!);

      if (!mounted) return;

      setState(() {
        _productData = completeData;
        _isSaved = false;
      });

      // Load related/other products from same seller
      _loadRelatedProducts();
    } catch (e) {
      debugPrint('Error loading product: $e');
      if (mounted) {
        _setError('Failed to load product: $e');
      }
    } finally {
      _setLoading(false);
    }
  }

  /// Load related products from the same seller
  Future<void> _loadRelatedProducts() async {
    try {
      if (_productData == null) return;

      final seller = _productData!['seller'] as Map<String, dynamic>?;
      final sellerId = seller?['id'] as String?;

      if (sellerId == null) return;

      final products = await ProductRepository.getProductsBySeller(sellerId, size: 4);

      if (!mounted) return;

      setState(() {
        // Convert ProductModel to Map for compatibility with existing code
        _relatedProducts = products
            .where((p) => p.id != _productId) // Exclude current product
            .take(4)
            .map((p) => {
              'id': p.id,
              'title': p.name,
              'price': '\$${p.price.toStringAsFixed(2)}',
              'image': p.imageUrl,
              'seller': seller,
            })
            .toList();
      });
    } catch (e) {
      debugPrint('Error loading related products: $e');
    }
  }

  void _setError(String error) {
    setState(() {
      _hudError = error;
      _hudRetryAction = _loadProductData;
    });
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;

    // Show loading or error state
    if (_loading) {
      return Scaffold(
        backgroundColor: colorScheme.surface,
        appBar: _buildAppBar(context, theme),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              CircularProgressIndicator(color: colorScheme.primary),
              const SizedBox(height: 16),
              const Text('Loading product details...'),
            ],
          ),
        ),
      );
    }

    if (_hudError != null || _productData == null) {
      return Scaffold(
        backgroundColor: colorScheme.surface,
        appBar: _buildAppBar(context, theme),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(Icons.error_outline, size: 48, color: colorScheme.error),
              const SizedBox(height: 16),
              Text(_hudError ?? 'Failed to load product'),
              const SizedBox(height: 24),
              ElevatedButton(
                onPressed: _loadProductData,
                child: const Text('Retry'),
              ),
            ],
          ),
        ),
      );
    }

    // Extract data safely
    final product = _productData!;
    final seller = product['seller'] as Map<String, dynamic>? ?? {};
    final specifications = product['specifications'] as Map<String, dynamic>? ?? {};
    final shipping = product['shipping'] as Map<String, dynamic>? ?? {};
    final images = (product['images'] as List?)?.cast<String>() ?? ['https://via.placeholder.com/400x400?text=No+Image'];

    return Scaffold(
      backgroundColor: colorScheme.surface,
      appBar: _buildAppBar(context, theme),
      body: Stack(
        children: [
          Column(
            children: [
              Expanded(
                child: SingleChildScrollView(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      // Product Image Gallery
                      ProductImageGallery(
                        images: images,
                        productTitle: product['name'] as String? ?? product['title'] as String? ?? 'Product',
                      ),

                      const SizedBox(height: 16),

                      // Product Info Section
                      ProductInfoSection(
                        title: product['name'] as String? ?? product['title'] as String? ?? 'Product',
                        price: product['price'] != null
                            ? '\$${(product['price'] as num).toStringAsFixed(2)}'
                            : product['price'] as String? ?? 'Contact for price',
                        condition: product['condition'] as String? ?? 'New',
                        rating: ((product['rating'] as num?) ?? 4.0).toDouble(),
                        reviewCount: product['reviewCount'] as int? ?? 0,
                      ),

                      const SizedBox(height: 16),

                      // Seller Profile Section
                      if (seller.isNotEmpty)
                        SellerProfileSection(
                          sellerName: seller['name'] as String? ?? 'Unknown Seller',
                          sellerAvatar: seller['avatar'] as String? ?? '',
                          isVerified: seller['isVerified'] as bool? ?? false,
                          sellerRating: ((seller['rating'] as num?) ?? 4.0).toDouble(),
                          totalSales: seller['totalSales'] as int? ?? 0,
                          onViewProfile: () => _viewSellerProfile(seller['id'] as String? ?? ''),
                        ),

                      const SizedBox(height: 24),

                      // Product Description
                      if (product['description'] != null)
                        ExpandableDescription(
                          description: product['description'] as String,
                        ),

                      const SizedBox(height: 24),

                      // Technical Specifications
                      if (specifications.isNotEmpty)
                        SpecificationsSection(
                          specifications: specifications.cast<String, String>(),
                        ),

                      const SizedBox(height: 24),

                      // Shipping & Return Policy
                      if (shipping.isNotEmpty)
                        ShippingPolicySection(
                          shippingInfo: shipping['info'] as String? ?? 'Standard shipping available',
                          returnPolicy: shipping['returnPolicy'] as String? ?? 'Contact seller for returns',
                          estimatedDelivery: shipping['estimatedDelivery'] as String? ?? '5-7 business days',
                          shippingCost: shipping['cost'] as String? ?? 'Contact seller',
                        ),

                      const SizedBox(height: 24),

                      // Related Products
                      if (_relatedProducts.isNotEmpty)
                        RelatedProductsCarousel(
                          relatedProducts: _relatedProducts,
                          onProductTap: _navigateToProduct,
                        ),

                      const SizedBox(height: 100),
                    ],
                  ),
                ),
              ),

              // Action Buttons Bar
              ActionButtonsBar(
                onContactSeller: _contactSeller,
                onMakeOffer: _makeOffer,
                onSaveProduct: _toggleSaveProduct,
                isSaved: _isSaved,
              ),
            ],
          ),
        ],
      ),
    );
  }
              ),
            ),
        ],
      ),
    );
  }

  PreferredSizeWidget _buildAppBar(BuildContext context, ThemeData theme) {
    final colorScheme = theme.colorScheme;

    return AppBar(
      backgroundColor: colorScheme.surface,
      elevation: 0,
      leading: IconButton(
        onPressed: () => Navigator.pop(context),
        icon: CustomIconWidget(
          iconName: 'arrow_back_ios',
          color: colorScheme.onSurface,
          size: 24,
        ),
      ),
      actions: [
        IconButton(
          onPressed: _shareProduct,
          icon: CustomIconWidget(
            iconName: 'share',
            color: colorScheme.onSurface,
            size: 24,
          ),
        ),
        IconButton(
          onPressed: _toggleSaveProduct,
          icon: CustomIconWidget(
            iconName: _isSaved ? 'bookmark' : 'bookmark_border',
            color: _isSaved ? colorScheme.primary : colorScheme.onSurface,
            size: 24,
          ),
        ),
      ],
    );
  }

  void _viewSellerProfile() {
    HapticFeedback.lightImpact();
    // Navigate to seller profile
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text('Opening seller profile...'),
        duration: const Duration(seconds: 2),
      ),
    );
  }

  void _contactSeller() {
    HapticFeedback.lightImpact();
    // Open messaging interface or phone dialer
    showModalBottomSheet(
      context: context,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (context) => _buildContactOptions(context),
    );
  }

  Widget _buildContactOptions(BuildContext context) {
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;

    return Container(
      padding: const EdgeInsets.all(24),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(
            width: 40,
            height: 4,
            decoration: BoxDecoration(
              color: colorScheme.onSurface.withValues(alpha: 0.3),
              borderRadius: BorderRadius.circular(2),
            ),
          ),
          const SizedBox(height: 24),
          Text(
            'Contact Seller',
            style: GoogleFonts.inter(
              fontSize: 18,
              fontWeight: FontWeight.w600,
              color: colorScheme.onSurface,
            ),
          ),
          const SizedBox(height: 24),
          ListTile(
            leading: CustomIconWidget(
              iconName: 'chat_bubble_outline',
              color: colorScheme.primary,
              size: 24,
            ),
            title: Text(
              'Send Message',
              style: GoogleFonts.inter(
                fontSize: 16,
                fontWeight: FontWeight.w500,
                color: colorScheme.onSurface,
              ),
            ),
            subtitle: Text(
              'Chat with NetworkPro Solutions',
              style: GoogleFonts.inter(
                fontSize: 14,
                color: colorScheme.onSurface.withValues(alpha: 0.6),
              ),
            ),
            onTap: () async {
              Navigator.pop(context);
              await _openMessagingWithSeller();
            },
          ),
          ListTile(
            leading: CustomIconWidget(
              iconName: 'whatsapp',
              color: const Color(0xFF25D366), // WhatsApp green
              size: 24,
            ),
            title: Text(
              'WhatsApp',
              style: GoogleFonts.inter(
                fontSize: 16,
                fontWeight: FontWeight.w500,
                color: colorScheme.onSurface,
              ),
            ),
            subtitle: Text(
              'Chat via WhatsApp',
              style: GoogleFonts.inter(
                fontSize: 14,
                color: colorScheme.onSurface.withValues(alpha: 0.6),
              ),
            ),
            onTap: () async {
              Navigator.pop(context);
              await _openWhatsAppWithSeller();
            },
          ),
          ListTile(
            leading: CustomIconWidget(
              iconName: 'phone',
              color: colorScheme.primary,
              size: 24,
            ),
            title: Text(
              'Call Seller',
              style: GoogleFonts.inter(
                fontSize: 16,
                fontWeight: FontWeight.w500,
                color: colorScheme.onSurface,
              ),
            ),
            subtitle: Text(
              '+1 (555) 123-4567',
              style: GoogleFonts.inter(
                fontSize: 14,
                color: colorScheme.onSurface.withValues(alpha: 0.6),
              ),
            ),
            onTap: () async {
              Navigator.pop(context);
              await _callSellerWithSeller();
            },
          ),
          const SizedBox(height: 16),
        ],
      ),
    );
  }

  Future<void> _openMessagingWithSeller() async {
    _setLoading(true);

    // Try to upsert seller from product payload (will use existing id if present)
    final productSellerMap =
        (_productData['seller'] as Map<String, dynamic>?) ?? {};
    // Prefer explicit seller id if present
    String? explicitSellerId = productSellerMap['id'] as String?;
    Seller seller;
    if (explicitSellerId != null && explicitSellerId.isNotEmpty) {
      final existing =
          await SellerService.instance.getSellerById(explicitSellerId);
      if (existing != null) {
        seller = existing;
      } else {
        // upsert using provided id and other fields
        productSellerMap['id'] = explicitSellerId;
        seller =
            await SellerService.instance.upsertSellerFromMap(productSellerMap);
      }
    } else {
      seller =
          await SellerService.instance.upsertSellerFromMap(productSellerMap);
      explicitSellerId = seller.id;
    }

    _setLoading(false);

    final conversation =
        await ConversationService.instance.getOrCreateConversation(
      sellerId: seller.id,
      sellerName: seller.name,
      sellerAvatar: seller.avatar,
    );

    if (!mounted) return;
    
    // Capture Navigator after mounted check to avoid use_build_context_synchronously
    final navigator = Navigator.of(context);
    WidgetsBinding.instance.addPostFrameCallback((_) {
      navigator.pushNamed(AppRoutes.chat, arguments: conversation);
    });
  }

  Future<void> _callSellerWithSeller() async {
    _setLoading(true);

    final productSellerMap =
        (_productData['seller'] as Map<String, dynamic>?) ?? {};
    String? explicitSellerId = productSellerMap['id'] as String?;
    Seller seller;
    if (explicitSellerId != null && explicitSellerId.isNotEmpty) {
      final existing =
          await SellerService.instance.getSellerById(explicitSellerId);
      seller = existing ??
          await SellerService.instance.upsertSellerFromMap(productSellerMap);
    } else {
      seller =
          await SellerService.instance.upsertSellerFromMap(productSellerMap);
    }

    final rawPhone = seller.phone ?? '';
    final isPublic = seller.phonePrivacyPublic;

    if (rawPhone.isEmpty) {
      _setLoading(false);
      setState(() {
        _hudError = 'Seller phone number not available.';
        _hudRetryAction = () => _callSellerWithSeller();
      });
      return;
    }

    if (!isPublic) {
      _setLoading(false);
      setState(() {
        _hudError = 'Seller phone number is private.';
        _hudRetryAction = () => _callSellerWithSeller();
      });
      return;
    }

    final normalized = _normalizePhone(rawPhone, seller.countryCode);

    _setLoading(false); // clear HUD before showing confirmation
    if (!mounted) return;

    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Call Seller'),
        content: Text('Call $normalized?'),
        actions: [
          TextButton(
              onPressed: () => Navigator.pop(context),
              child: const Text('Cancel')),
          ElevatedButton(
            onPressed: () {
              Navigator.pop(context);
              final uri = Uri(scheme: 'tel', path: normalized);
              launchUrl(uri);
            },
            child: const Text('Call'),
          ),
        ],
      ),
    );
  }

  Future<void> _openWhatsAppWithSeller() async {
    _setLoading(true);

    final productSellerMap =
        (_productData['seller'] as Map<String, dynamic>?) ?? {};
    String? explicitSellerId = productSellerMap['id'] as String?;
    Seller seller;
    if (explicitSellerId != null && explicitSellerId.isNotEmpty) {
      final existing =
          await SellerService.instance.getSellerById(explicitSellerId);
      seller = existing ??
          await SellerService.instance.upsertSellerFromMap(productSellerMap);
    } else {
      seller =
          await SellerService.instance.upsertSellerFromMap(productSellerMap);
    }

    final rawPhone = seller.phone ?? '';
    final isPublic = seller.phonePrivacyPublic;

    if (rawPhone.isEmpty) {
      _setLoading(false);
      setState(() {
        _hudError = 'Seller phone number not available.';
        _hudRetryAction = () => _openWhatsAppWithSeller();
      });
      return;
    }

    if (!isPublic) {
      _setLoading(false);
      setState(() {
        _hudError = 'Seller phone number is private.';
        _hudRetryAction = () => _openWhatsAppWithSeller();
      });
      return;
    }

    final normalized = _normalizePhone(rawPhone, seller.countryCode);
    final uri = Uri.parse('https://wa.me/$normalized');

    _setLoading(false);
    if (!mounted) return;

    // show confirmation
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Open WhatsApp'),
        content: Text('Open WhatsApp chat with $normalized?'),
        actions: [
          TextButton(
              onPressed: () => Navigator.pop(context),
              child: const Text('Cancel')),
          ElevatedButton(
            onPressed: () async {
              final scaffold = ScaffoldMessenger.of(context);
              Navigator.pop(context);
              if (!await canLaunchUrl(uri)) {
                scaffold.showSnackBar(
                  const SnackBar(
                    content: Text('Could not open WhatsApp.'),
                    duration: Duration(seconds: 2),
                  ),
                );
                return;
              }
              await launchUrl(uri, mode: LaunchMode.externalApplication);
            },
            child: const Text('Open'),
          ),
        ],
      ),
    );
  }

  String _normalizePhone(String raw, String? countryCode) {
    final digits = raw.replaceAll(RegExp(r'[^0-9]'), '');
    final cc = (countryCode ?? '254').replaceAll(RegExp(r'[^0-9]'), '');
    // If digits already looks like an international number (starts with country code), return as-is.
    if (digits.length >= 10) return digits;
    // Otherwise prefix country code
    return '$cc$digits';
  }

  void _makeOffer() {
    HapticFeedback.lightImpact();
    showDialog(
      context: context,
      builder: (context) => _buildMakeOfferDialog(context),
    );
  }

  Widget _buildMakeOfferDialog(BuildContext context) {
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;
    final TextEditingController offerController = TextEditingController();

    return AlertDialog(
      title: Text(
        'Make an Offer',
        style: GoogleFonts.inter(
          fontSize: 18,
          fontWeight: FontWeight.w600,
          color: colorScheme.onSurface,
        ),
      ),
      content: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Current Price: ${_productData['price']}',
            style: GoogleFonts.inter(
              fontSize: 14,
              color: colorScheme.onSurface.withValues(alpha: 0.6),
            ),
          ),
          const SizedBox(height: 16),
          TextField(
            controller: offerController,
            keyboardType: TextInputType.number,
            decoration: InputDecoration(
              labelText: 'Your Offer (\$)',
              hintText: 'Enter your offer amount',
              prefixText: '\$ ',
            ),
          ),
        ],
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.pop(context),
          child: Text('Cancel'),
        ),
        ElevatedButton(
          onPressed: () {
            Navigator.pop(context);
            _submitOffer(offerController.text);
          },
          child: Text('Submit Offer'),
        ),
      ],
    );
  }

  void _submitOffer(String amount) {
    if (amount.isNotEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Offer of \$$amount submitted to seller'),
          duration: const Duration(seconds: 3),
        ),
      );
    }
  }

  void _toggleSaveProduct() {
    if (_productId == null) return;

    setState(() => _isSaved = !_isSaved);

    if (_isSaved) {
      ProductRepository.addToFavorites(_productId!).then((_) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('Added to favorites')),
          );
        }
      }).catchError((e) {
        setState(() => _isSaved = false);
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text('Error: $e')),
          );
        }
      });
    } else {
      ProductRepository.removeFromFavorites(_productId!).then((_) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('Removed from favorites')),
          );
        }
      }).catchError((e) {
        setState(() => _isSaved = true);
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text('Error: $e')),
          );
        }
      });
    }
  }

  void _shareProduct() {
    HapticFeedback.lightImpact();
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(
        content: Text('Sharing product...'),
        duration: Duration(seconds: 2),
      ),
    );
  }

  void _navigateToProduct(Map<String, dynamic> product) {
    final productId = product['id'] as String?;
    if (productId == null || productId.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Invalid product')),
      );
      return;
    }

    // Replace current product with new one
    _productId = productId;
    _loadProductData();
  }

  void _viewSellerProfile() {
    if (_productData == null) return;

    final seller = _productData!['seller'] as Map<String, dynamic>?;
    final sellerId = seller?['id'] as String?;

    if (sellerId == null || sellerId.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Seller information not available')),
      );
      return;
    }

    Navigator.pushNamed(
      context,
      '/seller-profile',
      arguments: {'sellerId': sellerId},
    );
  }

  void _contactSeller() {
    if (_productData == null) return;

    final seller = _productData!['seller'] as Map<String, dynamic>?;
    final sellerId = seller?['id'] as String?;
    final sellerName = seller?['name'] as String? ?? 'Seller';

    if (sellerId == null || sellerId.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Cannot contact seller at this time')),
      );
      return;
    }

    // Navigate to messages with seller
    Navigator.pushNamed(
      context,
      '/messages',
      arguments: {'userId': sellerId, 'userName': sellerName},
    );
  }

  void _makeOffer() {
    if (_productData == null) return;

    final TextEditingController offerController = TextEditingController();
    final TextEditingController messageController = TextEditingController();

    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Make an Offer'),
        content: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              TextField(
                controller: offerController,
                decoration: const InputDecoration(
                  labelText: 'Your Offer Price',
                  prefixText: '\$ ',
                  border: OutlineInputBorder(),
                ),
                keyboardType: TextInputType.number,
              ),
              const SizedBox(height: 16),
              TextField(
                controller: messageController,
                decoration: const InputDecoration(
                  labelText: 'Message (optional)',
                  border: OutlineInputBorder(),
                ),
                maxLines: 3,
              ),
            ],
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            onPressed: () {
              Navigator.pop(context);
              _submitOffer(offerController.text, messageController.text);
            },
            child: const Text('Send Offer'),
          ),
        ],
      ),
    );
  }

  void _submitOffer(String amount, String message) {
    if (amount.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please enter an offer amount')),
      );
      return;
    }

    if (_productData == null) return;

    final seller = _productData!['seller'] as Map<String, dynamic>?;
    final sellerId = seller?['id'] as String?;

    if (sellerId == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Cannot send offer at this time')),
      );
      return;
    }

    // Send offer to seller via message
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text('Offer of \$$amount submitted to seller'),
        duration: const Duration(seconds: 3),
      ),
    );
  }
      (product) => product['id'] == productId,
      orElse: () => <String, dynamic>{},
    );

    if (relatedProduct.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Product not found'),
          duration: Duration(seconds: 2),
        ),
      );
      return;
    }

    // Create full product data from related product
    final fullProductData = _createFullProductData(relatedProduct);

    // Navigate to product detail with full data
    Navigator.pushNamed(context, '/product-detail', arguments: fullProductData);
  }

  Map<String, dynamic> _createFullProductData(Map<String, dynamic> relatedProduct) {
    final productId = relatedProduct['id'] as String;

    // Create mock full product data based on the product ID
    // In a real app, this would fetch from an API
    switch (productId) {
      case 'prod_002':
        return {
          "title": "Cisco ASA 5506-X Firewall",
          "price": "\$1,250.00",
          "condition": "New",
          "rating": 4.7,
          "reviewCount": 23,
          "images": [
            "https://images.pexels.com/photos/442150/pexels-photo-442150.jpeg?auto=compress&cs=tinysrgb&w=400",
            "https://images.pexels.com/photos/442151/pexels-photo-442151.jpeg?auto=compress&cs=tinysrgb&w=400"
          ],
          "seller": {
            "id": "seller_002",
            "name": "SecureNet Solutions",
            "avatar": "https://images.pexels.com/photos/220453/pexels-photo-220453.jpeg?auto=compress&cs=tinysrgb&w=400",
            "isVerified": true,
            "rating": 4.8,
            "totalSales": 156
          },
          "description": "Advanced firewall security appliance with next-generation features including application visibility and control, multiple security services, and flexible VPN capabilities.",
          "specifications": {
            "Model": "ASA 5506-X",
            "Throughput": "750 Mbps",
            "VPN Peers": "50",
            "Interfaces": "8 x 1GbE",
            "Security Contexts": "2"
          },
          "shipping": {
            "info": "Ships within 1-2 business days",
            "estimatedDelivery": "3-5 business days",
            "cost": "Free shipping",
            "returnPolicy": "30-day return policy. Items must be in original condition with all accessories. Return shipping costs are covered by seller for defective items."
          }
        };

      case 'prod_003':
        return {
          "title": "Ubiquiti UniFi Switch 24-Port",
          "price": "\$379.00",
          "condition": "New",
          "rating": 4.6,
          "reviewCount": 45,
          "images": [
            "https://images.pexels.com/photos/325229/pexels-photo-325229.jpeg?auto=compress&cs=tinysrgb&w=400",
            "https://images.pexels.com/photos/325230/pexels-photo-325230.jpeg?auto=compress&cs=tinysrgb&w=400"
          ],
          "seller": {
            "id": "seller_003",
            "name": "NetworkPro Solutions",
            "avatar": "https://images.pexels.com/photos/220453/pexels-photo-220453.jpeg?auto=compress&cs=tinysrgb&w=400",
            "isVerified": true,
            "rating": 4.7,
            "totalSales": 89
          },
          "description": "Managed Gigabit switch with 24 ports, featuring auto-sensing Gigabit Ethernet, PoE+ support, and UniFi Controller management.",
          "specifications": {
            "Model": "US-24-250W",
            "Ports": "24 x 1GbE",
            "PoE Budget": "250W",
            "Switching Capacity": "52 Gbps",
            "Forwarding Rate": "38.69 Mpps"
          },
          "shipping": {
            "info": "Ships within 1-2 business days",
            "estimatedDelivery": "3-5 business days",
            "cost": "Free shipping",
            "returnPolicy": "30-day return policy. Items must be in original condition with all accessories."
          }
        };

      case 'prod_004':
        return {
          "title": "Netgear ProSAFE 48-Port Switch",
          "price": "\$899.00",
          "condition": "Like New",
          "rating": 4.4,
          "reviewCount": 31,
          "images": [
            "https://images.pexels.com/photos/159304/network-cable-ethernet-computer-159304.jpeg?auto=compress&cs=tinysrgb&w=400",
            "https://images.pexels.com/photos/159305/network-cable-ethernet-computer-159305.jpeg?auto=compress&cs=tinysrgb&w=400"
          ],
          "seller": {
            "id": "seller_004",
            "name": "TechHub Networks",
            "avatar": "https://images.pexels.com/photos/220453/pexels-photo-220453.jpeg?auto=compress&cs=tinysrgb&w=400",
            "isVerified": false,
            "rating": 4.2,
            "totalSales": 67
          },
          "description": "Enterprise-grade managed switch with 48 Gigabit ports, advanced security features, and comprehensive management capabilities.",
          "specifications": {
            "Model": "GS748T",
            "Ports": "48 x 1GbE + 4 x SFP",
            "Switching Capacity": "96 Gbps",
            "MAC Address Table": "16K",
            "Jumbo Frame Support": "9KB"
          },
          "shipping": {
            "info": "Ships within 2-3 business days",
            "estimatedDelivery": "4-6 business days",
            "cost": "\$25.00",
            "returnPolicy": "14-day return policy for unopened items."
          }
        };

      case 'prod_005':
        return {
          "title": "HP Aruba 2930F Switch Series",
          "price": "\$1,850.00",
          "condition": "New",
          "rating": 4.8,
          "reviewCount": 18,
          "images": [
            "https://images.pexels.com/photos/163064/play-stone-network-networked-interactive-163064.jpeg?auto=compress&cs=tinysrgb&w=400",
            "https://images.pexels.com/photos/163065/play-stone-network-networked-interactive-163065.jpeg?auto=compress&cs=tinysrgb&w=400"
          ],
          "seller": {
            "id": "seller_005",
            "name": "Enterprise Networks Inc",
            "avatar": "https://images.pexels.com/photos/220453/pexels-photo-220453.jpeg?auto=compress&cs=tinysrgb&w=400",
            "isVerified": true,
            "rating": 4.9,
            "totalSales": 203
          },
          "description": "High-performance managed switch with advanced Layer 3 features, PoE support, and Aruba's innovative management interface.",
          "specifications": {
            "Model": "JL253A",
            "Ports": "24 x 1GbE + 4 x 1GbE/SFP",
            "PoE Budget": "370W",
            "Switching Capacity": "88 Gbps",
            "Latency": "< 3.8 μs"
          },
          "shipping": {
            "info": "Ships within 1-2 business days",
            "estimatedDelivery": "3-5 business days",
            "cost": "Free shipping",
            "returnPolicy": "30-day return policy with full refund for defective items."
          }
        };

      default:
        // Fallback product data
        return {
          "title": relatedProduct['title'] ?? 'Unknown Product',
          "price": relatedProduct['price'] ?? '\$0.00',
          "condition": "Unknown",
          "rating": relatedProduct['rating'] ?? 0.0,
          "reviewCount": 0,
          "images": [relatedProduct['image'] ?? ''],
          "seller": {
            "id": "seller_unknown",
            "name": "Unknown Seller",
            "avatar": "https://images.pexels.com/photos/220453/pexels-photo-220453.jpeg?auto=compress&cs=tinysrgb&w=400",
            "isVerified": false,
            "rating": 0.0,
            "totalSales": 0
          },
          "description": "Product description not available.",
          "specifications": {},
          "shipping": {
            "info": "Shipping information not available",
            "estimatedDelivery": "TBD",
            "cost": "TBD",
            "returnPolicy": "Standard return policy applies."
          }
        };
    }
  }
}

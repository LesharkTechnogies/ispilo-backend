package com.ispilo.service;

import com.ispilo.exception.NotFoundException;
import com.ispilo.exception.BadRequestException;
import com.ispilo.model.dto.request.CreateProductRequest;
import com.ispilo.model.dto.request.AddReviewRequest;
import com.ispilo.model.dto.response.PageResponse;
import com.ispilo.model.dto.response.ProductResponse;
import com.ispilo.model.entity.Product;
import com.ispilo.model.entity.Seller;
import com.ispilo.model.entity.User;
import com.ispilo.repository.ProductRepository;
import com.ispilo.repository.SellerRepository;
import com.ispilo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final SellerRepository sellerRepository;
    private final UserRepository userRepository;

    /**
     * Get all products with pagination
     */
    public PageResponse<?> getAllProducts(Pageable pageable) {
        Page<Product> page = productRepository.findAll(pageable);
        return buildPageResponse(page);
    }

    /**
     * Get product by ID
     */
    public ProductResponse getProductById(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        return ProductResponse.fromEntity(product);
    }

    /**
     * Search products by keyword
     */
    public PageResponse<?> searchProducts(String keyword, Pageable pageable) {
        Page<Product> page = productRepository.searchProducts(keyword, pageable);
        return buildPageResponse(page);
    }

    /**
     * Get products by category
     */
    public PageResponse<?> getProductsByCategory(String category, Pageable pageable) {
        Page<Product> page = productRepository.findByCategory(category, pageable);
        return buildPageResponse(page);
    }

    /**
     * Get products by seller
     */
    public PageResponse<?> getProductsBySeller(String sellerId, Pageable pageable) {
        Page<Product> page = productRepository.findBySellerId(sellerId, pageable);
        return buildPageResponse(page);
    }

    /**
     * Get featured products
     */
    public PageResponse<?> getFeaturedProducts(Pageable pageable) {
        Page<Product> page = productRepository.findByIsFeaturedTrue(pageable);
        return buildPageResponse(page);
    }

    /**
     * Get trending products sorted by rating
     */
    public PageResponse<?> getTrendingProducts(Pageable pageable) {
        Page<Product> page = productRepository.findTopRatedProducts(pageable);
        return buildPageResponse(page);
    }

    /**
     * Create a new product
     */
    public ProductResponse createProduct(String userId, CreateProductRequest request) {
        // We need to check if user exists, even if we don't use the object directly here
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found");
        }

        Seller seller = sellerRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException("User is not a seller. Please register as seller first."));

        // Validate product data
        if (request.title() == null || request.title().trim().isEmpty()) {
            throw new BadRequestException("Product title is required");
        }
        if (request.price() == null || request.price().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Product price must be greater than 0");
        }

        Product product = Product.builder()
                .title(request.title())
                .name(request.title())
                .description(request.description())
                .price(request.price())
                .mainImage(request.mainImage())
                .images(request.images() != null ? request.images() : List.of())
                .category(request.category())
                .condition(request.condition() != null ? request.condition() : "New")
                .location(request.location())
                .stockQuantity(request.stockQuantity() != null ? request.stockQuantity() : 0)
                .isAvailable(true)
                .isFeatured(false)
                .seller(seller)
                .build();

        Product saved = productRepository.save(product);
        log.info("Product created: {}", saved.getId());

        return ProductResponse.fromEntity(saved);
    }

    /**
     * Update product
     */
    public ProductResponse updateProduct(String productId, CreateProductRequest request, String userId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        // Verify ownership
        if (!product.getSeller().getUser().getId().equals(userId)) {
            throw new BadRequestException("You do not have permission to update this product");
        }

        if (request.title() != null && !request.title().trim().isEmpty()) {
            product.setTitle(request.title());
            product.setName(request.title());
        }
        if (request.description() != null) {
            product.setDescription(request.description());
        }
        if (request.price() != null && request.price().compareTo(BigDecimal.ZERO) > 0) {
            product.setPrice(request.price());
        }
        if (request.mainImage() != null) {
            product.setMainImage(request.mainImage());
        }
        if (request.images() != null) {
            product.setImages(request.images());
        }
        if (request.category() != null) {
            product.setCategory(request.category());
        }
        if (request.condition() != null) {
            product.setCondition(request.condition());
        }
        if (request.stockQuantity() != null) {
            product.setStockQuantity(request.stockQuantity());
        }
        if (request.location() != null) {
            product.setLocation(request.location());
        }

        Product updated = productRepository.save(product);
        log.info("Product updated: {}", productId);

        return ProductResponse.fromEntity(updated);
    }

    /**
     * Delete product
     */
    public void deleteProduct(String productId, String userId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        // Verify ownership
        if (!product.getSeller().getUser().getId().equals(userId)) {
            throw new BadRequestException("You do not have permission to delete this product");
        }

        productRepository.delete(product);
        log.info("Product deleted: {}", productId);
    }

    /**
     * Add product to user's favorites
     */
    public void addToFavorites(String userId, String productId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found");
        }

        if (!productRepository.existsById(productId)) {
            throw new NotFoundException("Product not found");
        }

        // TODO: Implement UserFavorite entity and relationship
        log.info("Product {} added to favorites by user {}", productId, userId);
    }

    /**
     * Remove product from favorites
     */
    public void removeFromFavorites(String userId, String productId) {
        // TODO: Implement removal from favorites
        log.info("Product {} removed from favorites by user {}", productId, userId);
    }

    /**
     * Get all available product categories
     */
    public List<String> getCategories() {
        return productRepository.findAllCategories();
    }

    /**
     * Build page response from product page
     */
    private PageResponse<?> buildPageResponse(Page<Product> page) {
        return PageResponse.builder()
                .content(page.getContent().stream()
                        .map(ProductResponse::fromEntity)
                        .collect(Collectors.toList()))
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    /**
     * Get complete product details with seller and reviews
     */
    public Map<String, Object> getCompleteProductDetails(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found: " + productId));

        Map<String, Object> response = new HashMap<>();
        response.put("product", ProductResponse.fromEntity(product));

        // Get seller info
        if (product.getSeller() != null) {
            Map<String, Object> sellerInfo = new HashMap<>();
            sellerInfo.put("id", product.getSeller().getId());
            sellerInfo.put("name", product.getSeller().getName());
            sellerInfo.put("avatar", product.getSeller().getAvatar());
            sellerInfo.put("phone", product.getSeller().getPhone());
            sellerInfo.put("isVerified", product.getSeller().getIsVerified());
            sellerInfo.put("rating", product.getSeller().getRating());
            sellerInfo.put("totalSales", product.getSeller().getTotalSales());
            response.put("seller", sellerInfo);
        }

        // TODO: Get reviews (when Review entity is created)
        response.put("reviews", new ArrayList<>());

        // TODO: Get ratings breakdown
        response.put("ratings", new HashMap<>());

        return response;
    }

    /**
     * Get product with seller information
     */
    public Map<String, Object> getProductWithSeller(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        Map<String, Object> response = new HashMap<>();
        response.put("product", ProductResponse.fromEntity(product));

        if (product.getSeller() != null) {
            Map<String, Object> sellerInfo = new HashMap<>();
            sellerInfo.put("id", product.getSeller().getId());
            sellerInfo.put("name", product.getSeller().getName());
            sellerInfo.put("avatar", product.getSeller().getAvatar());
            sellerInfo.put("isVerified", product.getSeller().getIsVerified());
            sellerInfo.put("rating", product.getSeller().getRating());
            response.put("seller", sellerInfo);
        }

        return response;
    }

    /**
     * Get product reviews
     */
    public PageResponse<?> getProductReviews(String productId, Pageable pageable) {
        if (!productRepository.existsById(productId)) {
            throw new NotFoundException("Product not found");
        }

        // TODO: Implement Review entity and repository
        // For now, return empty list
        return PageResponse.builder()
                .content(new ArrayList<>())
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .totalElements(0)
                .totalPages(0)
                .last(true)
                .build();
    }

    /**
     * Add review to product
     */
    public Map<String, Object> addProductReview(String userId, String productId, AddReviewRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!productRepository.existsById(productId)) {
            throw new NotFoundException("Product not found");
        }

        // TODO: Create Review entity and save to database
        Map<String, Object> response = new HashMap<>();
        response.put("id", "review-" + System.currentTimeMillis());
        response.put("rating", request.getRating());
        response.put("comment", request.getComment());
        response.put("reviewer", user.getName());
        response.put("createdAt", new Date());

        log.info("Review added to product {} by user {}", productId, userId);
        return response;
    }
}

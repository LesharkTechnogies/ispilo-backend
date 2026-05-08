package com.ispilo.service;

import com.ispilo.exception.NotFoundException;
import com.ispilo.exception.BadRequestException;
import com.ispilo.model.dto.request.CreateProductRequest;
import com.ispilo.model.dto.request.AddReviewRequest;
import com.ispilo.model.dto.response.PageResponse;
import com.ispilo.model.dto.response.ProductResponse;
import com.ispilo.model.entity.Product;
import com.ispilo.model.entity.ProductReview;
import com.ispilo.model.entity.ProductReviewReaction;
import com.ispilo.model.entity.ProductReport;
import com.ispilo.model.entity.Seller;
import com.ispilo.model.entity.User;
import com.ispilo.model.enums.SellerVerificationLevel;
import com.ispilo.model.enums.ReviewReactionType;
import com.ispilo.model.dto.response.ProductReviewResponse;
import com.ispilo.model.dto.request.CreateReportRequest;
import com.ispilo.model.dto.response.ReportResponse;
import com.ispilo.repository.ProductRepository;
import com.ispilo.repository.ProductReviewReactionRepository;
import com.ispilo.repository.ProductReviewRepository;
import com.ispilo.repository.ProductReportRepository;
import com.ispilo.service.BannedDeviceCacheService;
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
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductReviewRepository productReviewRepository;
    private final ProductReviewReactionRepository productReviewReactionRepository;
    private final ProductReportRepository productReportRepository;
    private final BannedDeviceCacheService bannedDeviceCacheService;
    private final SellerRepository sellerRepository;
    private final UserRepository userRepository;

    private static final List<String> FLAGGED_WORDS = List.of(
        "scum",
        "ameniosha",
        "mwizi",
        "flag"
    );

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
     * Get products by seller verification level
     */
    public PageResponse<?> getProductsBySellerVerificationLevel(SellerVerificationLevel level, Pageable pageable) {
        Page<Product> page = productRepository.findBySellerVerificationLevel(level, pageable);
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
    public ProductResponse createProduct(String username, CreateProductRequest request) {
        com.ispilo.model.entity.User user = userRepository.findByEmail(username)
                .orElseGet(() -> userRepository.findByPhone(username)
                        .orElseThrow(() -> new NotFoundException("User not found")));
        
        boolean isAdmin = Boolean.TRUE.equals(user.getIsAdmin());
        Seller seller;

        if (isAdmin && request.sellerId() != null && !request.sellerId().trim().isEmpty()) {
            seller = sellerRepository.findById(request.sellerId())
                    .orElseThrow(() -> new NotFoundException("Seller not found for the provided ID"));
        } else {
            seller = sellerRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new BadRequestException("User is not a seller. Please register as seller first."));
        }

        if (seller.getUploadBlockedUntil() != null && seller.getUploadBlockedUntil().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Seller is temporarily blocked from uploading products.");
        }

        // Apply seller verification limits
        if (!isAdmin) {
            if (seller.getVerificationLevel() == com.ispilo.model.enums.SellerVerificationLevel.UNVERIFIED) {
                long activeCount = productRepository.countBySellerId(seller.getId());
                if (activeCount >= 3) {
                    throw new BadRequestException("Unverified sellers can only post up to 3 products. Please verify your ID or delete older products.");
                }
            } else if (seller.getVerificationLevel() == com.ispilo.model.enums.SellerVerificationLevel.ID_VERIFIED) {
                LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
                long weekCount = productRepository.countBySellerIdAndCreatedAtAfter(seller.getId(), oneWeekAgo);
                if (weekCount >= 10) {
                    throw new BadRequestException("ID verified sellers can only post up to 10 products per week. Please wait or upgrade your account to a Fully Verified Shop.");
                }
            }
        }

        // Calculate Expiry Date based on verification level
        LocalDateTime expiresAt = null;
        if (seller.getVerificationLevel() == com.ispilo.model.enums.SellerVerificationLevel.FULLY_VERIFIED) {
            expiresAt = LocalDateTime.now().plusMonths(1);
        } else {
            expiresAt = LocalDateTime.now().plusWeeks(1);
        }

        // Device Security Flag
        if (request.deviceId() != null && !request.deviceId().trim().isEmpty()) {
            if (bannedDeviceCacheService.isBanned(request.deviceId())) {
                throw new BadRequestException("This device is banned from using marketplace services.");
            }
            boolean deviceUsedByOthers = productRepository.existsByDeviceIdAndSellerIdNot(request.deviceId(), seller.getId());
            if (deviceUsedByOthers) {
                log.warn("SECURITY ALERT: Device ID {} used by Seller {} was previously used by another seller account. Flagging for review.", request.deviceId(), seller.getId());
                // In a production scenario, you could suspend the account or send a Brevo email to the admin here.
            }
        }

        // Validate product data
        if (request.title() == null || request.title().trim().isEmpty()) {
            throw new BadRequestException("Product title is required");
        }
        if (request.price() == null || request.price().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Product price must be greater than 0");
        }

        String imageUrl = request.mainImage();
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            imageUrl = "https://ispilo.com/default-product-image.png";
        }

        List<String> images = buildProductImages(imageUrl, request);

        Product product = Product.builder()
                .title(request.title())
                .name(request.title())
                .description(request.description())
                .price(request.price())
                .mainImage(imageUrl)
                .images(images)
                .category(request.category())
                .condition(request.condition() != null ? request.condition() : "New")
                .location(request.location())
                .stockQuantity(request.stockQuantity() != null ? request.stockQuantity() : 0)
                .isAvailable(true)
                .isFeatured(false)
                .expiresAt(expiresAt)
                .deviceId(request.deviceId())
                .seller(seller)
                .build();

        Product saved = productRepository.save(product);
        log.info("Product created: {} by user {}", saved.getId(), user.getId());

        return ProductResponse.fromEntity(saved);
    }

    /**
     * Update product
     */
    public ProductResponse updateProduct(String productId, CreateProductRequest request, String username) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        com.ispilo.model.entity.User user = userRepository.findByEmail(username)
                .orElseGet(() -> userRepository.findByPhone(username)
                        .orElseThrow(() -> new NotFoundException("User not found")));

        boolean isAdmin = Boolean.TRUE.equals(user.getIsAdmin());

        // Verify ownership
        if (!isAdmin && !product.getSeller().getUser().getId().equals(user.getId())) {
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
        if (request.mainImage() != null && !request.mainImage().trim().isEmpty()) {
            product.setMainImage(request.mainImage());
        } else if (product.getMainImage() == null || product.getMainImage().trim().isEmpty()) {
            product.setMainImage("https://ispilo.com/default-product-image.png");
        }

        boolean hasImageInputs = hasImageInputs(request);
        if (hasImageInputs) {
            String mainImage = product.getMainImage();
            product.setImages(buildProductImages(mainImage, request));
        } else if (product.getImages() == null || product.getImages().isEmpty()) {
            product.setImages(List.of("https://ispilo.com/default-product-image.png"));
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
        log.info("Product updated: {} by user {}", productId, user.getId());

        return ProductResponse.fromEntity(updated);
    }

    /**
     * Delete product
     */
    public void deleteProduct(String productId, String username) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        com.ispilo.model.entity.User user = userRepository.findByEmail(username)
                .orElseGet(() -> userRepository.findByPhone(username)
                        .orElseThrow(() -> new NotFoundException("User not found")));

        boolean isAdmin = Boolean.TRUE.equals(user.getIsAdmin());

        // Verify ownership
        if (!isAdmin && !product.getSeller().getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You do not have permission to delete this product");
        }

        productRepository.delete(product);
        log.info("Product deleted: {} by user {}", productId, user.getId());
    }

    /**
     * Add product to user's favorites
     */
    public void addToFavorites(String username, String productId) {
        com.ispilo.model.entity.User user = userRepository.findByEmail(username)
                .orElseGet(() -> userRepository.findByPhone(username)
                        .orElseThrow(() -> new NotFoundException("User not found")));

        if (!productRepository.existsById(productId)) {
            throw new NotFoundException("Product not found");
        }

        // TODO: Implement UserFavorite entity and relationship
        log.info("Product {} added to favorites by user {}", productId, user.getId());
    }

    /**
     * Remove product from favorites
     */
    public void removeFromFavorites(String username, String productId) {
        com.ispilo.model.entity.User user = userRepository.findByEmail(username)
                .orElseGet(() -> userRepository.findByPhone(username)
                        .orElseThrow(() -> new NotFoundException("User not found")));
        // TODO: Implement removal from favorites
        log.info("Product {} removed from favorites by user {}", productId, user.getId());
    }

    private boolean hasImageInputs(CreateProductRequest request) {
        return (request.mainImage() != null && !request.mainImage().trim().isEmpty())
                || (request.imageUrl1() != null && !request.imageUrl1().trim().isEmpty())
                || (request.imageUrl2() != null && !request.imageUrl2().trim().isEmpty())
                || (request.imageUrl3() != null && !request.imageUrl3().trim().isEmpty())
                || (request.imageUrl4() != null && !request.imageUrl4().trim().isEmpty())
                || (request.images() != null && !request.images().isEmpty());
    }

    private List<String> buildProductImages(String mainImage, CreateProductRequest request) {
        List<String> images = new ArrayList<>();
        if (mainImage != null && !mainImage.trim().isEmpty()) {
            images.add(mainImage);
        }

        addIfPresent(images, request.imageUrl1());
        addIfPresent(images, request.imageUrl2());
        addIfPresent(images, request.imageUrl3());
        addIfPresent(images, request.imageUrl4());

        if (request.images() != null) {
            for (String url : request.images()) {
                if (images.size() >= 5) {
                    break;
                }
                addIfPresent(images, url);
            }
        }

        if (images.isEmpty()) {
            images.add("https://ispilo.com/default-product-image.png");
        }

        if (images.size() > 5) {
            return images.subList(0, 5);
        }

        return images;
    }

    private void addIfPresent(List<String> images, String url) {
        if (url == null) {
            return;
        }
        String trimmed = url.trim();
        if (trimmed.isEmpty()) {
            return;
        }
        if (!images.contains(trimmed)) {
            images.add(trimmed);
        }
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

    response.put("reviews", productReviewRepository.findTop5ByProductIdOrderByCreatedAtDesc(productId)
        .stream()
        .map(ProductReviewResponse::fromEntity)
        .collect(Collectors.toList()));

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

    Page<ProductReview> page = productReviewRepository.findByProductIdOrderByCreatedAtDesc(productId, pageable);
    return PageResponse.builder()
        .content(page.getContent().stream()
            .map(ProductReviewResponse::fromEntity)
            .collect(Collectors.toList()))
        .page(page.getNumber())
        .size(page.getSize())
        .totalElements(page.getTotalElements())
        .totalPages(page.getTotalPages())
        .last(page.isLast())
        .build();
    }

    /**
     * Add review to product
     */
    public ProductReviewResponse addProductReview(String username, String productId, AddReviewRequest request) {
        User user = userRepository.findByEmail(username)
                .orElseGet(() -> userRepository.findByPhone(username)
                        .orElseThrow(() -> new NotFoundException("User not found")));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        if (product.getSeller() != null && product.getSeller().getUser() != null
                && product.getSeller().getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Sellers cannot review their own products");
        }

        if (productReviewRepository.existsByProductAndUser(product, user)) {
            throw new BadRequestException("You have already reviewed this product");
        }

        ProductReview review = ProductReview.builder()
                .product(product)
                .user(user)
                .rating(request.getRating())
                .comment(request.getComment())
                .title(request.getTitle())
                .wouldRecommend(request.getWouldRecommend())
                .build();

    applyReviewFlagging(review);

        review = productReviewRepository.save(review);

    if (Boolean.TRUE.equals(review.getIsFlagged())) {
        ProductReport autoReport = ProductReport.builder()
            .product(product)
            .seller(product.getSeller())
            .reporter(user)
            .reason("AUTO_FLAGGED_REVIEW")
            .description("Review flagged for: " + review.getFlagReason())
            .build();
        productReportRepository.save(autoReport);
    }

        updateProductRating(product);

        log.info("Review added to product {} by user {}", productId, user.getId());
        return ProductReviewResponse.fromEntity(review);
    }

    public ReportResponse reportProduct(String username, String productId, CreateReportRequest request) {
    User user = userRepository.findByEmail(username)
        .orElseGet(() -> userRepository.findByPhone(username)
            .orElseThrow(() -> new NotFoundException("User not found")));

    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new NotFoundException("Product not found"));

    Seller seller = product.getSeller();
    if (seller == null) {
        throw new BadRequestException("Product seller not found for report");
    }

    ProductReport report = ProductReport.builder()
        .product(product)
        .seller(seller)
        .reporter(user)
        .reason(request.getReason())
        .description(request.getDescription())
        .build();

    report = productReportRepository.save(report);

    return ReportResponse.builder()
        .id(report.getId())
        .targetId(product.getId())
        .targetType("PRODUCT")
        .reason(report.getReason())
        .description(report.getDescription())
        .status(report.getStatus())
        .createdAt(report.getCreatedAt())
        .build();
    }

    /**
     * Like or dislike a product review
     */
    public ProductReviewResponse reactToProductReview(String username, String reviewId, ReviewReactionType reactionType) {
        User user = userRepository.findByEmail(username)
                .orElseGet(() -> userRepository.findByPhone(username)
                        .orElseThrow(() -> new NotFoundException("User not found")));

        ProductReview review = productReviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review not found"));

        ProductReviewReaction existing = productReviewReactionRepository.findByReviewAndUser(review, user)
                .orElse(null);

        if (existing != null && existing.getReactionType() == reactionType) {
            applyReviewReactionDelta(review, reactionType, -1);
            productReviewReactionRepository.delete(existing);
        } else if (existing != null) {
            applyReviewReactionDelta(review, existing.getReactionType(), -1);
            existing.setReactionType(reactionType);
            productReviewReactionRepository.save(existing);
            applyReviewReactionDelta(review, reactionType, 1);
        } else {
            ProductReviewReaction reaction = ProductReviewReaction.builder()
                    .review(review)
                    .user(user)
                    .reactionType(reactionType)
                    .build();
            productReviewReactionRepository.save(reaction);
            applyReviewReactionDelta(review, reactionType, 1);
        }

        productReviewRepository.save(review);
        updateProductRating(review.getProduct());

        return ProductReviewResponse.fromEntity(review);
    }

    private void applyReviewReactionDelta(ProductReview review, ReviewReactionType reactionType, int delta) {
        if (reactionType == ReviewReactionType.LIKE) {
            int next = Math.max(0, (review.getLikeCount() == null ? 0 : review.getLikeCount()) + delta);
            review.setLikeCount(next);
        } else {
            int next = Math.max(0, (review.getDislikeCount() == null ? 0 : review.getDislikeCount()) + delta);
            review.setDislikeCount(next);
        }
    }

    private void updateProductRating(Product product) {
        String productId = product.getId();
        Double averageRating = productReviewRepository.calculateAverageRating(productId);
        Long likeSum = productReviewRepository.sumLikeCount(productId);
        Long dislikeSum = productReviewRepository.sumDislikeCount(productId);

        double baseRating = averageRating != null ? averageRating : 4.5;
        long likes = likeSum != null ? likeSum : 0;
        long dislikes = dislikeSum != null ? dislikeSum : 0;
        double adjustment = (likes - dislikes) * 0.02;

        double rating = clampRating(baseRating + adjustment);
        product.setRating(rating);
        product.setReviewCount((int) productReviewRepository.countByProductId(productId));
        productRepository.save(product);
    }

    private void applyReviewFlagging(ProductReview review) {
        String comment = review.getComment() == null ? "" : review.getComment().toLowerCase();
        for (String word : FLAGGED_WORDS) {
            if (comment.contains(word)) {
                review.setIsFlagged(true);
                review.setFlagReason("FLAGGED_WORD:" + word);
                return;
            }
        }
        review.setIsFlagged(false);
        review.setFlagReason(null);
    }

    private double clampRating(double rating) {
        if (rating < 1.0) {
            return 1.0;
        }
        if (rating > 5.0) {
            return 5.0;
        }
        return Math.round(rating * 10.0) / 10.0;
    }
}

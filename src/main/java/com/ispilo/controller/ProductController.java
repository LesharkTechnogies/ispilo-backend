package com.ispilo.controller;

import com.ispilo.exception.NotFoundException;
import com.ispilo.model.dto.request.CreateProductRequest;
import com.ispilo.model.dto.request.AddReviewRequest;
import com.ispilo.model.dto.request.CreateReportRequest;
import com.ispilo.model.dto.response.MediaUploadResponse;
import com.ispilo.model.dto.response.PageResponse;
import com.ispilo.model.dto.response.ProductResponse;
import com.ispilo.model.dto.response.ProductReviewResponse;
import com.ispilo.model.dto.response.ReportResponse;
import com.ispilo.model.entity.User;
import com.ispilo.model.enums.SellerVerificationLevel;
import com.ispilo.model.enums.ReviewReactionType;
import com.ispilo.repository.UserRepository;
import com.ispilo.service.MediaService;
import com.ispilo.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Products", description = "Marketplace Product APIs")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;
    private final MediaService mediaService;
    private final UserRepository userRepository;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a product image")
    public ResponseEntity<MediaUploadResponse> uploadProductImage(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found"));
        MediaUploadResponse response = mediaService.uploadFile(file, "products", user.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all products with pagination")
    public ResponseEntity<PageResponse<?>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String sortBy) {

        Sort.Direction direction = "asc".equalsIgnoreCase(sortBy) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));

        if (category != null && !category.isEmpty()) {
            return ResponseEntity.ok(productService.getProductsByCategory(category, pageable));
        }

        return ResponseEntity.ok(productService.getAllProducts(pageable));
    }

    @GetMapping("/search")
    @Operation(summary = "Search products by keyword")
    public ResponseEntity<PageResponse<?>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(productService.searchProducts(keyword, pageable));
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Get product details by ID")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable String productId) {
        return ResponseEntity.ok(productService.getProductById(productId));
    }

    @GetMapping("/seller/{sellerId}")
    @Operation(summary = "Get all products by a specific seller")
    public ResponseEntity<PageResponse<?>> getProductsBySeller(
            @PathVariable String sellerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(productService.getProductsBySeller(sellerId, pageable));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get products by category")
    public ResponseEntity<PageResponse<?>> getProductsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(productService.getProductsByCategory(category, pageable));
    }

    @GetMapping("/featured")
    @Operation(summary = "Get featured products")
    public ResponseEntity<PageResponse<?>> getFeaturedProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(productService.getFeaturedProducts(pageable));
    }

    @GetMapping("/seller-level/{level}")
    @Operation(summary = "Get products by seller verification level")
    public ResponseEntity<PageResponse<?>> getProductsBySellerLevel(
            @PathVariable String level,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        SellerVerificationLevel parsedLevel = parseSellerVerificationLevel(level);
        return ResponseEntity.ok(productService.getProductsBySellerVerificationLevel(parsedLevel, pageable));
    }

    @GetMapping("/trending")
    @Operation(summary = "Get trending products by rating")
    public ResponseEntity<PageResponse<?>> getTrendingProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("rating").descending());
        return ResponseEntity.ok(productService.getTrendingProducts(pageable));
    }

    @PostMapping
    @Operation(summary = "Create a new product")
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        ProductResponse response = productService.createProduct(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{productId}")
    @Operation(summary = "Update product details")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable String productId,
            @Valid @RequestBody CreateProductRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        ProductResponse response = productService.updateProduct(productId, request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "Delete a product")
    public ResponseEntity<?> deleteProduct(
            @PathVariable String productId,
            @AuthenticationPrincipal UserDetails userDetails) {
        productService.deleteProduct(productId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{productId}/favorite")
    @Operation(summary = "Add product to favorites")
    public ResponseEntity<?> addToFavorites(
            @PathVariable String productId,
            @AuthenticationPrincipal UserDetails userDetails) {
        productService.addToFavorites(userDetails.getUsername(), productId);
        return ResponseEntity.ok(new MessageResponse("Product added to favorites"));
    }

    @DeleteMapping("/{productId}/favorite")
    @Operation(summary = "Remove product from favorites")
    public ResponseEntity<?> removeFromFavorites(
            @PathVariable String productId,
            @AuthenticationPrincipal UserDetails userDetails) {
        productService.removeFromFavorites(userDetails.getUsername(), productId);
        return ResponseEntity.ok(new MessageResponse("Product removed from favorites"));
    }

    @GetMapping("/categories")
    @Operation(summary = "Get all product categories")
    public ResponseEntity<?> getCategories() {
        return ResponseEntity.ok(productService.getCategories());
    }

    @GetMapping("/{productId}/complete")
    @Operation(summary = "Get complete product details with seller and reviews")
    public ResponseEntity<?> getCompleteProductDetails(@PathVariable String productId) {
        return ResponseEntity.ok(productService.getCompleteProductDetails(productId));
    }

    @GetMapping("/{productId}/with-seller")
    @Operation(summary = "Get product details with seller information")
    public ResponseEntity<?> getProductWithSeller(@PathVariable String productId) {
        return ResponseEntity.ok(productService.getProductWithSeller(productId));
    }

    @GetMapping("/{productId}/reviews")
    @Operation(summary = "Get product reviews with pagination")
    public ResponseEntity<?> getProductReviews(
            @PathVariable String productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(productService.getProductReviews(productId, pageable));
    }

    @PostMapping("/{productId}/reviews")
    @Operation(summary = "Add review to product")
    public ResponseEntity<ProductReviewResponse> addProductReview(
            @PathVariable String productId,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AddReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.addProductReview(userDetails.getUsername(), productId, request));
    }

    @PostMapping("/reviews/{reviewId}/like")
    @Operation(summary = "Like a product review")
    public ResponseEntity<ProductReviewResponse> likeProductReview(
            @PathVariable String reviewId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(productService.reactToProductReview(userDetails.getUsername(), reviewId, ReviewReactionType.LIKE));
    }

    @PostMapping("/reviews/{reviewId}/dislike")
    @Operation(summary = "Dislike a product review")
    public ResponseEntity<ProductReviewResponse> dislikeProductReview(
            @PathVariable String reviewId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(productService.reactToProductReview(userDetails.getUsername(), reviewId, ReviewReactionType.DISLIKE));
    }

    @PostMapping("/{productId}/reports")
    @Operation(summary = "Report a product (also flags the attached seller)")
    public ResponseEntity<ReportResponse> reportProduct(
            @PathVariable String productId,
            @Valid @RequestBody CreateReportRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.reportProduct(userDetails.getUsername(), productId, request));
    }

    private SellerVerificationLevel parseSellerVerificationLevel(String level) {
        if (level == null) {
            throw new com.ispilo.exception.BadRequestException("Seller verification level is required");
        }

        String normalized = level.trim().toLowerCase();
        switch (normalized) {
            case "verified":
            case "fully_verified":
            case "fully-verified":
            case "business":
            case "verified-business":
                return SellerVerificationLevel.FULLY_VERIFIED;
            case "id_verified":
            case "id-verified":
            case "idverified":
                return SellerVerificationLevel.ID_VERIFIED;
            case "unverified":
            case "non-verified":
            case "nonverified":
            case "normal":
            case "basic":
                return SellerVerificationLevel.UNVERIFIED;
            default:
                throw new com.ispilo.exception.BadRequestException("Invalid seller verification level: " + level);
        }
    }

    public record MessageResponse(String message) {}
}

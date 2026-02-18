package com.ispilo.repository;

import com.ispilo.model.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

    Page<Product> findBySellerId(String sellerId, Pageable pageable);

    Page<Product> findByCategory(String category, Pageable pageable);

    Page<Product> findByIsAvailable(Boolean isAvailable, Pageable pageable);

    Page<Product> findByIsFeaturedTrue(Pageable pageable);

    Page<Product> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    /**
     * Search products by title or description
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Product> searchProducts(@Param("query") String query, Pageable pageable);

    /**
     * Get all available product categories
     */
    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.category IS NOT NULL")
    List<String> findAllCategories();

    /**
     * Get products by seller and category
     */
    @Query("SELECT p FROM Product p WHERE p.seller.id = :sellerId AND p.category = :category")
    Page<Product> findBySellerIdAndCategory(@Param("sellerId") String sellerId, @Param("category") String category, Pageable pageable);

    /**
     * Get products sorted by rating
     */
    @Query("SELECT p FROM Product p ORDER BY p.rating DESC, p.reviewCount DESC")
    Page<Product> findTopRatedProducts(Pageable pageable);

    /**
     * Find products in a price range
     */
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice")
    Page<Product> findByPriceRange(@Param("minPrice") java.math.BigDecimal minPrice, @Param("maxPrice") java.math.BigDecimal maxPrice, Pageable pageable);
}

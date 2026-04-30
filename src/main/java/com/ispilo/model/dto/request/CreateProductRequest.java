package com.ispilo.model.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record CreateProductRequest(
    @NotBlank(message = "Product title is required")
    String title,

    @NotBlank(message = "Description is required")
    String description,

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    BigDecimal price,

    Integer stockQuantity,

    @URL(message = "Main image must be a valid URL")
    String mainImage,

    @URL(message = "Image URL 1 must be a valid URL")
    String imageUrl1,

    @URL(message = "Image URL 2 must be a valid URL")
    String imageUrl2,

    @URL(message = "Image URL 3 must be a valid URL")
    String imageUrl3,

    @URL(message = "Image URL 4 must be a valid URL")
    String imageUrl4,

    List<@URL(message = "Each image must be a valid URL") String> images,
    
    String category,
    String condition,
    String location,
    String sellerId
) {}

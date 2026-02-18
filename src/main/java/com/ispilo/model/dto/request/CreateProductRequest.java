package com.ispilo.model.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record CreateProductRequest(
    @NotBlank(message = "Product title is required")
    String title,

    String description,

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    BigDecimal price,

    Integer stockQuantity,
    String mainImage,
    List<String> images,
    String category,
    String condition,
    String location
) {}

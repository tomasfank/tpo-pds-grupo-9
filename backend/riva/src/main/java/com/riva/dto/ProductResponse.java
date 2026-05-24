package com.riva.dto;

import java.math.BigDecimal;
import java.util.List;

import com.riva.model.product.Product;

public record ProductResponse(
        String id,
        String name,
        String description,
        String brand,
        BigDecimal price,
        String material,
        List<String> imageUrls,
        boolean active,
        String categoryId,
        List<String> categoryAncestorIds,
        List<ProductVariantDto> variants
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getBrand(),
                product.getPrice(),
                product.getMaterial(),
                product.getImageUrls(),
                product.isActive(),
                product.getCategoryId(),
                product.getCategoryAncestorIds(),
                product.getVariants().stream().map(ProductVariantDto::from).toList()
        );
    }
}

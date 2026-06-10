package com.riva.dto;

import com.riva.model.product.ProductVariant;
import com.riva.model.product.Size;

import jakarta.validation.constraints.PositiveOrZero;

public record ProductVariantDto(
        String id,
        Size size,
        String color,
        @PositiveOrZero int stock
) {
    public ProductVariantDto(Size size, String color, int stock) {
        this(null, size, color, stock);
    }

    public ProductVariant toDomain() {
        return new ProductVariant(id, size, color, stock);
    }

    public static ProductVariantDto from(ProductVariant variant) {
        return new ProductVariantDto(variant.getId(), variant.getSize(), variant.getColor(), variant.getStock());
    }
}

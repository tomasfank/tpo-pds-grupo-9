package com.riva.dto;

import com.riva.model.product.ProductVariant;
import com.riva.model.product.Size;

import jakarta.validation.constraints.PositiveOrZero;

public record ProductVariantDto(
        Size size,
        String color,
        @PositiveOrZero int stock
) {
    public ProductVariant toDomain() {
        return new ProductVariant(size, color, stock);
    }

    public static ProductVariantDto from(ProductVariant variant) {
        return new ProductVariantDto(variant.getSize(), variant.getColor(), variant.getStock());
    }
}

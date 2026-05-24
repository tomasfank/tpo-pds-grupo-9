package com.riva.dto;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

// Todos los campos opcionales: solo se actualizan los que vienen no-null.
// categoryId aparte porque al cambiar de categoría hay que recalcular categoryAncestorIds.
public record UpdateProductRequest(
        @Size(max = 120) String name,
        @Size(max = 2000) String description,
        @Positive BigDecimal price,
        @Size(max = 200) String material,
        String categoryId,
        List<String> imageUrls,
        @Valid List<ProductVariantDto> variants
) {
}

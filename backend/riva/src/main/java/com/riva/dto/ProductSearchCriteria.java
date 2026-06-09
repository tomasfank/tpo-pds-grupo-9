package com.riva.dto;

import java.math.BigDecimal;

import com.riva.model.product.Size;

public record ProductSearchCriteria(
        String name,
        String categoryId,
        Size size,
        String color,
        BigDecimal priceMin,
        BigDecimal priceMax
) {
}

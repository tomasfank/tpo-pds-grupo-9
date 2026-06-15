package com.riva.dto;

import java.math.BigDecimal;

import com.riva.model.cart.ItemCarrito;
import com.riva.model.product.ProductVariant;
import com.riva.model.product.Size;

public record ItemCarritoResponse(
        String id,
        String variantId,
        String productId,
        String productName,
        Size size,
        String color,
        int cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal,
        int stockDisponible,
        // CU-12 — false cuando el producto fue desactivado mientras estaba en el carrito.
        boolean disponible
) {
    public static ItemCarritoResponse from(ItemCarrito item, boolean disponible) {
        ProductVariant variante = item.getVariante();
        return new ItemCarritoResponse(
                item.getId(),
                variante.getId(),
                variante.getProductId(),
                variante.getProductName(),
                variante.getSize(),
                variante.getColor(),
                item.getCantidad(),
                variante.getPrecioUnitario(),
                item.subtotalDecimal(),
                variante.getStock(),
                disponible
        );
    }
}

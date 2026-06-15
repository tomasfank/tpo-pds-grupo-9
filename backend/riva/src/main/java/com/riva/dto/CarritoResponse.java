package com.riva.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import com.riva.model.cart.Carrito;

public record CarritoResponse(
        String id,
        String clienteId,
        List<ItemCarritoResponse> items,
        BigDecimal total
) {
    // CU-12 — los items cuyo producto figure en productosInactivos se marcan como
    // no disponibles para avisar al cliente al refrescar el carrito.
    public static CarritoResponse from(Carrito carrito, Set<String> productosInactivos) {
        return new CarritoResponse(
                carrito.getId(),
                carrito.getClienteId(),
                carrito.getItems().stream()
                        .map(item -> ItemCarritoResponse.from(
                                item,
                                !productosInactivos.contains(item.getVariante().getProductId())))
                        .toList(),
                carrito.calcularTotalDecimal()
        );
    }
}

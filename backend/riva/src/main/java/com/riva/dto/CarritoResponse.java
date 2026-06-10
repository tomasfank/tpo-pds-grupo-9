package com.riva.dto;

import java.math.BigDecimal;
import java.util.List;

import com.riva.model.cart.Carrito;

public record CarritoResponse(
        String id,
        String clienteId,
        List<ItemCarritoResponse> items,
        BigDecimal total
) {
    public static CarritoResponse from(Carrito carrito) {
        return new CarritoResponse(
                carrito.getId(),
                carrito.getClienteId(),
                carrito.getItems().stream().map(ItemCarritoResponse::from).toList(),
                carrito.calcularTotalDecimal()
        );
    }
}

package com.riva.dto;

import java.math.BigDecimal;

import com.riva.model.pedido.ItemPedido;

public record ItemPedidoResponse(
        String id,
        String varianteId,
        String productoId,
        String productoNombre,
        String talla,
        String color,
        int cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) {

    public static ItemPedidoResponse from(ItemPedido item) {
        return new ItemPedidoResponse(
                item.getId(),
                item.getVarianteId(),
                item.getProductoId(),
                item.getProductoNombre(),
                item.getTalla(),
                item.getColor(),
                item.getCantidad(),
                item.getPrecioUnitario(),
                item.subtotalDecimal()
        );
    }
}

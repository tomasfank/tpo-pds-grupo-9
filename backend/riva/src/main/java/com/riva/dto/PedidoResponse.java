package com.riva.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.riva.model.pedido.Pedido;

public record PedidoResponse(
        String id,
        String clienteId,
        LocalDateTime fecha,
        BigDecimal total,
        String estado,
        List<ItemPedidoResponse> items,
        List<TransicionEstadoResponse> historialEstados,
        DireccionEnvioResponse direccionEnvio
) {

    public static PedidoResponse from(Pedido pedido) {
        return new PedidoResponse(
                pedido.getId(),
                pedido.getClienteId(),
                pedido.getFecha(),
                pedido.getTotal(),
                pedido.nombreEstadoActual(),
                pedido.getItems().stream().map(ItemPedidoResponse::from).toList(),
                pedido.getHistorialEstados().stream().map(TransicionEstadoResponse::from).toList(),
                DireccionEnvioResponse.from(pedido.getDireccionEnvio())
        );
    }
}

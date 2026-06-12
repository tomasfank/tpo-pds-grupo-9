package com.riva.dto;

public record ProcesarPagoResponse(
        boolean exito,
        String mensaje,
        PedidoResponse pedido
) {
}

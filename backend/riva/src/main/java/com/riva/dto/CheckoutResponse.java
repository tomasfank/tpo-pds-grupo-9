package com.riva.dto;

import java.math.BigDecimal;

// Resultado del checkout (Facade): pago + resumen de envio derivado del Singleton Configuracion.
public record CheckoutResponse(
        boolean exito,
        String mensaje,
        PedidoResponse pedido,
        BigDecimal costoEnvio,
        boolean envioGratis
) {
}

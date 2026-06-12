package com.riva.dto;

public record ProcesarPagoRequest(
        String metodo,
        String numeroTarjeta,
        String titular,
        String vencimiento,
        String cvv,
        String emailCuenta,
        String cbu,
        String alias,
        String banco
) {
}

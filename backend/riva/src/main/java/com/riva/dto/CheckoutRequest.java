package com.riva.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

// Entrada del checkout en un solo paso (patron Facade): datos de pago + direccion opcional.
public record CheckoutRequest(
        @NotNull @Valid ProcesarPagoRequest pago,
        DireccionEnvioRequest direccionEnvio
) {
}

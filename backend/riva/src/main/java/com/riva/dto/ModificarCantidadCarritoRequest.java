package com.riva.dto;

import jakarta.validation.constraints.Positive;

public record ModificarCantidadCarritoRequest(
        @Positive int cantidad
) {
}

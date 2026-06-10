package com.riva.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record AgregarItemCarritoRequest(
        @NotBlank String productId,
        @NotBlank String variantId,
        @Positive int cantidad
) {
}

package com.riva.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
        @NotBlank String actual,
        @NotBlank String nueva
) {
}

package com.riva.dto;

public record LoginResponse(
        String token,
        String rol,
        String nombre,
        String apellido,
        String email
) {
}

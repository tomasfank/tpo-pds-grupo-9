package com.riva.dto;

import com.riva.model.user.Usuario;

public record UsuarioResponse(
        String id,
        String nombre,
        String apellido,
        String email,
        String rol
) {
    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getEmail(),
                usuario.rol().name()
        );
    }
}

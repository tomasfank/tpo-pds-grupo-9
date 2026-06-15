package com.riva.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.riva.model.user.Rol;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService("un-secreto-de-prueba", 86_400_000L);

    @Test
    void generaYParseaUnTokenConSusClaims() {
        String token = jwtService.generarToken("user-1", "ana@riva.com", Rol.CLIENTE);

        Claims claims = jwtService.parse(token);

        assertThat(claims.getSubject()).isEqualTo("user-1");
        assertThat(claims.get("email", String.class)).isEqualTo("ana@riva.com");
        assertThat(claims.get("rol", String.class)).isEqualTo("CLIENTE");
    }

    @Test
    void rechazaUnTokenFirmadoConOtraClave() {
        String token = jwtService.generarToken("user-1", "ana@riva.com", Rol.CLIENTE);
        JwtService otro = new JwtService("otro-secreto-distinto", 86_400_000L);

        assertThatThrownBy(() -> otro.parse(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void rechazaUnTokenExpirado() {
        JwtService expirado = new JwtService("un-secreto-de-prueba", -1_000L);
        String token = expirado.generarToken("user-1", "ana@riva.com", Rol.CLIENTE);

        assertThatThrownBy(() -> expirado.parse(token)).isInstanceOf(JwtException.class);
    }
}

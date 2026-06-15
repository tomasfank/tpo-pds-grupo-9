package com.riva.model.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.riva.exception.ValidationException;

class UsuarioTest {

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    private Usuario usuarioCon(String rawPassword) {
        return new Cliente("Ana", "Diaz", "ANA@riva.com", encoder.encode(rawPassword));
    }

    @Test
    void normalizaElEmailAMinusculas() {
        Usuario usuario = usuarioCon("secreto12");
        assertThat(usuario.getEmail()).isEqualTo("ana@riva.com");
    }

    @Test
    void validarCredencialesReconoceLaPasswordCorrecta() {
        Usuario usuario = usuarioCon("secreto12");
        assertThat(usuario.validarCredenciales("secreto12", encoder)).isTrue();
        assertThat(usuario.validarCredenciales("incorrecta", encoder)).isFalse();
    }

    @Test
    void cambiarPasswordActualizaElHashCuandoLaActualEsCorrecta() {
        Usuario usuario = usuarioCon("secreto12");
        usuario.cambiarPassword("secreto12", "nuevaClave9", encoder);
        assertThat(usuario.validarCredenciales("nuevaClave9", encoder)).isTrue();
    }

    @Test
    void cambiarPasswordRechazaCuandoLaActualEsIncorrecta() {
        Usuario usuario = usuarioCon("secreto12");
        assertThatThrownBy(() -> usuario.cambiarPassword("mala", "nuevaClave9", encoder))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("contraseña actual");
    }

    @Test
    void cadaSubclaseExponeSuRol() {
        Usuario cliente = new Cliente("Ana", "Diaz", "ana@riva.com", encoder.encode("secreto12"));
        Usuario admin = new Administrador("Bob", "Ruiz", "bob@riva.com", encoder.encode("secreto12"));
        assertThat(cliente.rol()).isEqualTo(Rol.CLIENTE);
        assertThat(admin.rol()).isEqualTo(Rol.ADMINISTRADOR);
    }
}

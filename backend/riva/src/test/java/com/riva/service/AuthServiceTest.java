package com.riva.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.ArgumentCaptor;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.riva.dto.ChangePasswordRequest;
import com.riva.dto.LoginRequest;
import com.riva.dto.LoginResponse;
import com.riva.dto.RegisterRequest;
import com.riva.dto.UsuarioResponse;
import com.riva.exception.ConflictException;
import com.riva.exception.UnauthorizedException;
import com.riva.exception.ValidationException;
import com.riva.model.user.Cliente;
import com.riva.model.user.Usuario;
import com.riva.repository.UsuarioRepository;
import com.riva.security.JwtService;

class AuthServiceTest {

    private final UsuarioRepository repo = mock(UsuarioRepository.class);
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();
    private final JwtService jwtService = new JwtService("secreto-de-prueba", 86_400_000L);
    private final AuthService authService = new AuthService(repo, encoder, jwtService);

    @Test
    void registerCreaClienteYNoGuardaLaPasswordEnClaro() {
        when(repo.existsByEmail("ana@riva.com")).thenReturn(false);
        when(repo.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioResponse response = authService.register(
                new RegisterRequest("Ana", "Diaz", "ana@riva.com", "secreto12"));

        assertThat(response.rol()).isEqualTo("CLIENTE");
        assertThat(response.email()).isEqualTo("ana@riva.com");

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(repo).save(captor.capture());
        assertThat(captor.getValue().getPasswordHash()).isNotEqualTo("secreto12");
        assertThat(captor.getValue().getPasswordHash()).startsWith("$2");
    }

    @Test
    void registerRechazaEmailDuplicado() {
        when(repo.existsByEmail("ana@riva.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(
                new RegisterRequest("Ana", "Diaz", "ana@riva.com", "secreto12")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void registerRechazaPasswordDebil() {
        when(repo.existsByEmail("ana@riva.com")).thenReturn(false);

        assertThatThrownBy(() -> authService.register(
                new RegisterRequest("Ana", "Diaz", "ana@riva.com", "corta")))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void loginDevuelveTokenConCredencialesValidas() {
        Usuario cliente = new Cliente("Ana", "Diaz", "ana@riva.com", encoder.encode("secreto12"));
        when(repo.findByEmail("ana@riva.com")).thenReturn(Optional.of(cliente));

        LoginResponse response = authService.login(new LoginRequest("ana@riva.com", "secreto12"));

        assertThat(response.token()).isNotBlank();
        assertThat(response.rol()).isEqualTo("CLIENTE");
        assertThat(jwtService.parse(response.token()).get("rol", String.class)).isEqualTo("CLIENTE");
    }

    @Test
    void loginConPasswordIncorrectaLanzaErrorGenerico() {
        Usuario cliente = new Cliente("Ana", "Diaz", "ana@riva.com", encoder.encode("secreto12"));
        when(repo.findByEmail("ana@riva.com")).thenReturn(Optional.of(cliente));

        assertThatThrownBy(() -> authService.login(new LoginRequest("ana@riva.com", "incorrecta")))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Email o contraseña incorrectos");
    }

    @Test
    void loginConEmailInexistenteLanzaElMismoErrorGenerico() {
        when(repo.findByEmail("nadie@riva.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("nadie@riva.com", "secreto12")))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Email o contraseña incorrectos");
    }

    @Test
    void cambiarPasswordRechazaActualIncorrecta() {
        Usuario cliente = new Cliente("Ana", "Diaz", "ana@riva.com", encoder.encode("secreto12"));
        when(repo.findById("user-1")).thenReturn(Optional.of(cliente));

        assertThatThrownBy(() -> authService.cambiarPassword(
                "user-1", new ChangePasswordRequest("mala", "nuevaClave9")))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("contraseña actual");
    }
}

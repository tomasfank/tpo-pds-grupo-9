package com.riva.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.riva.dto.NotificationPreferencesRequest;
import com.riva.dto.NotificationPreferencesResponse;
import com.riva.exception.NotFoundException;
import com.riva.exception.ValidationException;
import com.riva.model.user.Administrador;
import com.riva.model.user.Cliente;
import com.riva.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void obtenerPreferenciasDevuelveLosCanalesActualesDelCliente() {
        Cliente cliente = new Cliente("Cliente", "Demo", "cliente@riva.com", "hash");
        when(usuarioRepository.findById("cliente-1")).thenReturn(Optional.of(cliente));

        NotificationPreferencesResponse response = notificationService.obtenerPreferencias("cliente-1");

        // Default: todos los canales activos al registrarse.
        assertThat(response.email()).isTrue();
        assertThat(response.sms()).isTrue();
        assertThat(response.push()).isTrue();
    }

    @Test
    void actualizarPreferenciasPersisteLosCanalesElegidos() {
        Cliente cliente = new Cliente("Cliente", "Demo", "cliente@riva.com", "hash");
        when(usuarioRepository.findById("cliente-1")).thenReturn(Optional.of(cliente));
        when(usuarioRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NotificationPreferencesResponse response = notificationService.actualizarPreferencias(
                "cliente-1", new NotificationPreferencesRequest(true, false, false));

        assertThat(response.email()).isTrue();
        assertThat(response.sms()).isFalse();
        assertThat(response.push()).isFalse();
        assertThat(cliente.getPreferencias().isSms()).isFalse();
        verify(usuarioRepository).save(cliente);
    }

    @Test
    void actualizarPreferenciasPermiteDesactivarTodosLosCanales() {
        Cliente cliente = new Cliente("Cliente", "Demo", "cliente@riva.com", "hash");
        when(usuarioRepository.findById("cliente-1")).thenReturn(Optional.of(cliente));
        when(usuarioRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NotificationPreferencesResponse response = notificationService.actualizarPreferencias(
                "cliente-1", new NotificationPreferencesRequest(false, false, false));

        // El backend permite los tres en false (la advertencia del flujo 3a es del front).
        assertThat(response.email()).isFalse();
        assertThat(response.sms()).isFalse();
        assertThat(response.push()).isFalse();
    }

    @Test
    void preferenciasRechazaUsuarioQueNoEsCliente() {
        Administrador admin = new Administrador("Admin", "RIVA", "admin@riva.com", "hash");
        when(usuarioRepository.findById("admin-1")).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> notificationService.obtenerPreferencias("admin-1"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Solo los clientes");
    }

    @Test
    void preferenciasRechazaUsuarioInexistente() {
        when(usuarioRepository.findById("fantasma")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.obtenerPreferencias("fantasma"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Usuario no encontrado");
    }
}

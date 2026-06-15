package com.riva.service;

import org.springframework.stereotype.Service;

import com.riva.dto.NotificationPreferencesRequest;
import com.riva.dto.NotificationPreferencesResponse;
import com.riva.exception.NotFoundException;
import com.riva.exception.ValidationException;
import com.riva.model.user.Cliente;
import com.riva.model.user.Usuario;
import com.riva.repository.UsuarioRepository;

// CU-24 — Configurar Canales de Notificacion. Persiste las preferencias del
// cliente; la suscripcion efectiva al pedido (patron Observer) se deriva de
// estas preferencias en PedidoService al momento de notificar.
@Service
public class NotificationService {

    private final UsuarioRepository usuarioRepository;

    public NotificationService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public NotificationPreferencesResponse obtenerPreferencias(String userId) {
        Cliente cliente = requireCliente(userId);
        return NotificationPreferencesResponse.from(cliente.getPreferencias());
    }

    public NotificationPreferencesResponse actualizarPreferencias(
            String userId, NotificationPreferencesRequest request) {
        Cliente cliente = requireCliente(userId);
        cliente.configurarNotificaciones(request.email(), request.sms(), request.push());
        usuarioRepository.save(cliente);
        return NotificationPreferencesResponse.from(cliente.getPreferencias());
    }

    private Cliente requireCliente(String userId) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        if (!(usuario instanceof Cliente cliente)) {
            throw new ValidationException("Solo los clientes tienen preferencias de notificacion");
        }
        return cliente;
    }
}

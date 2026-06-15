package com.riva.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.riva.dto.NotificationPreferencesRequest;
import com.riva.dto.NotificationPreferencesResponse;
import com.riva.security.UsuarioPrincipal;
import com.riva.service.NotificationService;

import jakarta.validation.Valid;

// CU-24 — Configurar Canales de Notificacion (solo Cliente autenticado).
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/preferences")
    public NotificationPreferencesResponse obtener(
            @AuthenticationPrincipal UsuarioPrincipal principal) {
        return notificationService.obtenerPreferencias(principal.userId());
    }

    @PutMapping("/preferences")
    public NotificationPreferencesResponse actualizar(
            @AuthenticationPrincipal UsuarioPrincipal principal,
            @Valid @RequestBody NotificationPreferencesRequest request) {
        return notificationService.actualizarPreferencias(principal.userId(), request);
    }
}

package com.riva.dto;

import com.riva.pattern.notification.PreferenciasNotificacion;

// CU-24 — estado actual de los canales de notificacion del cliente.
public record NotificationPreferencesResponse(
        boolean email,
        boolean sms,
        boolean push
) {
    public static NotificationPreferencesResponse from(PreferenciasNotificacion preferencias) {
        return new NotificationPreferencesResponse(
                preferencias.isEmail(),
                preferencias.isSms(),
                preferencias.isPush()
        );
    }
}

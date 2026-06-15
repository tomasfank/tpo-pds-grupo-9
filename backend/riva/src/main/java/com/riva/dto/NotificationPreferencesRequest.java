package com.riva.dto;

import jakarta.validation.constraints.NotNull;

// CU-24 — canales que el cliente desea recibir (Email, SMS, Push).
public record NotificationPreferencesRequest(
        @NotNull Boolean email,
        @NotNull Boolean sms,
        @NotNull Boolean push
) {
}

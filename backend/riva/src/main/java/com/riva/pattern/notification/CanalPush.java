package com.riva.pattern.notification;

import com.riva.model.pedido.Pedido;

public class CanalPush implements CanalNotificacion {

    private final String deviceToken;

    public CanalPush(String deviceToken) {
        if (deviceToken == null || deviceToken.isBlank()) {
            throw new IllegalArgumentException("deviceToken es obligatorio");
        }
        this.deviceToken = deviceToken;
    }

    @Override
    public void actualizar(Pedido pedido) {
        if (pedido == null) {
            throw new IllegalArgumentException("pedido es obligatorio");
        }
        // Simula el envio de una notificacion push al cliente.
        System.out.printf(
                "PUSH a %s: el pedido %s cambio al estado %s%n",
                deviceToken,
                pedido.getId(),
                pedido.nombreEstadoActual()
        );
    }

    public String getDeviceToken() {
        return deviceToken;
    }
}

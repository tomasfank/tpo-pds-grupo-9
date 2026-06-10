package com.riva.pattern.notification;

import com.riva.model.pedido.Pedido;

public class CanalSMS implements CanalNotificacion {

    private final String numeroTelefono;

    public CanalSMS(String numeroTelefono) {
        if (numeroTelefono == null || numeroTelefono.isBlank()) {
            throw new IllegalArgumentException("numeroTelefono es obligatorio");
        }
        this.numeroTelefono = numeroTelefono;
    }

    @Override
    public void actualizar(Pedido pedido) {
        if (pedido == null) {
            throw new IllegalArgumentException("pedido es obligatorio");
        }
        // Simula el envio de un SMS al cliente.
        System.out.printf(
                "SMS a %s: el pedido %s cambio al estado %s%n",
                numeroTelefono,
                pedido.getId(),
                pedido.nombreEstadoActual()
        );
    }

    public String getNumeroTelefono() {
        return numeroTelefono;
    }
}

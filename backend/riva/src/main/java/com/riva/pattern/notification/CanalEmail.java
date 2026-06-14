package com.riva.pattern.notification;

import com.riva.model.pedido.Pedido;

public class CanalEmail implements CanalNotificacion {

    private final String email;

    public CanalEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email es obligatorio");
        }
        this.email = email;
    }

    @Override
    public void actualizar(Pedido pedido) {
        if (pedido == null) {
            throw new IllegalArgumentException("pedido es obligatorio");
        }
        // Simula el envio de un email al cliente.
        System.out.printf(
                "EMAIL a %s: el pedido %s cambio al estado %s%n",
                email,
                pedido.getId(),
                pedido.nombreEstadoActual()
        );
    }

    public String getEmail() {
        return email;
    }
}

package com.riva.model.pedido;

import java.time.LocalDateTime;

public class TransicionEstado {

    private LocalDateTime fecha;
    private String estado;

    protected TransicionEstado() {
        // requerido por Spring Data
    }

    public TransicionEstado(LocalDateTime fecha, String estado) {
        if (fecha == null) {
            throw new IllegalArgumentException("fecha es obligatoria");
        }
        if (estado == null || estado.isBlank()) {
            throw new IllegalArgumentException("estado es obligatorio");
        }
        this.fecha = fecha;
        this.estado = estado;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getEstado() {
        return estado;
    }
}

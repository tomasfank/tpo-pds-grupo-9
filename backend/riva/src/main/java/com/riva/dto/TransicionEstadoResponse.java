package com.riva.dto;

import java.time.LocalDateTime;

import com.riva.model.pedido.TransicionEstado;

public record TransicionEstadoResponse(
        LocalDateTime fecha,
        String estado
) {

    public static TransicionEstadoResponse from(TransicionEstado transicion) {
        return new TransicionEstadoResponse(transicion.getFecha(), transicion.getEstado());
    }
}

package com.riva.dto;

import com.riva.model.pedido.DireccionEnvio;

public record CreatePedidoRequest(DireccionEnvioRequest direccionEnvio) {

    public DireccionEnvio direccionEnvioModel() {
        return direccionEnvio == null ? null : direccionEnvio.toModel();
    }
}

package com.riva.dto;

import com.riva.model.pedido.DireccionEnvio;

public record DireccionEnvioRequest(
        String calle,
        String numero,
        String ciudad,
        String provincia,
        String codigoPostal
) {

    public DireccionEnvio toModel() {
        return new DireccionEnvio(calle, numero, ciudad, provincia, codigoPostal);
    }
}

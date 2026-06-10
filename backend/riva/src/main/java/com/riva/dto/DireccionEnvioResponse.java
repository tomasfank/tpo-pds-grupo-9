package com.riva.dto;

import com.riva.model.pedido.DireccionEnvio;

public record DireccionEnvioResponse(
        String calle,
        String numero,
        String ciudad,
        String provincia,
        String codigoPostal
) {

    public static DireccionEnvioResponse from(DireccionEnvio direccion) {
        if (direccion == null) {
            return null;
        }
        return new DireccionEnvioResponse(
                direccion.getCalle(),
                direccion.getNumero(),
                direccion.getCiudad(),
                direccion.getProvincia(),
                direccion.getCodigoPostal()
        );
    }
}

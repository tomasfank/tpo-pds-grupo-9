package com.riva.dto;

import com.riva.config.Configuracion;

// Expone los parametros del Singleton Configuracion al frontend.
public record ConfigResponse(
        String moneda,
        double tasaIva,
        double costoEnvio,
        double umbralEnvioGratis
) {
    public static ConfigResponse from(Configuracion configuracion) {
        return new ConfigResponse(
                configuracion.getMoneda(),
                configuracion.getTasaIva(),
                configuracion.getCostoEnvio(),
                configuracion.getUmbralEnvioGratis()
        );
    }
}

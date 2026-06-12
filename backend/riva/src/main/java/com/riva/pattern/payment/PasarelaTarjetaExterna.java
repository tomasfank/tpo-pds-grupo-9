package com.riva.pattern.payment;

public class PasarelaTarjetaExterna {

    public RespuestaTarjeta autorizarImporte(String numeroTarjeta, double importe) {
        if (importe <= 0) {
            return new RespuestaTarjeta(false, "Importe invalido");
        }
        if (numeroTarjeta != null && numeroTarjeta.endsWith("0000")) {
            return new RespuestaTarjeta(false, "Pago rechazado por tarjeta");
        }
        return new RespuestaTarjeta(true, "Pago aprobado");
    }
}

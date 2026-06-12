package com.riva.pattern.payment;

public class AdapterTarjeta implements AdapterPagoExterno {

    private final PasarelaTarjetaExterna pasarela;

    public AdapterTarjeta(PasarelaTarjetaExterna pasarela) {
        if (pasarela == null) {
            throw new IllegalArgumentException("pasarela es obligatoria");
        }
        this.pasarela = pasarela;
    }

    @Override
    public ResultadoPago procesar(double monto) {
        return procesar(monto, null);
    }

    public ResultadoPago procesar(double monto, String numeroTarjeta) {
        RespuestaTarjeta respuesta = pasarela.autorizarImporte(numeroTarjeta, monto);
        return new ResultadoPago(respuesta.aprobada(), respuesta.mensaje());
    }
}

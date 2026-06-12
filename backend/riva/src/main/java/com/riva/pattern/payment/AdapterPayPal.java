package com.riva.pattern.payment;

public class AdapterPayPal implements AdapterPagoExterno {

    private final ServicioPayPalExterno servicio;

    public AdapterPayPal(ServicioPayPalExterno servicio) {
        if (servicio == null) {
            throw new IllegalArgumentException("servicio es obligatorio");
        }
        this.servicio = servicio;
    }

    @Override
    public ResultadoPago procesar(double monto) {
        return procesar(monto, null);
    }

    public ResultadoPago procesar(double monto, String emailCuenta) {
        RespuestaPayPal respuesta = servicio.enviarPago(emailCuenta, monto);
        return new ResultadoPago(respuesta.aprobada(), respuesta.mensaje());
    }
}

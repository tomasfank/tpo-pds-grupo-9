package com.riva.pattern.payment;

public class ServicioPayPalExterno {

    public RespuestaPayPal enviarPago(String emailCuenta, double total) {
        if (total <= 0) {
            return new RespuestaPayPal(false, "Importe invalido");
        }
        if (emailCuenta != null && emailCuenta.toLowerCase().endsWith("@rechazado.test")) {
            return new RespuestaPayPal(false, "Pago rechazado por PayPal");
        }
        return new RespuestaPayPal(true, "Pago aprobado");
    }
}

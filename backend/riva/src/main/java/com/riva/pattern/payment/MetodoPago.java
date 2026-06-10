package com.riva.pattern.payment;

public interface MetodoPago {

    ResultadoPago procesarPago(double monto);

    boolean validarDatosPago();
}

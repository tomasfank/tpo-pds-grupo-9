package com.riva.pattern.payment;

public class PagoPayPal implements MetodoPago {

    private final String emailCuenta;
    private final AdapterPayPal adapter;

    public PagoPayPal(String emailCuenta, AdapterPayPal adapter) {
        this.emailCuenta = emailCuenta == null ? "" : emailCuenta.trim();
        if (adapter == null) {
            throw new IllegalArgumentException("adapter es obligatorio");
        }
        this.adapter = adapter;
    }

    @Override
    public ResultadoPago procesarPago(double monto) {
        return adapter.procesar(monto, emailCuenta);
    }

    @Override
    public boolean validarDatosPago() {
        return emailCuenta.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    }

    public String nombre() {
        return "PayPal";
    }
}

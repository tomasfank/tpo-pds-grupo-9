package com.riva.pattern.payment;

public class PagoTarjeta implements MetodoPago {

    private final String numeroTarjeta;
    private final String titular;
    private final String vencimiento;
    private final String cvv;
    private final AdapterTarjeta adapter;

    public PagoTarjeta(String numeroTarjeta, String titular, String vencimiento, String cvv, AdapterTarjeta adapter) {
        this.numeroTarjeta = normalizar(numeroTarjeta);
        this.titular = titular == null ? "" : titular.trim();
        this.vencimiento = vencimiento == null ? "" : vencimiento.trim();
        this.cvv = normalizar(cvv);
        if (adapter == null) {
            throw new IllegalArgumentException("adapter es obligatorio");
        }
        this.adapter = adapter;
    }

    @Override
    public ResultadoPago procesarPago(double monto) {
        return adapter.procesar(monto, numeroTarjeta);
    }

    @Override
    public boolean validarDatosPago() {
        return !titular.isBlank()
                && numeroTarjeta.matches("\\d{13,19}")
                && vencimiento.matches("(0[1-9]|1[0-2])/\\d{2}")
                && cvv.matches("\\d{3,4}");
    }

    public String nombre() {
        return "Tarjeta";
    }

    private String normalizar(String valor) {
        return valor == null ? "" : valor.replaceAll("\\s+", "");
    }
}

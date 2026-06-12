package com.riva.pattern.payment;

public class PagoTransferencia implements MetodoPago {

    private final String cbu;
    private final String alias;
    private final String banco;
    private final AdapterTransferencia adapter;

    public PagoTransferencia(String cbu, String alias, String banco, AdapterTransferencia adapter) {
        this.cbu = cbu == null ? "" : cbu.replaceAll("\\s+", "");
        this.alias = alias == null ? "" : alias.trim();
        this.banco = banco == null ? "" : banco.trim();
        if (adapter == null) {
            throw new IllegalArgumentException("adapter es obligatorio");
        }
        this.adapter = adapter;
    }

    @Override
    public ResultadoPago procesarPago(double monto) {
        return adapter.procesar(monto, cbu.isBlank() ? null : cbu, alias.isBlank() ? null : alias);
    }

    @Override
    public boolean validarDatosPago() {
        boolean cbuValido = cbu.matches("\\d{22}");
        boolean aliasValido = alias.length() >= 6;
        return !banco.isBlank() && (cbuValido || aliasValido);
    }

    public String nombre() {
        return "Transferencia";
    }
}

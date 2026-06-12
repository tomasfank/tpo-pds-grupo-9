package com.riva.pattern.payment;

public class ServicioBancoExterno {

    public RespuestaBanco registrarTransferencia(String cbu, String alias, double importe) {
        if (importe <= 0) {
            return new RespuestaBanco(false, "Importe invalido");
        }
        if ("rechazada".equalsIgnoreCase(alias) || (cbu != null && cbu.endsWith("0000"))) {
            return new RespuestaBanco(false, "Transferencia rechazada");
        }
        return new RespuestaBanco(true, "Pago aprobado");
    }
}

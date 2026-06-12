package com.riva.pattern.payment;

public class AdapterTransferencia implements AdapterPagoExterno {

    private final ServicioBancoExterno servicio;

    public AdapterTransferencia(ServicioBancoExterno servicio) {
        if (servicio == null) {
            throw new IllegalArgumentException("servicio es obligatorio");
        }
        this.servicio = servicio;
    }

    @Override
    public ResultadoPago procesar(double monto) {
        return procesar(monto, null, null);
    }

    public ResultadoPago procesar(double monto, String cbu, String alias) {
        RespuestaBanco respuesta = servicio.registrarTransferencia(cbu, alias, monto);
        return new ResultadoPago(respuesta.aprobada(), respuesta.mensaje());
    }
}

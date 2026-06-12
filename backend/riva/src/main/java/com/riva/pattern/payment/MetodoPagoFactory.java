package com.riva.pattern.payment;

import org.springframework.stereotype.Component;

import com.riva.dto.ProcesarPagoRequest;
import com.riva.exception.ValidationException;

@Component
public class MetodoPagoFactory {

    public MetodoPago crearDesde(ProcesarPagoRequest request) {
        if (request == null || request.metodo() == null || request.metodo().isBlank()) {
            throw new ValidationException("metodo de pago es obligatorio");
        }
        return switch (request.metodo().trim().toUpperCase()) {
            case "TARJETA" -> new PagoTarjeta(
                    request.numeroTarjeta(),
                    request.titular(),
                    request.vencimiento(),
                    request.cvv(),
                    new AdapterTarjeta(new PasarelaTarjetaExterna())
            );
            case "PAYPAL" -> new PagoPayPal(
                    request.emailCuenta(),
                    new AdapterPayPal(new ServicioPayPalExterno())
            );
            case "TRANSFERENCIA" -> new PagoTransferencia(
                    request.cbu(),
                    request.alias(),
                    request.banco(),
                    new AdapterTransferencia(new ServicioBancoExterno())
            );
            default -> throw new ValidationException("Metodo de pago no soportado: " + request.metodo());
        };
    }

    public String nombreMetodo(ProcesarPagoRequest request) {
        if (request == null || request.metodo() == null) {
            throw new ValidationException("metodo de pago es obligatorio");
        }
        return switch (request.metodo().trim().toUpperCase()) {
            case "TARJETA" -> "Tarjeta";
            case "PAYPAL" -> "PayPal";
            case "TRANSFERENCIA" -> "Transferencia";
            default -> throw new ValidationException("Metodo de pago no soportado: " + request.metodo());
        };
    }
}

package com.riva.pattern.state;

public final class EstadoPedidoFactory {

    private EstadoPedidoFactory() {
    }

    public static EstadoPedido desdeNombre(String nombre) {
        return switch (nombre) {
            case "Pendiente" -> new EstadoPendiente();
            case "Pagado" -> new EstadoPagado();
            case "Enviado" -> new EstadoEnviado();
            case "Entregado" -> new EstadoEntregado();
            default -> throw new IllegalStateException("Estado de pedido invalido: " + nombre);
        };
    }
}

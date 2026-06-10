package com.riva.pattern.state;

import com.riva.model.pedido.Pedido;

public class EstadoEntregado implements EstadoPedido {

    @Override
    public void avanzar(Pedido pedido) {
        throw new IllegalStateException("El pedido ya esta en estado final");
    }

    @Override
    public boolean puedeAvanzar() {
        return false;
    }

    @Override
    public String nombre() {
        return "Entregado";
    }
}

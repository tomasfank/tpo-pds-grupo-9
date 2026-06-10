package com.riva.pattern.state;

import com.riva.model.pedido.Pedido;

public class EstadoPendiente implements EstadoPedido {

    @Override
    public void avanzar(Pedido pedido) {
        pedido.setEstado(new EstadoPagado());
    }

    @Override
    public boolean puedeAvanzar() {
        return true;
    }

    @Override
    public String nombre() {
        return "Pendiente";
    }
}

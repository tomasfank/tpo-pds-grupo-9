package com.riva.pattern.state;

import com.riva.model.pedido.Pedido;

public class EstadoPagado implements EstadoPedido {

    @Override
    public void avanzar(Pedido pedido) {
        pedido.setEstado(new EstadoEnviado());
    }

    @Override
    public boolean puedeAvanzar() {
        return true;
    }

    @Override
    public String nombre() {
        return "Pagado";
    }
}

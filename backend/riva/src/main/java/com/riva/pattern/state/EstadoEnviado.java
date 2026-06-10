package com.riva.pattern.state;

import com.riva.model.pedido.Pedido;

public class EstadoEnviado implements EstadoPedido {

    @Override
    public void avanzar(Pedido pedido) {
        pedido.setEstado(new EstadoEntregado());
    }

    @Override
    public boolean puedeAvanzar() {
        return true;
    }

    @Override
    public String nombre() {
        return "Enviado";
    }
}

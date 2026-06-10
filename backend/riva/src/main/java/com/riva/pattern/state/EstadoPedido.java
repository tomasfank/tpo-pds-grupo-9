package com.riva.pattern.state;

import com.riva.model.pedido.Pedido;

public interface EstadoPedido {

    void avanzar(Pedido pedido);

    boolean puedeAvanzar();

    String nombre();
}

package com.riva.pattern.notification;

import com.riva.model.pedido.Pedido;

public interface CanalNotificacion {

    void actualizar(Pedido pedido);
}

package com.riva.pattern.notification;

public interface SujetoObservable {
    void suscribir(CanalNotificacion canal);
    void desuscribir(CanalNotificacion canal);
    void notificar();
}

package com.riva.model.user;

import org.springframework.data.annotation.TypeAlias;

import com.riva.pattern.notification.PreferenciasNotificacion;

@TypeAlias("cliente")
public class Cliente extends Usuario {

    private PreferenciasNotificacion preferencias;

    protected Cliente() {
        // requerido por Spring Data
    }

    public Cliente(String nombre, String apellido, String email, String passwordHash) {
        super(nombre, apellido, email, passwordHash);
        this.preferencias = new PreferenciasNotificacion(true, true, true);
    }

    @Override
    public Rol rol() {
        return Rol.CLIENTE;
    }

    public PreferenciasNotificacion getPreferencias() {
        return preferencias;
    }
}

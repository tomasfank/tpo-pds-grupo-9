package com.riva.model.user;

import org.springframework.data.annotation.TypeAlias;

@TypeAlias("administrador")
public class Administrador extends Usuario {

    protected Administrador() {
        // requerido por Spring Data
    }

    public Administrador(String nombre, String apellido, String email, String passwordHash) {
        super(nombre, apellido, email, passwordHash);
    }

    @Override
    public Rol rol() {
        return Rol.ADMINISTRADOR;
    }
}

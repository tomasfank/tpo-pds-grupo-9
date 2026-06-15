package com.riva.model.user;

import com.riva.exception.ValidationException;

public final class PasswordPolicy {

    private static final int LONGITUD_MINIMA = 8;

    private PasswordPolicy() {
    }

    public static void validar(String password) {
        if (password == null || password.length() < LONGITUD_MINIMA) {
            throw new ValidationException(
                    "La contraseña debe tener al menos " + LONGITUD_MINIMA + " caracteres");
        }
    }
}

package com.riva.model.user;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.riva.exception.ValidationException;

@Document(collection = "usuarios")
public abstract class Usuario {

    @Id
    private String id;
    private String nombre;
    private String apellido;

    @Indexed(unique = true)
    private String email;
    private String passwordHash;

    protected Usuario() {
        // requerido por Spring Data
    }

    protected Usuario(String nombre, String apellido, String email, String passwordHash) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("nombre es obligatorio");
        }
        if (apellido == null || apellido.isBlank()) {
            throw new IllegalArgumentException("apellido es obligatorio");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email es obligatorio");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("passwordHash es obligatorio");
        }
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email.toLowerCase();
        this.passwordHash = passwordHash;
    }

    public boolean validarCredenciales(String rawPassword, PasswordEncoder encoder) {
        return rawPassword != null && encoder.matches(rawPassword, passwordHash);
    }

    public void cambiarPassword(String actual, String nueva, PasswordEncoder encoder) {
        if (!validarCredenciales(actual, encoder)) {
            throw new ValidationException("La contraseña actual es incorrecta");
        }
        PasswordPolicy.validar(nueva);
        this.passwordHash = encoder.encode(nueva);
    }

    public abstract Rol rol();

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getEmail() {
        return email;
    }

    @JsonIgnore
    public String getPasswordHash() {
        return passwordHash;
    }
}

package com.riva.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.riva.model.user.Rol;
import com.riva.model.user.Usuario;

public class UsuarioPrincipal implements UserDetails {

    private final String userId;
    private final String email;
    private final String passwordHash;
    private final Rol rol;

    public UsuarioPrincipal(String userId, String email, String passwordHash, Rol rol) {
        this.userId = userId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.rol = rol;
    }

    public static UsuarioPrincipal from(Usuario usuario) {
        return new UsuarioPrincipal(
                usuario.getId(), usuario.getEmail(), usuario.getPasswordHash(), usuario.rol());
    }

    public String userId() {
        return userId;
    }

    public Rol rol() {
        return rol;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

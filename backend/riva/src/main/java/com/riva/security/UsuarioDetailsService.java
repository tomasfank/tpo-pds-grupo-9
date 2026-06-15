package com.riva.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.riva.model.user.Usuario;
import com.riva.repository.UsuarioRepository;

// Bean UserDetailsService de la aplicacion. El JwtAuthenticationFilter es stateless y arma el
// principal desde los claims del token, por lo que no invoca este servicio en el flujo normal.
// Se mantiene porque: (1) provee el bean UserDetailsService, evitando el usuario por defecto que
// Spring Boot genera (con password aleatoria en el log) cuando no hay ninguno; (2) queda disponible
// para un eventual flujo basado en AuthenticationManager/DaoAuthenticationProvider.
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        return UsuarioPrincipal.from(usuario);
    }
}

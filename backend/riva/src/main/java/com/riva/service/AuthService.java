package com.riva.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.riva.dto.ChangePasswordRequest;
import com.riva.dto.LoginRequest;
import com.riva.dto.LoginResponse;
import com.riva.dto.RegisterRequest;
import com.riva.dto.UsuarioResponse;
import com.riva.exception.ConflictException;
import com.riva.exception.NotFoundException;
import com.riva.exception.UnauthorizedException;
import com.riva.model.user.Cliente;
import com.riva.model.user.PasswordPolicy;
import com.riva.model.user.Usuario;
import com.riva.repository.UsuarioRepository;
import com.riva.security.JwtService;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public UsuarioResponse register(RegisterRequest request) {
        String email = request.email().toLowerCase();
        if (usuarioRepository.existsByEmail(email)) {
            throw new ConflictException("El email ya está registrado");
        }
        PasswordPolicy.validar(request.password());
        Cliente cliente = new Cliente(
                request.nombre(),
                request.apellido(),
                email,
                passwordEncoder.encode(request.password()));
        return UsuarioResponse.from(usuarioRepository.save(cliente));
    }

    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("Email o contraseña incorrectos"));
        if (!usuario.validarCredenciales(request.password(), passwordEncoder)) {
            throw new UnauthorizedException("Email o contraseña incorrectos");
        }
        String token = jwtService.generarToken(usuario.getId(), usuario.getEmail(), usuario.rol());
        return new LoginResponse(
                token,
                usuario.rol().name(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getEmail());
    }

    public void cambiarPassword(String userId, ChangePasswordRequest request) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        usuario.cambiarPassword(request.actual(), request.nueva(), passwordEncoder);
        usuarioRepository.save(usuario);
    }
}

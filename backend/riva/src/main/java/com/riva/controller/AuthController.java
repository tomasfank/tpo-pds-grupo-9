package com.riva.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.riva.dto.ChangePasswordRequest;
import com.riva.dto.LoginRequest;
import com.riva.dto.LoginResponse;
import com.riva.dto.RegisterRequest;
import com.riva.dto.UsuarioResponse;
import com.riva.security.UsuarioPrincipal;
import com.riva.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout() {
        // JWT stateless: no hay sesión server-side que invalidar.
        // El cliente descarta el token; la expiración corta cubre el resto.
    }

    @PostMapping("/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cambiarPassword(
            @AuthenticationPrincipal UsuarioPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.cambiarPassword(principal.userId(), request);
    }
}

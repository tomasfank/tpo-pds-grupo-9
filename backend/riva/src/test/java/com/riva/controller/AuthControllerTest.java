package com.riva.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import com.riva.dto.ChangePasswordRequest;
import com.riva.dto.LoginRequest;
import com.riva.dto.LoginResponse;
import com.riva.dto.RegisterRequest;
import com.riva.dto.UsuarioResponse;
import com.riva.model.user.Rol;
import com.riva.security.UsuarioPrincipal;
import com.riva.service.AuthService;

class AuthControllerTest {

    private final AuthService authService = mock(AuthService.class);
    private final AuthController controller = new AuthController(authService);

    @Test
    void registerDelegaEnElService() {
        RegisterRequest request = new RegisterRequest("Ana", "Diaz", "ana@riva.com", "secreto12");
        UsuarioResponse expected = new UsuarioResponse("u1", "Ana", "Diaz", "ana@riva.com", "CLIENTE");
        when(authService.register(request)).thenReturn(expected);

        UsuarioResponse response = controller.register(request);

        assertThat(response).isEqualTo(expected);
        verify(authService).register(request);
    }

    @Test
    void loginDelegaEnElService() {
        LoginRequest request = new LoginRequest("ana@riva.com", "secreto12");
        LoginResponse expected = new LoginResponse("tok", "CLIENTE", "Ana", "Diaz", "ana@riva.com");
        when(authService.login(request)).thenReturn(expected);

        LoginResponse response = controller.login(request);

        assertThat(response.token()).isEqualTo("tok");
        verify(authService).login(request);
    }

    @Test
    void cambiarPasswordUsaElUserIdDelPrincipal() {
        UsuarioPrincipal principal = new UsuarioPrincipal("u1", "ana@riva.com", "hash", Rol.CLIENTE);
        ChangePasswordRequest request = new ChangePasswordRequest("secreto12", "nuevaClave9");

        controller.cambiarPassword(principal, request);

        verify(authService).cambiarPassword("u1", request);
    }
}

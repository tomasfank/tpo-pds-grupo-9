package com.riva.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.riva.model.user.Rol;

class JwtAuthenticationFilterTest {

    private final JwtService jwtService = new JwtService("secreto-de-prueba-filter", 86_400_000L);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void tokenValidoPueblaElPrincipalDesdeLosClaims() throws Exception {
        String token = jwtService.generarToken("user-42", "ana@riva.com", Rol.ADMINISTRADOR);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        UsuarioPrincipal principal = (UsuarioPrincipal) auth.getPrincipal();
        assertThat(principal.userId()).isEqualTo("user-42");
        assertThat(principal.getUsername()).isEqualTo("ana@riva.com");
        assertThat(principal.rol()).isEqualTo(Rol.ADMINISTRADOR);
        assertThat(auth.getAuthorities()).extracting("authority").containsExactly("ROLE_ADMINISTRADOR");
    }

    @Test
    void sinHeaderNoAutentica() throws Exception {
        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void tokenInvalidoNoAutentica() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer no.es.un.token");

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}

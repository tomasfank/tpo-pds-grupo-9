package com.riva.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.StaticWebApplicationContext;

import com.riva.config.SecurityConfig;
import com.riva.dto.ProductSearchCriteria;
import com.riva.model.user.Rol;
import com.riva.security.JwtAuthenticationFilter;
import com.riva.security.JwtService;
import com.riva.service.ProductService;

/**
 * Verifica las reglas de Spring Security: GET público devuelve 200, POST sin token devuelve 401,
 * POST con rol Cliente devuelve 403, POST con rol Administrador pasa la seguridad (400 por
 * validación de @Valid en CreateProductRequest, NO por bloqueo de autorización).
 *
 * Test standalone (sin contexto Spring, sin MongoDB) que construye manualmente la cadena de
 * filtros de Spring Security y la monta sobre un MockMvc con ProductController.
 */
class SecurityRulesTest {

    private static final String JWT_SECRET = "secreto-de-prueba";

    private MockMvc mockMvc;
    private JwtService jwtService;
    private ProductService productService;
    private StaticWebApplicationContext appCtx;

    @BeforeEach
    void setUp() throws Exception {
        jwtService = new JwtService(JWT_SECRET, 86_400_000L);
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtService);

        productService = mock(ProductService.class);
        ProductController controller = new ProductController(productService);

        // Construir la cadena de filtros de Spring Security sin levantar ApplicationContext
        SecurityFilterChain filterChain = buildFilterChain(jwtFilter);
        FilterChainProxy filterChainProxy = new FilterChainProxy(List.of(filterChain));

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addFilter(filterChainProxy)
                .build();
    }

    @AfterEach
    void tearDown() {
        if (appCtx != null) {
            appCtx.close();
            appCtx = null;
        }
    }

    /**
     * Construye el SecurityFilterChain usando la API interna de Spring Security,
     * sin ApplicationContext ni MongoDB.
     */
    private SecurityFilterChain buildFilterChain(JwtAuthenticationFilter jwtFilter) throws Exception {
        // ObjectPostProcessor sin-op necesario para HttpSecurity
        ObjectPostProcessor<Object> noOpOpp = new ObjectPostProcessor<>() {
            @Override
            public <O> O postProcess(O object) {
                return object;
            }
        };

        // AuthenticationManagerBuilder mínimo (el SecurityConfig no configura autenticación vía
        // AuthenticationManager — solo usa el JwtFilter stateless)
        AuthenticationManagerBuilder authBuilder = new AuthenticationManagerBuilder(noOpOpp);
        authBuilder.parentAuthenticationManager(
                (AuthenticationManager) authentication -> { throw new org.springframework.security.core.AuthenticationException("n/a") {}; });

        // PathPatternRequestMatcher.Builder es requerido por SecurityConfig para el DSL de URLs
        PathPatternRequestMatcher.Builder matcherBuilder = PathPatternRequestMatcher.withDefaults();

        // ApplicationContext mínimo requerido por AuthorizeHttpRequestsConfigurer
        appCtx = new StaticWebApplicationContext();
        appCtx.refresh();

        Map<Class<?>, Object> sharedObjects = new java.util.HashMap<>();
        sharedObjects.put(PathPatternRequestMatcher.Builder.class, matcherBuilder);
        sharedObjects.put(org.springframework.context.ApplicationContext.class, appCtx);

        HttpSecurity http = new HttpSecurity(noOpOpp, authBuilder, sharedObjects);

        SecurityConfig config = new SecurityConfig(jwtFilter);
        return config.filterChain(http);
    }

    private static final String BODY = "{}";

    @Test
    void getProductosEsPublico() throws Exception {
        when(productService.searchActive(any(ProductSearchCriteria.class))).thenReturn(List.of());
        mockMvc.perform(get("/api/products")).andExpect(status().isOk());
    }

    @Test
    void postProductoSinTokenDevuelve401() throws Exception {
        mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void postProductoConRolClienteDevuelve403() throws Exception {
        String token = jwtService.generarToken("u-cli", "ana@riva.com", Rol.CLIENTE);

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    void postProductoConRolAdminNoEs401Ni403() throws Exception {
        String token = jwtService.generarToken("u-adm", "bob@riva.com", Rol.ADMINISTRADOR);

        // Con token de Admin, la seguridad lo deja pasar.
        // El 400 viene de @Valid en CreateProductRequest, no de un bloqueo de autorización.
        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isBadRequest());
    }
}

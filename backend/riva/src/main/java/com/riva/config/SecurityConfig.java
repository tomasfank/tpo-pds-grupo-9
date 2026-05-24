package com.riva.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

// Configuración temporal de Spring Security: permite todos los endpoints sin autenticación
// para poder validar que el backend arranca y expone /api/health.
// Cuando se implemente el módulo de autenticación (CU-01 / CU-02) hay que reemplazar el
// permitAll() por un esquema basado en JWT + roles (Cliente / Administrador).
@Configuration
public class SecurityConfig {

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
		return http.build();
	}
}

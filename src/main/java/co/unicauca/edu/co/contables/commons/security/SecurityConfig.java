package co.unicauca.edu.co.contables.commons.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/** @brief Configuración de seguridad para la aplicación con JWT y OAuth2 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthConverter jwtAuthConverter;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(http -> http
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**", "/actuator/**", "/api/config/help-center/**").permitAll()
                        .requestMatchers("/api/enterprises/logos/**").permitAll()
                        // A propósito NO se agregan aquí /api/enterprises/*/backup,
                        // /api/enterprises/backups/**, /api/enterprises/restore ni
                        // /api/enterprises/*/duplicate: deben caer en anyRequest().authenticated()
                        // (son datos sensibles de la empresa, no públicos como el logo).
                        // Stream SSE del módulo auxiliary-book: el origen lo exponía sin autenticación
                        .requestMatchers("/api/notifications/stream").permitAll()
                        .requestMatchers(
                                "/api/keycloak/token/**", 
                                "/api/keycloak/register",
                                "/api/keycloak/forgot-password",
                                "/api/keycloak/reset-password",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/actuator/**"
                        ).permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter)))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }
}



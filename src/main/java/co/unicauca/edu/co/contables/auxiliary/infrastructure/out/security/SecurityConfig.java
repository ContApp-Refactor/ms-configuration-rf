package co.unicauca.edu.co.contables.auxiliary.infrastructure.out.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import lombok.RequiredArgsConstructor;

/**
 * @brief Configuracion de seguridad HTTP de la aplicacion.
 *
 * Deshabilita CSRF, permite acceso publico a Swagger/actuator y al
 * stream SSE de notificaciones, exige autenticacion para el resto,
 * utiliza JWT (resource server) y establece sesion sin estado. CORS
 * se delega al API Gateway que sirve como entrada de las peticiones.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Autowired
    private JwtAuthConverter jwtAuthConverter;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(http -> http
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**","/actuator/**").permitAll()
                        .requestMatchers("/api/notifications/stream").permitAll()
                        .anyRequest()
                        .authenticated())
                .oauth2ResourceServer(oauth -> {
                    oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter));
                })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }
}

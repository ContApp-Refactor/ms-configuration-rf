package co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest;

import co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.error.BillingRestExceptionHandler;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.mapper.BillingRestMapper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

/**
 * Configuracion de prueba SOLO de billing para los {@code @WebMvcTest} (D18): no carga
 * {@code WebConfiguration} del monolito (TenantInterceptor) ni su data.sql. Aporta seguridad
 * minima equivalente a la real (todo autenticado, 401 sin credenciales, CSRF off) con seguridad de
 * metodos activa para que {@code @PreAuthorize} se evalue, mas el advice de errores de billing, el
 * mapper REST. El soporte de endpoints ({@code BillingInvoiceEndpointSupport} para facturas,
 * {@code BillingReturnEndpointSupport} para devoluciones) se lista en el {@code @ContextConfiguration}
 * de cada test, de modo que un slice solo necesita los {@code @MockitoBean} de SU soporte (H8d).
 */
@TestConfiguration
@EnableWebSecurity
@EnableMethodSecurity
@Import({BillingRestExceptionHandler.class, BillingRestMapper.class})
public class BillingWebMvcTestConfig {

    @Bean
    SecurityFilterChain billingTestSecurityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));
        return http.build();
    }
}

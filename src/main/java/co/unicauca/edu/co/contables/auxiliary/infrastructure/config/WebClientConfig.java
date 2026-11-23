package co.unicauca.edu.co.contables.auxiliary.infrastructure.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @brief Configuración central de {@link WebClient} para llamadas HTTP salientes.
 *
 * Expone un builder genérico con propagación automática del token JWT
 * presente en el contexto de seguridad y un builder anotado con
 * {@code @LoadBalanced} para resolver nombres de servicios registrados
 * en el discovery (por ejemplo "lb://STOCK").
 */
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder externalWebClientBuilder() {
        return WebClient.builder()
                .filter(jwtPropagationFilter());
    }

    /**
     * Creates a WebClient.Builder bean that is already prepared for load balancing.
     * The @LoadBalanced annotation is crucial for Spring Cloud to resolve
     * service names registered in Eureka (e.g., "lb://STOCK").
     *
     * @return A configured WebClient.Builder.
     */
    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }

    /**
     * Defines a filter for WebClient that propagates the JWT token.
     * This filter will run on every outgoing request.
     * It extracts the JWT token from the security context of the incoming request
     * and adds it as an "Authorization" header to the outgoing request.
     *
     * @return An ExchangeFilterFunction that adds the authorization header.
     */
    private ExchangeFilterFunction jwtPropagationFilter() {
        return (clientRequest, next) -> {
            // Gets the current authentication from the security context
            var authentication = SecurityContextHolder.getContext().getAuthentication();

            // Checks if the authentication is of type JWT
            if (authentication instanceof JwtAuthenticationToken jwtAuth) {
                // Extracts the token value (the string)
                String tokenValue = jwtAuth.getToken().getTokenValue();

                // Clones the original request and adds the authorization header
                ClientRequest newRequest = ClientRequest.from(clientRequest)
                        .header("Authorization", "Bearer " + tokenValue)
                        .build();

                // Continues the filter chain with the new request
                return next.exchange(newRequest);
            }

            // If there is no token, continue with the original request
            return next.exchange(clientRequest);
        };
    }
}
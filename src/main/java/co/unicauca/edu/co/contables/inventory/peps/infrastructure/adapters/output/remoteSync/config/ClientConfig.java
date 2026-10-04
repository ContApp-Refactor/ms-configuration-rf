package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.remoteSync.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import co.unicauca.edu.co.contables.commons.config.aspect.JwtTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Configuration for external service clients
 *
 * Configures the HTTP proxy used to talk with the only external service that
 * survived the migration: KardexExternal (inventory-weighted-average-rf).
 * Stock, Products y Config pasaron a llamadas in-process (paso 7 del contrato),
 * asi que desaparecieron sus clientes.
 *
 * Implementado con {@link RestClient} en lugar de {@code WebClient}: el
 * monolito es una app servlet ({@code spring-boot-starter-web}) sin
 * {@code spring-webflux} en el classpath, asi que no puede construir
 * {@code WebClient.Builder}. La firma declarativa de
 * {@link IKardexExternalClient} no cambia.
 */
@Configuration
@EnableConfigurationProperties(ClientProperties.class)
@RequiredArgsConstructor
@Slf4j
public class ClientConfig {
     
    private final JwtTokenService jwtTokenService;

    /**
     * @brief Generic factory method to create declarative HTTP client proxies
     * 
     * Centralizes the logic for building RestClient instances and HttpServiceProxyFactory.
     * Applies common configurations like base URL and JWT propagation.
     * 
     * @param <T> The type of the client interface
     * @param baseUrl The base URL for the service
     * @param clientInterface The interface class of the client
     * @return A proxy instance of the client interface
     * @throws IllegalArgumentException if baseUrl is null or empty
     */
    private <T> T createRestClientProxy(String baseUrl, Class<T> clientInterface) {
        // Validar que baseUrl no sea null o vacío
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("BaseURL cannot be null or empty for client: " + clientInterface.getSimpleName());
        }
        
        log.info("Creating RestClient proxy for {} with baseUrl: {}", clientInterface.getSimpleName(), baseUrl);
        
        RestClient restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestInterceptor(jwtPropagationInterceptor())
                .build();

        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();

        return factory.createClient(clientInterface);
    }

    @Bean
    IKardexExternalClient kardexExternalClient(ClientProperties properties) {
        String baseUrl = properties.getKardexExternal().getBaseUrl();
        return createRestClientProxy(baseUrl, IKardexExternalClient.class);
    }

    /**
     * @brief Interceptor that propagates the JWT token in outgoing HTTP requests
     * 
     * Injects the "Authorization: Bearer ..." header. Retrieves the token from
     * the current context (HTTP or RabbitMQ).
     * 
     * @return ClientHttpRequestInterceptor that applies the JWT header
     */
    private ClientHttpRequestInterceptor jwtPropagationInterceptor() {
        return (request, body, execution) -> {
            try {
                String tokenValue = jwtTokenService.getToken();
                
                // Remover prefijo "Bearer " si ya existe en el token
                final String finalTokenValue = tokenValue.startsWith("Bearer ") 
                    ? tokenValue.substring(7) 
                    : tokenValue;
                
                request.getHeaders().setBearerAuth(finalTokenValue);

                return execution.execute(request, body);
                
            } catch (Exception e) {
                log.error("Error al obtener token JWT para propagación: {}", e.getMessage());
                throw new IllegalStateException("No JWT token available for propagation", e);
            }
        };
    }

}

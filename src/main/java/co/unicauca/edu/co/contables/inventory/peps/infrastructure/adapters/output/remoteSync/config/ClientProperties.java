package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.remoteSync.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Configuration properties for external services
 *
 * Maps configuration values from application.yml/properties with prefix "services".
 *
 * Solo se conserva {@code kardex-external}: stock, products y config ya viven
 * dentro del monolito, asi que esos tres {@code base-url} desaparecieron junto
 * con sus clientes HTTP (paso 7 del contrato de migracion).
 */
@ConfigurationProperties(prefix = "services")
@Data
@Slf4j
public class ClientProperties {
    private KardexExternal kardexExternal = new KardexExternal();

    /**
     * @brief Properties for Kardex External service
     */
    @Data
    public static class KardexExternal {
        private String baseUrl;
    }


    /**
     * @brief Validates configuration properties after initialization
     *
     * Checks that the required base URL is configured and not empty.
     * Throws IllegalStateException if the property is missing.
     */
    @PostConstruct
    public void validate() {
        log.info("Validating client properties...");
        log.info("Kardex External base URL: {}", kardexExternal.getBaseUrl());

        if (kardexExternal.getBaseUrl() == null || kardexExternal.getBaseUrl().trim().isEmpty()) {
            throw new IllegalStateException("Kardex External base URL not configured. Expected 'services.kardex-external.base-url'");
        }

        log.info("Client properties validated successfully");

    }

    
}

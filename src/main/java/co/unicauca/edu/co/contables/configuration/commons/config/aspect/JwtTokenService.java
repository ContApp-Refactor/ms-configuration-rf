package co.unicauca.edu.co.contables.configuration.commons.config.aspect;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.configuration.commons.security.IJwtUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Unified service for managing JWT tokens from both HTTP and RabbitMQ contexts
 * 
 * Provides seamless token access across different execution contexts,
 * handling both web requests and message processing scenarios.
 */
@Service
@Slf4j
public class JwtTokenService {

    @Autowired
    private IJwtUtils jwtUtils;

    // ThreadLocal for tokens from RabbitMQ context
    private static final ThreadLocal<String> rabbitJwtToken = new ThreadLocal<>();
    private static final ThreadLocal<String> rabbitTenantId = new ThreadLocal<>();

    /**
     * @brief Sets JWT token for RabbitMQ context
     * @param token JWT token from RabbitMQ message header
     */
    public void setRabbitJwtToken(String token) {
        rabbitJwtToken.set(token);
        log.debug("Token JWT establecido para contexto RabbitMQ");
    }

    /**
     * @brief Sets tenant ID for RabbitMQ context
     * @param tenantId Tenant identifier from RabbitMQ message
     */
    public void setRabbitTenantId(String tenantId) {
        rabbitTenantId.set(tenantId);
        log.debug("Tenant ID establecido para contexto RabbitMQ: {}", tenantId);
    }

    /**
     * @brief Gets JWT token from current context (RabbitMQ or HTTP)
     * @return JWT token string
     * @throws IllegalStateException If no token is available in any context
     */
    public String getToken() {
        String token = rabbitJwtToken.get();
        if (token != null) {
            log.debug("Usando token JWT del contexto RabbitMQ");
            return token;
        }

        try {
            token = jwtUtils.getToken();
            log.debug("Usando token JWT del contexto HTTP");
            return token;
        } catch (Exception e) {
            log.warn("No se pudo obtener token del contexto HTTP: {}", e.getMessage());
            throw new IllegalStateException("No hay token JWT disponible ni en contexto RabbitMQ ni HTTP", e);
        }
    }

    /**
     * @brief Gets tenant ID from current context (RabbitMQ or HTTP)
     * @return Tenant identifier string
     * @throws IllegalStateException If no tenant ID is available in any context
     */
    public String getTenantId() {
        String tenantId = rabbitTenantId.get();
        if (tenantId != null) {
            log.debug("Usando tenant ID del contexto RabbitMQ: {}", tenantId);
            return tenantId;
        }

        try {
            tenantId = jwtUtils.getId();
            log.debug("Usando tenant ID del contexto HTTP: {}", tenantId);
            return tenantId;
        } catch (Exception e) {
            log.warn("No se pudo obtener tenant ID del contexto HTTP: {}", e.getMessage());
            throw new IllegalStateException("No hay tenant ID disponible ni en contexto RabbitMQ ni HTTP", e);
        }
    }

    /**
     * @brief Clears RabbitMQ context for current thread
     * 
     * Must be called in finally block of RabbitMQ aspect to prevent memory leaks.
     */
    public void clearRabbitContext() {
        rabbitJwtToken.remove();
        rabbitTenantId.remove();
        log.debug("Contexto RabbitMQ limpiado");
    }

    /**
     * @brief Checks if currently in RabbitMQ context
     * @return True if RabbitMQ context is active, false otherwise
     */
    public boolean isInRabbitContext() {
        return rabbitJwtToken.get() != null;
    }
}

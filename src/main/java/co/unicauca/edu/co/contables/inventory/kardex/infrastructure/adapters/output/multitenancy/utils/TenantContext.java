package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.multitenancy.utils;

import lombok.extern.slf4j.Slf4j;

/**
 * @brief Thread-local context for managing tenant identifiers
 * 
 * Provides thread-safe access to the current tenant ID across the application.
 * Uses InheritableThreadLocal to ensure child threads inherit tenant context.
 */
@Slf4j
public class TenantContext {
    private TenantContext() {
    }

    private static final InheritableThreadLocal<String> currentTenant = new InheritableThreadLocal<>();

    /**
     * @brief Sets the tenant identifier for the current thread context
     * @param tenantId The tenant identifier to set
     */
    public static void setTenantId(String tenantId) {
        log.debug("Setting tenantId to " + tenantId);
        currentTenant.set(tenantId);
    }

    /**
     * @brief Gets the tenant identifier from the current thread context
     * @return The tenant identifier, or null if none has been set
     */
    public static String getTenantId() {
        return currentTenant.get();
    }

    /**
     * @brief Clears the tenant identifier from the current thread context
     * 
     * Should be called after request processing is complete to prevent
     * memory leaks in thread pools.
     */
    public static void clear() {
        currentTenant.remove();
    }
}
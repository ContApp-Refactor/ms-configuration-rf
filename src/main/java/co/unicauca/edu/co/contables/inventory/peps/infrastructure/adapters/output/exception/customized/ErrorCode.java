package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.exception.customized;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @brief Enumeration of standard error codes
 * 
 * Defines standardized error prefixes and descriptions for logging and responses.
 */
@RequiredArgsConstructor
@Getter
public enum ErrorCode {
    /** @brief Generic system error */
    GENERIC_ERROR("GC-001: Generic error -> "),
    /** @brief Entity duplication error */
    ENTITY_ALREADY_EXISTS("GC-002: Entity already exists -> "),
    /** @brief Entity lookup error */
    ENTITY_NOT_FOUND("GC-003: Entity not found -> "),
    /** @brief Domain rule violation */
    BUSINESS_RULE_VIOLATION("GC-004: Business rule violation -> ");

    private final String description;
    
}

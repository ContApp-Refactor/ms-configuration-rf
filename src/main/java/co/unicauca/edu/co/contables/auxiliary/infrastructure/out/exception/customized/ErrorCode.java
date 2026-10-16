package co.unicauca.edu.co.contables.auxiliary.infrastructure.out.exception.customized;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @brief Enumeración de códigos de error para las excepciones personalizadas.
 *
 * Centraliza los códigos y descripciones usados al formatear mensajes
 * de error, asegurando consistencia en toda la aplicación.
 */
@RequiredArgsConstructor
@Getter
public enum ErrorCode {
    /**
     * @brief Código para errores genéricos no categorizados.
     */
    GENERIC_ERROR("GC-001: Generic error -> "),
    /**
     * @brief Código para el caso de entidad duplicada.
     */
    ENTITY_ALREADY_EXISTS("GC-002: Entity already exists -> "),
    /**
     * @brief Código para el caso de entidad no encontrada.
     */
    ENTITY_NOT_FOUND("GC-003: Entity not found -> "),
    /**
     * @brief Código para violación de regla de negocio.
     */
    BUSINESS_RULE_VIOLATION("GC-004: Business rule violation -> ");

    /**
     * @brief Descripción asociada al código de error.
     */
    private final String description;
}

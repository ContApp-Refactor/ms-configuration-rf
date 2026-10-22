package co.unicauca.edu.co.contables.configuration.enterprise.domain.enums;

/**
 * @brief Tipos de operación de copia de seguridad de una empresa.
 *
 * Igual que en el microservicio de referencia ({@code copy}), DUPLICATE no es
 * un tipo real de operación: es BACKUP del origen encadenado con RESTORE
 * sobre el destino (ver {@code IEnterpriseDuplicateService}).
 */
public enum CopyOperationTypeEnum {
    BACKUP,
    RESTORE
}

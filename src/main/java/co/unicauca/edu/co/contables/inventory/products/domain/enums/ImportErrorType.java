package co.unicauca.edu.co.contables.inventory.products.domain.enums;

/**
 * @brief Tipos de errores durante proceso de importación
 *
 * Clasifica los diferentes tipos de errores que pueden ocurrir durante
 * la importación de productos desde archivos Excel.
 */
public enum ImportErrorType {
    FORMAT_ERROR,
    VALIDATION_ERROR,
    BUSINESS_RULE_VIOLATION,
    DUPLICATE_ERROR,
    SYSTEM_ERROR,
    MISSING_DATA
}
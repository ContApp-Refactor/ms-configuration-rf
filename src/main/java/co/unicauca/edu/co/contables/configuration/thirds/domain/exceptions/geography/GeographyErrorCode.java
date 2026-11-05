package co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.geography;

import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.ErrorCodeDefinition;
import lombok.Getter;

/**
 * @brief Códigos de error específicos del dominio geográfico
 *
 * Define todos los códigos de error estándar utilizados en las excepciones
 * relacionadas con operaciones geográficas como validación de países,
 * estados, ciudades y jerarquías geográficas.
 */
@Getter
public enum GeographyErrorCode implements ErrorCodeDefinition {

    COUNTRY_NOT_FOUND("COUNTRY_NOT_FOUND", "País no encontrado"),
    STATE_NOT_FOUND("STATE_NOT_FOUND", "Estado/departamento no encontrado"),
    CITY_NOT_FOUND("CITY_NOT_FOUND", "Ciudad no encontrada o inactiva"),
    INVALID_COUNTRY_CODE("INVALID_COUNTRY_CODE", "Código de país inválido"),
    INVALID_STATE_CODE("INVALID_STATE_CODE", "Código de estado inválido"),
    INVALID_CITY_CODE("INVALID_CITY_CODE", "Código de ciudad inválido"),
    GEOGRAPHY_HIERARCHY_VIOLATION("GEOGRAPHY_HIERARCHY_VIOLATION", "Violación de jerarquía geográfica"),
    GEOGRAPHY_DATA_INITIALIZATION_FAILED("GEOGRAPHY_DATA_INITIALIZATION_FAILED", "Error en la inicialización de datos geográficos");

    private final String code;
    private final String message;

    /**
     * @brief Constructor del enum
     * @param code código único del error utilizado internamente
     * @param message mensaje descriptivo del error para usuarios
     */
    GeographyErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}

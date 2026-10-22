package co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions;

/**
 * @brief Contrato común para cualquier código de error de la aplicación
 *
 * Define la interfaz que deben implementar todos los códigos de error,
 * permitiendo que múltiples catálogos (por dominio) sean usados de forma uniforme
 * en todo el sistema de manejo de excepciones.
 */
public interface ErrorCodeDefinition {

    /**
     * @brief Obtiene el código único y estable del error
     * @return código identificador del error utilizado internamente
     */
    String getCode();

    /**
     * @brief Obtiene el mensaje por defecto asociado al error
     * @return mensaje descriptivo del error para usuarios finales
     */
    String getMessage();
}



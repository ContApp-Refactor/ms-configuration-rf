package co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.thirdType;

import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.BaseBusinessException;

/**
 * @brief Excepción que se lanza cuando ya existe un tipo de tercero
 *
 * Se utiliza durante operaciones de creación de tipos de tercero para prevenir
 * duplicados basados en criterios únicos como nombre o código.
 */
public class ThirdTypeAlreadyExists extends BaseBusinessException {

    /**
     * @brief Constructor por defecto
     *
     * Crea una instancia de la excepción utilizando el código de error
     * estándar para tipos de tercero que ya existen.
     */
    public ThirdTypeAlreadyExists() {
        super(ThirdTypeErrorCode.THIRD_TYPE_ALREADY_EXISTS);
    }

    /**
     * @brief Constructor con mensaje personalizado
     * @param customMessage mensaje personalizado que describe el error específico
     */
    public ThirdTypeAlreadyExists(String customMessage) {
        super(ThirdTypeErrorCode.THIRD_TYPE_ALREADY_EXISTS, customMessage);
    }

    /**
     * @brief Constructor con mensaje personalizado y causa
     * @param customMessage mensaje personalizado que describe el error específico
     * @param cause causa original del error que provocó esta excepción
     */
    public ThirdTypeAlreadyExists(String customMessage, Throwable cause) {
        super(ThirdTypeErrorCode.THIRD_TYPE_ALREADY_EXISTS, customMessage, cause);
    }
}

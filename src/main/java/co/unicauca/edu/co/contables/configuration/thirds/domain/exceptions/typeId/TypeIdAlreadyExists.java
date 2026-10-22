package co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.typeId;

import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.BaseBusinessException;

/**
 * @brief Excepción que se lanza cuando ya existe un tipo de identificación
 *
 * Se utiliza durante operaciones de creación de tipos de identificación para prevenir
 * duplicados basados en criterios únicos como nombre, código o clasificación.
 */
public class TypeIdAlreadyExists extends BaseBusinessException {

    /**
     * @brief Constructor por defecto
     *
     * Crea una instancia de la excepción utilizando el código de error
     * estándar para tipos de identificación que ya existen.
     */
    public TypeIdAlreadyExists() {
        super(TypeIdErrorCode.TYPE_ID_ALREADY_EXISTS);
    }

    /**
     * @brief Constructor con mensaje personalizado
     * @param customMessage mensaje personalizado que describe el error específico
     */
    public TypeIdAlreadyExists(String customMessage) {
        super(TypeIdErrorCode.TYPE_ID_ALREADY_EXISTS, customMessage);
    }

    /**
     * @brief Constructor con mensaje personalizado y causa
     * @param customMessage mensaje personalizado que describe el error específico
     * @param cause causa original del error que provocó esta excepción
     */
    public TypeIdAlreadyExists(String customMessage, Throwable cause) {
        super(TypeIdErrorCode.TYPE_ID_ALREADY_EXISTS, customMessage, cause);
    }
}

package co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.typeId;

import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.BaseBusinessException;

/**
 * @brief Excepción que se lanza cuando la clasificación de persona es inválida o no se encuentra
 *
 * Esta excepción se utiliza cuando se proporciona un valor de clasificación que no es válido
 * o cuando no se puede determinar la clasificación apropiada para un tipo de identificación.
 * Las clasificaciones válidas son NATURAL_PERSON y LEGAL_ENTITY.
 */
public class PersonClassificationInvalidException extends BaseBusinessException {

    /**
     * @brief Constructor por defecto
     *
     * Crea una instancia de la excepción utilizando el código de error
     * estándar para clasificaciones de persona inválidas.
     */
    public PersonClassificationInvalidException() {
        super(TypeIdErrorCode.TYPE_ID_INVALID_CLASSIFICATION);
    }

    /**
     * @brief Constructor con mensaje personalizado
     * @param customMessage mensaje personalizado que describe el error de clasificación
     */
    public PersonClassificationInvalidException(String customMessage) {
        super(TypeIdErrorCode.TYPE_ID_INVALID_CLASSIFICATION, customMessage);
    }

    /**
     * @brief Constructor con mensaje personalizado y causa
     * @param customMessage mensaje personalizado que describe el error de clasificación
     * @param cause causa original del error que provocó esta excepción
     */
    public PersonClassificationInvalidException(String customMessage, Throwable cause) {
        super(TypeIdErrorCode.TYPE_ID_INVALID_CLASSIFICATION, customMessage, cause);
    }

    /**
     * @brief Constructor con causa
     *
     * Crea una excepción con un mensaje estándar basado en la causa proporcionada.
     * @param cause causa original del error que provocó esta excepción
     */
    public PersonClassificationInvalidException(Throwable cause) {
        super(TypeIdErrorCode.TYPE_ID_INVALID_CLASSIFICATION,
              "Error en la clasificación de persona: " + cause.getMessage(), cause);
    }

    /**
     * @brief Crea excepción para código de clasificación inválido
     *
     * Se utiliza cuando se proporciona un código de clasificación que no existe
     * o no está permitido en el sistema.
     * @param invalidCode código de clasificación que no es válido
     * @return nueva instancia de la excepción configurada para este escenario
     */
    public static PersonClassificationInvalidException forInvalidCode(String invalidCode) {
        return new PersonClassificationInvalidException(
            "La clasificación de persona '" + invalidCode + "' no es válida. " +
            "Valores válidos: NATURAL_PERSON, LEGAL_ENTITY"
        );
    }

    /**
     * @brief Crea excepción para clasificación nula
     *
     * Se utiliza cuando se intenta procesar un tipo de identificación sin
     * especificar la clasificación de persona requerida.
     * @return nueva instancia de la excepción configurada para este escenario
     */
    public static PersonClassificationInvalidException forNullClassification() {
        return new PersonClassificationInvalidException(
            "La clasificación de persona no puede ser nula. " +
            "Debe especificar: NATURAL_PERSON o LEGAL_ENTITY"
        );
    }
}

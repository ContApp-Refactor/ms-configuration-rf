package co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.third;

import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.BaseBusinessException;

/**
 * @brief Excepción lanzada cuando el formato del NIT no es válido para personas jurídicas
 *
 * Se utiliza para validar que los NIT de personas jurídicas cumplan con
 * las regulaciones colombianas, específicamente que deben empezar por 8 o 9.
 */
public class ThirdNitInvalidFormatException extends BaseBusinessException {

    private static final String DEFAULT_MESSAGE = "El NIT debe empezar por 8 o 9 para personas jurídicas";

    /**
     * @brief Constructor con número de identificación específico
     *
     * Crea una excepción específica cuando se detecta que un NIT no cumple
     * con el formato requerido, generando automáticamente un mensaje descriptivo.
     * @param nitNumber el número de NIT que no cumple con el formato requerido
     */
    public ThirdNitInvalidFormatException(String nitNumber) {
        super(
            ThirdsErrorCode.THIRD_NIT_INVALID_FORMAT,
            String.format("El NIT '%s' no es válido. Debe empezar por 8 o 9 para personas jurídicas", nitNumber)
        );
    }

    /**
     * @brief Constructor con mensaje por defecto
     *
     * Crea una instancia de la excepción utilizando el mensaje estándar
     * para errores de formato de NIT.
     */
    public ThirdNitInvalidFormatException() {
        super(ThirdsErrorCode.THIRD_NIT_INVALID_FORMAT, DEFAULT_MESSAGE);
    }

    /**
     * @brief Constructor con NIT y mensaje personalizado
     * @param nitNumber el número de NIT que no cumple con el formato
     * @param customMessage mensaje personalizado que describe el error específico
     */
    public ThirdNitInvalidFormatException(String nitNumber, String customMessage) {
        super(ThirdsErrorCode.THIRD_NIT_INVALID_FORMAT, customMessage);
    }

    /**
     * @brief Constructor con mensaje personalizado y causa
     * @param message mensaje personalizado que describe el error específico
     * @param cause causa original del error que provocó esta excepción
     */
    public ThirdNitInvalidFormatException(String message, Throwable cause) {
        super(ThirdsErrorCode.THIRD_NIT_INVALID_FORMAT, message, cause);
    }
}

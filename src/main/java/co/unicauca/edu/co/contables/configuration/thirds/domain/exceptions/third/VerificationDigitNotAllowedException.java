package co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.third;

import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.BaseBusinessException;

/**
 * @brief Excepción lanzada cuando se intenta agregar un dígito de verificación a una persona natural
 *
 * Se utiliza cuando se intenta asignar un dígito de verificación a personas naturales,
 * donde solo está permitido para personas jurídicas según la normatividad colombiana.
 */
public class VerificationDigitNotAllowedException extends BaseBusinessException {

    /**
     * @brief Constructor por defecto
     *
     * Crea una instancia de la excepción utilizando el código de error
     * estándar para dígitos de verificación no permitidos.
     */
    public VerificationDigitNotAllowedException() {
        super(ThirdsErrorCode.THIRD_VERIFICATION_DIGIT_NOT_ALLOWED);
    }

    /**
     * @brief Constructor con tipo de persona
     *
     * Crea una excepción específica cuando se intenta asignar dígito de verificación
     * a un tipo de persona que no lo permite, generando automáticamente un mensaje descriptivo.
     * @param personType tipo de persona para el cual no se permite dígito de verificación
     */
    public VerificationDigitNotAllowedException(String personType) {
        super(ThirdsErrorCode.THIRD_VERIFICATION_DIGIT_NOT_ALLOWED,
                String.format("El dígito de verificación no se permite para personas de tipo '%s'. Solo se permite para personas jurídicas", personType));
    }
}

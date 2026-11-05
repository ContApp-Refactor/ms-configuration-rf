package co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.third;

import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.BaseBusinessException;

/**
 * @brief Excepción lanzada cuando una persona jurídica con NIT no tiene el dígito de verificación obligatorio
 *
 * Se utiliza en validaciones de personas jurídicas donde el dígito de verificación
 * del NIT es requerido por normatividad colombiana para completar la identificación.
 */
public class VerificationDigitRequiredException extends BaseBusinessException {

    /**
     * @brief Constructor por defecto
     *
     * Crea una instancia de la excepción utilizando el código de error
     * estándar para dígitos de verificación requeridos.
     */
    public VerificationDigitRequiredException() {
        super(ThirdsErrorCode.THIRD_VERIFICATION_DIGIT_REQUIRED);
    }

    /**
     * @brief Constructor con código de tipo de identificación
     *
     * Crea una excepción específica cuando se detecta que falta el dígito de verificación
     * para un tipo de identificación determinado, generando automáticamente un mensaje descriptivo.
     * @param typeIdCode código del tipo de identificación que requiere dígito de verificación
     */
    public VerificationDigitRequiredException(String typeIdCode) {
        super(ThirdsErrorCode.THIRD_VERIFICATION_DIGIT_REQUIRED,
                String.format("El dígito de verificación es obligatorio para personas jurídicas con tipo de identificación '%s'", typeIdCode));
    }
}

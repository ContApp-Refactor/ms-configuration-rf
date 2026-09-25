package co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.third;

import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.BaseBusinessException;

/**
 * @brief Excepción que se lanza cuando hay inconsistencias entre el tipo de persona y los campos requeridos para crear un tercero
 *
 * Valida las reglas de negocio específicas para personas naturales y jurídicas,
 * asegurando que se proporcionen los campos correctos según el tipo de persona.
 */
public class ThirdPersonTypeValidationException extends BaseBusinessException {

    /**
     * @brief Constructor por defecto
     *
     * Crea una instancia de la excepción utilizando el código de error
     * estándar para validaciones de tipo de persona.
     */
    public ThirdPersonTypeValidationException() {
        super(ThirdsErrorCode.THIRD_PERSON_TYPE_VALIDATION_ERROR);
    }

    /**
     * @brief Constructor con mensaje personalizado
     * @param customMessage mensaje personalizado que describe la inconsistencia específica
     */
    public ThirdPersonTypeValidationException(String customMessage) {
        super(ThirdsErrorCode.THIRD_PERSON_TYPE_VALIDATION_ERROR, customMessage);
    }

    /**
     * @brief Constructor con mensaje personalizado y causa
     * @param customMessage mensaje personalizado que describe la inconsistencia específica
     * @param cause causa original del error que provocó esta excepción
     */
    public ThirdPersonTypeValidationException(String customMessage, Throwable cause) {
        super(ThirdsErrorCode.THIRD_PERSON_TYPE_VALIDATION_ERROR, customMessage, cause);
    }

    /**
     * @brief Crea excepción para persona natural sin campos requeridos
     *
     * Se utiliza cuando se intenta crear una persona natural pero faltan
     * los campos obligatorios (nombres y apellidos) o se incluye razón social.
     * @return nueva instancia de la excepción configurada para este escenario
     */
    public static ThirdPersonTypeValidationException forNaturalPersonMissingFields() {
        return new ThirdPersonTypeValidationException(
            "Para personas naturales son obligatorios: nombres y apellidos. " +
            "La razón social debe estar vacía."
        );
    }

    /**
     * @brief Crea excepción para persona jurídica sin razón social
     *
     * Se utiliza cuando se intenta crear una persona jurídica pero falta
     * la razón social obligatoria o se incluyen campos de persona natural.
     * @return nueva instancia de la excepción configurada para este escenario
     */
    public static ThirdPersonTypeValidationException forLegalEntityMissingFields() {
        return new ThirdPersonTypeValidationException(
            "Para personas jurídicas es obligatoria la razón social. " +
            "Los campos nombres, apellidos y género deben estar vacíos."
        );
    }

    /**
     * @brief Crea excepción para persona natural con campos no permitidos
     *
     * Se utiliza cuando se intenta incluir razón social en una persona natural,
     * lo cual no está permitido por las reglas de negocio.
     * @return nueva instancia de la excepción configurada para este escenario
     */
    public static ThirdPersonTypeValidationException forNaturalPersonWithForbiddenFields() {
        return new ThirdPersonTypeValidationException(
            "Para personas naturales no se permite el campo razón social. " +
            "Use nombres, apellidos y género."
        );
    }

    /**
     * @brief Crea excepción para persona jurídica con campos no permitidos
     *
     * Se utiliza cuando se intentan incluir campos de persona natural
     * (nombres, apellidos, género) en una persona jurídica.
     * @return nueva instancia de la excepción configurada para este escenario
     */
    public static ThirdPersonTypeValidationException forLegalEntityWithForbiddenFields() {
        return new ThirdPersonTypeValidationException(
            "Para personas jurídicas no se permiten los campos nombres, apellidos o género. " +
            "Use únicamente razón social."
        );
    }

    /**
     * @brief Crea excepción para persona jurídica con campo específico no permitido
     *
     * Se utiliza cuando se intenta incluir un campo específico de persona natural
     * en una persona jurídica, permitiendo identificar exactamente cuál campo.
     * @param fieldName nombre del campo específico que no está permitido
     * @return nueva instancia de la excepción configurada para este escenario
     */
    public static ThirdPersonTypeValidationException forLegalEntityWithForbiddenField(String fieldName) {
        return new ThirdPersonTypeValidationException(
            String.format("Para personas jurídicas no se permite el campo %s. Use únicamente razón social.", fieldName)
        );
    }
}

package co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.third;

import co.unicauca.edu.co.contables.configuration.thirds.domain.enums.ePersonType;
import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.BaseBusinessException;

/**
 * @brief Excepción que se lanza cuando hay incompatibilidad entre el tipo de identificación y el tipo de persona
 *
 * Valida que los tipos de identificación sean compatibles con el tipo de persona
 * (natural o jurídica) según las regulaciones colombianas.
 */
public class ThirdTypeIdPersonTypeIncompatibilityException extends BaseBusinessException {

    /**
     * @brief Constructor por defecto
     *
     * Crea una instancia de la excepción utilizando el código de error
     * estándar para incompatibilidades entre tipo de ID y tipo de persona.
     */
    public ThirdTypeIdPersonTypeIncompatibilityException() {
        super(ThirdsErrorCode.THIRD_TYPE_ID_PERSON_TYPE_INCOMPATIBILITY);
    }

    /**
     * @brief Constructor con mensaje personalizado
     * @param customMessage mensaje personalizado que describe la incompatibilidad específica
     */
    public ThirdTypeIdPersonTypeIncompatibilityException(String customMessage) {
        super(ThirdsErrorCode.THIRD_TYPE_ID_PERSON_TYPE_INCOMPATIBILITY, customMessage);
    }

    /**
     * @brief Constructor con mensaje personalizado y causa
     * @param customMessage mensaje personalizado que describe la incompatibilidad específica
     * @param cause causa original del error que provocó esta excepción
     */
    public ThirdTypeIdPersonTypeIncompatibilityException(String customMessage, Throwable cause) {
        super(ThirdsErrorCode.THIRD_TYPE_ID_PERSON_TYPE_INCOMPATIBILITY, customMessage, cause);
    }

    /**
     * @brief Crea excepción para tipo de identificación no válido para persona natural
     *
     * Se utiliza cuando se intenta asignar un tipo de identificación que solo
     * está permitido para personas jurídicas a una persona natural.
     * @param typeIdCode código del tipo de identificación incompatible
     * @return nueva instancia de la excepción configurada para este escenario
     */
    public static ThirdTypeIdPersonTypeIncompatibilityException forNaturalPersonInvalidTypeId(String typeIdCode) {
        return new ThirdTypeIdPersonTypeIncompatibilityException(
            "El tipo de identificación '" + typeIdCode + "' no es válido para personas naturales"
        );
    }

    /**
     * @brief Crea excepción para tipo de identificación no válido para persona jurídica
     *
     * Se utiliza cuando se intenta asignar un tipo de identificación que solo
     * está permitido para personas naturales a una persona jurídica.
     * @param typeIdCode código del tipo de identificación incompatible
     * @return nueva instancia de la excepción configurada para este escenario
     */
    public static ThirdTypeIdPersonTypeIncompatibilityException forLegalEntityInvalidTypeId(String typeIdCode) {
        return new ThirdTypeIdPersonTypeIncompatibilityException(
            "El tipo de identificación '" + typeIdCode + "' no es válido para personas jurídicas"
        );
    }

    /**
     * @brief Crea excepción genérica para incompatibilidad entre tipo de ID y persona
     *
     * Método genérico que crea una excepción específica basada en el tipo de persona
     * y el código de identificación proporcionados.
     * @param typeIdCode código del tipo de identificación incompatible
     * @param personType tipo de persona para el cual es incompatible el tipo de ID
     * @return nueva instancia de la excepción configurada para este escenario
     */
    public static ThirdTypeIdPersonTypeIncompatibilityException forIncompatibility(String typeIdCode, ePersonType personType) {
        String personTypeDesc = personType.isNatural() ? "persona natural" : "persona jurídica";
        return new ThirdTypeIdPersonTypeIncompatibilityException(
            "El tipo de identificación '" + typeIdCode + "' no es compatible con " + personTypeDesc
        );
    }
}

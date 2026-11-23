package co.unicauca.edu.co.contables.configuration.thirds.application.service.third;

import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.third.*;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.Third;
import co.unicauca.edu.co.contables.configuration.thirds.domain.utils.ValidationUtils;
import org.springframework.stereotype.Component;

/**
 * @brief Servicio para validaciones comunes de terceros
 */
@Component
public class ThirdValidationService {

    /**
     * @brief Valida la consistencia entre el tipo de persona y los campos requeridos
     * @param third tercero a validar
     * @throws ThirdPersonTypeValidationException si hay inconsistencias
     */
    public void validatePersonTypeConsistency(Third third) {
        if (third.getPersonType() == null) {
            throw new ThirdInvalidDataException("El tipo de persona no puede estar vacío");
        }

        boolean hasNames = third.getNames() != null && !third.getNames().trim().isEmpty();
        boolean hasLastNames = third.getLastNames() != null && !third.getLastNames().trim().isEmpty();
        boolean hasGender = third.getGender() != null;
        boolean hasSocialReason = third.getSocialReason() != null && !third.getSocialReason().trim().isEmpty();

        if (third.getPersonType().isNatural()) {
            // Para persona natural: nombres y apellidos son obligatorios (género es opcional)
            if (!hasNames || !hasLastNames) {
                throw ThirdPersonTypeValidationException.forNaturalPersonMissingFields();
            }

            // Para persona natural: razón social NO debe estar presente
            if (hasSocialReason) {
                throw ThirdPersonTypeValidationException.forNaturalPersonWithForbiddenFields();
            }
        } else if (third.getPersonType().isJuridica()) {
            // Para persona jurídica: razón social es obligatoria
            if (!hasSocialReason) {
                throw ThirdPersonTypeValidationException.forLegalEntityMissingFields();
            }

            // Para persona jurídica: nombres, apellidos y género NO deben estar presentes
            if (hasNames) {
                throw ThirdPersonTypeValidationException.forLegalEntityWithForbiddenField("nombres");
            }
            if (hasLastNames) {
                throw ThirdPersonTypeValidationException.forLegalEntityWithForbiddenField("apellidos");
            }
            if (hasGender) {
                throw ThirdPersonTypeValidationException.forLegalEntityWithForbiddenField("género");
            }
        }
    }

    /**
     * @brief Valida que el tipo de identificación sea compatible con el tipo de persona
     * @param third tercero a validar
     * @throws ThirdTypeIdPersonTypeIncompatibilityException si hay incompatibilidad
     */
    public void validateTypeIdPersonTypeCompatibility(Third third) {
        if (third.getTypeId() == null || third.getPersonType() == null) {
            return; // Si no hay TypeId o PersonType, no se puede validar
        }

        // Validar que el código del TypeId no sea null o vacío
        String typeIdCode = third.getTypeId().getTypeId();
        if (typeIdCode == null || typeIdCode.trim().isEmpty()) {
            throw new ThirdInvalidDataException("El código del tipo de identificación no puede estar vacío");
        }

        if (third.getPersonType().isNatural()) {
            // Para persona natural, el TypeId debe ser válido para personas naturales
            if (!third.getTypeId().isValidForNaturalPerson()) {
                throw ThirdTypeIdPersonTypeIncompatibilityException.forNaturalPersonInvalidTypeId(typeIdCode);
            }
        } else if (third.getPersonType().isJuridica()) {
            // Para persona jurídica, el TypeId debe ser válido para personas jurídicas
            if (!third.getTypeId().isValidForLegalEntity()) {
                throw ThirdTypeIdPersonTypeIncompatibilityException.forLegalEntityInvalidTypeId(typeIdCode);
            }
        }
    }

    /**
     * @brief Valida que el formato del NIT sea correcto para personas jurídicas
     * @param third tercero a validar
     * @throws ThirdNitInvalidFormatException si el NIT no tiene el formato correcto
     */
    public void validateNitFormat(Third third) {
        // Solo validar si es persona jurídica y tiene tipo de identificación NIT
        if (third.getPersonType() == null || !third.getPersonType().isJuridica()) {
            return;
        }

        if (third.getTypeId() == null || third.getTypeId().getTypeId() == null) {
            return;
        }

        String typeIdCode = ValidationUtils.normalizeForComparison(third.getTypeId().getTypeId());
        if (!"NIT".equals(typeIdCode)) {
            return;
        }

        if (third.getIdNumber() == null) {
            throw new ThirdInvalidDataException("El número de identificación es obligatorio para NIT");
        }

        String nitNumber = third.getIdNumber().toString();

        // Validar que tenga exactamente 9 dígitos
        if (nitNumber.length() != 9) {
            throw new ThirdNitInvalidFormatException(nitNumber, 
                "El NIT debe tener exactamente 9 dígitos. Valor proporcionado: " + nitNumber + " (" + nitNumber.length() + " dígitos)");
        }

        // Validar que empiece por 8 o 9
        if (!nitNumber.startsWith("8") && !nitNumber.startsWith("9")) {
            throw new ThirdNitInvalidFormatException(nitNumber,
                "El NIT debe empezar por 8 o 9. Valor proporcionado: " + nitNumber);
        }
    }

    /**
     * @brief Valida el dígito de verificación según el tipo de persona
     * @param third tercero a validar
     * @throws VerificationDigitNotAllowedException si es persona natural y tiene dígito de verificación
     * @throws VerificationDigitRequiredException si es persona jurídica con NIT y no tiene dígito de verificación
     */
    public void validateVerificationDigit(Third third) {
        // Validar que el tipo de persona no sea null
        if (third.getPersonType() == null) {
            return;
        }

        boolean hasVerificationDigit = third.getVerificationNumber() != null;

        // REGLA 1: Personas naturales NO pueden tener dígito de verificación
        if (third.getPersonType().isNatural() && hasVerificationDigit) {
            throw new VerificationDigitNotAllowedException(third.getPersonType().name());
        }

        // REGLA 2: Personas jurídicas con NIT DEBEN tener dígito de verificación
        if (third.getPersonType().isJuridica()) {
            if (third.getTypeId() != null && third.getTypeId().getTypeId() != null) {
                String typeIdCode = ValidationUtils.normalizeForComparison(third.getTypeId().getTypeId());
                
                if ("NIT".equals(typeIdCode) && !hasVerificationDigit) {
                    throw new VerificationDigitRequiredException(typeIdCode);
                }
            }
        }
    }
}

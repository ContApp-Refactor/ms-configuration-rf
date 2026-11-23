package co.unicauca.edu.co.contables.configuration.thirds.domain.enums;

import lombok.Getter;

/**
 * @brief Enumeración que define los tipos de persona disponibles en el sistema
 *
 * Distingue entre personas naturales y jurídicas para aplicar validaciones
 * y reglas de negocio específicas según el contexto colombiano.
 */
@Getter
public enum ePersonType {

    Natural("NATURAL", "Persona Natural"),
    Juridica("JURIDICA", "Persona Jurídica");

    private final String code;
    private final String description;

    /**
     * @brief Constructor del enum
     * @param code Código interno del tipo de persona
     * @param description Descripción legible del tipo de persona
     */
    ePersonType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * @brief Verifica si el tipo de persona es natural
     * @return true si es persona natural, false en caso contrario
     */
    public boolean isNatural() {
        return this == Natural;
    }

    /**
     * @brief Verifica si el tipo de persona es jurídica
     * @return true si es persona jurídica, false en caso contrario
     */
    public boolean isJuridica() {
        return this == Juridica;
    }

    /**
     * @brief Convierte un código en un tipo de persona
     * @param code Código a convertir
     * @return Tipo de persona correspondiente o null si no existe
     */
    public static ePersonType fromCode(String code) {
        if (code == null) {
            return null;
        }

        for (ePersonType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * @brief Convierte una descripción en un tipo de persona
     * @param description Descripción a convertir
     * @return Tipo de persona correspondiente o null si no existe
     */
    public static ePersonType fromDescription(String description) {
        if (description == null) {
            return null;
        }

        for (ePersonType type : values()) {
            if (type.description.equalsIgnoreCase(description)) {
                return type;
            }
        }
        return null;
    }

    /**
     * @brief Convierte el enum a su representación en cadena
     * @return La descripción del tipo de persona
     */
    @Override
    public String toString() {
        return description;
    }
}

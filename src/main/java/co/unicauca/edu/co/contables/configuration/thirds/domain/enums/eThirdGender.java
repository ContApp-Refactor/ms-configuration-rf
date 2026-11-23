package co.unicauca.edu.co.contables.configuration.thirds.domain.enums;

import lombok.Getter;

/**
 * @brief Enumeración que define los géneros disponibles para terceros en el sistema
 *
 * Se utiliza principalmente para personas naturales y proporciona opciones
 * inclusivas para diferentes identidades de género según estándares modernos.
 */
@Getter
public enum eThirdGender {

    Masculino("M", "Masculino"),
    Femenino("F", "Femenino"),
    Otro("O", "Otro/Preferir no decir");

    private final String code;
    private final String description;

    /**
     * @brief Constructor del enum
     * @param code Código interno del género
     * @param description Descripción legible del género
     */
    eThirdGender(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * @brief Verifica si el género es masculino
     * @return true si es masculino, false en caso contrario
     */
    public boolean isMasculino() {
        return this == Masculino;
    }

    /**
     * @brief Verifica si el género es femenino
     * @return true si es femenino, false en caso contrario
     */
    public boolean isFemenino() {
        return this == Femenino;
    }

    /**
     * @brief Verifica si el género es otro o no especificado
     * @return true si es otro/no especificado, false en caso contrario
     */
    public boolean isOtro() {
        return this == Otro;
    }

    /**
     * @brief Convierte un código en un género
     * @param code Código a convertir
     * @return Género correspondiente o null si no existe
     */
    public static eThirdGender fromCode(String code) {
        if (code == null) {
            return null;
        }

        for (eThirdGender gender : values()) {
            if (gender.code.equalsIgnoreCase(code)) {
                return gender;
            }
        }
        return null;
    }

    /**
     * @brief Convierte una descripción en un género
     * @param description Descripción a convertir
     * @return Género correspondiente o null si no existe
     */
    public static eThirdGender fromDescription(String description) {
        if (description == null) {
            return null;
        }

        for (eThirdGender gender : values()) {
            if (gender.description.equalsIgnoreCase(description)) {
                return gender;
            }
        }
        return null;
    }

    /**
     * @brief Valida si un código de género es válido
     * @param code Código a validar
     * @return true si el código es válido, false en caso contrario
     */
    public static boolean isValidCode(String code) {
        return fromCode(code) != null;
    }

    /**
     * @brief Convierte el enum a su representación en cadena
     * @return La descripción del género
     */
    @Override
    public String toString() {
        return description;
    }
}

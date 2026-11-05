package co.unicauca.edu.co.contables.configuration.thirds.domain.model;

/**
 * @brief Enumeración que define la clasificación de personas para tipos de identificación
 *
 * Determina si un tipo de identificación aplica para personas naturales,
 * jurídicas o ambas, según la normatividad colombiana.
 */
public enum PersonClassification {

    NATURAL_PERSON("NATURAL_PERSON", "Persona Natural"),

    LEGAL_ENTITY("LEGAL_ENTITY", "Persona Jurídica");

    private final String code;
    private final String description;

  
    PersonClassification(String code, String description) {
        this.code = code;
        this.description = description;
    }

  
    public String getCode() {
        return code;
    }

   
    public String getDescription() {
        return description;
    }

    /**
     * @brief Verifica si la clasificación es válida para personas naturales
     * @return true si esta clasificación permite documentos para personas naturales
     */
    public boolean isValidForNaturalPerson() {
        return this == NATURAL_PERSON;
    }

    /**
     * @brief Verifica si la clasificación es válida para personas jurídicas
     * @return true si esta clasificación permite documentos para personas jurídicas
     */
    public boolean isValidForLegalEntity() {
        return this == LEGAL_ENTITY;
    }

    /**
     * @brief Obtiene una clasificación por su código de forma segura
     *
     * Busca una clasificación por su código sin lanzar excepciones.
     * Retorna null si el código no es válido, permitiendo que las capas
     * superiores decidan cómo manejar valores inválidos.
     * @param code código de la clasificación a buscar
     * @return PersonClassification correspondiente al código, o null si no existe
     */
    public static PersonClassification fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }

        for (PersonClassification classification : values()) {
            if (classification.getCode().equalsIgnoreCase(code.trim())) {
                return classification;
            }
        }

        return null; // No lanza excepción, retorna null
    }

    /**
     * @brief Verifica si un código es válido sin lanzar excepciones
     * @param code código a validar
     * @return true si el código corresponde a una clasificación válida, false en caso contrario
     */
    public static boolean isValidCode(String code) {
        return fromCode(code) != null;
    }
}

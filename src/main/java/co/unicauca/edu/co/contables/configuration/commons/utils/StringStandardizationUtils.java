package co.unicauca.edu.co.contables.configuration.commons.utils;

import java.util.Locale;

/**
 * @brief Clase utilitaria para estandarización de strings en toda la aplicación.
 * Centraliza la lógica de normalización para evitar duplicación de código.
 */
public final class StringStandardizationUtils {

    private static final Locale SPANISH_LOCALE = new Locale("es", "ES");

    // Constructor privado para evitar instanciación
    private StringStandardizationUtils() {
        throw new UnsupportedOperationException("Esta es una clase utilitaria y no debe ser instanciada");
    }

    /**
     * @brief Estandariza nombres: primera letra mayúscula, resto minúsculas, espacios normalizados.
     * Utilizado para nombres de clases de documentos, tipos de documentos, centros de costo, etc.
     * 
     * @param input el string a estandarizar
     * @return string estandarizado o null si input es null
     */
    public static String standardizeName(String input) {
        if (input == null) {
            return null;
        }
        
        String trimmed = input.trim().replaceAll("\\s+", " ").toLowerCase(SPANISH_LOCALE);
        if (trimmed.isEmpty()) {
            return trimmed;
        }
        
        return trimmed.substring(0, 1).toUpperCase(SPANISH_LOCALE) + trimmed.substring(1);
    }

    /**
     * @brief Estandariza prefijos: convierte a mayúsculas y elimina espacios.
     * Utilizado para prefijos de tipos de documentos y otros códigos similares.
     * 
     * @param input el string a estandarizar
     * @return string estandarizado o null si input es null
     */
    public static String standardizePrefix(String input) {
        if (input == null) {
            return null;
        }
        
        return input.trim().toUpperCase(Locale.ROOT);
    }

    /**
     * @brief Estandariza códigos: convierte a mayúsculas y elimina espacios.
     * Alias de standardizePrefix para mayor claridad semántica.
     * 
     * @param input el string a estandarizar
     * @return string estandarizado o null si input es null
     */
    public static String standardizeCode(String input) {
        return standardizePrefix(input);
    }
}

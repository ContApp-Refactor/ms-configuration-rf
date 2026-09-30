package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.utils;

import java.text.Normalizer;

/**
 * @brief Utilidad para normalización de texto en importación Excel
 *
 * Proporciona métodos estáticos para limpiar, formatear y comparar textos
 * de manera consistente durante el proceso de importación de cuentas contables.
 */
public final class StringNormalizer {

    private StringNormalizer() {
        throw new UnsupportedOperationException("StringNormalizer es una clase de utilidad y no debe ser instanciada");
    }

    /**
     * @brief Normaliza texto para comparaciones case-insensitive eliminando acentos
     * @param input texto a normalizar para comparación
     * @return texto normalizado para comparación, o null si el input es null
     */
    public static String normalizeForComparison(String input) {
        if (input == null || input.trim().isEmpty()) {
            return input;
        }
        
        String normalized = removeAccents(input.trim().toLowerCase());
        // Eliminar espacios múltiples
        return normalized.replaceAll("\\s+", " ");
    }

    /**
     * @brief Normaliza descripción de cuenta para almacenamiento en BD     *
     * @param input descripción a normalizar
     * @return descripción normalizada, o null si el input es null
     */
    public static String normalizeDescription(String input) {
        if (input == null) {
            return null;
        }
        
        // Trim y eliminar espacios múltiples
        return input.trim().replaceAll("\\s+", " ");
    }

    /**
     * @brief Normaliza código de cuenta aplicando trim para almacenamiento
     * @param input código a normalizar
     * @return código normalizado, o null si el input es null
     */
    public static String normalizeCode(String input) {
        if (input == null) {
            return null;
        }
        
        return input.trim();
    }

    /**
     * @brief Elimina tildes y acentos de una cadena de texto
     * @param input texto del cual eliminar acentos
     * @return texto sin acentos
     */
    private static String removeAccents(String input) {
        if (input == null) {
            return null;
        }        
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);        
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    /**
     * @brief Normaliza nombres de encabezados Excel eliminando metadatos y formateo
     * @param headerName nombre del encabezado original
     * @return nombre normalizado sin indicativos de requerimiento ni saltos de línea
     */
    public static String normalizeHeaderName(String headerName) {
        if (headerName == null) {
            return null;
        }
        
        // Eliminar saltos de línea y caracteres de retorno de carro
        String normalized = headerName.replaceAll("[\n\r]+", " ");
        
        // Eliminar texto entre paréntesis (indicativos de requerimiento)
        normalized = normalized.replaceAll("\\s*\\([^)]+\\)\\s*", "");
        
        // Limpiar espacios múltiples y trim
        normalized = normalized.replaceAll("\\s+", " ").trim();
        
        return normalized;
    }
}

package co.unicauca.edu.co.contables.configuration.thirds.domain.utils;

import java.text.Normalizer;

/**
 * @brief Utilidad para normalizar cadenas de texto para validaciones de unicidad
 *
 * Clase de utilidad que proporciona métodos para limpiar, normalizar y formatear
 * nombres y textos de manera consistente, especialmente para validaciones de unicidad,
 * compatibilidad con Excel, y comparaciones case-insensitive.
 */
public final class StringNormalizer {

    private StringNormalizer() {
        throw new UnsupportedOperationException("StringNormalizer es una clase de utilidad y no debe ser instanciada");
    }

    // ===== NORMALIZACIONES BÁSICAS =====

    /**
     * @brief Normaliza un nombre de manera consistente para validación y almacenamiento
     *
     * Elimina tildes/acentos, espacios extra y capitaliza la primera letra.
     * Este formato se usa tanto para comparar como para guardar en la base de datos.
     * @param input el texto a normalizar
     * @return el texto normalizado, o null si el input es null
     */
    public static String normalize(String input) {
        if (input == null || input.trim().isEmpty()) {
            return input;
        }

        String cleaned = removeAccents(input.trim().toLowerCase());
        return capitalizeFirstLetter(cleaned);
    }

    /**
     * @brief Normaliza un nombre eliminando acentos pero preservando el formato de mayúsculas/minúsculas
     *
     * Útil para nombres descriptivos donde se quiere mantener la presentación original
     * pero eliminar caracteres especiales que puedan causar problemas de búsqueda.
     * @param input el texto a normalizar
     * @return el texto sin acentos pero con formato original, o null si el input es null
     */
    public static String normalizePreservingCase(String input) {
        if (input == null || input.trim().isEmpty()) {
            return input;
        }

        return removeAccents(input.trim());
    }

    /**
     * @brief Normaliza un texto para comparaciones case-insensitive
     *
     * Elimina acentos, espacios extra y convierte a minúsculas para facilitar
     * comparaciones insensibles a mayúsculas/minúsculas y acentos.
     * @param input el texto a normalizar para comparación
     * @return el texto normalizado para comparación, o null si el input es null
     */
    public static String normalizeForComparison(String input) {
        if (input == null || input.trim().isEmpty()) {
            return input;
        }

        return removeAccents(input.trim().toLowerCase());
    }

    /**
     * @brief Normaliza un código/referencia manteniendo el formato en mayúsculas
     *
     * Elimina tildes/acentos, espacios extra y caracteres especiales, manteniendo solo letras y números.
     * Ideal para códigos, referencias y valores que deben ser únicos e insensibles a formato.
     * @param input el código/referencia a normalizar
     * @return el código normalizado en mayúsculas, o null si el input es null
     */
    public static String normalizeCode(String input) {
        if (input == null || input.trim().isEmpty()) {
            return input;
        }

        String normalized = removeAccents(input.trim().toUpperCase());
        // Eliminar caracteres especiales, dejando solo letras y números
        normalized = normalized.replaceAll("[^A-Z0-9Ñ]", "");

        return normalized;
    }

    /**
     * @brief Elimina tildes y acentos de una cadena de texto
     *
     * Utiliza la clase Normalizer de Java para descomponer caracteres con acentos
     * y eliminar los caracteres diacríticos, obteniendo texto plano ASCII.
     * @param input el texto del cual eliminar acentos
     * @return el texto sin acentos, o null si el input es null
     */
    private static String removeAccents(String input) {
        if (input == null) {
            return null;
        }

        // Preservar ñ y Ñ antes de normalizar
        String preserved = input.replace("ñ", "###PRESERVE_ENYE###").replace("Ñ", "###PRESERVE_ENYE_MAYUS###");

        // Normaliza a forma NFD (descompone caracteres con acentos)
        String normalized = Normalizer.normalize(preserved, Normalizer.Form.NFD);

        // Elimina los caracteres diacríticos (tildes, acentos, etc.)
        String withoutAccents = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // Restaurar ñ y Ñ
        return withoutAccents.replace("###PRESERVE_ENYE###", "ñ").replace("###PRESERVE_ENYE_MAYUS###", "Ñ");
    }

    // ===== NORMALIZACIONES PARA EXCEL =====

    /**
     * @brief Normaliza un nombre para uso en rangos con nombre de Excel
     *
     * Aplica normalización específica para compatibilidad con fórmulas INDIRECT de Excel.
     * Convierte espacios en guiones bajos y normaliza caracteres especiales básicos.
     * @param input el texto a normalizar para rango con nombre
     * @return el texto normalizado para Excel, o "Unknown" si el input es inválido
     */
    public static String normalizeForExcelNamedRange(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "Unknown";
        }

        return input.trim()
                .replaceAll("\\s+", "_")
                .replaceAll("ñ", "n")
                .replaceAll("ó", "o")
                .replaceAll("á", "a")
                .replaceAll("é", "e")
                .replaceAll("í", "i")
                .replaceAll("ú", "u")
                .replaceAll(",", "_")
                .replaceAll("\\.", "_");
    }

    /**
     * @brief Normaliza un nombre para uso general en Excel (rangos con nombre, encabezados, etc.)
     *
     * Aplica normalización completa para manejar acentos y caracteres especiales.
     * Incluye validaciones de longitud y caracteres válidos para Excel (máximo 255 caracteres).
     * @param input el texto a normalizar para Excel
     * @return el texto normalizado para Excel con validaciones aplicadas
     */
    public static String normalizeForExcel(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "Unknown";
        }

        String normalized = input.trim()
                   .replaceAll("\\s+", "_")           // Espacios por guión bajo
                   .replaceAll("[áàäâ]", "a")         // Acentos a
                   .replaceAll("[éèëê]", "e")         // Acentos e
                   .replaceAll("[íìïî]", "i")         // Acentos i
                   .replaceAll("[óòöô]", "o")         // Acentos o
                   .replaceAll("[úùüû]", "u")         // Acentos u
                   .replaceAll("[ñ]", "n")            // Ñ por n
                   .replaceAll("[ÁÀÄÂ]", "A")         // Acentos A
                   .replaceAll("[ÉÈËÊ]", "E")         // Acentos E
                   .replaceAll("[ÍÌÏÎ]", "I")         // Acentos I
                   .replaceAll("[ÓÒÖÔ]", "O")         // Acentos O
                   .replaceAll("[ÚÙÜÛ]", "U")         // Acentos U
                   .replaceAll("[Ñ]", "N")            // Ñ por N
                   .replaceAll("[^a-zA-Z0-9_]", "_")  // Otros caracteres especiales por guión bajo
                   .replaceAll("_+", "_")             // Múltiples guiones bajos por uno solo
                   .replaceAll("^_|_$", "");          // Remover guiones bajos al inicio y final

        // Validar que no esté vacío después de la normalización
        if (normalized.isEmpty()) {
            normalized = "Item_" + Math.abs(input.hashCode());
        }

        // Validar longitud máxima para Excel (255 caracteres)
        if (normalized.length() > 255) {
            normalized = normalized.substring(0, 252) + "_" + Math.abs(input.hashCode() % 100);
        }

        return normalized;
    }

    // ===== CONSTRUCCIÓN DE FÓRMULAS EXCEL =====

    /**
     * @brief Construye una fórmula de Excel con normalización de caracteres especiales usando SUBSTITUTE anidados
     *
     * Esta fórmula se ejecuta dentro de Excel para normalizar referencias de celdas dinámicamente.
     * Debe coincidir exactamente con la normalización aplicada en normalizeForExcelNamedRange().
     * @param cellReference la referencia de celda de Excel (ej: "A1", "B2")
     * @return la fórmula SUBSTITUTE anidada para normalización en Excel
     */
    public static String buildExcelNormalizationFormula(String cellReference) {
        if (cellReference == null || cellReference.trim().isEmpty()) {
            return "\"Unknown\"";
        }

        return "SUBSTITUTE(" +
                "SUBSTITUTE(" +
                    "SUBSTITUTE(" +
                        "SUBSTITUTE(" +
                            "SUBSTITUTE(" +
                                "SUBSTITUTE(" +
                                    "SUBSTITUTE(" +
                                        "SUBSTITUTE(" +
                                            "SUBSTITUTE(" + cellReference +
                                            ",\" \",\"_\")" +
                                        ",\"ñ\",\"n\")" +
                                    ",\"ó\",\"o\")" +
                                ",\"á\",\"a\")" +
                            ",\"é\",\"e\")" +
                        ",\"í\",\"i\")" +
                    ",\"ú\",\"u\")" +
                ",\",\",\"_\")" +
            ",\".\",\"_\")";
    }

    /**
     * @brief Construye una fórmula INDIRECT completa con normalización para Excel
     *
     * Combina la normalización con la función INDIRECT para crear referencias dinámicas
     * a rangos con nombre en Excel, permitiendo validaciones dependientes del contexto.
     * @param cellReference la referencia de celda de Excel
     * @param prefix prefijo opcional para el nombre del rango (ej: "Estados_")
     * @return la fórmula INDIRECT completa con normalización
     */
    public static String buildNormalizedIndirectFormula(String cellReference, String prefix) {
        String baseFormula = buildExcelNormalizationFormula(cellReference);

        if (prefix != null && !prefix.isEmpty()) {
            return "INDIRECT(\"" + prefix + "\"&" + baseFormula + ")";
        } else {
            return "INDIRECT(" + baseFormula + ")";
        }
    }

    // ===== UTILIDADES DE PROCESAMIENTO =====

    /**
     * @brief Normaliza el nombre de un encabezado de Excel eliminando texto entre paréntesis y saltos de línea
     *
     * Útil para procesar encabezados con indicativos de requerimiento, limpiando el texto
     * para obtener solo el nombre esencial del campo.
     *
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
        normalized = normalized.replaceAll("\\s*\\([^)]*\\)\\s*", "");

        normalized = normalized.replaceAll("\\s+", " ").trim();

        return normalized;
    }

    /**
     * @brief Capitaliza la primera letra de una cadena
     *
     * Función auxiliar privada que convierte la primera letra de una cadena a mayúscula,
     * dejando el resto de la cadena sin cambios.
     * @param input el texto a capitalizar
     * @return el texto con la primera letra en mayúscula, o null si el input es null
     */
    private static String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        if (input.length() == 1) {
            return input.toUpperCase();
        }

        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }
}

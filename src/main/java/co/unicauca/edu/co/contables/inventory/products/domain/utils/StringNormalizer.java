package co.unicauca.edu.co.contables.inventory.products.domain.utils;

import java.text.Normalizer;

/**
 * @brief Utilidad para normalización de cadenas de texto
 *
 * Proporciona métodos estáticos para normalizar nombres, códigos y headers,
 * eliminando acentos, espacios extra y aplicando formatos consistentes para
 * validaciones de unicidad y procesamiento de datos.
 */
public final class StringNormalizer {

    private StringNormalizer() {
        // Clase utilitaria - constructor privado
    }

    /**
     * @brief Normaliza nombres para validación y almacenamiento
     *
     * Aplica normalización completa: elimina acentos, espacios extra,
     * convierte a minúsculas y capitaliza primera letra. Ideal para nombres de entidades.
     *
     * @param input Texto a normalizar
     * @return Texto normalizado o null si input es null
     */
    public static String normalize(String input) {
        if (input == null || input.trim().isEmpty()) {
            return input;
        }
        
        String cleaned = removeAccents(input.trim().toLowerCase());
        return capitalizeFirstLetter(cleaned);
    }

    /**
     * @brief Normaliza códigos y referencias en mayúsculas
     *
     * Elimina acentos y espacios extra, convirtiendo todo a mayúsculas.
     * Ideal para códigos SKU, referencias y identificadores técnicos.
     *
     * @param input Código/referencia a normalizar
     * @return Código normalizado en mayúsculas o null si input es null
     */
    public static String normalizeCode(String input) {
        if (input == null || input.trim().isEmpty()) {
            return input;
        }
        
        return removeAccents(input.trim().toUpperCase());
    }

    /**
     * @brief Elimina tildes y acentos de texto@
     *
     * Utiliza Normalizer de Java para descomponer caracteres con acentos
     * y eliminar las marcas diacríticas, produciendo texto ASCII limpio.
     *
     * @param input Texto del cual eliminar acentos
     * @return Texto sin acentos
     */
    private static String removeAccents(String input) {
        if (input == null) {
            return null;
        }
        
        // Normaliza a forma NFD (descompone caracteres con acentos)
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        
        // Elimina los caracteres diacríticos (tildes, acentos, etc.)
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    /**
     * @brief Normaliza nombres de encabezados Excel para comparación
     *
     * Limpia y normaliza headers de Excel: elimina acentos, caracteres especiales,
     * texto entre paréntesis, y aplica reglas específicas para mapear variaciones
     * comunes de nombres de columnas.
     *
     * @param header Nombre del encabezado a normalizar
     * @return Header normalizado para comparación consistente
     */
    public static String normalizeHeaderName(String header) {
        if (header == null) {
            return null;
        }

        // Remover todo después del primer salto de línea
        String cleaned = header;
        if (cleaned.contains("\n")) {
            cleaned = cleaned.substring(0, cleaned.indexOf("\n"));
        }
        if (cleaned.contains("\r")) {
            cleaned = cleaned.substring(0, cleaned.indexOf("\r"));
        }
        
        // Remover texto entre paréntesis como "(Requerido)", "(Opcional)", etc.
        cleaned = cleaned.replaceAll("\\s*\\([^)]*?\\)\\s*", ""); // Usando lazy matching para evitar backtracking excesivo
        cleaned = cleaned.trim();

        // Reemplazar caracteres especiales comunes por encoding issues
        cleaned = cleaned.replace('Ý', 'í')  // categoría
                        .replace('¾', 'ó')  // descripción, presentación
                        .replace('Ã', 'í')  // categoría alternativo
                        .replace('³', 'ó'); // descripción alternativo

        String normalized = Normalizer.normalize(cleaned, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        normalized = normalized.toLowerCase();
        normalized = applyHeaderRules(normalized);
        return normalized.trim();
    }

    /**
     * @brief Aplica reglas específicas para mapear headers de Excel
     *
     * Convierte variaciones comunes de nombres de columnas (con acentos, en inglés,
     * abreviaturas) a los nombres canónicos definidos en ImportConstants.
     *
     * @param header Header normalizado a mapear
     * @return Nombre canónico del header o el original si no hay regla específica
     */
    private static String applyHeaderRules(String header) {
        // Reglas específicas para headers de productos
        switch (header) {
            case "nombre":
            case "name":
                return ImportConstants.NAME_COLUMN;
            case "descripcion":
            case "descripción":
            case "descripci¾n": // Manejar caracteres especiales por encoding
            case "description":
                return ImportConstants.DESCRIPTION_COLUMN;
            case "unidad de medida":
            case "unidad":
            case "uom":
                return ImportConstants.UNIT_MEASURE_COLUMN;
            case "categoria":
            case "categoría":
            case "categorÝa": // Manejar caracteres especiales por encoding
            case "category":
                return ImportConstants.CATEGORY_COLUMN;
            case "tipo de producto":
            case "tipo":
            case "product type":
                return ImportConstants.PRODUCT_TYPE_COLUMN;
            case "referencia/sku": // Manejar el formato de exportación primero (más específico)
            case "referencia":
            case "sku":
            case "reference":
                return ImportConstants.REFERENCE_COLUMN;
            case "presentacion":
            case "presentación":
            case "presentaci¾n": // Manejar caracteres especiales por encoding
            case "presentation":
                return ImportConstants.PRESENTATION_COLUMN;
            case "cantidad":
            case "quantity":
                return ImportConstants.QUANTITY_COLUMN;
            case "costo":
            case "cost":
            case "price":
                return ImportConstants.COST_COLUMN;
            case "codigo":
            case "code":
                return "Código"; // Campo opcional
            case "estado":
            case "state":
            case "status":
                return "Estado"; // Campo opcional
            default:
                return header;
        }
    }

    /**
     * @brief Capitaliza la primera letra de un texto
     *
     * Convierte la primera letra a mayúscula y el resto mantiene su formato.
     * Maneja casos edge como textos de un solo caracter.
     *
     * @param input Texto a capitalizar
     * @return Texto con primera letra en mayúscula
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

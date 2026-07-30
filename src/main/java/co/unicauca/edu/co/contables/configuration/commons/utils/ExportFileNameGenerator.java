package co.unicauca.edu.co.contables.configuration.commons.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @brief Clase utilitaria para generar nombres de archivos de exportación.
 * Centraliza la lógica de generación de nombres para mantener consistencia
 * en toda la aplicación.
 */
public final class ExportFileNameGenerator {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final String INVALID_CHARS_REGEX = "[^a-zA-Z0-9_-]";

    // Constructor privado para evitar instanciación
    private ExportFileNameGenerator() {
        throw new UnsupportedOperationException("Esta es una clase utilitaria y no debe ser instanciada");
    }

    /**
     * Genera un nombre de archivo para exportación con timestamp.
     * 
     * @param baseFileName Nombre base del archivo (ej: "centros_costo")
     * @param companyName  Nombre de la empresa (opcional)
     * @param status       Estado del filtro (true=activos, false=inactivos, null=todos)
     * @param extension    Extensión del archivo (ej: "xlsx", "pdf")
     * @return Nombre del archivo generado
     */
    public static String generateFileName(String baseFileName, String companyName, Boolean status, String extension) {
        StringBuilder fileName = new StringBuilder(baseFileName);

        // Agregar nombre de empresa si se proporciona
        if (companyName != null && !companyName.trim().isEmpty()) {
            String sanitizedCompanyName = sanitizeFileName(companyName);
            fileName.append("_").append(sanitizedCompanyName);
        }

        // Agregar sufijo de estado si se proporciona
        if (status != null) {
            fileName.append(status ? "_activos" : "_inactivos");
        }

        // Agregar timestamp
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        fileName.append("_").append(timestamp);

        // Agregar extensión
        fileName.append(".").append(extension);

        return fileName.toString();
    }

    /**
     * Genera un nombre de archivo para exportación de Excel con timestamp.
     * 
     * @param baseFileName Nombre base del archivo 
     * @param companyName  Nombre de la empresa (opcional)
     * @param status       Estado del filtro (true=activos, false=inactivos, null=todos)
     * @return Nombre del archivo Excel generado
     */
    public static String generateExcelFileName(String baseFileName, String companyName, Boolean status) {
        return generateFileName(baseFileName, companyName, status, "xlsx");
    }

    
    /**
     * Sanitiza un nombre de archivo reemplazando caracteres no válidos por guiones bajos.
     * 
     * @param fileName Nombre de archivo a sanitizar
     * @return Nombre de archivo sanitizado
     */
    public static String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return "";
        }
        return fileName.trim().replaceAll(INVALID_CHARS_REGEX, "_");
    }

    /**
     * Genera un timestamp en formato estándar para nombres de archivo.
     * 
     * @return Timestamp formateado (yyyyMMdd_HHmmss)
     */
    public static String generateTimestamp() {
        return LocalDateTime.now().format(TIMESTAMP_FORMATTER);
    }
}

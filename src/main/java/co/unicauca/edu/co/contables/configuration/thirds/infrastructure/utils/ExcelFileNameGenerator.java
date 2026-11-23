package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.utils;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @brief Generador de nombres de archivo Excel para operaciones de importación/exportación
 *
 * Utilidad especializada en crear nombres de archivo descriptivos y únicos para
 * plantillas y exportaciones de terceros. Incluye timestamps, nombres de empresa
 * y estados para facilitar identificación y organización de archivos.
 */
@Component
public class ExcelFileNameGenerator {

    /**
     * @brief Genera nombre único para plantilla de importación de terceros
     * @details Crea nombre con formato "Plantilla_Terceros_yyyyMMdd_HHmmss.xlsx"
     * que incluye timestamp para evitar conflictos de nombre.
     * @return nombre de archivo único para plantilla Excel
     */
    public String generateTemplateFileName() {
        String timestamp = generateTimestamp();
        return "Plantilla_Terceros_" + timestamp + ".xlsx";
    }
    
    /**
     * @brief Genera nombre descriptivo para archivo de exportación de terceros
     * @details Construye nombre con formato "Terceros_[Empresa]_[Estado]_yyyyMMdd_HHmmss.xlsx"
     * incluyendo nombre de empresa normalizado y estado si se especifican.
     * @param entId ID de la empresa (usado internamente, no en nombre)
     * @param companyName nombre de la empresa para incluir en el archivo (opcional)
     * @param status estado de los terceros (true=activos, false=inactivos, null=todos)
     * @return nombre de archivo descriptivo y único para exportación
     */
    public String generateExportFileName(String entId, String companyName, Boolean status) {
        String timestamp = generateTimestamp();
        StringBuilder fileName = new StringBuilder("Terceros");
        
        // Agregar nombre de empresa si se proporciona
        if (companyName != null && !companyName.trim().isEmpty()) {
            String normalizedCompanyName = normalizeForFileName(companyName);
            fileName.append("_").append(normalizedCompanyName);
        }
        
        // Agregar estado si se especifica
        if (status != null) {
            String statusText = status ? "activos" : "inactivos";
            fileName.append("_").append(statusText);
        }
        
        fileName.append("_").append(timestamp).append(".xlsx");
        return fileName.toString();
    }
    
    /**
     * @brief Normaliza nombre de empresa para uso seguro en nombres de archivo
     * @details Reemplaza espacios por guiones bajos para evitar problemas de path
     * y caracteres especiales en sistemas de archivo.
     * @param name nombre original de la empresa
     * @return nombre normalizado seguro para archivo
     */
    private String normalizeForFileName(String name) {
        return name.replace(" ", "_");
    }
    
    /**
     * @brief Genera timestamp formateado para nombres de archivo únicos
     * @details Crea timestamp con formato yyyyMMdd_HHmmss usando hora actual del sistema.
     * Garantiza unicidad de nombres de archivo en operaciones secuenciales.
     * @return timestamp formateado seguro para nombres de archivo
     */
    private String generateTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }
}

package co.unicauca.edu.co.contables.inventory.products.infraestructure.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

/**
 * @brief Generador de nombres de archivos Excel para productos
 *
 * Crea nombres de archivos descriptivos para plantillas y exportaciones
 * de productos con timestamps, nombres de empresa y estados.
 */
@Component("productsExcelFileNameGenerator")
public class ExcelFileNameGenerator {
    
    /**
     * @brief Genera nombre de archivo para plantilla de productos
     * @return nombre del archivo con formato Plantilla_Productos_timestamp.xlsx
     */
    public String generateTemplateFileName() {
        String timestamp = generateTimestamp();
        return "Plantilla_Productos_" + timestamp + ".xlsx";
    }
    
    /**
     * @brief Genera nombre de archivo para exportación de productos
     * @param entId ID de la empresa
     * @param companyName nombre de la empresa (opcional)
     * @param status estado de productos (true=activos, false=inactivos, null=todos)
     * @return nombre descriptivo con empresa, estado y timestamp
     */
    public String generateExportFileName(String entId, String companyName, Boolean status) {
        String timestamp = generateTimestamp();
        StringBuilder fileName = new StringBuilder("Productos");
        
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
     * @brief Normaliza nombre para uso en nombres de archivo
     * @param name nombre a normalizar
     * @return nombre con espacios reemplazados por guiones bajos
     */
    private String normalizeForFileName(String name) {
        return name.replace(" ", "_");
    }

    /**
     * @brief Genera timestamp en formato yyyyMMdd_HHmmss
     * @return timestamp formateado para nombres de archivo
     */
    private String generateTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }
}

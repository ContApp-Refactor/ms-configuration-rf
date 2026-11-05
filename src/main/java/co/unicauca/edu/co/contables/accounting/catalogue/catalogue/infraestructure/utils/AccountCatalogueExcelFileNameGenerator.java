package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @brief Generador de nombres de archivo para exportaciones Excel
 *
 * Utilidad para crear nombres de archivo consistentes y únicos para plantillas
 * y exportaciones de catálogo de cuentas, incluyendo timestamps y filtros aplicados.
 */
@Slf4j
@Component
public class AccountCatalogueExcelFileNameGenerator {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /**
     * @brief Genera nombre único para archivo de plantilla Excel
     *
     * Crea nombre de archivo con prefijo "Plantilla_Catalogo_Cuentas_" seguido
     * de timestamp para garantizar unicidad en descargas.
     * @return nombre de archivo con formato: Plantilla_Catalogo_Cuentas_YYYYMMDD_HHMMSS.xlsx
     */
    public String generateTemplateFileName() {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        return "Plantilla_Catalogo_Cuentas_" + timestamp + ".xlsx";
    }

    /**
     * @brief Genera nombre descriptivo para archivo de exportación Excel
     *
     * Construye nombre de archivo que incluye empresa, filtros aplicados y timestamp.
     * Formato: Catalogo_Cuentas_[Empresa]_[Estado]_[Timestamp].xlsx
     * @param entId ID de la empresa (no usado en nombre, solo para contexto)
     * @param companyName nombre de la empresa para incluir en el nombre
     * @param status filtro de estado aplicado (true=activos, false=inactivos, null=todos)
     * @return nombre de archivo con empresa, estado y timestamp incluidos
     */
    public String generateExportFileName(String entId, String companyName, Boolean status) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String companySuffix = (companyName != null && !companyName.trim().isEmpty())
            ? "_" + companyName.replaceAll("[^a-zA-Z0-9]", "_")
            : "";

        String statusSuffix = "";
        if (status != null) {
            if (status) {
                statusSuffix = "_activos";
            } else {
                statusSuffix = "_inactivos";
            }
        }

        return "Catalogo_Cuentas" + companySuffix + statusSuffix + "_" + timestamp + ".xlsx";
    }
}
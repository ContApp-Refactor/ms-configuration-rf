package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.ClassificationEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.FinancialStatusEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.NatureEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @brief Modelo de dominio para datos de cuenta parseados desde Excel
 *
 * Representa una fila completa de datos Excel parseados y validados, con campos
 * mapeados a enums del dominio. Incluye métodos utilitarios para validación
 * y logging. Estado intermedio antes de conversión a entidades de BD.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountCatalogueExcelData {

    private Integer rowNumber;
    private String idEnterprise;
    private String code;
    private String description;
    private NatureEnum nature;
    private FinancialStatusEnum financialStatus;
    private ClassificationEnum classification;
    private Boolean crossing;
    private Boolean costCenter;

    /**
     * @brief Valida presencia de campos obligatorios en registro Excel
     * @return true si todos los campos requeridos están presentes y no vacíos
     */
    public boolean hasRequiredFields() {
        return code != null && !code.trim().isEmpty()
                && description != null && !description.trim().isEmpty()
                && nature != null
                && financialStatus != null
                && classification != null;
    }

    /**
     * @brief Determina si la cuenta es auxiliar basado en longitud del código
     * @return true si el código tiene exactamente 8 dígitos (cuenta auxiliar)
     */
    public boolean isAuxiliaryAccount() {
        return code != null && code.trim().length() == 8;
    }

    /**
     * @brief Genera representación string para logging y depuración
     * @return string formateado con fila, código, descripción y naturaleza
     */
    public String toLogString() {
        return String.format("Fila %d: Código=%s, Descripción=%s, Naturaleza=%s",
                rowNumber,
                code != null ? code : "N/A",
                description != null ? description : "N/A",
                nature != null ? nature.getState() : "N/A");
    }
}


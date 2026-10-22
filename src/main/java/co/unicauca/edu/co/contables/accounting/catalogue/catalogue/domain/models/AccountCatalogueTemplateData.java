package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.ClassificationEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.FinancialStatusEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.NatureEnum;
import lombok.Builder;
import lombok.Data;

/**
 * @brief Modelo de datos para filas de plantilla Excel de catálogo de cuentas
 *
 * Representa una fila completa de datos en plantillas Excel con todos los campos
 * necesarios para importar cuentas contables, incluyendo campos opcionales.
 */
@Data
@Builder
public class AccountCatalogueTemplateData {
    private String code;
    private String name;
    private NatureEnum nature;
    private FinancialStatusEnum financialStatus;
    private ClassificationEnum classification;
    private Boolean cruce;
    private Boolean centroCosto;
}
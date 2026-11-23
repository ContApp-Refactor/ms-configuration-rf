package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @brief DTO para solicitudes de exportación de catálogo de cuentas
 *
 * Contiene los parámetros de filtro para exportación asíncrona de catálogo:
 * empresa, estado de cuentas y nombre de empresa para generación del archivo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountCatalogueExportRequest {
    private String entId;
    private String companyName;
    private Boolean status; // true=activas, false=inactivas, null=todas
}


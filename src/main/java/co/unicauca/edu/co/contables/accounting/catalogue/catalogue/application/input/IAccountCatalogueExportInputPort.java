package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.input;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.ExportJobStatus;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.dto.request.AccountCatalogueExportRequest;
import org.springframework.core.io.Resource;

import java.util.Optional;

/**
 * @brief Puerto de entrada para operaciones de exportación de catálogo de cuentas
 *
 * Define el contrato para exportar plantillas y datos del catálogo de cuentas
 * a formatos Excel con validaciones incluidas, soportando exportación asíncrona.
 */
public interface IAccountCatalogueExportInputPort {

    /**
     * @brief Exporta plantilla de catálogo de cuentas con validaciones (síncrono)
     * @param entId ID de la empresa
     * @return archivo Excel con estructura y validaciones para importación
     */
    Resource exportAccountCatalogueTemplate(String entId);

    /**
     * @brief Inicia exportación asíncrona de catálogo de cuentas
     * @param request solicitud con parámetros de exportación (entId, companyName, status)
     * @return jobId único para rastrear el estado de la exportación
     */
    String exportAccountCatalogueAsync(AccountCatalogueExportRequest request);

    /**
     * @brief Obtiene el estado actual de una exportación asíncrona
     * @param jobId identificador del trabajo de exportación
     * @return Optional con el estado del trabajo si existe
     */
    Optional<ExportJobStatus> getExportStatus(String jobId);
}
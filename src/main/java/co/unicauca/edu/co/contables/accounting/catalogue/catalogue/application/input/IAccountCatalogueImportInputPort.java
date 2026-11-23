package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.input;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.ImportJobStatus;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.dto.request.AccountCatalogueImportRequest;

import java.util.Optional;

/**
 * @brief Puerto de entrada para operaciones de importación de catálogo de cuentas
 *
 * Define el contrato para importar cuentas contables desde archivos Excel
 * de forma asíncrona con validaciones y procesamiento por lotes.
 */
public interface IAccountCatalogueImportInputPort {

    /**
     * @brief Inicia importación asíncrona de cuentas contables desde archivo Excel
     * @param request solicitud con archivo Excel y configuración de importación
     * @return jobId único para rastrear el estado de la importación
     */
    String importAccountCatalogueAsync(AccountCatalogueImportRequest request);

    /**
     * @brief Obtiene el estado actual de una importación asíncrona
     * @param jobId identificador del trabajo de importación
     * @return Optional con el estado del trabajo si existe
     */
    Optional<ImportJobStatus> getImportStatus(String jobId);
}


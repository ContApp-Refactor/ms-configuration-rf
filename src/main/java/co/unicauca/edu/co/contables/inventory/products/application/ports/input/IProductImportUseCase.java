package co.unicauca.edu.co.contables.inventory.products.application.ports.input;

import co.unicauca.edu.co.contables.inventory.products.domain.model.ImportJobStatus;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.dto.request.ProductImportRequest;

import java.util.Optional;

/**
 * @brief Puerto de entrada para importación masiva de productos desde Excel
 *
 * Define contrato para procesamiento asíncrono de archivos Excel con datos de productos,
 * incluyendo validación, transformación y persistencia masiva de productos.
 */
public interface IProductImportUseCase {

    /**
     * @brief Inicia importación asíncrona de productos desde Excel
     * @details Procesa el archivo en un thread separado para evitar timeouts en archivos grandes.
     * Retorna inmediatamente un jobId que puede usarse para consultar el estado.
     * @param request solicitud de importación conteniendo archivo y metadatos
     * @return jobId único para tracking del proceso asíncrono
     */
    String importProductsAsync(ProductImportRequest request);

    /**
     * @brief Obtiene el estado de un job de importación asíncrona
     * @param jobId identificador único del job de importación
     * @return Optional con el estado del job si existe
     */
    Optional<ImportJobStatus> getImportStatus(String jobId);
}
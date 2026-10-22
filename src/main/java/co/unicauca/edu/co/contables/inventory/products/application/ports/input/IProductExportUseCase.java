package co.unicauca.edu.co.contables.inventory.products.application.ports.input;

import co.unicauca.edu.co.contables.inventory.products.domain.model.ExportJobStatus;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.dto.request.ProductExportRequest;
import org.springframework.core.io.Resource;

import java.util.Optional;

/**
 * @brief Puerto de entrada para exportación de productos a Excel
 *
 * Define contrato para generación de archivos Excel con productos:
 * - Plantillas con validaciones (listas desplegables)
 * - Exportación asíncrona de datos existentes con filtros por estado
 */
public interface IProductExportUseCase {

    /**
     * @brief Exporta plantilla de productos con validaciones Excel
     * @param entId ID de la entidad
     * @return Resource que contiene la plantilla Excel con validaciones
     */
    Resource exportProductTemplateWithValidations(String entId);

    /**
     * @brief Inicia exportación asíncrona de productos
     * @param exportRequest solicitud de exportación con filtros
     * @return jobId único para consultar el estado
     */
    String exportProductsAsync(ProductExportRequest exportRequest);

    /**
     * @brief Obtiene el estado de un job de exportación
     * @param jobId identificador único del job
     * @return estado del job si existe
     */
    Optional<ExportJobStatus> getExportStatus(String jobId);

}

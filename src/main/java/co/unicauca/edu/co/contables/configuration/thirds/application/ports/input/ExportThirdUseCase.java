package co.unicauca.edu.co.contables.configuration.thirds.application.ports.input;

import co.unicauca.edu.co.contables.configuration.thirds.domain.model.ExportJobStatus;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.request.ThirdExportRequest;
import org.springframework.core.io.Resource;

import java.util.Optional;

/**
 * @brief Caso de uso para exportación de terceros a Excel
 *
 * Proporciona funcionalidades para exportar plantillas y datos
 * de terceros en formato Excel con validaciones integradas.
 */
public interface ExportThirdUseCase {

    /**
     * @brief Exporta plantilla de terceros con validaciones
     *
     * Genera una plantilla Excel vacía con listas desplegables
     * y validaciones de datos para facilitar la importación.
     *
     * @param entId ID de la entidad
     * @return Resource que contiene la plantilla Excel con validaciones
     */
    Resource exportThirdTemplateWithValidations(String entId);

    /**
     * @brief Exporta terceros existentes con validaciones (SÍNCRONO)
     *
     * Exporta los datos actuales de terceros en formato Excel,
     * incluyendo validaciones y listas desplegables para edición.
     *
     * @param exportRequest Los filtros y parámetros para la exportación
     * @return Resource que contiene el archivo Excel con datos y validaciones
     */
    Resource exportThirdsWithValidations(ThirdExportRequest exportRequest);

    /**
     * @brief Inicia exportación asíncrona de terceros
     *
     * Inicia el proceso de exportación en segundo plano y retorna
     * inmediatamente un ID de job para consultar el estado.
     *
     * @param exportRequest Los filtros y parámetros para la exportación
     * @return jobId único para consultar el estado de la exportación
     */
    String exportThirdsAsync(ThirdExportRequest exportRequest);

    /**
     * @brief Obtiene el estado de un job de exportación
     *
     * Permite consultar el progreso y estado de una exportación asíncrona.
     *
     * @param jobId identificador único del job
     * @return estado del job si existe, incluyendo el archivo generado si está completo
     */
    Optional<ExportJobStatus> getExportStatus(String jobId);

}

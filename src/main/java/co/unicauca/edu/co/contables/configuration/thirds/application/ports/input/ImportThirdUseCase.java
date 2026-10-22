package co.unicauca.edu.co.contables.configuration.thirds.application.ports.input;

import co.unicauca.edu.co.contables.configuration.thirds.domain.model.ImportJobStatus;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.request.ThirdImportRequest;

import java.util.Optional;

/**
 * @brief Caso de uso para importación asíncrona masiva de terceros desde Excel
 *
 * Define el contrato para la funcionalidad de importación asíncrona con tracking de estado:
 * - La importación se ejecuta de forma asíncrona en segundo plano
 * - Los duplicados se omiten automáticamente para mejorar usabilidad
 * - Se procesan archivos grandes sin límite de lote específico
 * - Permite consultar el estado del procesamiento mediante el jobId retornado
 * - Permite reimportar archivos corregidos omitiendo registros existentes
 */
public interface ImportThirdUseCase {

    /**
     * @brief Inicia una importación asíncrona de terceros desde archivo Excel
     *
     * Importa terceros desde un archivo Excel de forma asíncrona con omisión automática de duplicados.
     * Los registros duplicados se omiten silenciosamente permitiendo reimportaciones del mismo archivo.
     * El procesamiento se ejecuta en segundo plano y retorna inmediatamente el ID de job.
     *
     * @param importRequest la solicitud de importación con el archivo
     * @return ID único del job de importación para consultar su estado
     * @throws IllegalArgumentException si los parámetros son inválidos
     * @throws ExcelValidationException si el archivo Excel tiene formato inválido inicial
     */
    String importThirdsFromExcel(ThirdImportRequest importRequest);

    /**
     * @brief Consulta el estado de un job de importación asíncrona
     *
     * Permite consultar el progreso y resultados de una importación en ejecución o completada.
     * Incluye métricas detalladas, porcentaje de progreso y errores acumulados.
     *
     * @param jobId identificador único del job de importación
     * @return estado completo del job si existe, Optional.empty() si no se encuentra
     */
    Optional<ImportJobStatus> getImportStatus(String jobId);
}

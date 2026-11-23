package co.unicauca.edu.co.contables.auxiliary.domain.ports.scheduledReport;

import co.unicauca.edu.co.contables.auxiliary.domain.models.scheduledJobExecution.ReportExecution;

/**
 * @brief Puerto de salida para operaciones de escritura de ejecuciones de reportes programados.
 *
 * Define el contrato para persistir o actualizar el registro de una
 * ejecución ({@link ReportExecution}) en el almacenamiento.
 */
public interface IScheduledReportExecutionCommandRepositoryPort {
    ReportExecution save(ReportExecution execution);
}

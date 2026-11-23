package co.unicauca.edu.co.contables.auxiliary.domain.ports.scheduledReport;

import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EJobStatus;
import co.unicauca.edu.co.contables.auxiliary.domain.models.scheduling.ScheduledAuxiliaryBookJob;

/**
 * @brief Puerto de salida para operaciones de escritura de reportes programados.
 *
 * Define el contrato para guardar un job programado y para actualizar
 * su estado ({@link EJobStatus}) sin modificar el resto de campos.
 */
public interface IScheduledReportCommandRepositoryPort {
    ScheduledAuxiliaryBookJob save(ScheduledAuxiliaryBookJob job);
    void updateStatus(Long jobId, EJobStatus status);
}

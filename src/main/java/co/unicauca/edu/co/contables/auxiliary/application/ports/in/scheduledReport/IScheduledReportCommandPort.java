package co.unicauca.edu.co.contables.auxiliary.application.ports.in.scheduledReport;

import co.unicauca.edu.co.contables.auxiliary.domain.models.scheduling.ScheduledAuxiliaryBookJob;

/**
 * @brief Puerto de entrada para la escritura de reportes programados.
 *
 * Define el contrato, dentro de la capa de aplicación, para crear,
 * actualizar y cancelar trabajos programados de generación de libros
 * auxiliares.
 */
public interface IScheduledReportCommandPort {
    /**
     * @brief Crea un nuevo reporte programado.
     * @param job Configuración del trabajo programado a registrar.
     * @return Trabajo programado creado con identificadores y próxima corrida.
     */
    ScheduledAuxiliaryBookJob createScheduledReport(ScheduledAuxiliaryBookJob job);

    /**
     * @brief Actualiza un reporte programado existente.
     * @param publicId Identificador público del reporte a actualizar.
     * @param job Nuevos valores de configuración.
     * @return Trabajo programado persistido con los cambios aplicados.
     */
    ScheduledAuxiliaryBookJob updateScheduledReport(String publicId, ScheduledAuxiliaryBookJob job);

    /**
     * @brief Cancela un reporte programado.
     * @param publicId Identificador público del reporte a cancelar.
     */
    void cancelScheduledReport(String publicId);
}

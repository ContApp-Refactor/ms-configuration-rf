package co.unicauca.edu.co.contables.auxiliary.application.ports.in.scheduledReport;

import co.unicauca.edu.co.contables.auxiliary.domain.models.scheduling.ScheduledAuxiliaryBookJob;

import java.util.List;

/**
 * @brief Puerto de entrada para la consulta de reportes programados.
 *
 * Define el contrato, dentro de la capa de aplicación, para listar y
 * obtener el detalle de los trabajos programados de generación de
 * libros auxiliares.
 */
public interface IScheduledReportQueryPort {
    /**
     * @brief Lista los reportes programados de una entidad.
     * @param entId Identificador de la entidad propietaria de los reportes.
     * @return Lista de trabajos programados asociados a la entidad.
     */
    List<ScheduledAuxiliaryBookJob> listScheduledReports(String entId);

    /**
     * @brief Obtiene el detalle de un reporte programado.
     * @param publicId Identificador público del reporte programado.
     * @return Trabajo programado con su configuración completa.
     */
    ScheduledAuxiliaryBookJob getScheduledReport(String publicId);
}

package co.unicauca.edu.co.contables.auxiliary.application.ports.in.scheduledReport;

import co.unicauca.edu.co.contables.auxiliary.domain.models.scheduledJobExecution.ReportExecution;

import java.util.List;

/**
 * @brief Puerto de entrada para la consulta de ejecuciones de reportes programados.
 *
 * Define el contrato, dentro de la capa de aplicación, para listar las
 * ejecuciones registradas de un trabajo programado aplicando los
 * filtros que sean necesarios.
 */
public interface IScheduledReportExecutionsQueryPort {
    /**
     * @brief Lista las ejecuciones asociadas a un reporte programado.
     * @param scheduledReportPublicId Identificador público del reporte programado.
     * @param filters Cadena con los filtros opcionales a aplicar.
     * @return Lista de ejecuciones del trabajo que cumplen los filtros.
     */
    List<ReportExecution> listExecutionsByJob(String scheduledReportPublicId, String filters);
}

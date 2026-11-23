package co.unicauca.edu.co.contables.auxiliary.application.ports.in.scheduledReport;

/**
 * @brief Puerto de entrada para la ejecución de reportes programados vencidos.
 *
 * Define el contrato para disparar, desde el scheduler del sistema, la
 * búsqueda y ejecución de todos los reportes programados cuya fecha de
 * próxima corrida ya se cumplió.
 */
public interface IRunDueScheduledReportPort {
    /**
     * @brief Ejecuta los reportes programados cuya próxima corrida venció.
     */
    void runDueScheduledReports();
}

package co.unicauca.edu.co.contables.auxiliary.application.useCase.scheduledReport.services;

import co.unicauca.edu.co.contables.auxiliary.application.ports.in.scheduledReport.IRunDueScheduledReportPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @brief Disparador del polling de reportes programados vencidos.
 *
 * Servicio puente que el scheduler invoca periódicamente para delegar la
 * ejecución de los jobs vencidos en {@link IRunDueScheduledReportPort}.
 */
@Service
@RequiredArgsConstructor
public class DueScheduledReportsPoller {

    private final IRunDueScheduledReportPort runDueScheduledReportPort;

    public void poll() {
        this.runDueScheduledReportPort.runDueScheduledReports();
    }
}

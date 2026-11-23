package co.unicauca.edu.co.contables.auxiliary.infrastructure.in.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.auxiliary.application.useCase.scheduledReport.services.DueScheduledReportsPoller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Scheduler que dispara el polling de reportes programados vencidos.
 *
 * Componente Spring que invoca periódicamente al
 * {@link DueScheduledReportsPoller} según el intervalo configurable
 * {@code scheduled.reports.poller.delay-ms} (por defecto 60 segundos).
 * Maneja y registra cualquier excepción para evitar detener el ciclo
 * de polling ante fallas puntuales.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledReportScheduler {

    private final DueScheduledReportsPoller dueScheduledReportsPoller;

    @Scheduled(fixedDelayString = "${scheduled.reports.poller.delay-ms:60000}")
    // public void pollDueScheduledReports() {
    //     try {
    //         dueScheduledReportsPoller.poll();
    //     } catch (Exception ex) {
    //         log.error("Error running scheduled report poller", ex);
    //     }
    // }
    // @Scheduled(fixedDelayString = "${scheduled.reports.poller.delay-ms:60000}")
    public void pollDueScheduledReports() {
    log.info("[ScheduledReportScheduler] Poll triggered");
    try {
        dueScheduledReportsPoller.poll();
        log.info("[ScheduledReportScheduler] Poll finished");
    } catch (Exception ex) {
        log.error("[ScheduledReportScheduler] Error running scheduled report poller", ex);
    }
}
}

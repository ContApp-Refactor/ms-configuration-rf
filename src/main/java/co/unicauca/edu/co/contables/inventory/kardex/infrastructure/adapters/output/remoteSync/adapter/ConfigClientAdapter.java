package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.remoteSync.adapter;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.configuration.accountingCalendar.domain.services.IAccountingCalendarService;
import co.unicauca.edu.co.contables.inventory.kardex.domain.port.config.IConfigClientPort;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IFormatterResultOutputPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief In-process adapter for the accounting calendar check (paso 7).
 *
 * Replaces the former WebClient proxy to lb://CONFIG: the configuration
 * module already lives in this monolith, so the port is served directly by
 * {@link IAccountingCalendarService} without any HTTP hop.
 */
@Component("kardexConfigClientAdapter")@RequiredArgsConstructor
@Slf4j
public class ConfigClientAdapter implements IConfigClientPort {

    private final IAccountingCalendarService accountingCalendarService;
    private final IFormatterResultOutputPort formatterResultOutputPort;

    @Override
    public boolean isValidAccountingDate(String enterpriseId, LocalDate date) {
        try {
            return accountingCalendarService.existsDate(enterpriseId, date);
        } catch (Exception e) {
            log.warn("Unexpected error validating accounting date: {}", e.getMessage());
            formatterResultOutputPort.returnErrorGenericResponse(500,
                    "Unexpected error communicating with configuration service");
        }
        return false;
    }
}

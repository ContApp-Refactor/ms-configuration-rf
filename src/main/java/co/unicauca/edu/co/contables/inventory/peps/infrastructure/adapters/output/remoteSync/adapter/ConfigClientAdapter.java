package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.remoteSync.adapter;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.configuration.accountingCalendar.domain.services.IAccountingCalendarService;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IFormatterResultOutputPort;
import co.unicauca.edu.co.contables.inventory.peps.domain.port.output.external.IConfigClientPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Adapter for Configuration Service communication
 * 
 * Implements the output port used to validate accounting dates.
 * Antes de la migración llamaba por HTTP a {@code lb://CONFIG}
 * (IConfigClient + ClientConfig + WebClient). Como el calendario contable ya
 * vive dentro del monolito, ahora inyecta {@link IAccountingCalendarService}
 * directamente; la validación de fechas sigue siendo la misma.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ConfigClientAdapter implements IConfigClientPort{

    private final IAccountingCalendarService accountingCalendarService;
    private final IFormatterResultOutputPort formatterResultOutputPort;

    /**
     * @brief Validates if a date is within an allowed accounting period
     * 
     * Delegates to the in-process accounting calendar service.
     * 
     * @param enterpriseId The unique identifier of the enterprise
     * @param date The date to validate
     * @return true if the date is valid, false otherwise or if an error occurs
     */
    @Override
    public boolean isValidAccountingDate(String enterpriseId, LocalDate date) {
       
        try{

            return accountingCalendarService.existsDate(enterpriseId, date);
        
        } catch (Exception e) {
            log.warn("Unexpected error validating accounting date: {}", e.getMessage());
            formatterResultOutputPort.returnErrorGenericResponse(500,"Unexpected error communicating with configuration service");    
        }
        return false;
    }
    
}

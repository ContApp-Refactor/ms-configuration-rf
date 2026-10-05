package co.unicauca.edu.co.contables.inventory.kardex.application.service.kardex.command;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IFormatterResultOutputPort;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IMessageServicePort;
import co.unicauca.edu.co.contables.inventory.kardex.domain.port.config.IConfigClientPort;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.config.i18n.MessageKeys;

import lombok.RequiredArgsConstructor;

@Service("kardexKardexDateValidationService")@RequiredArgsConstructor
public class KardexDateValidationService {
    
    private final IConfigClientPort configClientPort;
    private final IFormatterResultOutputPort formatterResultOutputPort;
    private final IMessageServicePort messageService;

    /**
     * @brief Assigns current date if not present and validates against accounting calendar
     * @param kardex Kardex object with optional date
     * @param enterpriseId Enterprise identifier for calendar validation
     */
    public void validateAndSetDate(Kardex kardex, String enterpriseId) {
        // 1. If no date is provided, assign the current date
        if (kardex.getDate() == null) {
            kardex.addDate();
        }
        
        // 2. Convert ZonedDateTime to LocalDate for validation
        LocalDate kardexDate = kardex.getDate().toLocalDate();
        
        // 3. Validate the date (either provided or assigned) against the accounting calendar
        if (!configClientPort.isValidAccountingDate(enterpriseId, kardexDate)) {
            String errorMessage = messageService.getMessage(MessageKeys.INVALID_ACCOUNTING_DATE);
            formatterResultOutputPort.returnBusinessRuleErrorResponse(400, errorMessage);
        }
    }
}
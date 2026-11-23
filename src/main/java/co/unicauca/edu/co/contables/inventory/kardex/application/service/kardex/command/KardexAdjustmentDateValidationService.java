package co.unicauca.edu.co.contables.inventory.kardex.application.service.kardex.command;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IFormatterResultOutputPort;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IMessageServicePort;
import co.unicauca.edu.co.contables.inventory.kardex.domain.port.config.IConfigClientPort;
import co.unicauca.edu.co.contables.inventory.kardex.domain.port.kardex.IKardexQueryRepositoryPort;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.config.i18n.MessageKeys;

import lombok.RequiredArgsConstructor;

/**
 * @brief Service for validating and processing dates in kardex adjustments
 * 
 * Handles special date validation logic for adjustments:
 * - Assigns current date if null
 * - Validates date is not before last kardex record
 * - Adds one second if date matches last record's date
 * - Validates date is not in the future
 * - Validates against accounting calendar
 */
@Service
@RequiredArgsConstructor
public class KardexAdjustmentDateValidationService {
    
    private final IConfigClientPort configClientPort;
    private final IFormatterResultOutputPort formatterResultOutputPort;
    private final IMessageServicePort messageService;
    private final IKardexQueryRepositoryPort kardexQueryRepositoryPort;

    /**
     * @brief Validates and sets date for adjustment operations with special rules
     * @param kardex Kardex object with optional date
     * @param enterpriseId Enterprise identifier for calendar validation
     */
    public void validateAndSetDateForAdjustment(Kardex kardex, String enterpriseId) {
        // 1. Si no viene fecha, asignar fecha actual
        if (kardex.getDate() == null) {
            kardex.addDate();
        }
        
        ZonedDateTime adjustmentDate = kardex.getDate();
        ZonedDateTime currentDate = ZonedDateTime.now(ZoneId.of("America/Bogota"));
        
        // 2. Validar que la fecha no sea mayor al día actual
        if (adjustmentDate.isAfter(currentDate)) {
            String errorMessage = messageService.getMessage(MessageKeys.DATE_CANNOT_BE_FUTURE);
            formatterResultOutputPort.returnBusinessRuleErrorResponse(400, errorMessage);
        }
        
        // 3. Obtener el último registro de kardex para el producto
        Kardex lastKardex = kardexQueryRepositoryPort.getLatestKardexByProductId(kardex.getProductId());
        
        if (lastKardex != null && lastKardex.getDate() != null) {
            ZonedDateTime lastDate = lastKardex.getDate();
            
            // 4. Si la fecha enviada es del mismo día que el último registro, usar la fecha del último registro + 1 segundo
            if (adjustmentDate.toLocalDate().equals(lastDate.toLocalDate())) {
                kardex.setDate(lastDate.plusSeconds(1));
            } else {
                // 5. Solo validar que la fecha no sea menor si NO es del mismo día
                if (adjustmentDate.isBefore(lastDate)) {
                    String errorMessage = messageService.getMessage(MessageKeys.DATE_CANNOT_BE_BEFORE_LAST_RECORD);
                    formatterResultOutputPort.returnBusinessRuleErrorResponse(400, errorMessage);
                }
            }
        }
        
        // 6. Validar la fecha contra el calendario contable
        LocalDate kardexLocalDate = kardex.getDate().toLocalDate();
        if (!configClientPort.isValidAccountingDate(enterpriseId, kardexLocalDate)) {
            String errorMessage = messageService.getMessage(MessageKeys.INVALID_ACCOUNTING_DATE);
            formatterResultOutputPort.returnBusinessRuleErrorResponse(400, errorMessage);
        }
    }
}

package co.unicauca.edu.co.contables.inventory.peps.application.service.command;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.inventory.peps.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IFormatterResultOutputPort;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IMessageServicePort;
import co.unicauca.edu.co.contables.inventory.peps.domain.port.output.external.IConfigClientPort;
import co.unicauca.edu.co.contables.inventory.peps.domain.port.output.query.IKardexQueryOutputPort;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.config.i18n.MessageKeys;
import lombok.RequiredArgsConstructor;

/**
 * @brief Service for validating kardex adjustments
 * 
 * Provides validation logic for adjustment dates, ensuring they comply with
 * business rules and accounting periods.
 */
@Service
@RequiredArgsConstructor
public class KardexAdjustmentValidationService {
    private final IConfigClientPort configClientPort;
    private final IFormatterResultOutputPort formatterResultOutputPort;
    private final IMessageServicePort messageServicePort;
    private final IKardexQueryOutputPort kardexQueryOutputPort;

    /**
     * @brief Validates the date for a kardex adjustment
     * 
     * Checks if the date is in the future, if it's before the last record,
     * and if it falls within a valid accounting period.
     * 
     * @param kardex Kardex record to validate
     * @param enterpriseId Enterprise identifier
     */
    public void validateDateForAdjustment(Kardex kardex, String enterpriseId){
        // 1. Si no viene fecha, asignar fecha actual
        if(kardex.getDate()==null){
            kardex.addDate();
        }

        ZonedDateTime adjustmentDate = kardex.getDate();
        ZonedDateTime currentDate = ZonedDateTime.now(ZoneId.of("America/Bogota"));
        
        if(adjustmentDate.isAfter(currentDate)){
            String errorMessage=messageServicePort.getMessage(MessageKeys.DATE_CANNOT_BE_FUTURE);
            formatterResultOutputPort.returnBusinessRuleErrorResponse(400, errorMessage);

        }

        Kardex lastKardex=kardexQueryOutputPort.getLatestKardexByProductId(kardex.getProduct().getProductId());

        if(lastKardex!=null && lastKardex.getDate()!=null){
            ZonedDateTime lastDate = lastKardex.getDate();
             // 4. Si la fecha enviada es del mismo día que el último registro, usar la fecha del último registro + 1 segundo
            if(adjustmentDate.toLocalDate().equals(lastDate.toLocalDate())){
                kardex.setDate(lastDate.plusSeconds(1));
            }else{
                // 5. Solo validar que la fecha no sea menor si NO es del mismo día
                if(adjustmentDate.isBefore(lastDate)){
                    String errorMessage=messageServicePort.getMessage(MessageKeys.DATE_CANNOT_BE_BEFORE_LAST_RECORD);
                    formatterResultOutputPort.returnBusinessRuleErrorResponse(400, errorMessage);

                }
            }

        }

        LocalDate kardexLocalDate = kardex.getDate().toLocalDate();
        if(!configClientPort.isValidAccountingDate(enterpriseId, kardexLocalDate)){
            String errorMessage =messageServicePort.getMessage(MessageKeys.INVALID_ACCOUNTING_DATE );
            formatterResultOutputPort.returnErrorGenericResponse(400, errorMessage);
        }


    }
    
}

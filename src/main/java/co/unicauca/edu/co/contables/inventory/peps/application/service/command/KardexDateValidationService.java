package co.unicauca.edu.co.contables.inventory.peps.application.service.command;

import java.time.LocalDate;
import org.springframework.stereotype.Service;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IFormatterResultOutputPort;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IMessageServicePort;
import co.unicauca.edu.co.contables.inventory.peps.domain.port.output.external.IConfigClientPort;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.config.i18n.MessageKeys;
import lombok.RequiredArgsConstructor;

/**
 * @brief Service for validating kardex dates
 * 
 * Ensures kardex dates are valid according to accounting rules
 * and assigns default dates when missing.
 */
@Service
@RequiredArgsConstructor
public class KardexDateValidationService {
    private final IConfigClientPort  configClientPort;  
    private final IFormatterResultOutputPort formatterResultOutputPort;
    private final IMessageServicePort messageServicePort;


    /**
     * @brief Assigns current date if not present and validates against accounting calendar
     * @param kardex Kardex object with optional date
     * @param enterpiseId Enterprise identifier for calendar validation
     */
    public void validateAndSetDate(Kardex kardex,String enterpiseId){

        if(kardex.getDate() == null){
            kardex.addDate();
        }

        LocalDate kardexDate = kardex.getDate().toLocalDate();

        if(!configClientPort.isValidAccountingDate(enterpiseId, kardexDate)){
            String errorMessage =messageServicePort.getMessage(MessageKeys.INVALID_ACCOUNTING_DATE );
            formatterResultOutputPort.returnErrorGenericResponse(400, errorMessage);
        }
    }

}

package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.validation;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IMessageServicePort;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.request.KardexByDateDTORequest;


/**
 * @brief Validator for date range consistency
 * 
 * Implements the logic for the @ValidDateRange annotation.
 * Ensures that start and end dates are either both null or both present,
 * and that the end date is not before the start date.
 */
public class KardexDateRangeValidator implements ConstraintValidator<ValidDateRange,KardexByDateDTORequest> {

     @Autowired
    private IMessageServicePort messageService;
    
    /**
     * @brief Validates the date range in the DTO
     * 
     * @param value The DTO containing start and end dates
     * @param context Context for custom constraint violations
     * @return true if valid, false otherwise
     */
    @Override
    public boolean isValid(KardexByDateDTORequest value, ConstraintValidatorContext context) {
        if (value == null) return true;

        LocalDate start = value.getStartDate();
        LocalDate end = value.getEndDate();

        // Ambos nulos 
        if (start == null && end == null) return true;

        // Uno nulo → error
        if (start == null || end == null) {
            context.disableDefaultConstraintViolation();
            String message = messageService.getMessage(
                "kardex.validation.date.range.incomplete", 
                "Ambas fechas deben estar presentes o ambas deben ser nulas"
            );
            context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
            return false;
        }

        // end >= start
        if (end.isBefore(start)) {
            context.disableDefaultConstraintViolation();
            String message = messageService.getMessage(
                "kardex.validation.date.range.invalid", 
                "La fecha final no puede ser anterior a la fecha inicial"
            );
            context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
            return false;
        }

        return true;
    }
    
}

package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.validation;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.inventory.stock.domain.port.IMessageServicePort;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.dto.request.KardexFilterDto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * @brief Custom validator for Kardex date range constraints
 * 
 * Validates that both start and end dates are either present or null,
 * and ensures end date is not before start date.
 */
@Component
public class KardexDateRangeValidator implements ConstraintValidator<ValidDateRange, KardexFilterDto> {

    @Autowired
    private IMessageServicePort messageService;

    /**
     * @brief Validates the date range in KardexFilterDto
     * @param value The KardexFilterDto to validate
     * @param context Validation context for custom error messages
     * @return True if validation passes, false otherwise
     */
    @Override
    public boolean isValid(KardexFilterDto value, ConstraintValidatorContext context) {
        if (value == null) return true;

        LocalDate start = value.getStartDate();
        LocalDate end = value.getEndDate();

        // Both null - valid
        if (start == null && end == null) return true;

        // One null - error
        if (start == null || end == null) {
            context.disableDefaultConstraintViolation();
            String message = messageService.getMessage(
                "kardex.validation.date.range.incomplete", 
                "Both dates must be present or both must be null"
            );
            context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
            return false;
        }

        // end >= start
        if (end.isBefore(start)) {
            context.disableDefaultConstraintViolation();
            String message = messageService.getMessage(
                "kardex.validation.date.range.invalid", 
                "End date cannot be before start date"
            );
            context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
            return false;
        }

        return true;
    }
}

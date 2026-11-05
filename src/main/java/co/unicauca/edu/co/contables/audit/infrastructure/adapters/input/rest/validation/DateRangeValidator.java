package co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.rest.validation;

import java.time.temporal.ChronoUnit;

import co.unicauca.edu.co.contables.audit.infrastructure.adapters.input.rest.dto.request.DateRangeRequest;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DateRangeValidator implements ConstraintValidator<ValidateRange, DateRangeRequest> {

    private static final long MAX_DAYS = 365;

    @Override
    public boolean isValid(DateRangeRequest request, ConstraintValidatorContext context) {
        if (request.getDateFrom() == null || request.getDateTo() == null) {
            return true; 
        }

        if (!request.getDateFrom().isBefore(request.getDateTo())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("dateFrom must be before dateTo")
                .addConstraintViolation();
            return false;
        }

        long daysBetween = ChronoUnit.DAYS.between(
            request.getDateFrom().toLocalDate(),
            request.getDateTo().toLocalDate()
        );
        
        if (daysBetween > MAX_DAYS) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "Date range cannot exceed " + MAX_DAYS + " days"
            ).addConstraintViolation();
            return false;
        }
        
        return true;
    }
}

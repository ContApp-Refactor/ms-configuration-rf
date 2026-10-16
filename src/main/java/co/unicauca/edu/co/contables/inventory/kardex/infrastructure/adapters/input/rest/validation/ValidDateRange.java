package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * @brief Custom validation annotation for date range constraints
 * 
 * Validates that date ranges have consistent null/non-null values
 * and that end dates are not before start dates.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = KardexDateRangeValidator.class)
@Documented
public @interface ValidDateRange {
    
    /**
     * @brief Default validation error message
     * @return The error message template
     */
    String message() default "Invalid dates: both must be present and end date cannot be before start date.";
    
    /**
     * @brief Validation groups for conditional validation
     * @return Array of validation groups
     */
    Class<?>[] groups() default {};
    
    /**
     * @brief Payload for validation metadata
     * @return Array of payload classes
     */
    Class<? extends Payload>[] payload() default {};
}

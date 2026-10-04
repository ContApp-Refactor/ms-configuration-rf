package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * @brief Custom annotation for date range validation
 * 
 * Applied to KardexByDateDTORequest to enforce consistency between start and end dates.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy =KardexDateRangeValidator.class )
@Documented
public @interface ValidDateRange {
    String message() default "Fechas inválidas: ambas deben estar presentes y la final no puede ser anterior a la inicial.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    
}

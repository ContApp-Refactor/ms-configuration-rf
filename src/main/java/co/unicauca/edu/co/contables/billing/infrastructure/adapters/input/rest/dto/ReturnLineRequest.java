package co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * Linea de una devolucion: solo la linea de la factura original y la cantidad a devolver. Precio e
 * IVA se heredan de la original y nunca se leen del cliente (RET-2).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ReturnLineRequest(
        @NotNull @Positive Integer sourceLineNo,
        @NotNull @Positive BigDecimal cantidad) {
}

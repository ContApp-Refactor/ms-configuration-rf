package co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

/**
 * Linea de un borrador de factura. {@code precioUnitarioCents} e {@code ivaPct} nulos = seguir el
 * catalogo (DOC-4, RN-11). Cualquier otro campo enviado por el cliente (totales, nombre,
 * referencias de catalogo) se ignora.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record InvoiceLineRequest(
        @NotBlank String productId,
        @NotNull @Positive BigDecimal cantidad,
        @PositiveOrZero Long precioUnitarioCents,
        @DecimalMin("0") @DecimalMax("100") BigDecimal ivaPct) {
}

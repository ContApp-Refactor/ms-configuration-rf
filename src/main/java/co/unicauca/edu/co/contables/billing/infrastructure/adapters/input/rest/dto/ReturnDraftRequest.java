package co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

/**
 * Contenido completo de un borrador de devolucion (reemplazo total en PUT). {@code invoiceId} es la
 * factura original (UUID como texto); {@code date} nula = hoy al crear / conservar al actualizar;
 * {@code reason} puede faltar mientras es borrador pero es obligatorio al emitir; {@code version} se
 * verifica si se envia. Totales, precios e IVA enviados por el cliente se ignoran.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ReturnDraftRequest(
        @NotBlank @Size(max = 36) String invoiceId,
        LocalDate date,
        @Size(max = 500) String reason,
        @NotNull @Size(max = 500) List<@NotNull @Valid ReturnLineRequest> lines,
        Long version) {
}

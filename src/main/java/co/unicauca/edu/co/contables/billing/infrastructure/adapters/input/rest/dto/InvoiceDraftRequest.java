package co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

/**
 * Contenido completo de un borrador de factura (reemplazo total en PUT). {@code clientId} nulo o
 * vacio = sin tercero; {@code date} nula = hoy al crear / conservar al actualizar; {@code version}
 * se verifica si se envia (DRF-3). Totales y datos de catalogo enviados por el cliente se ignoran
 * (DOC-4).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record InvoiceDraftRequest(
        @Size(max = 20) String clientId,
        LocalDate date,
        @Size(max = 50) String supplierInvoiceNumber,
        @NotNull @Size(max = 500) List<@NotNull @Valid InvoiceLineRequest> lines,
        Long version) {
}

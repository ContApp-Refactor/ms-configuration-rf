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
        Long version,
        @Valid PaymentRequest payment) {

    /** Compatibilidad: sin forma de pago (compras). */
    public InvoiceDraftRequest(String clientId, LocalDate date, String supplierInvoiceNumber,
            List<InvoiceLineRequest> lines, Long version) {
        this(clientId, date, supplierInvoiceNumber, lines, version, null);
    }

    /**
     * Metodo de pago de la factura de VENTA; en compras
     * se ignora. Campos desconocidos de clientes anteriores (form, termDays, dueDate, bankAccountId) no se leen.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PaymentRequest(Long methodId) {
    }
}

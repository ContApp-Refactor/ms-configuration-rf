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
 * (DOC-4). {@code withholdings} (retenciones, solo facturas) reemplaza el conjunto completo; nulo o vacio =
 * sin retenciones.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record InvoiceDraftRequest(
        @Size(max = 20) String clientId,
        LocalDate date,
        @Size(max = 50) String supplierInvoiceNumber,
        @NotNull @Size(max = 500) List<@NotNull @Valid InvoiceLineRequest> lines,
        Long version,
        @Valid PaymentRequest payment,
        @Size(max = 10) List<@NotNull @Valid WithholdingRequest> withholdings) {

    /** Compatibilidad: sin retenciones. */
    public InvoiceDraftRequest(String clientId, LocalDate date, String supplierInvoiceNumber,
            List<InvoiceLineRequest> lines, Long version, PaymentRequest payment) {
        this(clientId, date, supplierInvoiceNumber, lines, version, payment, null);
    }

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

    /**
     * Retencion pedida: {@code code} (RETE_IVA, RETE_ICA, RETE_FUENTE, AUTO_RENTA solo venta) e impuesto de
     * la empresa ({@code taxId}) cuya tarifa se aplica al subtotal. Se valida en el dominio (422).
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WithholdingRequest(String code, Long taxId) {
    }
}

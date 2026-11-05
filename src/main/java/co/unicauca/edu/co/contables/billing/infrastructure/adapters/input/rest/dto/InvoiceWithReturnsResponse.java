package co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.dto;

import java.time.LocalDate;

/** Fila de "facturas con devolucion" (HU-24): datos de la factura, cuantas devoluciones, por cuanto y estado derivado. */
public record InvoiceWithReturnsResponse(
        String invoiceId,
        String invoiceNumber,
        String kind,
        String supplierInvoiceNumber,
        String clientName,
        LocalDate invoiceDate,
        long invoiceTotalCents,
        int returnsCount,
        long returnedTotalCents,
        String state) {
}

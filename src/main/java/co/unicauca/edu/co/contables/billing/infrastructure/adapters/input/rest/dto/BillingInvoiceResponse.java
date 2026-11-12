package co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Factura completa. Ids como string; {@code number} es el consecutivo con prefijo (FV-000123) o
 * nulo en borrador; {@code kind} es SALE o PURCHASE; {@code returnStatus} es derivado y puede ser
 * nulo; {@code issuer} es el emisor congelado al emitir (RN-06) y es nulo en borrador. {@code payment} es la forma de pago de la factura de venta
 * (nulo en compras y en ventas anteriores a este dato). {@code withholdings} son las retenciones
 * (lista vacia, nunca nula, si no hay): en borrador con la tarifa vigente; en emitida/anulada, el snapshot.
 * {@code clientAddress/clientCity/clientPhone/clientEmail} son los datos de contacto de la contraparte
 * (cliente o proveedor), opcionales: en borrador en vivo; en emitida/anulada, el snapshot congelado.
 */
public record BillingInvoiceResponse(
        String id,
        String number,
        String status,
        String kind,
        String documentType,
        String clientId,
        String clientName,
        String clientDocument,
        String supplierInvoiceNumber,
        LocalDate date,
        List<BillingInvoiceLineResponse> lines,
        BillingTotalsResponse totals,
        String returnStatus,
        Instant createdAt,
        Instant updatedAt,
        Instant emittedAt,
        Instant annulledAt,
        String annulReason,
        Long version,
        BillingIssuerResponse issuer,
        BillingPaymentResponse payment,
        List<BillingWithholdingResponse> withholdings,
        String verificationCode,
        String clientAddress,
        String clientCity,
        String clientPhone,
        String clientEmail) {

    public BillingInvoiceResponse {
        withholdings = withholdings == null ? List.of() : List.copyOf(withholdings);
    }

    /** Sin datos de contacto de la contraparte. */
    public BillingInvoiceResponse(String id, String number, String status, String kind, String documentType,
            String clientId, String clientName, String clientDocument, String supplierInvoiceNumber, LocalDate date,
            List<BillingInvoiceLineResponse> lines, BillingTotalsResponse totals, String returnStatus,
            Instant createdAt, Instant updatedAt, Instant emittedAt, Instant annulledAt, String annulReason,
            Long version, BillingIssuerResponse issuer, BillingPaymentResponse payment,
            List<BillingWithholdingResponse> withholdings, String verificationCode) {
        this(id, number, status, kind, documentType, clientId, clientName, clientDocument, supplierInvoiceNumber,
                date, lines, totals, returnStatus, createdAt, updatedAt, emittedAt, annulledAt, annulReason, version,
                issuer, payment, withholdings, verificationCode, null, null, null, null);
    }

    /** Sin retenciones. */
    public BillingInvoiceResponse(String id, String number, String status, String kind, String documentType,
            String clientId, String clientName, String clientDocument, String supplierInvoiceNumber, LocalDate date,
            List<BillingInvoiceLineResponse> lines, BillingTotalsResponse totals, String returnStatus,
            Instant createdAt, Instant updatedAt, Instant emittedAt, Instant annulledAt, String annulReason,
            Long version, BillingIssuerResponse issuer, BillingPaymentResponse payment) {
        this(id, number, status, kind, documentType, clientId, clientName, clientDocument, supplierInvoiceNumber,
                date, lines, totals, returnStatus, createdAt, updatedAt, emittedAt, annulledAt, annulReason, version,
                issuer, payment, List.of(), null);
    }
}

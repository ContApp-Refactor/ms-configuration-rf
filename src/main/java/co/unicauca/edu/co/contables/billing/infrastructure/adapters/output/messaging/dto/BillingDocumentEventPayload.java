package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.messaging.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Carga de estado del evento (event-carried state), mismo esquema para los 8 eventos (design §8).
 * Dinero en pesos con escala 2, cantidades con escala 3; los bloques opcionales viajan como null.
 * {@code payment} es siempre null en v1.
 */
public record BillingDocumentEventPayload(
        String documentId,
        Long documentVersion,
        String documentType,
        String kind,
        String number,
        LocalDate issueDate,
        Instant emittedAt,
        String emittedBy,
        String currency,
        Issuer issuer,
        Counterparty counterparty,
        String supplierInvoiceNumber,
        List<Line> lines,
        Totals totals,
        Object payment,
        Original original,
        @JsonProperty("return") ReturnInfo returnInfo,
        Annulment annulment) {

    public record Issuer(String enterpriseId, String name, String nit, String dv) {
    }

    public record Counterparty(String role, long thirdId, String name, String idType, String idNumber,
            String verificationNumber, String address, String email, String phone) {
    }

    public record Line(int lineNo, Integer sourceLineNo, long productId, String productCode, String productName,
            Long unitOfMeasureId, String unitOfMeasureName, BigDecimal quantity, BigDecimal catalogUnitPrice,
            BigDecimal unitPrice, BigDecimal taxRate, BigDecimal subtotal, BigDecimal taxAmount,
            BigDecimal total) {
    }

    public record Totals(BigDecimal subtotal, BigDecimal tax, BigDecimal total) {
    }

    public record Original(String documentId, String documentType, String number, LocalDate issueDate) {
    }

    public record ReturnInfo(String reason, Integer ordinal, String originalReturnStatus) {
    }

    public record Annulment(Instant annulledAt, String annulledBy, String reason) {
    }
}

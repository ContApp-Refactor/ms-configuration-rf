package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.messaging.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Carga de estado del evento (event-carried state), mismo esquema para los 8 eventos (design §8).
 * Dinero en pesos con escala 2, cantidades con escala 3; los bloques opcionales viajan como null.
 * {@code payment} (metodo de pago, cuenta bancaria y sus snapshots) solo
 * viaja en la factura de venta emitida; en los demas tipos es null. Es aditivo: los consumidores
 * del esquema v1 que ignoran campos desconocidos no se rompen, y quien no lo lea no se afecta.
 * Igual {@code withholdings}: retenciones de la factura (snapshot congelado al emitir; lista vacia si no hay)
 * y {@code totals.withholdings}; {@code totals.total} ya es NETO (subtotal + tax - withholdings).
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
        Payment payment,
        Original original,
        @JsonProperty("return") ReturnInfo returnInfo,
        Annulment annulment,
        List<Withholding> withholdings,
        String verificationCode) {

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

    public record Totals(BigDecimal subtotal, BigDecimal tax, BigDecimal total, BigDecimal withholdings) {
    }

    /** Retencion congelada: clase, impuesto elegido, su tarifa (%) y el monto en pesos que resta. */
    public record Withholding(String code, long taxId, String taxName, BigDecimal ratePct, BigDecimal amount) {
    }

    public record Payment(Long paymentMethodId, String paymentMethodName) {
    }

    public record Original(String documentId, String documentType, String number, LocalDate issueDate) {
    }

    public record ReturnInfo(String reason, Integer ordinal, String originalReturnStatus) {
    }

    public record Annulment(Instant annulledAt, String annulledBy, String reason) {
    }
}

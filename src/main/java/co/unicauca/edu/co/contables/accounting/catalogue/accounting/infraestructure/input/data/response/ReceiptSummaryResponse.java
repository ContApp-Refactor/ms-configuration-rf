package co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.input.data.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReceiptSummaryResponse {
    private Long id;
    private String receiptCode;
    private LocalDate issueDate;
    private BigDecimal amountPaid; // Monto aplicado a la factura
}

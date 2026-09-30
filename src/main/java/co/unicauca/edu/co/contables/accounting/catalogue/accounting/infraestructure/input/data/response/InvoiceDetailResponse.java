package co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.input.data.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InvoiceDetailResponse {
    private Long id;
    private String factCode;
    private Long clientId;
    private LocalDate creationDate;
    private LocalDate expirationDate;
    private BigDecimal totalValue;
    private BigDecimal totalPay;
    private BigDecimal pendingValue;
    private String status;
    private Integer daysInArrears;
}

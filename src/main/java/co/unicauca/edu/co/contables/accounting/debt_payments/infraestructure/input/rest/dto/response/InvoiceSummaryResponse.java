package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * DTO that contains a summary of an invoice
 * This DTO is used to enrich the response of write-off operations with invoice details
 */
@Getter
@Setter
@Builder
public class InvoiceSummaryResponse {
    private Long id;
    private String factCode;
    private Long totalValue;
    private Long pendingValue; // El saldo pendiente ANTES del castigo
    private LocalDate expirationDate;
    private Long accountingAccount; // Nueva propiedad para el codigo de cuenta contable  
}

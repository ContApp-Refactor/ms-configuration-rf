package co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class WriteOffDetail {
    private BigDecimal amountWrittenOff;
    private Long invoiceId;
    private String invoiceCode;
    private Long creditInvoiceAccount; 
}

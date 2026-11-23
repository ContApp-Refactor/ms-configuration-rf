package co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceSyncDto {
    //TODO Revisar que los nombres de los campos que devuelve el entpoint original son estos
    private Long factCode;
    private String entId;
    private Long thirdId;
    private BigDecimal totalValue;
    private BigDecimal totalPay;
    private BigDecimal pendingValue;
    private LocalDate expirationDate;
    private LocalDate creationDate;
    private boolean active;
    private Long accountingAccount;
}

package co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AccountingMovement {
    private Long id;       // ID del movimiento en la base de datos de contabilidad.
    private Long account;       // ID de la cuenta contable asociada al movimiento.
    private Long thirdPartyId;  // ID del tercero asociado al movimiento (si aplica).
    private String description; // Descripción del movimiento contable.
    private BigDecimal debit;   // Monto del débito.
    private BigDecimal credit;  // Monto del crédito.
}

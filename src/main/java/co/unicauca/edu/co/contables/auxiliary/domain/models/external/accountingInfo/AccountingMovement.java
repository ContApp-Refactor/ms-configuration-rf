package co.unicauca.edu.co.contables.auxiliary.domain.models.external.accountingInfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @brief Modelo de dominio para un movimiento contable.
 *
 * Contiene la descripción y los valores de débito y crédito asociados a
 * una cuenta dentro de un comprobante contable.
 */
@Data
@AllArgsConstructor @NoArgsConstructor
public class AccountingMovement {
    private String description;
    private Double debit;
    private Double credit;
}
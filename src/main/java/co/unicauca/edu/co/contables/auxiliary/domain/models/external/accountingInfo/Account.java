package co.unicauca.edu.co.contables.auxiliary.domain.models.external.accountingInfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @brief Modelo de dominio para una cuenta contable.
 *
 * Identifica la cuenta por su código, naturaleza (débito/crédito) y
 * nombre descriptivo dentro del plan contable.
 */
@Data
@AllArgsConstructor @NoArgsConstructor
public class Account {
    private Long code;
    private String nature;
    private String name;
}

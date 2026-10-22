package co.unicauca.edu.co.contables.auxiliary.domain.models.external.accountingInfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @brief Modelo de dominio para un comprobante contable.
 *
 * Almacena el número y el tipo del comprobante que agrupa los
 * movimientos contables registrados.
 */
@Data
@AllArgsConstructor @NoArgsConstructor
public class Voucher {
    private String number;
    private String type;
}

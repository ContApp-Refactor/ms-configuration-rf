package co.unicauca.edu.co.contables.auxiliary.domain.models.external.accountingInfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @brief Modelo de dominio para un centro de costo.
 *
 * Representa el centro de costo asociado a los movimientos contables,
 * identificado por su código y nombre.
 */
@Data
@AllArgsConstructor @NoArgsConstructor
public class CostCenter {
    private String code;
    private String name;
}

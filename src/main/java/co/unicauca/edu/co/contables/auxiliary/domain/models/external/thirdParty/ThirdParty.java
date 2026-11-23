package co.unicauca.edu.co.contables.auxiliary.domain.models.external.thirdParty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @brief Modelo de dominio para un tercero.
 *
 * Representa un tercero (cliente, proveedor, empleado) al que se asocian
 * los movimientos contables, con su identificación, nombre y tipo de
 * documento.
 */
@Data
@AllArgsConstructor @NoArgsConstructor
public class ThirdParty {
    private Long id;
    private String name;
    private Long identificationNumber;
    private String typeId;
}

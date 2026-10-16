package co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.dto.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @brief DTO REST para un comprobante contable.
 *
 * Contiene el número y tipo del comprobante usado para identificar
 * documentos fuente asociados a los movimientos contables.
 */
@Data
@AllArgsConstructor @NoArgsConstructor
public class VoucherDTO {
    private String number;
    private String type;
}

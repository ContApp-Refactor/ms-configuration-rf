package co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.dto.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @brief DTO REST para un tercero.
 *
 * Expone los datos mínimos (identificación y nombre) de un tercero
 * involucrado en los movimientos contables del libro auxiliar.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThirdPartyDTO {
    private String identification;
    private String name;
}

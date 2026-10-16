package co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.dto.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @brief DTO REST para una cuenta contable.
 *
 * Representa una cuenta (código y descripción) usada en las respuestas
 * de la capa REST para exponer la información básica de catálogo contable.
 */
@Data
@AllArgsConstructor @NoArgsConstructor
public class AccountDTO {
    private Integer code;
    private String description;
}

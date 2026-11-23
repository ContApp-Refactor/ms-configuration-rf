package co.unicauca.edu.co.contables.auxiliary.application.dto.auxiliaryBook;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @brief DTO para la información de una cuenta contable.
 *
 * Transporta los datos mínimos de identidad de la cuenta (código,
 * descripción y naturaleza) utilizados por los demás DTOs del libro
 * auxiliar para describir la cuenta asociada a un movimiento.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountDTO {
    private String nature;
    private Long accountCode;
    private String accountDescription;
}

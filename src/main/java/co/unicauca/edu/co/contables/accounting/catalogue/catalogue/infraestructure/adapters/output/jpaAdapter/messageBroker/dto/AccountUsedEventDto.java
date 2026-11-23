package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.messageBroker.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief DTO para eventos de uso de cuentas contables
 *
 * Contiene la información necesaria para actualizar el contador de uso
 * cuando otros servicios notifican que han utilizado una cuenta contable.
 * Puede llegar como evento tanto ID como código de cuenta.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AccountUsedEventDto {
    private Long account; // Información de la cuenta (ID/código)
    private String enterpriseId; // ID de empresa
    private String sourceAccountType; // "ID" o "CODE" para indicar el tipo de account
}


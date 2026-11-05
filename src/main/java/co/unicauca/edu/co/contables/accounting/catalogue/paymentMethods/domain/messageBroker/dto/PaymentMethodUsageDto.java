package co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.messageBroker.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief DTO para eventos de uso de métodos de pago
 *
 * Contiene la información necesaria para actualizar el contador de uso
 * cuando otros servicios notifican que han utilizado un método de pago.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentMethodUsageDto {
    private Long paymentMethodId;
    private String enterpriseId;
    private Integer quantityUsed;
}

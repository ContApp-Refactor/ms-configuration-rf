package co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.model;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import lombok.*;

/**
 * @brief Modelo de dominio que representa un método de pago
 *
 * Contiene la información esencial de un método de pago incluyendo
 * nombre, cuenta contable asociada y estado operativo.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethod {
    private Long id;
    private String name;
    private String accountingAccount;
    private AccountCatalogue accountingAccountEntity;
    private Boolean status;
    private String idEnterprise;
    @Builder.Default
    private Integer usageCount = 0;

    /**
     * @brief Incrementa el contador de uso del método de pago
     */
    public void incrementUsageCount() {
        this.usageCount = this.usageCount == null ? 1 : this.usageCount + 1;
    }

    /**
     * @brief Verifica si el método de pago está siendo usado
     * @return true si el método de pago tiene uso registrado
     */
    public boolean isInUse() {
        return this.usageCount != null && this.usageCount > 0;
    }
}

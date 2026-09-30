package co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.services;

/**
 * @brief Puerto de entrada para gestión de uso de métodos de pago
 *
 * Define contrato para operaciones relacionadas con el contador de uso de métodos de pago,
 * utilizado cuando otros servicios notifican que han utilizado un método de pago.
 */
public interface IPaymentMethodUsage {
    /**
     * @brief Incrementa el contador de uso de un método de pago
     * @param paymentMethodId ID del método de pago
     * @param enterpriseId ID de la empresa
     */
    void incrementUsageCount(Long paymentMethodId, String enterpriseId);
}

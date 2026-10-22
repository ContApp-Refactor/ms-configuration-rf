package co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.services;

/**
 * @brief Puerto de entrada para gestión de uso de cuentas bancarias
 *
 * Define contrato para operaciones relacionadas con el contador de uso de cuentas bancarias,
 * utilizado cuando otros servicios notifican que han utilizado una cuenta bancaria.
 */
public interface IBankAccountUsage {
    /**
     * @brief Incrementa el contador de uso de una cuenta bancaria
     * @param bankAccountId ID de la cuenta bancaria
     * @param enterpriseId ID de la empresa
     */
    void incrementUsageCount(Long bankAccountId, String enterpriseId);
}

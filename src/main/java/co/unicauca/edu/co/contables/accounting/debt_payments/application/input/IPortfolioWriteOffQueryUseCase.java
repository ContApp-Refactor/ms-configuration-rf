package co.unicauca.edu.co.contables.accounting.debt_payments.application.input;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.PortfolioWriteOff;

import java.util.List;
public interface IPortfolioWriteOffQueryUseCase {
    /**
     * @brief Find a portfolio write-off by its ID.
     * @param writeOffId The ID of the write-off to find.
     * @return The domain model.
     */
    PortfolioWriteOff findById(Long writeOffId);

    /**
     * @brief List all write-off records for a specific enterprise.
     * @param enterpriseId The ID of the enterprise.
     * @return A list of domain models.
     */
    List<PortfolioWriteOff> findByEnterpriseId(String enterpriseId);

    /**
     * @brief List all write-off records confirm or voided for a specific enterprise.
     * @param enterpriseId The ID of the enterprise.
     * @return A list of domain models.
     */
    List<PortfolioWriteOff> findConfirmedOrVoidedByEnterpriseId(String enterpriseId);
}

package co.unicauca.edu.co.contables.accounting.debt_payments.application.output;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.PortfolioWriteOff;

import java.util.List;
import java.util.Optional;

public interface IPortfolioWriteOffPersistencePort {
    /**
     * @brief Save a new portfolio write-off record.
     * @param writeOff The domain model to persist.
     * @return The persisted domain model (with the assigned ID).
     */
    PortfolioWriteOff save(PortfolioWriteOff writeOff);

    /**
     * Finds a portfolio write-off record by its ID.
     * @param writeOffId The ID to search for.
     * @return An Optional containing the domain model if found.
     */
    Optional<PortfolioWriteOff> findById(Long writeOffId);
    
    /**
     * Updates an existing portfolio write-off record.
     * @param writeOff The domain model with updated data.
     * @return The updated domain model.
     */
    PortfolioWriteOff update(PortfolioWriteOff writeOff);

    /**
     * Lists all portfolio write-off records for a specific enterprise.
     * @param enterpriseId The ID of the enterprise.
     * @return A list of domain models with the found write-offs.
     */
    List<PortfolioWriteOff> findByEnterpriseId(String enterpriseId);

    /**
     * Lists all confirmed or voided portfolio write-off records for a specific enterprise.
     * @param enterpriseId The ID of the enterprise.
     * @return A list of domain models with the found write-offs.
     */
    List<PortfolioWriteOff> findConfirmedOrVoidedByEnterpriseId(String enterpriseId);
}

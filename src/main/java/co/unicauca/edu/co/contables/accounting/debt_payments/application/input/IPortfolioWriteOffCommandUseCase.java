package co.unicauca.edu.co.contables.accounting.debt_payments.application.input;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.PortfolioWriteOff;

/**
 * Puerto de Entrada para los comandos relacionados con el Castigo de Cartera.
 */
public interface IPortfolioWriteOffCommandUseCase {

    /**
     * Create a new portfolio write-off.
     * @param writeOff The domain object to create (already validated).
     * @return The created portfolio write-off domain model.
     */
    PortfolioWriteOff createWriteOff(PortfolioWriteOff writeOff);

    /**
     * Confirm an existing portfolio write-off.
     * @param writeOffId The ID of the write-off to confirm.
     * @return The updated portfolio write-off domain model.
     */
    PortfolioWriteOff confirmWriteOff(Long writeOffId);

    /**
     * Void the confirmation of a portfolio write-off.
     * @param writeOffId The ID of the write-off to void.
     * @return The updated portfolio write-off domain model.
     */
    PortfolioWriteOff voidWriteOffConfirmation(Long writeOffId);
}

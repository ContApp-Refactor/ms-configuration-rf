package co.unicauca.edu.co.contables.accounting.debt_payments.application.output;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.Receipt;

import java.util.List;
import java.util.Optional;

public interface IReceiptQueryPersistencePort {
    /**
     * @brief Find a receipt by its ID.
     * @param id The ID of the receipt to find.
     * @return An Optional containing the Receipt domain object if found, or an empty Optional if not.
     */
    Optional<Receipt> findById(Long id);

    /**
     * @brief Find receipts by invoice ID.
     * @param invoiceId The ID of the invoice.
     * @return A list of Receipt domain objects associated with the given invoice ID.
     */
    List<Receipt> findByInvoiceId(String invoiceId);

    /**
     * @brief Find receipts by third party ID.
     * @param thirdPartyId The ID of the third party.
     * @param enterpriseId The ID of the enterprise
     * @return A list of Receipt domain objects associated with the given third party ID.
     */
    List<Receipt> findByThirdPartyId(String thirdPartyId, String enterpriseId);

    /**
     * @brief Find receipts by enterprise ID.
     * @param enterpriseId The ID of the enterprise.
     * @return A list of Receipt domain objects associated with the given enterprise ID.
     */
    List<Receipt> findByEnterpriseId(String enterpriseId);
    
    /**
     * @brief Check if a receipt exists by its ID.
     * @param id The ID of the receipt to check.
     * @return true if the receipt exists, false otherwise.
     */
    boolean existsById(Long id);
}

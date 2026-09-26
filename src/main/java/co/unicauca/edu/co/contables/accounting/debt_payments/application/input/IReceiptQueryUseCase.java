package co.unicauca.edu.co.contables.accounting.debt_payments.application.input;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.Receipt;

import java.util.List;

public interface IReceiptQueryUseCase {

    /**
     * @brief Finds a receipt by its ID.
     * @param id The ID of the receipt.
     * @return An Optional containing the found receipt, or empty if not found.
     */
    Receipt findById(Long id);

    /**
     * @brief Finds receipts by the associated invoice ID.
     * @param invoiceId The ID of the invoice.
     * @return A list of receipts associated with the given invoice ID.
     */
    List<Receipt> findByInvoiceId(String invoiceId);

    /**
     * @brief Finds receipts by the associated third party ID.
     * @param thirdPartyId The ID of the third party.
     * @param enterpriseId Thie ID of the enterprise 
     * @return A list of receipts associated with the given third party ID.
     */
    List<Receipt> findByThirdPartyId(String thirdPartyId, String enterpriseId);
    
    /**
     * @brief Finds receipts by the associated enterprise ID.
     * @param enterpriseId The ID of the enterprise.
     * @return A list of receipts associated with the given enterprise ID.
     */
    List<Receipt> findByEnterpriseId(String enterpriseId);
}

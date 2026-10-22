package co.unicauca.edu.co.contables.accounting.debt_payments.application.input;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.Receipt;

public interface IReceiptCommandUseCase {

    /**
     * @brief Create a new receipt.
     * @param receipt The domain object to create (already validated).
     * @return The created receipt domain model.
     */
    Receipt createReceipt(Receipt receipt);

    /**
     * @brief Void an existing receipt.
     * @param receiptId The ID of the receipt to void.
     * @param reasonDescription The reason for voiding the receipt.
     * @return The updated receipt domain model.
     */
    Receipt voidReceipt(Long receiptId, String reasonDescription);
}

package co.unicauca.edu.co.contables.accounting.debt_payments.application.input;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.exception.InvoiceNotFoundException;

import java.time.LocalDate;
import java.util.List;

public interface IInvoiceCommandUseCase {
    /**
     * Check one o more invoices as paid.
     * @param invoiceIds List of IDs of the invoices to write off.
     * @throws InvoiceNotFoundException if any of the specified invoices
     *         in the list are not found in the system.
     * @throws IllegalStateException if an attempt is made to write off an invoice that has already been paid.
     */
    void writeOffInvoices(List<Long> invoiceIds);

    /**
     * Update the due date of a specific invoice.
     * @param invoiceId The ID of the invoice to modify (type Long, as in your entity).
     * @param newDueDate The new due date.
     */
    void updateDueDate(Long invoiceId, LocalDate newDueDate);
}

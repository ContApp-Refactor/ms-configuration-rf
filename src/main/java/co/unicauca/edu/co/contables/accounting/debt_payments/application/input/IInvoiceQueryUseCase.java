package co.unicauca.edu.co.contables.accounting.debt_payments.application.input;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.enums.InvoiceStatus;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.Replica.InvoiceReplica;

import java.util.List;

public interface IInvoiceQueryUseCase {

    /**
     * Use case to find pending invoices by client ID.
     * @param clientId The ID of the client to query.
     * @return List of invoices with pending balance.
     */
    List<InvoiceReplica> findPendingInvoicesByClientIdAndEnterpriseId(Long clientId, String enterpriseId);

    /**
     * Use case to find invoices by client ID and status.
     * @param clientId The ID of the client to query.
     * @param status The status of the invoices to filter.
     * @param enterpriseId The ID of the enterprise to query.  
     * @return List of invoices of the client with the specified status.
     */
    List<InvoiceReplica> findStatusInvoicesByClientId(Long clientId, InvoiceStatus status, String enterpriseId);

    /**
     * Use case to find pending invoices by enterprise ID.
     * @param enterpriseId The ID of the enterprise to query.
     * @return List of pending invoices of the enterprise.
     */
    List<InvoiceReplica> findPendingInvoicesByEnterpriseId(String enterpriseId);

    /**
     * Use case to find invoices by enterprise ID.
     * @param enterpriseId The ID of the enterprise to query.
     * @return List of invoices of the enterprise.
     */
    List<InvoiceReplica> findInvoicesByEnterpriseId(String enterpriseId);

    /**
     * Use case to find an invoice by its ID.
     * @param invoiceId The ID of the invoice to query.
     * @return The found invoice or null if it does not exist.
     */
    InvoiceReplica findInvoiceById(Long invoiceId);

    /**
     * Use case to find pending invoices that expire within the next 'days' days.
     * @param enterpriseId The ID of the enterprise.
     * @param daysThreshold The number of days for the expiration threshold.
     * @return A list of InvoiceReplica domain objects representing the invoices nearing expiration.
     */
    List<InvoiceReplica> findExpiringInvoices(String enterpriseId, int daysThreshold);
}

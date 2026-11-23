package co.unicauca.edu.co.contables.accounting.treasury.application.input;

import co.unicauca.edu.co.contables.accounting.treasury.domain.model.SupplierInvoiceReplica;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.command.TreasuryCommands.DueDate;

public interface IPayableCommandUseCase {
    SupplierInvoiceReplica updateDueDate(Long id, String enterpriseId, DueDate command);

    SupplierInvoiceReplica reconcileBalance(Long id, String enterpriseId);

    int reconcileSupplierBalances(String enterpriseId, Long supplierId);
}

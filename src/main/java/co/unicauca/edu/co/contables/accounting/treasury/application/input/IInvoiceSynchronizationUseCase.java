package co.unicauca.edu.co.contables.accounting.treasury.application.input;

import co.unicauca.edu.co.contables.accounting.treasury.domain.model.command.TreasuryCommands.PurchaseInvoiceEvent;

public interface IInvoiceSynchronizationUseCase {
    void synchronize(PurchaseInvoiceEvent event);
}

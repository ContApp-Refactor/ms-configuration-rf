package co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.input.transaction;

import co.unicauca.edu.co.contables.accounting.treasury.application.input.IInvoiceSynchronizationUseCase;
import co.unicauca.edu.co.contables.accounting.treasury.application.output.IAccountCodeResolverPort;
import co.unicauca.edu.co.contables.accounting.treasury.application.output.ISupplierInvoiceProviderPort;
import co.unicauca.edu.co.contables.accounting.treasury.application.output.ITreasuryAuditPersistencePort;
import co.unicauca.edu.co.contables.accounting.treasury.application.service.InvoiceReplicaService;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.command.TreasuryCommands.PurchaseInvoiceEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TransactionalInvoiceSynchronizationUseCase implements IInvoiceSynchronizationUseCase {
    private final InvoiceReplicaService delegate;

    public TransactionalInvoiceSynchronizationUseCase(ISupplierInvoiceProviderPort invoices,
            ITreasuryAuditPersistencePort audit,
            IAccountCodeResolverPort accountCodes) {
        this.delegate = new InvoiceReplicaService(invoices, audit, accountCodes);
    }

    @Override
    @Transactional
    public void synchronize(PurchaseInvoiceEvent event) {
        delegate.synchronize(event);
    }
}

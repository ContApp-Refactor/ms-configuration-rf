package co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.input.transaction;

import co.unicauca.edu.co.contables.accounting.treasury.application.input.IPayableCommandUseCase;
import co.unicauca.edu.co.contables.accounting.treasury.application.input.IPayableQueryUseCase;
import co.unicauca.edu.co.contables.accounting.treasury.application.output.IAccountCodeResolverPort;
import co.unicauca.edu.co.contables.accounting.treasury.application.output.IExecutionContextPort;
import co.unicauca.edu.co.contables.accounting.treasury.application.output.IPayableWriteOffPersistencePort;
import co.unicauca.edu.co.contables.accounting.treasury.application.output.IPaymentVoucherQueryPersistencePort;
import co.unicauca.edu.co.contables.accounting.treasury.application.output.ISupplierInvoiceProviderPort;
import co.unicauca.edu.co.contables.accounting.treasury.application.output.ITreasuryAuditPersistencePort;
import co.unicauca.edu.co.contables.accounting.treasury.application.service.PayableQueryService;
import co.unicauca.edu.co.contables.accounting.treasury.application.service.SupplierInvoiceBalanceReconciliationService;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.SupplierInvoiceReplica;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.command.TreasuryCommands.AgingLine;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.command.TreasuryCommands.DueDate;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.command.TreasuryCommands.SupplierStatement;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TransactionalPayableUseCase implements IPayableCommandUseCase, IPayableQueryUseCase {
    private final PayableQueryService delegate;

    public TransactionalPayableUseCase(ISupplierInvoiceProviderPort invoices,
            IPaymentVoucherQueryPersistencePort vouchers, IPayableWriteOffPersistencePort writeOffs,
            ITreasuryAuditPersistencePort audit, IExecutionContextPort context,
            IAccountCodeResolverPort accountCodes) {
        SupplierInvoiceBalanceReconciliationService reconciliation =
                new SupplierInvoiceBalanceReconciliationService(invoices, vouchers, writeOffs, accountCodes);
        this.delegate = new PayableQueryService(invoices, vouchers, writeOffs, audit, context, reconciliation,
                accountCodes);
    }

    @Override @Transactional public SupplierInvoiceReplica updateDueDate(Long id, String enterpriseId, DueDate command) { return delegate.updateDueDate(id, enterpriseId, command); }
    @Override @Transactional public SupplierInvoiceReplica reconcileBalance(Long id, String enterpriseId) { return delegate.reconcileBalance(id, enterpriseId); }
    @Override @Transactional public int reconcileSupplierBalances(String enterpriseId, Long supplierId) { return delegate.reconcileSupplierBalances(enterpriseId, supplierId); }
    @Override @Transactional(readOnly = true) public List<SupplierInvoiceReplica> pending(String enterpriseId, Long supplierId) { return delegate.pending(enterpriseId, supplierId); }
    @Override @Transactional(readOnly = true) public SupplierInvoiceReplica find(Long id, String enterpriseId) { return delegate.find(id, enterpriseId); }
    @Override @Transactional(readOnly = true) public SupplierStatement statement(String enterpriseId, Long supplierId, LocalDate from, LocalDate to, String invoiceReference, Boolean active) { return delegate.statement(enterpriseId, supplierId, from, to, invoiceReference, active); }
    @Override @Transactional(readOnly = true) public List<AgingLine> aging(String enterpriseId, LocalDate cutoff, Long supplierId, String accountCode, String document) { return delegate.aging(enterpriseId, cutoff, supplierId, accountCode, document); }
}

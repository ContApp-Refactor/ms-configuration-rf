package co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.input.transaction;

import co.unicauca.edu.co.contables.accounting.treasury.application.input.IPayableWriteOffCommandUseCase;
import co.unicauca.edu.co.contables.accounting.treasury.application.input.IPayableWriteOffQueryUseCase;
import co.unicauca.edu.co.contables.accounting.treasury.application.output.IExecutionContextPort;
import co.unicauca.edu.co.contables.accounting.treasury.application.output.IPayableWriteOffPersistencePort;
import co.unicauca.edu.co.contables.accounting.treasury.application.output.IPaymentSchedulePersistencePort;
import co.unicauca.edu.co.contables.accounting.treasury.application.output.ISupplierInvoiceProviderPort;
import co.unicauca.edu.co.contables.accounting.treasury.application.output.ITreasuryAuditPersistencePort;
import co.unicauca.edu.co.contables.accounting.treasury.application.output.ITreasuryEventPublisher;
import co.unicauca.edu.co.contables.accounting.treasury.application.service.PayableWriteOffService;
import co.unicauca.edu.co.contables.accounting.treasury.application.service.PaymentScheduleBalanceGuard;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.PayableWriteOff;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.command.TreasuryCommands.AccountingResult;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.command.TreasuryCommands.WriteOff;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TransactionalPayableWriteOffUseCase
        implements IPayableWriteOffCommandUseCase, IPayableWriteOffQueryUseCase {
    private final PayableWriteOffService delegate;

    public TransactionalPayableWriteOffUseCase(IPayableWriteOffPersistencePort writeOffs,
            ISupplierInvoiceProviderPort invoices, ITreasuryEventPublisher events,
            ITreasuryAuditPersistencePort audit, IExecutionContextPort context,
            IPaymentSchedulePersistencePort schedules) {
        PaymentScheduleBalanceGuard scheduleBalanceGuard = new PaymentScheduleBalanceGuard(schedules);
        this.delegate = new PayableWriteOffService(writeOffs, invoices, events, audit, context, scheduleBalanceGuard);
    }

    @Override @Transactional public PayableWriteOff create(WriteOff command) { return delegate.create(command); }
    @Override @Transactional public PayableWriteOff confirm(Long id) { return delegate.confirm(id); }
    @Override @Transactional public PayableWriteOff discardDraft(Long id) { return delegate.discardDraft(id); }
    @Override @Transactional public PayableWriteOff voidWriteOff(Long id) { return delegate.voidWriteOff(id); }
    @Override @Transactional public void applyAccountingResult(AccountingResult result) { delegate.applyAccountingResult(result); }
    @Override @Transactional(readOnly = true) public PayableWriteOff find(Long id) { return delegate.find(id); }
    @Override @Transactional(readOnly = true) public List<PayableWriteOff> list(String enterpriseId) { return delegate.list(enterpriseId); }
}

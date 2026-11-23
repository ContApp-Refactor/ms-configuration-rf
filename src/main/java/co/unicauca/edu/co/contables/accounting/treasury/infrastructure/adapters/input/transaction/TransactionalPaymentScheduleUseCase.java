package co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.input.transaction;

import co.unicauca.edu.co.contables.accounting.treasury.application.input.IPaymentScheduleCommandUseCase;
import co.unicauca.edu.co.contables.accounting.treasury.application.input.IPaymentScheduleExecutionUseCase;
import co.unicauca.edu.co.contables.accounting.treasury.application.input.IPaymentScheduleQueryUseCase;
import co.unicauca.edu.co.contables.accounting.treasury.application.input.IPaymentVoucherCommandUseCase;
import co.unicauca.edu.co.contables.accounting.treasury.application.input.IPaymentVoucherQueryUseCase;
import co.unicauca.edu.co.contables.accounting.treasury.application.output.IExecutionContextPort;
import co.unicauca.edu.co.contables.accounting.treasury.application.output.IPaymentSchedulePersistencePort;
import co.unicauca.edu.co.contables.accounting.treasury.application.output.ISupplierInvoiceProviderPort;
import co.unicauca.edu.co.contables.accounting.treasury.application.output.ITimeProviderPort;
import co.unicauca.edu.co.contables.accounting.treasury.application.output.ITransactionRunnerPort;
import co.unicauca.edu.co.contables.accounting.treasury.application.output.IPaymentMethodProviderPort;
import co.unicauca.edu.co.contables.accounting.treasury.application.output.IPaymentVoucherQueryPersistencePort;
import co.unicauca.edu.co.contables.accounting.treasury.application.service.PaymentScheduleBalanceGuard;
import co.unicauca.edu.co.contables.accounting.treasury.application.service.PaymentScheduleService;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.PaymentSchedule;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.command.TreasuryCommands.Schedule;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.command.TreasuryCommands.ScheduleFilter;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.command.TreasuryCommands.AccountingResult;
import java.time.LocalDate;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TransactionalPaymentScheduleUseCase implements IPaymentScheduleCommandUseCase,
        IPaymentScheduleQueryUseCase, IPaymentScheduleExecutionUseCase {
    private final PaymentScheduleService delegate;

    public TransactionalPaymentScheduleUseCase(IPaymentSchedulePersistencePort schedules,
            ISupplierInvoiceProviderPort invoices, IPaymentVoucherCommandUseCase voucherCommands,
            IPaymentVoucherQueryUseCase voucherQueries,
            IPaymentVoucherQueryPersistencePort voucherQueryPersistence,
            IExecutionContextPort context,
            ITransactionRunnerPort transactions, ITimeProviderPort time,
            IPaymentMethodProviderPort paymentMethods) {
        PaymentScheduleBalanceGuard scheduleBalanceGuard = new PaymentScheduleBalanceGuard(schedules);
        this.delegate = new PaymentScheduleService(schedules, invoices, voucherCommands,
                voucherQueries, voucherQueryPersistence, context, transactions, time, paymentMethods,
                scheduleBalanceGuard);
    }

    @Override @Transactional public PaymentSchedule create(Schedule command) { return delegate.create(command); }
    @Override @Transactional public PaymentSchedule update(Long id, Schedule command) { return delegate.update(id, command); }
    @Override @Transactional public void delete(Long id) { delegate.delete(id); }
    @Override @Transactional public PaymentSchedule cancel(Long id) { return delegate.cancel(id); }
    @Override @Transactional(readOnly = true) public PaymentSchedule find(Long id) { return delegate.find(id); }
    @Override @Transactional(readOnly = true) public List<PaymentSchedule> list(ScheduleFilter filter) { return delegate.list(filter); }
    @Override @Transactional public PaymentSchedule execute(Long id) { return delegate.execute(id); }
    @Override public void executeDue(LocalDate date) { delegate.executeDue(date); }
    @Override @Transactional public void applyAccountingResult(AccountingResult result) { delegate.applyAccountingResult(result); }
    @Override public void recoverAbandoned(Instant before) { delegate.recoverAbandoned(before); }
}

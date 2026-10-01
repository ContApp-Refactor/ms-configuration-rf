package co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.input.transaction;

import co.unicauca.edu.co.contables.accounting.treasury.application.input.IPaymentVoucherCommandUseCase;
import co.unicauca.edu.co.contables.accounting.treasury.application.input.IPaymentVoucherQueryUseCase;
import co.unicauca.edu.co.contables.accounting.treasury.application.output.IExecutionContextPort;
import co.unicauca.edu.co.contables.accounting.treasury.application.output.IPayableWriteOffPersistencePort;
import co.unicauca.edu.co.contables.accounting.treasury.application.output.IPaymentVoucherCommandPersistencePort;
import co.unicauca.edu.co.contables.accounting.treasury.application.output.IPaymentVoucherQueryPersistencePort;
import co.unicauca.edu.co.contables.accounting.treasury.application.output.ISupplierInvoiceProviderPort;
import co.unicauca.edu.co.contables.accounting.treasury.application.output.ITreasuryAuditPersistencePort;
import co.unicauca.edu.co.contables.accounting.treasury.application.output.ITreasuryEventPublisher;
import co.unicauca.edu.co.contables.accounting.treasury.application.output.IPaymentMethodProviderPort;
import co.unicauca.edu.co.contables.accounting.treasury.application.output.IPaymentSchedulePersistencePort;
import co.unicauca.edu.co.contables.accounting.treasury.application.service.PaymentScheduleBalanceGuard;
import co.unicauca.edu.co.contables.accounting.treasury.application.service.PaymentVoucherService;
import co.unicauca.edu.co.contables.accounting.treasury.application.service.SupplierInvoiceBalanceReconciliationService;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.PaymentVoucher;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.command.TreasuryCommands.AccountingResult;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.command.TreasuryCommands.PageResult;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.command.TreasuryCommands.Voucher;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.command.TreasuryCommands.VoucherFilter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TransactionalPaymentVoucherUseCase
        implements IPaymentVoucherCommandUseCase, IPaymentVoucherQueryUseCase {
    private final PaymentVoucherService delegate;

    public TransactionalPaymentVoucherUseCase(IPaymentVoucherCommandPersistencePort voucherCommands,
            IPaymentVoucherQueryPersistencePort voucherQueries,
            ISupplierInvoiceProviderPort invoices, ITreasuryEventPublisher events,
            ITreasuryAuditPersistencePort audit, IExecutionContextPort context,
            IPaymentMethodProviderPort paymentMethods,
            IPayableWriteOffPersistencePort writeOffs,
            IPaymentSchedulePersistencePort schedules) {
        SupplierInvoiceBalanceReconciliationService reconciliation =
                new SupplierInvoiceBalanceReconciliationService(invoices, voucherQueries, writeOffs);
        PaymentScheduleBalanceGuard scheduleBalanceGuard = new PaymentScheduleBalanceGuard(schedules);
        this.delegate = new PaymentVoucherService(voucherCommands, voucherQueries, invoices, events, audit, context,
                paymentMethods, reconciliation, scheduleBalanceGuard);
    }

    @Override @Transactional public PaymentVoucher create(Voucher command) { return delegate.create(command); }
    @Override @Transactional public PaymentVoucher update(Long id, Voucher command) { return delegate.update(id, command); }
    @Override @Transactional public void delete(Long id, String enterpriseId) { delegate.delete(id, enterpriseId); }
    @Override @Transactional public PaymentVoucher post(Long id, String enterpriseId, String key) { return delegate.post(id, enterpriseId, key); }
    @Override @Transactional public PaymentVoucher voidVoucher(Long id, String enterpriseId, String reason) { return delegate.voidVoucher(id, enterpriseId, reason); }
    @Override @Transactional public void applyAccountingResult(AccountingResult result) { delegate.applyAccountingResult(result); }
    @Override @Transactional(readOnly = true) public PaymentVoucher find(Long id, String enterpriseId) { return delegate.find(id, enterpriseId); }
    @Override @Transactional(readOnly = true) public PageResult<PaymentVoucher> search(VoucherFilter filter) { return delegate.search(filter); }
}

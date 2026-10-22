package co.unicauca.edu.co.contables.accounting.treasury.application.input;

import co.unicauca.edu.co.contables.accounting.treasury.domain.model.PaymentVoucher;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.command.TreasuryCommands.AccountingResult;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.command.TreasuryCommands.Voucher;

public interface IPaymentVoucherCommandUseCase {
    PaymentVoucher create(Voucher command);
    PaymentVoucher update(Long id, Voucher command);
    void delete(Long id, String enterpriseId);
    PaymentVoucher post(Long id, String enterpriseId, String idempotencyKey);
    PaymentVoucher voidVoucher(Long id, String enterpriseId, String reason);
    void applyAccountingResult(AccountingResult result);
}

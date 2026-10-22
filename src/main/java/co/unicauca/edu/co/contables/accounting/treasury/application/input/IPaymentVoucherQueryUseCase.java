package co.unicauca.edu.co.contables.accounting.treasury.application.input;

import co.unicauca.edu.co.contables.accounting.treasury.domain.model.PaymentVoucher;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.command.TreasuryCommands.PageResult;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.command.TreasuryCommands.VoucherFilter;

public interface IPaymentVoucherQueryUseCase {
    PaymentVoucher find(Long id, String enterpriseId);
    PageResult<PaymentVoucher> search(VoucherFilter filter);
}

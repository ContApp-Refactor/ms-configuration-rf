package co.unicauca.edu.co.contables.accounting.treasury.application.output;

import co.unicauca.edu.co.contables.accounting.treasury.domain.model.PaymentVoucher;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.command.TreasuryCommands.PageResult;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.command.TreasuryCommands.VoucherFilter;
import java.util.Optional;

public interface IPaymentVoucherQueryPersistencePort {
    Optional<PaymentVoucher> find(Long id, String enterpriseId);
    Optional<PaymentVoucher> findById(Long id);
    Optional<PaymentVoucher> findByIdempotencyKey(String key, String enterpriseId);
    PageResult<PaymentVoucher> search(VoucherFilter filter);
}

package co.unicauca.edu.co.contables.accounting.treasury.application.output;

import co.unicauca.edu.co.contables.accounting.treasury.domain.model.PaymentVoucher;

public interface IPaymentVoucherCommandPersistencePort {
    PaymentVoucher save(PaymentVoucher voucher);
    void delete(PaymentVoucher voucher);
}

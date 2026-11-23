package co.unicauca.edu.co.contables.accounting.treasury.application.output;

import co.unicauca.edu.co.contables.accounting.treasury.domain.model.PaymentMethodData;
import java.util.Optional;

public interface IPaymentMethodProviderPort {
    Optional<PaymentMethodData> findActive(Long paymentMethodId, String enterpriseId);
    boolean isActiveBankAccount(Long bankAccountId, String enterpriseId);
    void validateForPayment(Long paymentMethodId, Long bankAccountId, String enterpriseId);
}

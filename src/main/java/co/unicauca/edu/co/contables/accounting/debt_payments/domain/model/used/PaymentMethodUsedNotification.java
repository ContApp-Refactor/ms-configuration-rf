package co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.used;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports.ResourceUsageNotification;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * @brief Notification class indicating that a payment method is being used.
 * This class implements the ResourceUsageNotification interface and contains the ID of the payment method and the associated enterprise ID.
 */
@RequiredArgsConstructor
@Getter
@Setter
public class PaymentMethodUsedNotification implements ResourceUsageNotification{
    private final Long paymentMethodId;
    private final String enterpriseId;
}

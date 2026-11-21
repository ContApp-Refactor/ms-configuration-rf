package co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.messageBroker;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.services.IPaymentMethodUsage;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.used.PaymentMethodUsedEventDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "false", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class SpringEventsPaymentMethodUsageListener {

    private final IPaymentMethodUsage paymentMethodUsagePort;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onPaymentMethodUsed(PaymentMethodUsedEventDto event) {
        try {
            if (!isValidEvent(event)) {
                log.warn("Invalid payment method usage event received");
                return;
            }

            log.info("Registrando uso de método de pago ID: {}, empresa: {}, cantidad: {}",
                    event.getPaymentMethodId(), event.getEnterpriseId(), event.getQuantityUsed());
            paymentMethodUsagePort.incrementUsageCount(event.getPaymentMethodId(), event.getEnterpriseId());
            log.info("Uso registrado correctamente para método de pago ID: {}", event.getPaymentMethodId());
        } catch (Exception e) {
            log.error("Error procesando evento de método de pago: {}", e.getMessage());
        }
    }

    private boolean isValidEvent(PaymentMethodUsedEventDto data) {
        if (data == null) {
            log.warn("Evento es null");
            return false;
        }

        if (data.getPaymentMethodId() == null) {
            log.warn("PaymentMethodId es null - campo obligatorio");
            return false;
        }

        if (data.getEnterpriseId() == null || data.getEnterpriseId().trim().isEmpty()) {
            log.warn("EnterpriseId es null o vacío - campo obligatorio");
            return false;
        }

        if (data.getQuantityUsed() == null || data.getQuantityUsed() <= 0) {
            log.warn("QuantityUsed es null o inválido - campo obligatorio");
            return false;
        }

        return true;
    }
}

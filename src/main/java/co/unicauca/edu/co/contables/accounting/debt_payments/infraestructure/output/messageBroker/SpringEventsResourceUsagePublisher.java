package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.accounting.debt_payments.application.output.IResourceUsageNotifierPort;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.used.CostCenterUsedNotification;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.used.PaymentMethodUsedNotification;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.used.ThirdPartyUsedNotification;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports.ResourceUsageNotification;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.used.CostCenterUsedDto;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.used.PaymentMethodUsedEventDto;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.used.ThirdUsedEvenDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "false", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class SpringEventsResourceUsagePublisher implements IResourceUsageNotifierPort {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void notifyAll(List<ResourceUsageNotification> notifications) {
        notifications.forEach(this::dispatch);
    }

    private void dispatch(ResourceUsageNotification notification) {
        if (notification instanceof ThirdPartyUsedNotification) {
            handle((ThirdPartyUsedNotification) notification);
        } else if (notification instanceof CostCenterUsedNotification) {
            handle((CostCenterUsedNotification) notification);
        } else if (notification instanceof PaymentMethodUsedNotification) {
            handle((PaymentMethodUsedNotification) notification);
        } else {
            log.warn("No handler found for notification type: {}", notification.getClass().getSimpleName());
        }
    }

    private void handle(ThirdPartyUsedNotification notification) {
        ThirdUsedEvenDto dto = new ThirdUsedEvenDto(notification.getThirdId(), notification.getEnterpriseId(), 1);
        log.info("Publishing third used event: {}", notification.getThirdId());

        eventPublisher.publishEvent(dto);
    }

    private void handle(CostCenterUsedNotification notification) {
        CostCenterUsedDto dto = new CostCenterUsedDto(notification.getCostCenterId(), notification.getEnterpriseId(), 1);
        log.info("Publishing cost center used event: {}", notification.getCostCenterId());

        eventPublisher.publishEvent(dto);
    }

    private void handle(PaymentMethodUsedNotification notification) {
        PaymentMethodUsedEventDto dto = new PaymentMethodUsedEventDto(
                notification.getPaymentMethodId(), notification.getEnterpriseId(), 1);
        log.info("Publishing payment method used event: {}", notification.getPaymentMethodId());

        eventPublisher.publishEvent(dto);
    }
}

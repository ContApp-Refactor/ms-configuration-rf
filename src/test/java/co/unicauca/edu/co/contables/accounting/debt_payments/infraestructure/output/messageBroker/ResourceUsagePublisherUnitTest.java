package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.used.CostCenterUsedNotification;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.used.PaymentMethodUsedNotification;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.used.ThirdPartyUsedNotification;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.config.used.RabbitPaymentMethodConfig;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.ResourceUsagePublisher;
import co.unicauca.edu.co.contables.commons.security.IJwtUtils;
import co.unicauca.edu.co.contables.configuration.costCenters.domain.messageBroker.config.RabbitCostCenterUsedConfig;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.config.rabbitConfig.RabbitThirdUsedConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit tests for ResourceUsagePublisher")
public class ResourceUsagePublisherUnitTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private IJwtUtils jwtUtils;

    @InjectMocks
    private ResourceUsagePublisher publisher;

    @Test
    @DisplayName("notifyAll should dispatch ThirdPartyUsedNotification")
    void shouldDispatchThirdPartyUsedNotification() {
        // Arrange
        ThirdPartyUsedNotification notification = new ThirdPartyUsedNotification(100L, "ENT-1");
        lenient().when(jwtUtils.getToken()).thenReturn("test-token");

        // Act
        publisher.notifyAll(List.of(notification));

        // Assert
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitThirdUsedConfig.THIRD_USED_EXCHANGE),
                eq(""),
                isA(Object.class),
                isA(org.springframework.amqp.core.MessagePostProcessor.class)
        );
    }

    @Test
    @DisplayName("notifyAll should dispatch CostCenterUsedNotification")
    void shouldDispatchCostCenterUsedNotification() {
        // Arrange
        CostCenterUsedNotification notification = new CostCenterUsedNotification(200L, "ENT-2");
        lenient().when(jwtUtils.getToken()).thenReturn("test-token");

        // Act
        publisher.notifyAll(List.of(notification));

        // Assert
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitCostCenterUsedConfig.COST_CENTER_USED_EXCHANGE),
                eq(""),
                isA(Object.class),
                isA(org.springframework.amqp.core.MessagePostProcessor.class)
        );
    }

    @Test
    @DisplayName("notifyAll should dispatch PaymentMethodUsedNotification")
    void shouldDispatchPaymentMethodUsedNotification() {
        // Arrange
        PaymentMethodUsedNotification notification = new PaymentMethodUsedNotification(300L, "ENT-3");
        lenient().when(jwtUtils.getToken()).thenReturn("test-token");

        // Act
        publisher.notifyAll(List.of(notification));

        // Assert
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitPaymentMethodConfig.PAYMENTMETHOD_USED_EXCHANGE),
                eq(""),
                isA(Object.class),
                isA(org.springframework.amqp.core.MessagePostProcessor.class)
        );
    }

    @Test
    @DisplayName("notifyAll should handle multiple mixed notifications")
    void shouldHandleMultipleMixedNotifications() {
        // Arrange
        ThirdPartyUsedNotification third = new ThirdPartyUsedNotification(10L, "ENT-X");
        CostCenterUsedNotification costCenter = new CostCenterUsedNotification(20L, "ENT-X");
        PaymentMethodUsedNotification payment = new PaymentMethodUsedNotification(30L, "ENT-X");
        lenient().when(jwtUtils.getToken()).thenReturn("test-token");

        // Act
        publisher.notifyAll(List.of(third, costCenter, payment));

        // Assert
        verify(rabbitTemplate, times(3)).convertAndSend(
                anyString(), 
                eq(""), 
                isA(Object.class), 
                isA(org.springframework.amqp.core.MessagePostProcessor.class)
        );
    }

    @Test
    @DisplayName("notifyAll should handle empty list")
    void shouldHandleEmptyList() {
        // Act
        publisher.notifyAll(List.of());

        // Assert
        verifyNoInteractions(rabbitTemplate);
    }
}

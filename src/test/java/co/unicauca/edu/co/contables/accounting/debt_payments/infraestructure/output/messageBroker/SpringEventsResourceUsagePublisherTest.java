package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.used.CostCenterUsedNotification;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.used.PaymentMethodUsedNotification;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.used.ThirdPartyUsedNotification;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.used.CostCenterUsedDto;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.used.PaymentMethodUsedEventDto;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.used.ThirdUsedEvenDto;

@ExtendWith(MockitoExtension.class)
class SpringEventsResourceUsagePublisherTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private SpringEventsResourceUsagePublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new SpringEventsResourceUsagePublisher(eventPublisher);
    }

    @Test
    @DisplayName("thirdParty - publica el DTO del productor en el bus en proceso")
    void thirdParty_publicaEvento() {
        publisher.notifyAll(List.of(new ThirdPartyUsedNotification(100L, "ENT-1")));

        ArgumentCaptor<ThirdUsedEvenDto> captor = ArgumentCaptor.forClass(ThirdUsedEvenDto.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertEquals(100L, captor.getValue().getThirdId());
        assertEquals("ENT-1", captor.getValue().getEnterpriseId());
        assertEquals(1, captor.getValue().getQuantityUsed());
    }

    @Test
    @DisplayName("costCenter - publica el DTO del productor en el bus en proceso")
    void costCenter_publicaEvento() {
        publisher.notifyAll(List.of(new CostCenterUsedNotification(200L, "ENT-2")));

        ArgumentCaptor<CostCenterUsedDto> captor = ArgumentCaptor.forClass(CostCenterUsedDto.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertEquals(200L, captor.getValue().getCostCenterId());
        assertEquals("ENT-2", captor.getValue().getEnterpriseId());
        assertEquals(1, captor.getValue().getQuantityUsed());
    }

    @Test
    @DisplayName("paymentMethod - publica el DTO del productor en el bus en proceso")
    void paymentMethod_publicaEvento() {
        publisher.notifyAll(List.of(new PaymentMethodUsedNotification(300L, "ENT-3")));

        ArgumentCaptor<PaymentMethodUsedEventDto> captor = ArgumentCaptor.forClass(PaymentMethodUsedEventDto.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertEquals(300L, captor.getValue().getPaymentMethodId());
        assertEquals("ENT-3", captor.getValue().getEnterpriseId());
        assertEquals(1, captor.getValue().getQuantityUsed());
    }

    @Test
    @DisplayName("lista mixta - publica un evento por cada notificación")
    void listaMixta_publicaTresEventos() {
        publisher.notifyAll(List.of(
                new ThirdPartyUsedNotification(10L, "ENT-X"),
                new CostCenterUsedNotification(20L, "ENT-X"),
                new PaymentMethodUsedNotification(30L, "ENT-X")));

        verify(eventPublisher, times(3)).publishEvent(org.mockito.ArgumentMatchers.any(Object.class));
    }

    @Test
    @DisplayName("lista vacía - no publica nada")
    void listaVacia_noPublicaNada() {
        publisher.notifyAll(List.of());

        verifyNoInteractions(eventPublisher);
    }
}

package co.unicauca.edu.co.contables.notifications;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import co.unicauca.edu.co.contables.accounting.debt_payments.application.output.IInvoiceNotificationEventPublisher;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports.IMessageErrorHandlingPort;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.adapter.InvoiceNotificationEventPublisherAdapter;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.adapter.SpringEventsInvoiceNotificationEventPublisher;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.InvoiceDetailEventDto;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.InvoiceDueReminderEvent;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.InvoiceDueReminderEventDto;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.config.GeographyDataInitializer;
import co.unicauca.edu.co.contables.notifications.application.input.IProcessNotificationUseCase;
import co.unicauca.edu.co.contables.notifications.infrastructure.output.messageBroker.listener.NotificationEventListener;
import co.unicauca.edu.co.contables.notifications.infrastructure.output.messageBroker.listener.SpringEventsNotificationEventListener;

@SpringBootTest(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.defer-datasource-initialization=false"
})
@ActiveProfiles("test")
@DisplayName("debt_payments → notifications — flujo en proceso (reemplazo de notifications.exchange)")
class InvoiceNotificationFlowTest {

    private static final long THIRD_PARTY_ID = 77L;

    @MockitoBean
    private GeographyDataInitializer geographyDataInitializer;

    @MockitoBean
    private IProcessNotificationUseCase processNotificationUseCase;

    @MockitoBean
    private IMessageErrorHandlingPort messageErrorHandlingPort;

    @Autowired
    private ApplicationEventPublisher events;

    @Autowired
    private IInvoiceNotificationEventPublisher notificationPublisher;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("recordatorio válido - el publicador y el listener lo procesan en el mismo hilo")
    void recordatorioValido_loProcesaElListenerEnProceso() {
        notificationPublisher.publishInvoiceDueReminder(reminder(THIRD_PARTY_ID));

        ArgumentCaptor<co.unicauca.edu.co.contables.notifications.infrastructure.output.messageBroker.dto.InvoiceDueReminderEventDto> captor =
                ArgumentCaptor.forClass(
                        co.unicauca.edu.co.contables.notifications.infrastructure.output.messageBroker.dto.InvoiceDueReminderEventDto.class);
        verify(processNotificationUseCase).processInvoiceDueReminder(captor.capture());

        co.unicauca.edu.co.contables.notifications.infrastructure.output.messageBroker.dto.InvoiceDueReminderEventDto processed =
                captor.getValue();
        assertEquals(THIRD_PARTY_ID, processed.getThirdPartyId());
        assertEquals(1, processed.getInvoiceDetails().size());

        co.unicauca.edu.co.contables.notifications.infrastructure.output.messageBroker.dto.InvoiceDetailEventDto detail =
                processed.getInvoiceDetails().get(0);
        assertEquals(1L, detail.getInvoiceId());
        assertEquals(1000L, detail.getInvoiceCode());
        assertEquals(LocalDate.of(2026, 1, 20), detail.getExpirationDate());
        assertEquals(50000L, detail.getTotalAmount());
        assertEquals(50000L, detail.getPendingValue());

        verifyNoInteractions(messageErrorHandlingPort);
    }

    @Test
    @DisplayName("tipo desconocido - se descarta sin procesar ni persistir")
    void tipoDesconocido_seDescarta() {
        InvoiceDueReminderEvent event = new InvoiceDueReminderEvent(reminder(THIRD_PARTY_ID));
        event.setType("SOMETHING_ELSE");

        events.publishEvent(event);

        verifyNoInteractions(processNotificationUseCase);
        verifyNoInteractions(messageErrorHandlingPort);
    }

    @Test
    @DisplayName("Perfil por defecto - sólo el nuevo publicador y el nuevo listener están activos")
    void perfilPorDefecto_soloSpringEvents() {
        assertNotNull(applicationContext.getBean("invoiceNotificationEventPublisherAdapter",
                SpringEventsInvoiceNotificationEventPublisher.class));
        assertNotNull(applicationContext.getBean("notificationsSpringEventsNotificationListener",
                SpringEventsNotificationEventListener.class));

        assertThrows(NoSuchBeanDefinitionException.class,
                () -> applicationContext.getBean(InvoiceNotificationEventPublisherAdapter.class));
        assertThrows(NoSuchBeanDefinitionException.class,
                () -> applicationContext.getBean(NotificationEventListener.class));
        assertThrows(NoSuchBeanDefinitionException.class,
                () -> applicationContext.getBean(
                        co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.config.RabbitNotificationsConfig.class));
        assertThrows(NoSuchBeanDefinitionException.class,
                () -> applicationContext.getBean(
                        co.unicauca.edu.co.contables.notifications.infrastructure.config.RabbitNotificationsConfig.class));
    }

    private InvoiceDueReminderEventDto reminder(Long thirdPartyId) {
        InvoiceDetailEventDto detail = new InvoiceDetailEventDto();
        detail.setInvoiceId(1L);
        detail.setInvoiceCode(1000L);
        detail.setExpirationDate(LocalDate.of(2026, 1, 20));
        detail.setTotalAmount(50000L);
        detail.setPendingValue(50000L);

        InvoiceDueReminderEventDto dto = new InvoiceDueReminderEventDto();
        dto.setThirdPartyId(thirdPartyId);
        dto.setInvoiceDetails(List.of(detail));
        return dto;
    }
}

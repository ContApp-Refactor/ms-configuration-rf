package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.InvoiceDetailEventDto;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.InvoiceDueReminderEvent;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.InvoiceDueReminderEventDto;

@ExtendWith(MockitoExtension.class)
@DisplayName("debt_payments SpringEventsInvoiceNotificationEventPublisher Unit Tests")
class SpringEventsInvoiceNotificationEventPublisherTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private SpringEventsInvoiceNotificationEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new SpringEventsInvoiceNotificationEventPublisher(eventPublisher);
    }

    @Test
    @DisplayName("Recordatorio válido - publica el evento concreto con tipo y datos")
    void recordatorioValido_publicaEventoConcreto() {
        InvoiceDueReminderEventDto dto = reminder(77L);

        publisher.publishInvoiceDueReminder(dto);

        InvoiceDueReminderEvent event = capturePublishedEvent();
        assertEquals(InvoiceDueReminderEvent.TYPE, event.getType());
        assertEquals("INVOICE_DUE_REMINDER", event.getType());
        assertSame(dto, event.getData());
    }

    @Test
    @DisplayName("Recordatorio sin tercero - publica sin validar (la validación es del consumidor)")
    void recordatorioSinTercero_publicaSinValidar() {
        InvoiceDueReminderEventDto dto = reminder(null);

        publisher.publishInvoiceDueReminder(dto);

        InvoiceDueReminderEvent event = capturePublishedEvent();
        assertEquals(InvoiceDueReminderEvent.TYPE, event.getType());
        assertSame(dto, event.getData());

        verify(eventPublisher, times(1)).publishEvent(event);
    }

    private InvoiceDueReminderEvent capturePublishedEvent() {
        ArgumentCaptor<InvoiceDueReminderEvent> captor = ArgumentCaptor.forClass(InvoiceDueReminderEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        return captor.getValue();
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

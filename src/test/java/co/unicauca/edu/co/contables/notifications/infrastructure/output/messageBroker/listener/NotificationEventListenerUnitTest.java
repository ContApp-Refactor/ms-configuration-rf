package co.unicauca.edu.co.contables.notifications.infrastructure.output.messageBroker.listener;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.test.util.ReflectionTestUtils;

import com.rabbitmq.client.Channel;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports.IMessageErrorHandlingPort;
import co.unicauca.edu.co.contables.notifications.application.input.IProcessNotificationUseCase;
import co.unicauca.edu.co.contables.notifications.infrastructure.output.messageBroker.dto.EventDto;
import co.unicauca.edu.co.contables.notifications.infrastructure.output.messageBroker.dto.InvoiceDetailEventDto;
import co.unicauca.edu.co.contables.notifications.infrastructure.output.messageBroker.dto.InvoiceDueReminderEventDto;

@ExtendWith(MockitoExtension.class)
@DisplayName("notifications NotificationEventListener Unit Tests")
class NotificationEventListenerUnitTest {

    @Mock
    private IProcessNotificationUseCase processNotificationUseCase;

    @Mock
    private IMessageErrorHandlingPort messageErrorHandlingPort;

    @Mock
    private Channel channel;

    private NotificationEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new NotificationEventListener(processNotificationUseCase, messageErrorHandlingPort);
        ReflectionTestUtils.setField(listener, "messageErrorHandlingPort", messageErrorHandlingPort);
    }

    @Test
    @DisplayName("Recordatorio válido - procesa y hace ACK")
    void recordatorioValido_procesaYAck() throws IOException {
        listener.listenToNotificationQueue(message(), validEvent(), channel, 1L);

        verify(processNotificationUseCase).processInvoiceDueReminder(any(InvoiceDueReminderEventDto.class));
        verify(channel).basicAck(1L, false);
        verifyNoInteractions(messageErrorHandlingPort);
    }

    @Test
    @DisplayName("Tipo desconocido - descarta y hace ACK")
    void tipoDesconocido_descartaYAck() throws IOException {
        EventDto<InvoiceDueReminderEventDto> event = validEvent();
        event.setType("SOMETHING_ELSE");

        listener.listenToNotificationQueue(message(), event, channel, 2L);

        verify(processNotificationUseCase, never()).processInvoiceDueReminder(any());
        verify(channel).basicAck(2L, false);
        verifyNoInteractions(messageErrorHandlingPort);
    }

    @Test
    @DisplayName("thirdPartyId nulo - persiste validación y hace ACK")
    void thirdPartyIdNulo_persisteValidacionYAck() throws IOException {
        EventDto<InvoiceDueReminderEventDto> event = validEvent();
        event.getData().setThirdPartyId(null);

        listener.listenToNotificationQueue(message(), event, channel, 3L);

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("INVOICE_DUE_REMINDER"),
                eq("Validation failed: thirdPartyId is null"),
                contains("\"thirdPartyId\":null"),
                eq("Notification.InvoiceDueReminder"));
        verify(channel).basicAck(3L, false);
        verify(processNotificationUseCase, never()).processInvoiceDueReminder(any());
    }

    @Test
    @DisplayName("invoiceDetails nulo - persiste validación y hace ACK")
    void invoiceDetailsNulo_persisteValidacionYAck() throws IOException {
        EventDto<InvoiceDueReminderEventDto> event = validEvent();
        event.getData().setInvoiceDetails(null);

        listener.listenToNotificationQueue(message(), event, channel, 4L);

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("INVOICE_DUE_REMINDER"),
                eq("Validation failed: invoiceDetails is null or empty"),
                contains("\"invoiceDetails\":null"),
                eq("Notification.InvoiceDueReminder"));
        verify(channel).basicAck(4L, false);
    }

    @Test
    @DisplayName("invoiceDetails vacío - persiste validación y hace ACK")
    void invoiceDetailsVacio_persisteValidacionYAck() throws IOException {
        EventDto<InvoiceDueReminderEventDto> event = validEvent();
        event.getData().setInvoiceDetails(List.of());

        listener.listenToNotificationQueue(message(), event, channel, 5L);

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("INVOICE_DUE_REMINDER"),
                eq("Validation failed: invoiceDetails is null or empty"),
                contains("\"invoiceDetails\":[]"),
                eq("Notification.InvoiceDueReminder"));
        verify(channel).basicAck(5L, false);
    }

    @Test
    @DisplayName("invoiceDetails con elementos nulos - persiste validación y hace ACK")
    void invoiceDetailsConNulos_persisteValidacionYAck() throws IOException {
        EventDto<InvoiceDueReminderEventDto> event = validEvent();
        event.getData().setInvoiceDetails(Arrays.asList(detail(), null));

        listener.listenToNotificationQueue(message(), event, channel, 6L);

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("INVOICE_DUE_REMINDER"),
                eq("Validation failed: invoiceDetails list contains null items"),
                anyString(),
                eq("Notification.InvoiceDueReminder"));
        verify(channel).basicAck(6L, false);
    }

    @Test
    @DisplayName("expirationDate nula - persiste validación y hace ACK")
    void expirationDateNula_persisteValidacionYAck() throws IOException {
        EventDto<InvoiceDueReminderEventDto> event = validEvent();
        event.getData().getInvoiceDetails().get(0).setExpirationDate(null);

        listener.listenToNotificationQueue(message(), event, channel, 7L);

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("INVOICE_DUE_REMINDER"),
                eq("Validation failed: expirationDate is null in invoiceDetails"),
                contains("\"expirationDate\":null"),
                eq("Notification.InvoiceDueReminder"));
        verify(channel).basicAck(7L, false);
    }

    @Test
    @DisplayName("Error del caso de uso - persiste procesamiento, hace ACK y no propaga")
    void errorDelCasoDeUso_persisteProcesamientoYAck() throws IOException {
        doThrow(new RuntimeException("SMTP down"))
                .when(processNotificationUseCase).processInvoiceDueReminder(any());

        assertDoesNotThrow(() -> listener.listenToNotificationQueue(message(), validEvent(), channel, 8L));

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("INVOICE_DUE_REMINDER"),
                eq("Processing error: SMTP down"),
                contains("\"thirdPartyId\":77"),
                eq("Notification.InvoiceDueReminder"));
        verify(channel).basicAck(8L, false);
    }

    @Test
    @DisplayName("Fallo al persistir la validación - hace ACK y no propaga")
    void falloAlPersistirValidacion_haceAckYNoPropaga() throws IOException {
        doThrow(new RuntimeException("db down")).when(messageErrorHandlingPort)
                .saveProcessingError(any(), anyString(), anyString(), anyString());
        EventDto<InvoiceDueReminderEventDto> event = validEvent();
        event.getData().setThirdPartyId(null);

        assertDoesNotThrow(() -> listener.listenToNotificationQueue(message(), event, channel, 9L));

        verify(channel).basicAck(9L, false);
    }

    @Test
    @DisplayName("Fallo del ACK - no propaga")
    void falloAck_noPropaga() throws IOException {
        doThrow(new IOException("channel closed")).when(channel).basicAck(anyLong(), eq(false));

        assertDoesNotThrow(() -> listener.listenToNotificationQueue(message(), validEvent(), channel, 10L));
    }

    private Message message() {
        return new Message(new byte[0]);
    }

    private EventDto<InvoiceDueReminderEventDto> validEvent() {
        InvoiceDueReminderEventDto data = new InvoiceDueReminderEventDto();
        data.setThirdPartyId(77L);
        data.setInvoiceDetails(List.of(detail()));
        return new EventDto<>("INVOICE_DUE_REMINDER", data);
    }

    private InvoiceDetailEventDto detail() {
        InvoiceDetailEventDto detail = new InvoiceDetailEventDto();
        detail.setInvoiceId(1L);
        detail.setInvoiceCode(1000L);
        detail.setExpirationDate(LocalDate.of(2026, 1, 20));
        detail.setTotalAmount(50000L);
        detail.setPendingValue(50000L);
        return detail;
    }
}

package co.unicauca.edu.co.contables.notifications.infrastructure.output.messageBroker.listener;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports.IMessageErrorHandlingPort;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.InvoiceDueReminderEvent;
import co.unicauca.edu.co.contables.notifications.application.input.IProcessNotificationUseCase;
import co.unicauca.edu.co.contables.notifications.infrastructure.output.messageBroker.dto.InvoiceDetailEventDto;
import co.unicauca.edu.co.contables.notifications.infrastructure.output.messageBroker.dto.InvoiceDueReminderEventDto;

@ExtendWith(MockitoExtension.class)
@DisplayName("notifications SpringEventsNotificationEventListener Unit Tests")
class SpringEventsNotificationEventListenerTest {

    @Mock
    private IProcessNotificationUseCase processNotificationUseCase;

    @Mock
    private IMessageErrorHandlingPort messageErrorHandlingPort;

    private SpringEventsNotificationEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new SpringEventsNotificationEventListener(processNotificationUseCase, messageErrorHandlingPort);
    }

    @Test
    @DisplayName("Recordatorio válido - traduce todos los campos y procesa")
    void recordatorioValido_traduceTodosLosCamposYProcesa() {
        co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.InvoiceDueReminderEventDto dto =
                reminder(77L);
        co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.InvoiceDetailEventDto second =
                detail();
        second.setInvoiceId(2L);
        second.setInvoiceCode(2000L);
        second.setExpirationDate(LocalDate.of(2026, 2, 20));
        second.setTotalAmount(75000L);
        second.setPendingValue(0L);
        dto.setInvoiceDetails(List.of(dto.getInvoiceDetails().get(0), second));

        listener.onInvoiceDueReminder(new InvoiceDueReminderEvent(dto));

        InvoiceDueReminderEventDto captured = captureProcessed();
        assertEquals(77L, captured.getThirdPartyId());
        assertEquals(2, captured.getInvoiceDetails().size());

        InvoiceDetailEventDto first = captured.getInvoiceDetails().get(0);
        assertEquals(1L, first.getInvoiceId());
        assertEquals(1000L, first.getInvoiceCode());
        assertEquals(LocalDate.of(2026, 1, 20), first.getExpirationDate());
        assertEquals(50000L, first.getTotalAmount());
        assertEquals(50000L, first.getPendingValue());

        InvoiceDetailEventDto nonZeroPending = captured.getInvoiceDetails().get(1);
        assertEquals(2L, nonZeroPending.getInvoiceId());
        assertEquals(2000L, nonZeroPending.getInvoiceCode());
        assertEquals(LocalDate.of(2026, 2, 20), nonZeroPending.getExpirationDate());
        assertEquals(75000L, nonZeroPending.getTotalAmount());
        assertEquals(0L, nonZeroPending.getPendingValue());

        verifyNoInteractions(messageErrorHandlingPort);
    }

    @Test
    @DisplayName("Evento nulo - persiste error de validación y no procesa")
    void eventoNulo_persisteValidacion() {
        listener.onInvoiceDueReminder(null);

        verifyValidationError("Event is null", "unknown",
                "{\"error\": \"Event or event data is null\"}");
    }

    @Test
    @DisplayName("Datos nulos - persiste error de validación con el tipo de evento")
    void datosNulos_persisteValidacion() {
        listener.onInvoiceDueReminder(new InvoiceDueReminderEvent(null));

        verifyValidationError("Event data is null", "INVOICE_DUE_REMINDER",
                "{\"error\": \"Event or event data is null\"}");
    }

    @Test
    @DisplayName("Tipo nulo - persiste error de validación")
    void tipoNulo_persisteValidacion() {
        InvoiceDueReminderEvent event = new InvoiceDueReminderEvent(reminder(77L));
        event.setType(null);

        listener.onInvoiceDueReminder(event);

        verifyValidationError("Event type is null", null, "\"thirdPartyId\":77");
    }

    @Test
    @DisplayName("thirdPartyId nulo - persiste error de validación")
    void thirdPartyIdNulo_persisteValidacion() {
        listener.onInvoiceDueReminder(new InvoiceDueReminderEvent(reminder(null)));

        verifyValidationError("thirdPartyId is null", "INVOICE_DUE_REMINDER",
                "\"thirdPartyId\":null");
    }

    @Test
    @DisplayName("invoiceDetails nulo - persiste error de validación")
    void invoiceDetailsNulo_persisteValidacion() {
        co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.InvoiceDueReminderEventDto dto =
                reminder(77L);
        dto.setInvoiceDetails(null);

        listener.onInvoiceDueReminder(new InvoiceDueReminderEvent(dto));

        verifyValidationError("invoiceDetails is null or empty", "INVOICE_DUE_REMINDER",
                "\"invoiceDetails\":null");
    }

    @Test
    @DisplayName("invoiceDetails vacío - persiste error de validación")
    void invoiceDetailsVacio_persisteValidacion() {
        co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.InvoiceDueReminderEventDto dto =
                reminder(77L);
        dto.setInvoiceDetails(List.of());

        listener.onInvoiceDueReminder(new InvoiceDueReminderEvent(dto));

        verifyValidationError("invoiceDetails is null or empty", "INVOICE_DUE_REMINDER",
                "\"invoiceDetails\":[]");
    }

    @Test
    @DisplayName("invoiceDetails con elementos nulos - persiste error de validación")
    void invoiceDetailsConNulos_persisteValidacion() {
        co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.InvoiceDueReminderEventDto dto =
                reminder(77L);
        dto.setInvoiceDetails(Arrays.asList(detail(), null));

        listener.onInvoiceDueReminder(new InvoiceDueReminderEvent(dto));

        verifyValidationError("invoiceDetails list contains null items", "INVOICE_DUE_REMINDER",
                "\"invoiceDetails\":[{");
    }

    @Test
    @DisplayName("invoiceId nulo - persiste error de validación")
    void invoiceIdNulo_persisteValidacion() {
        listener.onInvoiceDueReminder(eventWithDetail(detail -> detail.setInvoiceId(null)));

        verifyValidationError("invoiceId is null in invoiceDetails", "INVOICE_DUE_REMINDER",
                "\"invoiceId\":null");
    }

    @Test
    @DisplayName("invoiceCode nulo - persiste error de validación")
    void invoiceCodeNulo_persisteValidacion() {
        listener.onInvoiceDueReminder(eventWithDetail(detail -> detail.setInvoiceCode(null)));

        verifyValidationError("invoiceCode is null in invoiceDetails", "INVOICE_DUE_REMINDER",
                "\"invoiceCode\":null");
    }

    @Test
    @DisplayName("expirationDate nula - persiste error de validación")
    void expirationDateNula_persisteValidacion() {
        listener.onInvoiceDueReminder(eventWithDetail(detail -> detail.setExpirationDate(null)));

        verifyValidationError("expirationDate is null in invoiceDetails", "INVOICE_DUE_REMINDER",
                "\"expirationDate\":null");
    }

    @Test
    @DisplayName("totalAmount nulo - persiste error de validación")
    void totalAmountNulo_persisteValidacion() {
        listener.onInvoiceDueReminder(eventWithDetail(detail -> detail.setTotalAmount(null)));

        verifyValidationError("totalAmount is null in invoiceDetails", "INVOICE_DUE_REMINDER",
                "\"totalAmount\":null");
    }

    @Test
    @DisplayName("pendingValue nulo - persiste error de validación")
    void pendingValueNulo_persisteValidacion() {
        listener.onInvoiceDueReminder(eventWithDetail(detail -> detail.setPendingValue(null)));

        verifyValidationError("pendingValue is null in invoiceDetails", "INVOICE_DUE_REMINDER",
                "\"pendingValue\":null");
    }

    @Test
    @DisplayName("Tipo desconocido - descarta sin procesar ni persistir")
    void tipoDesconocido_descarta() {
        InvoiceDueReminderEvent event = new InvoiceDueReminderEvent(reminder(77L));
        event.setType("SOMETHING_ELSE");

        listener.onInvoiceDueReminder(event);

        verifyNoInteractions(processNotificationUseCase);
        verifyNoInteractions(messageErrorHandlingPort);
    }

    @Test
    @DisplayName("Error del caso de uso - persiste error de procesamiento y no propaga")
    void errorDelCasoDeUso_persisteProcesamiento() {
        doThrow(new RuntimeException("SMTP down"))
                .when(processNotificationUseCase).processInvoiceDueReminder(any());

        assertDoesNotThrow(() -> listener.onInvoiceDueReminder(
                new InvoiceDueReminderEvent(reminder(77L))));

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("INVOICE_DUE_REMINDER"),
                eq("Processing error: SMTP down"),
                contains("\"thirdPartyId\":77"),
                eq("Notification.InvoiceDueReminder"));
    }

    @Test
    @DisplayName("Fallo al persistir el error de validación - no propaga")
    void falloAlPersistirValidacion_noPropaga() {
        doThrow(new RuntimeException("db down")).when(messageErrorHandlingPort)
                .saveProcessingError(any(), anyString(), anyString(), anyString());

        assertDoesNotThrow(() -> listener.onInvoiceDueReminder(
                new InvoiceDueReminderEvent(reminder(null))));

        verify(processNotificationUseCase, never()).processInvoiceDueReminder(any());
    }

    @Test
    @DisplayName("Fallo al persistir el error de procesamiento - no propaga")
    void falloAlPersistirProcesamiento_noPropaga() {
        doThrow(new RuntimeException("SMTP down"))
                .when(processNotificationUseCase).processInvoiceDueReminder(any());
        doThrow(new RuntimeException("db down")).when(messageErrorHandlingPort)
                .saveProcessingError(any(), anyString(), anyString(), anyString());

        assertDoesNotThrow(() -> listener.onInvoiceDueReminder(
                new InvoiceDueReminderEvent(reminder(77L))));

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("INVOICE_DUE_REMINDER"),
                eq("Processing error: SMTP down"),
                anyString(),
                eq("Notification.InvoiceDueReminder"));
    }

    private void verifyValidationError(String expectedMessage, String expectedEventType, String jsonFragment) {
        verify(messageErrorHandlingPort).saveProcessingError(
                expectedEventType == null ? isNull() : eq(expectedEventType),
                eq("Validation failed: " + expectedMessage),
                contains(jsonFragment),
                eq("Notification.InvoiceDueReminder"));
        verify(processNotificationUseCase, never()).processInvoiceDueReminder(any());
    }

    private InvoiceDueReminderEventDto captureProcessed() {
        ArgumentCaptor<InvoiceDueReminderEventDto> captor = ArgumentCaptor.forClass(InvoiceDueReminderEventDto.class);
        verify(processNotificationUseCase).processInvoiceDueReminder(captor.capture());
        return captor.getValue();
    }

    private InvoiceDueReminderEvent eventWithDetail(
            java.util.function.Consumer<co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.InvoiceDetailEventDto> customizer) {
        co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.InvoiceDueReminderEventDto dto =
                reminder(77L);
        customizer.accept(dto.getInvoiceDetails().get(0));
        return new InvoiceDueReminderEvent(dto);
    }

    private co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.InvoiceDueReminderEventDto reminder(
            Long thirdPartyId) {
        co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.InvoiceDueReminderEventDto dto =
                new co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.InvoiceDueReminderEventDto();
        dto.setThirdPartyId(thirdPartyId);
        dto.setInvoiceDetails(List.of(detail()));
        return dto;
    }

    private co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.InvoiceDetailEventDto detail() {
        co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.InvoiceDetailEventDto detail =
                new co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.InvoiceDetailEventDto();
        detail.setInvoiceId(1L);
        detail.setInvoiceCode(1000L);
        detail.setExpirationDate(LocalDate.of(2026, 1, 20));
        detail.setTotalAmount(50000L);
        detail.setPendingValue(50000L);
        return detail;
    }
}

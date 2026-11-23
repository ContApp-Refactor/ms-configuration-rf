package co.unicauca.edu.co.contables.notifications.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import co.unicauca.edu.co.contables.notifications.application.output.IEmailProviderPort;
import co.unicauca.edu.co.contables.notifications.application.output.IThirdProviderPort;
import co.unicauca.edu.co.contables.notifications.domain.model.ThirdReplica;
import co.unicauca.edu.co.contables.notifications.infrastructure.output.messageBroker.dto.InvoiceDueReminderEventDto;
import co.unicauca.edu.co.contables.notifications.infrastructure.output.messageBroker.dto.InvoiceDetailEventDto;

@DisplayName("ProcessNotificationService - Unit Tests")
class ProcessNotificationServiceUnitTest {

    private ProcessNotificationService service;
    private IEmailProviderPort emailProviderMock;
    private TemplateEngine templateEngineMock;
    private IThirdProviderPort thirdProviderMock;

    @BeforeEach
    void setUp() {
        // Crear mocks sin cargar contexto de Spring
        emailProviderMock = mock(IEmailProviderPort.class);
        templateEngineMock = mock(TemplateEngine.class);
        thirdProviderMock = mock(IThirdProviderPort.class);

        // Inyectar las dependencias manualmente
        service = new ProcessNotificationService(emailProviderMock, templateEngineMock, thirdProviderMock);
    }

    @Test
    @DisplayName("Should process invoice due reminder successfully")
    void testProcessInvoiceDueReminder_Success() {
        // Arrange
        Long thirdPartyId = 100L;
        ThirdReplica thirdParty = new ThirdReplica(1L, thirdPartyId, "Juan Pérez", "juan@example.com");
        InvoiceDueReminderEventDto event = createValidEvent(thirdPartyId);

        when(thirdProviderMock.findByThirdPartyId(thirdPartyId))
                .thenReturn(Optional.of(thirdParty));
        when(templateEngineMock.process(eq("invoice-reminder-template"), any(Context.class)))
                .thenReturn("<html><body>Invoice Reminder</body></html>");

        // Act
        service.processInvoiceDueReminder(event);

        // Assert
        verify(thirdProviderMock, times(1)).findByThirdPartyId(thirdPartyId);
        verify(templateEngineMock, times(1)).process(eq("invoice-reminder-template"), any(Context.class));
        verify(emailProviderMock, times(1)).sendInvoiceReminderEmail(
                "Juan Pérez",
                "juan@example.com",
                "<html><body>Invoice Reminder</body></html>");
    }

    @Test
    @DisplayName("Should throw exception when third party not found")
    void testProcessInvoiceDueReminder_ThirdPartyNotFound() {
        // Arrange
        Long thirdPartyId = 999L;
        InvoiceDueReminderEventDto event = createValidEvent(thirdPartyId);

        when(thirdProviderMock.findByThirdPartyId(thirdPartyId))
                .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.processInvoiceDueReminder(event);
        });

        assertEquals("Tercero no encontrado con ID: 999", exception.getMessage());
        verify(thirdProviderMock, times(1)).findByThirdPartyId(thirdPartyId);
        verify(templateEngineMock, never()).process(anyString(), any(Context.class));
        verify(emailProviderMock, never()).sendInvoiceReminderEmail(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should send email with correct recipient information")
    void testProcessInvoiceDueReminder_EmailRecipientValidation() {
        // Arrange
        Long thirdPartyId = 50L;
        ThirdReplica thirdParty = new ThirdReplica(2L, thirdPartyId, "María García", "maria.garcia@empresa.com");
        InvoiceDueReminderEventDto event = createValidEvent(thirdPartyId);

        when(thirdProviderMock.findByThirdPartyId(thirdPartyId))
                .thenReturn(Optional.of(thirdParty));
        when(templateEngineMock.process(anyString(), any(Context.class)))
                .thenReturn("<html>Contenido HTML</html>");

        // Act
        service.processInvoiceDueReminder(event);

        // Assert
        verify(emailProviderMock).sendInvoiceReminderEmail(
                "María García",
                "maria.garcia@empresa.com",
                "<html>Contenido HTML</html>");
    }

    @Test
    @DisplayName("Should process template with correct context variables")
    void testProcessInvoiceDueReminder_TemplateContextValidation() {
        // Arrange
        Long thirdPartyId = 75L;
        ThirdReplica thirdParty = new ThirdReplica(3L, thirdPartyId, "Carlos López", "carlos@mail.com");
        InvoiceDueReminderEventDto event = createValidEvent(thirdPartyId);

        when(thirdProviderMock.findByThirdPartyId(thirdPartyId))
                .thenReturn(Optional.of(thirdParty));
        when(templateEngineMock.process(anyString(), any(Context.class)))
                .thenReturn("<html>Result</html>");

        // Act
        service.processInvoiceDueReminder(event);

        // Assert
        verify(templateEngineMock, times(1)).process(
                eq("invoice-reminder-template"),
                any(Context.class));
    }

    @Test
    @DisplayName("Should handle multiple invoices in event details")
    void testProcessInvoiceDueReminder_MultipleInvoices() {
        // Arrange
        Long thirdPartyId = 200L;
        ThirdReplica thirdParty = new ThirdReplica(4L, thirdPartyId, "Roberto Sánchez", "roberto@example.com");

        List<InvoiceDetailEventDto> invoices = Arrays.asList(
                createInvoiceDetail(1L, 100L, LocalDate.of(2026, 1, 15), 50000L, 50000L),
                createInvoiceDetail(2L, 101L, LocalDate.of(2026, 2, 20), 75000L, 0L),
                createInvoiceDetail(3L, 102L, LocalDate.of(2026, 3, 10), 100000L, 100000L));

        InvoiceDueReminderEventDto event = new InvoiceDueReminderEventDto();
        event.setThirdPartyId(thirdPartyId);
        event.setInvoiceDetails(invoices);

        when(thirdProviderMock.findByThirdPartyId(thirdPartyId))
                .thenReturn(Optional.of(thirdParty));
        when(templateEngineMock.process(anyString(), any(Context.class)))
                .thenReturn("<html>Multi-invoice template</html>");

        // Act
        service.processInvoiceDueReminder(event);

        // Assert
        verify(emailProviderMock, times(1)).sendInvoiceReminderEmail(
                "Roberto Sánchez",
                "roberto@example.com",
                "<html>Multi-invoice template</html>");
    }

    @Test
    @DisplayName("Should throw exception if event is null")
    void testProcessInvoiceDueReminder_NullEvent() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            service.processInvoiceDueReminder(null);
        });

        verify(thirdProviderMock, never()).findByThirdPartyId(anyLong());
        verify(emailProviderMock, never()).sendInvoiceReminderEmail(anyString(), anyString(), anyString());
    }

    // Helper methods

    private InvoiceDueReminderEventDto createValidEvent(Long thirdPartyId) {
        InvoiceDueReminderEventDto event = new InvoiceDueReminderEventDto();
        event.setThirdPartyId(thirdPartyId);
        event.setInvoiceDetails(Arrays.asList(
                createInvoiceDetail(1L, 1000L, LocalDate.of(2026, 1, 20), 50000L, 50000L)));
        return event;
    }

    private InvoiceDetailEventDto createInvoiceDetail(Long id, Long code, LocalDate expirationDate,
            Long totalAmount, Long pendingValue) {
        InvoiceDetailEventDto detail = new InvoiceDetailEventDto();
        detail.setInvoiceId(id);
        detail.setInvoiceCode(code);
        detail.setExpirationDate(expirationDate);
        detail.setTotalAmount(totalAmount);
        detail.setPendingValue(pendingValue);
        return detail;
    }

}

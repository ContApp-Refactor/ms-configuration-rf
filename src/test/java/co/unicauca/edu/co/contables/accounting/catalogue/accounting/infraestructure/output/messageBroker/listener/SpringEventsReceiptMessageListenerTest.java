package co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.input.IReceiptProcessInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.Receipt;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.DTO.ReceiptEventDTO;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.mapper.IReceiptEventInboundMapper;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports.IMessageErrorHandlingPort;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.ReceiptAccountingEvent;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.ReceiptDetailEventDto;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.ReceiptEventDto;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit tests for SpringEventsReceiptMessageListener")
class SpringEventsReceiptMessageListenerTest {

    @Mock
    private IReceiptProcessInputPort receiptProcessInputPort;

    @Mock
    private IReceiptEventInboundMapper receiptEventMapper;

    @Mock
    private IMessageErrorHandlingPort messageErrorHandlingPort;

    private SpringEventsReceiptMessageListener listener;

    @BeforeEach
    void setUp() {
        listener = new SpringEventsReceiptMessageListener(
                receiptProcessInputPort,
                receiptEventMapper,
                messageErrorHandlingPort);
    }

    @Test
    @DisplayName("RECEIPT_CREATED maps every producer field and processes the creation")
    void shouldProcessReceiptCreatedEvent() {
        Receipt receipt = Receipt.builder().build();
        when(receiptEventMapper.toDomain(any(ReceiptEventDTO.class))).thenReturn(receipt);

        listener.onReceiptEvent(new ReceiptAccountingEvent(
                ReceiptAccountingEvent.RECEIPT_CREATED,
                invoicePaymentReceiptDto()));

        ArgumentCaptor<ReceiptEventDTO> captor = ArgumentCaptor.forClass(ReceiptEventDTO.class);
        verify(receiptEventMapper).toDomain(captor.capture());
        ReceiptEventDTO local = captor.getValue();
        assertThat(local.getId()).isEqualTo(7L);
        assertThat(local.getReceiptCode()).isEqualTo("RC-100");
        assertThat(local.getThirdPartyId()).isEqualTo(456L);
        assertThat(local.getEnterpriseId()).isEqualTo("ENT-1");
        assertThat(local.getReceiptTypeId()).isEqualTo(1L);
        assertThat(local.getPaymentMethodId()).isEqualTo(2L);
        assertThat(local.getPaymentMethodAccount()).isEqualTo(110501L);
        assertThat(local.getStatus()).isEqualTo("FINALIZED");
        assertThat(local.getIssueDate()).isEqualTo(LocalDate.of(2026, 11, 19));
        assertThat(local.getTotalAmount()).isEqualByComparingTo("1000.00");
        assertThat(local.getObservations()).isEqualTo("obs");
        assertThat(local.getCenterCostId()).isEqualTo(9L);
        assertThat(local.getDetails()).hasSize(1);
        assertThat(local.getDetails().get(0).getInvoiceId()).isEqualTo(11L);
        assertThat(local.getDetails().get(0).getAmountPaid()).isEqualByComparingTo("500.00");
        assertThat(local.getDetails().get(0).getInvoiceCode()).isEqualTo("2024001");
        assertThat(local.getDetails().get(0).getAccountingAccount()).isEqualTo(130505L);

        verify(receiptProcessInputPort).processReceiptCreation(receipt);
        verifyNoInteractions(messageErrorHandlingPort);
    }

    @Test
    @DisplayName("RECEIPT_VOIDED processes the void operation")
    void shouldProcessReceiptVoidedEvent() {
        Receipt receipt = Receipt.builder().build();
        when(receiptEventMapper.toDomain(any(ReceiptEventDTO.class))).thenReturn(receipt);

        listener.onReceiptEvent(new ReceiptAccountingEvent(
                ReceiptAccountingEvent.RECEIPT_VOIDED,
                invoicePaymentReceiptDto()));

        verify(receiptProcessInputPort).processReceiptVoid(receipt);
        verifyNoInteractions(messageErrorHandlingPort);
    }

    @Test
    @DisplayName("unknown event type is ignored without touching the process port")
    void shouldIgnoreUnknownEventType() {
        when(receiptEventMapper.toDomain(any(ReceiptEventDTO.class)))
                .thenReturn(Receipt.builder().build());

        listener.onReceiptEvent(new ReceiptAccountingEvent("UNKNOWN", invoicePaymentReceiptDto()));

        verifyNoInteractions(receiptProcessInputPort, messageErrorHandlingPort);
    }

    @Test
    @DisplayName("validation error is persisted and swallowed")
    void shouldPersistValidationErrorAndSwallow() {
        ReceiptEventDto dto = invoicePaymentReceiptDto();
        dto.setReceiptCode("  ");

        assertThatCode(() -> listener.onReceiptEvent(new ReceiptAccountingEvent(
                ReceiptAccountingEvent.RECEIPT_CREATED, dto)))
                .doesNotThrowAnyException();

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("RECEIPT_CREATED"),
                eq("Validation failed: ReceiptCode is null or blank"),
                contains("\"receiptCode\":\"  \""),
                eq("Receipt"));
        verifyNoInteractions(receiptEventMapper, receiptProcessInputPort);
    }

    @Test
    @DisplayName("processing error is persisted and swallowed")
    void shouldPersistProcessingErrorAndSwallow() {
        when(receiptEventMapper.toDomain(any(ReceiptEventDTO.class)))
                .thenThrow(new IllegalStateException("database down"));

        assertThatCode(() -> listener.onReceiptEvent(new ReceiptAccountingEvent(
                ReceiptAccountingEvent.RECEIPT_CREATED, invoicePaymentReceiptDto())))
                .doesNotThrowAnyException();

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("RECEIPT_CREATED"),
                eq("Processing error: database down"),
                contains("RC-100"),
                eq("Receipt"));
    }

    private ReceiptEventDto invoicePaymentReceiptDto() {
        ReceiptEventDto dto = new ReceiptEventDto();
        dto.setId(7L);
        dto.setReceiptCode("RC-100");
        dto.setThirdPartyId(456L);
        dto.setEnterpriseId("ENT-1");
        dto.setReceiptTypeId(1L);
        dto.setPaymentMethodId(2L);
        dto.setPaymentMethodAccount(110501L);
        dto.setStatus("FINALIZED");
        dto.setIssueDate(LocalDate.of(2026, 11, 19));
        dto.setTotalAmount(new BigDecimal("1000.00"));
        dto.setObservations("obs");
        dto.setCenterCostId(9L);

        ReceiptDetailEventDto detail = new ReceiptDetailEventDto();
        detail.setInvoiceId(11L);
        detail.setAmountPaid(new BigDecimal("500.00"));
        detail.setInvoiceCode("2024001");
        detail.setAccountingAccount(130505L);
        dto.setDetails(List.of(detail));

        return dto;
    }
}

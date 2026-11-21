package co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.input.IWriteOffProcessInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.PortfolioWriteOff;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.DTO.PortfolioWriteOffResponse;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.mapper.IPortfolioWriteOffInboundMapper;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.enums.WriteOffStatus;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports.IMessageErrorHandlingPort;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.InvoiceSummaryEventDto;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.PortfolioWriteOffEventDto;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.WriteOffAccountingEvent;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.WriteOffDetailEventDto;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit tests for SpringEventsWriteOffMessageListener")
class SpringEventsWriteOffMessageListenerTest {

    @Mock
    private IWriteOffProcessInputPort writeOffProcessInputPort;

    @Mock
    private IPortfolioWriteOffInboundMapper writeOffEventMapper;

    @Mock
    private IMessageErrorHandlingPort messageErrorHandlingPort;

    private SpringEventsWriteOffMessageListener listener;

    @BeforeEach
    void setUp() {
        listener = new SpringEventsWriteOffMessageListener(
                writeOffProcessInputPort,
                writeOffEventMapper,
                messageErrorHandlingPort);
    }

    @Test
    @DisplayName("WRITEOFF_CONFIRMED maps every producer field and processes the confirmation")
    void shouldProcessWriteOffConfirmedEvent() {
        PortfolioWriteOff writeOff = PortfolioWriteOff.builder().build();
        when(writeOffEventMapper.toDomain(any(PortfolioWriteOffResponse.class))).thenReturn(writeOff);

        listener.onWriteOffEvent(new WriteOffAccountingEvent(
                WriteOffAccountingEvent.WRITEOFF_CONFIRMED,
                writeOffDto()));

        ArgumentCaptor<PortfolioWriteOffResponse> captor = ArgumentCaptor.forClass(PortfolioWriteOffResponse.class);
        verify(writeOffEventMapper).toDomain(captor.capture());
        PortfolioWriteOffResponse local = captor.getValue();
        assertThat(local.getId()).isEqualTo(7L);
        assertThat(local.getCode()).isEqualTo("CC-100");
        assertThat(local.getJustification()).isEqualTo("obs");
        assertThat(local.getTotalAmount()).isEqualTo(1000L);
        assertThat(local.getWriteOffDate()).isEqualTo(LocalDate.of(2026, 11, 19));
        assertThat(local.getDebitAuxiliaryAccount()).isEqualTo(110501L);
        assertThat(local.getDebitAuxiliaryAccountId()).isEqualTo(55L);
        assertThat(local.getThirdId()).isEqualTo(456L);
        assertThat(local.getStatus()).isEqualTo(WriteOffStatus.CONFIRMED);
        assertThat(local.getEnterpriseId()).isEqualTo("ENT-1");
        assertThat(local.getCenterCostId()).isEqualTo(9L);
        assertThat(local.getDetails()).hasSize(1);
        assertThat(local.getDetails().get(0).getAmountWrittenOff()).isEqualTo(500L);
        assertThat(local.getDetails().get(0).getInvoice().getId()).isEqualTo(11L);
        assertThat(local.getDetails().get(0).getInvoice().getFactCode()).isEqualTo("2024001");
        assertThat(local.getDetails().get(0).getInvoice().getTotalValue()).isEqualTo(1000L);
        assertThat(local.getDetails().get(0).getInvoice().getPendingValue()).isEqualTo(500L);
        assertThat(local.getDetails().get(0).getInvoice().getExpirationDate())
                .isEqualTo(LocalDate.of(2026, 11, 30));
        assertThat(local.getDetails().get(0).getInvoice().getAccountingAccount()).isEqualTo(130505L);

        verify(writeOffProcessInputPort).processWriteOffConfirmation(writeOff);
        verifyNoInteractions(messageErrorHandlingPort);
    }

    @Test
    @DisplayName("WRITEOFF_VOIDED processes the annulment")
    void shouldProcessWriteOffVoidedEvent() {
        PortfolioWriteOff writeOff = PortfolioWriteOff.builder().build();
        when(writeOffEventMapper.toDomain(any(PortfolioWriteOffResponse.class))).thenReturn(writeOff);

        listener.onWriteOffEvent(new WriteOffAccountingEvent(
                WriteOffAccountingEvent.WRITEOFF_VOIDED,
                writeOffDto()));

        verify(writeOffProcessInputPort).processWriteOffAnnulment(writeOff);
        verifyNoInteractions(messageErrorHandlingPort);
    }

    @Test
    @DisplayName("unknown event type is ignored without touching the process port")
    void shouldIgnoreUnknownEventType() {
        when(writeOffEventMapper.toDomain(any(PortfolioWriteOffResponse.class)))
                .thenReturn(PortfolioWriteOff.builder().build());

        listener.onWriteOffEvent(new WriteOffAccountingEvent("UNKNOWN", writeOffDto()));

        verifyNoInteractions(writeOffProcessInputPort, messageErrorHandlingPort);
    }

    @Test
    @DisplayName("validation error is persisted with the error message and swallowed")
    void shouldPersistValidationErrorAndSwallow() {
        PortfolioWriteOffEventDto dto = writeOffDto();
        dto.setId(null);

        assertThatCode(() -> listener.onWriteOffEvent(new WriteOffAccountingEvent(
                WriteOffAccountingEvent.WRITEOFF_CONFIRMED, dto)))
                .doesNotThrowAnyException();

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("WRITEOFF_CONFIRMED"),
                eq("Validation failed: Id is null"),
                contains("\"id\":null"),
                eq("PortfolioWriteOff"));
        verifyNoInteractions(writeOffEventMapper, writeOffProcessInputPort);
    }

    @Test
    @DisplayName("processing error is persisted and swallowed")
    void shouldPersistProcessingErrorAndSwallow() {
        when(writeOffEventMapper.toDomain(any(PortfolioWriteOffResponse.class)))
                .thenThrow(new IllegalStateException("database down"));

        assertThatCode(() -> listener.onWriteOffEvent(new WriteOffAccountingEvent(
                WriteOffAccountingEvent.WRITEOFF_CONFIRMED, writeOffDto())))
                .doesNotThrowAnyException();

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("WRITEOFF_CONFIRMED"),
                eq("Processing error: database down"),
                contains("CC-100"),
                eq("PortfolioWriteOff"));
    }

    private PortfolioWriteOffEventDto writeOffDto() {
        InvoiceSummaryEventDto invoice = InvoiceSummaryEventDto.builder()
                .id(11L)
                .factCode("2024001")
                .totalValue(1000L)
                .pendingValue(500L)
                .expirationDate(LocalDate.of(2026, 11, 30))
                .accountingAccount(130505L)
                .build();

        WriteOffDetailEventDto detail = WriteOffDetailEventDto.builder()
                .amountWrittenOff(500L)
                .invoice(invoice)
                .build();

        PortfolioWriteOffEventDto dto = new PortfolioWriteOffEventDto();
        dto.setId(7L);
        dto.setCode("CC-100");
        dto.setJustification("obs");
        dto.setTotalAmount(1000L);
        dto.setWriteOffDate(LocalDate.of(2026, 11, 19));
        dto.setDebitAuxiliaryAccount(110501L);
        dto.setDebitAuxiliaryAccountId(55L);
        dto.setThirdId(456L);
        dto.setStatus(WriteOffStatus.CONFIRMED);
        dto.setEnterpriseId("ENT-1");
        dto.setCenterCostId(9L);
        dto.setDetails(List.of(detail));
        return dto;
    }
}

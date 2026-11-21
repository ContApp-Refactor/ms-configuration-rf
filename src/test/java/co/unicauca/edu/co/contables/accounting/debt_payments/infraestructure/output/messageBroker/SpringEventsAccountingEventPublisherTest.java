package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker;

import static co.unicauca.edu.co.contables.accounting.debt_payments.test.fixtures.TestFixtures.invoiceReplicaWith;
import static co.unicauca.edu.co.contables.accounting.debt_payments.test.fixtures.TestFixtures.portfolioWriteOffWithDetails;
import static co.unicauca.edu.co.contables.accounting.debt_payments.test.fixtures.TestFixtures.receiptForDirectIncome;
import static co.unicauca.edu.co.contables.accounting.debt_payments.test.fixtures.TestFixtures.writeOffDetail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;

import co.unicauca.edu.co.contables.accounting.debt_payments.application.output.IInvoiceProviderPort;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.enums.InvoiceStatus;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.PortfolioWriteOffEventDto;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.ReceiptAccountingEvent;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.ReceiptEventDto;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.WriteOffAccountingEvent;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.mapper.IPortfolioWriteOffEventMapper;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.mapper.IReceiptEventMapper;
import co.unicauca.edu.co.contables.commons.security.IJwtUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit tests for SpringEventsAccountingEventPublisher")
class SpringEventsAccountingEventPublisherTest {

    @Mock
    private IReceiptEventMapper receiptEventMapper;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private IPortfolioWriteOffEventMapper writeOffEventMapper;

    @Mock
    private IInvoiceProviderPort invoiceProviderPort;

    @Mock
    private IJwtUtils jwtUtils;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private SpringEventsAccountingEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new SpringEventsAccountingEventPublisher(
                receiptEventMapper,
                rabbitTemplate,
                writeOffEventMapper,
                invoiceProviderPort,
                jwtUtils,
                eventPublisher);
    }

    @Test
    @DisplayName("publishReceiptCreatedEvent publishes RECEIPT_CREATED in-process and does not touch Rabbit")
    void shouldPublishReceiptCreatedEventInProcess() {
        var receipt = receiptForDirectIncome("ENT-1", 1L, 2L, "test", 1000L, 100L);
        var receiptDto = new ReceiptEventDto();
        receiptDto.setReceiptCode("RC-1");
        when(receiptEventMapper.toEventDto(receipt)).thenReturn(receiptDto);

        publisher.publishReceiptCreatedEvent(receipt);

        ReceiptAccountingEvent event = capturePublishedEvent(ReceiptAccountingEvent.class);
        assertThat(event.getType()).isEqualTo(ReceiptAccountingEvent.RECEIPT_CREATED);
        assertThat(event.getData()).isSameAs(receiptDto);
        verifyNoInteractions(rabbitTemplate, jwtUtils);
    }

    @Test
    @DisplayName("publishVoidReceiptEvent publishes RECEIPT_VOIDED in-process and does not touch Rabbit")
    void shouldPublishVoidReceiptEventInProcess() {
        var receipt = receiptForDirectIncome("ENT-1", 2L, 3L, "voided", 2000L, 200L);
        var receiptDto = new ReceiptEventDto();
        receiptDto.setReceiptCode("RC-2");
        when(receiptEventMapper.toEventDto(receipt)).thenReturn(receiptDto);

        publisher.publishVoidReceiptEvent(receipt);

        ReceiptAccountingEvent event = capturePublishedEvent(ReceiptAccountingEvent.class);
        assertThat(event.getType()).isEqualTo(ReceiptAccountingEvent.RECEIPT_VOIDED);
        assertThat(event.getData()).isSameAs(receiptDto);
        verifyNoInteractions(rabbitTemplate, jwtUtils);
    }

    @Test
    @DisplayName("receipt publisher failure propagates")
    void shouldPropagatePublishFailure() {
        var receipt = receiptForDirectIncome("ENT-1", 1L, 2L, "test", 1000L, 100L);
        var receiptDto = new ReceiptEventDto();
        when(receiptEventMapper.toEventDto(receipt)).thenReturn(receiptDto);
        doThrow(new IllegalStateException("event bus down")).when(eventPublisher).publishEvent(any(Object.class));

        assertThatThrownBy(() -> publisher.publishReceiptCreatedEvent(receipt))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("event bus down");

        verify(receiptEventMapper).toEventDto(receipt);
        verifyNoInteractions(rabbitTemplate, jwtUtils);
    }

    @Test
    @DisplayName("publishWriteOffConfirmedEvent enriches details and publishes WRITEOFF_CONFIRMED in-process")
    void shouldPublishWriteOffConfirmedEventInProcess() {
        PortfolioWriteOffEventDto eventDto = enrichedWriteOffEventDto();

        publisher.publishWriteOffConfirmedEvent(pendingWriteOff());

        WriteOffAccountingEvent event = capturePublishedEvent(WriteOffAccountingEvent.class);
        assertThat(event.getType()).isEqualTo(WriteOffAccountingEvent.WRITEOFF_CONFIRMED);
        assertThat(event.getData()).isSameAs(eventDto);
        assertThat(event.getData().getDetails()).hasSize(1);
        assertThat(event.getData().getDetails().get(0).getAmountWrittenOff()).isEqualTo(500L);
        assertThat(event.getData().getDetails().get(0).getInvoice().getId()).isEqualTo(1L);
        assertThat(event.getData().getDetails().get(0).getInvoice().getPendingValue()).isEqualTo(500L);
        verifyNoInteractions(rabbitTemplate, jwtUtils);
    }

    @Test
    @DisplayName("publishWriteOffVoidedEvent enriches details and publishes WRITEOFF_VOIDED in-process")
    void shouldPublishWriteOffVoidedEventInProcess() {
        PortfolioWriteOffEventDto eventDto = enrichedWriteOffEventDto();

        publisher.publishWriteOffVoidedEvent(pendingWriteOff());

        WriteOffAccountingEvent event = capturePublishedEvent(WriteOffAccountingEvent.class);
        assertThat(event.getType()).isEqualTo(WriteOffAccountingEvent.WRITEOFF_VOIDED);
        assertThat(event.getData()).isSameAs(eventDto);
        assertThat(event.getData().getDetails()).hasSize(1);
        verifyNoInteractions(rabbitTemplate, jwtUtils);
    }

    private co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.PortfolioWriteOff pendingWriteOff() {
        var detail = writeOffDetail(1L, 500L);
        return portfolioWriteOffWithDetails("ENT-1", 10L, "test", List.of(detail));
    }

    private PortfolioWriteOffEventDto enrichedWriteOffEventDto() {
        var eventDto = new PortfolioWriteOffEventDto();
        eventDto.setCode("CC-1");
        var invoice = invoiceReplicaWith(1L, 1000L, 500L, InvoiceStatus.PENDING);
        when(writeOffEventMapper.toEventDto(any())).thenReturn(eventDto);
        when(invoiceProviderPort.findInvoicesByIds(List.of(1L))).thenReturn(List.of(invoice));
        return eventDto;
    }

    private <T> T capturePublishedEvent(Class<T> eventType) {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue()).isInstanceOf(eventType);
        return eventType.cast(captor.getValue());
    }
}

package co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.output.messageBroker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import co.unicauca.edu.co.contables.accounting.treasury.domain.model.PaymentVoucher;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.TreasuryEvent;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.input.rabbit.TreasuryAccountingEventDtos.TreasuryAccountingEvent;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit tests for SpringEventsTreasuryEventPublisher")
class SpringEventsTreasuryEventPublisherTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private ObjectMapper mapper;
    private SpringEventsTreasuryEventPublisher publisher;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        publisher = new SpringEventsTreasuryEventPublisher(mapper, eventPublisher);
    }

    @Test
    @DisplayName("PAYMENT_VOUCHER_CREATED publishes the accounting envelope in-process")
    void shouldPublishAccountingEventInProcess() {
        PaymentVoucher voucher = voucher();

        publisher.enqueue(new TreasuryEvent(
                "evt-1", "PAYMENT_VOUCHER", 10L, "PAYMENT_VOUCHER_CREATED",
                voucher, "tenant-1", "ENT-1", "corr-1"));

        TreasuryAccountingEvent event = capturePublishedEvent();
        assertThat(event.eventId()).isEqualTo("evt-1");
        assertThat(event.eventType()).isEqualTo("PAYMENT_VOUCHER_CREATED");
        assertThat(event.eventVersion()).isEqualTo(1);
        assertThat(event.tenantId()).isEqualTo("tenant-1");
        assertThat(event.enterpriseId()).isEqualTo("ENT-1");
        assertThat(event.correlationId()).isEqualTo("corr-1");
        assertThat(event.sourceDocument().type()).isEqualTo("PAYMENT_VOUCHER");
        assertThat(event.sourceDocument().id()).isEqualTo(10L);
        assertThat(event.payload().get("id").asLong()).isEqualTo(10L);
        assertThat(event.payload().get("voucherNumber").asText()).isEqualTo("PV-1");
        assertThat(event.payload().get("total").decimalValue()).isEqualByComparingTo("150.00");
    }

    @Test
    @DisplayName("PAYABLE_WRITEOFF_CREATED maps the aggregate into sourceDocument")
    void shouldMapAggregateIntoSourceDocument() {
        publisher.enqueue(new TreasuryEvent(
                "evt-2", "PAYABLE_WRITEOFF", 20L, "PAYABLE_WRITEOFF_CREATED",
                new co.unicauca.edu.co.contables.accounting.treasury.domain.model.PayableWriteOff(),
                "tenant-1", "ENT-1", "corr-2"));

        TreasuryAccountingEvent event = capturePublishedEvent();
        assertThat(event.eventType()).isEqualTo("PAYABLE_WRITEOFF_CREATED");
        assertThat(event.sourceDocument().type()).isEqualTo("PAYABLE_WRITEOFF");
        assertThat(event.sourceDocument().id()).isEqualTo(20L);
    }

    @Test
    @DisplayName("PAYMENT_VOUCHER_POSTED is logged and dropped (dormant notification branch)")
    void shouldDropPaymentVoucherPosted() {
        publisher.enqueue(new TreasuryEvent(
                "evt-3", "PAYMENT_VOUCHER", 10L, "PAYMENT_VOUCHER_POSTED",
                voucher(), "tenant-1", "ENT-1", "corr-3"));

        verifyNoInteractions(eventPublisher);
    }

    private TreasuryAccountingEvent capturePublishedEvent() {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue()).isInstanceOf(TreasuryAccountingEvent.class);
        return (TreasuryAccountingEvent) captor.getValue();
    }

    private PaymentVoucher voucher() {
        PaymentVoucher voucher = new PaymentVoucher();
        voucher.setId(10L);
        voucher.setVoucherNumber("PV-1");
        voucher.setEnterpriseId("ENT-1");
        voucher.setIssueDate(LocalDate.of(2026, 11, 20));
        voucher.setTotal(new BigDecimal("150.00"));
        return voucher;
    }
}

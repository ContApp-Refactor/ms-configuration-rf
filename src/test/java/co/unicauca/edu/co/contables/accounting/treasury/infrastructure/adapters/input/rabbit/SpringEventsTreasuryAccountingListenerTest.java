package co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.input.rabbit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import co.unicauca.edu.co.contables.accounting.treasury.application.input.ITreasuryAccountingInputPort;
import co.unicauca.edu.co.contables.accounting.treasury.application.input.ITreasuryAccountingInputPort.AccountingEventContext;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.PayableWriteOff;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.PaymentVoucher;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.input.rabbit.TreasuryAccountingEventDtos.SourceDocument;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.input.rabbit.TreasuryAccountingEventDtos.TreasuryAccountingEvent;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit tests for SpringEventsTreasuryAccountingListener")
class SpringEventsTreasuryAccountingListenerTest {

    @Mock
    private ITreasuryAccountingInputPort treasuryAccounting;

    private ObjectMapper mapper;
    private SpringEventsTreasuryAccountingListener listener;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        listener = new SpringEventsTreasuryAccountingListener(treasuryAccounting, mapper);
    }

    @Test
    @DisplayName("PAYMENT_VOUCHER_CREATED converts the payload and posts the voucher")
    void shouldPostPaymentVoucher() {
        PaymentVoucher voucher = voucher();
        listener.onAccountingEvent(event("PAYMENT_VOUCHER_CREATED", "PAYMENT_VOUCHER", 10L, voucher));

        ArgumentCaptor<PaymentVoucher> voucherCaptor = ArgumentCaptor.forClass(PaymentVoucher.class);
        ArgumentCaptor<AccountingEventContext> contextCaptor = ArgumentCaptor.forClass(AccountingEventContext.class);
        verify(treasuryAccounting).postPaymentVoucher(voucherCaptor.capture(), contextCaptor.capture());

        assertThat(voucherCaptor.getValue().getId()).isEqualTo(10L);
        assertThat(voucherCaptor.getValue().getVoucherNumber()).isEqualTo("PV-1");
        assertThat(voucherCaptor.getValue().getTotal()).isEqualByComparingTo("150.00");
        assertThat(contextCaptor.getValue().eventId()).isEqualTo("evt-1");
        assertThat(contextCaptor.getValue().eventType()).isEqualTo("PAYMENT_VOUCHER_CREATED");
        assertThat(contextCaptor.getValue().tenantId()).isEqualTo("tenant-1");
        assertThat(contextCaptor.getValue().enterpriseId()).isEqualTo("ENT-1");
        assertThat(contextCaptor.getValue().correlationId()).isEqualTo("corr-1");
    }

    @Test
    @DisplayName("PAYMENT_VOUCHER_VOID_REQUESTED voids by source document id")
    void shouldVoidPaymentVoucher() {
        listener.onAccountingEvent(event(
                "PAYMENT_VOUCHER_VOID_REQUESTED", "PAYMENT_VOUCHER", 10L, voucher()));

        ArgumentCaptor<AccountingEventContext> contextCaptor = ArgumentCaptor.forClass(AccountingEventContext.class);
        verify(treasuryAccounting).voidPaymentVoucher(
                org.mockito.ArgumentMatchers.eq(10L), contextCaptor.capture());
        assertThat(contextCaptor.getValue().eventType()).isEqualTo("PAYMENT_VOUCHER_VOID_REQUESTED");
    }

    @Test
    @DisplayName("PAYABLE_WRITEOFF_CREATED converts the payload and posts the write-off")
    void shouldPostPayableWriteOff() {
        PayableWriteOff writeOff = writeOff();
        listener.onAccountingEvent(event("PAYABLE_WRITEOFF_CREATED", "PAYABLE_WRITEOFF", 20L, writeOff));

        ArgumentCaptor<PayableWriteOff> writeOffCaptor = ArgumentCaptor.forClass(PayableWriteOff.class);
        verify(treasuryAccounting).postPayableWriteOff(
                writeOffCaptor.capture(), any(AccountingEventContext.class));

        assertThat(writeOffCaptor.getValue().getId()).isEqualTo(20L);
        assertThat(writeOffCaptor.getValue().getReason()).isEqualTo("write-off");
        assertThat(writeOffCaptor.getValue().getTotal()).isEqualByComparingTo("75.00");
    }

    @Test
    @DisplayName("PAYABLE_WRITEOFF_VOID_REQUESTED voids by source document id")
    void shouldVoidPayableWriteOff() {
        listener.onAccountingEvent(event(
                "PAYABLE_WRITEOFF_VOID_REQUESTED", "PAYABLE_WRITEOFF", 20L, writeOff()));

        verify(treasuryAccounting).voidPayableWriteOff(
                org.mockito.ArgumentMatchers.eq(20L), any(AccountingEventContext.class));
    }

    @Test
    @DisplayName("unsupported event type is ignored")
    void shouldIgnoreUnsupportedEventType() {
        listener.onAccountingEvent(event("SOMETHING_ELSE", "PAYMENT_VOUCHER", 10L, voucher()));

        verifyNoInteractions(treasuryAccounting);
    }

    @Test
    @DisplayName("event without type is ignored")
    void shouldIgnoreEventWithoutType() {
        listener.onAccountingEvent(new TreasuryAccountingEvent(
                "evt-1", null, 1, Instant.parse("2026-11-20T10:00:00Z"),
                "tenant-1", "ENT-1", "corr-1",
                new SourceDocument("PAYMENT_VOUCHER", 10L),
                mapper.valueToTree(voucher())));

        verifyNoInteractions(treasuryAccounting);
    }

    @Test
    @DisplayName("processing error is logged and swallowed")
    void shouldSwallowProcessingError() {
        doThrow(new IllegalStateException("database down"))
                .when(treasuryAccounting).voidPaymentVoucher(any(), any());

        assertThatCode(() -> listener.onAccountingEvent(event(
                "PAYMENT_VOUCHER_VOID_REQUESTED", "PAYMENT_VOUCHER", 10L, voucher())))
                .doesNotThrowAnyException();

        verify(treasuryAccounting).voidPaymentVoucher(
                org.mockito.ArgumentMatchers.eq(10L), any(AccountingEventContext.class));
    }

    private TreasuryAccountingEvent event(String type, String aggregateType, Long aggregateId, Object payload) {
        return new TreasuryAccountingEvent(
                "evt-1", type, 1, Instant.parse("2026-11-20T10:00:00Z"),
                "tenant-1", "ENT-1", "corr-1",
                new SourceDocument(aggregateType, aggregateId),
                mapper.valueToTree(payload));
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

    private PayableWriteOff writeOff() {
        PayableWriteOff writeOff = new PayableWriteOff();
        writeOff.setId(20L);
        writeOff.setEnterpriseId("ENT-1");
        writeOff.setReason("write-off");
        writeOff.setTotal(new BigDecimal("75.00"));
        return writeOff;
    }
}

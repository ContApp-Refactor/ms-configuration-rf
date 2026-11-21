package co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.input.rabbit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
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

import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.PurchaseInvoiceEventDto;
import co.unicauca.edu.co.contables.accounting.treasury.application.input.IInvoiceSynchronizationUseCase;
import co.unicauca.edu.co.contables.accounting.treasury.application.input.IPayableWriteOffCommandUseCase;
import co.unicauca.edu.co.contables.accounting.treasury.application.input.IPaymentScheduleExecutionUseCase;
import co.unicauca.edu.co.contables.accounting.treasury.application.input.IPaymentVoucherCommandUseCase;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.command.TreasuryCommands;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.input.rabbit.TreasuryRabbitDtos.AccountingResult;

@ExtendWith(MockitoExtension.class)
@DisplayName("treasury SpringEventsTreasuryEventListener Unit Tests")
class SpringEventsTreasuryEventListenerTest {

    @Mock
    private IInvoiceSynchronizationUseCase invoiceSynchronization;

    @Mock
    private IPaymentVoucherCommandUseCase vouchers;

    @Mock
    private IPayableWriteOffCommandUseCase writeOffs;

    @Mock
    private IPaymentScheduleExecutionUseCase schedules;

    private SpringEventsTreasuryEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new SpringEventsTreasuryEventListener(
                invoiceSynchronization, vouchers, writeOffs, schedules);
    }

    @Test
    @DisplayName("traduce el DTO de compra al comando local campo a campo")
    void traduceElDtoAlComandoLocal() {
        PurchaseInvoiceEventDto dto = PurchaseInvoiceEventDto.builder()
                .eventId("evt-1")
                .eventType("PURCHASE_INVOICE_UPDATED")
                .invoiceId(9L)
                .reference("1001")
                .enterpriseId("ENT-1")
                .supplierId(77L)
                .originalAmount(new BigDecimal("100.50"))
                .paidAmount(new BigDecimal("40.25"))
                .pendingAmount(new BigDecimal("60.25"))
                .issueDate(LocalDate.of(2026, 5, 10))
                .dueDate(LocalDate.of(2026, 6, 10))
                .payableAccountId(220505L)
                .payableAccountCode("22050501")
                .active(true)
                .tenantId("tenant-a")
                .build();

        listener.invoice(dto);

        ArgumentCaptor<TreasuryCommands.PurchaseInvoiceEvent> captor =
                ArgumentCaptor.forClass(TreasuryCommands.PurchaseInvoiceEvent.class);
        verify(invoiceSynchronization).synchronize(captor.capture());
        TreasuryCommands.PurchaseInvoiceEvent command = captor.getValue();
        assertThat(command.eventId()).isEqualTo("evt-1");
        assertThat(command.eventType()).isEqualTo("PURCHASE_INVOICE_UPDATED");
        assertThat(command.invoiceId()).isEqualTo(9L);
        assertThat(command.reference()).isEqualTo("1001");
        assertThat(command.enterpriseId()).isEqualTo("ENT-1");
        assertThat(command.supplierId()).isEqualTo(77L);
        assertThat(command.originalAmount()).isEqualByComparingTo("100.50");
        assertThat(command.paidAmount()).isEqualByComparingTo("40.25");
        assertThat(command.pendingAmount()).isEqualByComparingTo("60.25");
        assertThat(command.issueDate()).isEqualTo(LocalDate.of(2026, 5, 10));
        assertThat(command.dueDate()).isEqualTo(LocalDate.of(2026, 6, 10));
        assertThat(command.payableAccountId()).isEqualTo(220505L);
        assertThat(command.payableAccountCode()).isEqualTo("22050501");
        assertThat(command.active()).isTrue();
        assertThat(command.tenantId()).isEqualTo("tenant-a");
        verifyNoInteractions(vouchers, writeOffs, schedules);
    }

    @Test
    @DisplayName("un fallo de sincronización de compra se registra y no se propaga")
    void falloDeCompraSeTraga() {
        doThrow(new IllegalStateException("treasury caido"))
                .when(invoiceSynchronization).synchronize(any());

        assertThatCode(() -> listener.invoice(PurchaseInvoiceEventDto.builder()
                .eventId("evt-2")
                .eventType("PURCHASE_INVOICE_CREATED")
                .reference("1002")
                .build())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("traduce el resultado contable y lo aplica en los tres puertos")
    void aplicaElResultadoEnLosTresPuertos() {
        AccountingResult event = new AccountingResult(
                "evt-r1", "evt-src", "CREATE", "PAYMENT_VOUCHER", 42L, true, 99L, null,
                "tenant-a", "ENT-1");

        listener.accounting(event);

        ArgumentCaptor<TreasuryCommands.AccountingResult> captor =
                ArgumentCaptor.forClass(TreasuryCommands.AccountingResult.class);
        verify(vouchers).applyAccountingResult(captor.capture());
        verify(writeOffs).applyAccountingResult(captor.capture());
        verify(schedules).applyAccountingResult(captor.capture());
        TreasuryCommands.AccountingResult result = captor.getValue();
        assertThat(result.eventId()).isEqualTo("evt-r1");
        assertThat(result.sourceEventId()).isEqualTo("evt-src");
        assertThat(result.operation()).isEqualTo("CREATE");
        assertThat(result.documentType()).isEqualTo("PAYMENT_VOUCHER");
        assertThat(result.documentId()).isEqualTo(42L);
        assertThat(result.accepted()).isTrue();
        assertThat(result.accountingEntryId()).isEqualTo(99L);
        assertThat(result.reason()).isNull();
        assertThat(result.tenantId()).isEqualTo("tenant-a");
    }

    @Test
    @DisplayName("un fallo al aplicar el resultado se registra y no se propaga")
    void falloDeResultadoSeTraga() {
        doThrow(new IllegalStateException("db caida"))
                .when(vouchers).applyAccountingResult(any());

        assertThatCode(() -> listener.accounting(new AccountingResult(
                "evt-r2", "evt-src", "CREATE", "PAYMENT_VOUCHER", 7L, true, 8L, null,
                "tenant-a", "ENT-1"))).doesNotThrowAnyException();
        verifyNoInteractions(writeOffs, schedules);
    }
}

package co.unicauca.edu.co.contables.accounting.debt_payments.application.service;

import co.unicauca.edu.co.contables.accounting.debt_payments.application.input.IAccountingEventPublisher;
import co.unicauca.edu.co.contables.accounting.debt_payments.application.output.IInvoiceProviderPort;
import co.unicauca.edu.co.contables.accounting.debt_payments.application.output.IReceiptCommandPersistencePort;
import co.unicauca.edu.co.contables.accounting.debt_payments.application.output.IReceiptQueryPersistencePort;
import co.unicauca.edu.co.contables.accounting.debt_payments.application.output.IResourceUsageNotifierPort;
import co.unicauca.edu.co.contables.accounting.debt_payments.application.service.ReceiptService;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.enums.InvoiceStatus;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.exception.ReceiptNotFoundException;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.Receipt;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.ReceiptStatus;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.Replica.InvoiceReplica;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static co.unicauca.edu.co.contables.accounting.debt_payments.test.fixtures.TestFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit tests for ReceiptService")
public class ReceiptServiceUnitTest {

    @Mock
    private IReceiptCommandPersistencePort receiptCommandPersistencePort;
    @Mock
    private IReceiptQueryPersistencePort receiptQueryPersistencePort;
    @Mock
    private IInvoiceProviderPort invoiceProviderPort;
    @Mock
    private IAccountingEventPublisher accountingEventPublisher;
    @Mock
    private IResourceUsageNotifierPort resourceUsageNotifier;

    @InjectMocks
    private ReceiptService receiptService;

    @Test
    @DisplayName("createReceipt should process invoice payments and persist receipt")
    void shouldCreateReceiptAndProcessInvoices() {
        var detail = receiptDetail(1L, 100L);
        var receipt = receiptForInvoicePayment("ENT-1", 1L, 1L, "obs", List.of(detail));

        InvoiceReplica inv = invoiceReplicaWith(1L, 500L, 400L, InvoiceStatus.PENDING);

        when(invoiceProviderPort.findInvoiceById(1L)).thenReturn(Optional.of(inv));
        when(receiptCommandPersistencePort.save(any())).thenReturn(receipt);

        Receipt result = receiptService.createReceipt(receipt);

        assertThat(result).isEqualTo(receipt);
        verify(invoiceProviderPort).updateInvoice(inv);
        verify(accountingEventPublisher).publishReceiptCreatedEvent(receipt);
    }

    @Test
    @DisplayName("createReceipt wraps exceptions from invoice processing")
    void shouldWrapInvoiceProcessingExceptions() {
        var detail = receiptDetail(2L, 50L);
        var receipt = receiptForInvoicePayment("ENT-1", 1L, 1L, "obs", List.of(detail));

        when(invoiceProviderPort.findInvoiceById(2L)).thenThrow(new RuntimeException("remote error"));

        assertThatThrownBy(() -> receiptService.createReceipt(receipt)).isInstanceOf(IllegalStateException.class);
        verify(receiptCommandPersistencePort, never()).save(any());
        verify(accountingEventPublisher, never()).publishReceiptCreatedEvent(any());
    }

    @Test
    @DisplayName("voidReceipt should void and publish event when exists")
    void shouldVoidReceiptSuccessfully() {
        var detail = receiptDetail(3L, 50L);
        var receipt = receiptForInvoicePayment("ENT-1", 1L, 1L, "obs", List.of(detail));
        receipt.setId(10L);

        InvoiceReplica inv = invoiceReplicaWith(3L, 500L, 450L, InvoiceStatus.PAID);

        when(receiptQueryPersistencePort.findById(10L)).thenReturn(Optional.of(receipt));
        when(invoiceProviderPort.findInvoiceById(3L)).thenReturn(Optional.of(inv));
        when(receiptCommandPersistencePort.save(receipt)).thenReturn(receipt);

        Receipt result = receiptService.voidReceipt(10L, "customer request");

        assertThat(result.getStatus()).isEqualTo(ReceiptStatus.VOIDED);
        verify(invoiceProviderPort).updateInvoice(inv);
        verify(accountingEventPublisher).publishVoidReceiptEvent(receipt);
    }

    @Test
    @DisplayName("voidReceipt throws ReceiptNotFoundException when missing")
    void shouldThrowWhenVoidingMissingReceipt() {
        when(receiptQueryPersistencePort.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> receiptService.voidReceipt(999L, "r")).isInstanceOf(ReceiptNotFoundException.class);
        verify(accountingEventPublisher, never()).publishVoidReceiptEvent(any());
    }

    @Test
    @DisplayName("createReceipt should handle DIRECT_INCOME receipts and persist without invoice updates")
    void shouldCreateDirectIncomeReceiptAndPersist() {
        var receipt = receiptForDirectIncome("ENT-1", 2L, 2L, "direct", 1000L, 555L);

        when(receiptCommandPersistencePort.save(any())).thenReturn(receipt);

        Receipt result = receiptService.createReceipt(receipt);

        assertThat(result).isEqualTo(receipt);
        verify(receiptCommandPersistencePort).save(receipt);
        verify(accountingEventPublisher).publishReceiptCreatedEvent(receipt);
        verify(invoiceProviderPort, never()).updateInvoice(any());
    }

    @Test
    @DisplayName("createReceipt should propagate persistence exceptions and not publish event")
    void shouldPropagatePersistenceExceptionAndNotPublish() {
        var receipt = receiptForDirectIncome("ENT-1", 3L, 2L, "direct", 2000L, 777L);

        when(receiptCommandPersistencePort.save(any())).thenThrow(new RuntimeException("DB fail"));

        assertThatThrownBy(() -> receiptService.createReceipt(receipt)).isInstanceOf(RuntimeException.class);
        verify(accountingEventPublisher, never()).publishReceiptCreatedEvent(any());
    }

    @Test
    @DisplayName("createReceipt should notify resource usage")
    void shouldNotifyResourceUsageWhenCreatingReceipt() {
        var receipt = receiptForDirectIncome("ENT-1", 4L, 3L, "test", 500L, 100L);

        when(receiptCommandPersistencePort.save(any())).thenReturn(receipt);

        receiptService.createReceipt(receipt);

        verify(resourceUsageNotifier).notifyAll(any());
    }

    @Test
    @DisplayName("voidReceipt should throw IllegalStateException when receipt is already voided")
    void shouldThrowWhenVoidingAlreadyVoidedReceipt() {
        var receipt = receiptForDirectIncome("ENT-1", 5L, 4L, "already voided", 800L, 200L);
        receipt.setId(20L);
        receipt.setStatus(ReceiptStatus.VOIDED);

        when(receiptQueryPersistencePort.findById(20L)).thenReturn(Optional.of(receipt));

        assertThatThrownBy(() -> receiptService.voidReceipt(20L, "trying again"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already voided");

        verify(invoiceProviderPort, never()).updateInvoice(any());
        verify(receiptCommandPersistencePort, never()).save(any());
        verify(accountingEventPublisher, never()).publishVoidReceiptEvent(any());
    }

    @Test
    @DisplayName("voidReceipt should wrap exception when reversing invoice payments fails")
    void shouldWrapExceptionWhenReversingInvoicePaymentsFails() {
        var detail = receiptDetail(4L, 100L);
        var receipt = receiptForInvoicePayment("ENT-1", 6L, 5L, "obs", List.of(detail));
        receipt.setId(30L);

        when(receiptQueryPersistencePort.findById(30L)).thenReturn(Optional.of(receipt));
        when(invoiceProviderPort.findInvoiceById(4L)).thenThrow(new RuntimeException("invoice service down"));

        assertThatThrownBy(() -> receiptService.voidReceipt(30L, "reason"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Error reversing invoice payments");

        verify(receiptCommandPersistencePort, never()).save(any());
        verify(accountingEventPublisher, never()).publishVoidReceiptEvent(any());
    }

    @Test
    @DisplayName("findById should delegate to query persistence port")
    void shouldDelegateFindByIdToQueryPort() {
        var receipt = receiptForDirectIncome("ENT-1", 7L, 6L, "test", 1000L, 300L);
        receipt.setId(40L);

        when(receiptQueryPersistencePort.findById(40L)).thenReturn(Optional.of(receipt));

        Receipt result = receiptService.findById(40L);

        assertThat(result).isEqualTo(receipt);
        verify(receiptQueryPersistencePort).findById(40L);
    }

    @Test
    @DisplayName("findById should throw ReceiptNotFoundException when receipt not found")
    void shouldThrowWhenReceiptNotFound() {
        when(receiptQueryPersistencePort.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> receiptService.findById(999L))
                .isInstanceOf(ReceiptNotFoundException.class)
                .hasMessageContaining("no fue encontrado");
        verify(receiptQueryPersistencePort).findById(999L);
    }

    @Test
    @DisplayName("findByInvoiceId should delegate to query persistence port")
    void shouldDelegateFindByInvoiceIdToQueryPort() {
        List<Receipt> receipts = List.of(receiptForDirectIncome("ENT-1", 8L, 7L, "test", 500L, 100L));

        when(receiptQueryPersistencePort.findByInvoiceId("INV-123")).thenReturn(receipts);

        List<Receipt> result = receiptService.findByInvoiceId("INV-123");

        assertThat(result).hasSize(1).isEqualTo(receipts);
        verify(receiptQueryPersistencePort).findByInvoiceId("INV-123");
    }

    @Test
    @DisplayName("findByThirdPartyId should delegate to query persistence port")
    void shouldDelegateFindByThirdPartyIdToQueryPort() {
        List<Receipt> receipts = List.of(
                receiptForDirectIncome("ENT-1", 9L, 8L, "test1", 500L, 100L),
                receiptForDirectIncome("ENT-1", 9L, 8L, "test2", 600L, 150L)
        );

        when(receiptQueryPersistencePort.findByThirdPartyId("9", "ENT-1")).thenReturn(receipts);

        List<Receipt> result = receiptService.findByThirdPartyId("9", "ENT-1");

        assertThat(result).hasSize(2).isEqualTo(receipts);
        verify(receiptQueryPersistencePort).findByThirdPartyId("9", "ENT-1");
    }

    @Test
    @DisplayName("findByEnterpriseId should delegate to query persistence port")
    void shouldDelegateFindByEnterpriseIdToQueryPort() {
        List<Receipt> receipts = List.of(
                receiptForDirectIncome("ENT-99", 10L, 9L, "test1", 700L, 200L),
                receiptForDirectIncome("ENT-99", 11L, 10L, "test2", 800L, 250L),
                receiptForDirectIncome("ENT-99", 12L, 11L, "test3", 900L, 300L)
        );

        when(receiptQueryPersistencePort.findByEnterpriseId("ENT-99")).thenReturn(receipts);

        List<Receipt> result = receiptService.findByEnterpriseId("ENT-99");

        assertThat(result).hasSize(3).isEqualTo(receipts);
        verify(receiptQueryPersistencePort).findByEnterpriseId("ENT-99");
    }
}


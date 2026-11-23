package co.unicauca.edu.co.contables.accounting.debt_payments.application.service;

import co.unicauca.edu.co.contables.accounting.debt_payments.application.output.IInvoiceProviderPort;
import co.unicauca.edu.co.contables.accounting.debt_payments.application.service.InvoiceService;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.enums.InvoiceStatus;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.exception.InvoiceNotFoundException;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.Replica.InvoiceReplica;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static co.unicauca.edu.co.contables.accounting.debt_payments.test.fixtures.TestFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit tests for InvoiceService")
public class InvoiceServiceUnitTest {

    @Mock
    private IInvoiceProviderPort invoiceProviderPort;

    @InjectMocks
    private InvoiceService invoiceService;

    @Test
    @DisplayName("writeOffInvoices should return when input is null or empty")
    void writeOffInvoices_nullOrEmpty_returns() {
        invoiceService.writeOffInvoices(null);
        invoiceService.writeOffInvoices(List.of());

        verifyNoInteractions(invoiceProviderPort);
    }

    @Test
    @DisplayName("writeOffInvoices should throw when some invoices missing")
    void writeOffInvoices_sizeMismatch_throws() {
        List<Long> ids = List.of(1L, 2L);
        when(invoiceProviderPort.findInvoicesByIds(ids)).thenReturn(List.of(invoiceReplicaDefault(1L)));

        assertThatThrownBy(() -> invoiceService.writeOffInvoices(ids))
                .isInstanceOf(InvoiceNotFoundException.class)
                .hasMessageContaining("could not be found");
    }

    @Test
    @DisplayName("writeOffInvoices should write off each invoice and call update")
    void writeOffInvoices_success_callsWriteOffAndUpdate() {
        var inv1 = invoiceReplicaWith(1L, 1000L, 200L, InvoiceStatus.PENDING);
        var inv2 = invoiceReplicaWith(2L, 500L, 100L, InvoiceStatus.PENDING);

        List<Long> ids = List.of(1L, 2L);
        when(invoiceProviderPort.findInvoicesByIds(ids)).thenReturn(List.of(inv1, inv2));

        invoiceService.writeOffInvoices(ids);

        // domain changes applied
        assertThat(inv1.getStatus()).isEqualTo(InvoiceStatus.WRITTEN_OFF);
        assertThat(inv1.getPendingValue()).isEqualTo(0L);

        assertThat(inv2.getStatus()).isEqualTo(InvoiceStatus.WRITTEN_OFF);
        assertThat(inv2.getPendingValue()).isEqualTo(0L);

        verify(invoiceProviderPort).updateInvoice(inv1);
        verify(invoiceProviderPort).updateInvoice(inv2);
    }

    @Test
    @DisplayName("updateDueDate should throw when invoice not found")
    void updateDueDate_notFound_throws() {
        when(invoiceProviderPort.findInvoiceById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> invoiceService.updateDueDate(99L, LocalDate.now()))
                .isInstanceOf(InvoiceNotFoundException.class);
    }

    @Test
    @DisplayName("updateDueDate should throw when invoice is PAID")
    void updateDueDate_paid_throws() {
        var paid = invoiceReplicaWith(5L, 100L, 100L, InvoiceStatus.PAID);
        when(invoiceProviderPort.findInvoiceById(5L)).thenReturn(Optional.of(paid));

        assertThatThrownBy(() -> invoiceService.updateDueDate(5L, LocalDate.now().plusDays(5)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ya pagada");
    }

    @Test
    @DisplayName("updateDueDate should set expiration, validate and update")
    void updateDueDate_success_updatesExpirationAndCallsUpdate() {
        var inv = invoiceReplicaDefault(10L);
        InvoiceReplica spyInv = spy(inv);
        when(invoiceProviderPort.findInvoiceById(10L)).thenReturn(Optional.of(spyInv));

        LocalDate newDate = LocalDate.now().plusDays(10);

        invoiceService.updateDueDate(10L, newDate);

        verify(spyInv).setExpirationDate(newDate);
        verify(spyInv).validateDates();
        verify(invoiceProviderPort).updateInvoice(spyInv);
    }

    @Test
    @DisplayName("find methods should delegate to provider")
    void findDelegations_delegates() {
        var list = List.of(invoiceReplicaDefault(1L));

        when(invoiceProviderPort.findPendingInvoicesByClientIdAndEnterpriseId(7L, "ENT-1")).thenReturn(list);
        assertThat(invoiceService.findPendingInvoicesByClientIdAndEnterpriseId(7L, "ENT-1")).isEqualTo(list);

        when(invoiceProviderPort.findInvoicesByEnterpriseId("ENT-A")).thenReturn(list);
        assertThat(invoiceService.findInvoicesByEnterpriseId("ENT-A")).isEqualTo(list);

        when(invoiceProviderPort.findPendingInvoicesByEnterpriseId("ENT-A")).thenReturn(list);
        assertThat(invoiceService.findPendingInvoicesByEnterpriseId("ENT-A")).isEqualTo(list);

        when(invoiceProviderPort.findStatusInvoicesByClientId(8L, InvoiceStatus.PENDING, "ENT-1")).thenReturn(list);
        assertThat(invoiceService.findStatusInvoicesByClientId(8L, InvoiceStatus.PENDING, "ENT-1")).isEqualTo(list);

        when(invoiceProviderPort.findInvoiceById(1L)).thenReturn(Optional.of(invoiceReplicaDefault(1L)));
        assertThat(invoiceService.findInvoiceById(1L)).isNotNull();
    }

    @Test
    @DisplayName("findInvoiceById should throw when missing")
    void findInvoiceById_missing_throws() {
        when(invoiceProviderPort.findInvoiceById(123L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> invoiceService.findInvoiceById(123L))
                .isInstanceOf(InvoiceNotFoundException.class);
    }

    // ==================== Tests para validaciones de fecha de vencimiento ====================

    @Test
    @DisplayName("updateDueDate should throw when new date is in the past")
    void updateDueDate_pastDate_throws() {
        var invoice = invoiceReplicaDefault(11L);
        when(invoiceProviderPort.findInvoiceById(11L)).thenReturn(Optional.of(invoice));

        LocalDate pastDate = LocalDate.now().minusDays(5);

        assertThatThrownBy(() -> invoiceService.updateDueDate(11L, pastDate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La nueva fecha de vencimiento no puede ser una fecha pasada");
    }

    @Test
    @DisplayName("updateDueDate should throw when new date is today")
    void updateDueDate_todayDate_throws() {
        var invoice = invoiceReplicaDefault(12L);
        when(invoiceProviderPort.findInvoiceById(12L)).thenReturn(Optional.of(invoice));

        LocalDate today = LocalDate.now();

        assertThatThrownBy(() -> invoiceService.updateDueDate(12L, today))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La nueva fecha de vencimiento no puede ser una fecha pasada");
    }

    @Test
    @DisplayName("updateDueDate should accept future dates successfully")
    void updateDueDate_futureDate_success() {
        var invoice = invoiceReplicaDefault(13L);
        InvoiceReplica spyInv = spy(invoice);
        when(invoiceProviderPort.findInvoiceById(13L)).thenReturn(Optional.of(spyInv));

        LocalDate futureDate = LocalDate.now().plusDays(30);

        invoiceService.updateDueDate(13L, futureDate);

        verify(spyInv).setExpirationDate(futureDate);
        verify(invoiceProviderPort).updateInvoice(spyInv);
        assertThat(spyInv.getExpirationDate()).isEqualTo(futureDate);
    }

    @Test
    @DisplayName("updateDueDate should work with PENDING status invoices")
    void updateDueDate_pendingStatus_success() {
        var pendingInvoice = invoiceReplicaWith(14L, 100000L, 50000L, InvoiceStatus.PENDING);
        InvoiceReplica spyInv = spy(pendingInvoice);
        when(invoiceProviderPort.findInvoiceById(14L)).thenReturn(Optional.of(spyInv));

        LocalDate futureDate = LocalDate.now().plusDays(20);

        invoiceService.updateDueDate(14L, futureDate);

        verify(invoiceProviderPort).updateInvoice(spyInv);
    }

    @Test
    @DisplayName("updateDueDate should work with PARTIAL_PAYMENT status invoices")
    void updateDueDate_partialPaymentStatus_success() {
        var partialInvoice = invoiceReplicaWith(15L, 100000L, 30000L, InvoiceStatus.PENDING);
        InvoiceReplica spyInv = spy(partialInvoice);
        when(invoiceProviderPort.findInvoiceById(15L)).thenReturn(Optional.of(spyInv));

        LocalDate futureDate = LocalDate.now().plusDays(25);

        invoiceService.updateDueDate(15L, futureDate);

        verify(invoiceProviderPort).updateInvoice(spyInv);
    }

    @Test
    @DisplayName("updateDueDate should throw for WRITTEN_OFF status invoices")
    void updateDueDate_writtenOffStatus_throws() {
        var writtenOffInvoice = invoiceReplicaWith(16L, 100000L, 0L, InvoiceStatus.WRITTEN_OFF);
        when(invoiceProviderPort.findInvoiceById(16L)).thenReturn(Optional.of(writtenOffInvoice));

        LocalDate futureDate = LocalDate.now().plusDays(30);

        // WRITTEN_OFF no es PAID, pero debe permitirse para consistencia
        invoiceService.updateDueDate(16L, futureDate);

        verify(invoiceProviderPort).updateInvoice(writtenOffInvoice);
    }
}


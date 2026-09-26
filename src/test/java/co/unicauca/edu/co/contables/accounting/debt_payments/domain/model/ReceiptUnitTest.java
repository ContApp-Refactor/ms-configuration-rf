package co.unicauca.edu.co.contables.accounting.debt_payments.domain.model;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.enums.InvoiceStatus;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.enums.ReceiptType;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.exception.InvoiceNotFoundException;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.Receipt;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.ReceiptStatus;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.Replica.InvoiceReplica;
import co.unicauca.edu.co.contables.accounting.debt_payments.test.fixtures.TestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Unit tests for Receipt domain model")
public class ReceiptUnitTest {

    @Test
    @DisplayName("isInvoicePayment returns true when type is INVOICE_PAYMENT")
    void isInvoicePaymentTrue() {
        var r = TestFixtures.receiptForInvoicePayment("ENT-1", 1L, 1L, "obs", List.of(TestFixtures.receiptDetail(1L, 100L)));
        assertThat(r.isInvoicePayment()).isTrue();
    }

    @Test
    @DisplayName("processInvoicePayments applies payments and fills detail invoice info")
    void processInvoicePaymentsAppliesPayments() throws Exception {
        var detail = TestFixtures.receiptDetail(1L, 200L);
        var receipt = TestFixtures.receiptForInvoicePayment("ENT-1", 1L, 1L, "obs", List.of(detail));

        InvoiceReplica inv = TestFixtures.invoiceReplicaWith(1L, 1000L, 500L, InvoiceStatus.PENDING);

        var modified = receipt.processInvoicePayments(id -> Optional.of(inv));

        assertThat(modified).containsExactly(inv);
        assertThat(inv.getTotalPay()).isEqualTo( (1000L - 500L) + 200L );
        assertThat(detail.getInvoiceCode()).isEqualTo(inv.getFactCode());
        assertThat(detail.getAccountingAccount()).isEqualTo(inv.getAccountingAccount());
    }

    @Test
    @DisplayName("voidReceipt reverses payments and sets status to VOIDED")
    void voidReceiptReversesPayments() throws Exception {
        var detail = TestFixtures.receiptDetail(2L, 150L);
        var receipt = TestFixtures.receiptForInvoicePayment("ENT-1", 1L, 1L, "obs", List.of(detail));

        InvoiceReplica inv = TestFixtures.invoiceReplicaWith(2L, 1000L, 850L, InvoiceStatus.PAID);

        var modified = receipt.voidReceipt("reason", id -> Optional.of(inv));

        assertThat(receipt.getStatus()).isEqualTo(ReceiptStatus.VOIDED);
        assertThat(modified).containsExactly(inv);
        assertThat(inv.getTotalPay()).isEqualTo( (1000L - 850L) - 150L );
    }

    @Test
    @DisplayName("assignReceiptCode throws if already assigned")
    void assignReceiptCodeThrowsWhenAlreadyAssigned() {
        var r = TestFixtures.receiptForDirectIncome("ENT-1", 1L, 1L, "obs", 1000L, 200L);
        r.setReceiptCode("RC-1");

        assertThatThrownBy(() -> r.assignReceiptCode("RC-2")).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("assignReceiptCode assigns when not present")
    void assignReceiptCodeAssignsWhenNotPresent() {
        var r = TestFixtures.receiptForDirectIncome("ENT-1", 1L, 1L, "obs", 500L, 200L);
        r.setReceiptCode(null);

        r.assignReceiptCode("RC-NEW");
        assertThat(r.getReceiptCode()).isEqualTo("RC-NEW");
    }

    @Test
    @DisplayName("processInvoicePayments returns empty for non-invoice receipts")
    void processInvoicePaymentsReturnsEmptyForNonInvoice() throws Exception {
        var r = TestFixtures.receiptForDirectIncome("ENT-1", 1L, 1L, "obs", 500L, 200L);

        var res = r.processInvoicePayments(id -> Optional.empty());
        assertThat(res).isEmpty();
    }

    @Test
    @DisplayName("processInvoicePayments throws when invoice not found")
    void processInvoicePaymentsThrowsWhenInvoiceNotFound() {
        var detail = TestFixtures.receiptDetail(99L, 100L);
        var receipt = TestFixtures.receiptForInvoicePayment("ENT-1", 1L, 1L, "obs", List.of(detail));

        org.junit.jupiter.api.Assertions.assertThrows(InvoiceNotFoundException.class,
                () -> receipt.processInvoicePayments(id -> Optional.empty()));
    }

    @Test
    @DisplayName("voidReceipt throws when already voided or reason blank")
    void voidReceiptValidations() throws Exception {
        var detail = TestFixtures.receiptDetail(2L, 150L);
        var receipt = TestFixtures.receiptForInvoicePayment("ENT-1", 1L, 1L, "obs", List.of(detail));

        // already voided
        receipt.setStatus(ReceiptStatus.VOIDED);
        assertThatThrownBy(() -> receipt.voidReceipt("reason", id -> Optional.empty())).isInstanceOf(IllegalStateException.class);

        // reason blank
        receipt.setStatus(ReceiptStatus.FINALIZED);
        assertThatThrownBy(() -> receipt.voidReceipt("  ", id -> Optional.empty())).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("getUsageNotifications returns third party and payment method notifications for invoice payment")
    void getUsageNotificationsForInvoicePayment() {
        var detail = TestFixtures.receiptDetail(1L, 100L);
        var receipt = TestFixtures.receiptForInvoicePayment("ENT-1", 5L, 10L, "obs", List.of(detail));

        var notifications = receipt.getUsageNotifications();

        assertThat(notifications).hasSize(2);
        assertThat(notifications).anySatisfy(n -> {
            assertThat(n.getClass().getSimpleName()).isEqualTo("ThirdPartyUsedNotification");
        });
        assertThat(notifications).anySatisfy(n -> {
            assertThat(n.getClass().getSimpleName()).isEqualTo("PaymentMethodUsedNotification");
        });
    }

    @Test
    @DisplayName("getUsageNotifications includes cost center for direct income with cost center")
    void getUsageNotificationsIncludesCostCenterForDirectIncome() {
        var receipt = TestFixtures.receiptForDirectIncome("ENT-1", 5L, 10L, "obs", 1000L, 200L);
        receipt.setCenterCostId(50L);

        var notifications = receipt.getUsageNotifications();

        assertThat(notifications).hasSize(3);
        assertThat(notifications).anySatisfy(n -> {
            assertThat(n.getClass().getSimpleName()).isEqualTo("CostCenterUsedNotification");
        });
    }

    @Test
    @DisplayName("processInvoicePayments throws IllegalStateException when invoice belongs to different third party")
    void processInvoicePaymentsThrowsWhenInvoiceBelongsToDifferentThirdParty() {
        // Arrange: Receipt for thirdPartyId = 5L
        var detail = TestFixtures.receiptDetail(1L, 200L);
        var receipt = TestFixtures.receiptForInvoicePayment("ENT-1", 5L, 1L, "obs", List.of(detail));

        // Invoice belongs to thirdPartyId = 1L (different from receipt)
        InvoiceReplica inv = TestFixtures.invoiceReplicaWith(1L, 1000L, 500L, InvoiceStatus.PENDING);

        // Act & Assert
        assertThatThrownBy(() -> receipt.processInvoicePayments(id -> Optional.of(inv)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Error de inconsistencia")
                .hasMessageContaining("La factura con código")
                .hasMessageContaining("pertenece al tercero con ID 1")
                .hasMessageContaining("pero el recibo se está creando para el tercero con ID 5");
    }

    @Test
    @DisplayName("processInvoicePayments succeeds when invoice belongs to same third party as receipt")
    void processInvoicePaymentsSucceedsWhenInvoiceBelongsToSameThirdParty() throws Exception {
        // Arrange: Receipt and Invoice both for thirdPartyId = 10L
        var detail = TestFixtures.receiptDetail(1L, 300L);
        var receipt = TestFixtures.receiptForInvoicePayment("ENT-1", 10L, 1L, "obs", List.of(detail));

        // Create invoice with matching thirdId = 10L
        InvoiceReplica inv = new InvoiceReplica(
                1L,
                "F-1",
                1000L,
                200L,
                10L, // thirdId matches receipt
                800L,
                1000L,
                java.time.LocalDate.now().minusDays(30),
                java.time.LocalDate.now().plusDays(30),
                "ENT-1",
                InvoiceStatus.PENDING,
                true
        );

        // Act
        var modified = receipt.processInvoicePayments(id -> Optional.of(inv));

        // Assert
        assertThat(modified).containsExactly(inv);
        assertThat(inv.getTotalPay()).isEqualTo(500L); // 200 + 300
        assertThat(detail.getInvoiceCode()).isEqualTo(inv.getFactCode());
        assertThat(detail.getAccountingAccount()).isEqualTo(inv.getAccountingAccount());
    }

    // ==================== Tests para nueva validación createForInvoicePayment ====================

    @Test
    @DisplayName("createForInvoicePayment throws when details is null")
    void createForInvoicePaymentThrowsWhenDetailsNull() {
        assertThatThrownBy(() -> Receipt.createForInvoicePayment(
                "ENT-1", 1L, 1L, "obs", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invoice payment receipt must have at least one detail");
    }

    @Test
    @DisplayName("createForInvoicePayment throws when details is empty")
    void createForInvoicePaymentThrowsWhenDetailsEmpty() {
        assertThatThrownBy(() -> Receipt.createForInvoicePayment(
                "ENT-1", 1L, 1L, "obs", List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invoice payment receipt must have at least one detail");
    }

    @Test
    @DisplayName("createForInvoicePayment succeeds with single detail")
    void createForInvoicePaymentSucceedsWithSingleDetail() {
        var detail = TestFixtures.receiptDetail(1L, 100L);
        var receipt = Receipt.createForInvoicePayment("ENT-1", 5L, 10L, "obs", List.of(detail));

        assertThat(receipt).isNotNull();
        assertThat(receipt.getReceiptType()).isEqualTo(ReceiptType.INVOICE_PAYMENT);
        assertThat(receipt.getEnterpriseId()).isEqualTo("ENT-1");
        assertThat(receipt.getThirdPartyId()).isEqualTo(5L);
        assertThat(receipt.getPaymentMethodId()).isEqualTo(10L);
        assertThat(receipt.getObservations()).isEqualTo("obs");
        assertThat(receipt.getDetails()).hasSize(1);
        assertThat(receipt.getStatus()).isEqualTo(ReceiptStatus.FINALIZED);
        assertThat(receipt.getIssueDate()).isEqualTo(java.time.LocalDate.now());
    }

    @Test
    @DisplayName("createForInvoicePayment succeeds with multiple details")
    void createForInvoicePaymentSucceedsWithMultipleDetails() {
        var details = List.of(
                TestFixtures.receiptDetail(1L, 100L),
                TestFixtures.receiptDetail(2L, 200L),
                TestFixtures.receiptDetail(3L, 150L)
        );
        var receipt = Receipt.createForInvoicePayment("ENT-2", 8L, 5L, "multi", details);

        assertThat(receipt.getDetails()).hasSize(3);
        assertThat(receipt.getReceiptType()).isEqualTo(ReceiptType.INVOICE_PAYMENT);
    }

    @Test
    @DisplayName("processInvoicePayments throws IllegalStateException when details null")
    void processInvoicePaymentsThrowsWhenDetailsNull() {
        var receipt = new Receipt();
        receipt.setReceiptType(ReceiptType.INVOICE_PAYMENT);
        receipt.setDetails(null);

        assertThatThrownBy(() -> receipt.processInvoicePayments(id -> Optional.empty()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invoice payment receipt must have at least one detail");
    }

    @Test
    @DisplayName("processInvoicePayments throws IllegalStateException when details empty")
    void processInvoicePaymentsThrowsWhenDetailsEmpty() {
        var receipt = new Receipt();
        receipt.setReceiptType(ReceiptType.INVOICE_PAYMENT);
        receipt.setDetails(List.of());

        assertThatThrownBy(() -> receipt.processInvoicePayments(id -> Optional.empty()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invoice payment receipt must have at least one detail");
    }
}


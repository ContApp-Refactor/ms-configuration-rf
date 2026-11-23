package co.unicauca.edu.co.contables.accounting.debt_payments.domain.model;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.exception.InvoiceNotFoundException;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.Receipt;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.Replica.InvoiceReplica;
import co.unicauca.edu.co.contables.accounting.debt_payments.test.fixtures.TestFixtures;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ReceiptSadPathsUnitTest {

    @Test
    void createForInvoicePayment_emptyDetails_throws() {
        assertThatThrownBy(() -> Receipt.createForInvoicePayment("ENT", 1L, 2L, "obs", null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> Receipt.createForInvoicePayment("ENT", 1L, 2L, "obs", java.util.List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createForDirectIncome_invalidArguments_throw() {
        assertThatThrownBy(() -> Receipt.createForDirectIncome("ENT", 1L, 2L, "obs", 0L, 1L))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> Receipt.createForDirectIncome("ENT", 1L, 2L, "obs", 100L, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void assignReceiptCode_alreadyAssigned_throws() {
        var r = TestFixtures.receiptForDirectIncome("ENT-1", 1L, 2L, "obs", 1000L, 5000L);
        r.setReceiptCode("RC-1");
        assertThatThrownBy(() -> r.assignReceiptCode("NEW"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void processInvoicePayments_invoiceNotFound_throws() {
        var detail = TestFixtures.receiptDetail(99L, 100L);
        var r = TestFixtures.receiptForInvoicePayment("ENT-1", 1L, 2L, "obs", java.util.List.of(detail));

        Function<Long, Optional<InvoiceReplica>> finder = id -> Optional.empty();

        assertThatThrownBy(() -> r.processInvoicePayments(finder))
                .isInstanceOf(InvoiceNotFoundException.class);
    }

    @Test
    void voidReceipt_nullReason_throws() {
        var detail = TestFixtures.receiptDetail(1L, 100L);
        var r = TestFixtures.receiptForInvoicePayment("ENT-1", 1L, 2L, "obs", java.util.List.of(detail));

        Function<Long, Optional<InvoiceReplica>> finder = id -> Optional.of(TestFixtures.invoiceReplicaDefault(id));

        assertThatThrownBy(() -> r.voidReceipt(null, finder)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> r.voidReceipt("  ", finder)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void voidReceipt_alreadyVoided_throws() throws Exception {
        var detail = TestFixtures.receiptDetail(1L, 100L);
        var r = TestFixtures.receiptForInvoicePayment("ENT-1", 1L, 2L, "obs", java.util.List.of(detail));

        Function<Long, Optional<InvoiceReplica>> finder = id -> Optional.of(TestFixtures.invoiceReplicaDefault(id));

        r.voidReceipt("reason", finder);

        assertThatThrownBy(() -> r.voidReceipt("another", finder)).isInstanceOf(IllegalStateException.class);
    }
}


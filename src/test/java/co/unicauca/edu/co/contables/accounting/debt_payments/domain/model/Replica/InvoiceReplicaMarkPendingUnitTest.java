package co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.Replica;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.enums.InvoiceStatus;
import co.unicauca.edu.co.contables.accounting.debt_payments.test.fixtures.TestFixtures;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class InvoiceReplicaMarkPendingUnitTest {

    @Test
    void markAsPendingWriteOff_setsStatusPendingWrittenOff() {
        var invoice = TestFixtures.invoiceReplicaDefault(10L);
        invoice.markAsPendingWriteOff();
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PENDING_WRITTEN_OFF);
    }
}


package co.unicauca.edu.co.contables.billing.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import co.unicauca.edu.co.contables.billing.application.ports.input.command.InvoiceDraftCommand;
import co.unicauca.edu.co.contables.billing.application.ports.input.command.InvoiceDraftCommand.WithholdingCommand;
import co.unicauca.edu.co.contables.billing.application.ports.input.command.InvoiceLineCommand;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentType;
import co.unicauca.edu.co.contables.billing.domain.model.WithholdingCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

/** La huella de idempotencia distingue las retenciones pero no cambia para peticiones sin ellas. */
class BillingRequestFingerprintWithholdingTest {

    private static final DocumentType SALE = DocumentType.SALE_INVOICE;

    private static InvoiceDraftCommand with(List<WithholdingCommand> withholdings) {
        return new InvoiceDraftCommand(10L, LocalDate.of(2026, 1, 15), null,
                List.of(new InvoiceLineCommand(1L, new BigDecimal("2"), null, null)), null, null, withholdings);
    }

    @Test
    void no_withholdings_null_or_empty_keep_the_pre_existing_fingerprint() {
        InvoiceDraftCommand legacy = new InvoiceDraftCommand(10L, LocalDate.of(2026, 1, 15), null,
                List.of(new InvoiceLineCommand(1L, new BigDecimal("2"), null, null)), null);

        assertThat(BillingRequestFingerprint.of(SALE, with(null))).isEqualTo(BillingRequestFingerprint.of(SALE, legacy));
        assertThat(BillingRequestFingerprint.of(SALE, with(List.of())))
                .isEqualTo(BillingRequestFingerprint.of(SALE, legacy));
    }

    @Test
    void the_same_withholdings_give_the_same_fingerprint() {
        List<WithholdingCommand> a = List.of(new WithholdingCommand(WithholdingCode.RETE_ICA, 3L));
        List<WithholdingCommand> b = List.of(new WithholdingCommand(WithholdingCode.RETE_ICA, 3L));

        assertThat(BillingRequestFingerprint.of(SALE, with(a))).isEqualTo(BillingRequestFingerprint.of(SALE, with(b)));
    }

    @Test
    void a_different_code_tax_or_extra_withholding_changes_the_fingerprint() {
        String base = BillingRequestFingerprint.of(SALE,
                with(List.of(new WithholdingCommand(WithholdingCode.RETE_ICA, 3L))));

        assertThat(BillingRequestFingerprint.of(SALE,
                with(List.of(new WithholdingCommand(WithholdingCode.RETE_FUENTE, 3L))))).isNotEqualTo(base);
        assertThat(BillingRequestFingerprint.of(SALE,
                with(List.of(new WithholdingCommand(WithholdingCode.RETE_ICA, 4L))))).isNotEqualTo(base);
        assertThat(BillingRequestFingerprint.of(SALE, with(List.of(new WithholdingCommand(WithholdingCode.RETE_ICA, 3L),
                new WithholdingCommand(WithholdingCode.RETE_IVA, 3L))))).isNotEqualTo(base);
        assertThat(BillingRequestFingerprint.of(SALE, with(null))).isNotEqualTo(base);
    }
}

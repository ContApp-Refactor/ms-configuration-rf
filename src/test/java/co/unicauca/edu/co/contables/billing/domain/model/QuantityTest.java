package co.unicauca.edu.co.contables.billing.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.unicauca.edu.co.contables.billing.domain.exception.BillingErrorCode;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/** DOC-2 / DOC-7: cantidad &gt; 0 con escala &lt;= 3. */
class QuantityTest {

    @Test
    void DOC_2_normalizes_to_scale_three() {
        assertThat(Quantity.of(new BigDecimal("3")).value().toPlainString()).isEqualTo("3.000");
        assertThat(Quantity.of(new BigDecimal("0.5")).value().toPlainString()).isEqualTo("0.500");
        assertThat(Quantity.of(new BigDecimal("1.250")).value().toPlainString()).isEqualTo("1.250");
    }

    @Test
    void DOC_7_rejects_zero_negative_and_scale_over_three() {
        assertInvalid(BigDecimal.ZERO);
        assertInvalid(new BigDecimal("-1"));
        assertInvalid(new BigDecimal("0.0001"));
        assertInvalid(new BigDecimal("1.2345"));
    }

    @Test
    void DOC_7_smallest_positive_quantity_is_accepted() {
        assertThat(Quantity.of(new BigDecimal("0.001")).value().toPlainString()).isEqualTo("0.001");
    }

    @Test
    void DOC_7_null_is_invalid_line() {
        assertThatThrownBy(() -> Quantity.of(null))
                .isInstanceOf(BillingException.class)
                .extracting(e -> ((BillingException) e).errorCode())
                .isEqualTo(BillingErrorCode.BILLING_INVALID_LINE);
    }

    @Test
    void DOC_2_equality_ignores_input_scale() {
        assertThat(Quantity.of(new BigDecimal("2"))).isEqualTo(Quantity.of(new BigDecimal("2.000")));
    }

    private static void assertInvalid(BigDecimal value) {
        assertThatThrownBy(() -> Quantity.of(value))
                .isInstanceOf(BillingException.class)
                .extracting(e -> ((BillingException) e).errorCode())
                .isEqualTo(BillingErrorCode.BILLING_INVALID_LINE);
    }
}

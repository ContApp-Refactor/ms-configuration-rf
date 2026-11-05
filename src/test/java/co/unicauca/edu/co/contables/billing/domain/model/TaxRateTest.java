package co.unicauca.edu.co.contables.billing.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.unicauca.edu.co.contables.billing.domain.exception.BillingErrorCode;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/** DOC-5 / DOC-7: tasa de IVA 0..100, escala <= 2. */
class TaxRateTest {

    @Test
    void DOC_7_accepts_range_boundaries_and_normalizes_scale() {
        assertThat(TaxRate.of(new BigDecimal("0")).value().toPlainString()).isEqualTo("0.00");
        assertThat(TaxRate.of(new BigDecimal("100")).value().toPlainString()).isEqualTo("100.00");
        assertThat(TaxRate.of(new BigDecimal("19")).value().toPlainString()).isEqualTo("19.00");
        assertThat(TaxRate.of(new BigDecimal("5.5")).value().toPlainString()).isEqualTo("5.50");
    }

    @Test
    void DOC_7_rejects_negative_over_100_and_scale_over_two() {
        assertInvalid(new BigDecimal("-0.01"));
        assertInvalid(new BigDecimal("100.01"));
        assertInvalid(new BigDecimal("19.001"));
    }

    @Test
    void DOC_7_trailing_zeros_beyond_scale_two_are_accepted() {
        assertThat(TaxRate.of(new BigDecimal("19.000")).value().toPlainString()).isEqualTo("19.00");
    }

    @Test
    void DOC_7_null_is_invalid_line() {
        assertThatThrownBy(() -> TaxRate.of(null))
                .isInstanceOf(BillingException.class)
                .extracting(e -> ((BillingException) e).errorCode())
                .isEqualTo(BillingErrorCode.BILLING_INVALID_LINE);
    }

    @Test
    void DOC_5_applyTo_computes_rounded_tax() {
        // 9.99 * 19% = 1.8981 -> 1.90
        assertThat(TaxRate.of(new BigDecimal("19")).applyTo(Money.ofCents(999L))).isEqualTo(Money.ofCents(190L));
        // 0.05 * 10% = 0.005 -> 0.01 (HALF_UP)
        assertThat(TaxRate.of(new BigDecimal("10")).applyTo(Money.ofCents(5L))).isEqualTo(Money.ofCents(1L));
        // 0.04 * 10% = 0.004 -> 0.00
        assertThat(TaxRate.of(new BigDecimal("10")).applyTo(Money.ofCents(4L))).isEqualTo(Money.zero());
    }

    @Test
    void DOC_5_zero_rate_yields_zero_tax() {
        assertThat(TaxRate.zero().applyTo(Money.ofCents(123456L))).isEqualTo(Money.zero());
    }

    @Test
    void DOC_5_equality_ignores_input_scale() {
        assertThat(TaxRate.of(new BigDecimal("19"))).isEqualTo(TaxRate.of(new BigDecimal("19.00")));
    }

    private static void assertInvalid(BigDecimal value) {
        assertThatThrownBy(() -> TaxRate.of(value))
                .isInstanceOf(BillingException.class)
                .extracting(e -> ((BillingException) e).errorCode())
                .isEqualTo(BillingErrorCode.BILLING_INVALID_LINE);
    }
}

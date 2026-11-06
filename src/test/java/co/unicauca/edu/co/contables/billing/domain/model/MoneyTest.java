package co.unicauca.edu.co.contables.billing.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/** DOC-5 / DOC-6: aritmetica monetaria exacta, escala 2, HALF_UP. */
class MoneyTest {

    @Test
    void DOC_5_of_normalizes_to_scale_two() {
        assertThat(Money.of(new BigDecimal("10")).value()).isEqualByComparingTo("10.00");
        assertThat(Money.of(new BigDecimal("10")).value().scale()).isEqualTo(2);
        assertThat(Money.of(new BigDecimal("12.3")).value().toPlainString()).isEqualTo("12.30");
    }

    @Test
    void DOC_5_half_up_boundary_0_005_rounds_up() {
        assertThat(Money.of(new BigDecimal("0.005")).value().toPlainString()).isEqualTo("0.01");
        assertThat(Money.of(new BigDecimal("0.004")).value().toPlainString()).isEqualTo("0.00");
        assertThat(Money.of(new BigDecimal("1.994")).value().toPlainString()).isEqualTo("1.99");
        assertThat(Money.of(new BigDecimal("1.995")).value().toPlainString()).isEqualTo("2.00");
    }

    @Test
    void DOC_6_cents_round_trip_is_exact() {
        Money money = Money.ofCents(1234L);

        assertThat(money.value().toPlainString()).isEqualTo("12.34");
        assertThat(money.toCents()).isEqualTo(1234L);
        assertThat(Money.ofCents(0L).toCents()).isZero();
        assertThat(Money.ofCents(100_000_000_007L).toCents()).isEqualTo(100_000_000_007L);
    }

    @Test
    void DOC_6_cost_double_converts_with_half_up_to_scale_two() {
        assertThat(Money.of(BigDecimal.valueOf(12.345d)).value().toPlainString()).isEqualTo("12.35");
        assertThat(Money.of(BigDecimal.valueOf(0.1d + 0.2d)).value().toPlainString()).isEqualTo("0.30");
    }

    @Test
    void DOC_5_zero_plus_and_is_positive() {
        assertThat(Money.zero().value().toPlainString()).isEqualTo("0.00");
        assertThat(Money.zero().isPositive()).isFalse();
        assertThat(Money.ofCents(1L).isPositive()).isTrue();
        assertThat(Money.ofCents(150L).plus(Money.ofCents(275L))).isEqualTo(Money.ofCents(425L));
    }

    @Test
    void DOC_5_times_quantity_rounds_half_up() {
        // 3 x 3.33 = 9.99 exacto
        assertThat(Money.ofCents(333L).times(Quantity.of(new BigDecimal("3")))).isEqualTo(Money.ofCents(999L));
        // 0.5 x 0.01 = 0.005 -> 0.01
        assertThat(Money.ofCents(1L).times(Quantity.of(new BigDecimal("0.5")))).isEqualTo(Money.ofCents(1L));
        // 0.333 x 1.00 = 0.333 -> 0.33
        assertThat(Money.ofCents(100L).times(Quantity.of(new BigDecimal("0.333")))).isEqualTo(Money.ofCents(33L));
    }

    @Test
    void DOC_5_equality_ignores_input_scale() {
        assertThat(Money.of(new BigDecimal("5"))).isEqualTo(Money.of(new BigDecimal("5.00")));
        assertThat(Money.of(new BigDecimal("5")).hashCode()).isEqualTo(Money.ofCents(500L).hashCode());
    }

    @Test
    void DOC_5_null_amount_is_rejected() {
        assertThatThrownBy(() -> Money.of(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void DOC_6_toCents_overflow_is_not_silent() {
        Money huge = Money.of(new BigDecimal("999999999999999999999.99"));

        assertThatThrownBy(huge::toCents).isInstanceOf(ArithmeticException.class);
    }
}

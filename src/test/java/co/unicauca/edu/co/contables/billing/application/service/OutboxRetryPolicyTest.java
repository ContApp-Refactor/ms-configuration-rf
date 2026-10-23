package co.unicauca.edu.co.contables.billing.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Politica de reintentos del relay (EVT-4, RNF-05): intento n (ya incrementado) >= maximo -> FAILED
 * definitivo; si no, proximo intento = ahora + min(5s * 2^(n-1), 10 min).
 */
class OutboxRetryPolicyTest {

    private static final Instant NOW = Instant.parse("2026-10-22T15:00:00Z");

    private final OutboxRetryPolicy policy = new OutboxRetryPolicy(10, Duration.ofSeconds(5), Duration.ofMinutes(10));

    @ParameterizedTest(name = "intento {0} -> espera {1} s")
    @CsvSource({"1,5", "2,10", "3,20", "4,40", "5,80", "6,160", "7,320", "8,600", "9,600"})
    void EVT_4_backoff_doubles_from_five_seconds_and_is_capped_at_ten_minutes(int attempts, long expectedSeconds) {
        OutboxRetryPolicy.Decision decision = policy.decide(attempts, NOW);

        assertThat(decision.exhausted()).isFalse();
        assertThat(decision.nextAttemptAt()).isEqualTo(NOW.plusSeconds(expectedSeconds));
    }

    @Test
    void EVT_4_tenth_attempt_is_definitive_failure() {
        OutboxRetryPolicy.Decision decision = policy.decide(10, NOW);

        assertThat(decision.exhausted()).isTrue();
        assertThat(decision.nextAttemptAt()).isEqualTo(NOW);
    }

    @Test
    void EVT_4_attempts_beyond_the_maximum_are_also_exhausted() {
        assertThat(policy.decide(11, NOW).exhausted()).isTrue();
        assertThat(policy.decide(500, NOW).exhausted()).isTrue();
    }

    @Test
    void EVT_4_ninth_attempt_still_retries() {
        assertThat(policy.decide(9, NOW).exhausted()).isFalse();
    }

    @Test
    void EVT_4_huge_maximum_does_not_overflow_the_backoff() {
        OutboxRetryPolicy generous = new OutboxRetryPolicy(10_000, Duration.ofSeconds(5), Duration.ofMinutes(10));

        OutboxRetryPolicy.Decision decision = generous.decide(5_000, NOW);

        assertThat(decision.exhausted()).isFalse();
        assertThat(decision.nextAttemptAt()).isEqualTo(NOW.plus(Duration.ofMinutes(10)));
    }

    @Test
    void EVT_4_configured_base_and_maximum_are_respected() {
        OutboxRetryPolicy custom = new OutboxRetryPolicy(3, Duration.ofSeconds(1), Duration.ofSeconds(3));

        assertThat(custom.decide(1, NOW).nextAttemptAt()).isEqualTo(NOW.plusSeconds(1));
        assertThat(custom.decide(2, NOW).nextAttemptAt()).isEqualTo(NOW.plusSeconds(2));
        assertThat(custom.decide(3, NOW).exhausted()).isTrue();
    }

    @Test
    void EVT_4_attempt_number_must_be_positive() {
        assertThatThrownBy(() -> policy.decide(0, NOW)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> policy.decide(-1, NOW)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void EVT_4_invalid_configuration_is_rejected() {
        assertThatThrownBy(() -> new OutboxRetryPolicy(0, Duration.ofSeconds(5), Duration.ofMinutes(10)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new OutboxRetryPolicy(10, Duration.ZERO, Duration.ofMinutes(10)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new OutboxRetryPolicy(10, Duration.ofSeconds(5), Duration.ofSeconds(1)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

package co.unicauca.edu.co.contables.auxiliary.unit.infrastructure;

import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.adapter.ScheduledReport.ClockAdapter;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

/**
 * @brief Pruebas unitarias para {@link ClockAdapter}.
 */
class ClockAdapterTest {

    private final ClockAdapter clockAdapter = new ClockAdapter();

    @Test
    @DisplayName("now() debe retornar un Instant no nulo cercano a Instant.now()")
    void nowReturnsCurrentInstant() {
        Instant before = Instant.now();
        Instant result = clockAdapter.now();
        Instant after = Instant.now();

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(Duration.between(before, after).abs())
                .isGreaterThanOrEqualTo(Duration.ZERO);
        Assertions.assertThat(result).isBetween(before.minusSeconds(1), after.plusSeconds(1));
    }

    @Test
    @DisplayName("llamadas sucesivas a now() deben ser monótonas no-decrecientes")
    void nowIsMonotonic() {
        Instant first = clockAdapter.now();
        Instant second = clockAdapter.now();

        Assertions.assertThat(second).isAfterOrEqualTo(first);
    }
}

package co.unicauca.edu.co.contables.auxiliary.domain.models.scheduling;

import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EFrequency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Optional;

/**
 * @brief Especificación temporal de un reporte programado.
 *
 * Define la frecuencia ({@link EFrequency}), el instante de inicio,
 * el instante de fin opcional y la próxima ejecución ({@code nextRunAt})
 * que el scheduler utiliza para disparar el job.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleSpec {
    private EFrequency frequency;
    private Instant startAt;
    private Optional<Instant> endAt;
    private Instant nextRunAt;
}

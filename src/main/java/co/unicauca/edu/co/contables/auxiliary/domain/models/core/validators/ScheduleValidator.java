package co.unicauca.edu.co.contables.auxiliary.domain.models.core.validators;

import jdk.jfr.Frequency;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @brief Validador de dominio para la configuración de frecuencia del schedule.
 *
 * Define el conjunto de frecuencias soportadas sobre las que puede
 * programarse un reporte, permitiendo validar especificaciones
 * ({@code ScheduleSpec}) contra valores admitidos.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleValidator {
    private Set<Frequency> supportedFrequencies;
}

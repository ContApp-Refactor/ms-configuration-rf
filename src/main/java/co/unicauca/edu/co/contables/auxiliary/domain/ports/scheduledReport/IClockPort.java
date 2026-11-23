package co.unicauca.edu.co.contables.auxiliary.domain.ports.scheduledReport;

import java.time.Instant;

/**
 * @brief Puerto de salida para acceder al reloj del sistema.
 *
 * Abstracción sobre el reloj que permite obtener el instante actual
 * de forma desacoplada, facilitando pruebas deterministas.
 */
public interface IClockPort {
    Instant now();
}

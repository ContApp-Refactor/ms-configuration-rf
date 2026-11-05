package co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.adapter.ScheduledReport;

import co.unicauca.edu.co.contables.auxiliary.domain.ports.scheduledReport.IClockPort;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;

/**
 * @brief Adaptador de reloj del sistema utilizado por el planificador.
 *
 * Implementa {@link IClockPort} devolviendo el instante actual a partir
 * de un {@link Clock} con la zona horaria por defecto del sistema, lo
 * que permite sustituir el reloj en pruebas y desacoplar al dominio de
 * {@code Instant.now()}.
 */
@Component
@Slf4j
public class ClockAdapter implements IClockPort {

    private final Clock clock = Clock.systemDefaultZone();

    /**
     * @brief Registra en el log la zona horaria del reloj al arrancar el bean.
     */
    @PostConstruct
    void logClockConfiguration() {
        log.info("[ClockAdapter] Initialized clock with zone={}", clock.getZone());
    }

    /**
     * @brief Obtiene el instante actual según el reloj configurado.
     * @return Instante actual {@link Instant}.
     */
    @Override
    public Instant now() {
        return Instant.now(clock);
    }
}

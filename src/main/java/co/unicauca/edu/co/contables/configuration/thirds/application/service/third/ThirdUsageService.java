package co.unicauca.edu.co.contables.configuration.thirds.application.service.third;

import co.unicauca.edu.co.contables.configuration.thirds.application.ports.input.IThirdUsagePort;
import co.unicauca.edu.co.contables.configuration.thirds.application.ports.output.ThirdOutputPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @brief Servicio para manejo del contador de uso de terceros
 * 
 * Implementa el caso de uso de incremento de contador de uso.
 * Mantiene la separación arquitectónica entre adaptadores de entrada (listeners)
 * y adaptadores de salida (persistencia).
 */
@Service
@RequiredArgsConstructor
public class ThirdUsageService implements IThirdUsagePort {

    private final ThirdOutputPort thirdOutputPort;

    @Override
    public void incrementUsageCount(Long thirdId) {
        thirdOutputPort.incrementUsageCount(thirdId);
    }
}

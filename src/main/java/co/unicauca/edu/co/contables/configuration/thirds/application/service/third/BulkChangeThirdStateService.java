package co.unicauca.edu.co.contables.configuration.thirds.application.service.third;

import co.unicauca.edu.co.contables.configuration.thirds.application.ports.input.BulkChangeThirdStateUseCase;
import co.unicauca.edu.co.contables.configuration.thirds.application.ports.output.ThirdOutputPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @brief Servicio para cambiar el estado de múltiples terceros de forma masiva
 *
 * Implementa operaciones transaccionales para garantizar consistencia.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BulkChangeThirdStateService implements BulkChangeThirdStateUseCase {

    private final ThirdOutputPort thirdOutputPort;

    @Override
    @Transactional
    public int changeAllThirdsState(String entId, Boolean newState) {
        return thirdOutputPort.bulkUpdateThirdState(entId, newState);
    }
}

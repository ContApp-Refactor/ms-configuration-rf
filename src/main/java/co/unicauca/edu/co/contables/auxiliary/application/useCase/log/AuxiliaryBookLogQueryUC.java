package co.unicauca.edu.co.contables.auxiliary.application.useCase.log;

import co.unicauca.edu.co.contables.auxiliary.application.ports.in.log.IAuxiliaryBookLogQueryPort;
import co.unicauca.edu.co.contables.auxiliary.domain.models.log.AuxiliaryBookLog;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.AuxiliaryBookLog.IAuxiliaryBookLogQueryRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @brief Caso de uso de consulta para los logs de Libros Auxiliares.
 *
 * Implementa el puerto de entrada {@link IAuxiliaryBookLogQueryPort} y
 * recupera todos los eventos asociados a un libro auxiliar a partir de
 * su identificador público.
 */
@Service
@RequiredArgsConstructor
public class AuxiliaryBookLogQueryUC implements IAuxiliaryBookLogQueryPort {

    private final IAuxiliaryBookLogQueryRepositoryPort abLogQueryRepositoryPort;

    @Override
    public List<AuxiliaryBookLog> findAllByAuxiliaryBookPublicId(String auxiliaryBookId) {
        return this.abLogQueryRepositoryPort.findAllByAuxiliaryBookPublicId(auxiliaryBookId);
    }
}

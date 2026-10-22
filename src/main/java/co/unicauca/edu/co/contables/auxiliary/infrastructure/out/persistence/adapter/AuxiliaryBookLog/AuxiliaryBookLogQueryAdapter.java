package co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.adapter.AuxiliaryBookLog;

import co.unicauca.edu.co.contables.auxiliary.domain.models.log.AuxiliaryBookLog;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.AuxiliaryBookLog.IAuxiliaryBookLogQueryRepositoryPort;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.mapper.IAuxiliaryBookLogQueryEntityMapper;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.repository.IAuxiliaryBookLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @brief Adaptador para las operaciones de lectura de logs de libros auxiliares.
 *
 * Implementa {@link IAuxiliaryBookLogQueryRepositoryPort} consultando
 * los logs asociados al ID público de un libro auxiliar y mapeándolos
 * a la representación de dominio.
 */
@Component
@RequiredArgsConstructor
public class AuxiliaryBookLogQueryAdapter implements IAuxiliaryBookLogQueryRepositoryPort {

    private final IAuxiliaryBookLogQueryEntityMapper abLogQueryEntityMapper;
    private final IAuxiliaryBookLogRepository abLogRepository;

    @Override
    public List<AuxiliaryBookLog> findAllByAuxiliaryBookPublicId(String auxiliaryBookId) {
        return this.abLogQueryEntityMapper.toDomainList(this.abLogRepository.findByAuxiliaryBookPublicId(auxiliaryBookId));
    }

}

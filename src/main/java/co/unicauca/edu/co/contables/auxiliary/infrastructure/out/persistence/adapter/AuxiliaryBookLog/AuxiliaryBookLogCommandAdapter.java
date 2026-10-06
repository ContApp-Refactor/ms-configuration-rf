package co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.adapter.AuxiliaryBookLog;

import co.unicauca.edu.co.contables.auxiliary.domain.models.log.AuxiliaryBookLog;
import co.unicauca.edu.co.contables.auxiliary.domain.ports.AuxiliaryBookLog.IAuxiliaryBookLogCommandRepositoryPort;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.entity.AuxiliaryBookLogEntity;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.mapper.IAuxiliaryBookLogCommandEntityMapper;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.repository.IAuxiliaryBookLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @brief Adaptador para las operaciones de escritura de logs de libros auxiliares.
 *
 * Implementa {@link IAuxiliaryBookLogCommandRepositoryPort} persistiendo
 * registros de log en la base de datos y devolviendo el log recién
 * almacenado mapeado al dominio.
 */
@Component
@RequiredArgsConstructor
public class AuxiliaryBookLogCommandAdapter implements IAuxiliaryBookLogCommandRepositoryPort {

    private final IAuxiliaryBookLogCommandEntityMapper auxiliaryBookLogEntityMapper;
    private final IAuxiliaryBookLogRepository auxiliaryBookLogRepository;

    /**
     * @brief Persiste un registro de log del libro auxiliar.
     * @param auxiliaryBookLog Log a guardar.
     * @return Log persistido con los identificadores generados.
     */
    @Override
    public AuxiliaryBookLog registerAuxiliaryBookLog(AuxiliaryBookLog auxiliaryBookLog) {
        AuxiliaryBookLogEntity abLogEntity = this.auxiliaryBookLogEntityMapper.toEntity(auxiliaryBookLog);
        return this.auxiliaryBookLogEntityMapper.toDomain(this.auxiliaryBookLogRepository.save(abLogEntity));
    }
}

package co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import co.unicauca.edu.co.contables.auxiliary.domain.models.log.AuxiliaryBookLog;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.entity.AuxiliaryBookLogEntity;

/**
 * @brief Mapper MapStruct para convertir entidades de log a dominio (consultas).
 *
 * Convierte {@link AuxiliaryBookLogEntity} (individual o en lista) a
 * {@link AuxiliaryBookLog}, reutilizando el mapper del libro auxiliar
 * para mantener coherencia con la asociación anidada.
 */
@Mapper(componentModel = "spring", uses = { IAuxiliaryBookCommandEntityMapper.class })
public interface IAuxiliaryBookLogQueryEntityMapper {
    AuxiliaryBookLog toDomain(AuxiliaryBookLogEntity logEntity);
    List<AuxiliaryBookLog> toDomainList(List<AuxiliaryBookLogEntity> logsEntities);
}

package co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import co.unicauca.edu.co.contables.auxiliary.domain.models.history.AuxiliaryBookHistory;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.entity.AuxiliaryBookHistoryEntity;

/**
 * @brief Mapper MapStruct para convertir entidades de historial a dominio (consultas).
 *
 * Convierte {@link AuxiliaryBookHistoryEntity} individual o en lista a
 * su modelo de dominio {@link AuxiliaryBookHistory}, reutilizando el
 * mapper del libro auxiliar para la asociación anidada.
 */
@Mapper(componentModel = "spring", uses = { IAuxiliaryBookCommandEntityMapper.class })
public interface IAuxiliaryBookHistoryQueryEntityMapper {
    AuxiliaryBookHistory toDomain(AuxiliaryBookHistoryEntity historyEntity);
    List<AuxiliaryBookHistory> toDomainList(List<AuxiliaryBookHistoryEntity> historyEntities);
}

package co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.mapper;

import co.unicauca.edu.co.contables.auxiliary.domain.models.history.AuxiliaryBookHistory;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.entity.AuxiliaryBookHistoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @brief Mapper MapStruct entre el historial de dominio y su entidad JPA.
 *
 * Convierte bidireccionalmente entre {@link AuxiliaryBookHistory} y
 * {@link AuxiliaryBookHistoryEntity}, delegando el mapeo del libro
 * auxiliar asociado al {@link IAuxiliaryBookCommandEntityMapper}.
 */
@Mapper(
        componentModel = "spring",
        uses = { IAuxiliaryBookCommandEntityMapper.class }
)
public interface IAuxiliaryBookHistoryCommandEntityMapper {
    @Mapping(source = "auxiliaryBook", target = "auxiliaryBook")
    AuxiliaryBookHistoryEntity toEntity(AuxiliaryBookHistory auxiliaryBookHistory);

    @Mapping(source = "auxiliaryBook", target = "auxiliaryBook")
    AuxiliaryBookHistory toDomain(AuxiliaryBookHistoryEntity auxiliaryBookHistoryEntity);
}

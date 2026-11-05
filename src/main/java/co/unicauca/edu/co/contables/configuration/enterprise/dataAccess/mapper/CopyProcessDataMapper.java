package co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper;

import org.mapstruct.Mapper;

import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.CopyProcessEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.CopyProcess;

/**
 * @brief Mapeador de datos para un proceso de copia (BACKUP/DUPLICATE/RESTORE).
 */
@Mapper(componentModel = "spring")
public interface CopyProcessDataMapper {
    CopyProcessEntity toEntity(CopyProcess domain);

    CopyProcess toDomain(CopyProcessEntity entity);
}

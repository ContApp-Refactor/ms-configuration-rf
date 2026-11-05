package co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.mapper;

import co.unicauca.edu.co.contables.auxiliary.domain.models.history.AuxiliaryBookHistory;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.dto.response.AuxiliaryBookHistoryResponseDTO;
import org.mapstruct.Mapper;

/**
 * @brief Mapper REST entre el dominio de historial y su DTO de respuesta.
 *
 * Convierte instancias de {@link AuxiliaryBookHistory} del dominio a
 * {@link AuxiliaryBookHistoryResponseDTO}, utilizado por los
 * controladores de consulta de historial. Implementado por MapStruct.
 */
@Mapper(componentModel = "spring")
public interface IAuxiliaryBookHistoryResponseMapper {
    AuxiliaryBookHistoryResponseDTO toDtoResponse(AuxiliaryBookHistory history);
}

package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.dto.response.KardexDtoResponse;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.dto.response.ListLastProductKardexDtoResponse;

@Mapper(componentModel = "spring")
public interface IKardexResponseMapper {
    KardexDtoResponse toDtoResponse(Kardex kardex);
    
    ListLastProductKardexDtoResponse toListLastProductKardexDtoResponse(Kardex kardex);
    
    List<ListLastProductKardexDtoResponse> toListLastProductKardexDtoResponseList(List<Kardex> kardexList);
}

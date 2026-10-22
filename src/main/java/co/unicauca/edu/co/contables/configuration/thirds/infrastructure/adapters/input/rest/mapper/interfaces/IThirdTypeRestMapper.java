package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.mapper.interfaces;

import co.unicauca.edu.co.contables.configuration.thirds.domain.model.ThirdType;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.request.ThirdTypeCreateRequest;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * @brief Interfaz mapper para tipos de tercero
 */
@Mapper(componentModel = "spring")
public interface IThirdTypeRestMapper {

    ThirdType toThirdType(ThirdTypeCreateRequest thirdTypeCreateRequest);

    ThirdType toThirdTypeResponse(ThirdType thirdType);

    List<ThirdType> toThirdTypeResponseList(List<ThirdType> thirdTypeList);
}

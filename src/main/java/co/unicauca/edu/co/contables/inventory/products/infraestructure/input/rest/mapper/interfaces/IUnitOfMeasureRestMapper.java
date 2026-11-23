package co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.mapper.interfaces;

import co.unicauca.edu.co.contables.inventory.products.domain.model.UnitOfMeasure;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.dto.request.UnitOfMeasureCreateRequest;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.dto.response.UnitOfMeasureResponse;

import java.util.List;

import org.mapstruct.Mapper;

/**
 * @brief Mapper para transformación de entidades de unidad de medida
 *
 * Define contratos de mapeo entre entidades de dominio UnitOfMeasure
 * y DTOs de request/response para operaciones REST.
 */
@Mapper(componentModel = "spring")
public interface IUnitOfMeasureRestMapper {

    /**
     * @brief Convierte DTO de creación a entidad de unidad de medida
     * @param unitOfMeasureCreateRequest solicitud de creación de unidad de medida
     * @return entidad de unidad de medida convertida
     */
    UnitOfMeasure toUnitOfMeasure(UnitOfMeasureCreateRequest unitOfMeasureCreateRequest);

    /**
     * @brief Convierte entidad de unidad de medida a DTO de respuesta
     * @param unitOfMeasure entidad de unidad de medida
     * @return respuesta de unidad de medida convertida
     */
    UnitOfMeasureResponse toUnitOfMeasureResponse(UnitOfMeasure unitOfMeasure);

    /**
     * @brief Convierte lista de entidades a lista de DTOs de respuesta
     * @param unitOfMeasureList lista de entidades de unidad de medida
     * @return lista de respuestas de unidad de medida convertidas
     */
    List<UnitOfMeasureResponse> toUnitOfMeasureResponseList(List<UnitOfMeasure> unitOfMeasureList);
}

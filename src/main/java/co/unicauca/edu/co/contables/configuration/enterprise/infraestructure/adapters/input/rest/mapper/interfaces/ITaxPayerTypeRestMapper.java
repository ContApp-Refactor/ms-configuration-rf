package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.mapper.interfaces;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.TaxPayerType;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.data.response.TaxPayerTypeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;


@Mapper
public interface ITaxPayerTypeRestMapper {
    List<TaxPayerTypeResponse> toDomain(List<TaxPayerType> taxPayerType);
    @Mapping(target = "taxPayerTypes", ignore = true)
    TaxPayerTypeResponse toResponse(TaxPayerType taxPayerType);
}

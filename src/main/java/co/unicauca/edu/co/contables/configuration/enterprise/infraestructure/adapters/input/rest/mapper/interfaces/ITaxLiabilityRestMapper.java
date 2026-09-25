package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.mapper.interfaces;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.TaxLiability;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.data.response.TaxLiabilityResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;


@Mapper
public interface ITaxLiabilityRestMapper {
    List<TaxLiabilityResponse> toDomain(List<TaxLiability> taxLiabilities);
  
    @Mapping( target = "taxLiabilitys", ignore = true)
    TaxLiabilityResponse toResponse(TaxLiability taxLiability);
}

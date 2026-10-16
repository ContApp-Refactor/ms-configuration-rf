package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.mapper.interfaces;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.EnterpriseType;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.data.response.EnterpriseTypeResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface IEnterpriseTypeRestMapper {

    List<EnterpriseTypeResponse> toEnterpriseTypeResponseList(List<EnterpriseType> enterpriseTypes);
}
package co.unicauca.edu.co.contables.configuration.enterprise.domain.mapper;

import org.mapstruct.Mapper;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.EnterpriseType;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.DTO.response.EnterpriseTypeRes;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EnterpriseTypeDomainMapper {
    List<EnterpriseTypeRes> toResList(List<EnterpriseType> domain);
}

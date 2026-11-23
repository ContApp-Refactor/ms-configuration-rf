package co.unicauca.edu.co.contables.configuration.enterprise.domain.mapper;

import org.mapstruct.Mapper;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.TaxLiability;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.DTO.response.TaxLiabilityRes;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TaxLiabilityDomainMapper {
    TaxLiabilityRes toRes(TaxLiability domain);

    List<TaxLiabilityRes> toResList(List<TaxLiability> domain);
}

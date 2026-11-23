package co.unicauca.edu.co.contables.configuration.enterprise.domain.mapper;

import org.mapstruct.Mapper;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.TaxPayerType;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.DTO.response.TaxPayerTypeRes;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TaxPayerTypeDomainMapper {
    TaxPayerTypeRes toRes(TaxPayerType domain);

    List<TaxPayerTypeRes> toResList(List<TaxPayerType> domain);
}

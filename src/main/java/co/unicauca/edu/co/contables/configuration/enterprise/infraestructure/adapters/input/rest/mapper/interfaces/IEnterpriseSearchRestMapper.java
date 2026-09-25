package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.mapper.interfaces;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Enterprise;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.data.dto.CityResponseDto;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.data.dto.CountryResponseDto;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.data.dto.DepartmentResponseDto;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.data.dto.LocationResponseDto;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.data.response.EnterpriseByIdResponse;
import org.mapstruct.Mapper;

@Mapper
public interface IEnterpriseSearchRestMapper {

    default EnterpriseByIdResponse toEnterpriseByIdResponse(Enterprise enterprise) {
        return EnterpriseByIdResponse.builder()
                .id(enterprise.getId())
                .name(enterprise.getName())
                .nit(enterprise.getNit())
                .DV(enterprise.getDV())
                .phone(enterprise.getPhone())
                .branch(enterprise.getBranch())
                .email(enterprise.getEmail())
                .logo(enterprise.getLogo())
                .state(enterprise.getState())
                .inventoryConfigurationType(enterprise.getInventoryConfigurationType())
                .mainActivity(enterprise.getMainActivity())
                .secondaryActivity(enterprise.getSecondaryActivity())
                .taxLiabilities(enterprise.getTaxLiabilities())

                .taxPayerType(enterprise.getTaxPayerType())

                .enterpriseType(enterprise.getEnterpriseType())

                .personType(enterprise.getPersonType())

                .location(
                    LocationResponseDto.builder()
                    .id(enterprise.getLocation().getId())
                    .address(enterprise.getLocation().getAddress())
                    .city(CityResponseDto.builder()
                        .id(enterprise.getLocation().getCity().getId())
                        .name(enterprise.getLocation().getCity().getName())
                        .build()
                    )
                    .country(CountryResponseDto.builder()
                        .id(enterprise.getLocation().getCountry().getId())
                        .name(enterprise.getLocation().getCountry().getName())
                        .build()
                    )
                    .department(DepartmentResponseDto.builder()
                        .id(enterprise.getLocation().getDepartment().getId())
                        .name(enterprise.getLocation().getDepartment().getName())
                        .build()
                    )           
                    .build()
                )           
                .build();
    }
}

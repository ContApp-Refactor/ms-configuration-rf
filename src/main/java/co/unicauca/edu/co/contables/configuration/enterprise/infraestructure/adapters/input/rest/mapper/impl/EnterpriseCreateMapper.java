package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.mapper.impl;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.*;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.data.dto.CityResponseDto;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.data.dto.CountryResponseDto;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.data.dto.DepartmentResponseDto;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.data.dto.LocationResponseDto;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.data.request.EnterpriseCreateRequest;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.data.response.EnterpriseCreateResponse;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.mapper.interfaces.IEnterpriseCreateRestMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
@AllArgsConstructor
public class EnterpriseCreateMapper implements IEnterpriseCreateRestMapper{


    @Override
    public Enterprise toDomain(EnterpriseCreateRequest enterpriseCreateResponse) {
        Enterprise enterprise = Enterprise.builder()
        .name(enterpriseCreateResponse.getName())
        .nit(enterpriseCreateResponse.getNit())
        .DV(enterpriseCreateResponse.getDV())
        .phone(enterpriseCreateResponse.getPhone())
        .branch(enterpriseCreateResponse.getBranch())
        .email(enterpriseCreateResponse.getEmail())
        .logo(enterpriseCreateResponse.getLogo())
        .state(enterpriseCreateResponse.getState())
        .inventoryConfigurationType(enterpriseCreateResponse.getInventoryConfigurationType())
        .mainActivity(enterpriseCreateResponse.getMainActivity())
        .secondaryActivity(enterpriseCreateResponse.getSecondaryActivity())
        .taxLiabilities(toTaxLiability(enterpriseCreateResponse.getTaxLiabilities()))
        
        .taxPayerType(TaxPayerType.builder().id(enterpriseCreateResponse.getTaxPayerType()).build())

        .enterpriseType(EnterpriseType.builder().id(enterpriseCreateResponse.getEnterpriseType()).build())
                                            
        .personType(PersonType.builder()
            .name(enterpriseCreateResponse.getPersonType().getName())
            .surname(enterpriseCreateResponse.getPersonType().getSurname())
            .bussinessName(enterpriseCreateResponse.getPersonType().getBussinessName())
            .type(enterpriseCreateResponse.getPersonType().getType())
            .build()
        )
        
        .location(
            Location.builder()
            .address(enterpriseCreateResponse.getLocation().getAddress())
            .city(City.builder().id(enterpriseCreateResponse.getLocation().getCity()).build())
            .country(Country.builder().id(enterpriseCreateResponse.getLocation().getCountry()).build())
            .department(Department.builder().id(enterpriseCreateResponse.getLocation().getDepartment()).build())
            .build()
        )     

        .build();  //fin de enterprise
        
        return enterprise;    
    }

    @Override
    public EnterpriseCreateResponse toCreateResponse(Enterprise enterprise) {
        EnterpriseCreateResponse enterpriseCreateResponse = EnterpriseCreateResponse.builder()
        .id(enterprise.getId())
        .name(enterprise.getName())
        .nit(enterprise.getNit())
        .DV(enterprise.getDV())
        .phone(enterprise.getPhone())
        .branch(enterprise.getBranch())
        .email(enterprise.getEmail())
        .logo(enterprise.getLogo())
        .inventoryConfigurationType(enterprise.getInventoryConfigurationType())

        .taxLiabilities(enterprise.getTaxLiabilities())

        .taxPayerType(enterprise.getTaxPayerType())

        .enterpriseType(enterprise.getEnterpriseType())

        .personType(PersonType.builder()
            .name(enterprise.getPersonType().getName())
            .surname(enterprise.getPersonType().getSurname())
            .bussinessName(enterprise.getPersonType().getBussinessName())
            .type(enterprise.getPersonType().getType())
            .build()
        )

        .location(LocationResponseDto.builder()
            .address(enterprise.getLocation().getAddress())
            .city(CityResponseDto.builder()
                .id(enterprise.getLocation().getCity().getId())
                .name(enterprise.getLocation().getCity().getName())
                .build())
            .country(CountryResponseDto.builder()
                .id(enterprise.getLocation().getCountry().getId())
                .name(enterprise.getLocation().getCountry().getName())
                .build())
            .department(DepartmentResponseDto.builder()
                .id(enterprise.getLocation().getDepartment().getId())
                .name(enterprise.getLocation().getDepartment().getName())
                .build())
            .build())
   
        .build();
        return enterpriseCreateResponse;
    }

    private  List<TaxLiability> toTaxLiability(List<Long> taxLiabilities){
        List<TaxLiability> taxLiabilitiesList = new ArrayList<>();
        for (Long taxLiability : taxLiabilities) {
            taxLiabilitiesList.add(TaxLiability.builder().id(taxLiability).build());
        }
        return taxLiabilitiesList;
    }
       
}

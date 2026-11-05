package co.unicauca.edu.co.contables.configuration.enterprise.domain.mapper;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.City;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Country;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Department;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Enterprise;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.EnterpriseType;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Location;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.PersonType;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.TaxLiability;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.TaxPayerType;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.DTO.request.EnterpriseCreateReq;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.DTO.request.EnterpriseUpdateReq;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.DTO.response.CityRes;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.DTO.response.CountryRes;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.DTO.response.DepartmentRes;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.DTO.response.EnterpriseRes;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.DTO.response.LocationRes;

/**
 * @brief Mapeador de dominio para empresas.
 *
 * Convierte entre los DTOs de presentación (request/response) y el modelo
 * de dominio {@link Enterprise}. Se implementa manualmente (no MapStruct)
 * porque construye explícitamente los objetos de dominio anidados
 * (ubicación, tipo de persona) a partir de los ids recibidos en el request.
 */
@Component
public class EnterpriseDomainMapper {

    public Enterprise toDomain(EnterpriseCreateReq req) {
        if (req == null) {
            return null;
        }
        return Enterprise.builder()
                .name(req.getName())
                .nit(req.getNit())
                .DV(req.getDV())
                .phone(req.getPhone())
                .branch(req.getBranch())
                .email(req.getEmail())
                .logo(req.getLogo())
                .state(req.getState())
                .inventoryConfigurationType(req.getInventoryConfigurationType())
                .mainActivity(req.getMainActivity())
                .secondaryActivity(req.getSecondaryActivity())
                .taxLiabilities(toTaxLiabilities(req.getTaxLiabilities()))
                .taxPayerType(TaxPayerType.builder().id(req.getTaxPayerType()).build())
                .enterpriseType(EnterpriseType.builder().id(req.getEnterpriseType()).build())
                .personType(req.getPersonType() == null ? null
                        : PersonType.builder()
                                .type(req.getPersonType().getType())
                                .name(req.getPersonType().getName())
                                .surname(req.getPersonType().getSurname())
                                .bussinessName(req.getPersonType().getBussinessName())
                                .build())
                .location(req.getLocation() == null ? null
                        : Location.builder()
                                .address(req.getLocation().getAddress())
                                .city(City.builder().id(req.getLocation().getCity()).build())
                                .country(Country.builder().id(req.getLocation().getCountry()).build())
                                .department(Department.builder().id(req.getLocation().getDepartment()).build())
                                .build())
                .build();
    }

    public Enterprise toDomain(EnterpriseUpdateReq req) {
        if (req == null) {
            return null;
        }
        return Enterprise.builder()
                .name(req.getName())
                .nit(req.getNit())
                .DV(req.getDV())
                .phone(req.getPhone())
                .branch(req.getBranch())
                .email(req.getEmail())
                .logo(req.getLogo())
                .state(req.getState())
                .inventoryConfigurationType(req.getInventoryConfigurationType())
                .mainActivity(req.getMainActivity())
                .secondaryActivity(req.getSecondaryActivity())
                .taxLiabilities(toTaxLiabilities(req.getTaxLiabilities()))
                .taxPayerType(TaxPayerType.builder().id(req.getTaxPayerType()).build())
                .enterpriseType(EnterpriseType.builder().id(req.getEnterpriseType()).build())
                .personType(req.getPersonType() == null ? null
                        : PersonType.builder()
                                .type(req.getPersonType().getType())
                                .name(req.getPersonType().getName())
                                .surname(req.getPersonType().getSurname())
                                .bussinessName(req.getPersonType().getBussinessName())
                                .build())
                .location(req.getLocation() == null ? null
                        : Location.builder()
                                .address(req.getLocation().getAddress())
                                .city(City.builder().id(req.getLocation().getCity()).build())
                                .country(Country.builder().id(req.getLocation().getCountry()).build())
                                .department(Department.builder().id(req.getLocation().getDepartment()).build())
                                .build())
                .build();
    }

    public EnterpriseRes toRes(Enterprise enterprise) {
        if (enterprise == null) {
            return null;
        }
        return EnterpriseRes.builder()
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
                .location(toLocationRes(enterprise.getLocation()))
                .build();
    }

    private LocationRes toLocationRes(Location location) {
        if (location == null) {
            return null;
        }
        return LocationRes.builder()
                .id(location.getId())
                .address(location.getAddress())
                .city(location.getCity() == null ? null
                        : CityRes.builder().id(location.getCity().getId()).name(location.getCity().getName())
                                .build())
                .country(location.getCountry() == null ? null
                        : CountryRes.builder().id(location.getCountry().getId())
                                .name(location.getCountry().getName()).build())
                .department(location.getDepartment() == null ? null
                        : DepartmentRes.builder().id(location.getDepartment().getId())
                                .name(location.getDepartment().getName()).build())
                .build();
    }

    private List<TaxLiability> toTaxLiabilities(List<Long> ids) {
        if (ids == null) {
            return null;
        }
        List<TaxLiability> result = new ArrayList<>();
        for (Long id : ids) {
            result.add(TaxLiability.builder().id(id).build());
        }
        return result;
    }
}

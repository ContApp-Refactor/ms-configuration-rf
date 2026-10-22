package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.mapper;

import co.unicauca.edu.co.contables.configuration.thirds.domain.model.City;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.Country;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.State;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.Third;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.request.ThirdCreateRequest;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.request.ThirdUpdateRequest;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.response.ChangeThirdStateResponse;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.response.GetThirdResponse;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.response.ThirdResponse;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.response.ThirdsEnterpriseListResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * @brief Mapper para conversión entre DTOs de terceros y entidades del dominio
 */
@Mapper(componentModel = "spring")
public interface ThirdRestMapper {

    @Mapping(target = "country", ignore = true)
    @Mapping(target = "province", ignore = true)
    @Mapping(target = "city", ignore = true)
    Third toThird(ThirdCreateRequest thirdCreateRequest);

    @Mapping(target = "country", ignore = true)
    @Mapping(target = "province", ignore = true)
    @Mapping(target = "city", ignore = true)
    Third toThird(ThirdUpdateRequest thirdUpdateRequest);

    @Mapping(source = "thId", target = "id")
    @Mapping(target = "name", expression = "java(getThirdName(third))")
    @Mapping(target = "description", expression = "java(getThirdDescription(third))")
    ThirdResponse toThirdCreateResponse(Third third);

    default String getThirdName(Third third) {
        if (third == null) {
            return null;
        }

        if (third.isLegalEntity()) {
            return third.getSocialReason();
        } else {
            StringBuilder name = new StringBuilder();
            if (third.getNames() != null && !third.getNames().trim().isEmpty()) {
                name.append(third.getNames());
            }
            if (third.getLastNames() != null && !third.getLastNames().trim().isEmpty()) {
                if (name.length() > 0) {
                    name.append(" ");
                }
                name.append(third.getLastNames());
            }
            return name.length() > 0 ? name.toString() : null;
        }
    }

    default String getThirdDescription(Third third) {
        if (third == null) {
            return null;
        }

        StringBuilder description = new StringBuilder();

        if (third.getPersonType() != null) {
            description.append("Persona ").append(third.getPersonType().name());
        }

        if (third.getTypeId() != null && third.getTypeId().getTypeIdname() != null) {
            if (description.length() > 0) {
                description.append(" - ");
            }
            description.append(third.getTypeId().getTypeIdname())
                      .append(": ").append(third.getIdNumber());
        }

        if (description.length() > 0) {
            description.append(" - ");
        }
        description.append("Estado: ").append(third.isActive() ? "Activo" : "Inactivo");

        return description.toString();
    }

    ChangeThirdStateResponse toChangeThirdStateResponse(Boolean result);

    @Mapping(source = "page", target = "results")
    ThirdsEnterpriseListResponse toListThirdsResponse(Page<Third> page);

    @Mapping(target = "country", expression = "java(mapCountryToString(third.getCountry()))")
    @Mapping(target = "province", expression = "java(mapStateToString(third.getProvince()))")
    @Mapping(target = "city", expression = "java(mapCityToString(third.getCity()))")
    GetThirdResponse toGetThirdResponse(Third third);

    List<GetThirdResponse> toGetThirdResponseList(List<Third> thirdList);

    default String mapCountryToString(Country country) {
        return country != null ? country.getCountryCode() : null;
    }

    default String mapStateToString(State state) {
        return state != null ? state.getStateCode() : null;
    }

    default String mapCityToString(City city) {
        return city != null ? city.getCityCode() : null;
    }
}

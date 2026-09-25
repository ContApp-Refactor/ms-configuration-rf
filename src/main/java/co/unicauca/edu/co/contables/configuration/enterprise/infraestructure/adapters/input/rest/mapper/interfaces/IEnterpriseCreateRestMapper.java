package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.mapper.interfaces;


import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Enterprise;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.data.request.EnterpriseCreateRequest;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.data.response.EnterpriseCreateResponse;

public interface IEnterpriseCreateRestMapper {

    Enterprise toDomain(EnterpriseCreateRequest enterpriseCreateResponse);

    EnterpriseCreateResponse toCreateResponse(Enterprise enterprise);
}

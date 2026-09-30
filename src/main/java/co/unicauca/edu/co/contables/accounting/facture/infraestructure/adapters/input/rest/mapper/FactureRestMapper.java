package co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.input.rest.mapper;

import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import co.unicauca.edu.co.contables.accounting.facture.domain.model.Facture;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.input.rest.data.request.FactureCreateRequest;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.input.rest.data.response.FactureCreateResponse;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.input.rest.data.response.FactureGetResponse;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.input.rest.data.response.FactureListResponse;

@Mapper
public interface FactureRestMapper {
    Facture toFacture(FactureCreateRequest factureCreateRequest);

    FactureCreateResponse toFactureCreateResponse(int code,byte[] pdf,String status);

    FactureListResponse toFactureListResponse(Page<Facture> results);

    FactureGetResponse toGetFactureResponse(Facture facture);
}

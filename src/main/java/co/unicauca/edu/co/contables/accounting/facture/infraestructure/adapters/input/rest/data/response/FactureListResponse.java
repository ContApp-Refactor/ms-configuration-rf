package co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.input.rest.data.response;

import org.springframework.data.domain.Page;

import co.unicauca.edu.co.contables.accounting.facture.domain.model.Facture;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FactureListResponse {
    private Page<Facture> results;
}

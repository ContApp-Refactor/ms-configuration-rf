package co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.input.rest.data.response;

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
public class FactureCreateResponse {
    private int code;
    private byte[] pdf;
    private String status;
}

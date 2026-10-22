package co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.input.rest.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * @brief DTO de solicitud para crear impuestos
 *
 * Contiene los campos requeridos para crear un nuevo impuesto
 * con validaciones de formato y obligatoriedad.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaxCreateReq {
    @JsonIgnore
    private  Long id;
    @NotBlank(message = "El id de la empresa es requerido")
    private String idEnterprise;
    @NotBlank(message = "El código es requerido")
    private String code;
    @NotBlank(message = "La descripción es requerida")
    private String description;
    @NotNull(message = "El interés es requerido")
    private Double interest;
    private Long purchaseTaxId;
    private Long salesTaxId;
}

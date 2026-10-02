package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.dto.request;


import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief DTO para solicitud de creación de cuenta contable
 *
 * Contiene los datos necesarios para crear una nueva cuenta en el catálogo,
 * incluyendo campos obligatorios y opcionales para jerarquía y funcionalidad avanzada.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountCatalogueCreateReq {
    @JsonIgnore
    private Long id;

    private String idEnterprise;

    @NotBlank(message = "El código es requerido")
    private String code;

    @NotBlank(message = "La descripcion es requerida")
    private String description;

    @NotBlank(message = "La naturaleza es requerida")
    private String nature;

    @NotBlank(message = "La estado financiero es requerido")
    private String financialStatus;
    
    @NotBlank(message = "La clasificacion es requerido")
    private String classification;
    
    private Long parent;

    private List<AccountCatalogueCreateReq> children;
    
    private Boolean crossing;
    private Boolean costCenter;
}

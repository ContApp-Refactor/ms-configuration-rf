package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.request;

import co.unicauca.edu.co.contables.configuration.thirds.domain.model.PersonClassification;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * @brief DTO para solicitud de actualización de tipo de identificación
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TypeIdUpdateRequest {

    private Long id;

    @NotNull(message = "El ID de la empresa no puede estar vacío")
    private String entId;

    @NotNull(message = "El código del tipo de identificación no puede estar vacío")
    @Size(min = 2, max = 10, message = "El código debe tener entre 2 y 10 caracteres")
    private String typeId;

    @NotNull(message = "El nombre del tipo de identificación no puede estar vacío")
    private String typeIdname;

    private Boolean status;

    private PersonClassification classification;
}

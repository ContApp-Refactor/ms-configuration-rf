package co.unicauca.edu.co.contables.configuration.typesOfDocuments.presentation.DTO.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentTypeUpdateReq {

    @NotNull(message = "El id es obligatorio")
    private Long id;

    @NotBlank(message = "La empresa es obligatoria")
    private String idEnterprise;

    @NotBlank(message = "El prefijo es obligatorio")
    @Size(max = 10, message = "El prefijo no debe exceder 10 caracteres")
    private String prefix;

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotNull(message = "La clase de documento es obligatoria")
    private Long documentClassId;

    @NotNull(message = "El módulo es obligatorio")
    @Min(value = 1, message = "El ID del módulo debe ser mayor a 0")
    @Max(value = 8, message = "El ID del módulo debe estar entre 1 y 8")
    private Integer moduleId;
}



package co.unicauca.edu.co.contables.configuration.classesOfDocuments.presentation.DTO.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief DTO de solicitud para actualizar clases de documento
 *
 * Estructura de datos para solicitudes de actualización de clases de documento,
 * incluyendo validaciones de obligatoriedad para ID, empresa y nombre.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DocumentClassUpdateReq {
    @NotNull(message = "El id es obligatorio")
    private Long id;

    @NotBlank(message = "La empresa es obligatoria")
    private String idEnterprise;

    @NotBlank(message = "El nombre es obligatorio")
    private String name;
}



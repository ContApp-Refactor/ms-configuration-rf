package co.unicauca.edu.co.contables.configuration.classesOfDocuments.presentation.DTO.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief DTO de solicitud para crear clases de documento
 *
 * Estructura de datos para solicitudes de creación de nuevas clases de documento,
 * con validaciones de obligatoriedad para empresa y nombre.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DocumentClassCreateReq {
    @NotBlank(message = "La empresa es obligatoria")
    private String idEnterprise;

    @NotBlank(message = "El nombre es obligatorio")
    private String name;
}



package co.unicauca.edu.co.contables.configuration.enterprise.presentation.DTO.response;

import lombok.*;

/**
 * @brief Respuesta con la información de una materia. Mantiene exactamente
 * la forma que consume el frontend ({@code Subject { code, name }}).
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SubjectRes {
    private String code;
    private String name;
}

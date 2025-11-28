package co.unicauca.edu.co.contables.configuration.helpCenter.presentation.DTO.response;

import lombok.*;

/**
 * @brief DTO de respuesta para registros del centro de ayuda
 *
 * DTO que representa la respuesta de operaciones con registros del centro de ayuda,
 * incluyendo información de módulo, nombre, descripción y estado.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HelpCenterRes {
    private Long id;
    private Integer moduleId;
    private String moduleName;
    private String name;
    private String description;
    private Boolean status;
}

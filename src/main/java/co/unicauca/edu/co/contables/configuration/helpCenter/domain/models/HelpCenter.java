package co.unicauca.edu.co.contables.configuration.helpCenter.domain.models;

import lombok.*;

/**
 * @brief Modelo de dominio para registro del centro de ayuda
 *
 * Modelo de dominio que representa un registro de ayuda en la lógica de negocio,
 * con información de módulo, nombre, descripción y estado.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HelpCenter {
    private Long id;
    private Integer moduleId;
    private String name;
    private String description;
    @Builder.Default
    private Boolean status = true;
}

package co.unicauca.edu.co.contables.configuration.thirds.domain.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * @brief Modelo de dominio que representa una ciudad
 *
 * Entidad que contiene la información básica de una ciudad para completar
 * la jerarquía geográfica. Está relacionada con un estado/departamento específico.
 */
@Builder
@Getter
@Setter
@EqualsAndHashCode(of = {"cityCode", "stateCode", "countryCode"})
@ToString(of = {"cityCode", "cityName", "stateCode"})
@AllArgsConstructor
@NoArgsConstructor
public class City {

    @NotBlank(message = "El código de la ciudad no puede estar vacío")
    @Size(max = 10, message = "El código de la ciudad no puede exceder los 10 caracteres")
    private String cityCode;

    @NotBlank(message = "El nombre de la ciudad no puede estar vacío")
    @Size(max = 100, message = "El nombre de la ciudad no puede exceder los 100 caracteres")
    private String cityName;

    @NotNull(message = "El estado es obligatorio")
    private State state;

    @NotBlank(message = "El código del estado no puede estar vacío")
    @Size(max = 10, message = "El código del estado no puede exceder los 10 caracteres")
    private String stateCode;

    @NotBlank(message = "El código del país no puede estar vacío")
    @Size(max = 3, message = "El código del país no puede exceder los 3 caracteres")
    private String countryCode;

}

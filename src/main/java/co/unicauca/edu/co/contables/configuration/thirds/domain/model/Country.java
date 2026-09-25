package co.unicauca.edu.co.contables.configuration.thirds.domain.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * @brief Modelo de dominio que representa un país
 *
 * Entidad que contiene la información básica de un país para la jerarquía geográfica.
 * Forma parte del sistema de ubicación geográfica utilizado en los terceros.
 */
@Builder
@Getter
@Setter
@EqualsAndHashCode(of = {"countryCode"})
@ToString(of = {"countryCode", "countryName"})
@AllArgsConstructor
@NoArgsConstructor
public class Country {

    @NotBlank(message = "El código del país no puede estar vacío")
    @Size(max = 3, message = "El código del país no puede exceder los 3 caracteres")
    private String countryCode;

    @NotBlank(message = "El nombre del país no puede estar vacío")
    @Size(max = 100, message = "El nombre del país no puede exceder los 100 caracteres")
    private String countryName;

}

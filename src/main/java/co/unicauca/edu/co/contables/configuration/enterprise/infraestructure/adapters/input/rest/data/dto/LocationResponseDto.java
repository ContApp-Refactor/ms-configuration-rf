package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.data.dto;

import lombok.*;

/**
 * DTO que representa la respuesta de una ubicación.
 * Se utiliza para transferir datos de ubicación desde y hacia las interfaces REST,
 * incluyendo detalles como dirección, ciudad, departamento y país.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocationResponseDto {

    /**
     * Identificador único de la ubicación.
     */
    private Long id;

    /**
     * Dirección específica de la ubicación.
     */
    private String address;

    /**
     * DTO de la ciudad asociada a la ubicación.
     */
    private CityResponseDto city;

    /**
     * DTO del departamento asociado a la ubicación.
     */
    private DepartmentResponseDto department;

    /**
     * DTO del país asociado a la ubicación.
     */
    private CountryResponseDto country;
}

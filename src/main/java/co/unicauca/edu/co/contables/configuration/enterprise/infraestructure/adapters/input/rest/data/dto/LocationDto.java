package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.data.dto;


import lombok.*;

/**
 * DTO que representa la información de una ubicación.
 * Se utiliza para transferir datos de ubicación desde y hacia las interfaces REST,
 * incluyendo la jerarquía geográfica completa (país, departamento, ciudad).
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
public class LocationDto {

    /**
     * Dirección específica de la ubicación.
     */
    private String address;

    /**
     * Identificador de la ciudad.
     */
    private Long city;

    /**
     * Identificador del departamento.
     */
    private Long department;

    /**
     * Identificador del país.
     */
    private Long country; 
}

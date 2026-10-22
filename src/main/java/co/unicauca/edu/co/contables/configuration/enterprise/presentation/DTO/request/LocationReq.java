package co.unicauca.edu.co.contables.configuration.enterprise.presentation.DTO.request;

import lombok.*;

/**
 * @brief DTO de solicitud con la ubicación de una empresa (por ids).
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocationReq {
    private String address;
    private Long city;
    private Long department;
    private Long country;
}

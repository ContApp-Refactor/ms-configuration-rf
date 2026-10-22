package co.unicauca.edu.co.contables.configuration.enterprise.presentation.DTO.request;

import lombok.*;

/**
 * @brief DTO de solicitud con el tipo de persona de una empresa.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PersonTypeReq {
    private String type;
    private String name;
    private String surname;
    private String bussinessName;
}

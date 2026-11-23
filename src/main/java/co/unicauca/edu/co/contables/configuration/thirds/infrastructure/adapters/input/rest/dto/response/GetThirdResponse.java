package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.response;

import co.unicauca.edu.co.contables.configuration.thirds.domain.enums.ePersonType;
import co.unicauca.edu.co.contables.configuration.thirds.domain.enums.eThirdGender;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.ThirdType;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.TypeId;
import lombok.*;

import java.util.Set;

/**
 * @brief DTO de respuesta para consulta de tercero específico
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetThirdResponse {
    private Long thId;
    private Long entId;

    private TypeId typeId;

    private Set<ThirdType> thirdTypes;

    private ePersonType personType;
    private String names;
    private String lastNames;
    private String socialReason;
    private eThirdGender gender;
    private Long idNumber;
    private Long verificationNumber;
    private Boolean state;
    private String country;
    private String province;
    private String city;
    private String address;
    private String phoneNumber;
    private String email;
    private Integer usageCount;
}

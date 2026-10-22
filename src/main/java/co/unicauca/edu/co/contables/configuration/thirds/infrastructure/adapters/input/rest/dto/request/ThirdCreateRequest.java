package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.request;

import co.unicauca.edu.co.contables.configuration.thirds.domain.enums.ePersonType;
import co.unicauca.edu.co.contables.configuration.thirds.domain.enums.eThirdGender;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.ThirdType;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.TypeId;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.Set;

/**
 * @brief DTO para solicitud de creación de tercero
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ThirdCreateRequest {

    private Long thId;

    @NotNull(message = "El ID de la empresa no puede estar vacío")
    private String entId;

    @NotNull(message = "El tipo de identificación no puede estar vacío")
    private TypeId typeId;

    @NotNull(message = "El tipo de persona no puede estar vacío")
    private ePersonType personType;

    @NotNull(message = "El tipo de tercero no puede estar vacío")
    private Set<ThirdType> thirdTypes;

    private String names;
    private String lastNames;
    private String socialReason;
    private eThirdGender gender;
    private Long idNumber;

    @Min(value = 0, message = "El dígito de verificación debe estar entre 0 y 9")
    @Max(value = 9, message = "El dígito de verificación debe estar entre 0 y 9")
    private Long verificationNumber;

    @Builder.Default
    private Boolean state = true;

    @Size(max = 3, message = "El código del país no puede exceder los 3 caracteres")
    private String countryCode;

    @Size(max = 10, message = "El código del estado no puede exceder los 10 caracteres")
    private String stateCode;

    @Size(max = 10, message = "El código de la ciudad no puede exceder los 10 caracteres")
    private String cityCode;

    @NotBlank(message = "La dirección no puede estar vacía")
    private String address;

    @NotBlank(message = "El número de teléfono no puede estar vacío")
    private String phoneNumber;

    @NotBlank(message = "El correo electrónico no puede estar vacío")
    @Email(message = "El correo electrónico debe tener un formato válido")
    private String email;
}


package co.unicauca.edu.co.contables.configuration.enterprise.presentation.DTO.request;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.InventoryConfigurationTypeEnum;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.StateEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;
import java.util.UUID;

/**
 * @brief DTO de solicitud para crear una empresa
 *
 * Contiene todos los campos necesarios para crear una nueva empresa,
 * incluyendo validaciones para asegurar la integridad de los datos.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnterpriseCreateReq {

    @NotBlank(message = "El nombre de la empresa es requerido")
    private String name;

    @NotBlank(message = "El NIT es requerido")
    private String nit;

    private String DV;

    @NotBlank(message = "El teléfono es requerido")
    private String phone;

    private String branch;

    @NotBlank(message = "El email es requerido")
    @Email(message = "El email no es válido")
    private String email;

    private String logo;

    private Long mainActivity;
    private Long secondaryActivity;

    private List<Long> taxLiabilities;

    /**
     * Ids de las materias de la empresa. Ids inexistentes o de otro tenant se ignoran.
     * En actualizacion: null = no modificar, lista vacia = quitar todas las materias.
     */
    @Size(max = 500, message = "Demasiadas materias")
    private List<@NotNull(message = "El id de materia no puede ser nulo") UUID> subjects;

    @Builder.Default
    private StateEnum state = StateEnum.ACTIVE;

    private InventoryConfigurationTypeEnum inventoryConfigurationType;

    @NotNull(message = "El tipo de contribuyente es requerido")
    @Min(value = 1, message = "El tipo de contribuyente no es válido")
    private Long taxPayerType;

    @NotNull(message = "El tipo de empresa es requerido")
    @Min(value = 1, message = "El tipo de empresa no es válido")
    private Long enterpriseType;

    @NotNull(message = "El tipo de persona es requerido")
    private PersonTypeReq personType;

    @NotNull(message = "La ubicación es requerida")
    private LocationReq location;
}

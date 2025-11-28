package co.unicauca.edu.co.contables.configuration.costCenters.presentation.DTO.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;
import lombok.*;

/**
 * @brief DTO de solicitud para actualizar centros de costo
 *
 * DTO que representa la solicitud para actualizar un centro de costo,
 * con validaciones de código y campos obligatorios.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CostCenterUpdateReq {

    @NotNull
    private Long id;

    @NotBlank
    private String idEnterprise;

    @NotNull(message = "El codigo es obligatorio")
    private String code;

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    private Long parentId; // opcional

    @AssertTrue(message = "El codigo debe tener longitud 2 o longitud >= 4.")
    public boolean isCodeLengthValid() {
        if (code == null) {
            return false;
        }
        int length = code.trim().length();
        return length == 2 || length >= 4;
    }
}



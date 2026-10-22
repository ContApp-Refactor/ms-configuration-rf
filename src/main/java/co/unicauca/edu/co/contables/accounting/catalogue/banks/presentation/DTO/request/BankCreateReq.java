package co.unicauca.edu.co.contables.accounting.catalogue.banks.presentation.DTO.request;

import co.unicauca.edu.co.contables.accounting.catalogue.banks.domain.enums.Currency;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.Set;

/**
 * @brief DTO de solicitud para crear bancos
 *
 * Contiene los campos requeridos para crear un nuevo banco
 * con validaciones de formato y obligatoriedad.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankCreateReq {

    @NotBlank(message = "La empresa es obligatoria")
    private String idEnterprise;

    @NotBlank(message = "El código del banco es obligatorio")
    @Pattern(regexp = "^\\d{2}$", message = "El código debe tener exactamente 2 dígitos (01-99)")
    @Positive(message = "El código debe ser positivo")
    private String code;

    @NotBlank(message = "El nombre del banco es obligatorio")
    @Size(max = 100, message = "The name must not exceed 100 characters")
    private String name;

    @NotEmpty(message = "Debe seleccionar al menos una moneda")
    private Set<Currency> currencies;

    @Builder.Default
    private Boolean status = true;
}

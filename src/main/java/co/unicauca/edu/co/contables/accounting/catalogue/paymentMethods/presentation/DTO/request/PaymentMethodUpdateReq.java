package co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.presentation.DTO.request;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * @brief DTO de solicitud para actualizar métodos de pago
 *
 * Contiene los campos necesarios para actualizar un método de pago existente
 * con validaciones de formato y obligatoriedad.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethodUpdateReq {

    @NotNull(message = "El ID es obligatorio")
    private Long id;

    @NotBlank(message = "La empresa es obligatoria")
    private String idEnterprise;

    @NotBlank(message = "El nombre del método de pago es obligatorio")
    @Size(max = 100, message = "El nombre no debe exceder 100 caracteres")
    private String name;

    @NotNull(message = "El ID de la cuenta contable es obligatorio")
    private Long accountingAccountId;
}

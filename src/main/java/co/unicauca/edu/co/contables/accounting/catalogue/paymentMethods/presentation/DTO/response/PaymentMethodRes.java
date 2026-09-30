package co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.presentation.DTO.response;

import lombok.*;

/**
 * @brief DTO de respuesta para métodos de pago
 *
 * Contiene la información completa de un método de pago
 * para respuestas de la API REST.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethodRes {
    private Long id;
    private String name;
    private String accountingAccount; // Código completo (código - descripción)
    private Long accountingAccountId; // Solo el ID de la cuenta contable
    private Boolean status;
    private String idEnterprise;
    private Integer usageCount;
}

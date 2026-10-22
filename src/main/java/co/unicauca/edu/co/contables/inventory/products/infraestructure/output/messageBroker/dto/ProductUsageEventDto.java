package co.unicauca.edu.co.contables.inventory.products.infraestructure.output.messageBroker.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief DTO para eventos de uso de productos recibidos desde PEPS
 *
 * Contiene la información necesaria para actualizar el contador de uso
 * cuando PEPS notifica que ha utilizado un producto.
 */
@Getter 
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductUsageEventDto {
    private Long productId;
    private String enterpriseId;
    private Integer quantityUsed;
}


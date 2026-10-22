package co.unicauca.edu.co.contables.inventory.products.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief DTO para sincronización de productos entre sistemas
 *
 * Contiene los datos esenciales de un producto para operaciones
 * de sincronización y replicación entre diferentes instancias del sistema.
 */
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductSyncDto {
    private Long productId;
    private String name;
    private String reference;
    private String enterpriseId;
    private String presentation;
    private Integer quantity;
    private double cost;
    private boolean state;
}

package co.unicauca.edu.co.contables.inventory.products.domain.model;

import java.util.List;

import lombok.*;

/**
 * @brief Entidad que representa una categoría de productos
 *
 * Agrupa productos relacionados y define configuraciones contables específicas
 * como inventario, costos, ventas y retornos asociados.
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Category {

    private Long id;
    private String name;
    private String description;
    private String enterpriseId;
    private Long inventoryId;
    private Long costId;
    private Long saleId;
    private Long returnId;
    private List<Long> taxes;

    @Builder.Default
    private boolean state = true;

}

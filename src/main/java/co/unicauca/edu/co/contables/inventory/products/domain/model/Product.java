package co.unicauca.edu.co.contables.inventory.products.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief Entidad que representa un producto
 *
 * Contiene toda la información relacionada con un producto: datos básicos,
 * relaciones con entidades relacionadas y estado de activación.
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    private Long id;
    private String code;
    private String name;
    private String description;
    private Integer quantity;
    private Long unitOfMeasureId;
    private Long categoryId;
    private String enterpriseId;
    private double cost;

    @Builder.Default
    private boolean state = true;

    private String reference;

    private Long productTypeId;

    private String presentation;

    @Builder.Default
    private Integer usageCount = 0;
 
    /**
     * @brief Genera un código único basado en el nombre del producto, ID de la categoría y ID del producto.
     */
    public void generateCode() {
        String namePrefix = name != null && name.length() >= 3 ? name.substring(0, 3).toUpperCase() : "UNK";
        this.code = String.format("%s-%d-%d", namePrefix, categoryId, id);
    }

    /**
     * @brief Incrementa el contador de uso del producto
     */
    public void incrementUsageCount() {
        this.usageCount = this.usageCount == null ? 1 : this.usageCount + 1;
    }

    /**
     * @brief Verifica si el producto está siendo usado
     * @return true si el producto tiene uso registrado
     */
    public boolean isInUse() {
        return this.usageCount != null && this.usageCount > 0;
    }
}

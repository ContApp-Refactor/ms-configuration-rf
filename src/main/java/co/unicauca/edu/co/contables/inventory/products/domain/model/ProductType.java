package co.unicauca.edu.co.contables.inventory.products.domain.model;


import lombok.*;

/**
 * @brief Entidad que representa un tipo de producto
 *
 * Clasifica productos según su naturaleza o características específicas,
 * permitiendo agrupaciones lógicas para gestión y reporting.
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductType {

    private Long id;
    private String name;
    private String description;
    private String enterpriseId;

    @Builder.Default
    private boolean state = true;

    /**
     * Vendible: el tipo puede usarse en facturas de venta.
     */
    @Builder.Default
    private boolean sellable = true;

    /**
     * Comprable: el tipo puede usarse en facturas de compra.
     */
    @Builder.Default
    private boolean purchasable = true;

    /**
     * Inventariable: el tipo tiene uso en el kardex (control de entradas,
     * salidas y valoración según el sistema de inventario configurado).
     */
    @Builder.Default
    private boolean inventoryable = false;

}

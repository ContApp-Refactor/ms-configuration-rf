package co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * @brief DTO de solicitud para creación de categorías
 *
 * Contiene datos requeridos para crear nuevas categorías en el sistema,
 * incluyendo configuración contable y referencias a entidades relacionadas.
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryCreateRequest {

    @JsonIgnore
    private Long id;

    @NotBlank(message = "Nombre es requerido")
    private String name;

    @NotBlank(message = "Descripción es requerida")
    private String description;

    @NotBlank(message = "Id de la empresa es requerido")
    private String enterpriseId;

    @NotNull(message = "Id del inventario es requerido")
    private Long inventoryId;

    @NotNull(message = "Id del costo es requerido")
    private Long costId;

    @NotNull(message = "Id de la venta es requerido")
    private Long saleId;

    @NotNull(message = "Id de la devolución es requerido")
    private Long returnId;

    @NotNull(message = "Lista de impuestos es requerida")
    private List<Long> taxes;

    @JsonIgnore
    @Builder.Default
    private boolean state = true;

    
}

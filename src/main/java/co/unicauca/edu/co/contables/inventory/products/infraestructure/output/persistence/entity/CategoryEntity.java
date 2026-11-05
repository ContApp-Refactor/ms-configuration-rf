package co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.entity;

import java.util.List;

import org.hibernate.annotations.TenantId;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief Entidad JPA para persistencia de categorías
 *
 * Representa la tabla de categorías en base de datos con soporte para multitenancy,
 * incluyendo configuración contable y asociaciones con productos.
 */
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "category")
public class CategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private String enterpriseId;
    private Long inventoryId;
    private Long costId;
    private Long saleId;
    private Long returnId;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "category_taxes", joinColumns = @JoinColumn(name = "category_id"))
    @Column(name = "tax_id")
    private List<Long> taxes;
    private boolean state;

    @TenantId
    private String tenantId;
}

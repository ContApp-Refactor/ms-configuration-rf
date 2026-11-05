package co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.entity;

import java.time.Instant;

import org.hibernate.annotations.TenantId;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
/**
 * @brief Entidad JPA para persistencia de productos
 *
 * Representa la tabla de productos en base de datos con soporte para multitenancy,
 * incluyendo relaciones con categorías, tipos de producto y unidades de medida.
 */
@Getter
@Setter
@Entity(name = "InventoryProductEntity")
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "product")
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code;
    private String name;
    private String description;
    private Integer quantity;
    private Long unitOfMeasureId;
    private Long categoryId;
    private String enterpriseId;
    private double cost;

    private boolean state;

    private String reference;
    private Long productTypeId;
    private String presentation;
    
    private Integer usageCount;
    
    @UpdateTimestamp
    private Instant lastModifiedDate;

    @TenantId
    private String tenantId;

}

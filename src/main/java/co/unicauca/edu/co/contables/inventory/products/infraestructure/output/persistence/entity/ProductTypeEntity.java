package co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.entity;

import lombok.*;

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
 * @brief Entidad JPA para persistencia de tipos de producto
 *
 * Representa la tabla de tipos de producto en base de datos,
 * permitiendo clasificación lógica de productos por empresa.
 */
@Entity
@Table(name = "product_type")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;    
    private String description;
    private String enterpriseId;
    private boolean state;
}

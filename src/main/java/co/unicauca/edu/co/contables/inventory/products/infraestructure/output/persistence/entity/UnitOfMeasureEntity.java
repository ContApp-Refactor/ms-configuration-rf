package co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief Entidad JPA para persistencia de unidades de medida
 *
 * Representa la tabla de unidades de medida en base de datos,
 * definiendo abreviaturas y nombres para cuantificación de productos.
 */
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "unitOfMeasure")
public class UnitOfMeasureEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private String abbreviation;
    private String enterpriseId;
    private boolean state;

}

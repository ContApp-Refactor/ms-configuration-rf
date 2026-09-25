package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad JPA que representa una ciudad en la base de datos.
 * Mapea la tabla "city" y define las relaciones con otras entidades.
 */
@Entity(name = "EnterpriseCityEntity")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name="city")

public class CityEntity
{
    /**
     * Identificador único de la ciudad.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
   /**
    * Departamento al que pertenece la ciudad.
    */
   @ManyToOne
   //@JoinColumn(name="department_id",referencedColumnName = "id")
    private DepartmentEntity department;
    /**
     * Nombre de la ciudad.
     */
    private String name;
}

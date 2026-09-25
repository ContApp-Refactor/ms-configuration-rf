package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.repository;

import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.entity.PersonTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio JPA para la entidad PersonTypeEntity.
 * Proporciona métodos para realizar operaciones CRUD en la base de datos.
 */
public interface IPersonTypeRepository extends JpaRepository<PersonTypeEntity, Long> {
    // Métodos adicionales personalizados pueden ser definidos aquí
}

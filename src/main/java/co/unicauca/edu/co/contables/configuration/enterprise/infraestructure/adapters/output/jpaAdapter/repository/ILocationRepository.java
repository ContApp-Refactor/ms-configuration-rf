package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.repository;

import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.entity.LocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio JPA para la entidad LocationEntity.
 * Proporciona métodos para realizar operaciones CRUD en la base de datos.
 */
public interface ILocationRepository extends JpaRepository<LocationEntity, Long> {
    // Métodos adicionales pueden ser definidos aquí
}

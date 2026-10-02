package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.repository;

import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.entity.CountryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio JPA para la entidad CountryEntity.
 * Proporciona métodos para realizar operaciones CRUD en la base de datos.
 */
public interface ICountryRepository extends JpaRepository<CountryEntity, Long> {
    // Métodos adicionales personalizados pueden ser definidos aquí
}
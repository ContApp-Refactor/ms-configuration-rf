package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.repository;


import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.entity.TaxLiabilityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio JPA para la entidad TaxLiabilityEntity.
 * Proporciona métodos para realizar operaciones CRUD en la base de datos.
 */
public interface ITaxLiabilityRepository extends JpaRepository<TaxLiabilityEntity, Long> {
    // Métodos adicionales personalizados pueden ser definidos aquí
}

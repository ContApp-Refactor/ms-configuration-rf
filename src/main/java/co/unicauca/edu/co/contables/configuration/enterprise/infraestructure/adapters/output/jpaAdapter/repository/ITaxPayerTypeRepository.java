package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.repository;


import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.entity.TaxPayerTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio JPA para la entidad TaxPayerTypeEntity.
 * Proporciona métodos para realizar operaciones CRUD en la base de datos.
 */
public interface ITaxPayerTypeRepository extends JpaRepository<TaxPayerTypeEntity, Long> {
    // Métodos adicionales personalizados pueden ser definidos aquí
}

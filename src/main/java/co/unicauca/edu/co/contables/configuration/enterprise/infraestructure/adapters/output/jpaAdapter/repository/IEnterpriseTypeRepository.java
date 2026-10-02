package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.repository;

import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.entity.EnterpriseTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio JPA para la entidad EnterpriseTypeEntity.
 * Proporciona métodos para realizar operaciones CRUD en la base de datos.
 */
public interface IEnterpriseTypeRepository extends JpaRepository<EnterpriseTypeEntity, Long> {
    // Métodos adicionales personalizados pueden ser definidos aquí
}
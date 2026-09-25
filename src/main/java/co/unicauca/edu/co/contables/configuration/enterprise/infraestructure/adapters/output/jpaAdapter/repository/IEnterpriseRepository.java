package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.repository;

import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.entity.EnterpriseEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.projection.IEnterpriseInfoProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio JPA para la entidad EnterpriseEntity.
 * Proporciona métodos para realizar operaciones CRUD en la base de datos.
 */
public interface IEnterpriseRepository extends JpaRepository<EnterpriseEntity, UUID> {

    /**
     * Obtiene información básica de las empresas activas (state = 0).
     *
     * @return lista de proyecciones con información básica de empresas activas
     */
    @Query("SELECT e.id AS id, e.name AS name, e.nit AS nit, e.logo AS logo, e.state AS state, e.inventoryConfigurationType AS inventoryConfigurationType FROM EnterpriseEntity e WHERE e.state = 0")
    List<IEnterpriseInfoProjection> findEnterpriseInfo();

    /**
     * Verifica si existe una empresa con el NIT especificado.
     *
     * @param nit el NIT de la empresa
     * @return true si existe una empresa con el NIT, false en caso contrario
     */
    boolean existsByNit(String nit);

    /**
     * Obtiene información básica de las empresas inactivas (state = 1).
     *
     * @return lista de proyecciones con información básica de empresas inactivas
     */
    @Query("SELECT e.id AS id, e.name AS name, e.nit AS nit, e.logo AS logo, e.state AS state, e.inventoryConfigurationType AS inventoryConfigurationType FROM EnterpriseEntity e WHERE e.state = 1")
    List<IEnterpriseInfoProjection> findEnterpriseInfoInactive();
}

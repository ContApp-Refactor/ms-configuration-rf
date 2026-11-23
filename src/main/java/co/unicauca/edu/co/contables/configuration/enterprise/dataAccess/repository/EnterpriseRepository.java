package co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.repository;

import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.EnterpriseEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.projection.EnterpriseInfoProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface EnterpriseRepository extends JpaRepository<EnterpriseEntity, UUID> {

    @Query("SELECT e.id AS id, e.name AS name, e.nit AS nit, e.logo AS logo, e.state AS state, e.inventoryConfigurationType AS inventoryConfigurationType FROM EnterpriseEntity e WHERE e.state = 0")
    List<EnterpriseInfoProjection> findEnterpriseInfo();

    @Query("SELECT e.id AS id, e.name AS name, e.nit AS nit, e.logo AS logo, e.state AS state, e.inventoryConfigurationType AS inventoryConfigurationType FROM EnterpriseEntity e WHERE e.state = 1")
    List<EnterpriseInfoProjection> findEnterpriseInfoInactive();

    boolean existsByNit(String nit);

    /** NIT usado por OTRA empresa del tenant actual (restore sobre una empresa existente). */
    boolean existsByNitAndIdNot(String nit, UUID id);

    /** Logo compartido con OTRA empresa del tenant actual (no se borra el archivo si es así). */
    boolean existsByLogoAndIdNot(String logo, UUID id);

    /** Indica si alguna empresa (del tenant actual) tiene asignada la materia. */
    @Query("SELECT COUNT(e) > 0 FROM EnterpriseEntity e JOIN e.subjects s WHERE s.id = :subjectId")
    boolean existsBySubjectId(@Param("subjectId") UUID subjectId);

    @Query("SELECT e FROM EnterpriseEntity e WHERE LOWER(e.name) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(e.nit) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<EnterpriseEntity> searchByNameOrNit(@Param("q") String q);
}

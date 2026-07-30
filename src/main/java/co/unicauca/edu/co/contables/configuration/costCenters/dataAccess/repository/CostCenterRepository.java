package co.unicauca.edu.co.contables.configuration.costCenters.dataAccess.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

import co.unicauca.edu.co.contables.configuration.costCenters.dataAccess.entity.CostCenterEntity;

/**
 * @brief Repositorio para centros de costo
 *
 * Repositorio JPA que proporciona operaciones de acceso a datos para entidades
 * de centros de costo, incluyendo consultas personalizadas y paginación.
 */
public interface CostCenterRepository extends JpaRepository<CostCenterEntity, Long> {

    boolean existsByCodeAndIdEnterprise(String code, String idEnterprise);

    boolean existsByNameAndIdEnterprise(String name, String idEnterprise);

    boolean existsByNameAndIdEnterpriseAndIdNot(String name, String idEnterprise, Long id);

    Page<CostCenterEntity> findAllByIdEnterprise(String idEnterprise, Pageable pageable);

    Page<CostCenterEntity> findAllByIdEnterpriseAndStatus(String idEnterprise, Boolean status, Pageable pageable);

    Optional<CostCenterEntity> findByIdAndIdEnterprise(Long id, String idEnterprise);

    boolean existsByParentId(Long parentId);
   
    List<CostCenterEntity> findByParentId(Long parentId);

    List<CostCenterEntity> findByIdEnterpriseAndParentIsNullOrderByCode(String idEnterprise);

    /**
     * @brief Obtiene los centros de costo activos de último nivel (código >= 5 caracteres)
     * Filtra por empresa, estado activo y longitud de código
     * @param idEnterprise ID de la empresa
     * @param status Estado del centro de costo (true para activos)
     * @return Lista de centros de costo de último nivel ordenados por código
     */
    List<CostCenterEntity> findByIdEnterpriseAndStatusOrderByCode(String idEnterprise, Boolean status);

    long countByIdEnterprise(String idEnterprise);

    long countByIdEnterpriseAndStatus(String idEnterprise, Boolean status);

    /**
     * @brief Busca centros de costo por empresa y término de búsqueda en código o nombre (case-insensitive)
     * @param idEnterprise1 ID de la empresa para búsqueda por código
     * @param code Término de búsqueda en código
     * @param idEnterprise2 ID de la empresa para búsqueda por nombre
     * @param name Término de búsqueda en nombre
     * @param pageable Configuración de paginación
     * @return Página de centros de costo que coinciden con la búsqueda
     */
    Page<CostCenterEntity> findByIdEnterpriseAndCodeContainingIgnoreCaseOrIdEnterpriseAndNameContainingIgnoreCase(
            String idEnterprise1, String code, String idEnterprise2, String name, Pageable pageable);

    /**
     * @brief Cuenta centros de costo por empresa y término de búsqueda en código o nombre (case-insensitive)
     * @param idEnterprise1 ID de la empresa para búsqueda por código
     * @param code Término de búsqueda en código
     * @param idEnterprise2 ID de la empresa para búsqueda por nombre
     * @param name Término de búsqueda en nombre
     * @return Número total de centros de costo que coinciden con la búsqueda
     */
    long countByIdEnterpriseAndCodeContainingIgnoreCaseOrIdEnterpriseAndNameContainingIgnoreCase(
            String idEnterprise1, String code, String idEnterprise2, String name);

   
    List<CostCenterEntity> findAllByIdEnterprise(String idEnterprise);

    /**
     * @brief Obtiene todos los centros de costo filtrados por estado sin paginación
     * @param idEnterprise ID de la empresa
     * @param status Estado del centro de costo
     * @return Lista de centros de costo con el estado especificado
     */
    List<CostCenterEntity> findAllByIdEnterpriseAndStatus(String idEnterprise, Boolean status);

    /**
     * @brief Obtiene centros de costo auxiliares (último nivel) 
     * Filtra por: empresa, estado activo y código con longitud >= 5
     * @param idEnterprise ID de la empresa
     * @return Lista de centros de costo auxiliares ordenados por código
     */
    @Query("SELECT c FROM CostCenterEntity c WHERE c.idEnterprise = :idEnterprise " +
           "AND c.status = true AND LENGTH(c.code) >= 5 ORDER BY c.code ASC")
    List<CostCenterEntity> findAuxiliaryCostCenters(@Param("idEnterprise") String idEnterprise);
}



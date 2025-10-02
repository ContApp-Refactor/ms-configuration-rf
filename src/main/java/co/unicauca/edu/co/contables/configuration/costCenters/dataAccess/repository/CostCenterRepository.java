package co.unicauca.edu.co.contables.configuration.costCenters.dataAccess.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;
import java.util.Optional;

import co.unicauca.edu.co.contables.configuration.costCenters.dataAccess.entity.CostCenterEntity;


public interface CostCenterRepository extends JpaRepository<CostCenterEntity, Long>, JpaSpecificationExecutor<CostCenterEntity> {

    boolean existsByCodeAndIdEnterprise(String code, String idEnterprise);

    boolean existsByNameAndIdEnterprise(String name, String idEnterprise);

    boolean existsByNameAndIdEnterpriseAndIdNot(String name, String idEnterprise, Long id);

    Page<CostCenterEntity> findAllByIdEnterprise(String idEnterprise, Pageable pageable);

    Page<CostCenterEntity> findAllByIdEnterpriseAndStatus(String idEnterprise, Boolean status, Pageable pageable);

    Optional<CostCenterEntity> findByIdAndIdEnterprise(Long id, String idEnterprise);

    /**
     * Verifica si un centro de costo tiene centros de costo hijos
     * @param parentId ID del centro de costo padre
     * @return true si tiene hijos, false en caso contrario
     */
    boolean existsByParentId(Long parentId);

    /**
     * Obtiene todos los centros de costo hijos de un centro de costo padre
     * @param parentId ID del centro de costo padre
     * @return Lista de centros de costo hijos
     */
    List<CostCenterEntity> findByParentId(Long parentId);

    /**
     * Obtiene solo los centros de costo raíz (sin padre) ordenados por código
     * @param idEnterprise ID de la empresa
     * @return Lista de centros de costo raíz ordenados por código
     */
    List<CostCenterEntity> findByIdEnterpriseAndParentIsNullOrderByCode(String idEnterprise);

    /**
     * Obtiene los centros de costo activos de último nivel (código >= 5 caracteres)
     * Filtra por empresa, estado activo y longitud de código
     * @param idEnterprise ID de la empresa
     * @param status Estado del centro de costo (true para activos)
     * @return Lista de centros de costo de último nivel ordenados por código
     */
    List<CostCenterEntity> findByIdEnterpriseAndStatusOrderByCode(String idEnterprise, Boolean status);

    /**
     * Cuenta el total de centros de costo por empresa
     * @param idEnterprise ID de la empresa
     * @return Número total de centros de costo
     */
    long countByIdEnterprise(String idEnterprise);

    /**
     * Cuenta el total de centros de costo filtrados por estado
     * @param idEnterprise ID de la empresa
     * @param status Estado del centro de costo
     * @return Número total de centros de costo con el estado especificado
     */
    long countByIdEnterpriseAndStatus(String idEnterprise, Boolean status);

    /**
     * Busca centros de costo por empresa y término de búsqueda en código o nombre (case-insensitive)
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
     * Cuenta centros de costo por empresa y término de búsqueda en código o nombre (case-insensitive)
     * @param idEnterprise1 ID de la empresa para búsqueda por código
     * @param code Término de búsqueda en código
     * @param idEnterprise2 ID de la empresa para búsqueda por nombre
     * @param name Término de búsqueda en nombre
     * @return Número total de centros de costo que coinciden con la búsqueda
     */
    long countByIdEnterpriseAndCodeContainingIgnoreCaseOrIdEnterpriseAndNameContainingIgnoreCase(
            String idEnterprise1, String code, String idEnterprise2, String name);
}



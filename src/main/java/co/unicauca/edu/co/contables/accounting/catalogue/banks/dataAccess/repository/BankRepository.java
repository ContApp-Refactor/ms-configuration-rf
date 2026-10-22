package co.unicauca.edu.co.contables.accounting.catalogue.banks.dataAccess.repository;

import co.unicauca.edu.co.contables.accounting.catalogue.banks.dataAccess.entity.BankEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

/**
 * @brief Repositorio de datos para operaciones de bancos
 *
 * Proporciona métodos para consultas especializadas de bancos
 * con filtros por empresa, estado y búsqueda por código o nombre.
 */
public interface BankRepository extends JpaRepository<BankEntity, Long> {
    
    boolean existsByCodeAndIdEnterprise(String code, String idEnterprise);

    boolean existsByNameAndIdEnterprise(String name, String idEnterprise);

    boolean existsByCodeAndIdEnterpriseAndIdNot(String code, String idEnterprise, Long id);

    boolean existsByNameAndIdEnterpriseAndIdNot(String name, String idEnterprise, Long id);
    
    Optional<BankEntity> findByIdAndIdEnterprise(Long id, String idEnterprise);
    
    Page<BankEntity> findAllByIdEnterprise(String idEnterprise, Pageable pageable);
    
    Page<BankEntity> findAllByIdEnterpriseAndStatus(String idEnterprise, Boolean status, Pageable pageable);

    long countByIdEnterpriseAndStatus(String idEnterprise, Boolean status);

    long countByIdEnterprise(String idEnterprise);

    /**
     * @brief Busca bancos por código o nombre con búsqueda parcial
     * @param idEnterprise ID de la empresa
     * @param search Término de búsqueda que filtra por código o nombre
     * @param pageable Configuración de paginación
     * @return Página de bancos que coinciden con la búsqueda
     */
    @Query("SELECT b FROM BankEntity b WHERE b.idEnterprise = ?1 AND (LOWER(b.code) LIKE LOWER(CONCAT('%', ?2, '%')) OR LOWER(b.name) LIKE LOWER(CONCAT('%', ?2, '%')))")
    Page<BankEntity> findByIdEnterpriseAndSearch(String idEnterprise, String search, Pageable pageable);

    /**
     * @brief Cuenta bancos por código o nombre con búsqueda parcial
     * @param idEnterprise ID de la empresa
     * @param search Término de búsqueda que filtra por código o nombre
     * @return Cantidad total de bancos que coinciden con la búsqueda
     */
    @Query("SELECT COUNT(b) FROM BankEntity b WHERE b.idEnterprise = ?1 AND (LOWER(b.code) LIKE LOWER(CONCAT('%', ?2, '%')) OR LOWER(b.name) LIKE LOWER(CONCAT('%', ?2, '%')))")
    long countByIdEnterpriseAndSearch(String idEnterprise, String search);
}

package co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.dataAccess.repository;

import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.dataAccess.entity.PaymentMethodEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

/**
 * @brief Repositorio de datos para operaciones de métodos de pago
 *
 * Proporciona métodos para consultas especializadas de métodos de pago
 * con filtros por empresa, estado y búsqueda en relaciones contables.
 */
public interface PaymentMethodRepository extends JpaRepository<PaymentMethodEntity, Long> {    
   
    boolean existsByNameAndIdEnterprise(String name, String idEnterprise);
    
    boolean existsByNameAndIdEnterpriseAndIdNot(String name, String idEnterprise, Long id);
    
    @EntityGraph(attributePaths = "accountingAccount")
    Optional<PaymentMethodEntity> findByIdAndIdEnterprise(Long id, String idEnterprise);

    @EntityGraph(attributePaths = "accountingAccount")
    Page<PaymentMethodEntity> findAllByIdEnterprise(String idEnterprise, Pageable pageable);

    @EntityGraph(attributePaths = "accountingAccount")
    Page<PaymentMethodEntity> findAllByIdEnterpriseAndStatus(String idEnterprise, Boolean status, Pageable pageable);

    long countByIdEnterprise(String idEnterprise);

    long countByIdEnterpriseAndStatus(String idEnterprise, Boolean status);

    /**
     * @brief Busca métodos de pago por nombre, código contable o descripción
     * @param idEnterprise ID de la empresa
     * @param search Término de búsqueda que filtra por nombre del método de pago, código contable o descripción
     * @param pageable Configuración de paginación
     * @return Página de métodos de pago que coinciden con la búsqueda
     */
    @Query("SELECT pm FROM PaymentMethodEntity pm LEFT JOIN pm.accountingAccount ac WHERE pm.idEnterprise = ?1 AND (LOWER(pm.name) LIKE LOWER(CONCAT('%', ?2, '%')) OR LOWER(ac.code) LIKE LOWER(CONCAT('%', ?2, '%')) OR LOWER(ac.description) LIKE LOWER(CONCAT('%', ?2, '%')))")
    Page<PaymentMethodEntity> findByIdEnterpriseAndSearch(String idEnterprise, String search, Pageable pageable);

    /**
     * @brief Cuenta métodos de pago por nombre, código contable o descripción
     * @param idEnterprise ID de la empresa
     * @param search Término de búsqueda que filtra por nombre del método de pago, código contable o descripción
     * @return Cantidad total de métodos de pago que coinciden con la búsqueda
     */
    @Query("SELECT COUNT(pm) FROM PaymentMethodEntity pm LEFT JOIN pm.accountingAccount ac WHERE pm.idEnterprise = ?1 AND (LOWER(pm.name) LIKE LOWER(CONCAT('%', ?2, '%')) OR LOWER(ac.code) LIKE LOWER(CONCAT('%', ?2, '%')) OR LOWER(ac.description) LIKE LOWER(CONCAT('%', ?2, '%')))")
    long countByIdEnterpriseAndSearch(String idEnterprise, String search);

    boolean existsByAccountingAccountIdAndIdEnterprise(Long accountingAccountId, String idEnterprise);

}

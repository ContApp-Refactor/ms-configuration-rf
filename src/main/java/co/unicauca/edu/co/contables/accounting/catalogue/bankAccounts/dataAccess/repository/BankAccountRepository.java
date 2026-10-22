package co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.dataAccess.repository;

import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.dataAccess.entity.BankAccountEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

/**
 * @brief Repositorio de datos para operaciones de cuentas bancarias
 *
 * Proporciona métodos para consultas especializadas de cuentas bancarias
 * con filtros por empresa, estado, banco y tipo de cuenta.
 */
public interface BankAccountRepository extends JpaRepository<BankAccountEntity, Long> {
    
    boolean existsByAccountNumberAndIdEnterprise(Long accountNumber, String idEnterprise);
    
    boolean existsByAccountNumberAndIdEnterpriseAndIdNot(Long accountNumber, String idEnterprise, Long id);
    
    Optional<BankAccountEntity> findByIdAndIdEnterprise(Long id, String idEnterprise);
    
    Page<BankAccountEntity> findAllByIdEnterprise(String idEnterprise, Pageable pageable);

    Page<BankAccountEntity> findAllByIdEnterpriseAndStatus(String idEnterprise, Boolean status, Pageable pageable);

    long countByIdEnterprise(String idEnterprise);

    long countByIdEnterpriseAndStatus(String idEnterprise, Boolean status);

    Page<BankAccountEntity> findAllByIdEnterpriseAndBankId(String idEnterprise, Long bankId, Pageable pageable);

    Page<BankAccountEntity> findAllByIdEnterpriseAndAccountType(String idEnterprise, String accountType, Pageable pageable);

    /**
     * @brief Busca cuentas bancarias por número de cuenta con búsqueda parcial
     * @param idEnterprise Identificador de la empresa
     * @param search Término de búsqueda para filtrar por número de cuenta
     * @param pageable Configuración de paginación
     * @return Página de cuentas bancarias que coinciden con la búsqueda
     */
    @Query("SELECT ba FROM BankAccountEntity ba WHERE ba.idEnterprise = ?1 AND CAST(ba.accountNumber AS string) LIKE LOWER(CONCAT('%', ?2, '%'))")
    Page<BankAccountEntity> findByIdEnterpriseAndAccountNumberSearch(String idEnterprise, String search, Pageable pageable);

    /**
     * @brief Cuenta cuentas bancarias por número de cuenta con búsqueda parcial
     * @param idEnterprise Identificador de la empresa
     * @param search Término de búsqueda para filtrar por número de cuenta
     * @return Cantidad total de cuentas que coinciden con la búsqueda
     */
    @Query("SELECT COUNT(ba) FROM BankAccountEntity ba WHERE ba.idEnterprise = ?1 AND CAST(ba.accountNumber AS string) LIKE LOWER(CONCAT('%', ?2, '%'))")
    long countByIdEnterpriseAndAccountNumberSearch(String idEnterprise, String search);

    boolean existsByAccountingAccountIdAndIdEnterprise(Long accountingAccountId, String idEnterprise);

    long countByAccountingAccountIdAndIdEnterprise(Long accountingAccountId, String idEnterprise);

    /**
     * @brief Verifica si existe alguna cuenta bancaria del banco especificado que tenga movimientos registrados
     * @param bankId ID del banco
     * @param idEnterprise ID de la empresa
     * @return true si existe al menos una cuenta bancaria del banco con movimientos registrados
     */
    @Query("SELECT COUNT(ba) > 0 FROM BankAccountEntity ba WHERE ba.idEnterprise = ?2 AND ba.bank.id = ?1 AND ba.usageCount > 0")
    boolean existsByBankIdAndIdEnterpriseAndUsageCountGreaterThanZero(Long bankId, String idEnterprise);
}

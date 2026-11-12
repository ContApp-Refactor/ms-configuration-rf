package co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters.entity.TaxEntity;

import java.util.List;

/**
 * @brief Repositorio de datos para operaciones de impuestos
 *
 * Proporciona métodos para consultas especializadas de impuestos
 * con filtros por empresa, estado, código y descripción.
 */
public interface ITaxRepository extends JpaRepository<TaxEntity, Long> {

    /**
     * @brief Busca impuesto por código y empresa
     * @param code código del impuesto
     * @param idEnterprise ID de la empresa
     * @return impuesto encontrado o null
     */
    @Query("SELECT a FROM TaxEntity a WHERE a.code=?1 AND a.idEnterprise=?2")
    TaxEntity findByCode(String code, String idEnterprise);

    /**
     * @brief Busca impuesto por ID y empresa
     * @param id ID del impuesto
     * @param idEnterprise ID de la empresa
     * @return impuesto encontrado o null
     */
    @Query("SELECT a FROM TaxEntity a WHERE a.id=?1 AND a.idEnterprise=?2")
    TaxEntity findByIdAndIdEnterprise(Long id, String idEnterprise);

    /**
     * @brief Busca impuestos activos por empresa
     * @param idEnterprise ID de la empresa
     * @return lista de impuestos activos
     */
    @Query("SELECT a FROM TaxEntity a WHERE a.idEnterprise=?1 AND a.status = true")
    List<TaxEntity> findActiveByIdEnterprise(String idEnterprise);

    /**
     * @brief Busca todos los impuestos por empresa
     * @param idEnterprise ID de la empresa
     * @return lista completa de impuestos
     */
    @Query("SELECT a FROM TaxEntity a WHERE a.idEnterprise=?1")
    List<TaxEntity> findAllByIdEnterprise(String idEnterprise);

    /**
     * @brief Busca impuestos paginados por empresa
     * @param idEnterprise ID de la empresa
     * @param pageable configuración de paginación
     * @return página de impuestos
     */
    @Query("SELECT a FROM TaxEntity a WHERE a.idEnterprise=?1")
    Page<TaxEntity> findAllByIdEnterprise(String idEnterprise, Pageable pageable);

    /**
     * @brief Cuenta total de impuestos por empresa
     * @param idEnterprise ID de la empresa
     * @return cantidad total de impuestos
     */
    @Query("SELECT COUNT(a) FROM TaxEntity a WHERE a.idEnterprise=?1")
    long countByIdEnterprise(String idEnterprise);

    /**
     * @brief Busca impuestos por código o descripción con paginación
     * @param idEnterprise ID de la empresa
     * @param search término de búsqueda parcial (case-insensitive)
     * @param pageable configuración de paginación
     * @return página de impuestos filtrados
     */
    @Query("SELECT a FROM TaxEntity a WHERE a.idEnterprise=?1 AND (LOWER(a.code) LIKE LOWER(CONCAT('%', ?2, '%')) OR LOWER(a.description) LIKE LOWER(CONCAT('%', ?2, '%')))")
    Page<TaxEntity> findByIdEnterpriseAndDescriptionContainingIgnoreCase(String idEnterprise, String search, Pageable pageable);

    /**
     * @brief Cuenta impuestos filtrados por código o descripción
     * @param idEnterprise ID de la empresa
     * @param search término de búsqueda parcial (case-insensitive)
     * @return cantidad de impuestos filtrados
     */
    @Query("SELECT COUNT(a) FROM TaxEntity a WHERE a.idEnterprise=?1 AND (LOWER(a.code) LIKE LOWER(CONCAT('%', ?2, '%')) OR LOWER(a.description) LIKE LOWER(CONCAT('%', ?2, '%')))")
    long countByIdEnterpriseAndDescriptionContainingIgnoreCase(String idEnterprise, String search);

    /**
     * @brief Incrementa el contador de uso de un impuesto
     * @param taxId ID del impuesto
     * @param enterpriseId ID de la empresa
     */
    @Query("UPDATE TaxEntity t SET t.usageCount = t.usageCount + 1 WHERE t.id = ?1 AND t.idEnterprise = ?2")
    void incrementUsageCount(Long taxId, String enterpriseId);

    /**
     * @brief Verifica si existe algún impuesto que tenga la cuenta especificada como salesTax o purchaseTax
     * @param accountCode código de la cuenta contable
     * @param enterpriseId ID de la empresa
     * @return true si existe al menos un impuesto asociado a la cuenta
     */
    @Query("SELECT COUNT(t) > 0 FROM TaxEntity t WHERE t.idEnterprise = ?2 AND (t.salesTax.code = ?1 OR t.purchaseTax.code = ?1)")
    boolean existsBySalesTaxCodeOrPurchaseTaxCode(String accountCode, String enterpriseId);

    /**
     * @brief Lote de impuestos de la empresa con id mayor a {@code afterId} (keyset), para la copia de seguridad
     * @param idEnterprise ID de la empresa
     * @param afterId último id leído (0 para empezar)
     * @param pageable tamaño del lote (página 0)
     */
    @Query("SELECT t FROM TaxEntity t LEFT JOIN FETCH t.salesTax LEFT JOIN FETCH t.purchaseTax WHERE t.idEnterprise = ?1 AND t.id > ?2 ORDER BY t.id ASC")
    List<TaxEntity> findBackupBatch(String idEnterprise, Long afterId, Pageable pageable);

    /**
     * @brief Impuestos cuya cuenta de IVA ventas o compras no pertenece a la misma empresa (huérfanos tras un restore)
     * @param idEnterprise ID de la empresa
     * @return cantidad de impuestos con FK a cuentas de otra empresa
     */
    @Query("SELECT COUNT(t) FROM TaxEntity t WHERE t.idEnterprise = ?1 AND ("
            + "(t.salesTax IS NOT NULL AND t.salesTax.idEnterprise <> ?1) OR "
            + "(t.purchaseTax IS NOT NULL AND t.purchaseTax.idEnterprise <> ?1))")
    long countWithAccountOutsideEnterprise(String idEnterprise);

    /** @brief Restore en el lugar: borra (bulk) todos los impuestos de la empresa */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM TaxEntity t WHERE t.idEnterprise = ?1")
    int deleteAllByEnterprise(String idEnterprise);
}

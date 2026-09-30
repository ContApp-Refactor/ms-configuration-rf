package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.entity.AccountCatalogueEntity;

public interface IAccountCatalogueRepository extends JpaRepository<AccountCatalogueEntity, Long> {

    @Query("SELECT a FROM AccountCatalogueEntity a LEFT JOIN FETCH a.parent WHERE a.code = ?1 AND a.idEnterprise = ?2")
    AccountCatalogueEntity findByCode(String code, String idEnterprise);

    /**
     * @brief Busca cuenta por código cargando toda la jerarquía de padres para importación
     * @details Usa múltiples JOIN FETCH para cargar recursivamente todos los niveles de padres,
     * evitando LazyInitializationException durante procesamiento asíncrono batch.
     * Limitado a 4 niveles de profundidad (suficiente para catálogos contables estándar).
     * @param code código de la cuenta a buscar
     * @param idEnterprise ID de la empresa
     * @return entidad con toda la jerarquía de padres cargada eagerly
     */
    @Query("SELECT a FROM AccountCatalogueEntity a " +
           "LEFT JOIN FETCH a.parent p1 " +
           "LEFT JOIN FETCH p1.parent p2 " +
           "LEFT JOIN FETCH p2.parent p3 " +
           "LEFT JOIN FETCH p3.parent p4 " +
           "WHERE a.code = ?1 AND a.idEnterprise = ?2")
    AccountCatalogueEntity findByCodeWithFullHierarchy(String code, String idEnterprise);

    @Query("SELECT a FROM AccountCatalogueEntity a LEFT JOIN FETCH a.parent WHERE a.id = ?1 AND a.idEnterprise = ?2")
    AccountCatalogueEntity findByIdAndIdEnterprise(Long id, String idEnterprise);

    @Query("SELECT a FROM AccountCatalogueEntity a WHERE UPPER(a.description) = UPPER(?1) AND a.idEnterprise = ?2")
    AccountCatalogueEntity findByDescriptionIgnoreCaseAndIdEnterprise(String description, String idEnterprise);

    @Query("SELECT DISTINCT a FROM AccountCatalogueEntity a LEFT JOIN FETCH a.parent WHERE a.codeLength = 8 AND a.idEnterprise = ?1 AND a.status = true ORDER BY a.code ASC")
    List<AccountCatalogueEntity> findAuxiliaryAccountsByIdEnterprise(String idEnterprise);

    @Query("SELECT DISTINCT a FROM AccountCatalogueEntity a LEFT JOIN FETCH a.parent WHERE a.codeLength = 8 AND a.idEnterprise = ?1 AND a.status = true AND a.crossing = true ORDER BY a.code ASC")
    List<AccountCatalogueEntity> findAuxiliaryAccountsWithCrossingByIdEnterprise(String idEnterprise);

    /**
     * @brief Query recursiva nativa para encontrar todos los IDs de descendientes en jerarquía
     *
     * Utiliza CTE recursiva para navegar árbol jerárquico completo desde un nodo padre,
     * excluyendo el propio padre del resultado. Esencial para operaciones de eliminación jerárquica.
     * @param parentId ID del nodo padre para iniciar recursión
     * @param idEnterprise filtro de empresa para aislamiento de datos
     * @param tenantId filtro de tenant para multi-tenancy
     * @return lista completa de IDs de todos los descendientes directos e indirectos
     */
    @Query(value = "WITH RECURSIVE descendants AS ( " +
                   "SELECT id FROM account WHERE id = :parentId AND id_enterprise = :idEnterprise AND tenant_id = :tenantId " +
                   "UNION ALL " +
                   "SELECT ac.id FROM account ac INNER JOIN descendants d ON ac.parent_id = d.id WHERE ac.id_enterprise = :idEnterprise AND ac.tenant_id = :tenantId " +
                   ") SELECT id FROM descendants WHERE id != :parentId", nativeQuery = true)
    List<Long> findDescendantIds(@Param("parentId") Long parentId, @Param("idEnterprise") String idEnterprise, @Param("tenantId") String tenantId);

    @Modifying
    @Query("UPDATE AccountCatalogueEntity a SET a.status = :status WHERE a.id IN :ids AND a.idEnterprise = :idEnterprise AND a.tenantId = :tenantId")
    void updateStatusByIds(@Param("status") Boolean status, @Param("ids") List<Long> ids, @Param("idEnterprise") String idEnterprise, @Param("tenantId") String tenantId);

    @Modifying
    @Query("UPDATE AccountCatalogueEntity a SET a.status = :status WHERE a.code IN :codes AND a.idEnterprise = :idEnterprise AND a.tenantId = :tenantId")
    void updateStatusByCodes(@Param("status") Boolean status, @Param("codes") List<String> codes, @Param("idEnterprise") String idEnterprise, @Param("tenantId") String tenantId);

    @Query("SELECT a FROM AccountCatalogueEntity a WHERE a.idEnterprise = ?1 ORDER BY a.code ASC")
    Page<AccountCatalogueEntity> findAllByIdEnterpriseOrderByCode(String idEnterprise, Pageable pageable);

    @Query("SELECT a FROM AccountCatalogueEntity a WHERE a.idEnterprise = ?1 AND a.status = true ORDER BY a.code ASC")
    Page<AccountCatalogueEntity> findAllActiveByIdEnterpriseOrderByCode(String idEnterprise, Pageable pageable);

    @Query("SELECT a FROM AccountCatalogueEntity a WHERE a.idEnterprise = ?1 AND a.status = false ORDER BY a.code ASC")
    Page<AccountCatalogueEntity> findAllInactiveByIdEnterpriseOrderByCode(String idEnterprise, Pageable pageable);

    @Query("SELECT DISTINCT a FROM AccountCatalogueEntity a LEFT JOIN FETCH a.parent WHERE a.idEnterprise = :idEnterprise ORDER BY a.code ASC")
    Page<AccountCatalogueEntity> findAllByIdEnterpriseForExport(@Param("idEnterprise") String idEnterprise, Pageable pageable);

    @Query("SELECT DISTINCT a FROM AccountCatalogueEntity a LEFT JOIN FETCH a.parent WHERE a.idEnterprise = :idEnterprise AND a.status = true ORDER BY a.code ASC")
    Page<AccountCatalogueEntity> findAllActiveByIdEnterpriseForExport(@Param("idEnterprise") String idEnterprise, Pageable pageable);

    @Query("SELECT DISTINCT a FROM AccountCatalogueEntity a LEFT JOIN FETCH a.parent WHERE a.idEnterprise = :idEnterprise AND a.status = false ORDER BY a.code ASC")
    Page<AccountCatalogueEntity> findAllInactiveByIdEnterpriseForExport(@Param("idEnterprise") String idEnterprise, Pageable pageable);


    /**
     * @brief Query recursiva nativa para construir jerarquía completa de árbol de cuentas
     *
     * Utiliza CTE recursiva para navegar desde nodo raíz hacia abajo en la jerarquía,
     * devolviendo datos completos de cada nivel para construcción de árbol en memoria.
     * Retorna arrays con todos los campos necesarios para reconstruir estructura jerárquica.
     * @param code código de la cuenta raíz desde donde iniciar la recursión
     * @param idEnterprise filtro de empresa para aislamiento de datos
     * @return lista de Object[] con datos jerárquicos [id, code, description, parent_id, ...]
     */
    @Query(value = """
        WITH RECURSIVE hierarchy AS (
            SELECT id, code, description, parent_id, nature, financial_status, classification, crossing, cost_center, status, id_enterprise
            FROM account
            WHERE code = :code AND id_enterprise = :idEnterprise
            UNION ALL
            SELECT a.id, a.code, a.description, a.parent_id, a.nature, a.financial_status, a.classification, a.crossing, a.cost_center, a.status, a.id_enterprise
            FROM account a
            INNER JOIN hierarchy h ON a.parent_id = h.id
            WHERE a.id_enterprise = :idEnterprise
        )
        SELECT * FROM hierarchy
        """, nativeQuery = true)
    List<Object[]> findHierarchyByCode(@Param("code") String code, @Param("idEnterprise") String idEnterprise);

    @Query("SELECT DISTINCT a FROM AccountCatalogueEntity a LEFT JOIN FETCH a.parent WHERE a.idEnterprise = ?1 ORDER BY a.code ASC")
    List<AccountCatalogueEntity> findByIdEnterpriseOrderByCode(String idEnterprise);

    /**
     * @brief Búsqueda inteligente por código o descripción con LIKE y case-insensitive
     *
     * Combina búsqueda por código exacto con LIKE y búsqueda
     * case-insensitive por descripción usando UPPER. Útil para búsqueda global.
     * Incluye LEFT JOIN FETCH del parent para evitar LazyInitializationException.
     * @param idEnterprise filtro de empresa para aislamiento de datos
     * @param search término de búsqueda que puede coincidir con código o descripción
     * @return lista de cuentas que coinciden con el criterio de búsqueda
     */
    @Query("SELECT DISTINCT a FROM AccountCatalogueEntity a LEFT JOIN FETCH a.parent WHERE a.idEnterprise = ?1 AND (a.code LIKE %?2% OR UPPER(a.description) LIKE UPPER(CONCAT('%', ?2, '%'))) ORDER BY a.code ASC")
    List<AccountCatalogueEntity> findByIdEnterpriseAndCodeOrDescription(String idEnterprise, String search);

    @Query("SELECT a FROM AccountCatalogueEntity a WHERE a.parent.id = ?1 AND a.idEnterprise = ?2 ORDER BY a.code ASC")
    List<AccountCatalogueEntity> findByParentIdAndIdEnterprise(Long parentId, String idEnterprise);

    /**
     * @brief Busca múltiples cuentas por códigos en batch
     * @details Ejecuta una sola query con IN clause en lugar de N queries individuales
     * @param codes lista de códigos a buscar
     * @param idEnterprise ID de la empresa
     * @return lista de entidades encontradas
     */
    @Query("SELECT a FROM AccountCatalogueEntity a WHERE a.code IN :codes AND a.idEnterprise = :idEnterprise")
    List<AccountCatalogueEntity> findByCodesIn(@Param("codes") List<String> codes, @Param("idEnterprise") String idEnterprise);

    /**
     * @brief Busca múltiples cuentas por descripciones en batch
     * @details Ejecuta una sola query con IN clause case-insensitive
     * @param descriptions lista de descripciones a buscar
     * @param idEnterprise ID de la empresa
     * @return lista de entidades encontradas
     */
    @Query("SELECT a FROM AccountCatalogueEntity a WHERE UPPER(a.description) IN :descriptions AND a.idEnterprise = :idEnterprise")
    List<AccountCatalogueEntity> findByDescriptionsInIgnoreCase(@Param("descriptions") List<String> descriptions, @Param("idEnterprise") String idEnterprise);

    /**
     * @brief Incrementa el contador de uso de forma atómica
     * @details Usa UPDATE nativo para evitar race conditions
     * @param id ID de la cuenta
     * @return número de filas actualizadas
     */
    @Modifying
    @Query("UPDATE AccountCatalogueEntity a SET a.usageCount = COALESCE(a.usageCount, 0) + 1 WHERE a.id = :id")
    int incrementUsageCount(@Param("id") Long id);
}

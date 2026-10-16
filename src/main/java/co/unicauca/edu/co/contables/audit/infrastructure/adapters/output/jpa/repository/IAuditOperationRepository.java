package co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.jpa.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.jpa.entity.AuditOperationEntity;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.jpa.projection.ModuleTableProjection;

@Repository
public interface IAuditOperationRepository extends JpaRepository<AuditOperationEntity, Long> {

    Optional<AuditOperationEntity> findByRegisterIdAndAffectedTable(
            String registerId, String affectedTable);

    @Query("""
            SELECT DISTINCT a.moduleName AS moduleName, a.affectedTable AS affectedTable
            FROM AuditOperationEntity a
            WHERE a.enterpriseId = :enterpriseId
            """)
    List<ModuleTableProjection> findDistinctModulesAndTables(String enterpriseId);

    @Query(value = """
            SELECT *
            FROM audit_operation ao
            WHERE
                (CAST(:enterpriseId AS text) IS NULL OR ao.enterprise_id = :enterpriseId)
                AND (CAST(:dateFrom AS timestamp) IS NULL OR ao.operation_at >= CAST(:dateFrom AS timestamp))
                AND (CAST(:dateTo AS timestamp) IS NULL OR ao.operation_at <= CAST(:dateTo AS timestamp))
                AND (CAST(:moduleName AS text) IS NULL OR ao.module_name = :moduleName)
                AND (CAST(:affectedTable AS text) IS NULL OR ao.affected_table = :affectedTable)
                AND (CAST(:userName AS text) IS NULL OR LOWER(ao.user_name) LIKE LOWER(CONCAT(:userName, '%')))
                AND (CAST(:operationType AS text) IS NULL OR ao.operation_type = :operationType)
                AND (
                    CAST(:userRole AS text) IS NULL
                    OR EXISTS (
                        SELECT 1
                        FROM jsonb_array_elements_text(ao.user_role) AS role
                        WHERE LOWER(role) LIKE LOWER(CONCAT('%', :userRole, '%'))
                    )
                )
            """, countQuery = """
            SELECT COUNT(*)
            FROM audit_operation ao
            WHERE
                (CAST(:enterpriseId AS text) IS NULL OR ao.enterprise_id = :enterpriseId)
                AND (CAST(:dateFrom AS timestamp) IS NULL OR ao.operation_at >= CAST(:dateFrom AS timestamp))
                AND (CAST(:dateTo AS timestamp) IS NULL OR ao.operation_at <= CAST(:dateTo AS timestamp))
                AND (CAST(:moduleName AS text) IS NULL OR ao.module_name = :moduleName)
                AND (CAST(:affectedTable AS text) IS NULL OR ao.affected_table = :affectedTable)
                AND (CAST(:userName AS text) IS NULL OR LOWER(ao.user_name) LIKE LOWER(CONCAT(:userName, '%')))
                AND (CAST(:operationType AS text) IS NULL OR ao.operation_type = :operationType)
                AND (
                    CAST(:userRole AS text) IS NULL
                    OR EXISTS (
                        SELECT 1
                        FROM jsonb_array_elements_text(ao.user_role) AS role
                        WHERE LOWER(role) LIKE LOWER(CONCAT('%', :userRole, '%'))
                    )
                )
            """, nativeQuery = true)
    Page<AuditOperationEntity> findByFilters(
            @Param("enterpriseId") String enterpriseId,
            @Param("dateFrom") Instant dateFrom,
            @Param("dateTo") Instant dateTo,
            @Param("moduleName") String moduleName,
            @Param("affectedTable") String affectedTable,
            @Param("userName") String userName,
            @Param("operationType") String operationType,
            @Param("userRole") String userRole,
            Pageable pageable);

    @Query(value = """
            SELECT *
            FROM audit_operation ao
            WHERE
                (CAST(:enterpriseId AS text) IS NULL OR ao.enterprise_id = :enterpriseId)
                AND (CAST(:dateFrom AS timestamp) IS NULL OR ao.operation_at >= CAST(:dateFrom AS timestamp))
                AND (CAST(:dateTo AS timestamp) IS NULL OR ao.operation_at <= CAST(:dateTo AS timestamp))
                AND (CAST(:moduleName AS text) IS NULL OR ao.module_name = :moduleName)
                AND (CAST(:affectedTable AS text) IS NULL OR ao.affected_table = :affectedTable)
                AND (CAST(:userName AS text) IS NULL OR LOWER(ao.user_name) LIKE LOWER(CONCAT(:userName, '%')))
                AND (CAST(:operationType AS text) IS NULL OR ao.operation_type = :operationType)
                AND (
                    CAST(:userRole AS text) IS NULL
                    OR EXISTS (
                        SELECT 1
                        FROM jsonb_array_elements_text(ao.user_role) AS role
                        WHERE LOWER(role) LIKE LOWER(CONCAT('%', :userRole, '%'))
                    )
                )
            ORDER BY ao.operation_at DESC
            """, nativeQuery = true)
    List<AuditOperationEntity> findAllForExport(
            @Param("enterpriseId") String enterpriseId,
            @Param("dateFrom") Instant dateFrom,
            @Param("dateTo") Instant dateTo,
            @Param("moduleName") String moduleName,
            @Param("affectedTable") String affectedTable,
            @Param("userName") String userName,
            @Param("operationType") String operationType,
            @Param("userRole") String userRole);

}

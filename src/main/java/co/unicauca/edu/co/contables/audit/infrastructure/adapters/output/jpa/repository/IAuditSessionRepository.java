package co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.jpa.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.jpa.entity.AuditSessionEntity;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.jpa.projection.SessionProjection;

@Repository
public interface IAuditSessionRepository
        extends JpaRepository<AuditSessionEntity, Long>,
        JpaSpecificationExecutor<AuditSessionEntity> {

    Optional<AuditSessionEntity> findBySessionId(String sessionId);

    @Query(value = """
            SELECT
                login.session_id as sessionId,
                login.user_name as userName,
                login.user_role as userRole,
                login.action_at as loginTime,
                logout.action_at as logoutTime
            FROM audit_session login
            LEFT JOIN audit_session logout
                ON login.session_id = logout.session_id
                AND logout.action = 'LOGOUT'
            WHERE login.action = 'LOGIN'
                AND login.action_at >= :dateFrom
                AND login.action_at <= :dateTo
                AND (:userName IS NULL OR login.user_name ILIKE CONCAT(:userName, '%'))
                AND (
                    :userRole IS NULL
                    OR EXISTS (
                        SELECT 1
                        FROM jsonb_array_elements_text(login.user_role::jsonb) AS role
                        WHERE LOWER(role) LIKE LOWER(CONCAT('%', :userRole, '%'))
                    )
                )
            /*#sortBy*/
            """, countQuery = """
            SELECT COUNT(*)
            FROM audit_session
            WHERE action = 'LOGIN'
                AND action_at >= :dateFrom
                AND action_at <= :dateTo
                AND (:userName IS NULL OR user_name ILIKE CONCAT(:userName, '%'))
            """, nativeQuery = true)
    Page<SessionProjection> findCombinedSessions(
            @Param("dateFrom") Instant dateFrom,
            @Param("dateTo") Instant dateTo,
            @Param("userName") String userName,
            @Param("userRole") String userRole,
            Pageable pageable);

    @Query(value = """
            SELECT COUNT(*)
            FROM audit_session
            WHERE action = 'LOGIN'
                AND action_at >= :dateFrom
                AND action_at <= :dateTo
                AND (:userName IS NULL OR user_name ILIKE CONCAT(:userName, '%'))
                AND (
                    :userRole IS NULL
                    OR EXISTS (
                        SELECT 1
                        FROM jsonb_array_elements_text(user_role::jsonb) AS role
                        WHERE LOWER(role) LIKE LOWER(CONCAT('%', :userRole, '%'))
                    )
                )
            """, nativeQuery = true)
    long countCombinedSessions(
            @Param("dateFrom") Instant dateFrom,
            @Param("dateTo") Instant dateTo,
            @Param("userName") String userName,
            @Param("userRole") String userRole);

    @Query(value = """
            SELECT
                s.session_id AS sessionId,
                s.user_name AS userName,
                s.user_role AS userRole,
                MIN(CASE WHEN s.action = 'LOGIN' THEN s.action_at END) AS loginTime,
                MAX(CASE WHEN s.action = 'LOGOUT' THEN s.action_at END) AS logoutTime
            FROM audit_session s
            WHERE
                (CAST(:dateFrom AS TIMESTAMP) IS NULL OR s.action_at >= :dateFrom)
                AND (CAST(:dateTo AS TIMESTAMP) IS NULL OR s.action_at <= :dateTo)
                AND (
                    CAST(:userName AS VARCHAR) IS NULL
                    OR LOWER(s.user_name) LIKE LOWER(CONCAT('%', :userName, '%'))
                )
                AND (
                    CAST(:userRole AS VARCHAR) IS NULL
                    OR EXISTS (
                        SELECT 1
                        FROM jsonb_array_elements_text(s.user_role::jsonb) AS role
                        WHERE LOWER(role) LIKE LOWER(CONCAT('%', :userRole, '%'))
                    )
                )
            GROUP BY s.session_id, s.user_name, s.user_role

            HAVING MIN(
                CASE WHEN s.action = 'LOGIN'
                THEN s.action_at
                END
            ) IS NOT NULL

            ORDER BY loginTime DESC
            """, nativeQuery = true)
    List<SessionProjection> findAllForExport(
            @Param("dateFrom") Instant dateFrom,
            @Param("dateTo") Instant dateTo,
            @Param("userName") String userName,
            @Param("userRole") String userRole);
}

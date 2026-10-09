package co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.jpa.adapter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import co.unicauca.edu.co.contables.audit.application.internal.DocumentSummaryProjection;
import co.unicauca.edu.co.contables.audit.application.internal.query.AuditDocumentCriteria;
import co.unicauca.edu.co.contables.audit.application.internal.query.AuditDocumentExportCriteria;
import co.unicauca.edu.co.contables.audit.application.internal.query.PageResult;
import co.unicauca.edu.co.contables.audit.application.internal.query.QueryOptions;
import co.unicauca.edu.co.contables.audit.application.port.output.AuditDocumentEventQueryPort;
import co.unicauca.edu.co.contables.audit.domain.enums.AuditDateType;
import co.unicauca.edu.co.contables.audit.domain.model.AuditDocumentEvent;
import co.unicauca.edu.co.contables.audit.domain.port.output.AuditDocumentEventRepositoryPort;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.jpa.entity.AuditDocumentEventEntity;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.jpa.mapper.AuditDocumentEventJpaMapper;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.jpa.repository.IAuditDocumentEventRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Repository
@RequiredArgsConstructor
@Slf4j
public class AuditDocumentEventRepositoryAdapter implements AuditDocumentEventQueryPort,
        AuditDocumentEventRepositoryPort {

    private final IAuditDocumentEventRepository jpaRepository;
    private final AuditDocumentEventJpaMapper mapper;
    private final EntityManager entityManager;

    private static final String BASE_SUMMARY_SQL = """
            SELECT
                e.document_code,
                e.document_type,
                e.third_party_name,
                e.document_date,
                creator.user_name    AS created_by,
                latest.user_name     AS last_modified_by,
                latest.operation_at  AS last_modified_at
            FROM audit_document_event e
            JOIN audit_document_event creator
                ON creator.document_code  = e.document_code
                AND creator.enterprise_id = e.enterprise_id
                AND creator.operation_type = 'CREATE'
            JOIN audit_document_event latest
                ON latest.id = (
                    SELECT id FROM audit_document_event sub
                    WHERE sub.document_code = e.document_code
                    AND sub.enterprise_id = e.enterprise_id
                    ORDER BY sub.operation_at DESC, sub.id DESC
                    LIMIT 1
                )
            WHERE e.enterprise_id = :enterpriseId
                """;
    private static final String GROUP_BY_CLAUSE = """
            GROUP BY
                e.document_code, e.document_type, e.third_party_name,
                e.document_date, creator.user_name,
                latest.user_name, latest.operation_at
            """;

    // Puerto de dominio
    @Override
    public AuditDocumentEvent save(AuditDocumentEvent event) {
        AuditDocumentEventEntity entity = mapper.toEntity(event);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<AuditDocumentEvent> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    // Puerto de aplicacion
    @Override
    @SuppressWarnings("unchecked")
    public PageResult<DocumentSummaryProjection> findDocumentSummaries(AuditDocumentCriteria criteria,
            QueryOptions options) {
        StringBuilder sql = new StringBuilder(BASE_SUMMARY_SQL);
        Map<String, Object> params = new HashMap<>();
        params.put("enterpriseId", criteria.getEnterpriseId());
        applyDateFilter(sql, criteria, params);
        applyCreatedByFilter(sql, criteria, params);
        applyDocumentTypeFilter(sql, criteria, params);
        applyDocumentCodeFilter(sql, criteria, params);
        applyThirdPartyFilter(sql, criteria, params);

        sql.append(" ").append(GROUP_BY_CLAUSE);

        String countSql = "SELECT COUNT(*) FROM (" + sql + ") AS sub";
        Query countQuery = entityManager.createNativeQuery(countSql);
        params.forEach(countQuery::setParameter);
        long total = ((Number) countQuery.getSingleResult()).longValue();

        sql.append(" ORDER BY ").append(resolveSortField(options))
                .append(" ").append(options.getSortDirection())
                .append(" LIMIT :size OFFSET :offset");
        params.put("size", options.getSize());
        params.put("offset", options.getPage() * options.getSize());

        Query query = entityManager.createNativeQuery(sql.toString());
        params.forEach(query::setParameter);

        List<DocumentSummaryProjection> results = mapToProjection(query.getResultList());
        return new PageResult<>(results, total);
    }

    @Override
    public List<AuditDocumentEvent> findEventsByDocumentCode(String enterpriseId, String documentCode) {
        return jpaRepository.findByEnterpriseIdAndDocumentCodeOrderByOperationAtAsc(
                enterpriseId, documentCode)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<DocumentSummaryProjection> findAllDocumentSummariesForExport(
            AuditDocumentCriteria criteria) {

        StringBuilder sql = new StringBuilder(BASE_SUMMARY_SQL);
        Map<String, Object> params = new HashMap<>();
        params.put("enterpriseId", criteria.getEnterpriseId());

        applyDateFilter(sql, criteria, params);
        applyCreatedByFilter(sql, criteria, params);
        applyDocumentTypeFilter(sql, criteria, params);
        applyDocumentCodeFilter(sql, criteria, params);
        applyThirdPartyFilter(sql, criteria, params);

        sql.append(" ").append(GROUP_BY_CLAUSE);

        Query query = entityManager.createNativeQuery(sql.toString());
        params.forEach(query::setParameter);

        return mapToProjection(query.getResultList());
    }

    @Override
    public List<AuditDocumentEvent> findAllEventsForExport(AuditDocumentExportCriteria criteria) {

        StringBuilder sql = new StringBuilder("""
                SELECT * FROM audit_document_event e
                WHERE e.enterprise_id = :enterpriseId
                """);

        Map<String, Object> params = new HashMap<>();
        params.put("enterpriseId", criteria.getEnterpriseId());

        if (criteria.hasDateRange()) {
            boolean useDocumentDate = criteria.getDateType() == AuditDateType.DOCUMENT_DATE;
            if (criteria.getDateFrom() != null) {
                if (useDocumentDate) {
                    sql.append(" AND e.document_date >= :dateFrom");
                } else {
                    sql.append(" AND e.operation_at >= :dateFrom");
                }
                params.put("dateFrom", criteria.getDateFrom());
            }
            if (criteria.getDateTo() != null) {
                if (useDocumentDate) {
                    sql.append(" AND e.document_date <= :dateTo");
                } else {
                    sql.append(" AND e.operation_at <= :dateTo");
                }
                params.put("dateTo", criteria.getDateTo());
            }
        }

        if (criteria.hasDocumentType()) {
            sql.append(" AND e.document_type = :documentType");
            params.put("documentType", criteria.getDocumentType());
        }

        if (criteria.hasDocumentCode()) {
            sql.append(" AND LOWER(e.document_code) LIKE LOWER(:documentCode)");
            params.put("documentCode", "%" + criteria.getDocumentCode().trim() + "%");
        }

        if (criteria.hasThirdPartyName()) {
            sql.append(" AND LOWER(e.third_party_name) LIKE LOWER(:thirdPartyName)");
            params.put("thirdPartyName", "%" + criteria.getThirdPartyName().trim() + "%");
        }

        if (criteria.hasOperationType()) {
            sql.append(" AND e.operation_type = :operationType");
            params.put("operationType", criteria.getOperationType().name());
        }

        if (criteria.hasUserName()) {
            sql.append(" AND LOWER(e.user_name) LIKE LOWER(:userName)");
            params.put("userName", "%" + criteria.getUserName().trim() + "%");
        }
        sql.append(" ORDER BY e.document_code ASC, e.operation_at ASC");

        Query query = entityManager.createNativeQuery(sql.toString(), AuditDocumentEventEntity.class);
        params.forEach(query::setParameter);

        @SuppressWarnings("unchecked")
        List<AuditDocumentEventEntity> entities = query.getResultList();
        return entities.stream().map(mapper::toDomain).toList();
    }

    @Override
    public long countDistinctDocuments(AuditDocumentCriteria criteria) {

        StringBuilder sql = new StringBuilder("""
                    SELECT COUNT(DISTINCT e.document_code)

                    FROM audit_document_event e

                    JOIN audit_document_event creator
                        ON creator.document_code = e.document_code
                        AND creator.enterprise_id = e.enterprise_id
                        AND creator.operation_type = 'CREATE'

                    JOIN audit_document_event latest
                        ON latest.id = (
                            SELECT sub.id
                            FROM audit_document_event sub
                            WHERE sub.document_code = e.document_code
                            AND sub.enterprise_id = e.enterprise_id
                            ORDER BY sub.operation_at DESC, sub.id DESC
                            LIMIT 1
                        )

                    WHERE e.enterprise_id = :enterpriseId
                """);

        Map<String, Object> params = new HashMap<>();
        params.put("enterpriseId", criteria.getEnterpriseId());

        applyDateFilter(sql, criteria, params);
        applyCreatedByFilter(sql, criteria, params);
        applyDocumentTypeFilter(sql, criteria, params);
        applyDocumentCodeFilter(sql, criteria, params);
        applyThirdPartyFilter(sql, criteria, params);

        Query query = entityManager.createNativeQuery(sql.toString());

        params.forEach(query::setParameter);

        return ((Number) query.getSingleResult()).longValue();
    }

    private String resolveSortField(QueryOptions options) {
        return switch (options.getSortField()) {
            case "documentDate" -> "e.document_date";
            case "lastModifiedAt" -> "latest.operation_at";
            case "documentCode" -> "e.document_code";
            default -> "latest.operation_at";
        };
    }

    private void applyCreatedByFilter(StringBuilder sql, AuditDocumentCriteria criteria,
            Map<String, Object> params) {
        if (!criteria.hasCreatedBy())
            return;
        sql.append(" AND LOWER(creator.user_name) LIKE LOWER(:createdBy)");
        params.put("createdBy", "%" + criteria.getCreatedBy().trim() + "%");
    }

    private void applyDateFilter(StringBuilder sql, AuditDocumentCriteria criteria,
            Map<String, Object> params) {
        if (!criteria.hasDateRange())
            return;

        if (criteria.getDateType() == AuditDateType.DOCUMENT_DATE) {
            if (criteria.getDateFrom() != null) {
                sql.append(" AND e.document_date >= :dateFrom");
                params.put("dateFrom", criteria.getDateFrom()
                        .atZone(ZoneId.of("America/Bogota")).toLocalDate());
            }
            if (criteria.getDateTo() != null) {
                sql.append(" AND e.document_date <= :dateTo");
                params.put("dateTo", criteria.getDateTo()
                        .atZone(ZoneId.of("America/Bogota")).toLocalDate());
            }
        } else {
            if (criteria.getDateFrom() != null) {
                sql.append(" AND latest.operation_at >= :dateFrom");
                params.put("dateFrom", criteria.getDateFrom());
            }
            if (criteria.getDateTo() != null) {
                sql.append(" AND latest.operation_at <= :dateTo");
                params.put("dateTo", criteria.getDateTo());
            }
        }
    }

    private void applyDocumentTypeFilter(StringBuilder sql, AuditDocumentCriteria criteria,
            Map<String, Object> params) {
        if (!criteria.hasDocumentType())
            return;
        sql.append(" AND e.document_type = :documentType");
        params.put("documentType", criteria.getDocumentType());
    }

    private void applyDocumentCodeFilter(StringBuilder sql, AuditDocumentCriteria criteria,
            Map<String, Object> params) {
        if (!criteria.hasDocumentCode())
            return;

        sql.append(" AND LOWER(e.document_code) LIKE LOWER(:documentCode)");

        params.put(
                "documentCode",
                "%" + criteria.getDocumentCode().trim() + "%");
    }

    private void applyThirdPartyFilter(StringBuilder sql, AuditDocumentCriteria criteria, Map<String, Object> params) {
        if (!criteria.hasThirdPartyName())
            return;
        sql.append(" AND LOWER(e.third_party_name) LIKE LOWER(:thirdPartyName)");
        params.put("thirdPartyName", "%" + criteria.getThirdPartyName().trim() + "%");
    }

    private List<DocumentSummaryProjection> mapToProjection(List<Object[]> rows) {
        return rows.stream()
                .map(row -> DocumentSummaryProjection.of(
                        (String) row[0], // document_code
                        (String) row[1], // document_type
                        (String) row[2], // third_party_name
                        row[3] != null ? convertToLocalDate(row[3]) : null,
                        (String) row[4], // created_by
                        (String) row[5], // last_modified_by
                        row[6] != null
                                ? convertToInstant(row[6])
                                : null))
                .toList();
    }

    private LocalDate convertToLocalDate(Object value) {
        if (value instanceof java.sql.Date d) {
            return d.toLocalDate();
        }
        if (value instanceof LocalDate ld) {
            return ld;
        }
        throw new IllegalArgumentException("Unsupported date type: " + value.getClass());
    }

    private Instant convertToInstant(Object value) {
        if (value instanceof java.sql.Timestamp ts) {
            return ts.toInstant();
        }
        if (value instanceof Instant instant) {
            return instant;
        }
        if (value instanceof java.time.OffsetDateTime odt) {
            return odt.toInstant();
        }
        throw new IllegalArgumentException("Unsupported date type: " + value.getClass());
    }
}

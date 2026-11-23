package co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.jpa.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.audit.application.internal.CombinedSession;
import co.unicauca.edu.co.contables.audit.application.internal.query.AuditSessionCriteria;
import co.unicauca.edu.co.contables.audit.application.internal.query.PageResult;
import co.unicauca.edu.co.contables.audit.application.internal.query.QueryOptions;
import co.unicauca.edu.co.contables.audit.application.port.output.AuditSessionQueryPort;
import co.unicauca.edu.co.contables.audit.domain.model.AuditSession;
import co.unicauca.edu.co.contables.audit.domain.port.output.AuditSessionRepositoryPort;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.jpa.entity.AuditSessionEntity;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.jpa.mapper.AuditSessionJpaMapper;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.jpa.projection.SessionProjection;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.jpa.repository.IAuditSessionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditSessionRepositoryAdapter implements AuditSessionQueryPort,
        AuditSessionRepositoryPort {

    private final IAuditSessionRepository auditSessionRepository;
    private final AuditSessionJpaMapper mapper;
    private final ObjectMapper objectMapper;

    @Override
    public AuditSession save(AuditSession auditSession) {
        AuditSessionEntity entity = mapper.toEntity(auditSession);
        AuditSessionEntity saved = auditSessionRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<AuditSession> findBySessionId(String sessionId) {
        return auditSessionRepository.findBySessionId(sessionId)
                .map(mapper::toDomain);
    }

    @Override
    public PageResult<CombinedSession> findCombinedSessions(AuditSessionCriteria filter, QueryOptions options) {
        String sanitizedUserName = sanitizeUserName(filter.getUserName());
        Sort sort = buildSortForNativeQuery(options);
        Pageable pageable = PageRequest.of(
                options.getPage(),
                options.getSize(),
                sort);
        Page<SessionProjection> page = auditSessionRepository.findCombinedSessions(
                filter.getDateFrom(),
                filter.getDateTo(),
                sanitizedUserName,
                filter.getUserRole() != null ? filter.getUserRole() : null,
                pageable);
        List<CombinedSession> sessions = page.getContent().stream()
                .map(this::projectionToDomain)
                .toList();
        return new PageResult<>(sessions, page.getTotalElements());
    }

    private Sort buildSortForNativeQuery(QueryOptions options) {
        String sortField = options.getSortField() != null ? options.getSortField() : "loginTime";
        String sortDirection = options.getSortDirection() != null ? options.getSortDirection() : "DESC";

        Set<String> allowedFields = Set.of("loginTime", "logoutTime", "userName");
        if (!allowedFields.contains(sortField)) {
            sortField = "loginTime";
        }

        if (!"ASC".equalsIgnoreCase(sortDirection) && !"DESC".equalsIgnoreCase(sortDirection)) {
            sortDirection = "DESC";
        }

        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return JpaSort.unsafe(direction, sortField);
    }

    private CombinedSession projectionToDomain(SessionProjection projection) {
        List<String> roles = parseRolesFromJson(projection.getUserRole());
        return CombinedSession.of(
                projection.getSessionId(),
                projection.getUserName(),
                roles,
                projection.getLoginTime(),
                projection.getLogoutTime());
    }

    private String sanitizeUserName(String userName) {
        if (userName == null || userName.trim().isEmpty()) {
            return null;
        }
        String sanitized = userName.trim();
        if (sanitized.length() < 3) {
            throw new IllegalArgumentException("userName filter must be at least 3 characters");
        }
        sanitized = sanitized.replace("_", "\\_").replace("%", "\\%");
        return sanitized;
    }

    private List<String> parseRolesFromJson(String userRoleJson) {
        if (userRoleJson == null || userRoleJson.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(userRoleJson,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (Exception e) {
            log.warn("Could not parse user roles from JSON: {}", userRoleJson);
            return List.of();
        }
    }

    @Override
    public long countByCriteria(AuditSessionCriteria filter) {
        return auditSessionRepository.countCombinedSessions(
                filter.getDateFrom(),
                filter.getDateTo(),
                sanitizeUserName(filter.getUserName()),
                filter.getUserRole() != null ? filter.getUserRole() : null);
    }

    @Override
    public List<CombinedSession> findAllForExport(AuditSessionCriteria criteria) {
        return auditSessionRepository.findAllForExport(
                criteria.getDateFrom(),
                criteria.getDateTo(),
                sanitizeUserName(criteria.getUserName()),
                criteria.getUserRole() != null ? criteria.getUserRole() : null)
                .stream()
                .map(this::projectionToDomain)
                .toList();
    }
}

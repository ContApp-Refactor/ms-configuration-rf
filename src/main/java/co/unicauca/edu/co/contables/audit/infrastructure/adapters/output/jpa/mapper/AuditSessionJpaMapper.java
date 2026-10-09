package co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.jpa.mapper;


import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.audit.domain.model.AuditSession;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.jpa.entity.AuditSessionEntity;

@Component
public class AuditSessionJpaMapper {

    public AuditSession toDomain(AuditSessionEntity entity) {
        return AuditSession.reconstruct(
                entity.getId(),
                entity.getSessionId(),
                entity.getUserId(),
                entity.getUserName(),
                entity.getUserRole(),
                entity.getAction(),
                entity.getActionAt(),
                entity.getIpAddress(),
                entity.getCreatedAt());
    }

    public AuditSessionEntity toEntity(AuditSession domain) {
        return AuditSessionEntity.builder()
                .id(domain.getId())
                .sessionId(domain.getSessionId())
                .userId(domain.getUserId())
                .userName(domain.getUserName())
                .userRole(domain.getUserRole())
                .action(domain.getAction())
                .actionAt(domain.getActionAt())
                .ipAddress(domain.getIpAddress())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}

package co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.exception.customized;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ErrorCode {
    GENERIC_ERROR("GC-001: Generic error -> "),
    ENTITY_ALREADY_EXISTS("GC-002: Entity already exists -> "),
    ENTITY_NOT_FOUND("GC-003: Entity not found -> "),
    BUSINESS_RULE_VIOLATION("GC-004: Business rule violation -> ");

    private final String description;
}

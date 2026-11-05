package co.unicauca.edu.co.contables.financial.domain.models.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FinancialStatementLog {
    private String eventType;
    private String message;
    private String icon;
    private String color;
    private OffsetDateTime createdAt;
}
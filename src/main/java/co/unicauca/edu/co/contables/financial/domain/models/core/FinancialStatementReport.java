package co.unicauca.edu.co.contables.financial.domain.models.core;

import co.unicauca.edu.co.contables.financial.domain.models.enums.EFinancialStatementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FinancialStatementReport {
    private UUID reportId;
    private EFinancialStatementType type;
    private String entId;
    private FinancialStatementCriteria criteria;
    private OffsetDateTime createdAt;
    private String downloadUrl;
}
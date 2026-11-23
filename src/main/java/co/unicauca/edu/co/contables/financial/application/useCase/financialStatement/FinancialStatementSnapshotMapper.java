package co.unicauca.edu.co.contables.financial.application.useCase.financialStatement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import co.unicauca.edu.co.contables.financial.domain.models.core.FinancialStatementDataPayload;
import co.unicauca.edu.co.contables.financial.domain.models.core.FinancialStatementGenerationResult;
import co.unicauca.edu.co.contables.financial.domain.models.core.FinancialStatementReport;
import co.unicauca.edu.co.contables.financial.domain.models.core.FinancialStatementSnapshot;
import co.unicauca.edu.co.contables.financial.infrastructure.out.persistence.entity.FinancialStatementEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FinancialStatementSnapshotMapper {

    private final ObjectMapper objectMapper;
    private final FinancialStatementRowMapper financialStatementRowMapper;
    private final FinancialStatementReportMetadataMapper financialStatementReportMetadataMapper;

    public String toJson(FinancialStatementReport report, FinancialStatementDataPayload payload) {
        FinancialStatementSnapshot snapshot = FinancialStatementSnapshot.builder()
                .version(FinancialStatementSnapshot.CURRENT_VERSION)
                .reportId(report.getReportId())
                .type(report.getType())
                .entId(report.getEntId())
                .criteria(report.getCriteria())
                .createdAt(report.getCreatedAt())
                .financialStatementData(financialStatementRowMapper.toTypedRows(payload.getRows()))
                .totalAssets(payload.getTotalAssets())
                .totalLiabilities(payload.getTotalLiabilities())
                .totalEquity(payload.getTotalEquity())
                .build();

        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("No fue posible serializar el snapshot del estado financiero.", exception);
        }
    }

    public FinancialStatementSnapshot fromJson(String reportSnapshot) {
        if (!StringUtils.hasText(reportSnapshot)) {
            return null;
        }

        try {
            FinancialStatementSnapshot snapshot = objectMapper.readValue(reportSnapshot, FinancialStatementSnapshot.class);
            if (snapshot == null) {
                return null;
            }
            if (snapshot.getVersion() == null) {
                snapshot.setVersion(FinancialStatementSnapshot.CURRENT_VERSION);
            }
            if (snapshot.getFinancialStatementData() == null) {
                snapshot.setFinancialStatementData(List.of());
            }
            return snapshot;
        } catch (Exception exception) {
            return null;
        }
    }

    public FinancialStatementGenerationResult toGenerationResult(FinancialStatementEntity entity) {
        FinancialStatementSnapshot snapshot = fromJson(entity.getReportSnapshot());

        return FinancialStatementGenerationResult.builder()
                .financialStatement(FinancialStatementReport.builder()
                        .reportId(entity.getReportId())
                        .type(entity.getType())
                        .entId(entity.getEntId())
                        .criteria(snapshot != null ? snapshot.getCriteria() : null)
                        .createdAt(entity.getCreatedAt())
                        .downloadUrl(financialStatementReportMetadataMapper.buildDownloadUrl(entity.getReportId()))
                        .build())
                .financialStatementData(snapshot != null
                        ? snapshot.getFinancialStatementData()
                        : List.of())
                .annotations(List.of())
                .totalAssets(snapshot != null ? snapshot.getTotalAssets() : null)
                .totalLiabilities(snapshot != null ? snapshot.getTotalLiabilities() : null)
                .totalEquity(snapshot != null ? snapshot.getTotalEquity() : null)
                .build();
    }
}

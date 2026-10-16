package co.unicauca.edu.co.contables.financial.application.ports.in.financialStatement;

import co.unicauca.edu.co.contables.financial.domain.models.core.FinancialStatementHistoryItem;
import co.unicauca.edu.co.contables.financial.domain.models.core.FinancialStatementLog;
import co.unicauca.edu.co.contables.financial.domain.models.core.FinancialStatementAnnotation;
import co.unicauca.edu.co.contables.financial.domain.models.core.FinancialStatementGenerationResult;
import co.unicauca.edu.co.contables.financial.domain.models.core.FinancialStatementReport;
import co.unicauca.edu.co.contables.financial.domain.models.core.FinancialStatementRequest;
import co.unicauca.edu.co.contables.financial.domain.models.core.FinancialStatementTemplate;
import co.unicauca.edu.co.contables.financial.domain.models.core.PageResult;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IFinancialStatementCommandPort {

    FinancialStatementGenerationResult previewFinancialStatement(FinancialStatementRequest request);

    FinancialStatementGenerationResult registerFinancialStatement(FinancialStatementRequest request);

    Optional<FinancialStatementReport> getFinancialStatementReport(UUID reportId);

    Optional<FinancialStatementGenerationResult> getFinancialStatementSnapshot(UUID reportId);

    PageResult<FinancialStatementHistoryItem> getHistoryByEnterprise(String enterpriseId, int page, int size, String sort);

    List<FinancialStatementLog> getLogsByReportId(UUID reportId);

    FinancialStatementTemplate saveTemplate(FinancialStatementTemplate template);

    FinancialStatementTemplate saveDefaultTemplate(FinancialStatementTemplate template);

    List<FinancialStatementTemplate> getTemplatesByEnterprise(String enterpriseId);

    Optional<FinancialStatementTemplate> getDefaultTemplateByEnterprise(String enterpriseId);

    void deleteTemplate(String enterpriseId, Long templateId);

    int deleteTemplates(String enterpriseId, List<Long> templateIds);

    int deleteAllTemplates(String enterpriseId);

    List<FinancialStatementAnnotation> getAnnotations(UUID reportId);

    FinancialStatementAnnotation createAnnotation(UUID reportId, String text);

    FinancialStatementAnnotation updateAnnotation(UUID reportId, Long annotationId, String text);

    void deleteAnnotation(UUID reportId, Long annotationId);

    void registerDeliveryEvent(UUID reportId, String deliveryWay, String message, String eventType);

    void registerLogEvent(UUID reportId, String eventType, String message, String icon, String color);
}
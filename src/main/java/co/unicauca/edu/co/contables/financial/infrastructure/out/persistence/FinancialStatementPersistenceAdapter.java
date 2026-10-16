package co.unicauca.edu.co.contables.financial.infrastructure.out.persistence;

import co.unicauca.edu.co.contables.financial.application.ports.out.IFinancialStatementPersistencePort;
import co.unicauca.edu.co.contables.financial.infrastructure.out.persistence.entity.FinancialStatementAnnotationEntity;
import co.unicauca.edu.co.contables.financial.infrastructure.out.persistence.entity.FinancialStatementEntity;
import co.unicauca.edu.co.contables.financial.infrastructure.out.persistence.entity.FinancialStatementHistoryEntity;
import co.unicauca.edu.co.contables.financial.infrastructure.out.persistence.entity.FinancialStatementLogEntity;
import co.unicauca.edu.co.contables.financial.infrastructure.out.persistence.entity.FinancialStatementTemplateEntity;
import co.unicauca.edu.co.contables.financial.infrastructure.out.persistence.repository.IFinancialStatementAnnotationRepository;
import co.unicauca.edu.co.contables.financial.infrastructure.out.persistence.repository.IFinancialStatementRepository;
import co.unicauca.edu.co.contables.financial.infrastructure.out.persistence.repository.IFinancialStatementHistoryRepository;
import co.unicauca.edu.co.contables.financial.infrastructure.out.persistence.repository.IFinancialStatementLogRepository;
import co.unicauca.edu.co.contables.financial.infrastructure.out.persistence.repository.IFinancialStatementTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FinancialStatementPersistenceAdapter implements IFinancialStatementPersistencePort {

    private final IFinancialStatementRepository financialStatementRepository;
    private final IFinancialStatementAnnotationRepository financialStatementAnnotationRepository;
    private final IFinancialStatementHistoryRepository financialStatementHistoryRepository;
    private final IFinancialStatementLogRepository financialStatementLogRepository;
    private final IFinancialStatementTemplateRepository financialStatementTemplateRepository;

    @Override
    public FinancialStatementEntity saveFinancialStatement(FinancialStatementEntity entity) {
        return financialStatementRepository.save(entity);
    }

    @Override
    public Optional<FinancialStatementEntity> findFinancialStatementByReportId(UUID reportId) {
        return financialStatementRepository.findByReportId(reportId);
    }

    @Override
    public FinancialStatementHistoryEntity saveHistory(FinancialStatementHistoryEntity entity) {
        return financialStatementHistoryRepository.save(entity);
    }

    @Override
    public Page<FinancialStatementHistoryEntity> findHistoryByEnterprise(String entId, Pageable pageable) {
        return financialStatementHistoryRepository.findByFinancialStatement_EntId(entId, pageable);
    }

    @Override
    public FinancialStatementLogEntity saveLog(FinancialStatementLogEntity entity) {
        return financialStatementLogRepository.save(entity);
    }

    @Override
    public List<FinancialStatementLogEntity> findLogsByReportId(UUID reportId) {
        return financialStatementLogRepository.findByFinancialStatement_ReportIdOrderByCreatedAtDesc(reportId);
    }

    @Override
    public FinancialStatementTemplateEntity saveTemplate(FinancialStatementTemplateEntity entity) {
        return financialStatementTemplateRepository.save(entity);
    }

    @Override
    public Optional<FinancialStatementTemplateEntity> findTemplateByIdAndEnterprise(Long id, String entId) {
        return financialStatementTemplateRepository.findByIdAndEntId(id, entId);
    }

    @Override
    public List<FinancialStatementTemplateEntity> findTemplatesByEnterprise(String entId) {
        return financialStatementTemplateRepository.findByEntIdOrderByIsDefaultDescCreatedAtAsc(entId);
    }

    @Override
    public long countTemplatesByEnterprise(String entId) {
        return financialStatementTemplateRepository.countByEntId(entId);
    }

    @Override
    public Optional<FinancialStatementTemplateEntity> findDefaultTemplateByEnterprise(String entId) {
        return financialStatementTemplateRepository.findByEntIdAndIsDefaultTrue(entId);
    }

    @Override
    public void deleteTemplate(FinancialStatementTemplateEntity entity) {
        financialStatementTemplateRepository.delete(entity);
    }

    @Override
    public FinancialStatementAnnotationEntity saveAnnotation(FinancialStatementAnnotationEntity entity) {
        return financialStatementAnnotationRepository.save(entity);
    }

    @Override
    public Optional<FinancialStatementAnnotationEntity> findAnnotationByIdAndReportId(Long annotationId, UUID reportId) {
        return financialStatementAnnotationRepository.findByIdAndFinancialStatement_ReportId(annotationId, reportId);
    }

    @Override
    public List<FinancialStatementAnnotationEntity> findAnnotationsByReportId(UUID reportId) {
        return financialStatementAnnotationRepository.findByFinancialStatement_ReportIdOrderByCreatedAtAsc(reportId);
    }

    @Override
    public void deleteAnnotation(FinancialStatementAnnotationEntity entity) {
        financialStatementAnnotationRepository.delete(entity);
    }
}
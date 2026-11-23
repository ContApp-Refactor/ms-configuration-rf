package co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.audit.aspect;

import java.time.LocalDate;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.audit.annotation.FactureDocumentAuditable;
import co.unicauca.edu.co.contables.commons.audit.annotation.DocumentOperationType;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.audit.builder.FactureAuditEventBuilder;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.audit.builder.DocumentDataBuilder;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.audit.builder.DocumentEventDto;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.audit.publisher.FactureAuditEventPublisher;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.dto.ReturnRequestDto;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.dto.SkeletonFactureDetailDto;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.dto.SkeletonFactureRequestDto;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.SkeletonFacture;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.SkeletonReturn;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.repository.SkeletonFactureRepository;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.repository.SkeletonReturnRepository;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class AuditAspect {

    private final FactureAuditEventBuilder auditEventBuilder;
    private final DocumentDataBuilder documentDataBuilder;
    private final FactureAuditEventPublisher auditEventPublisher;
    private final SkeletonFactureRepository factureRepository;
    private final SkeletonReturnRepository returnRepository;

    public AuditAspect(FactureAuditEventBuilder auditEventBuilder, DocumentDataBuilder documentDataBuilder,
            FactureAuditEventPublisher auditEventPublisher, SkeletonFactureRepository factureRepository,
            SkeletonReturnRepository returnRepository) {
        this.auditEventBuilder = auditEventBuilder;
        this.documentDataBuilder = documentDataBuilder;
        this.auditEventPublisher = auditEventPublisher;
        this.factureRepository = factureRepository;
        this.returnRepository = returnRepository;
    }

    @Around("@annotation(documentAuditable)")
    public Object audit(ProceedingJoinPoint joinPoint, FactureDocumentAuditable documentAuditable) throws Throwable {

        Object[] args = joinPoint.getArgs();
        Map<String, Object> beforeData = captureBeforeData(documentAuditable.operationType(), args);

        Object result = joinPoint.proceed();

        try {
            DocumentContext ctx = extractContext(documentAuditable.operationType(), args, result);
            if (ctx == null) {
                log.warn("No se pudo extraer contexto para operación {}", documentAuditable.operationType());
                return result;
            }

            Map<String, Object> documentData = documentDataBuilder.build(
                    documentAuditable.operationType(), args, result, beforeData, ctx);

            DocumentEventDto event = auditEventBuilder.build(
                    documentAuditable.moduleName(),
                    documentAuditable.operationType(),
                    ctx.enterpriseId(),
                    ctx.documentId(),
                    ctx.documentCode(),
                    ctx.documentType(),
                    ctx.documentDate(),
                    ctx.thirdPartyId(),
                    ctx.thirdPartyName(),
                    documentData);

            auditEventPublisher.publish(event);

        } catch (Exception e) {
            log.error("Error construyendo o publicando evento de auditoría [operation={}]: {}",
                    documentAuditable.operationType(), e.getMessage(), e);
        }

        return result;
    }

    private Map<String, Object> captureBeforeData(DocumentOperationType operationType, Object[] args) {
        if (operationType == DocumentOperationType.CREATE || operationType == DocumentOperationType.APPROVE) {
            return null;
        }
        try {
            Long factCode = extractFactCode(args);
            if (factCode != null) {
                return documentDataBuilder.fetchCurrentState(factCode);
            }
        } catch (Exception e) {
            log.warn("No se pudo capturar estado previo: {}", e.getMessage());
        }
        return null;
    }

    private DocumentContext extractContext(DocumentOperationType operationType, Object[] args, Object result) {
        return switch (operationType) {
            case CREATE -> extractFromCreateResult(result, args);
            case UPDATE -> extractFromUpdateResult(result);
            case APPROVE -> extractFromApproveResult(result);
            case VOID -> extractFromVoidResult(result, args);
            case DELETE -> extractFromDeleteArgs(args);
        };
    }

    private DocumentContext extractFromCreateResult(Object result, Object[] args) {
        if (result instanceof SkeletonFactureDetailDto dto) {
            String prefix = switch (dto.getFactureType()) {
                case PURCHASE, SALE -> "FAC";
                case NON_COMMERCIAL_ENTRY, NON_COMMERCIAL_EXIT -> "NC";
                case RETURN_ON_SALE, RETURN_ON_PURCHASE -> "DEV";
                default -> "DOC";
            };
            return new DocumentContext(
                    dto.getId() != null ? dto.getId().toString() : null,
                    dto.getFactCode() != null ? prefix + "-" + dto.getFactCode().toString() : null,
                    dto.getEntId(),
                    dto.getFactureType() != null ? dto.getFactureType().name() : null,
                    dto.getCreatedAt() != null ? dto.getCreatedAt().toLocalDate() : LocalDate.now(),
                    dto.getThId() != null ? dto.getThId().toString() : null,
                    dto.getThId() != null ? "Cliente " + dto.getThId() : null);
        }

        // devolucion es void
        if (result == null) {
            return extractFromReturnArgs(args);
        }
        return null;
    }

    private DocumentContext extractFromReturnArgs(Object[] args) {
        ReturnRequestDto returnDto = null;
        SkeletonReturn.ReturnType returnType = null;

        for (Object arg : args) {
            if (arg instanceof ReturnRequestDto r)
                returnDto = r;
            if (arg instanceof SkeletonReturn.ReturnType rt)
                returnType = rt;
        }

        if (returnDto == null)
            return null;

        // consultamos la factura original para obtener entId y thId
        SkeletonFacture facture = factureRepository
                .findByFactCodeWithProducts(returnDto.getFactCode())
                .orElse(null);

        // Consultamos el ultimo registro de devolucion para sacar el id
        SkeletonReturn lastReturn = returnRepository
                .findTopByOriginalFactCodeOrderByIdDesc(returnDto.getFactCode())
                .orElse(null);

        String documentId = lastReturn != null ? lastReturn.getId().toString() : null;
        String documentCode = lastReturn != null ? "DEV-" + lastReturn.getId() : returnDto.getFactCode().toString();

        String entId = facture != null ? facture.getEntId() : "";
        String thId = facture != null ? facture.getThId().toString() : "";
        String returnTypeName = returnType != null ? returnType.name() : "RETURN";

        return new DocumentContext(
                documentId,
                documentCode,
                entId,
                returnTypeName,
                LocalDate.now(),
                thId,
                "Cliente " + thId);
    }

    private DocumentContext extractFromUpdateResult(Object result) {
        // Mock implementation
        return new DocumentContext(
                "mock-document-id",
                "mock-document-code",
                "mock-enterprise-id",
                "UPDATE",
                LocalDate.now(),
                "mock-third-party-id",
                "mock-third-party-name");
    }

    private DocumentContext extractFromApproveResult(Object result) {
        // Mock implementation
        return new DocumentContext(
                "mock-document-id",
                "mock-document-code",
                "mock-enterprise-id",
                "APPROVE",
                LocalDate.now(),
                "mock-third-party-id",
                "mock-third-party-name");
    }

    private DocumentContext extractFromVoidResult(Object result, Object[] args) {
        // Mock implementation
        return new DocumentContext(
                "mock-document-id",
                "mock-document-code",
                "mock-enterprise-id",
                "VOID",
                LocalDate.now(),
                "mock-third-party-id",
                "mock-third-party-name");
    }

    private DocumentContext extractFromDeleteArgs(Object[] args) {
        // Mock implementation
        return new DocumentContext(
                "mock-document-id",
                "mock-document-code",
                "mock-enterprise-id",
                "DELETE",
                LocalDate.now(),
                "mock-third-party-id",
                "mock-third-party-name");
    }

    private Long extractFactCode(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof ReturnRequestDto r)
                return r.getFactCode();
            if (arg instanceof SkeletonFactureRequestDto r)
                return r.getFactCode();
            if (arg instanceof Long l)
                return l;
        }
        return null;
    }

}

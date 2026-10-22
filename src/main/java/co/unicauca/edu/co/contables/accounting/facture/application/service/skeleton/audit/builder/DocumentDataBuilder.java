package co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.audit.builder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.commons.audit.annotation.DocumentOperationType;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.audit.aspect.DocumentContext;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.dto.ReturnRequestDto;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.dto.SkeletonFactureDetailDto;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.SkeletonReturn;

@Component
public class DocumentDataBuilder {

    public Map<String, Object> build(
            DocumentOperationType operationType,
            Object[] args,
            Object result,
            Map<String, Object> beforeData,
            DocumentContext ctx) {

        return switch (operationType) {
            case CREATE -> buildForCreate(result, args);
            case UPDATE -> buildForUpdate(result, beforeData);
            case APPROVE -> buildForApprove(result);
            case VOID -> buildForVoid(result, beforeData, ctx);
            case DELETE -> buildForDelete(beforeData, ctx);
        };
    }

    private Map<String, Object> buildForCreate(Object result, Object[] args) {
        if (result instanceof SkeletonFactureDetailDto dto) {
            return Map.of(
                    "header", buildHeader(dto),
                    "details", buildDetails(dto),
                    "totals", buildTotals(dto),
                    "metadata", Map.of());
        }
        // Devolución: result es void, datos vienen del argumento
        for (Object arg : args) {
            if (arg instanceof ReturnRequestDto returnDto) {
                return buildForReturn(returnDto, args);
            }
        }
        return Map.of();
    }

    private Map<String, Object> buildForUpdate(Object result, Map<String, Object> beforeData) {
        if (result instanceof SkeletonFactureDetailDto dto) {
            Map<String, Object> afterData = buildHeader(dto);
            return Map.of(
                    "header", buildContext(beforeData),
                    "changes", buildDiff(beforeData, afterData),
                    "details", buildDetails(dto),
                    "totals", buildTotals(dto));
        }
        return Map.of();
    }

    private Map<String, Object> buildForApprove(Object result) {
        if (result instanceof SkeletonFactureDetailDto dto) {
            return Map.of(
                    "header", buildHeader(dto),
                    "details", buildDetails(dto),
                    "totals", buildTotals(dto));
        }
        return Map.of();
    }

    private Map<String, Object> buildForVoid(
            Object result, Map<String, Object> beforeData, DocumentContext ctx) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("header", beforeData != null ? buildContext(beforeData)
                : Map.of("documentCode", ctx.documentCode()));
        data.put("details", List.of());
        data.put("totals", Map.of());
        data.put("metadata", Map.of("voidedDocument", ctx.documentCode()));
        return data;
    }

    private Map<String, Object> buildForDelete(
            Map<String, Object> beforeData, DocumentContext ctx) {
        return Map.of(
                "header", beforeData != null ? beforeData
                        : Map.of("documentCode", ctx.documentCode()),
                "details", List.of(),
                "totals", Map.of());
    }

    private Map<String, Object> buildForReturn(ReturnRequestDto dto, Object[] args) {
        String returnType = "RETURN";
        for (Object arg : args) {
            if (arg instanceof SkeletonReturn.ReturnType rt)
                returnType = rt.name();
        }
        return Map.of(
                "header", Map.of(
                        "originalFactCode", dto.getFactCode(),
                        "returnType", returnType),
                "details", dto.getProducts().stream()
                        .map(p -> Map.of(
                                "productId", p.getProductId(),
                                "quantity", p.getQuantity(),
                                "reason", p.getReason() != null ? p.getReason() : ""))
                        .toList(),
                "totals", Map.of(),
                "metadata", Map.of());
    }

    public Map<String, Object> fetchCurrentState(Long factCode) {
        // Este método lo implementas consultando el repositorio directamente
        // o usando el servicio. Se inyecta el repositorio aquí:
        // Optional<SkeletonFacture> facture =
        // factureRepository.findByFactCodeWithProducts(factCode);
        // return facture.map(this::entityToMap).orElse(null);
        // Por ahora retorna vacío hasta que lo conectes:
        return Map.of("documentCode", factCode.toString());
    }

    private Map<String, Object> buildHeader(SkeletonFactureDetailDto dto) {
        Map<String, Object> h = new LinkedHashMap<>();
        h.put("factCode", dto.getFactCode());
        h.put("factureType", dto.getFactureType() != null ? dto.getFactureType().name() : null);
        h.put("thirdPartName", dto.getThId() != null ? "Cliente " + dto.getThId() : null);
        h.put("expirationDate", dto.getExpirationDate());
        h.put("accountingAccount", dto.getAccountingAccount());
        return h;
    }

    private List<Map<String, Object>> buildDetails(SkeletonFactureDetailDto dto) {
        if (dto.getProducts() == null)
            return List.of();
        return dto.getProducts().stream()
                .map(p -> {
                    Map<String, Object> line = new LinkedHashMap<>();
                    line.put("productId", p.getProductId());
                    line.put("description", p.getDescription());
                    line.put("amount", p.getAmount());
                    line.put("unitPrice", p.getUnitPrice());
                    line.put("discount", p.getDiscount());
                    line.put("taxPercentage", p.getTaxPercentage());
                    line.put("subtotal", p.getSubtotal());
                    return line;
                })
                .toList();
    }

    private Map<String, Object> buildTotals(SkeletonFactureDetailDto dto) {
        Map<String, Object> t = new LinkedHashMap<>();
        t.put("totalValue", dto.getTotalValue());
        t.put("totalPay", dto.getTotalPay());
        t.put("pendingValue", dto.getPendingValue());
        return t;
    }

    private Map<String, Object> buildContext(Map<String, Object> data) {
        // Retorna solo los campos clave para contexto reducido
        Set<String> contextFields = Set.of(
                "factCode", "factureType", "totalValue", "totalPay", "thirdPartyId");
        Map<String, Object> ctx = new LinkedHashMap<>();
        data.forEach((k, v) -> {
            if (contextFields.contains(k))
                ctx.put(k, v);
        });
        return ctx;
    }

    private Map<String, Object> buildDiff(
            Map<String, Object> before, Map<String, Object> after) {
        Map<String, Object> diff = new LinkedHashMap<>();
        after.forEach((key, afterVal) -> {
            Object beforeVal = before != null ? before.get(key) : null;
            if (!Objects.equals(beforeVal, afterVal)) {
                diff.put(key, Map.of("before", beforeVal != null ? beforeVal : "null",
                        "after", afterVal != null ? afterVal : "null"));
            }
        });
        return diff;
    }
}

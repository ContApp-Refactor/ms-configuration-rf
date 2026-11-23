package co.unicauca.edu.co.contables.inventory.products.infraestructure.audit.aspect;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.inventory.products.application.ports.output.ICategoryPersistencePort;
import co.unicauca.edu.co.contables.inventory.products.application.ports.output.IProductPersistencePort;
import co.unicauca.edu.co.contables.inventory.products.application.ports.output.IProductTypePersistencePort;
import co.unicauca.edu.co.contables.inventory.products.application.ports.output.IUnitOfMeasurePersistencePort;
import co.unicauca.edu.co.contables.inventory.products.domain.model.Category;
import co.unicauca.edu.co.contables.inventory.products.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.products.domain.model.ProductType;
import co.unicauca.edu.co.contables.inventory.products.domain.model.UnitOfMeasure;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.audit.annotation.Auditable;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.audit.annotation.OperationType;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.audit.builder.AuditEventBuilder;
import co.unicauca.edu.co.contables.commons.audit.builder.OperationEventDto;
import co.unicauca.edu.co.contables.commons.audit.publisher.AuditEventPublisher;

import lombok.extern.slf4j.Slf4j;

//Aspect
@Aspect
@Component("productsAuditAspect")
@Slf4j
public class AuditAspect {

    private final AuditEventBuilder auditEventBuilder;
    private final AuditEventPublisher auditEventPublisher;
    @Lazy
    private final IProductPersistencePort productPersistencePort;
    @Lazy
    private final ICategoryPersistencePort categoryPersistencePort;
    @Lazy
    private final IProductTypePersistencePort productTypePersistencePort;
    @Lazy
    private final IUnitOfMeasurePersistencePort unitOfMeasurePersistencePort;

    private static final Map<Class<?>, List<String>> CONTEXT_FIELDS = Map.of(
            Product.class, List.of("code", "name"),
            Category.class, List.of("name"),
            ProductType.class, List.of("name"),
            UnitOfMeasure.class, List.of("name", "abbreviation"));

    private static final Map<String, Class<?>> TABLE_TO_CLASS = Map.of(
            "PRODUCT", Product.class,
            "CATEGORY", Category.class,
            "PRODUCT_TYPE", ProductType.class,
            "UNIT_OF_MEASURE", UnitOfMeasure.class);

    public AuditAspect(
            AuditEventBuilder auditEventBuilder,
            AuditEventPublisher auditEventPublisher,
            @Lazy IProductPersistencePort productPersistencePort,
            @Lazy ICategoryPersistencePort categoryPersistencePort,
            @Lazy IProductTypePersistencePort productTypePersistencePort,
            @Lazy IUnitOfMeasurePersistencePort unitOfMeasurePersistencePort) {
        this.auditEventBuilder = auditEventBuilder;
        this.auditEventPublisher = auditEventPublisher;
        this.productPersistencePort = productPersistencePort;
        this.categoryPersistencePort = categoryPersistencePort;
        this.productTypePersistencePort = productTypePersistencePort;
        this.unitOfMeasurePersistencePort = unitOfMeasurePersistencePort;
    }

    @Around("@annotation(auditable)")
    public Object audit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {

        Object[] args = joinPoint.getArgs();

        // Capturamos el estado BEFORE para update, change state y delete
        Map<String, Object> beforeData = captureBeforeData(auditable, args);

        // Ejecutamos el metodo de negocio, este si o si se debe ejecutar
        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable ex) {
            // Si el negocio falla no auditamos
            throw ex;
        }

        // Se intenta construir y publicar el evento , no afectamos el negocio
        try {
            OperationType resolvedType = resolveFinalOperationType(auditable, beforeData);
            String enterpriseId = resolveEnterpriseId(auditable, args, result);
            String registerId = resolveRegisterId(auditable, args, result);
            Map<String, Object> dataObject = buildDataObject(resolvedType, args, result, beforeData, auditable);

            if (dataObject == null || dataObject.isEmpty()) {
                return result;
            }

            if (dataObject.containsKey("changes")) {
                Map<?, ?> changes = (Map<?, ?>) dataObject.get("changes");
                if (changes == null || changes.isEmpty()) {
                    return result;
                }
            }

            OperationEventDto dto = auditEventBuilder.build(
                    auditable,
                    resolvedType,
                    enterpriseId,
                    registerId,
                    dataObject);

            auditEventPublisher.publish(dto);

        } catch (Exception e) {
            log.error("Error construyendo evento de auditoría [{}]: {}",
                    auditable.operationType(), e.getMessage(), e);
        }

        return result;

    }

    protected Map<String, Object> captureBeforeData(Auditable auditable, Object[] args) {
        try {
            return switch (auditable.operationType()) {
                case UPDATE, ACTIVATE, INACTIVATE, DELETE -> fetchCurrentState(auditable, args);
                default -> null;
            };
        } catch (Exception e) {
            log.warn("No se pudo capturar estado before para {}: {}",
                    auditable.operationType(), e.getMessage());
            return null;
        }
    }

    protected OperationType resolveFinalOperationType(Auditable auditable, Map<String, Object> beforeData) {
        if (auditable.operationType() == OperationType.INACTIVATE) {
            boolean wasActive = beforeData != null && (boolean) beforeData.get("state");
            return wasActive ? OperationType.INACTIVATE : OperationType.ACTIVATE;
        }
        return auditable.operationType();
    }

    protected Map<String, Object> fetchCurrentState(Auditable auditable, Object[] args) {
        Long id = (Long) args[auditable.idArgIndex()];
        String enterpriseId = (String) args[auditable.enterpriseIdArgIndex()];

        return switch (auditable.affectedTable()) {
            case "PRODUCT" -> productPersistencePort
                    .findByIdAndEnterpriseId(id, enterpriseId)
                    .map(this::productToMap)
                    .orElse(null);
            case "CATEGORY" -> categoryPersistencePort
                    .findByIdAndEnterpriseId(id, enterpriseId)
                    .map(this::categoryToMap)
                    .orElse(null);
            case "PRODUCT_TYPE" -> productTypePersistencePort
                    .findByIdAndEnterpriseId(id, enterpriseId)
                    .map(this::productTypeToMap)
                    .orElse(null);
            case "UNIT_OF_MEASURE" -> unitOfMeasurePersistencePort
                    .findByIdAndEnterpriseId(id, enterpriseId)
                    .map(this::unitOfMeasureToMap)
                    .orElse(null);
            default -> null;
        };
    }

    protected Map<String, Object> buildDataObject(OperationType operationType,
            Object[] args,
            Object result,
            Map<String, Object> beforeData,
            Auditable auditable) {
        Class<?> entityClass = TABLE_TO_CLASS.get(auditable.affectedTable());
        return switch (operationType) {

            case CREATE -> {
                Map<String, Object> data = new LinkedHashMap<>();
                if (result instanceof Product p) {
                    data.put("entity", productToMap(p));
                }
                if (result instanceof Category c) {
                    data.put("entity", categoryToMap(c));
                }
                if (result instanceof ProductType pt) {
                    data.put("entity", productTypeToMap(pt));
                }
                if (result instanceof UnitOfMeasure u) {
                    data.put("entity", unitOfMeasureToMap(u));
                }
                yield data;
            }

            case UPDATE -> {
                Map<String, Object> afterData = fetchCurrentState(auditable, args);
                Map<String, Object> data = new LinkedHashMap<>();
                Map<String, Object> context = buildContext(entityClass, beforeData);
                if (!context.isEmpty())
                    data.put("context", context);
                data.put("changes", buildDiff(beforeData, afterData));
                yield data;
            }

            case ACTIVATE, INACTIVATE -> {
                if (beforeData != null) {
                    Map<String, Object> data = new LinkedHashMap<>();
                    Map<String, Object> context = buildContext(entityClass, beforeData);
                    if (!context.isEmpty())
                        data.put("context", context);
                    data.put("changes", buildDiff(
                            Map.of("state", beforeData.get("state")),
                            Map.of("state", !((Boolean) beforeData.get("state")))));
                    yield data;
                }
                yield Map.of();
            }

            case DELETE -> Map.of("entity", beforeData != null ? beforeData : Map.of("id", args[0]));
        };
    }

    protected Map<String, Object> buildDiff(Map<String, Object> before, Map<String, Object> after) {
        Map<String, Object> diff = new LinkedHashMap<>();
        if (before == null || after == null)
            return diff;

        after.forEach((key, afterValue) -> {
            Object beforeValue = before.get(key);
            if (!Objects.equals(beforeValue, afterValue)) {
                Map<String, Object> change = new LinkedHashMap<>();
                change.put("before", beforeValue);
                change.put("after", afterValue);
                diff.put(key, change);
            }
        });
        return diff;
    }

    protected Map<String, Object> buildContext(Class<?> entityClass, Map<String, Object> data) {
        if (data == null || entityClass == null)
            return Map.of();

        return CONTEXT_FIELDS.getOrDefault(entityClass, List.of())
                .stream()
                .filter(field -> data.get(field) != null)
                .collect(Collectors.toMap(
                        field -> field,
                        data::get,
                        (a, b) -> a,
                        LinkedHashMap::new));
    }

    protected String resolveEnterpriseId(Auditable auditable, Object[] args, Object result) {
        return switch (auditable.operationType()) {
            case CREATE -> {
                if (result instanceof Product p)
                    yield p.getEnterpriseId();
                if (result instanceof Category c)
                    yield c.getEnterpriseId();
                if (result instanceof ProductType pt)
                    yield pt.getEnterpriseId();
                if (result instanceof UnitOfMeasure u)
                    yield u.getEnterpriseId();
                yield "UNKNOWN";
            }
            default -> (String) args[auditable.enterpriseIdArgIndex()];
        };
    }

    protected String resolveRegisterId(Auditable auditable, Object[] args, Object result) {
        return switch (auditable.operationType()) {
            case CREATE -> {
                if (result instanceof Product p)
                    yield String.valueOf(p.getId());
                if (result instanceof Category c)
                    yield String.valueOf(c.getId());
                if (result instanceof ProductType pt)
                    yield String.valueOf(pt.getId());
                if (result instanceof UnitOfMeasure u)
                    yield String.valueOf(u.getId());
                yield "UNKNOWN";
            }
            default -> String.valueOf((Long) args[auditable.idArgIndex()]);
        };
    }

    protected Map<String, Object> productToMap(Product p) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", p.getId());
        map.put("code", p.getCode());
        map.put("name", p.getName());
        map.put("description", p.getDescription());
        map.put("quantity", p.getQuantity());
        map.put("cost", p.getCost());
        map.put("unitOfMeasureId", p.getUnitOfMeasureId());
        map.put("categoryId", p.getCategoryId());
        map.put("productTypeId", p.getProductTypeId());
        map.put("reference", p.getReference());
        map.put("presentation", p.getPresentation());
        map.put("state", p.isState());

        map.entrySet().removeIf(entry -> entry.getValue() == null);
        return map;
    }

    protected Map<String, Object> categoryToMap(Category c) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", c.getId());
        map.put("name", c.getName());
        map.put("description", c.getDescription());
        map.put("inventoryId", c.getInventoryId());
        map.put("costId", c.getCostId());
        map.put("saleId", c.getSaleId());
        map.put("returnId", c.getReturnId());
        map.put("taxes", c.getTaxes());
        map.put("state", c.isState());

        map.entrySet().removeIf(entry -> entry.getValue() == null);
        return map;
    }

    protected Map<String, Object> productTypeToMap(ProductType pt) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", pt.getId());
        map.put("name", pt.getName());
        map.put("description", pt.getDescription());
        map.put("state", pt.isState());

        map.entrySet().removeIf(entry -> entry.getValue() == null);
        return map;
    }

    protected Map<String, Object> unitOfMeasureToMap(UnitOfMeasure u) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", u.getId());
        map.put("name", u.getName());
        map.put("description", u.getDescription());
        map.put("abbreviation", u.getAbbreviation());
        map.put("state", u.isState());

        map.entrySet().removeIf(entry -> entry.getValue() == null);
        return map;
    }
}

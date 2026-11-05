package co.unicauca.edu.co.contables.inventory.products.application.service.importExport;

import co.unicauca.edu.co.contables.inventory.products.application.ports.output.*;
import co.unicauca.edu.co.contables.inventory.products.domain.enums.ImportErrorType;
import co.unicauca.edu.co.contables.inventory.products.domain.model.Category;
import co.unicauca.edu.co.contables.inventory.products.domain.model.ImportErrorDetail;
import co.unicauca.edu.co.contables.inventory.products.domain.model.ProductExcelData;
import co.unicauca.edu.co.contables.inventory.products.domain.model.ProductType;
import co.unicauca.edu.co.contables.inventory.products.domain.model.UnitOfMeasure;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @brief Servicio que valida lotes de productos importados aplicando reglas de negocio y referencias
 *
 * Realiza validaciones masivas de datos Excel verificando campos requeridos,
 * formatos, unicidad de referencias y existencia de entidades relacionadas.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductBatchValidationService {

    private final ICategoryPersistencePort categoryPersistencePort;
    private final IUnitOfMeasurePersistencePort unitOfMeasurePersistencePort;
    private final IProductTypePersistencePort productTypePersistencePort;
    private final IProductPersistencePort productPersistencePort;

    private static final String COLUMN_NAME = "Nombre";
    private static final String COLUMN_DESCRIPTION = "Descripción";
    private static final String COLUMN_REFERENCE = "Referencia";
    private static final String COLUMN_PRESENTATION = "Presentación";
    private static final String COLUMN_QUANTITY = "Cantidad";
    private static final String COLUMN_COST = "Costo";
    private static final String COLUMN_UNIT_MEASURE = "Unidad de Medida";
    private static final String COLUMN_CATEGORY = "Categoría";
    private static final String COLUMN_PRODUCT_TYPE = "Tipo de Producto";

    private static final String REQUIRED_FIELD_MISSING = "REQUIRED_FIELD_MISSING";
    private static final String ENTITY_NOT_ACTIVE_SUFFIX = " no existe o está inactiva.";
    private static final String ENTITY_NOT_ACTIVE_SUFFIX_MASC = " no existe o está inactivo.";

    /**
     * @brief Valida lote de productos usando cache pre-cargado para eliminar N+1 queries
     * @details Pre-carga todas las entidades de referencia (categorías, unidades, tipos) y
     * referencias existentes en una sola operación, luego valida usando datos en memoria.
     * @param productsData lista de datos de productos a validar
     * @param entId ID de la empresa
     * @param columnMap mapa de columnas para información de errores
     * @return resultado de la validación por lotes
     */
    public BatchValidationResult validateBatch(List<ProductExcelData> productsData, String entId,
                                               Map<String, Integer> columnMap) {
        // Pre-cargar cache de datos de referencia (3 queries en lugar de N*3)
        ReferenceDataCache cache = preloadReferenceData(entId, productsData);

        List<ProductExcelData> validRecords = new ArrayList<>();
        List<ImportErrorDetail> errors = new ArrayList<>();
        int duplicateCount = 0;

        for (ProductExcelData productData : productsData) {
            List<ImportErrorDetail> productErrors = validateProduct(productData, columnMap);

            ProductExcelData resolvedData = resolveEntityIdsWithCache(productData, cache, productErrors, columnMap);

            if (productErrors.isEmpty()) {
                // Verificar si es duplicado usando cache
                String reference = productData.getReference();
                if (reference != null && cache.existingReferences.contains(reference)) {
                    duplicateCount++;
                } else {
                    if (resolvedData != null) {
                        validRecords.add(resolvedData);
                    }
                }
            } else {
                errors.addAll(productErrors);
            }
        }

        return BatchValidationResult.builder()
                .validRecords(validRecords)
                .errors(errors)
                .duplicateCount(duplicateCount)
                .validCount(validRecords.size())
                .build();
    }

    /**
     * @brief Pre-carga todos los datos de referencia necesarios para validación
     * @details Usa paginación dinámica para obtener TODOS los registros activos sin límites hardcodeados.
     * Esto evita problemas cuando hay más de 1000 categorías/unidades/tipos.
     * @param entId ID de la empresa
     * @param productsData lista de productos a validar
     * @return cache con todos los datos pre-cargados
     */
    private ReferenceDataCache preloadReferenceData(String entId, List<ProductExcelData> productsData) {
        ReferenceDataCache cache = new ReferenceDataCache();

        // Cargar TODAS las unidades de medida activas con paginación dinámica
        loadAllUnitOfMeasures(entId, cache);

        // Cargar TODAS las categorías activas con paginación dinámica
        loadAllCategories(entId, cache);

        // Cargar TODOS los tipos de producto activos con paginación dinámica
        loadAllProductTypes(entId, cache);

        // Cargar referencias existentes en batch
        Set<String> referencesToCheck = productsData.stream()
                .map(ProductExcelData::getReference)
                .filter(ref -> ref != null && !ref.trim().isEmpty())
                .collect(Collectors.toSet());

        for (String ref : referencesToCheck) {
            if (productPersistencePort.existsByReferenceAndEnterpriseId(ref, entId)) {
                cache.existingReferences.add(ref);
            }
        }

        return cache;
    }

    /**
     * @brief Carga todas las unidades de medida activas usando paginación dinámica
     * @details Itera sobre todas las páginas hasta obtener todos los registros activos
     * @param entId ID de la empresa
     * @param cache cache donde almacenar los datos
     */
    private void loadAllUnitOfMeasures(String entId, ReferenceDataCache cache) {
        int pageNumber = 0;
        int pageSize = 500;
        Page<UnitOfMeasure> page;

        do {
            page = unitOfMeasurePersistencePort.getAllUnitOfMeasuresByState(
                    entId, true, PageRequest.of(pageNumber, pageSize));
            
            page.getContent().forEach(unit -> 
                cache.unitsByName.put(unit.getName().toLowerCase(), unit.getId())
            );
            
            pageNumber++;
        } while (page.hasNext());
    }

    /**
     * @brief Carga todas las categorías activas usando paginación dinámica
     * @details Itera sobre todas las páginas hasta obtener todos los registros activos
     * @param entId ID de la empresa
     * @param cache cache donde almacenar los datos
     */
    private void loadAllCategories(String entId, ReferenceDataCache cache) {
        int pageNumber = 0;
        int pageSize = 500;
        Page<Category> page;

        do {
            page = categoryPersistencePort.getAllCategoriesByState(
                    entId, true, PageRequest.of(pageNumber, pageSize));
            
            page.getContent().forEach(cat -> 
                cache.categoriesByName.put(cat.getName().toLowerCase(), cat.getId())
            );
            
            pageNumber++;
        } while (page.hasNext());
    }

    /**
     * @brief Carga todos los tipos de producto activos usando paginación dinámica
     * @details Itera sobre todas las páginas hasta obtener todos los registros activos
     * @param entId ID de la empresa
     * @param cache cache donde almacenar los datos
     */
    private void loadAllProductTypes(String entId, ReferenceDataCache cache) {
        int pageNumber = 0;
        int pageSize = 500;
        Page<ProductType> page;

        do {
            page = productTypePersistencePort.findActivatedByEnterpriseId(entId, pageNumber, pageSize);
            
            page.getContent().forEach(type -> 
                cache.productTypesByName.put(type.getName().toLowerCase(), type.getId())
            );
            
            pageNumber++;
        } while (page.hasNext());
    }

    /**
     * @brief Resuelve IDs usando cache en memoria (sin queries adicionales)
     * @param productData datos del producto
     * @param cache cache con datos pre-cargados
     * @param errors lista de errores
     * @param columnMap mapa de columnas
     * @return producto con IDs resueltos o null si hay errores
     */
    private ProductExcelData resolveEntityIdsWithCache(ProductExcelData productData, ReferenceDataCache cache,
                                                       List<ImportErrorDetail> errors, Map<String, Integer> columnMap) {
        try {
            ProductExcelData.ProductExcelDataBuilder builder = productData.toBuilder();

            // Resolver unidad de medida desde cache
            if (productData.getUnitOfMeasureName() != null) {
                Long unitId = cache.unitsByName.get(productData.getUnitOfMeasureName().toLowerCase());
                if (unitId != null) {
                    builder.unitOfMeasureId(unitId);
                } else {
                    addEntityNotFoundError(productData.getRowNumber(), COLUMN_UNIT_MEASURE, columnMap, errors,
                            productData.getUnitOfMeasureName());
                    return null;
                }
            }

            // Resolver categoría desde cache
            if (productData.getCategoryName() != null) {
                Long catId = cache.categoriesByName.get(productData.getCategoryName().toLowerCase());
                if (catId != null) {
                    builder.categoryId(catId);
                } else {
                    addEntityNotFoundError(productData.getRowNumber(), COLUMN_CATEGORY, columnMap, errors,
                            productData.getCategoryName());
                    return null;
                }
            }

            // Resolver tipo de producto desde cache
            if (productData.getProductTypeName() != null) {
                Long typeId = cache.productTypesByName.get(productData.getProductTypeName().toLowerCase());
                if (typeId != null) {
                    builder.productTypeId(typeId);
                } else {
                    addEntityNotFoundError(productData.getRowNumber(), COLUMN_PRODUCT_TYPE, columnMap, errors,
                            productData.getProductTypeName());
                    return null;
                }
            }

            return builder.build();

        } catch (Exception e) {
            errors.add(ImportErrorDetail.builder()
                    .rowNumber(productData.getRowNumber())
                    .errorCode("ENTITY_RESOLUTION_ERROR")
                    .errorMessage("Error resolviendo entidades: " + e.getMessage())
                    .errorType(ImportErrorType.SYSTEM_ERROR)
                    .build());
            return null;
        }
    }

    /**
     * @brief Cache interno para datos de referencia pre-cargados
     */
    private static class ReferenceDataCache {
        final Map<String, Long> unitsByName = new HashMap<>();
        final Map<String, Long> categoriesByName = new HashMap<>();
        final Map<String, Long> productTypesByName = new HashMap<>();
        final Set<String> existingReferences = new HashSet<>();
    }

    /**
     * @brief Valida producto individual aplicando todas las reglas de validación
     * @param productData datos del producto a validar
     * @param columnMap mapa de columnas para información de errores
     * @return lista de errores encontrados en la validación
     */
    private List<ImportErrorDetail> validateProduct(ProductExcelData productData,
                                                    Map<String, Integer> columnMap) {
        List<ImportErrorDetail> errors = new ArrayList<>();

        // Validar campos requeridos
        validateRequiredFields(productData, errors, columnMap);

        // Validar formatos y longitudes
        validateFieldFormats(productData, errors, columnMap);

        // Validar valores numéricos
        validateNumericFields(productData, errors, columnMap);

        return errors;
    }

    /**
     * @brief Valida presencia de campos obligatorios en datos del producto
     * @param productData datos del producto a validar
     * @param errors lista donde agregar errores encontrados
     * @param columnMap mapa de columnas para información de errores
     */
    private void validateRequiredFields(ProductExcelData productData, List<ImportErrorDetail> errors,
                                       Map<String, Integer> columnMap) {
        if (isNullOrEmpty(productData.getName())) {
            errors.add(createValidationError(productData.getRowNumber(),
                    REQUIRED_FIELD_MISSING, "El nombre es requerido",
                    COLUMN_NAME, columnMap.get(COLUMN_NAME), productData.getName()));
        }

        if (isNullOrEmpty(productData.getDescription())) {
            errors.add(createValidationError(productData.getRowNumber(),
                    REQUIRED_FIELD_MISSING, "La descripción es requerida",
                    COLUMN_DESCRIPTION, columnMap.get(COLUMN_DESCRIPTION), productData.getDescription()));
        }

        if (isNullOrEmpty(productData.getReference())) {
            errors.add(createValidationError(productData.getRowNumber(),
                    REQUIRED_FIELD_MISSING, "La referencia es requerida",
                    COLUMN_REFERENCE, columnMap.get(COLUMN_REFERENCE), productData.getReference()));
        }

        if (isNullOrEmpty(productData.getPresentation())) {
            errors.add(createValidationError(productData.getRowNumber(),
                    REQUIRED_FIELD_MISSING, "La presentación es requerida",
                    COLUMN_PRESENTATION, columnMap.get(COLUMN_PRESENTATION), productData.getPresentation()));
        }

        if (isNullOrEmpty(productData.getUnitOfMeasureName())) {
            errors.add(createValidationError(productData.getRowNumber(),
                    REQUIRED_FIELD_MISSING, "La Unidad de Medida es requerida",
                    COLUMN_UNIT_MEASURE, columnMap.get(COLUMN_UNIT_MEASURE), productData.getUnitOfMeasureName()));
        }

        if (isNullOrEmpty(productData.getCategoryName())) {
            errors.add(createValidationError(productData.getRowNumber(),
                    REQUIRED_FIELD_MISSING, "La Categoría es requerida",
                    COLUMN_CATEGORY, columnMap.get(COLUMN_CATEGORY), productData.getCategoryName()));
        }

        if (isNullOrEmpty(productData.getProductTypeName())) {
            errors.add(createValidationError(productData.getRowNumber(),
                    REQUIRED_FIELD_MISSING, "El Tipo de Producto es requerido",
                    COLUMN_PRODUCT_TYPE, columnMap.get(COLUMN_PRODUCT_TYPE), productData.getProductTypeName()));
        }
    }

    /**
     * @brief Valida formatos y restricciones de longitud en campos de texto
     * @param productData datos del producto a validar
     * @param errors lista donde agregar errores encontrados
     * @param columnMap mapa de columnas para información de errores
     */
    private void validateFieldFormats(ProductExcelData productData, List<ImportErrorDetail> errors,
                                     Map<String, Integer> columnMap) {
        // Validar longitud de referencia
        if (productData.getReference() != null &&
            productData.getReference().length() > 255) {
            errors.add(createValidationError(productData.getRowNumber(),
                    "INVALID_REFERENCE", "La referencia excede la longitud máxima de 255 caracteres",
                    COLUMN_REFERENCE, columnMap.get(COLUMN_REFERENCE), productData.getReference()));
        }

        // Validar longitud de presentación
        if (productData.getPresentation() != null &&
            productData.getPresentation().length() > 255) {
            errors.add(createValidationError(productData.getRowNumber(),
                    "INVALID_PRESENTATION", "La presentación excede la longitud máxima de 255 caracteres",
                    COLUMN_PRESENTATION, columnMap.get(COLUMN_PRESENTATION), productData.getPresentation()));
        }
    }

    /**
     * @brief Valida restricciones numéricas en campos de cantidad y costo
     * @param productData datos del producto a validar
     * @param errors lista donde agregar errores encontrados
     * @param columnMap mapa de columnas para información de errores
     */
    private void validateNumericFields(ProductExcelData productData, List<ImportErrorDetail> errors,
                                      Map<String, Integer> columnMap) {
        // Validar cantidad (opcional, pero si tiene valor debe ser positivo)
        if (productData.getQuantity() != null && productData.getQuantity() < 0) {
            errors.add(createValidationError(productData.getRowNumber(),
                    "INVALID_NUMBER", "La cantidad debe ser un valor numerico y positivo",
                    COLUMN_QUANTITY, columnMap.get(COLUMN_QUANTITY), productData.getQuantity().toString()));
        }

        // Validar costo (opcional, pero si tiene valor debe ser positivo)
        if (productData.getCost() != null && productData.getCost() < 0) {
            errors.add(createValidationError(productData.getRowNumber(),
                    "INVALID_NUMBER", "El costo debe ser un valor numerico y positivo",
                    COLUMN_COST, columnMap.get(COLUMN_COST), productData.getCost().toString()));
        }
    }

    /**
     * @brief Agrega error específico cuando entidad relacionada no existe o está inactiva
     * @param rowNumber número de fila donde ocurrió el error
     * @param columnConstant constante que identifica el tipo de columna
     * @param columnMap mapa de columnas para información de errores
     * @param errors lista donde agregar el error encontrado
     * @param entityName nombre de la entidad que no se pudo encontrar
     */
    private void addEntityNotFoundError(int rowNumber, String columnConstant,
                                       Map<String, Integer> columnMap, List<ImportErrorDetail> errors,
                                       String entityName) {
        String errorCode;
        String errorMessage;

        switch (columnConstant) {
            case COLUMN_UNIT_MEASURE:
                errorCode = "UNIT_OF_MEASURE_NOT_FOUND";
                errorMessage = "La unidad de medida " + entityName + ENTITY_NOT_ACTIVE_SUFFIX;
                break;
            case COLUMN_CATEGORY:
                errorCode = "CATEGORY_NOT_FOUND";
                errorMessage = "La categoría " + entityName + ENTITY_NOT_ACTIVE_SUFFIX;
                break;
            case COLUMN_PRODUCT_TYPE:
                errorCode = "PRODUCT_TYPE_NOT_FOUND";
                errorMessage = "El tipo de producto " + entityName + ENTITY_NOT_ACTIVE_SUFFIX_MASC;
                break;
            default:
                errorCode = "ENTITY_NOT_FOUND";
                errorMessage = "La entidad " + entityName + ENTITY_NOT_ACTIVE_SUFFIX;
        }

        errors.add(ImportErrorDetail.builder()
                .rowNumber(rowNumber)
                .columnNumber(columnMap.get(columnConstant) != null ? columnMap.get(columnConstant) + 1 : null)
                .columnName(columnConstant)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .errorType(ImportErrorType.VALIDATION_ERROR)
                .fieldValue(entityName)
                .build());
    }

    /**
     * @brief Crea objeto de error de validación con toda la información necesaria
     * @param rowNumber número de fila donde ocurrió el error
     * @param errorCode código identificador del tipo de error
     * @param message mensaje descriptivo del error
     * @param columnName nombre de la columna donde ocurrió el error
     * @param columnNumber número de columna (índice basado en 0)
     * @param fieldValue valor del campo que causó el error
     * @return objeto ImportErrorDetail completamente configurado
     */
    private ImportErrorDetail createValidationError(int rowNumber, String errorCode, String message,
                                                   String columnName, Integer columnNumber, String fieldValue) {
        return ImportErrorDetail.builder()
                .rowNumber(rowNumber)
                .columnNumber(columnNumber != null ? columnNumber + 1 : null)
                .columnName(columnName)
                .errorCode(errorCode)
                .errorMessage(message)
                .errorType(ImportErrorType.VALIDATION_ERROR)
                .fieldValue(fieldValue)
                .build();
    }


    private boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * @brief Contenedor con resultados de validación por lotes incluyendo estadísticas y errores
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchValidationResult {
        private List<ProductExcelData> validRecords;
        private List<ImportErrorDetail> errors;
        private int duplicateCount;
        private int validCount;
    }
}
package co.unicauca.edu.co.contables.inventory.products.unit.application.service.importExport;

import co.unicauca.edu.co.contables.inventory.products.application.ports.output.ICategoryPersistencePort;
import co.unicauca.edu.co.contables.inventory.products.application.ports.output.IProductPersistencePort;
import co.unicauca.edu.co.contables.inventory.products.application.ports.output.IProductTypePersistencePort;
import co.unicauca.edu.co.contables.inventory.products.application.ports.output.IUnitOfMeasurePersistencePort;
import co.unicauca.edu.co.contables.inventory.products.application.service.importExport.ProductBatchValidationService;
import co.unicauca.edu.co.contables.inventory.products.domain.model.Category;
import co.unicauca.edu.co.contables.inventory.products.domain.model.ProductExcelData;
import co.unicauca.edu.co.contables.inventory.products.domain.model.ProductType;
import co.unicauca.edu.co.contables.inventory.products.domain.model.UnitOfMeasure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductBatchValidationServiceUnitTest {

    @Mock
    private ICategoryPersistencePort categoryPersistencePort;

    @Mock
    private IUnitOfMeasurePersistencePort unitOfMeasurePersistencePort;

    @Mock
    private IProductTypePersistencePort productTypePersistencePort;

    @Mock
    private IProductPersistencePort productPersistencePort;

    @InjectMocks
    private ProductBatchValidationService productBatchValidationService;

    private String testEntId;
    private Map<String, Integer> columnMap;
    private List<ProductExcelData> productsData;

    @BeforeEach
    void setUp() {
        testEntId = "ENT123";
        columnMap = createColumnMap();
        productsData = new ArrayList<>();
    }

    @Test
    @DisplayName("Debe validar batch exitosamente con todos los campos válidos")
    void testValidateBatchSuccess() {
        ProductExcelData product = createValidProductData(1);
        productsData.add(product);

        setupMocksForValidData();

        ProductBatchValidationService.BatchValidationResult result = 
            productBatchValidationService.validateBatch(productsData, testEntId, columnMap);

        assertEquals(1, result.getValidRecords().size());
        assertEquals(0, result.getDuplicateCount());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Debe detectar campos requeridos faltantes")
    void testValidateBatchMissingRequiredFields() {
        ProductExcelData product = ProductExcelData.builder()
                .rowNumber(1)
                .name(null)
                .description(null)
                .reference(null)
                .presentation(null)
                .unitOfMeasureName(null)
                .categoryName(null)
                .productTypeName(null)
                .enterpriseId(testEntId)
                .build();
        productsData.add(product);

        setupMocksForValidData();

        ProductBatchValidationService.BatchValidationResult result = 
            productBatchValidationService.validateBatch(productsData, testEntId, columnMap);

        assertEquals(0, result.getValidRecords().size());
        assertFalse(result.getErrors().isEmpty());
        assertTrue(result.getErrors().size() >= 7);
    }

    @Test
    @DisplayName("Debe detectar cantidad negativa")
    void testValidateBatchNegativeQuantity() {
        ProductExcelData product = createValidProductData(1);
        product.setQuantity(-10);
        productsData.add(product);

        setupMocksForValidData();

        ProductBatchValidationService.BatchValidationResult result = 
            productBatchValidationService.validateBatch(productsData, testEntId, columnMap);

        assertEquals(0, result.getValidRecords().size());
        assertFalse(result.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Debe detectar costo negativo")
    void testValidateBatchNegativeCost() {
        ProductExcelData product = createValidProductData(1);
        product.setCost(-100.0);
        productsData.add(product);

        setupMocksForValidData();

        ProductBatchValidationService.BatchValidationResult result = 
            productBatchValidationService.validateBatch(productsData, testEntId, columnMap);

        assertEquals(0, result.getValidRecords().size());
        assertFalse(result.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Debe detectar referencia demasiado larga")
    void testValidateBatchReferenceTooLong() {
        ProductExcelData product = createValidProductData(1);
        product.setReference("A".repeat(256));
        productsData.add(product);

        setupMocksForValidData();

        ProductBatchValidationService.BatchValidationResult result = 
            productBatchValidationService.validateBatch(productsData, testEntId, columnMap);

        assertEquals(0, result.getValidRecords().size());
        assertFalse(result.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Debe detectar presentación demasiado larga")
    void testValidateBatchPresentationTooLong() {
        ProductExcelData product = createValidProductData(1);
        product.setPresentation("A".repeat(256));
        productsData.add(product);

        setupMocksForValidData();

        ProductBatchValidationService.BatchValidationResult result = 
            productBatchValidationService.validateBatch(productsData, testEntId, columnMap);

        assertEquals(0, result.getValidRecords().size());
        assertFalse(result.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Debe detectar categoría inexistente")
    void testValidateBatchCategoryNotFound() {
        ProductExcelData product = createValidProductData(1);
        productsData.add(product);

        Page<Category> emptyPage = new PageImpl<>(new ArrayList<>());
        when(categoryPersistencePort.getAllCategoriesByState(eq(testEntId), eq(true), any(PageRequest.class)))
            .thenReturn(emptyPage);
        
        setupMocksForOtherEntities();

        ProductBatchValidationService.BatchValidationResult result = 
            productBatchValidationService.validateBatch(productsData, testEntId, columnMap);

        assertEquals(0, result.getValidRecords().size());
        assertFalse(result.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Debe detectar unidad de medida inexistente")
    void testValidateBatchUnitOfMeasureNotFound() {
        ProductExcelData product = createValidProductData(1);
        productsData.add(product);

        Page<UnitOfMeasure> emptyPage = new PageImpl<>(new ArrayList<>());
        when(unitOfMeasurePersistencePort.getAllUnitOfMeasuresByState(eq(testEntId), eq(true), any(PageRequest.class)))
            .thenReturn(emptyPage);
        
        setupMocksForCategories();
        setupMocksForProductTypes();

        ProductBatchValidationService.BatchValidationResult result = 
            productBatchValidationService.validateBatch(productsData, testEntId, columnMap);

        assertEquals(0, result.getValidRecords().size());
        assertFalse(result.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Debe detectar tipo de producto inexistente")
    void testValidateBatchProductTypeNotFound() {
        ProductExcelData product = createValidProductData(1);
        productsData.add(product);

        Page<ProductType> emptyPage = new PageImpl<>(new ArrayList<>());
        when(productTypePersistencePort.findActivatedByEnterpriseId(eq(testEntId), anyInt(), anyInt()))
            .thenReturn(emptyPage);
        
        setupMocksForCategories();
        setupMocksForUnitsOfMeasure();

        ProductBatchValidationService.BatchValidationResult result = 
            productBatchValidationService.validateBatch(productsData, testEntId, columnMap);

        assertEquals(0, result.getValidRecords().size());
        assertFalse(result.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Debe detectar duplicados y omitirlos")
    void testValidateBatchDuplicates() {
        ProductExcelData product = createValidProductData(1);
        productsData.add(product);

        setupMocksForValidData();
        when(productPersistencePort.existsByReferenceAndEnterpriseId("REF001", testEntId)).thenReturn(true);

        ProductBatchValidationService.BatchValidationResult result = 
            productBatchValidationService.validateBatch(productsData, testEntId, columnMap);

        assertEquals(0, result.getValidRecords().size());
        assertEquals(1, result.getDuplicateCount());
    }

    @Test
    @DisplayName("Debe manejar batch vacío")
    void testValidateBatchEmpty() {
        setupMocksForValidData();
        
        ProductBatchValidationService.BatchValidationResult result = 
            productBatchValidationService.validateBatch(productsData, testEntId, columnMap);

        assertEquals(0, result.getValidRecords().size());
        assertEquals(0, result.getDuplicateCount());
        assertTrue(result.getErrors().isEmpty());
    }

    private ProductExcelData createValidProductData(int rowNumber) {
        return ProductExcelData.builder()
                .rowNumber(rowNumber)
                .name("Producto Test")
                .description("Descripción Test")
                .reference("REF001")
                .presentation("Caja x12")
                .quantity(100)
                .cost(150.50)
                .unitOfMeasureName("Unidad")
                .categoryName("Categoría")
                .productTypeName("Tipo")
                .enterpriseId(testEntId)
                .build();
    }

    private Map<String, Integer> createColumnMap() {
        Map<String, Integer> map = new HashMap<>();
        map.put("Nombre", 0);
        map.put("Descripción", 1);
        map.put("Referencia", 2);
        map.put("Presentación", 3);
        map.put("Cantidad", 4);
        map.put("Costo", 5);
        map.put("Unidad de Medida", 6);
        map.put("Categoría", 7);
        map.put("Tipo de Producto", 8);
        return map;
    }

    private void setupMocksForValidData() {
        setupMocksForUnitsOfMeasure();
        setupMocksForCategories();
        setupMocksForProductTypes();
        when(productPersistencePort.existsByReferenceAndEnterpriseId(anyString(), eq(testEntId))).thenReturn(false);
    }

    private void setupMocksForUnitsOfMeasure() {
        List<UnitOfMeasure> units = List.of(
            UnitOfMeasure.builder().id(1L).name("Unidad").abbreviation("UND").state(true).build()
        );
        Page<UnitOfMeasure> page = new PageImpl<>(units);
        when(unitOfMeasurePersistencePort.getAllUnitOfMeasuresByState(eq(testEntId), eq(true), any(PageRequest.class)))
            .thenReturn(page);
    }

    private void setupMocksForCategories() {
        List<Category> categories = List.of(
            Category.builder().id(1L).name("Categoría").state(true).build()
        );
        Page<Category> page = new PageImpl<>(categories);
        when(categoryPersistencePort.getAllCategoriesByState(eq(testEntId), eq(true), any(PageRequest.class)))
            .thenReturn(page);
    }

    private void setupMocksForProductTypes() {
        List<ProductType> types = List.of(
            ProductType.builder().id(1L).name("Tipo").state(true).build()
        );
        Page<ProductType> page = new PageImpl<>(types);
        when(productTypePersistencePort.findActivatedByEnterpriseId(eq(testEntId), anyInt(), anyInt()))
            .thenReturn(page);
    }

    private void setupMocksForOtherEntities() {
        setupMocksForUnitsOfMeasure();
        setupMocksForProductTypes();
        when(productPersistencePort.existsByReferenceAndEnterpriseId(anyString(), eq(testEntId))).thenReturn(false);
    }
}

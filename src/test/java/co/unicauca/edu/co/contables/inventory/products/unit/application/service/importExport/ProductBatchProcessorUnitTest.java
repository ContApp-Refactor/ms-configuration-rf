package co.unicauca.edu.co.contables.inventory.products.unit.application.service.importExport;

import co.unicauca.edu.co.contables.inventory.products.application.ports.output.IProductPersistencePort;
import co.unicauca.edu.co.contables.inventory.products.application.service.importExport.ProductBatchProcessor;
import co.unicauca.edu.co.contables.inventory.products.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.products.domain.model.ProductExcelData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductBatchProcessorUnitTest {

    @Mock
    private IProductPersistencePort productPersistencePort;

    @InjectMocks
    private ProductBatchProcessor productBatchProcessor;

    private String testEntId;
    private List<ProductExcelData> productsData;

    @BeforeEach
    void setUp() {
        testEntId = "ENT123";
        productsData = new ArrayList<>();
    }

    @Test
    @DisplayName("Debe procesar batch exitosamente")
    void testProcessBatchSuccess() {
        productsData.add(createProductData(1, "REF001", "Producto 1"));
        productsData.add(createProductData(2, "REF002", "Producto 2"));

        List<Product> savedProducts = new ArrayList<>();
        savedProducts.add(createProduct(1L, "REF001", "Producto 1"));
        savedProducts.add(createProduct(2L, "REF002", "Producto 2"));

        when(productPersistencePort.saveAll(anyList())).thenReturn(savedProducts);

        ProductBatchProcessor.BatchProcessingResult result = 
            productBatchProcessor.processBatch(productsData, testEntId);

        assertEquals(2, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
        assertTrue(result.getErrors().isEmpty());
        verify(productPersistencePort, times(2)).saveAll(anyList());
    }

    @Test
    @DisplayName("Debe manejar batch vacío")
    void testProcessEmptyBatch() {
        ProductBatchProcessor.BatchProcessingResult result = 
            productBatchProcessor.processBatch(productsData, testEntId);

        assertEquals(0, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
        assertTrue(result.getErrors().isEmpty());
        verify(productPersistencePort, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Debe manejar error durante el guardado en batch")
    void testProcessBatchSaveError() {
        productsData.add(createProductData(1, "REF001", "Producto 1"));

        when(productPersistencePort.saveAll(anyList())).thenThrow(new RuntimeException("Error de BD"));

        ProductBatchProcessor.BatchProcessingResult result = 
            productBatchProcessor.processBatch(productsData, testEntId);

        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
        assertFalse(result.getErrors().isEmpty());
        assertEquals("Error guardando batch: Error de BD", result.getErrors().get(0).getErrorMessage());
    }

    @Test
    @DisplayName("Debe dividir productos en múltiples batches")
    void testProcessMultipleBatches() {
        for (int i = 1; i <= 150; i++) {
            productsData.add(createProductData(i, "REF" + String.format("%03d", i), "Producto " + i));
        }

        List<Product> savedProducts = new ArrayList<>();
        for (int i = 1; i <= 150; i++) {
            savedProducts.add(createProduct((long) i, "REF" + String.format("%03d", i), "Producto " + i));
        }

        when(productPersistencePort.saveAll(anyList())).thenReturn(savedProducts);

        ProductBatchProcessor.BatchProcessingResult result = 
            productBatchProcessor.processBatch(productsData, testEntId);

        assertEquals(150, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
        verify(productPersistencePort, atLeast(2)).saveAll(anyList());
    }

    @Test
    @DisplayName("Debe generar código después del primer save")
    void testGenerateCodeAfterSave() {
        productsData.add(createProductData(1, "REF001", "Producto 1"));

        List<Product> savedProductsFirstSave = new ArrayList<>();
        Product product = createProduct(1L, "REF001", "Producto 1");
        savedProductsFirstSave.add(product);

        when(productPersistencePort.saveAll(anyList())).thenReturn(savedProductsFirstSave);

        ProductBatchProcessor.BatchProcessingResult result = 
            productBatchProcessor.processBatch(productsData, testEntId);

        assertEquals(1, result.getSuccessCount());
        verify(productPersistencePort, times(2)).saveAll(anyList());
    }

    @Test
    @DisplayName("Debe convertir ProductExcelData con costo null a 0.0")
    void testConvertToProductWithNullCost() {
        ProductExcelData excelData = createProductData(1, "REF001", "Producto Test");
        excelData.setCost(null);
        productsData.add(excelData);

        List<Product> savedProducts = new ArrayList<>();
        Product product = createProduct(1L, "REF001", "Producto Test");
        product.setCost(0.0);
        savedProducts.add(product);

        when(productPersistencePort.saveAll(anyList())).thenReturn(savedProducts);

        ProductBatchProcessor.BatchProcessingResult result = 
            productBatchProcessor.processBatch(productsData, testEntId);

        assertEquals(1, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
        assertTrue(result.getErrors().isEmpty());
    }

    private ProductExcelData createProductData(int rowNumber, String reference, String name) {
        return ProductExcelData.builder()
                .rowNumber(rowNumber)
                .reference(reference)
                .name(name)
                .description("Descripción")
                .presentation("Presentación")
                .quantity(10)
                .cost(100.0)
                .unitOfMeasureId(1L)
                .categoryId(2L)
                .productTypeId(3L)
                .enterpriseId(testEntId)
                .build();
    }

    private Product createProduct(Long id, String reference, String name) {
        Product product = Product.builder()
                .id(id)
                .reference(reference)
                .name(name)
                .description("Descripción")
                .presentation("Presentación")
                .quantity(10)
                .cost(100.0)
                .unitOfMeasureId(1L)
                .categoryId(2L)
                .productTypeId(3L)
                .enterpriseId(testEntId)
                .state(true)
                .build();
        return product;
    }
}

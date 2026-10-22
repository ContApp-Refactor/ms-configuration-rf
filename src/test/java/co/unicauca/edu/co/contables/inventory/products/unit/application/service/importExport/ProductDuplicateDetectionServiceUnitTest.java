package co.unicauca.edu.co.contables.inventory.products.unit.application.service.importExport;

import co.unicauca.edu.co.contables.inventory.products.application.ports.output.IProductPersistencePort;
import co.unicauca.edu.co.contables.inventory.products.application.service.importExport.ProductDuplicateDetectionService;
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
class ProductDuplicateDetectionServiceUnitTest {

    @Mock
    private IProductPersistencePort productPersistencePort;

    @InjectMocks
    private ProductDuplicateDetectionService productDuplicateDetectionService;

    private String testEntId;
    private List<ProductExcelData> productsData;

    @BeforeEach
    void setUp() {
        testEntId = "ENT123";
        productsData = new ArrayList<>();
    }

    @Test
    @DisplayName("Debe detectar productos sin duplicados")
    void testDetectDuplicatesNoDuplicates() {
        ProductExcelData product1 = createProductData(1, "REF001", "Producto 1");
        ProductExcelData product2 = createProductData(2, "REF002", "Producto 2");
        productsData.add(product1);
        productsData.add(product2);

        when(productPersistencePort.existsByReferenceAndEnterpriseId("REF001", testEntId)).thenReturn(false);
        when(productPersistencePort.existsByReferenceAndEnterpriseId("REF002", testEntId)).thenReturn(false);

        ProductDuplicateDetectionService.DuplicateDetectionResult result = 
            productDuplicateDetectionService.detectDuplicates(productsData, testEntId);

        assertEquals(2, result.getUniqueRecords().size());
        assertEquals(0, result.getDuplicateCount());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Debe detectar duplicados en el sistema existente")
    void testDetectDuplicatesExistingInSystem() {
        ProductExcelData product1 = createProductData(1, "REF001", "Producto 1");
        ProductExcelData product2 = createProductData(2, "REF002", "Producto 2");
        productsData.add(product1);
        productsData.add(product2);

        when(productPersistencePort.existsByReferenceAndEnterpriseId("REF001", testEntId)).thenReturn(true);
        when(productPersistencePort.existsByReferenceAndEnterpriseId("REF002", testEntId)).thenReturn(false);

        ProductDuplicateDetectionService.DuplicateDetectionResult result = 
            productDuplicateDetectionService.detectDuplicates(productsData, testEntId);

        assertEquals(1, result.getUniqueRecords().size());
        assertEquals(1, result.getDuplicateCount());
        assertEquals("REF002", result.getUniqueRecords().get(0).getReference());
    }

    @Test
    @DisplayName("Debe detectar duplicados dentro del mismo archivo Excel")
    void testDetectDuplicatesWithinFile() {
        ProductExcelData product1 = createProductData(1, "REF001", "Producto 1");
        ProductExcelData product2 = createProductData(2, "REF001", "Producto 1 duplicado");
        productsData.add(product1);
        productsData.add(product2);

        when(productPersistencePort.existsByReferenceAndEnterpriseId("REF001", testEntId)).thenReturn(false);

        ProductDuplicateDetectionService.DuplicateDetectionResult result = 
            productDuplicateDetectionService.detectDuplicates(productsData, testEntId);

        assertEquals(1, result.getUniqueRecords().size());
        assertEquals(1, result.getDuplicateCount());
        assertEquals(1, result.getUniqueRecords().get(0).getRowNumber());
    }

    @Test
    @DisplayName("Debe detectar múltiples duplicados dentro del archivo")
    void testDetectMultipleDuplicatesWithinFile() {
        ProductExcelData product1 = createProductData(1, "REF001", "Producto 1");
        ProductExcelData product2 = createProductData(2, "REF001", "Duplicado 1");
        ProductExcelData product3 = createProductData(3, "REF001", "Duplicado 2");
        productsData.add(product1);
        productsData.add(product2);
        productsData.add(product3);

        when(productPersistencePort.existsByReferenceAndEnterpriseId("REF001", testEntId)).thenReturn(false);

        ProductDuplicateDetectionService.DuplicateDetectionResult result = 
            productDuplicateDetectionService.detectDuplicates(productsData, testEntId);

        assertEquals(1, result.getUniqueRecords().size());
        assertEquals(2, result.getDuplicateCount());
        assertEquals(1, result.getUniqueRecords().get(0).getRowNumber());
    }

    @Test
    @DisplayName("Debe manejar productos con referencias nulas")
    void testDetectDuplicatesWithNullReferences() {
        ProductExcelData product1 = createProductData(1, null, "Producto sin referencia");
        ProductExcelData product2 = createProductData(2, "REF002", "Producto con referencia");
        productsData.add(product1);
        productsData.add(product2);

        when(productPersistencePort.existsByReferenceAndEnterpriseId("REF002", testEntId)).thenReturn(false);

        ProductDuplicateDetectionService.DuplicateDetectionResult result = 
            productDuplicateDetectionService.detectDuplicates(productsData, testEntId);

        assertEquals(2, result.getUniqueRecords().size());
        assertEquals(0, result.getDuplicateCount());
        verify(productPersistencePort, times(1)).existsByReferenceAndEnterpriseId(anyString(), eq(testEntId));
    }

    @Test
    @DisplayName("Debe detectar duplicados mixtos del sistema y archivo")
    void testDetectMixedDuplicates() {
        ProductExcelData product1 = createProductData(1, "REF001", "Producto 1");
        ProductExcelData product2 = createProductData(2, "REF002", "Producto 2");
        ProductExcelData product3 = createProductData(3, "REF002", "Producto 2 duplicado");
        ProductExcelData product4 = createProductData(4, "REF003", "Producto 3");
        productsData.add(product1);
        productsData.add(product2);
        productsData.add(product3);
        productsData.add(product4);

        when(productPersistencePort.existsByReferenceAndEnterpriseId("REF001", testEntId)).thenReturn(true);
        when(productPersistencePort.existsByReferenceAndEnterpriseId("REF002", testEntId)).thenReturn(false);
        when(productPersistencePort.existsByReferenceAndEnterpriseId("REF003", testEntId)).thenReturn(false);

        ProductDuplicateDetectionService.DuplicateDetectionResult result = 
            productDuplicateDetectionService.detectDuplicates(productsData, testEntId);

        assertEquals(2, result.getUniqueRecords().size());
        assertEquals(2, result.getDuplicateCount());
    }

    @Test
    @DisplayName("Debe manejar lista vacía de productos")
    void testDetectDuplicatesEmptyList() {
        ProductDuplicateDetectionService.DuplicateDetectionResult result = 
            productDuplicateDetectionService.detectDuplicates(productsData, testEntId);

        assertEquals(0, result.getUniqueRecords().size());
        assertEquals(0, result.getDuplicateCount());
        assertTrue(result.getErrors().isEmpty());
        verify(productPersistencePort, never()).existsByReferenceAndEnterpriseId(anyString(), anyString());
    }

    @Test
    @DisplayName("Debe detectar todos los productos como duplicados del sistema")
    void testDetectAllDuplicatesInSystem() {
        ProductExcelData product1 = createProductData(1, "REF001", "Producto 1");
        ProductExcelData product2 = createProductData(2, "REF002", "Producto 2");
        productsData.add(product1);
        productsData.add(product2);

        when(productPersistencePort.existsByReferenceAndEnterpriseId("REF001", testEntId)).thenReturn(true);
        when(productPersistencePort.existsByReferenceAndEnterpriseId("REF002", testEntId)).thenReturn(true);

        ProductDuplicateDetectionService.DuplicateDetectionResult result = 
            productDuplicateDetectionService.detectDuplicates(productsData, testEntId);

        assertEquals(0, result.getUniqueRecords().size());
        assertEquals(2, result.getDuplicateCount());
    }

    @Test
    @DisplayName("Debe ignorar productos con referencias vacías")
    void testDetectDuplicatesWithEmptyReferences() {
        ProductExcelData product1 = createProductData(1, "", "Producto vacío");
        ProductExcelData product2 = createProductData(2, "   ", "Producto espacios");
        ProductExcelData product3 = createProductData(3, "REF003", "Producto válido");
        productsData.add(product1);
        productsData.add(product2);
        productsData.add(product3);

        when(productPersistencePort.existsByReferenceAndEnterpriseId("REF003", testEntId)).thenReturn(false);

        ProductDuplicateDetectionService.DuplicateDetectionResult result = 
            productDuplicateDetectionService.detectDuplicates(productsData, testEntId);

        assertEquals(3, result.getUniqueRecords().size());
        assertEquals(0, result.getDuplicateCount());
    }

    private ProductExcelData createProductData(int rowNumber, String reference, String name) {
        return ProductExcelData.builder()
                .rowNumber(rowNumber)
                .reference(reference)
                .name(name)
                .description("Descripción")
                .enterpriseId(testEntId)
                .build();
    }
}

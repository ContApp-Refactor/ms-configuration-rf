package co.unicauca.edu.co.contables.inventory.products.unit.application.service.importExport;

import co.unicauca.edu.co.contables.inventory.products.application.ports.input.ICategoryServicePort;
import co.unicauca.edu.co.contables.inventory.products.application.ports.input.IProductTypeServicePort;
import co.unicauca.edu.co.contables.inventory.products.application.ports.input.IUnitOfMeasureServicePort;
import co.unicauca.edu.co.contables.inventory.products.application.service.importExport.ProductExcelValidationService;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.product.ExcelValidationException;
import co.unicauca.edu.co.contables.inventory.products.domain.model.Category;
import co.unicauca.edu.co.contables.inventory.products.domain.model.ProductType;
import co.unicauca.edu.co.contables.inventory.products.domain.model.UnitOfMeasure;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductExcelValidationServiceUnitTest {

    @Mock
    private ICategoryServicePort categoryServicePort;

    @Mock
    private IProductTypeServicePort productTypeServicePort;

    @Mock
    private IUnitOfMeasureServicePort unitOfMeasureServicePort;

    @InjectMocks
    private ProductExcelValidationService productExcelValidationService;

    private String testEntId;

    @BeforeEach
    void setUp() {
        testEntId = "ENT123";
    }

    @Test
    @DisplayName("Debe obtener opciones de categorías activas")
    void testGetCategoryOptions() {
        List<Category> categories = List.of(
            createCategory(1L, "Categoría 1"),
            createCategory(2L, "Categoría 2")
        );
        Page<Category> page = new PageImpl<>(categories);

        when(categoryServicePort.countActiveCategoriesByEntId(testEntId)).thenReturn(2L);
        when(categoryServicePort.getAllActiveCategoriesByWithSort(eq(testEntId), eq(0), eq(2), eq("name"), eq("asc")))
            .thenReturn(page);

        List<String> options = productExcelValidationService.getCategoryOptions(testEntId);

        assertEquals(2, options.size());
        assertTrue(options.contains("Categoría 1"));
        assertTrue(options.contains("Categoría 2"));
        verify(categoryServicePort, times(1)).countActiveCategoriesByEntId(testEntId);
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay categorías activas")
    void testGetCategoryOptionsEmpty() {
        when(categoryServicePort.countActiveCategoriesByEntId(testEntId)).thenReturn(0L);

        List<String> options = productExcelValidationService.getCategoryOptions(testEntId);

        assertTrue(options.isEmpty());
        verify(categoryServicePort, never()).getAllActiveCategoriesByWithSort(anyString(), anyInt(), anyInt(), anyString(), anyString());
    }

    @Test
    @DisplayName("Debe obtener opciones de tipos de producto activos")
    void testGetProductTypeOptions() {
        List<ProductType> types = List.of(
            createProductType(1L, "Tipo 1"),
            createProductType(2L, "Tipo 2")
        );
        Page<ProductType> page = new PageImpl<>(types);

        when(productTypeServicePort.countActivatedByEnterpriseId(testEntId)).thenReturn(2L);
        when(productTypeServicePort.findActivatedWithPagination(eq(testEntId), eq(0), eq(2)))
            .thenReturn(page);

        List<String> options = productExcelValidationService.getProductTypeOptions(testEntId);

        assertEquals(2, options.size());
        assertTrue(options.contains("Tipo 1"));
        assertTrue(options.contains("Tipo 2"));
        verify(productTypeServicePort, times(1)).countActivatedByEnterpriseId(testEntId);
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay tipos de producto activos")
    void testGetProductTypeOptionsEmpty() {
        when(productTypeServicePort.countActivatedByEnterpriseId(testEntId)).thenReturn(0L);

        List<String> options = productExcelValidationService.getProductTypeOptions(testEntId);

        assertTrue(options.isEmpty());
        verify(productTypeServicePort, never()).findActivatedWithPagination(anyString(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("Debe obtener opciones de unidades de medida activas")
    void testGetUnitOfMeasureOptions() {
        List<UnitOfMeasure> units = List.of(
            createUnitOfMeasure(1L, "Unidad 1"),
            createUnitOfMeasure(2L, "Unidad 2")
        );
        Page<UnitOfMeasure> page = new PageImpl<>(units);

        when(unitOfMeasureServicePort.countActiveUnitOfMeasuresByEntId(testEntId)).thenReturn(2L);
        when(unitOfMeasureServicePort.getAllActiveUnitOfMeasuresBy(eq(testEntId), eq(0), eq(2)))
            .thenReturn(page);

        List<String> options = productExcelValidationService.getUnitOfMeasureOptions(testEntId);

        assertEquals(2, options.size());
        assertTrue(options.contains("Unidad 1"));
        assertTrue(options.contains("Unidad 2"));
        verify(unitOfMeasureServicePort, times(1)).countActiveUnitOfMeasuresByEntId(testEntId);
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay unidades de medida activas")
    void testGetUnitOfMeasureOptionsEmpty() {
        when(unitOfMeasureServicePort.countActiveUnitOfMeasuresByEntId(testEntId)).thenReturn(0L);

        List<String> options = productExcelValidationService.getUnitOfMeasureOptions(testEntId);

        assertTrue(options.isEmpty());
        verify(unitOfMeasureServicePort, never()).getAllActiveUnitOfMeasuresBy(anyString(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("Debe obtener opciones de estado")
    void testGetStatusOptions() {
        List<String> options = productExcelValidationService.getStatusOptions();

        assertEquals(2, options.size());
        assertTrue(options.contains("ACTIVO"));
        assertTrue(options.contains("INACTIVO"));
    }

    @Test
    @DisplayName("Debe aplicar validaciones de producto completas")
    void testApplyProductValidations() {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Productos");

            List<Category> categories = List.of(createCategory(1L, "Cat1"));
            List<ProductType> types = List.of(createProductType(1L, "Type1"));
            List<UnitOfMeasure> units = List.of(createUnitOfMeasure(1L, "Unit1"));

            when(categoryServicePort.countActiveCategoriesByEntId(testEntId)).thenReturn(1L);
            when(categoryServicePort.getAllActiveCategoriesByWithSort(eq(testEntId), eq(0), eq(1), eq("name"), eq("asc")))
                    .thenReturn(new PageImpl<>(categories));
            when(productTypeServicePort.countActivatedByEnterpriseId(testEntId)).thenReturn(1L);
            when(productTypeServicePort.findActivatedWithPagination(eq(testEntId), eq(0), eq(1)))
                    .thenReturn(new PageImpl<>(types));
            when(unitOfMeasureServicePort.countActiveUnitOfMeasuresByEntId(testEntId)).thenReturn(1L);
            when(unitOfMeasureServicePort.getAllActiveUnitOfMeasuresBy(eq(testEntId), eq(0), eq(1)))
                    .thenReturn(new PageImpl<>(units));

            productExcelValidationService.applyProductValidations(sheet, testEntId, 1, 100);

            List<? extends DataValidation> validations = sheet.getDataValidations();
            assertFalse(validations.isEmpty());
            verify(categoryServicePort, times(1)).countActiveCategoriesByEntId(testEntId);
            verify(productTypeServicePort, times(1)).countActivatedByEnterpriseId(testEntId);
            verify(unitOfMeasureServicePort, times(1)).countActiveUnitOfMeasuresByEntId(testEntId);
        } catch (Exception e) {
            fail("No se esperaba excepción: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Debe aplicar validación de código")
    void testApplyCodeValidation() {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Test");

            productExcelValidationService.applyCodeValidation(sheet, 0, 1, 100);

            List<? extends DataValidation> validations = sheet.getDataValidations();
            assertFalse(validations.isEmpty());
            assertEquals(1, validations.size());
        } catch (Exception e) {
            fail("No se esperaba excepción: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Debe lanzar excepción al aplicar validación de código con hoja inválida")
    void testApplyCodeValidationWithInvalidSheet() {
        Sheet invalidSheet = null;

        assertThrows(ExcelValidationException.class, () ->
                productExcelValidationService.applyCodeValidation(invalidSheet, 0, 1, 100)
        );
    }

    @Test
    @DisplayName("Debe aplicar validación de texto")
    void testApplyTextValidation() {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Test");

            productExcelValidationService.applyTextValidation(sheet, 1, 1, 100, "Nombre");

            List<? extends DataValidation> validations = sheet.getDataValidations();
            assertFalse(validations.isEmpty());
            assertEquals(1, validations.size());
        } catch (Exception e) {
            fail("No se esperaba excepción: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Debe lanzar excepción al aplicar validación de texto con hoja inválida")
    void testApplyTextValidationWithInvalidSheet() {
        Sheet invalidSheet = null;

        assertThrows(ExcelValidationException.class, () ->
                productExcelValidationService.applyTextValidation(invalidSheet, 1, 1, 100, "Nombre")
        );
    }

    @Test
    @DisplayName("Debe aplicar validación decimal")
    void testApplyDecimalValidation() {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Test");

            productExcelValidationService.applyDecimalValidation(sheet, 5, 1, 100, "Costo");

            List<? extends DataValidation> validations = sheet.getDataValidations();
            assertFalse(validations.isEmpty());
            assertEquals(1, validations.size());
        } catch (Exception e) {
            fail("No se esperaba excepción: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Debe lanzar excepción al aplicar validación decimal con hoja inválida")
    void testApplyDecimalValidationWithInvalidSheet() {
        Sheet invalidSheet = null;

        assertThrows(ExcelValidationException.class, () ->
                productExcelValidationService.applyDecimalValidation(invalidSheet, 5, 1, 100, "Costo")
        );
    }

    @Test
    @DisplayName("Debe aplicar validación entera")
    void testApplyIntegerValidation() {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Test");

            productExcelValidationService.applyIntegerValidation(sheet, 6, 1, 100, "Cantidad");

            List<? extends DataValidation> validations = sheet.getDataValidations();
            assertFalse(validations.isEmpty());
            assertEquals(1, validations.size());
        } catch (Exception e) {
            fail("No se esperaba excepción: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Debe lanzar excepción al aplicar validación entera con hoja inválida")
    void testApplyIntegerValidationWithInvalidSheet() {
        Sheet invalidSheet = null;

        assertThrows(ExcelValidationException.class, () ->
                productExcelValidationService.applyIntegerValidation(invalidSheet, 6, 1, 100, "Cantidad")
        );
    }

    @Test
    @DisplayName("Debe aplicar validación dropdown con opciones")
    void testApplyDropdownValidation() {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Test");
            List<String> options = List.of("ACTIVO", "INACTIVO");

            productExcelValidationService.applyDropdownValidation(sheet, 10, 1, 100, options, "Seleccione estado");

            List<? extends DataValidation> validations = sheet.getDataValidations();
            assertFalse(validations.isEmpty());
            assertEquals(1, validations.size());
        } catch (Exception e) {
            fail("No se esperaba excepción: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("No debe aplicar validación dropdown cuando opciones es null")
    void testApplyDropdownValidationWithNullOptions() {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Test");

            productExcelValidationService.applyDropdownValidation(sheet, 10, 1, 100, null, "Error");

            List<? extends DataValidation> validations = sheet.getDataValidations();
            assertTrue(validations.isEmpty());
        } catch (Exception e) {
            fail("No se esperaba excepción: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("No debe aplicar validación dropdown cuando opciones está vacía")
    void testApplyDropdownValidationWithEmptyOptions() {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Test");

            productExcelValidationService.applyDropdownValidation(sheet, 10, 1, 100, List.of(), "Error");

            List<? extends DataValidation> validations = sheet.getDataValidations();
            assertTrue(validations.isEmpty());
        } catch (Exception e) {
            fail("No se esperaba excepción: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Debe lanzar excepción al aplicar validación dropdown con hoja inválida")
    void testApplyDropdownValidationWithInvalidSheet() {
        Sheet invalidSheet = null;
        List<String> options = List.of("ACTIVO", "INACTIVO");

        assertThrows(ExcelValidationException.class, () ->
                productExcelValidationService.applyDropdownValidation(invalidSheet, 10, 1, 100, options, "Error")
        );
    }

    private Category createCategory(Long id, String name) {
        return Category.builder()
                .id(id)
                .name(name)
                .state(true)
                .build();
    }

    private ProductType createProductType(Long id, String name) {
        return ProductType.builder()
                .id(id)
                .name(name)
                .state(true)
                .build();
    }

    private UnitOfMeasure createUnitOfMeasure(Long id, String name) {
        return UnitOfMeasure.builder()
                .id(id)
                .name(name)
                .abbreviation("ABV")
                .state(true)
                .build();
    }
}

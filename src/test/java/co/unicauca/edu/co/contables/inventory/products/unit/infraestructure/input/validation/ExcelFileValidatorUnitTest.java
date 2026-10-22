package co.unicauca.edu.co.contables.inventory.products.unit.infraestructure.input.validation;

import co.unicauca.edu.co.contables.inventory.products.domain.exception.product.ProductFileSizeExceededException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.product.ProductFileValidationException;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.config.FileUploadProperties;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.validation.ExcelFileValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ExcelFileValidatorUnitTest {

    @Mock
    private FileUploadProperties fileProperties;

    @InjectMocks
    private ExcelFileValidator excelFileValidator;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_EXTENSIONS = List.of(".xlsx", ".xls");
    private static final List<String> ALLOWED_MIME_TYPES = List.of(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-excel"
    );

    @BeforeEach
    void setUp() {
        when(fileProperties.getMaxSize()).thenReturn(MAX_FILE_SIZE);
        when(fileProperties.getAllowedExtensions()).thenReturn(Map.of("excel", ALLOWED_EXTENSIONS));
        when(fileProperties.getAllowedMimeTypes()).thenReturn(Map.of("excel", ALLOWED_MIME_TYPES));
    }

    // ==================== Tests de validate con archivo válido ====================

    @Test
    @DisplayName("Debe validar archivo Excel válido con extensión .xlsx")
    void testValidate_WithValidXlsxFile_Success() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "productos.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "test content".getBytes()
        );

        // Act & Assert
        assertDoesNotThrow(() -> excelFileValidator.validate(file));
        verify(fileProperties).getMaxSize();
        verify(fileProperties, atLeastOnce()).getAllowedExtensions();
    }

    @Test
    @DisplayName("Debe validar archivo Excel válido con extensión .xls")
    void testValidate_WithValidXlsFile_Success() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "productos.xls",
                "application/vnd.ms-excel",
                "test content".getBytes()
        );

        // Act & Assert
        assertDoesNotThrow(() -> excelFileValidator.validate(file));
    }

    @Test
    @DisplayName("Debe validar archivo con nombre en mayúsculas")
    void testValidate_WithUppercaseExtension_Success() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "PRODUCTOS.XLSX",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "test content".getBytes()
        );

        // Act & Assert
        assertDoesNotThrow(() -> excelFileValidator.validate(file));
    }

    @Test
    @DisplayName("Debe validar archivo con extensión en casos mixtos")
    void testValidate_WithMixedCaseExtension_Success() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "productos.XlSx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "test content".getBytes()
        );

        // Act & Assert
        assertDoesNotThrow(() -> excelFileValidator.validate(file));
    }

    // ==================== Tests de validateNotNull ====================

    @Test
    @DisplayName("Debe lanzar excepción cuando archivo es nulo")
    void testValidate_WithNullFile_ThrowsException() {
        // Arrange
        MultipartFile file = null;

        // Act & Assert
        ProductFileValidationException exception = assertThrows(
                ProductFileValidationException.class,
                () -> excelFileValidator.validate(file)
        );

        assertTrue(exception.getMessage().contains("archivo es nulo") || 
                   exception.getMessage().contains("null"));
    }

    // ==================== Tests de validateNotEmpty ====================

    @Test
    @DisplayName("Debe lanzar excepción cuando archivo está vacío")
    void testValidate_WithEmptyFile_ThrowsException() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "productos.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[0]
        );

        // Act & Assert
        ProductFileValidationException exception = assertThrows(
                ProductFileValidationException.class,
                () -> excelFileValidator.validate(file)
        );

        assertTrue(exception.getMessage().contains("vacío") || 
                   exception.getMessage().contains("empty"));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando archivo tiene tamaño cero")
    void testValidate_WithZeroSizeFile_ThrowsException() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "productos.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "".getBytes()
        );

        // Act & Assert
        assertThrows(ProductFileValidationException.class, () -> excelFileValidator.validate(file));
    }

    // ==================== Tests de validateSize ====================

    @Test
    @DisplayName("Debe lanzar excepción cuando archivo excede tamaño máximo")
    void testValidate_WhenFileSizeExceedsLimit_ThrowsException() {
        // Arrange
        byte[] largeContent = new byte[(int) MAX_FILE_SIZE + 1];
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "productos.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                largeContent
        );

        // Act & Assert
        ProductFileSizeExceededException exception = assertThrows(
                ProductFileSizeExceededException.class,
                () -> excelFileValidator.validate(file)
        );

        assertNotNull(exception.getMessage());
    }

    @Test
    @DisplayName("Debe validar archivo con tamaño exactamente en el límite")
    void testValidate_WithExactMaxSize_Success() {
        // Arrange
        byte[] maxContent = new byte[(int) MAX_FILE_SIZE];
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "productos.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                maxContent
        );

        // Act & Assert
        assertDoesNotThrow(() -> excelFileValidator.validate(file));
    }

    @Test
    @DisplayName("Debe validar archivo con tamaño menor al límite")
    void testValidate_WithSizeBelowLimit_Success() {
        // Arrange
        byte[] smallContent = new byte[1024]; // 1KB
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "productos.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                smallContent
        );

        // Act & Assert
        assertDoesNotThrow(() -> excelFileValidator.validate(file));
    }

    // ==================== Tests de validateExtension ====================

    @Test
    @DisplayName("Debe lanzar excepción cuando archivo no tiene nombre")
    void testValidate_WithNullFilename_ThrowsException() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                null,
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "test content".getBytes()
        );

        // Act & Assert
        ProductFileValidationException exception = assertThrows(
                ProductFileValidationException.class,
                () -> excelFileValidator.validate(file)
        );

        assertTrue(exception.getMessage().contains("extensión") || 
                   exception.getMessage().contains("archivo sin nombre"));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando extensión no es válida")
    void testValidate_WithInvalidExtension_ThrowsException() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "productos.pdf",
                "application/pdf",
                "test content".getBytes()
        );

        // Act & Assert
        ProductFileValidationException exception = assertThrows(
                ProductFileValidationException.class,
                () -> excelFileValidator.validate(file)
        );

        assertTrue(exception.getMessage().contains("extensión") || 
                   exception.getMessage().contains("extension"));
        assertTrue(exception.getMessage().contains("productos.pdf"));
    }

    @Test
    @DisplayName("Debe lanzar excepción con extensión .txt")
    void testValidate_WithTxtExtension_ThrowsException() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "productos.txt",
                "text/plain",
                "test content".getBytes()
        );

        // Act & Assert
        assertThrows(ProductFileValidationException.class, () -> excelFileValidator.validate(file));
    }

    @Test
    @DisplayName("Debe lanzar excepción con extensión .csv")
    void testValidate_WithCsvExtension_ThrowsException() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "productos.csv",
                "text/csv",
                "test content".getBytes()
        );

        // Act & Assert
        assertThrows(ProductFileValidationException.class, () -> excelFileValidator.validate(file));
    }

    @Test
    @DisplayName("Debe validar archivo sin extensión que termine en .xlsx")
    void testValidate_WithFilenameEndingInXlsx_Success() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "archivo_backup_2024.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "test content".getBytes()
        );

        // Act & Assert
        assertDoesNotThrow(() -> excelFileValidator.validate(file));
    }

    // ==================== Tests de getSupportedMimeTypes ====================

    @Test
    @DisplayName("Debe retornar tipos MIME soportados")
    void testGetSupportedMimeTypes_ReturnsConfiguredMimeTypes() {
        // Act
        String[] mimeTypes = excelFileValidator.getSupportedMimeTypes();

        // Assert
        assertNotNull(mimeTypes);
        assertEquals(2, mimeTypes.length);
        assertTrue(List.of(mimeTypes).contains("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        assertTrue(List.of(mimeTypes).contains("application/vnd.ms-excel"));
        verify(fileProperties).getAllowedMimeTypes();
    }

    @Test
    @DisplayName("Debe retornar array vacío cuando no hay tipos MIME configurados")
    void testGetSupportedMimeTypes_WithNullConfig_ReturnsEmptyArray() {
        // Arrange
        when(fileProperties.getAllowedMimeTypes()).thenReturn(Map.of());

        // Act
        String[] mimeTypes = excelFileValidator.getSupportedMimeTypes();

        // Assert
        assertNotNull(mimeTypes);
        assertEquals(0, mimeTypes.length);
    }

    @Test
    @DisplayName("Debe retornar array vacío cuando configuración de excel es nula")
    void testGetSupportedMimeTypes_WithNullExcelConfig_ReturnsEmptyArray() {
        // Arrange
        when(fileProperties.getAllowedMimeTypes()).thenReturn(Map.of("other", List.of("type")));

        // Act
        String[] mimeTypes = excelFileValidator.getSupportedMimeTypes();

        // Assert
        assertNotNull(mimeTypes);
        assertEquals(0, mimeTypes.length);
    }

    // ==================== Tests de getSupportedExtensions ====================

    @Test
    @DisplayName("Debe retornar extensiones soportadas")
    void testGetSupportedExtensions_ReturnsConfiguredExtensions() {
        // Act
        String[] extensions = excelFileValidator.getSupportedExtensions();

        // Assert
        assertNotNull(extensions);
        assertEquals(2, extensions.length);
        assertTrue(List.of(extensions).contains(".xlsx"));
        assertTrue(List.of(extensions).contains(".xls"));
        verify(fileProperties).getAllowedExtensions();
    }

    @Test
    @DisplayName("Debe retornar array vacío cuando no hay extensiones configuradas")
    void testGetSupportedExtensions_WithNullConfig_ReturnsEmptyArray() {
        // Arrange
        when(fileProperties.getAllowedExtensions()).thenReturn(Map.of());

        // Act
        String[] extensions = excelFileValidator.getSupportedExtensions();

        // Assert
        assertNotNull(extensions);
        assertEquals(0, extensions.length);
    }

    @Test
    @DisplayName("Debe retornar array vacío cuando configuración de excel es nula")
    void testGetSupportedExtensions_WithNullExcelConfig_ReturnsEmptyArray() {
        // Arrange
        when(fileProperties.getAllowedExtensions()).thenReturn(Map.of("other", List.of("ext")));

        // Act
        String[] extensions = excelFileValidator.getSupportedExtensions();

        // Assert
        assertNotNull(extensions);
        assertEquals(0, extensions.length);
    }

    // ==================== Tests de integración de validaciones ====================

    @Test
    @DisplayName("Debe ejecutar todas las validaciones en orden correcto")
    void testValidate_ExecutesAllValidationsInOrder() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "productos.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "test content".getBytes()
        );

        // Act
        excelFileValidator.validate(file);

        // Assert
        verify(fileProperties).getMaxSize();
        verify(fileProperties, atLeastOnce()).getAllowedExtensions();
    }

    @Test
    @DisplayName("Debe fallar en primera validación sin ejecutar siguientes")
    void testValidate_WithNullFile_DoesNotExecuteOtherValidations() {
        // Arrange
        MultipartFile file = null;

        // Act & Assert
        assertThrows(ProductFileValidationException.class, () -> excelFileValidator.validate(file));
        verify(fileProperties, never()).getMaxSize();
        verify(fileProperties, never()).getAllowedExtensions();
    }

    @Test
    @DisplayName("Debe validar archivo con nombre complejo")
    void testValidate_WithComplexFilename_Success() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "productos_backup_2024-11-27_v2.final.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "test content".getBytes()
        );

        // Act & Assert
        assertDoesNotThrow(() -> excelFileValidator.validate(file));
    }

    @Test
    @DisplayName("Debe validar archivo con espacios en el nombre")
    void testValidate_WithSpacesInFilename_Success() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "mis productos 2024.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "test content".getBytes()
        );

        // Act & Assert
        assertDoesNotThrow(() -> excelFileValidator.validate(file));
    }

    @Test
    @DisplayName("Debe validar archivo con caracteres especiales en el nombre")
    void testValidate_WithSpecialCharactersInFilename_Success() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "productos_áéíóú_ñ.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "test content".getBytes()
        );

        // Act & Assert
        assertDoesNotThrow(() -> excelFileValidator.validate(file));
    }

    @Test
    @DisplayName("Debe invocar getAllowedExtensions al validar extensión")
    void testValidate_InvokesGetAllowedExtensions() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "productos.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "test content".getBytes()
        );

        // Act
        excelFileValidator.validate(file);

        // Assert
        verify(fileProperties, atLeastOnce()).getAllowedExtensions();
    }
}

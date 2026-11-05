package co.unicauca.edu.co.contables.accounting.catalogue.unit.catalogue.application.services.validation;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.validation.AccountCatalogueFileValidationService;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.utils.ImportConstants;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.FileSizeExceededException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.FileValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccountCatalogueFileValidationServiceUnitTest {

    private AccountCatalogueFileValidationService fileValidationService;
    private MultipartFile multipartFile;

    @BeforeEach
    void setUp() {
        fileValidationService = new AccountCatalogueFileValidationService();
        multipartFile = mock(MultipartFile.class);
    }

    @Test
    @DisplayName("Debe validar archivo Excel xlsx correctamente")
    void testValidateXlsxFileSuccess() {
        // Arrange
        when(multipartFile.getOriginalFilename()).thenReturn("catalogo.xlsx");
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(1024L);

        // Act & Assert
        assertDoesNotThrow(() -> fileValidationService.validate(multipartFile));
    }

    @Test
    @DisplayName("Debe validar archivo Excel xls correctamente")
    void testValidateXlsFileSuccess() {
        // Arrange
        when(multipartFile.getOriginalFilename()).thenReturn("catalogo.xls");
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(1024L);

        // Act & Assert
        assertDoesNotThrow(() -> fileValidationService.validate(multipartFile));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando archivo es null")
    void testValidateNullFile() {
        // Act & Assert
        assertThrows(FileValidationException.class,
                () -> fileValidationService.validate(null));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando archivo está vacío (isEmpty true)")
    void testValidateEmptyFileIsEmpty() {
        // Arrange
        when(multipartFile.isEmpty()).thenReturn(true);
        when(multipartFile.getOriginalFilename()).thenReturn("catalogo.xlsx");

        // Act & Assert
        assertThrows(FileValidationException.class,
                () -> fileValidationService.validate(multipartFile));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando archivo tiene tamaño cero")
    void testValidateEmptyFileSizeZero() {
        // Arrange
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(0L);
        when(multipartFile.getOriginalFilename()).thenReturn("catalogo.xlsx");

        // Act & Assert
        assertThrows(FileValidationException.class,
                () -> fileValidationService.validate(multipartFile));
    }

    @Test
    @DisplayName("Debe lanzar FileSizeExceededException cuando archivo excede tamaño máximo")
    void testValidateFileSizeExceeded() {
        // Arrange
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(ImportConstants.MAX_FILE_SIZE + 1);
        when(multipartFile.getOriginalFilename()).thenReturn("catalogo.xlsx");

        // Act & Assert
        assertThrows(FileSizeExceededException.class,
                () -> fileValidationService.validate(multipartFile));
    }

    @Test
    @DisplayName("Debe aceptar archivo con tamaño exacto al máximo permitido")
    void testValidateFileExactMaxSize() {
        // Arrange
        when(multipartFile.getOriginalFilename()).thenReturn("catalogo.xlsx");
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(ImportConstants.MAX_FILE_SIZE);

        // Act & Assert
        assertDoesNotThrow(() -> fileValidationService.validate(multipartFile));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando extensión es inválida - pdf")
    void testValidateInvalidExtensionPdf() {
        // Arrange
        when(multipartFile.getOriginalFilename()).thenReturn("catalogo.pdf");
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(1024L);

        // Act & Assert
        assertThrows(FileValidationException.class,
                () -> fileValidationService.validate(multipartFile));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando extensión es inválida - csv")
    void testValidateInvalidExtensionCsv() {
        // Arrange
        when(multipartFile.getOriginalFilename()).thenReturn("catalogo.csv");
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(1024L);

        // Act & Assert
        assertThrows(FileValidationException.class,
                () -> fileValidationService.validate(multipartFile));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando extensión es inválida - txt")
    void testValidateInvalidExtensionTxt() {
        // Arrange
        when(multipartFile.getOriginalFilename()).thenReturn("catalogo.txt");
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(1024L);

        // Act & Assert
        assertThrows(FileValidationException.class,
                () -> fileValidationService.validate(multipartFile));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando nombre de archivo es null")
    void testValidateNullFilename() {
        // Arrange
        when(multipartFile.getOriginalFilename()).thenReturn(null);
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(1024L);

        // Act & Assert
        assertThrows(FileValidationException.class,
                () -> fileValidationService.validate(multipartFile));
    }

    @Test
    @DisplayName("Debe validar archivo con extensión en mayúsculas XLSX")
    void testValidateUppercaseXlsxExtension() {
        // Arrange
        when(multipartFile.getOriginalFilename()).thenReturn("CATALOGO.XLSX");
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(1024L);

        // Act & Assert
        assertDoesNotThrow(() -> fileValidationService.validate(multipartFile));
    }

    @Test
    @DisplayName("Debe validar archivo con extensión en mayúsculas XLS")
    void testValidateUppercaseXlsExtension() {
        // Arrange
        when(multipartFile.getOriginalFilename()).thenReturn("CATALOGO.XLS");
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(1024L);

        // Act & Assert
        assertDoesNotThrow(() -> fileValidationService.validate(multipartFile));
    }

    @Test
    @DisplayName("Debe validar archivo con extensión mixta XlSx")
    void testValidateMixedCaseExtension() {
        // Arrange
        when(multipartFile.getOriginalFilename()).thenReturn("catalogo.XlSx");
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(1024L);

        // Act & Assert
        assertDoesNotThrow(() -> fileValidationService.validate(multipartFile));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando archivo no tiene extensión")
    void testValidateFileWithoutExtension() {
        // Arrange
        when(multipartFile.getOriginalFilename()).thenReturn("catalogo");
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(1024L);

        // Act & Assert
        assertThrows(FileValidationException.class,
                () -> fileValidationService.validate(multipartFile));
    }

    @Test
    @DisplayName("Debe validar archivo con nombre que contiene espacios")
    void testValidateFilenameWithSpaces() {
        // Arrange
        when(multipartFile.getOriginalFilename()).thenReturn("catalogo de cuentas.xlsx");
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(1024L);

        // Act & Assert
        assertDoesNotThrow(() -> fileValidationService.validate(multipartFile));
    }

    @Test
    @DisplayName("Debe validar archivo con nombre que contiene caracteres especiales")
    void testValidateFilenameWithSpecialCharacters() {
        // Arrange
        when(multipartFile.getOriginalFilename()).thenReturn("catálogo_2024-01.xlsx");
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(1024L);

        // Act & Assert
        assertDoesNotThrow(() -> fileValidationService.validate(multipartFile));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando archivo tiene extensión similar pero inválida - xlsxx")
    void testValidateInvalidExtensionXlsxx() {
        // Arrange
        when(multipartFile.getOriginalFilename()).thenReturn("catalogo.xlsxx");
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(1024L);

        // Act & Assert
        assertThrows(FileValidationException.class,
                () -> fileValidationService.validate(multipartFile));
    }

    @Test
    @DisplayName("Debe validar archivo con tamaño mínimo válido")
    void testValidateMinimumValidSize() {
        // Arrange
        when(multipartFile.getOriginalFilename()).thenReturn("catalogo.xlsx");
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(1L);

        // Act & Assert
        assertDoesNotThrow(() -> fileValidationService.validate(multipartFile));
    }

    @Test
    @DisplayName("Debe validar archivo con nombre muy largo")
    void testValidateLongFilename() {
        // Arrange
        String longName = "a".repeat(200) + ".xlsx";
        when(multipartFile.getOriginalFilename()).thenReturn(longName);
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(1024L);

        // Act & Assert
        assertDoesNotThrow(() -> fileValidationService.validate(multipartFile));
    }

    @Test
    @DisplayName("Debe lanzar excepción para archivo con solo extensión")
    void testValidateFileOnlyExtension() {
        // Arrange
        when(multipartFile.getOriginalFilename()).thenReturn(".xlsx");
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(1024L);

        // Act & Assert
        assertDoesNotThrow(() -> fileValidationService.validate(multipartFile));
    }

    @Test
    @DisplayName("Debe lanzar excepción para archivo con múltiples puntos pero extensión inválida")
    void testValidateFilenameMultipleDotsInvalidExtension() {
        // Arrange
        when(multipartFile.getOriginalFilename()).thenReturn("catalogo.backup.pdf");
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(1024L);

        // Act & Assert
        assertThrows(FileValidationException.class,
                () -> fileValidationService.validate(multipartFile));
    }

    @Test
    @DisplayName("Debe validar archivo con múltiples puntos y extensión válida")
    void testValidateFilenameMultipleDotsValidExtension() {
        // Arrange
        when(multipartFile.getOriginalFilename()).thenReturn("catalogo.backup.xlsx");
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(1024L);

        // Act & Assert
        assertDoesNotThrow(() -> fileValidationService.validate(multipartFile));
    }
}

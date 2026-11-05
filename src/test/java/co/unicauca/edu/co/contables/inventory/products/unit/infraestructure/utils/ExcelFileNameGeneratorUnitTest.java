package co.unicauca.edu.co.contables.inventory.products.unit.infraestructure.utils;

import co.unicauca.edu.co.contables.inventory.products.infraestructure.utils.ExcelFileNameGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExcelFileNameGeneratorUnitTest {

    private ExcelFileNameGenerator excelFileNameGenerator;

    @BeforeEach
    void setUp() {
        excelFileNameGenerator = new ExcelFileNameGenerator();
    }

    // ==================== Tests de generateTemplateFileName ====================

    @Test
    @DisplayName("Debe generar nombre de plantilla con formato correcto")
    void testGenerateTemplateFileName_ReturnsCorrectFormat() {
        // Act
        String result = excelFileNameGenerator.generateTemplateFileName();

        // Assert
        assertNotNull(result);
        assertTrue(result.startsWith("Plantilla_Productos_"));
        assertTrue(result.endsWith(".xlsx"));
    }

    @Test
    @DisplayName("Debe incluir timestamp en nombre de plantilla")
    void testGenerateTemplateFileName_IncludesTimestamp() {
        // Act
        String result = excelFileNameGenerator.generateTemplateFileName();

        // Assert
        String timestamp = result.substring("Plantilla_Productos_".length(), result.length() - ".xlsx".length());
        assertTrue(timestamp.matches("\\d{8}_\\d{6}"));
    }

    @Test
    @DisplayName("Debe generar nombres diferentes en llamadas consecutivas")
    void testGenerateTemplateFileName_GeneratesDifferentNames() throws InterruptedException {
        // Act
        String result1 = excelFileNameGenerator.generateTemplateFileName();
        Thread.sleep(1100); // Esperar más de 1 segundo para garantizar timestamp diferente
        String result2 = excelFileNameGenerator.generateTemplateFileName();

        // Assert
        assertNotEquals(result1, result2);
    }

    // ==================== Tests de generateExportFileName ====================

    @Test
    @DisplayName("Debe generar nombre básico sin parámetros opcionales")
    void testGenerateExportFileName_WithOnlyEntId_ReturnsBasicName() {
        // Act
        String result = excelFileNameGenerator.generateExportFileName("ENT-001", null, null);

        // Assert
        assertNotNull(result);
        assertTrue(result.startsWith("Productos_"));
        assertTrue(result.endsWith(".xlsx"));
        assertFalse(result.contains("activos"));
        assertFalse(result.contains("inactivos"));
    }

    @Test
    @DisplayName("Debe incluir nombre de empresa en nombre de archivo")
    void testGenerateExportFileName_WithCompanyName_IncludesCompanyName() {
        // Act
        String result = excelFileNameGenerator.generateExportFileName("ENT-001", "Mi Empresa", null);

        // Assert
        assertTrue(result.contains("Mi_Empresa"));
    }

    @Test
    @DisplayName("Debe incluir estado activos en nombre de archivo")
    void testGenerateExportFileName_WithActiveStatus_IncludesActivos() {
        // Act
        String result = excelFileNameGenerator.generateExportFileName("ENT-001", null, true);

        // Assert
        assertTrue(result.contains("activos"));
    }

    @Test
    @DisplayName("Debe incluir estado inactivos en nombre de archivo")
    void testGenerateExportFileName_WithInactiveStatus_IncludesInactivos() {
        // Act
        String result = excelFileNameGenerator.generateExportFileName("ENT-001", null, false);

        // Assert
        assertTrue(result.contains("inactivos"));
    }

    @Test
    @DisplayName("Debe generar nombre completo con todos los parámetros")
    void testGenerateExportFileName_WithAllParameters_ReturnsCompleteFileName() {
        // Act
        String result = excelFileNameGenerator.generateExportFileName("ENT-001", "Acme Corp", true);

        // Assert
        assertTrue(result.startsWith("Productos_Acme_Corp_activos_"));
        assertTrue(result.endsWith(".xlsx"));
    }

    @Test
    @DisplayName("Debe normalizar espacios en nombre de empresa")
    void testGenerateExportFileName_NormalizesSpaces_InCompanyName() {
        // Act
        String result = excelFileNameGenerator.generateExportFileName("ENT-001", "Empresa Con Espacios", null);

        // Assert
        assertTrue(result.contains("Empresa_Con_Espacios"));
        assertFalse(result.contains("Empresa Con Espacios"));
    }

    @Test
    @DisplayName("Debe manejar nombre de empresa vacío")
    void testGenerateExportFileName_WithEmptyCompanyName_ExcludesCompanyName() {
        // Act
        String result = excelFileNameGenerator.generateExportFileName("ENT-001", "", null);

        // Assert
        assertTrue(result.startsWith("Productos_"));
        assertFalse(result.contains("__")); // No debe tener guiones bajos dobles
    }

    @Test
    @DisplayName("Debe manejar nombre de empresa con solo espacios")
    void testGenerateExportFileName_WithWhitespaceCompanyName_ExcludesCompanyName() {
        // Act
        String result = excelFileNameGenerator.generateExportFileName("ENT-001", "   ", null);

        // Assert
        assertTrue(result.startsWith("Productos_"));
        assertFalse(result.contains("___"));
    }

    @Test
    @DisplayName("Debe incluir timestamp en formato correcto")
    void testGenerateExportFileName_IncludesCorrectTimestamp() {
        // Act
        String result = excelFileNameGenerator.generateExportFileName("ENT-001", null, null);

        // Assert
        // El formato es: Productos_yyyyMMdd_HHmmss.xlsx
        assertTrue(result.matches("Productos_\\d{8}_\\d{6}\\.xlsx"));
    }

    @Test
    @DisplayName("Debe generar nombres diferentes para múltiples exportaciones")
    void testGenerateExportFileName_GeneratesDifferentNames() throws InterruptedException {
        // Act
        String result1 = excelFileNameGenerator.generateExportFileName("ENT-001", "Empresa", true);
        Thread.sleep(1100); // Esperar más de 1 segundo
        String result2 = excelFileNameGenerator.generateExportFileName("ENT-001", "Empresa", true);

        // Assert
        assertNotEquals(result1, result2);
    }

    @Test
    @DisplayName("Debe generar nombre con empresa y estado inactivo")
    void testGenerateExportFileName_WithCompanyAndInactive_ReturnsCorrectName() {
        // Act
        String result = excelFileNameGenerator.generateExportFileName("ENT-002", "Tech Solutions", false);

        // Assert
        assertTrue(result.contains("Productos_Tech_Solutions_inactivos_"));
        assertTrue(result.endsWith(".xlsx"));
    }

    @Test
    @DisplayName("Debe generar archivo sin estado cuando status es null")
    void testGenerateExportFileName_WithNullStatus_ExcludesStatus() {
        // Act
        String result = excelFileNameGenerator.generateExportFileName("ENT-001", "Mi Empresa", null);

        // Assert
        assertFalse(result.contains("activos"));
        assertFalse(result.contains("inactivos"));
        assertTrue(result.contains("Mi_Empresa"));
    }

    @Test
    @DisplayName("Debe manejar caracteres especiales en nombre de empresa")
    void testGenerateExportFileName_WithSpecialCharacters_NormalizesName() {
        // Act
        String result = excelFileNameGenerator.generateExportFileName("ENT-001", "Empresa & Asociados S.A.", null);

        // Assert
        assertTrue(result.contains("Empresa_&_Asociados_S.A."));
    }

    @Test
    @DisplayName("Debe generar nombre válido sin entId en el nombre del archivo")
    void testGenerateExportFileName_DoesNotIncludeEntId_InFileName() {
        // Act
        String result = excelFileNameGenerator.generateExportFileName("ENT-999", null, null);

        // Assert
        assertFalse(result.contains("ENT-999"));
        assertTrue(result.startsWith("Productos_"));
    }

    // ==================== Tests de timestamp ====================

    @Test
    @DisplayName("Debe generar timestamp con formato válido yyyyMMdd_HHmmss")
    void testTimestampFormat_IsValid() {
        // Act
        String templateName = excelFileNameGenerator.generateTemplateFileName();
        String exportName = excelFileNameGenerator.generateExportFileName("ENT-001", null, null);

        // Assert
        assertTrue(templateName.matches(".*\\d{8}_\\d{6}\\.xlsx"));
        assertTrue(exportName.matches(".*\\d{8}_\\d{6}\\.xlsx"));
    }

    @Test
    @DisplayName("Debe generar timestamp consistente en misma ejecución")
    void testTimestamp_IsConsistentInSameSecond() {
        // Act
        String result1 = excelFileNameGenerator.generateTemplateFileName();
        String result2 = excelFileNameGenerator.generateTemplateFileName();

        // Assert
        String timestamp1 = result1.substring("Plantilla_Productos_".length(), result1.length() - ".xlsx".length());
        String timestamp2 = result2.substring("Plantilla_Productos_".length(), result2.length() - ".xlsx".length());
        
        // Los timestamps deben ser iguales o diferir solo en segundos si se ejecutan rápido
        String date1 = timestamp1.substring(0, 8);
        String date2 = timestamp2.substring(0, 8);
        assertEquals(date1, date2); // La fecha debe ser la misma
    }
}

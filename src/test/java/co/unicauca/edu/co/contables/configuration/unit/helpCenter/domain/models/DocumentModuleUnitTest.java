package co.unicauca.edu.co.contables.configuration.unit.helpCenter.domain.models;

import co.unicauca.edu.co.contables.configuration.commons.exceptions.helpCenter.InvalidModuleException;
import co.unicauca.edu.co.contables.configuration.helpCenter.domain.enums.DocumentModule;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DocumentModuleUnitTest {

    // ========== FROM ID TESTS ==========

    @Test
    @DisplayName("fromId - Debe retornar INVENTARIO_PROMEDIO_PONDERADO para ID 1")
    void testFromIdReturnsInventarioPromedioPonderado() {
        // Arrange
        Integer id = 1;

        // Act
        DocumentModule result = DocumentModule.fromId(id);

        // Assert
        assertEquals(DocumentModule.INVENTARIO_PROMEDIO_PONDERADO, result);
        assertEquals("Inventario promedio ponderado", result.getName());
    }

    @Test
    @DisplayName("fromId - Debe retornar INVENTARIO_PEPS para ID 2")
    void testFromIdReturnsInventarioPeps() {
        // Arrange
        Integer id = 2;

        // Act
        DocumentModule result = DocumentModule.fromId(id);

        // Assert
        assertEquals(DocumentModule.INVENTARIO_PEPS, result);
    }

    @Test
    @DisplayName("fromId - Debe retornar COMERCIAL para ID 3")
    void testFromIdReturnsComercial() {
        // Arrange
        Integer id = 3;

        // Act
        DocumentModule result = DocumentModule.fromId(id);

        // Assert
        assertEquals(DocumentModule.COMERCIAL, result);
    }

    @Test
    @DisplayName("fromId - Debe retornar TESORERIA para ID 4")
    void testFromIdReturnsTesoreria() {
        // Arrange
        Integer id = 4;

        // Act
        DocumentModule result = DocumentModule.fromId(id);

        // Assert
        assertEquals(DocumentModule.TESORERIA, result);
    }

    @Test
    @DisplayName("fromId - Debe retornar CARTERA para ID 5")
    void testFromIdReturnsCartera() {
        // Arrange
        Integer id = 5;

        // Act
        DocumentModule result = DocumentModule.fromId(id);

        // Assert
        assertEquals(DocumentModule.CARTERA, result);
    }

    @Test
    @DisplayName("fromId - Debe retornar CONTABLE_COMERCIAL para ID 6")
    void testFromIdReturnsContableComercial() {
        // Arrange
        Integer id = 6;

        // Act
        DocumentModule result = DocumentModule.fromId(id);

        // Assert
        assertEquals(DocumentModule.CONTABLE_COMERCIAL, result);
    }

    @Test
    @DisplayName("fromId - Debe retornar CONTABLE_CARTERA para ID 7")
    void testFromIdReturnsContableCartera() {
        // Arrange
        Integer id = 7;

        // Act
        DocumentModule result = DocumentModule.fromId(id);

        // Assert
        assertEquals(DocumentModule.CONTABLE_CARTERA, result);
    }

    @Test
    @DisplayName("fromId - Debe retornar ESTADOS_FINANCIEROS para ID 8")
    void testFromIdReturnsEstadosFinancieros() {
        // Arrange
        Integer id = 8;

        // Act
        DocumentModule result = DocumentModule.fromId(id);

        // Assert
        assertEquals(DocumentModule.ESTADOS_FINANCIEROS, result);
    }

    @Test
    @DisplayName("fromId - Debe retornar CONFIGURACION para ID 9")
    void testFromIdReturnsConfiguracion() {
        // Arrange
        Integer id = 9;

        // Act
        DocumentModule result = DocumentModule.fromId(id);

        // Assert
        assertEquals(DocumentModule.CONFIGURACION, result);
    }

    @Test
    @DisplayName("fromId - Debe lanzar excepción cuando ID es null")
    void testFromIdThrowsExceptionWhenNull() {
        // Arrange
        Integer id = null;

        // Act & Assert
        assertThrows(InvalidModuleException.class, () -> DocumentModule.fromId(id));
    }

    @Test
    @DisplayName("fromId - Debe lanzar excepción cuando ID no existe")
    void testFromIdThrowsExceptionWhenInvalidId() {
        // Arrange
        Integer id = 999;

        // Act & Assert
        assertThrows(InvalidModuleException.class, () -> DocumentModule.fromId(id));
    }

    @Test
    @DisplayName("fromId - Debe lanzar excepción cuando ID es negativo")
    void testFromIdThrowsExceptionWhenNegativeId() {
        // Arrange
        Integer id = -1;

        // Act & Assert
        assertThrows(InvalidModuleException.class, () -> DocumentModule.fromId(id));
    }

    @Test
    @DisplayName("fromId - Debe lanzar excepción cuando ID es cero")
    void testFromIdThrowsExceptionWhenZeroId() {
        // Arrange
        Integer id = 0;

        // Act & Assert
        assertThrows(InvalidModuleException.class, () -> DocumentModule.fromId(id));
    }

    // ========== FROM NAME TESTS ==========

    @Test
    @DisplayName("fromName - Debe retornar módulo con nombre exacto")
    void testFromNameWithExactName() {
        // Arrange
        String name = "Inventario promedio ponderado";

        // Act
        DocumentModule result = DocumentModule.fromName(name);

        // Assert
        assertEquals(DocumentModule.INVENTARIO_PROMEDIO_PONDERADO, result);
    }

    @Test
    @DisplayName("fromName - Debe retornar módulo con nombre en mayúsculas")
    void testFromNameWithUppercaseName() {
        // Arrange
        String name = "INVENTARIO PROMEDIO PONDERADO";

        // Act
        DocumentModule result = DocumentModule.fromName(name);

        // Assert
        assertEquals(DocumentModule.INVENTARIO_PROMEDIO_PONDERADO, result);
    }

    @Test
    @DisplayName("fromName - Debe retornar módulo con nombre en minúsculas")
    void testFromNameWithLowercaseName() {
        // Arrange
        String name = "inventario promedio ponderado";

        // Act
        DocumentModule result = DocumentModule.fromName(name);

        // Assert
        assertEquals(DocumentModule.INVENTARIO_PROMEDIO_PONDERADO, result);
    }

    @Test
    @DisplayName("fromName - Debe retornar módulo con espacios extra")
    void testFromNameWithExtraSpaces() {
        // Arrange
        String name = "  Inventario   promedio   ponderado  ";

        // Act
        DocumentModule result = DocumentModule.fromName(name);

        // Assert
        assertEquals(DocumentModule.INVENTARIO_PROMEDIO_PONDERADO, result);
    }

    @Test
    @DisplayName("fromName - Debe retornar TESORERIA con tilde")
    void testFromNameWithAccent() {
        // Arrange
        String name = "Tesorería";

        // Act
        DocumentModule result = DocumentModule.fromName(name);

        // Assert
        assertEquals(DocumentModule.TESORERIA, result);
    }

    @Test
    @DisplayName("fromName - Debe lanzar excepción cuando nombre es null")
    void testFromNameThrowsExceptionWhenNull() {
        // Arrange
        String name = null;

        // Act & Assert
        assertThrows(InvalidModuleException.class, () -> DocumentModule.fromName(name));
    }

    @Test
    @DisplayName("fromName - Debe lanzar excepción cuando nombre está vacío")
    void testFromNameThrowsExceptionWhenEmpty() {
        // Arrange
        String name = "";

        // Act & Assert
        assertThrows(InvalidModuleException.class, () -> DocumentModule.fromName(name));
    }

    @Test
    @DisplayName("fromName - Debe lanzar excepción cuando nombre tiene solo espacios")
    void testFromNameThrowsExceptionWhenOnlySpaces() {
        // Arrange
        String name = "   ";

        // Act & Assert
        assertThrows(InvalidModuleException.class, () -> DocumentModule.fromName(name));
    }

    @Test
    @DisplayName("fromName - Debe lanzar excepción cuando nombre no existe")
    void testFromNameThrowsExceptionWhenInvalidName() {
        // Arrange
        String name = "Módulo inexistente";

        // Act & Assert
        assertThrows(InvalidModuleException.class, () -> DocumentModule.fromName(name));
    }

    // ========== IS VALID NAME TESTS ==========

    @Test
    @DisplayName("isValidName - Debe retornar true para nombre válido")
    void testIsValidNameReturnsTrue() {
        // Arrange
        String name = "Inventario promedio ponderado";

        // Act
        boolean result = DocumentModule.isValidName(name);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("isValidName - Debe retornar true para nombre en mayúsculas")
    void testIsValidNameReturnsTrueForUppercase() {
        // Arrange
        String name = "COMERCIAL";

        // Act
        boolean result = DocumentModule.isValidName(name);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("isValidName - Debe retornar true para nombre con espacios extra")
    void testIsValidNameReturnsTrueWithExtraSpaces() {
        // Arrange
        String name = "  Cartera  ";

        // Act
        boolean result = DocumentModule.isValidName(name);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("isValidName - Debe retornar false cuando nombre es null")
    void testIsValidNameReturnsFalseWhenNull() {
        // Arrange
        String name = null;

        // Act
        boolean result = DocumentModule.isValidName(name);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("isValidName - Debe retornar false cuando nombre está vacío")
    void testIsValidNameReturnsFalseWhenEmpty() {
        // Arrange
        String name = "";

        // Act
        boolean result = DocumentModule.isValidName(name);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("isValidName - Debe retornar false cuando nombre tiene solo espacios")
    void testIsValidNameReturnsFalseWhenOnlySpaces() {
        // Arrange
        String name = "   ";

        // Act
        boolean result = DocumentModule.isValidName(name);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("isValidName - Debe retornar false para nombre inválido")
    void testIsValidNameReturnsFalseForInvalidName() {
        // Arrange
        String name = "Módulo inexistente";

        // Act
        boolean result = DocumentModule.isValidName(name);

        // Assert
        assertFalse(result);
    }

    // ========== IS VALID ID TESTS ==========

    @Test
    @DisplayName("isValidId - Debe retornar true para ID válido 1")
    void testIsValidIdReturnsTrueForId1() {
        // Arrange
        Integer id = 1;

        // Act
        boolean result = DocumentModule.isValidId(id);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("isValidId - Debe retornar true para ID válido 9")
    void testIsValidIdReturnsTrueForId9() {
        // Arrange
        Integer id = 9;

        // Act
        boolean result = DocumentModule.isValidId(id);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("isValidId - Debe retornar true para todos los IDs del enum")
    void testIsValidIdReturnsTrueForAllEnumIds() {
        // Arrange & Act & Assert
        for (DocumentModule module : DocumentModule.values()) {
            assertTrue(DocumentModule.isValidId(module.getId()));
        }
    }

    @Test
    @DisplayName("isValidId - Debe retornar false cuando ID es null")
    void testIsValidIdReturnsFalseWhenNull() {
        // Arrange
        Integer id = null;

        // Act
        boolean result = DocumentModule.isValidId(id);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("isValidId - Debe retornar false para ID cero")
    void testIsValidIdReturnsFalseForZero() {
        // Arrange
        Integer id = 0;

        // Act
        boolean result = DocumentModule.isValidId(id);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("isValidId - Debe retornar false para ID negativo")
    void testIsValidIdReturnsFalseForNegative() {
        // Arrange
        Integer id = -1;

        // Act
        boolean result = DocumentModule.isValidId(id);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("isValidId - Debe retornar false para ID inexistente")
    void testIsValidIdReturnsFalseForInvalidId() {
        // Arrange
        Integer id = 999;

        // Act
        boolean result = DocumentModule.isValidId(id);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("isValidId - Debe retornar false para ID 10 (fuera de rango)")
    void testIsValidIdReturnsFalseForId10() {
        // Arrange
        Integer id = 10;

        // Act
        boolean result = DocumentModule.isValidId(id);

        // Assert
        assertFalse(result);
    }

    // ========== ENUM VALUES TESTS ==========

    @Test
    @DisplayName("values - Debe contener 9 módulos")
    void testValuesContainsNineModules() {
        // Arrange & Act
        DocumentModule[] modules = DocumentModule.values();

        // Assert
        assertEquals(9, modules.length);
    }

    @Test
    @DisplayName("getId - Cada módulo debe tener un ID único")
    void testEachModuleHasUniqueId() {
        // Arrange
        DocumentModule[] modules = DocumentModule.values();

        // Act & Assert
        for (int i = 0; i < modules.length; i++) {
            for (int j = i + 1; j < modules.length; j++) {
                assertNotEquals(modules[i].getId(), modules[j].getId(),
                        "Los módulos " + modules[i].name() + " y " + modules[j].name() + " tienen el mismo ID");
            }
        }
    }

    @Test
    @DisplayName("getName - Cada módulo debe tener un nombre no vacío")
    void testEachModuleHasNonEmptyName() {
        // Arrange & Act & Assert
        for (DocumentModule module : DocumentModule.values()) {
            assertNotNull(module.getName());
            assertFalse(module.getName().isEmpty());
        }
    }
}

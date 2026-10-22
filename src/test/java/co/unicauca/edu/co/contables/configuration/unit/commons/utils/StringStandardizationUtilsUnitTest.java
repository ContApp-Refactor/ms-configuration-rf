package co.unicauca.edu.co.contables.configuration.unit.commons.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.unicauca.edu.co.contables.commons.utils.StringStandardizationUtils;

import static org.junit.jupiter.api.Assertions.*;

class StringStandardizationUtilsUnitTest {

    // ==================== Tests de constructor privado ====================

    @Test
    @DisplayName("Debe lanzar excepción al intentar instanciar la clase utilitaria")
    void testConstructor_ThrowsUnsupportedOperationException() {
        // Act & Assert
        var exception = assertThrows(Exception.class, () -> {
            var constructor = StringStandardizationUtils.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        });

        assertTrue(exception.getCause() instanceof UnsupportedOperationException);
        assertEquals("Esta es una clase utilitaria y no debe ser instanciada", exception.getCause().getMessage());
    }

    // ==================== Tests de standardizeName ====================

    @Test
    @DisplayName("Debe estandarizar nombre con primera letra mayúscula")
    void testStandardizeName_WithLowercaseInput_ReturnsCapitalized() {
        // Arrange
        String input = "factura";

        // Act
        String result = StringStandardizationUtils.standardizeName(input);

        // Assert
        assertEquals("Factura", result);
    }

    @Test
    @DisplayName("Debe estandarizar nombre con mayúsculas a primera letra mayúscula")
    void testStandardizeName_WithUppercaseInput_ReturnsCapitalized() {
        // Arrange
        String input = "FACTURA";

        // Act
        String result = StringStandardizationUtils.standardizeName(input);

        // Assert
        assertEquals("Factura", result);
    }

    @Test
    @DisplayName("Debe estandarizar nombre con mezcla de mayúsculas y minúsculas")
    void testStandardizeName_WithMixedCase_ReturnsCapitalized() {
        // Arrange
        String input = "FaCTurA";

        // Act
        String result = StringStandardizationUtils.standardizeName(input);

        // Assert
        assertEquals("Factura", result);
    }

    @Test
    @DisplayName("Debe normalizar múltiples espacios en el nombre")
    void testStandardizeName_WithMultipleSpaces_NormalizesSpaces() {
        // Arrange
        String input = "centro   de    costo";

        // Act
        String result = StringStandardizationUtils.standardizeName(input);

        // Assert
        assertEquals("Centro de costo", result);
    }

    @Test
    @DisplayName("Debe eliminar espacios al inicio y final del nombre")
    void testStandardizeName_WithLeadingAndTrailingSpaces_TrimsSpaces() {
        // Arrange
        String input = "   factura de venta   ";

        // Act
        String result = StringStandardizationUtils.standardizeName(input);

        // Assert
        assertEquals("Factura de venta", result);
    }

    @Test
    @DisplayName("Debe retornar null cuando input es null")
    void testStandardizeName_WithNullInput_ReturnsNull() {
        // Act
        String result = StringStandardizationUtils.standardizeName(null);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Debe retornar string vacío cuando input es vacío")
    void testStandardizeName_WithEmptyString_ReturnsEmptyString() {
        // Act
        String result = StringStandardizationUtils.standardizeName("");

        // Assert
        assertEquals("", result);
    }

    @Test
    @DisplayName("Debe retornar string vacío cuando input contiene solo espacios")
    void testStandardizeName_WithOnlySpaces_ReturnsEmptyString() {
        // Act
        String result = StringStandardizationUtils.standardizeName("   ");

        // Assert
        assertEquals("", result);
    }

    @Test
    @DisplayName("Debe estandarizar nombre con un solo carácter")
    void testStandardizeName_WithSingleCharacter_ReturnsCapitalized() {
        // Arrange
        String input = "a";

        // Act
        String result = StringStandardizationUtils.standardizeName(input);

        // Assert
        assertEquals("A", result);
    }

    @Test
    @DisplayName("Debe manejar caracteres con tilde correctamente")
    void testStandardizeName_WithAccentedCharacters_HandlesCorrectly() {
        // Arrange
        String input = "FACTURACIÓN";

        // Act
        String result = StringStandardizationUtils.standardizeName(input);

        // Assert
        assertEquals("Facturación", result);
    }

    @Test
    @DisplayName("Debe manejar la letra ñ correctamente")
    void testStandardizeName_WithSpanishCharacters_HandlesCorrectly() {
        // Arrange
        String input = "AÑO FISCAL";

        // Act
        String result = StringStandardizationUtils.standardizeName(input);

        // Assert
        assertEquals("Año fiscal", result);
    }

    @Test
    @DisplayName("Debe estandarizar nombre con números")
    void testStandardizeName_WithNumbers_PreservesNumbers() {
        // Arrange
        String input = "factura 2024";

        // Act
        String result = StringStandardizationUtils.standardizeName(input);

        // Assert
        assertEquals("Factura 2024", result);
    }

    @Test
    @DisplayName("Debe estandarizar nombre con caracteres especiales")
    void testStandardizeName_WithSpecialCharacters_PreservesSpecialChars() {
        // Arrange
        String input = "tipo-documento";

        // Act
        String result = StringStandardizationUtils.standardizeName(input);

        // Assert
        assertEquals("Tipo-documento", result);
    }

    // ==================== Tests de standardizePrefix ====================

    @Test
    @DisplayName("Debe convertir prefijo a mayúsculas")
    void testStandardizePrefix_WithLowercaseInput_ReturnsUppercase() {
        // Arrange
        String input = "fv";

        // Act
        String result = StringStandardizationUtils.standardizePrefix(input);

        // Assert
        assertEquals("FV", result);
    }

    @Test
    @DisplayName("Debe mantener prefijo en mayúsculas")
    void testStandardizePrefix_WithUppercaseInput_ReturnsUppercase() {
        // Arrange
        String input = "FV";

        // Act
        String result = StringStandardizationUtils.standardizePrefix(input);

        // Assert
        assertEquals("FV", result);
    }

    @Test
    @DisplayName("Debe convertir prefijo con mezcla de casos a mayúsculas")
    void testStandardizePrefix_WithMixedCase_ReturnsUppercase() {
        // Arrange
        String input = "Fv";

        // Act
        String result = StringStandardizationUtils.standardizePrefix(input);

        // Assert
        assertEquals("FV", result);
    }

    @Test
    @DisplayName("Debe eliminar espacios del prefijo")
    void testStandardizePrefix_WithSpaces_TrimsSpaces() {
        // Arrange
        String input = "  FV  ";

        // Act
        String result = StringStandardizationUtils.standardizePrefix(input);

        // Assert
        assertEquals("FV", result);
    }

    @Test
    @DisplayName("Debe retornar null cuando prefijo es null")
    void testStandardizePrefix_WithNullInput_ReturnsNull() {
        // Act
        String result = StringStandardizationUtils.standardizePrefix(null);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Debe retornar string vacío cuando prefijo es vacío")
    void testStandardizePrefix_WithEmptyString_ReturnsEmptyString() {
        // Act
        String result = StringStandardizationUtils.standardizePrefix("");

        // Assert
        assertEquals("", result);
    }

    @Test
    @DisplayName("Debe manejar prefijo con números")
    void testStandardizePrefix_WithNumbers_PreservesNumbers() {
        // Arrange
        String input = "fv01";

        // Act
        String result = StringStandardizationUtils.standardizePrefix(input);

        // Assert
        assertEquals("FV01", result);
    }

    @Test
    @DisplayName("Debe manejar prefijo con guiones")
    void testStandardizePrefix_WithHyphens_PreservesHyphens() {
        // Arrange
        String input = "fv-001";

        // Act
        String result = StringStandardizationUtils.standardizePrefix(input);

        // Assert
        assertEquals("FV-001", result);
    }

    @Test
    @DisplayName("Debe convertir caracteres acentuados a mayúsculas")
    void testStandardizePrefix_WithAccentedCharacters_ConvertsToUppercase() {
        // Arrange
        String input = "código";

        // Act
        String result = StringStandardizationUtils.standardizePrefix(input);

        // Assert
        assertEquals("CÓDIGO", result);
    }

    // ==================== Tests de standardizeCode ====================

    @Test
    @DisplayName("Debe convertir código a mayúsculas")
    void testStandardizeCode_WithLowercaseInput_ReturnsUppercase() {
        // Arrange
        String input = "acc001";

        // Act
        String result = StringStandardizationUtils.standardizeCode(input);

        // Assert
        assertEquals("ACC001", result);
    }

    @Test
    @DisplayName("Debe mantener código en mayúsculas")
    void testStandardizeCode_WithUppercaseInput_ReturnsUppercase() {
        // Arrange
        String input = "ACC001";

        // Act
        String result = StringStandardizationUtils.standardizeCode(input);

        // Assert
        assertEquals("ACC001", result);
    }

    @Test
    @DisplayName("Debe eliminar espacios del código")
    void testStandardizeCode_WithSpaces_TrimsSpaces() {
        // Arrange
        String input = "  ACC001  ";

        // Act
        String result = StringStandardizationUtils.standardizeCode(input);

        // Assert
        assertEquals("ACC001", result);
    }

    @Test
    @DisplayName("Debe retornar null cuando código es null")
    void testStandardizeCode_WithNullInput_ReturnsNull() {
        // Act
        String result = StringStandardizationUtils.standardizeCode(null);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Debe retornar string vacío cuando código es vacío")
    void testStandardizeCode_WithEmptyString_ReturnsEmptyString() {
        // Act
        String result = StringStandardizationUtils.standardizeCode("");

        // Assert
        assertEquals("", result);
    }

    @Test
    @DisplayName("Debe manejar código con caracteres especiales")
    void testStandardizeCode_WithSpecialCharacters_PreservesSpecialChars() {
        // Arrange
        String input = "acc-001.5";

        // Act
        String result = StringStandardizationUtils.standardizeCode(input);

        // Assert
        assertEquals("ACC-001.5", result);
    }

    @Test
    @DisplayName("Debe comportarse igual que standardizePrefix")
    void testStandardizeCode_BehavesLikeStandardizePrefix() {
        // Arrange
        String input = "test code";

        // Act
        String resultCode = StringStandardizationUtils.standardizeCode(input);
        String resultPrefix = StringStandardizationUtils.standardizePrefix(input);

        // Assert
        assertEquals(resultPrefix, resultCode);
    }

    // ==================== Tests de casos edge ====================

    @Test
    @DisplayName("Debe manejar string con solo un espacio")
    void testStandardizeName_WithSingleSpace_ReturnsEmptyString() {
        // Act
        String result = StringStandardizationUtils.standardizeName(" ");

        // Assert
        assertEquals("", result);
    }

    @Test
    @DisplayName("Debe manejar prefijo con solo espacios")
    void testStandardizePrefix_WithOnlySpaces_ReturnsEmptyString() {
        // Act
        String result = StringStandardizationUtils.standardizePrefix("   ");

        // Assert
        assertEquals("", result);
    }

    @Test
    @DisplayName("Debe estandarizar nombre muy largo")
    void testStandardizeName_WithLongString_HandlesCorrectly() {
        // Arrange
        String input = "ESTE ES UN NOMBRE MUY LARGO PARA PROBAR EL MANEJO DE STRINGS EXTENSOS";

        // Act
        String result = StringStandardizationUtils.standardizeName(input);

        // Assert
        assertEquals("Este es un nombre muy largo para probar el manejo de strings extensos", result);
    }

    @Test
    @DisplayName("Debe estandarizar nombre con tabs y saltos de línea")
    void testStandardizeName_WithTabsAndNewlines_NormalizesWhitespace() {
        // Arrange
        String input = "centro\tde\ncosto";

        // Act
        String result = StringStandardizationUtils.standardizeName(input);

        // Assert
        assertEquals("Centro de costo", result);
    }
}

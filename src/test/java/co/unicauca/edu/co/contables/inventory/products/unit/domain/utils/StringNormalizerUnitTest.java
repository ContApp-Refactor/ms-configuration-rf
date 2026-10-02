package co.unicauca.edu.co.contables.inventory.products.unit.domain.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.unicauca.edu.co.contables.inventory.products.domain.utils.StringNormalizer;

/**
 * Tests unitarios para StringNormalizer (utilidad de normalización de cadenas)
 */
class StringNormalizerUnitTest {

    // ==================== Tests para normalize() ====================

    @Test
    @DisplayName("Debe normalizar nombre eliminando acentos y capitalizando primera letra")
    void testNormalize_RemovesAccentsAndCapitalizesFirstLetter() {
        // Arrange
        String input = "categoría";

        // Act
        String result = StringNormalizer.normalize(input);

        // Assert
        assertEquals("Categoria", result);
    }

    @Test
    @DisplayName("Debe normalizar nombre con múltiples acentos")
    void testNormalize_WithMultipleAccents() {
        // Arrange
        String input = "descripción ñoño maría josé";

        // Act
        String result = StringNormalizer.normalize(input);

        // Assert
        assertEquals("Descripcion nono maria jose", result);
    }

    @Test
    @DisplayName("Debe eliminar espacios al inicio y final pero mantener internos")
    void testNormalize_TrimsSpaces() {
        // Arrange
        String input = "   producto    con   espacios   ";

        // Act
        String result = StringNormalizer.normalize(input);

        // Assert
        assertEquals("Producto    con   espacios", result);
    }

    @Test
    @DisplayName("Debe convertir a minúsculas excepto primera letra")
    void testNormalize_ConvertsToLowerCaseExceptFirstLetter() {
        // Arrange
        String input = "PRODUCTO EN MAYÚSCULAS";

        // Act
        String result = StringNormalizer.normalize(input);

        // Assert
        assertEquals("Producto en mayusculas", result);
    }

    @Test
    @DisplayName("Debe manejar texto con una sola letra")
    void testNormalize_WithSingleCharacter() {
        // Arrange
        String input = "a";

        // Act
        String result = StringNormalizer.normalize(input);

        // Assert
        assertEquals("A", result);
    }

    @Test
    @DisplayName("Debe manejar texto con una sola letra mayúscula")
    void testNormalize_WithSingleUpperCaseCharacter() {
        // Arrange
        String input = "A";

        // Act
        String result = StringNormalizer.normalize(input);

        // Assert
        assertEquals("A", result);
    }

    @Test
    @DisplayName("Debe retornar null cuando input es null")
    void testNormalize_WithNullInput() {
        // Arrange
        String input = null;

        // Act
        String result = StringNormalizer.normalize(input);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Debe retornar string vacío cuando input es vacío")
    void testNormalize_WithEmptyInput() {
        // Arrange
        String input = "";

        // Act
        String result = StringNormalizer.normalize(input);

        // Assert
        assertEquals("", result);
    }

    @Test
    @DisplayName("Debe retornar string con espacios cuando solo hay espacios")
    void testNormalize_WithOnlySpaces() {
        // Arrange
        String input = "   ";

        // Act
        String result = StringNormalizer.normalize(input);

        // Assert
        assertEquals("   ", result);
    }

    @Test
    @DisplayName("Debe normalizar texto mixto con acentos y mayúsculas")
    void testNormalize_WithMixedCase() {
        // Arrange
        String input = "Descripción MIXTA con ACENTOS";

        // Act
        String result = StringNormalizer.normalize(input);

        // Assert
        assertEquals("Descripcion mixta con acentos", result);
    }

    // ==================== Tests para normalizeCode() ====================

    @Test
    @DisplayName("Debe normalizar código a mayúsculas sin acentos")
    void testNormalizeCode_ConvertsToUpperCaseWithoutAccents() {
        // Arrange
        String input = "código-sku-123";

        // Act
        String result = StringNormalizer.normalizeCode(input);

        // Assert
        assertEquals("CODIGO-SKU-123", result);
    }

    @Test
    @DisplayName("Debe normalizar código con múltiples acentos")
    void testNormalizeCode_WithMultipleAccents() {
        // Arrange
        String input = "referéncia-ñoño-josé";

        // Act
        String result = StringNormalizer.normalizeCode(input);

        // Assert
        assertEquals("REFERENCIA-NONO-JOSE", result);
    }

    @Test
    @DisplayName("Debe eliminar espacios al inicio y final pero mantener internos en código")
    void testNormalizeCode_TrimsSpaces() {
        // Arrange
        String input = "   REF  123   ";

        // Act
        String result = StringNormalizer.normalizeCode(input);

        // Assert
        assertEquals("REF  123", result);
    }

    @Test
    @DisplayName("Debe convertir código en minúsculas a mayúsculas")
    void testNormalizeCode_ConvertsLowerCaseToUpperCase() {
        // Arrange
        String input = "ref-producto-abc";

        // Act
        String result = StringNormalizer.normalizeCode(input);

        // Assert
        assertEquals("REF-PRODUCTO-ABC", result);
    }

    @Test
    @DisplayName("Debe retornar null cuando código es null")
    void testNormalizeCode_WithNullInput() {
        // Arrange
        String input = null;

        // Act
        String result = StringNormalizer.normalizeCode(input);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Debe retornar string vacío cuando código es vacío")
    void testNormalizeCode_WithEmptyInput() {
        // Arrange
        String input = "";

        // Act
        String result = StringNormalizer.normalizeCode(input);

        // Assert
        assertEquals("", result);
    }

    @Test
    @DisplayName("Debe mantener guiones y caracteres especiales en código")
    void testNormalizeCode_PreservesSpecialCharacters() {
        // Arrange
        String input = "ref-123_abc.xyz";

        // Act
        String result = StringNormalizer.normalizeCode(input);

        // Assert
        assertEquals("REF-123_ABC.XYZ", result);
    }

    // ==================== Tests para normalizeHeaderName() ====================

    @Test
    @DisplayName("Debe normalizar header eliminando texto entre paréntesis")
    void testNormalizeHeaderName_RemovesTextInParentheses() {
        // Arrange
        String input = "Nombre (Requerido)";

        // Act
        String result = StringNormalizer.normalizeHeaderName(input);

        // Assert
        assertEquals("Nombre", result);
    }

    @Test
    @DisplayName("Debe normalizar header eliminando texto después de salto de línea")
    void testNormalizeHeaderName_RemovesTextAfterNewline() {
        // Arrange
        String input = "Nombre\nTexto adicional";

        // Act
        String result = StringNormalizer.normalizeHeaderName(input);

        // Assert
        assertEquals("Nombre", result);
    }

    @Test
    @DisplayName("Debe reemplazar caracteres especiales por encoding issues y mapear a constantes")
    void testNormalizeHeaderName_ReplacesEncodingIssues() {
        // Arrange
        String input1 = "CategorÝa";
        String input2 = "Descripci¾n";

        // Act
        String result1 = StringNormalizer.normalizeHeaderName(input1);
        String result2 = StringNormalizer.normalizeHeaderName(input2);

        // Assert
        assertEquals("Categoría", result1);
        assertEquals("Descripción", result2);
    }

    @Test
    @DisplayName("Debe mapear header a constante con acentos")
    void testNormalizeHeaderName_MapsToConstantWithAccents() {
        // Arrange
        String input = "DESCRIPCIÓN";

        // Act
        String result = StringNormalizer.normalizeHeaderName(input);

        // Assert
        assertEquals("Descripción", result);
    }

    @Test
    @DisplayName("Debe retornar null cuando header es null")
    void testNormalizeHeaderName_WithNullInput() {
        // Arrange
        String input = null;

        // Act
        String result = StringNormalizer.normalizeHeaderName(input);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Debe mapear 'nombre' a columna canónica de nombre")
    void testNormalizeHeaderName_MapsNombreToNameColumn() {
        // Arrange
        String input = "Nombre";

        // Act
        String result = StringNormalizer.normalizeHeaderName(input);

        // Assert
        assertEquals("Nombre", result);
    }

    @Test
    @DisplayName("Debe mapear 'name' a columna canónica de nombre")
    void testNormalizeHeaderName_MapsNameToNameColumn() {
        // Arrange
        String input = "Name";

        // Act
        String result = StringNormalizer.normalizeHeaderName(input);

        // Assert
        assertEquals("Nombre", result);
    }

    @Test
    @DisplayName("Debe mapear 'descripcion' a columna canónica")
    void testNormalizeHeaderName_MapsDescripcionToDescriptionColumn() {
        // Arrange
        String input = "Descripcion";

        // Act
        String result = StringNormalizer.normalizeHeaderName(input);

        // Assert
        assertEquals("Descripción", result);
    }

    @Test
    @DisplayName("Debe mapear 'description' a columna canónica")
    void testNormalizeHeaderName_MapsDescriptionToDescriptionColumn() {
        // Arrange
        String input = "Description";

        // Act
        String result = StringNormalizer.normalizeHeaderName(input);

        // Assert
        assertEquals("Descripción", result);
    }

    @Test
    @DisplayName("Debe mapear 'unidad de medida' a columna canónica")
    void testNormalizeHeaderName_MapsUnidadDeMedidaToUnitMeasureColumn() {
        // Arrange
        String input = "Unidad de Medida";

        // Act
        String result = StringNormalizer.normalizeHeaderName(input);

        // Assert
        assertEquals("Unidad de Medida", result);
    }

    @Test
    @DisplayName("Debe mapear 'unidad' a columna canónica de unidad de medida")
    void testNormalizeHeaderName_MapsUnidadToUnitMeasureColumn() {
        // Arrange
        String input = "Unidad";

        // Act
        String result = StringNormalizer.normalizeHeaderName(input);

        // Assert
        assertEquals("Unidad de Medida", result);
    }

    @Test
    @DisplayName("Debe mapear 'uom' a columna canónica de unidad de medida")
    void testNormalizeHeaderName_MapsUomToUnitMeasureColumn() {
        // Arrange
        String input = "UOM";

        // Act
        String result = StringNormalizer.normalizeHeaderName(input);

        // Assert
        assertEquals("Unidad de Medida", result);
    }

    @Test
    @DisplayName("Debe mapear 'categoria' a columna canónica")
    void testNormalizeHeaderName_MapsCategoriaToCategory() {
        // Arrange
        String input = "Categoria";

        // Act
        String result = StringNormalizer.normalizeHeaderName(input);

        // Assert
        assertEquals("Categoría", result);
    }

    @Test
    @DisplayName("Debe mapear 'category' a columna canónica de categoría")
    void testNormalizeHeaderName_MapsCategoryToCategoryColumn() {
        // Arrange
        String input = "Category";

        // Act
        String result = StringNormalizer.normalizeHeaderName(input);

        // Assert
        assertEquals("Categoría", result);
    }

    @Test
    @DisplayName("Debe mapear 'tipo de producto' a columna canónica")
    void testNormalizeHeaderName_MapsTipoDeProductoToProductTypeColumn() {
        // Arrange
        String input = "Tipo de Producto";

        // Act
        String result = StringNormalizer.normalizeHeaderName(input);

        // Assert
        assertEquals("Tipo de Producto", result);
    }

    @Test
    @DisplayName("Debe mapear 'tipo' a columna canónica de tipo de producto")
    void testNormalizeHeaderName_MapsTipoToProductTypeColumn() {
        // Arrange
        String input = "Tipo";

        // Act
        String result = StringNormalizer.normalizeHeaderName(input);

        // Assert
        assertEquals("Tipo de Producto", result);
    }

    @Test
    @DisplayName("Debe mapear 'product type' a columna canónica")
    void testNormalizeHeaderName_MapsProductTypeToProductTypeColumn() {
        // Arrange
        String input = "Product Type";

        // Act
        String result = StringNormalizer.normalizeHeaderName(input);

        // Assert
        assertEquals("Tipo de Producto", result);
    }

    @Test
    @DisplayName("Debe mapear 'referencia/sku' a columna canónica")
    void testNormalizeHeaderName_MapsReferenciaSkuToReferenceColumn() {
        // Arrange
        String input = "Referencia/SKU";

        // Act
        String result = StringNormalizer.normalizeHeaderName(input);

        // Assert
        assertEquals("Referencia", result);
    }

    @Test
    @DisplayName("Debe mapear 'referencia' a columna canónica")
    void testNormalizeHeaderName_MapsReferenciaToReferenceColumn() {
        // Arrange
        String input = "Referencia";

        // Act
        String result = StringNormalizer.normalizeHeaderName(input);

        // Assert
        assertEquals("Referencia", result);
    }

    @Test
    @DisplayName("Debe mapear 'sku' a columna canónica de referencia")
    void testNormalizeHeaderName_MapsSkuToReferenceColumn() {
        // Arrange
        String input = "SKU";

        // Act
        String result = StringNormalizer.normalizeHeaderName(input);

        // Assert
        assertEquals("Referencia", result);
    }

    @Test
    @DisplayName("Debe mapear 'reference' a columna canónica")
    void testNormalizeHeaderName_MapsReferenceToReferenceColumn() {
        // Arrange
        String input = "Reference";

        // Act
        String result = StringNormalizer.normalizeHeaderName(input);

        // Assert
        assertEquals("Referencia", result);
    }

    @Test
    @DisplayName("Debe mapear 'presentacion' a columna canónica")
    void testNormalizeHeaderName_MapsPresentacionToPresentationColumn() {
        // Arrange
        String input = "Presentacion";

        // Act
        String result = StringNormalizer.normalizeHeaderName(input);

        // Assert
        assertEquals("Presentación", result);
    }

    @Test
    @DisplayName("Debe mapear 'presentation' a columna canónica")
    void testNormalizeHeaderName_MapsPresentationToPresentationColumn() {
        // Arrange
        String input = "Presentation";

        // Act
        String result = StringNormalizer.normalizeHeaderName(input);

        // Assert
        assertEquals("Presentación", result);
    }

    @Test
    @DisplayName("Debe mapear 'cantidad' a columna canónica")
    void testNormalizeHeaderName_MapsCantidadToQuantityColumn() {
        // Arrange
        String input = "Cantidad";

        // Act
        String result = StringNormalizer.normalizeHeaderName(input);

        // Assert
        assertEquals("Cantidad", result);
    }

    @Test
    @DisplayName("Debe mapear 'quantity' a columna canónica")
    void testNormalizeHeaderName_MapsQuantityToQuantityColumn() {
        // Arrange
        String input = "Quantity";

        // Act
        String result = StringNormalizer.normalizeHeaderName(input);

        // Assert
        assertEquals("Cantidad", result);
    }

    @Test
    @DisplayName("Debe mapear 'costo' a columna canónica")
    void testNormalizeHeaderName_MapsCostoToCostColumn() {
        // Arrange
        String input = "Costo";

        // Act
        String result = StringNormalizer.normalizeHeaderName(input);

        // Assert
        assertEquals("Costo", result);
    }

    @Test
    @DisplayName("Debe mapear 'cost' a columna canónica")
    void testNormalizeHeaderName_MapsCostToCostColumn() {
        // Arrange
        String input = "Cost";

        // Act
        String result = StringNormalizer.normalizeHeaderName(input);

        // Assert
        assertEquals("Costo", result);
    }

    @Test
    @DisplayName("Debe mapear 'price' a columna canónica de costo")
    void testNormalizeHeaderName_MapsPriceToCostColumn() {
        // Arrange
        String input = "Price";

        // Act
        String result = StringNormalizer.normalizeHeaderName(input);

        // Assert
        assertEquals("Costo", result);
    }

    @Test
    @DisplayName("Debe mapear 'codigo' a campo opcional")
    void testNormalizeHeaderName_MapsCodigoToOptionalField() {
        // Arrange
        String input = "Codigo";

        // Act
        String result = StringNormalizer.normalizeHeaderName(input);

        // Assert
        assertEquals("Código", result);
    }

    @Test
    @DisplayName("Debe mapear 'code' a campo opcional")
    void testNormalizeHeaderName_MapsCodeToOptionalField() {
        // Arrange
        String input = "Code";

        // Act
        String result = StringNormalizer.normalizeHeaderName(input);

        // Assert
        assertEquals("Código", result);
    }

    @Test
    @DisplayName("Debe mapear 'estado' a campo opcional")
    void testNormalizeHeaderName_MapsEstadoToOptionalField() {
        // Arrange
        String input = "Estado";

        // Act
        String result = StringNormalizer.normalizeHeaderName(input);

        // Assert
        assertEquals("Estado", result);
    }

    @Test
    @DisplayName("Debe mapear 'state' a campo opcional de estado")
    void testNormalizeHeaderName_MapsStateToOptionalField() {
        // Arrange
        String input = "State";

        // Act
        String result = StringNormalizer.normalizeHeaderName(input);

        // Assert
        assertEquals("Estado", result);
    }

    @Test
    @DisplayName("Debe mapear 'status' a campo opcional de estado")
    void testNormalizeHeaderName_MapsStatusToOptionalField() {
        // Arrange
        String input = "Status";

        // Act
        String result = StringNormalizer.normalizeHeaderName(input);

        // Assert
        assertEquals("Estado", result);
    }

    @Test
    @DisplayName("Debe retornar header sin cambios cuando no hay regla de mapeo")
    void testNormalizeHeaderName_ReturnsUnmappedHeaderAsIs() {
        // Arrange
        String input = "Campo Desconocido";

        // Act
        String result = StringNormalizer.normalizeHeaderName(input);

        // Assert
        assertEquals("campo desconocido", result);
    }

    @Test
    @DisplayName("Debe procesar header complejo con paréntesis, acentos y saltos de línea")
    void testNormalizeHeaderName_WithComplexInput() {
        // Arrange
        String input = "Descripción (Requerido)\nTexto adicional";

        // Act
        String result = StringNormalizer.normalizeHeaderName(input);

        // Assert
        assertEquals("Descripción", result);
    }

    @Test
    @DisplayName("Debe manejar múltiples paréntesis en header")
    void testNormalizeHeaderName_WithMultipleParentheses() {
        // Arrange
        String input = "Campo (Opcional) (Secundario)";

        // Act
        String result = StringNormalizer.normalizeHeaderName(input);

        // Assert
        assertEquals("campo", result);
    }

    @Test
    @DisplayName("Debe procesar header con todos los caracteres especiales de encoding")
    void testNormalizeHeaderName_WithAllEncodingIssues() {
        // Arrange
        String input1 = "CategorÝa";
        String input2 = "Descripci¾n";
        String input3 = "CategorÃa";
        String input4 = "Descripci³n";

        // Act
        String result1 = StringNormalizer.normalizeHeaderName(input1);
        String result2 = StringNormalizer.normalizeHeaderName(input2);
        String result3 = StringNormalizer.normalizeHeaderName(input3);
        String result4 = StringNormalizer.normalizeHeaderName(input4);

        // Assert
        assertEquals("Categoría", result1);
        assertEquals("Descripción", result2);
        assertEquals("Categoría", result3);
        assertEquals("Descripción", result4);
    }
}

package co.unicauca.edu.co.contables.configuration.unit.commons.utils;

import static org.junit.jupiter.api.Assertions.*;

import co.unicauca.edu.co.contables.commons.utils.PaginationHelper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class PaginationHelperUnitTest {

    @InjectMocks
    private PaginationHelper paginationHelper;

    @Test
    @DisplayName("Debe crear Pageable con parámetros especificados")
    void testCreateFlexiblePageableWithParameters() {
        // Arrange
        Optional<Integer> numPage = Optional.of(2);
        Optional<Integer> size = Optional.of(10);
        long totalRecords = 100L;

        // Act
        Pageable result = paginationHelper.createFlexiblePageable(numPage, size, totalRecords);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getPageNumber());
        assertEquals(10, result.getPageSize());
    }

    @Test
    @DisplayName("Debe crear Pageable con todos los registros cuando numPage está vacío")
    void testCreateFlexiblePageableWithEmptyNumPage() {
        // Arrange
        Optional<Integer> numPage = Optional.empty();
        Optional<Integer> size = Optional.of(10);
        long totalRecords = 50L;

        // Act
        Pageable result = paginationHelper.createFlexiblePageable(numPage, size, totalRecords);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getPageNumber());
        assertEquals(50, result.getPageSize());
    }

    @Test
    @DisplayName("Debe crear Pageable con todos los registros cuando size está vacío")
    void testCreateFlexiblePageableWithEmptySize() {
        // Arrange
        Optional<Integer> numPage = Optional.of(1);
        Optional<Integer> size = Optional.empty();
        long totalRecords = 75L;

        // Act
        Pageable result = paginationHelper.createFlexiblePageable(numPage, size, totalRecords);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getPageNumber());
        assertEquals(75, result.getPageSize());
    }

    @Test
    @DisplayName("Debe crear Pageable con todos los registros cuando ambos parámetros están vacíos")
    void testCreateFlexiblePageableWithBothEmpty() {
        // Arrange
        Optional<Integer> numPage = Optional.empty();
        Optional<Integer> size = Optional.empty();
        long totalRecords = 200L;

        // Act
        Pageable result = paginationHelper.createFlexiblePageable(numPage, size, totalRecords);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getPageNumber());
        assertEquals(200, result.getPageSize());
    }

    @Test
    @DisplayName("Debe crear Pageable con tamaño 1 cuando totalRecords es 0")
    void testCreateFlexiblePageableWithZeroRecords() {
        // Arrange
        Optional<Integer> numPage = Optional.empty();
        Optional<Integer> size = Optional.empty();
        long totalRecords = 0L;

        // Act
        Pageable result = paginationHelper.createFlexiblePageable(numPage, size, totalRecords);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getPageNumber());
        assertEquals(1, result.getPageSize());
    }

    @Test
    @DisplayName("Debe crear Pageable con tamaño 1 cuando totalRecords es negativo")
    void testCreateFlexiblePageableWithNegativeRecords() {
        // Arrange
        Optional<Integer> numPage = Optional.empty();
        Optional<Integer> size = Optional.empty();
        long totalRecords = -5L;

        // Act
        Pageable result = paginationHelper.createFlexiblePageable(numPage, size, totalRecords);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getPageNumber());
        assertEquals(1, result.getPageSize());
    }

    @Test
    @DisplayName("Debe manejar conversión segura cuando totalRecords excede Integer.MAX_VALUE")
    void testCreateFlexiblePageableWithLargeRecordCount() {
        // Arrange
        Optional<Integer> numPage = Optional.empty();
        Optional<Integer> size = Optional.empty();
        long totalRecords = (long) Integer.MAX_VALUE + 1000L;

        // Act
        Pageable result = paginationHelper.createFlexiblePageable(numPage, size, totalRecords);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getPageNumber());
        assertEquals(Integer.MAX_VALUE, result.getPageSize());
    }

    @Test
    @DisplayName("Debe crear Pageable correctamente cuando totalRecords es Integer.MAX_VALUE")
    void testCreateFlexiblePageableWithMaxIntRecords() {
        // Arrange
        Optional<Integer> numPage = Optional.empty();
        Optional<Integer> size = Optional.empty();
        long totalRecords = Integer.MAX_VALUE;

        // Act
        Pageable result = paginationHelper.createFlexiblePageable(numPage, size, totalRecords);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getPageNumber());
        assertEquals(Integer.MAX_VALUE, result.getPageSize());
    }

    @Test
    @DisplayName("Debe crear Pageable con página 0 y tamaño especificado cuando solo numPage es 0")
    void testCreateFlexiblePageableWithPageZero() {
        // Arrange
        Optional<Integer> numPage = Optional.of(0);
        Optional<Integer> size = Optional.of(20);
        long totalRecords = 100L;

        // Act
        Pageable result = paginationHelper.createFlexiblePageable(numPage, size, totalRecords);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getPageNumber());
        assertEquals(20, result.getPageSize());
    }

    @Test
    @DisplayName("Debe crear Pageable con tamaño 1 cuando size es 1")
    void testCreateFlexiblePageableWithSizeOne() {
        // Arrange
        Optional<Integer> numPage = Optional.of(0);
        Optional<Integer> size = Optional.of(1);
        long totalRecords = 100L;

        // Act
        Pageable result = paginationHelper.createFlexiblePageable(numPage, size, totalRecords);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getPageNumber());
        assertEquals(1, result.getPageSize());
    }

    @Test
    @DisplayName("Debe crear Pageable con número de página grande")
    void testCreateFlexiblePageableWithLargePageNumber() {
        // Arrange
        Optional<Integer> numPage = Optional.of(999);
        Optional<Integer> size = Optional.of(50);
        long totalRecords = 100000L;

        // Act
        Pageable result = paginationHelper.createFlexiblePageable(numPage, size, totalRecords);

        // Assert
        assertNotNull(result);
        assertEquals(999, result.getPageNumber());
        assertEquals(50, result.getPageSize());
    }

    @Test
    @DisplayName("Debe crear Pageable con tamaño de página grande")
    void testCreateFlexiblePageableWithLargePageSize() {
        // Arrange
        Optional<Integer> numPage = Optional.of(0);
        Optional<Integer> size = Optional.of(1000);
        long totalRecords = 5000L;

        // Act
        Pageable result = paginationHelper.createFlexiblePageable(numPage, size, totalRecords);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getPageNumber());
        assertEquals(1000, result.getPageSize());
    }

    @Test
    @DisplayName("Debe crear Pageable con totalRecords igual a 1")
    void testCreateFlexiblePageableWithOneRecord() {
        // Arrange
        Optional<Integer> numPage = Optional.empty();
        Optional<Integer> size = Optional.empty();
        long totalRecords = 1L;

        // Act
        Pageable result = paginationHelper.createFlexiblePageable(numPage, size, totalRecords);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getPageNumber());
        assertEquals(1, result.getPageSize());
    }

    @Test
    @DisplayName("Debe crear Pageable con valores típicos de paginación")
    void testCreateFlexiblePageableWithTypicalValues() {
        // Arrange
        Optional<Integer> numPage = Optional.of(3);
        Optional<Integer> size = Optional.of(25);
        long totalRecords = 500L;

        // Act
        Pageable result = paginationHelper.createFlexiblePageable(numPage, size, totalRecords);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getPageNumber());
        assertEquals(25, result.getPageSize());
        assertEquals(75, result.getOffset());
    }

    @Test
    @DisplayName("Debe verificar que Pageable sin parámetros tiene offset 0")
    void testCreateFlexiblePageableWithoutParametersHasZeroOffset() {
        // Arrange
        Optional<Integer> numPage = Optional.empty();
        Optional<Integer> size = Optional.empty();
        long totalRecords = 100L;

        // Act
        Pageable result = paginationHelper.createFlexiblePageable(numPage, size, totalRecords);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getOffset());
    }

    @Test
    @DisplayName("Debe crear Pageable con totalRecords muy pequeño")
    void testCreateFlexiblePageableWithVerySmallRecordCount() {
        // Arrange
        Optional<Integer> numPage = Optional.empty();
        Optional<Integer> size = Optional.empty();
        long totalRecords = 3L;

        // Act
        Pageable result = paginationHelper.createFlexiblePageable(numPage, size, totalRecords);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getPageNumber());
        assertEquals(3, result.getPageSize());
    }

    @Test
    @DisplayName("Debe crear Pageable correctamente con totalRecords medio")
    void testCreateFlexiblePageableWithMediumRecordCount() {
        // Arrange
        Optional<Integer> numPage = Optional.empty();
        Optional<Integer> size = Optional.empty();
        long totalRecords = 5000L;

        // Act
        Pageable result = paginationHelper.createFlexiblePageable(numPage, size, totalRecords);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getPageNumber());
        assertEquals(5000, result.getPageSize());
    }
}

package co.unicauca.edu.co.contables.configuration.unit.costCenters.presentation.DTO;

import co.unicauca.edu.co.contables.configuration.costCenters.presentation.DTO.request.CostCenterCreateReq;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CostCenterCreateReqUnitTest {

    private static final String ID_ENTERPRISE = "ENT001";
    private static final String NAME = "Centro de costo principal";
    private static final Long PARENT_ID = 10L;

    // ========== IS CODE LENGTH VALID TESTS ==========

    @Test
    @DisplayName("isCodeLengthValid - Debe retornar true para código de longitud 2")
    void testIsCodeLengthValidReturnsTrueForLength2() {
        // Arrange
        CostCenterCreateReq request = CostCenterCreateReq.builder()
                .idEnterprise(ID_ENTERPRISE)
                .code("AB")
                .name(NAME)
                .build();

        // Act
        boolean result = request.isCodeLengthValid();

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("isCodeLengthValid - Debe retornar true para código de longitud 4")
    void testIsCodeLengthValidReturnsTrueForLength4() {
        // Arrange
        CostCenterCreateReq request = CostCenterCreateReq.builder()
                .idEnterprise(ID_ENTERPRISE)
                .code("ABCD")
                .name(NAME)
                .build();

        // Act
        boolean result = request.isCodeLengthValid();

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("isCodeLengthValid - Debe retornar true para código de longitud mayor a 4")
    void testIsCodeLengthValidReturnsTrueForLengthGreaterThan4() {
        // Arrange
        CostCenterCreateReq request = CostCenterCreateReq.builder()
                .idEnterprise(ID_ENTERPRISE)
                .code("ABCDEFGH")
                .name(NAME)
                .build();

        // Act
        boolean result = request.isCodeLengthValid();

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("isCodeLengthValid - Debe retornar false para código de longitud 1")
    void testIsCodeLengthValidReturnsFalseForLength1() {
        // Arrange
        CostCenterCreateReq request = CostCenterCreateReq.builder()
                .idEnterprise(ID_ENTERPRISE)
                .code("A")
                .name(NAME)
                .build();

        // Act
        boolean result = request.isCodeLengthValid();

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("isCodeLengthValid - Debe retornar false para código de longitud 3")
    void testIsCodeLengthValidReturnsFalseForLength3() {
        // Arrange
        CostCenterCreateReq request = CostCenterCreateReq.builder()
                .idEnterprise(ID_ENTERPRISE)
                .code("ABC")
                .name(NAME)
                .build();

        // Act
        boolean result = request.isCodeLengthValid();

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("isCodeLengthValid - Debe retornar false cuando código es null")
    void testIsCodeLengthValidReturnsFalseWhenCodeNull() {
        // Arrange
        CostCenterCreateReq request = CostCenterCreateReq.builder()
                .idEnterprise(ID_ENTERPRISE)
                .code(null)
                .name(NAME)
                .build();

        // Act
        boolean result = request.isCodeLengthValid();

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("isCodeLengthValid - Debe retornar false para código vacío")
    void testIsCodeLengthValidReturnsFalseForEmptyCode() {
        // Arrange
        CostCenterCreateReq request = CostCenterCreateReq.builder()
                .idEnterprise(ID_ENTERPRISE)
                .code("")
                .name(NAME)
                .build();

        // Act
        boolean result = request.isCodeLengthValid();

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("isCodeLengthValid - Debe considerar espacios al inicio y final (trim)")
    void testIsCodeLengthValidTrimsSpaces() {
        // Arrange
        CostCenterCreateReq request = CostCenterCreateReq.builder()
                .idEnterprise(ID_ENTERPRISE)
                .code("  AB  ")
                .name(NAME)
                .build();

        // Act
        boolean result = request.isCodeLengthValid();

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("isCodeLengthValid - Debe retornar false para código con solo espacios de longitud 3")
    void testIsCodeLengthValidWithSpacesAndLength3() {
        // Arrange
        CostCenterCreateReq request = CostCenterCreateReq.builder()
                .idEnterprise(ID_ENTERPRISE)
                .code("  ABC  ")
                .name(NAME)
                .build();

        // Act
        boolean result = request.isCodeLengthValid();

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("isCodeLengthValid - Debe retornar true para código con espacios y longitud 4")
    void testIsCodeLengthValidWithSpacesAndLength4() {
        // Arrange
        CostCenterCreateReq request = CostCenterCreateReq.builder()
                .idEnterprise(ID_ENTERPRISE)
                .code("  ABCD  ")
                .name(NAME)
                .build();

        // Act
        boolean result = request.isCodeLengthValid();

        // Assert
        assertTrue(result);
    }

    // ========== BUILDER AND FIELDS TESTS ==========

    @Test
    @DisplayName("Builder - Debe crear request con todos los campos")
    void testBuilderCreatesRequestWithAllFields() {
        // Arrange & Act
        CostCenterCreateReq request = CostCenterCreateReq.builder()
                .idEnterprise(ID_ENTERPRISE)
                .code("CC01")
                .name(NAME)
                .parentId(PARENT_ID)
                .build();

        // Assert
        assertNotNull(request);
        assertEquals(ID_ENTERPRISE, request.getIdEnterprise());
        assertEquals("CC01", request.getCode());
        assertEquals(NAME, request.getName());
        assertEquals(PARENT_ID, request.getParentId());
    }

    @Test
    @DisplayName("Builder - Debe crear request sin parentId (opcional)")
    void testBuilderCreatesRequestWithoutParentId() {
        // Arrange & Act
        CostCenterCreateReq request = CostCenterCreateReq.builder()
                .idEnterprise(ID_ENTERPRISE)
                .code("CC01")
                .name(NAME)
                .build();

        // Assert
        assertNotNull(request);
        assertNull(request.getParentId());
    }
}

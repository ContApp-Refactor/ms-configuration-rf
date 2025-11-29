package co.unicauca.edu.co.contables.configuration.unit.costCenters.domain.mapper;

import co.unicauca.edu.co.contables.configuration.costCenters.domain.mapper.CostCenterDomainMapper;
import co.unicauca.edu.co.contables.configuration.costCenters.domain.models.CostCenter;
import co.unicauca.edu.co.contables.configuration.costCenters.presentation.DTO.request.CostCenterCreateReq;
import co.unicauca.edu.co.contables.configuration.costCenters.presentation.DTO.request.CostCenterUpdateReq;
import co.unicauca.edu.co.contables.configuration.costCenters.presentation.DTO.response.CostCenterRes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CostCenterDomainMapperUnitTest {

    private CostCenterDomainMapper mapper;

    private static final Long ID = 1L;
    private static final Long PARENT_ID = 10L;
    private static final String CODE = "CC01";
    private static final String NAME = "Centro de costo principal";
    private static final String ID_ENTERPRISE = "ENT001";
    private static final Boolean STATUS = true;
    private static final Integer USAGE_COUNT = 5;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(CostCenterDomainMapper.class);
    }

    // ========== TO DOMAIN (CREATE REQ) TESTS ==========

    @Test
    @DisplayName("toDomain(CreateReq) - Debe mapear campos básicos correctamente")
    void testToDomainFromCreateReqMapsBasicFields() {
        // Arrange
        CostCenterCreateReq request = CostCenterCreateReq.builder()
                .idEnterprise(ID_ENTERPRISE)
                .code(CODE)
                .name(NAME)
                .build();

        // Act
        CostCenter domain = mapper.toDomain(request);

        // Assert
        assertNotNull(domain);
        assertEquals(ID_ENTERPRISE, domain.getIdEnterprise());
        assertEquals(CODE, domain.getCode());
        assertEquals(NAME, domain.getName());
    }

    @Test
    @DisplayName("toDomain(CreateReq) - Debe ignorar ID (se genera automáticamente)")
    void testToDomainFromCreateReqIgnoresId() {
        // Arrange
        CostCenterCreateReq request = CostCenterCreateReq.builder()
                .idEnterprise(ID_ENTERPRISE)
                .code(CODE)
                .name(NAME)
                .build();

        // Act
        CostCenter domain = mapper.toDomain(request);

        // Assert
        assertNotNull(domain);
        assertNull(domain.getId());
    }

    @Test
    @DisplayName("toDomain(CreateReq) - Debe mapear parent con solo ID cuando parentId existe")
    void testToDomainFromCreateReqMapsParentWithId() {
        // Arrange
        CostCenterCreateReq request = CostCenterCreateReq.builder()
                .idEnterprise(ID_ENTERPRISE)
                .code(CODE)
                .name(NAME)
                .parentId(PARENT_ID)
                .build();

        // Act
        CostCenter domain = mapper.toDomain(request);

        // Assert
        assertNotNull(domain);
        assertNotNull(domain.getParent());
        assertEquals(PARENT_ID, domain.getParent().getId());
    }

    @Test
    @DisplayName("toDomain(CreateReq) - Debe mapear parent como null cuando parentId es null")
    void testToDomainFromCreateReqMapsNullParent() {
        // Arrange
        CostCenterCreateReq request = CostCenterCreateReq.builder()
                .idEnterprise(ID_ENTERPRISE)
                .code(CODE)
                .name(NAME)
                .parentId(null)
                .build();

        // Act
        CostCenter domain = mapper.toDomain(request);

        // Assert
        assertNotNull(domain);
        assertNull(domain.getParent());
    }

    @Test
    @DisplayName("toDomain(CreateReq) - Debe ignorar children")
    void testToDomainFromCreateReqIgnoresChildren() {
        // Arrange
        CostCenterCreateReq request = CostCenterCreateReq.builder()
                .idEnterprise(ID_ENTERPRISE)
                .code(CODE)
                .name(NAME)
                .build();

        // Act
        CostCenter domain = mapper.toDomain(request);

        // Assert
        assertNotNull(domain);
        assertNull(domain.getChildren());
    }

    @Test
    @DisplayName("toDomain(CreateReq) - Debe mapear null correctamente")
    void testToDomainFromCreateReqWithNull() {
        // Arrange
        CostCenterCreateReq request = null;

        // Act
        CostCenter domain = mapper.toDomain(request);

        // Assert
        assertNull(domain);
    }

    // ========== TO DOMAIN (UPDATE REQ) TESTS ==========

    @Test
    @DisplayName("toDomain(UpdateReq) - Debe mapear campos básicos correctamente")
    void testToDomainFromUpdateReqMapsBasicFields() {
        // Arrange
        CostCenterUpdateReq request = CostCenterUpdateReq.builder()
                .id(ID)
                .idEnterprise(ID_ENTERPRISE)
                .code(CODE)
                .name(NAME)
                .build();

        // Act
        CostCenter domain = mapper.toDomain(request);

        // Assert
        assertNotNull(domain);
        assertEquals(ID, domain.getId());
        assertEquals(ID_ENTERPRISE, domain.getIdEnterprise());
        assertEquals(CODE, domain.getCode());
        assertEquals(NAME, domain.getName());
    }

    @Test
    @DisplayName("toDomain(UpdateReq) - Debe mapear ID correctamente (a diferencia de CreateReq)")
    void testToDomainFromUpdateReqMapsId() {
        // Arrange
        CostCenterUpdateReq request = CostCenterUpdateReq.builder()
                .id(ID)
                .idEnterprise(ID_ENTERPRISE)
                .code(CODE)
                .name(NAME)
                .build();

        // Act
        CostCenter domain = mapper.toDomain(request);

        // Assert
        assertNotNull(domain);
        assertEquals(ID, domain.getId());
    }

    @Test
    @DisplayName("toDomain(UpdateReq) - Debe mapear parent con solo ID cuando parentId existe")
    void testToDomainFromUpdateReqMapsParentWithId() {
        // Arrange
        CostCenterUpdateReq request = CostCenterUpdateReq.builder()
                .id(ID)
                .idEnterprise(ID_ENTERPRISE)
                .code(CODE)
                .name(NAME)
                .parentId(PARENT_ID)
                .build();

        // Act
        CostCenter domain = mapper.toDomain(request);

        // Assert
        assertNotNull(domain);
        assertNotNull(domain.getParent());
        assertEquals(PARENT_ID, domain.getParent().getId());
    }

    @Test
    @DisplayName("toDomain(UpdateReq) - Debe mapear parent como null cuando parentId es null")
    void testToDomainFromUpdateReqMapsNullParent() {
        // Arrange
        CostCenterUpdateReq request = CostCenterUpdateReq.builder()
                .id(ID)
                .idEnterprise(ID_ENTERPRISE)
                .code(CODE)
                .name(NAME)
                .parentId(null)
                .build();

        // Act
        CostCenter domain = mapper.toDomain(request);

        // Assert
        assertNotNull(domain);
        assertNull(domain.getParent());
    }

    @Test
    @DisplayName("toDomain(UpdateReq) - Debe ignorar children")
    void testToDomainFromUpdateReqIgnoresChildren() {
        // Arrange
        CostCenterUpdateReq request = CostCenterUpdateReq.builder()
                .id(ID)
                .idEnterprise(ID_ENTERPRISE)
                .code(CODE)
                .name(NAME)
                .build();

        // Act
        CostCenter domain = mapper.toDomain(request);

        // Assert
        assertNotNull(domain);
        assertNull(domain.getChildren());
    }

    @Test
    @DisplayName("toDomain(UpdateReq) - Debe mapear null correctamente")
    void testToDomainFromUpdateReqWithNull() {
        // Arrange
        CostCenterUpdateReq request = null;

        // Act
        CostCenter domain = mapper.toDomain(request);

        // Assert
        assertNull(domain);
    }

    // ========== TO RES TESTS ==========

    @Test
    @DisplayName("toRes - Debe mapear campos básicos correctamente")
    void testToResMapsBasicFields() {
        // Arrange
        CostCenter domain = CostCenter.builder()
                .id(ID)
                .idEnterprise(ID_ENTERPRISE)
                .code(CODE)
                .name(NAME)
                .status(STATUS)
                .usageCount(USAGE_COUNT)
                .build();

        // Act
        CostCenterRes response = mapper.toRes(domain);

        // Assert
        assertNotNull(response);
        assertEquals(ID, response.getId());
        assertEquals(ID_ENTERPRISE, response.getIdEnterprise());
        assertEquals(CODE, response.getCode());
        assertEquals(NAME, response.getName());
        assertEquals(STATUS, response.getStatus());
        assertEquals(USAGE_COUNT, response.getUsageCount());
    }

    @Test
    @DisplayName("toRes - Debe mapear parentId desde parent.id cuando existe")
    void testToResMapsParentId() {
        // Arrange
        CostCenter parent = CostCenter.builder().id(PARENT_ID).build();
        CostCenter domain = CostCenter.builder()
                .id(ID)
                .idEnterprise(ID_ENTERPRISE)
                .code(CODE)
                .name(NAME)
                .parent(parent)
                .status(STATUS)
                .build();

        // Act
        CostCenterRes response = mapper.toRes(domain);

        // Assert
        assertNotNull(response);
        assertEquals(PARENT_ID, response.getParentId());
    }

    @Test
    @DisplayName("toRes - Debe mapear parentId como null cuando parent es null")
    void testToResMapsNullParentId() {
        // Arrange
        CostCenter domain = CostCenter.builder()
                .id(ID)
                .idEnterprise(ID_ENTERPRISE)
                .code(CODE)
                .name(NAME)
                .parent(null)
                .status(STATUS)
                .build();

        // Act
        CostCenterRes response = mapper.toRes(domain);

        // Assert
        assertNotNull(response);
        assertNull(response.getParentId());
    }

    @Test
    @DisplayName("toRes - Debe mapear null correctamente")
    void testToResWithNull() {
        // Arrange
        CostCenter domain = null;

        // Act
        CostCenterRes response = mapper.toRes(domain);

        // Assert
        assertNull(response);
    }

    @Test
    @DisplayName("toRes - Debe mapear status false correctamente")
    void testToResWithStatusFalse() {
        // Arrange
        CostCenter domain = CostCenter.builder()
                .id(ID)
                .idEnterprise(ID_ENTERPRISE)
                .code(CODE)
                .name(NAME)
                .status(false)
                .build();

        // Act
        CostCenterRes response = mapper.toRes(domain);

        // Assert
        assertNotNull(response);
        assertFalse(response.getStatus());
    }

    @Test
    @DisplayName("toRes - Debe mapear usageCount cero correctamente")
    void testToResWithZeroUsageCount() {
        // Arrange
        CostCenter domain = CostCenter.builder()
                .id(ID)
                .idEnterprise(ID_ENTERPRISE)
                .code(CODE)
                .name(NAME)
                .usageCount(0)
                .build();

        // Act
        CostCenterRes response = mapper.toRes(domain);

        // Assert
        assertNotNull(response);
        assertEquals(0, response.getUsageCount());
    }
}

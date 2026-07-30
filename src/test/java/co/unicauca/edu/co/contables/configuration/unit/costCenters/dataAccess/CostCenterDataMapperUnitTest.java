package co.unicauca.edu.co.contables.configuration.unit.costCenters.dataAccess;

import co.unicauca.edu.co.contables.configuration.costCenters.dataAccess.entity.CostCenterEntity;
import co.unicauca.edu.co.contables.configuration.costCenters.dataAccess.mapper.CostCenterDataMapper;
import co.unicauca.edu.co.contables.configuration.costCenters.domain.models.CostCenter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CostCenterDataMapperUnitTest {

    private CostCenterDataMapper mapper;

    private static final Long ID = 1L;
    private static final Long PARENT_ID = 10L;
    private static final String CODE = "CC001";
    private static final String NAME = "Centro de costo principal";
    private static final String ID_ENTERPRISE = "ENT001";
    private static final Boolean STATUS = true;
    private static final Integer USAGE_COUNT = 5;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(CostCenterDataMapper.class);
    }

    // ========== TO ENTITY TESTS ==========

    @Test
    @DisplayName("toEntity - Debe mapear campos básicos correctamente")
    void testToEntityMapsBasicFields() {
        // Arrange
        CostCenter domain = CostCenter.builder()
                .id(ID)
                .code(CODE)
                .name(NAME)
                .idEnterprise(ID_ENTERPRISE)
                .status(STATUS)
                .usageCount(USAGE_COUNT)
                .build();

        // Act
        CostCenterEntity entity = mapper.toEntity(domain);

        // Assert
        assertNotNull(entity);
        assertEquals(ID, entity.getId());
        assertEquals(CODE, entity.getCode());
        assertEquals(NAME, entity.getName());
        assertEquals(ID_ENTERPRISE, entity.getIdEnterprise());
        assertEquals(STATUS, entity.getStatus());
        assertEquals(USAGE_COUNT, entity.getUsageCount());
    }

    @Test
    @DisplayName("toEntity - Debe ignorar parent (se asigna en servicio)")
    void testToEntityIgnoresParent() {
        // Arrange
        CostCenter parentDomain = CostCenter.builder().id(PARENT_ID).build();
        CostCenter domain = CostCenter.builder()
                .id(ID)
                .code(CODE)
                .name(NAME)
                .idEnterprise(ID_ENTERPRISE)
                .parent(parentDomain)
                .build();

        // Act
        CostCenterEntity entity = mapper.toEntity(domain);

        // Assert
        assertNotNull(entity);
        assertNull(entity.getParent());
    }

    @Test
    @DisplayName("toEntity - Debe ignorar tenantId (lo maneja Hibernate)")
    void testToEntityIgnoresTenantId() {
        // Arrange
        CostCenter domain = CostCenter.builder()
                .id(ID)
                .code(CODE)
                .name(NAME)
                .idEnterprise(ID_ENTERPRISE)
                .build();

        // Act
        CostCenterEntity entity = mapper.toEntity(domain);

        // Assert
        assertNotNull(entity);
        assertNull(entity.getTenantId());
    }

    @Test
    @DisplayName("toEntity - Debe mapear null correctamente")
    void testToEntityWithNull() {
        // Arrange
        CostCenter domain = null;

        // Act
        CostCenterEntity entity = mapper.toEntity(domain);

        // Assert
        assertNull(entity);
    }

    @Test
    @DisplayName("toEntity - Debe mapear dominio con valores null en campos opcionales")
    void testToEntityWithNullOptionalFields() {
        // Arrange
        CostCenter domain = CostCenter.builder()
                .id(ID)
                .code(CODE)
                .name(NAME)
                .idEnterprise(ID_ENTERPRISE)
                .parent(null)
                .children(null)
                .build();

        // Act
        CostCenterEntity entity = mapper.toEntity(domain);

        // Assert
        assertNotNull(entity);
        assertEquals(ID, entity.getId());
        assertNull(entity.getParent());
    }

    @Test
    @DisplayName("toEntity - Debe mapear status false correctamente")
    void testToEntityWithStatusFalse() {
        // Arrange
        CostCenter domain = CostCenter.builder()
                .id(ID)
                .code(CODE)
                .name(NAME)
                .idEnterprise(ID_ENTERPRISE)
                .status(false)
                .build();

        // Act
        CostCenterEntity entity = mapper.toEntity(domain);

        // Assert
        assertNotNull(entity);
        assertFalse(entity.getStatus());
    }

    @Test
    @DisplayName("toEntity - Debe mapear usageCount cero correctamente")
    void testToEntityWithZeroUsageCount() {
        // Arrange
        CostCenter domain = CostCenter.builder()
                .id(ID)
                .code(CODE)
                .name(NAME)
                .idEnterprise(ID_ENTERPRISE)
                .usageCount(0)
                .build();

        // Act
        CostCenterEntity entity = mapper.toEntity(domain);

        // Assert
        assertNotNull(entity);
        assertEquals(0, entity.getUsageCount());
    }

    // ========== TO DOMAIN TESTS ==========

    @Test
    @DisplayName("toDomain - Debe mapear campos básicos correctamente")
    void testToDomainMapsBasicFields() {
        // Arrange
        CostCenterEntity entity = CostCenterEntity.builder()
                .id(ID)
                .code(CODE)
                .name(NAME)
                .idEnterprise(ID_ENTERPRISE)
                .status(STATUS)
                .usageCount(USAGE_COUNT)
                .build();

        // Act
        CostCenter domain = mapper.toDomain(entity);

        // Assert
        assertNotNull(domain);
        assertEquals(ID, domain.getId());
        assertEquals(CODE, domain.getCode());
        assertEquals(NAME, domain.getName());
        assertEquals(ID_ENTERPRISE, domain.getIdEnterprise());
        assertEquals(STATUS, domain.getStatus());
        assertEquals(USAGE_COUNT, domain.getUsageCount());
    }

    @Test
    @DisplayName("toDomain - Debe mapear parent con solo ID cuando existe")
    void testToDomainMapsParentWithIdOnly() {
        // Arrange
        CostCenterEntity parentEntity = CostCenterEntity.builder()
                .id(PARENT_ID)
                .code("PARENT")
                .name("Centro padre")
                .idEnterprise(ID_ENTERPRISE)
                .build();
        CostCenterEntity entity = CostCenterEntity.builder()
                .id(ID)
                .code(CODE)
                .name(NAME)
                .idEnterprise(ID_ENTERPRISE)
                .parent(parentEntity)
                .build();

        // Act
        CostCenter domain = mapper.toDomain(entity);

        // Assert
        assertNotNull(domain);
        assertNotNull(domain.getParent());
        assertEquals(PARENT_ID, domain.getParent().getId());
    }

    @Test
    @DisplayName("toDomain - Debe mapear parent como null cuando no existe")
    void testToDomainMapsNullParent() {
        // Arrange
        CostCenterEntity entity = CostCenterEntity.builder()
                .id(ID)
                .code(CODE)
                .name(NAME)
                .idEnterprise(ID_ENTERPRISE)
                .parent(null)
                .build();

        // Act
        CostCenter domain = mapper.toDomain(entity);

        // Assert
        assertNotNull(domain);
        assertNull(domain.getParent());
    }

    @Test
    @DisplayName("toDomain - Debe ignorar children (se cargan manualmente)")
    void testToDomainIgnoresChildren() {
        // Arrange
        CostCenterEntity entity = CostCenterEntity.builder()
                .id(ID)
                .code(CODE)
                .name(NAME)
                .idEnterprise(ID_ENTERPRISE)
                .build();

        // Act
        CostCenter domain = mapper.toDomain(entity);

        // Assert
        assertNotNull(domain);
        assertNull(domain.getChildren());
    }

    @Test
    @DisplayName("toDomain - Debe mapear null correctamente")
    void testToDomainWithNull() {
        // Arrange
        CostCenterEntity entity = null;

        // Act
        CostCenter domain = mapper.toDomain(entity);

        // Assert
        assertNull(domain);
    }

    @Test
    @DisplayName("toDomain - Debe mapear status false correctamente")
    void testToDomainWithStatusFalse() {
        // Arrange
        CostCenterEntity entity = CostCenterEntity.builder()
                .id(ID)
                .code(CODE)
                .name(NAME)
                .idEnterprise(ID_ENTERPRISE)
                .status(false)
                .build();

        // Act
        CostCenter domain = mapper.toDomain(entity);

        // Assert
        assertNotNull(domain);
        assertFalse(domain.getStatus());
    }

    @Test
    @DisplayName("toDomain - Debe mapear usageCount alto correctamente")
    void testToDomainWithHighUsageCount() {
        // Arrange
        CostCenterEntity entity = CostCenterEntity.builder()
                .id(ID)
                .code(CODE)
                .name(NAME)
                .idEnterprise(ID_ENTERPRISE)
                .usageCount(1000)
                .build();

        // Act
        CostCenter domain = mapper.toDomain(entity);

        // Assert
        assertNotNull(domain);
        assertEquals(1000, domain.getUsageCount());
    }

    @Test
    @DisplayName("toDomain - Parent mapeado no debe incluir otros campos del padre")
    void testToDomainParentOnlyContainsId() {
        // Arrange
        CostCenterEntity parentEntity = CostCenterEntity.builder()
                .id(PARENT_ID)
                .code("PARENT_CODE")
                .name("Nombre del padre")
                .idEnterprise(ID_ENTERPRISE)
                .status(true)
                .usageCount(50)
                .build();
        CostCenterEntity entity = CostCenterEntity.builder()
                .id(ID)
                .code(CODE)
                .name(NAME)
                .idEnterprise(ID_ENTERPRISE)
                .parent(parentEntity)
                .build();

        // Act
        CostCenter domain = mapper.toDomain(entity);

        // Assert
        assertNotNull(domain.getParent());
        assertEquals(PARENT_ID, domain.getParent().getId());
        assertNull(domain.getParent().getCode());
        assertNull(domain.getParent().getName());
        assertNull(domain.getParent().getIdEnterprise());
    }
}

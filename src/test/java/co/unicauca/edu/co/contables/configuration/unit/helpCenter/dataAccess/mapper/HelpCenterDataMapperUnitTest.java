package co.unicauca.edu.co.contables.configuration.unit.helpCenter.dataAccess.mapper;

import co.unicauca.edu.co.contables.configuration.helpCenter.dataAccess.entity.HelpCenterEntity;
import co.unicauca.edu.co.contables.configuration.helpCenter.dataAccess.mapper.HelpCenterDataMapper;
import co.unicauca.edu.co.contables.configuration.helpCenter.domain.enums.DocumentModule;
import co.unicauca.edu.co.contables.configuration.helpCenter.domain.models.HelpCenter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class HelpCenterDataMapperUnitTest {

    private HelpCenterDataMapper mapper;

    private static final Long ID = 1L;
    private static final Integer MODULE_ID = 3;
    private static final DocumentModule MODULE = DocumentModule.COMERCIAL;
    private static final String NAME = "Guía de facturación";
    private static final String DESCRIPTION = "Descripción detallada de cómo realizar facturación";
    private static final Boolean STATUS = true;

    @BeforeEach
    void setUp() {
        mapper = new HelpCenterDataMapper();
    }

    // ========== TO DOMAIN TESTS ==========

    @Test
    @DisplayName("toDomain - Debe mapear campos básicos correctamente")
    void testToDomainMapsBasicFields() {
        // Arrange
        HelpCenterEntity entity = HelpCenterEntity.builder()
                .id(ID)
                .module(MODULE)
                .moduleId(MODULE_ID)
                .name(NAME)
                .description(DESCRIPTION)
                .status(STATUS)
                .build();

        // Act
        HelpCenter domain = mapper.toDomain(entity);

        // Assert
        assertNotNull(domain);
        assertEquals(ID, domain.getId());
        assertEquals(MODULE_ID, domain.getModuleId());
        assertEquals(NAME, domain.getName());
        assertEquals(DESCRIPTION, domain.getDescription());
        assertEquals(STATUS, domain.getStatus());
    }

    @Test
    @DisplayName("toDomain - Debe extraer moduleId desde module.getId()")
    void testToDomainExtractsModuleIdFromModule() {
        // Arrange
        HelpCenterEntity entity = HelpCenterEntity.builder()
                .id(ID)
                .module(DocumentModule.TESORERIA)
                .moduleId(4)
                .name(NAME)
                .description(DESCRIPTION)
                .status(STATUS)
                .build();

        // Act
        HelpCenter domain = mapper.toDomain(entity);

        // Assert
        assertNotNull(domain);
        assertEquals(DocumentModule.TESORERIA.getId(), domain.getModuleId());
    }

    @Test
    @DisplayName("toDomain - Debe mapear moduleId como null cuando module es null")
    void testToDomainMapsNullModuleId() {
        // Arrange
        HelpCenterEntity entity = HelpCenterEntity.builder()
                .id(ID)
                .module(null)
                .moduleId(null)
                .name(NAME)
                .description(DESCRIPTION)
                .status(STATUS)
                .build();

        // Act
        HelpCenter domain = mapper.toDomain(entity);

        // Assert
        assertNotNull(domain);
        assertNull(domain.getModuleId());
    }

    @Test
    @DisplayName("toDomain - Debe mapear null correctamente")
    void testToDomainWithNull() {
        // Arrange
        HelpCenterEntity entity = null;

        // Act
        HelpCenter domain = mapper.toDomain(entity);

        // Assert
        assertNull(domain);
    }

    @Test
    @DisplayName("toDomain - Debe mapear status false correctamente")
    void testToDomainWithStatusFalse() {
        // Arrange
        HelpCenterEntity entity = HelpCenterEntity.builder()
                .id(ID)
                .module(MODULE)
                .moduleId(MODULE_ID)
                .name(NAME)
                .description(DESCRIPTION)
                .status(false)
                .build();

        // Act
        HelpCenter domain = mapper.toDomain(entity);

        // Assert
        assertNotNull(domain);
        assertFalse(domain.getStatus());
    }

    @Test
    @DisplayName("toDomain - Debe mapear todos los módulos disponibles")
    void testToDomainMapsAllModules() {
        // Arrange & Act & Assert
        for (DocumentModule module : DocumentModule.values()) {
            HelpCenterEntity entity = HelpCenterEntity.builder()
                    .id(ID)
                    .module(module)
                    .moduleId(module.getId())
                    .name(NAME)
                    .description(DESCRIPTION)
                    .status(STATUS)
                    .build();

            HelpCenter domain = mapper.toDomain(entity);

            assertNotNull(domain);
            assertEquals(module.getId(), domain.getModuleId());
        }
    }

    // ========== TO ENTITY TESTS ==========

    @Test
    @DisplayName("toEntity - Debe mapear campos básicos correctamente")
    void testToEntityMapsBasicFields() {
        // Arrange
        HelpCenter domain = HelpCenter.builder()
                .id(ID)
                .moduleId(MODULE_ID)
                .name(NAME)
                .description(DESCRIPTION)
                .status(STATUS)
                .build();

        // Act
        HelpCenterEntity entity = mapper.toEntity(domain);

        // Assert
        assertNotNull(entity);
        assertEquals(ID, entity.getId());
        assertEquals(MODULE_ID, entity.getModuleId());
        assertEquals(NAME, entity.getName());
        assertEquals(DESCRIPTION, entity.getDescription());
        assertEquals(STATUS, entity.getStatus());
    }

    @Test
    @DisplayName("toEntity - Debe convertir moduleId a DocumentModule enum")
    void testToEntityConvertsModuleIdToEnum() {
        // Arrange
        HelpCenter domain = HelpCenter.builder()
                .id(ID)
                .moduleId(MODULE_ID)
                .name(NAME)
                .description(DESCRIPTION)
                .status(STATUS)
                .build();

        // Act
        HelpCenterEntity entity = mapper.toEntity(domain);

        // Assert
        assertNotNull(entity);
        assertEquals(DocumentModule.COMERCIAL, entity.getModule());
    }

    @Test
    @DisplayName("toEntity - Debe mapear module como null cuando moduleId es null")
    void testToEntityMapsNullModule() {
        // Arrange
        HelpCenter domain = HelpCenter.builder()
                .id(ID)
                .moduleId(null)
                .name(NAME)
                .description(DESCRIPTION)
                .status(STATUS)
                .build();

        // Act
        HelpCenterEntity entity = mapper.toEntity(domain);

        // Assert
        assertNotNull(entity);
        assertNull(entity.getModule());
        assertNull(entity.getModuleId());
    }

    @Test
    @DisplayName("toEntity - Debe mapear null correctamente")
    void testToEntityWithNull() {
        // Arrange
        HelpCenter domain = null;

        // Act
        HelpCenterEntity entity = mapper.toEntity(domain);

        // Assert
        assertNull(entity);
    }

    @Test
    @DisplayName("toEntity - Debe mapear status false correctamente")
    void testToEntityWithStatusFalse() {
        // Arrange
        HelpCenter domain = HelpCenter.builder()
                .id(ID)
                .moduleId(MODULE_ID)
                .name(NAME)
                .description(DESCRIPTION)
                .status(false)
                .build();

        // Act
        HelpCenterEntity entity = mapper.toEntity(domain);

        // Assert
        assertNotNull(entity);
        assertFalse(entity.getStatus());
    }

    @Test
    @DisplayName("toEntity - Debe mapear todos los módulos disponibles")
    void testToEntityMapsAllModules() {
        // Arrange & Act & Assert
        for (DocumentModule module : DocumentModule.values()) {
            HelpCenter domain = HelpCenter.builder()
                    .id(ID)
                    .moduleId(module.getId())
                    .name(NAME)
                    .description(DESCRIPTION)
                    .status(STATUS)
                    .build();

            HelpCenterEntity entity = mapper.toEntity(domain);

            assertNotNull(entity);
            assertEquals(module, entity.getModule());
            assertEquals(module.getId(), entity.getModuleId());
        }
    }

    @Test
    @DisplayName("toEntity - Debe mapear módulo INVENTARIO_PROMEDIO_PONDERADO correctamente")
    void testToEntityMapsInventarioPromedioPonderado() {
        // Arrange
        HelpCenter domain = HelpCenter.builder()
                .id(ID)
                .moduleId(1)
                .name(NAME)
                .description(DESCRIPTION)
                .status(STATUS)
                .build();

        // Act
        HelpCenterEntity entity = mapper.toEntity(domain);

        // Assert
        assertNotNull(entity);
        assertEquals(DocumentModule.INVENTARIO_PROMEDIO_PONDERADO, entity.getModule());
    }

    @Test
    @DisplayName("toEntity - Debe mapear módulo CONFIGURACION correctamente")
    void testToEntityMapsConfiguracion() {
        // Arrange
        HelpCenter domain = HelpCenter.builder()
                .id(ID)
                .moduleId(9)
                .name(NAME)
                .description(DESCRIPTION)
                .status(STATUS)
                .build();

        // Act
        HelpCenterEntity entity = mapper.toEntity(domain);

        // Assert
        assertNotNull(entity);
        assertEquals(DocumentModule.CONFIGURACION, entity.getModule());
    }

    // ========== BIDIRECTIONAL MAPPING TESTS ==========

    @Test
    @DisplayName("Mapeo bidireccional - Domain -> Entity -> Domain debe preservar datos")
    void testBidirectionalMappingPreservesData() {
        // Arrange
        HelpCenter originalDomain = HelpCenter.builder()
                .id(ID)
                .moduleId(MODULE_ID)
                .name(NAME)
                .description(DESCRIPTION)
                .status(STATUS)
                .build();

        // Act
        HelpCenterEntity entity = mapper.toEntity(originalDomain);
        HelpCenter resultDomain = mapper.toDomain(entity);

        // Assert
        assertNotNull(resultDomain);
        assertEquals(originalDomain.getId(), resultDomain.getId());
        assertEquals(originalDomain.getModuleId(), resultDomain.getModuleId());
        assertEquals(originalDomain.getName(), resultDomain.getName());
        assertEquals(originalDomain.getDescription(), resultDomain.getDescription());
        assertEquals(originalDomain.getStatus(), resultDomain.getStatus());
    }

    @Test
    @DisplayName("Mapeo bidireccional - Entity -> Domain -> Entity debe preservar datos")
    void testBidirectionalMappingFromEntityPreservesData() {
        // Arrange
        HelpCenterEntity originalEntity = HelpCenterEntity.builder()
                .id(ID)
                .module(MODULE)
                .moduleId(MODULE_ID)
                .name(NAME)
                .description(DESCRIPTION)
                .status(STATUS)
                .build();

        // Act
        HelpCenter domain = mapper.toDomain(originalEntity);
        HelpCenterEntity resultEntity = mapper.toEntity(domain);

        // Assert
        assertNotNull(resultEntity);
        assertEquals(originalEntity.getId(), resultEntity.getId());
        assertEquals(originalEntity.getModule(), resultEntity.getModule());
        assertEquals(originalEntity.getModuleId(), resultEntity.getModuleId());
        assertEquals(originalEntity.getName(), resultEntity.getName());
        assertEquals(originalEntity.getDescription(), resultEntity.getDescription());
        assertEquals(originalEntity.getStatus(), resultEntity.getStatus());
    }
}

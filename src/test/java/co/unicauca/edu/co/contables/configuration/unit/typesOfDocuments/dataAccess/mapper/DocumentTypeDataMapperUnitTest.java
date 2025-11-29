package co.unicauca.edu.co.contables.configuration.unit.typesOfDocuments.dataAccess.mapper;

import co.unicauca.edu.co.contables.configuration.classesOfDocuments.dataAccess.entity.DocumentClassEntity;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.dataAccess.entity.DocumentTypeEntity;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.dataAccess.mapper.DocumentTypeDataMapper;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.enums.DocumentModule;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.models.DocumentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DocumentTypeDataMapperUnitTest {

    private DocumentTypeDataMapper mapper;

    private static final Long ID = 1L;
    private static final String PREFIX = "FAC";
    private static final String NAME = "Factura de venta";
    private static final Long DOCUMENT_CLASS_ID = 10L;
    private static final Integer MODULE_ID = 3;
    private static final String MODULE_NAME = "Comercial";
    private static final String ID_ENTERPRISE = "ENT001";
    private static final Boolean STATUS = true;
    private static final Integer USAGE_COUNT = 5;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(DocumentTypeDataMapper.class);
    }

    // ========== TO ENTITY TESTS ==========

    @Test
    @DisplayName("toEntity - Debe mapear campos básicos correctamente")
    void testToEntityMapsBasicFields() {
        // Arrange
        DocumentType domain = DocumentType.builder()
                .id(ID)
                .prefix(PREFIX)
                .name(NAME)
                .documentClassId(DOCUMENT_CLASS_ID)
                .moduleId(MODULE_ID)
                .idEnterprise(ID_ENTERPRISE)
                .status(STATUS)
                .usageCount(USAGE_COUNT)
                .build();

        // Act
        DocumentTypeEntity entity = mapper.toEntity(domain);

        // Assert
        assertNotNull(entity);
        assertEquals(ID, entity.getId());
        assertEquals(PREFIX, entity.getPrefix());
        assertEquals(NAME, entity.getName());
        assertEquals(ID_ENTERPRISE, entity.getIdEnterprise());
        assertEquals(STATUS, entity.getStatus());
        assertEquals(USAGE_COUNT, entity.getUsageCount());
    }

    @Test
    @DisplayName("toEntity - Debe mapear documentClass desde documentClassId")
    void testToEntityMapsDocumentClass() {
        // Arrange
        DocumentType domain = DocumentType.builder()
                .id(ID)
                .prefix(PREFIX)
                .name(NAME)
                .documentClassId(DOCUMENT_CLASS_ID)
                .moduleId(MODULE_ID)
                .idEnterprise(ID_ENTERPRISE)
                .build();

        // Act
        DocumentTypeEntity entity = mapper.toEntity(domain);

        // Assert
        assertNotNull(entity);
        assertNotNull(entity.getDocumentClass());
        assertEquals(DOCUMENT_CLASS_ID, entity.getDocumentClass().getId());
    }

    @Test
    @DisplayName("toEntity - Debe mapear module desde moduleId")
    void testToEntityMapsModuleFromModuleId() {
        // Arrange
        DocumentType domain = DocumentType.builder()
                .id(ID)
                .prefix(PREFIX)
                .name(NAME)
                .documentClassId(DOCUMENT_CLASS_ID)
                .moduleId(MODULE_ID)
                .idEnterprise(ID_ENTERPRISE)
                .build();

        // Act
        DocumentTypeEntity entity = mapper.toEntity(domain);

        // Assert
        assertNotNull(entity);
        assertEquals(MODULE_NAME, entity.getModule());
    }

    @Test
    @DisplayName("toEntity - Debe mapear documentClass como null cuando documentClassId es null")
    void testToEntityMapsNullDocumentClass() {
        // Arrange
        DocumentType domain = DocumentType.builder()
                .id(ID)
                .prefix(PREFIX)
                .name(NAME)
                .documentClassId(null)
                .moduleId(MODULE_ID)
                .idEnterprise(ID_ENTERPRISE)
                .build();

        // Act
        DocumentTypeEntity entity = mapper.toEntity(domain);

        // Assert
        assertNotNull(entity);
        assertNull(entity.getDocumentClass());
    }

    @Test
    @DisplayName("toEntity - Debe mapear module como null cuando moduleId es null")
    void testToEntityMapsNullModule() {
        // Arrange
        DocumentType domain = DocumentType.builder()
                .id(ID)
                .prefix(PREFIX)
                .name(NAME)
                .documentClassId(DOCUMENT_CLASS_ID)
                .moduleId(null)
                .idEnterprise(ID_ENTERPRISE)
                .build();

        // Act
        DocumentTypeEntity entity = mapper.toEntity(domain);

        // Assert
        assertNotNull(entity);
        assertNull(entity.getModule());
    }

    @Test
    @DisplayName("toEntity - Debe mapear module como null cuando moduleId es inválido")
    void testToEntityMapsNullModuleForInvalidId() {
        // Arrange
        DocumentType domain = DocumentType.builder()
                .id(ID)
                .prefix(PREFIX)
                .name(NAME)
                .documentClassId(DOCUMENT_CLASS_ID)
                .moduleId(999)
                .idEnterprise(ID_ENTERPRISE)
                .build();

        // Act
        DocumentTypeEntity entity = mapper.toEntity(domain);

        // Assert
        assertNotNull(entity);
        assertNull(entity.getModule());
    }

    @Test
    @DisplayName("toEntity - Debe ignorar tenantId")
    void testToEntityIgnoresTenantId() {
        // Arrange
        DocumentType domain = DocumentType.builder()
                .id(ID)
                .prefix(PREFIX)
                .name(NAME)
                .documentClassId(DOCUMENT_CLASS_ID)
                .moduleId(MODULE_ID)
                .idEnterprise(ID_ENTERPRISE)
                .build();

        // Act
        DocumentTypeEntity entity = mapper.toEntity(domain);

        // Assert
        assertNotNull(entity);
        assertNull(entity.getTenantId());
    }

    @Test
    @DisplayName("toEntity - Debe mapear null correctamente")
    void testToEntityWithNull() {
        // Arrange
        DocumentType domain = null;

        // Act
        DocumentTypeEntity entity = mapper.toEntity(domain);

        // Assert
        assertNull(entity);
    }

    // ========== TO DOMAIN TESTS ==========

    @Test
    @DisplayName("toDomain - Debe mapear campos básicos correctamente")
    void testToDomainMapsBasicFields() {
        // Arrange
        DocumentClassEntity documentClass = new DocumentClassEntity();
        documentClass.setId(DOCUMENT_CLASS_ID);
        
        DocumentTypeEntity entity = DocumentTypeEntity.builder()
                .id(ID)
                .prefix(PREFIX)
                .name(NAME)
                .documentClass(documentClass)
                .module(MODULE_NAME)
                .idEnterprise(ID_ENTERPRISE)
                .status(STATUS)
                .usageCount(USAGE_COUNT)
                .build();

        // Act
        DocumentType domain = mapper.toDomain(entity);

        // Assert
        assertNotNull(domain);
        assertEquals(ID, domain.getId());
        assertEquals(PREFIX, domain.getPrefix());
        assertEquals(NAME, domain.getName());
        assertEquals(ID_ENTERPRISE, domain.getIdEnterprise());
        assertEquals(STATUS, domain.getStatus());
        assertEquals(USAGE_COUNT, domain.getUsageCount());
    }

    @Test
    @DisplayName("toDomain - Debe mapear documentClassId desde documentClass.getId()")
    void testToDomainMapsDocumentClassId() {
        // Arrange
        DocumentClassEntity documentClass = new DocumentClassEntity();
        documentClass.setId(DOCUMENT_CLASS_ID);
        
        DocumentTypeEntity entity = DocumentTypeEntity.builder()
                .id(ID)
                .prefix(PREFIX)
                .name(NAME)
                .documentClass(documentClass)
                .module(MODULE_NAME)
                .idEnterprise(ID_ENTERPRISE)
                .build();

        // Act
        DocumentType domain = mapper.toDomain(entity);

        // Assert
        assertNotNull(domain);
        assertEquals(DOCUMENT_CLASS_ID, domain.getDocumentClassId());
    }

    @Test
    @DisplayName("toDomain - Debe mapear moduleId desde module name")
    void testToDomainMapsModuleId() {
        // Arrange
        DocumentClassEntity documentClass = new DocumentClassEntity();
        documentClass.setId(DOCUMENT_CLASS_ID);
        
        DocumentTypeEntity entity = DocumentTypeEntity.builder()
                .id(ID)
                .prefix(PREFIX)
                .name(NAME)
                .documentClass(documentClass)
                .module(MODULE_NAME)
                .idEnterprise(ID_ENTERPRISE)
                .build();

        // Act
        DocumentType domain = mapper.toDomain(entity);

        // Assert
        assertNotNull(domain);
        assertEquals(MODULE_ID, domain.getModuleId());
    }

    @Test
    @DisplayName("toDomain - Debe mapear documentClassId como null cuando documentClass es null")
    void testToDomainMapsNullDocumentClassId() {
        // Arrange
        DocumentTypeEntity entity = DocumentTypeEntity.builder()
                .id(ID)
                .prefix(PREFIX)
                .name(NAME)
                .documentClass(null)
                .module(MODULE_NAME)
                .idEnterprise(ID_ENTERPRISE)
                .build();

        // Act
        DocumentType domain = mapper.toDomain(entity);

        // Assert
        assertNotNull(domain);
        assertNull(domain.getDocumentClassId());
    }

    @Test
    @DisplayName("toDomain - Debe mapear moduleId como null cuando module es null")
    void testToDomainMapsNullModuleId() {
        // Arrange
        DocumentClassEntity documentClass = new DocumentClassEntity();
        documentClass.setId(DOCUMENT_CLASS_ID);
        
        DocumentTypeEntity entity = DocumentTypeEntity.builder()
                .id(ID)
                .prefix(PREFIX)
                .name(NAME)
                .documentClass(documentClass)
                .module(null)
                .idEnterprise(ID_ENTERPRISE)
                .build();

        // Act
        DocumentType domain = mapper.toDomain(entity);

        // Assert
        assertNotNull(domain);
        assertNull(domain.getModuleId());
    }

    @Test
    @DisplayName("toDomain - Debe mapear moduleId como null cuando module es vacío")
    void testToDomainMapsNullModuleIdForEmptyModule() {
        // Arrange
        DocumentClassEntity documentClass = new DocumentClassEntity();
        documentClass.setId(DOCUMENT_CLASS_ID);
        
        DocumentTypeEntity entity = DocumentTypeEntity.builder()
                .id(ID)
                .prefix(PREFIX)
                .name(NAME)
                .documentClass(documentClass)
                .module("   ")
                .idEnterprise(ID_ENTERPRISE)
                .build();

        // Act
        DocumentType domain = mapper.toDomain(entity);

        // Assert
        assertNotNull(domain);
        assertNull(domain.getModuleId());
    }

    @Test
    @DisplayName("toDomain - Debe mapear moduleId como null cuando module es inválido")
    void testToDomainMapsNullModuleIdForInvalidModule() {
        // Arrange
        DocumentClassEntity documentClass = new DocumentClassEntity();
        documentClass.setId(DOCUMENT_CLASS_ID);
        
        DocumentTypeEntity entity = DocumentTypeEntity.builder()
                .id(ID)
                .prefix(PREFIX)
                .name(NAME)
                .documentClass(documentClass)
                .module("Módulo inexistente")
                .idEnterprise(ID_ENTERPRISE)
                .build();

        // Act
        DocumentType domain = mapper.toDomain(entity);

        // Assert
        assertNotNull(domain);
        assertNull(domain.getModuleId());
    }

    @Test
    @DisplayName("toDomain - Debe mapear null correctamente")
    void testToDomainWithNull() {
        // Arrange
        DocumentTypeEntity entity = null;

        // Act
        DocumentType domain = mapper.toDomain(entity);

        // Assert
        assertNull(domain);
    }

    // ========== MAP DOCUMENT CLASS TESTS ==========

    @Test
    @DisplayName("mapDocumentClass - Debe crear entidad con ID")
    void testMapDocumentClassCreatesEntityWithId() {
        // Arrange & Act
        DocumentClassEntity result = mapper.mapDocumentClass(DOCUMENT_CLASS_ID);

        // Assert
        assertNotNull(result);
        assertEquals(DOCUMENT_CLASS_ID, result.getId());
    }

    @Test
    @DisplayName("mapDocumentClass - Debe retornar null cuando ID es null")
    void testMapDocumentClassReturnsNullForNullId() {
        // Arrange & Act
        DocumentClassEntity result = mapper.mapDocumentClass(null);

        // Assert
        assertNull(result);
    }

    // ========== MAP MODULE ID TO NAME TESTS ==========

    @Test
    @DisplayName("mapModuleIdToName - Debe retornar nombre del módulo para ID válido")
    void testMapModuleIdToNameReturnsModuleName() {
        // Arrange & Act
        String result = mapper.mapModuleIdToName(MODULE_ID);

        // Assert
        assertEquals(MODULE_NAME, result);
    }

    @Test
    @DisplayName("mapModuleIdToName - Debe retornar null para ID null")
    void testMapModuleIdToNameReturnsNullForNullId() {
        // Arrange & Act
        String result = mapper.mapModuleIdToName(null);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("mapModuleIdToName - Debe retornar null para ID inválido")
    void testMapModuleIdToNameReturnsNullForInvalidId() {
        // Arrange & Act
        String result = mapper.mapModuleIdToName(999);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("mapModuleIdToName - Debe mapear todos los módulos")
    void testMapModuleIdToNameMapsAllModules() {
        // Arrange & Act & Assert
        for (DocumentModule module : DocumentModule.values()) {
            String result = mapper.mapModuleIdToName(module.getId());
            assertEquals(module.getName(), result);
        }
    }

    // ========== MAP MODULE NAME TO ID TESTS ==========

    @Test
    @DisplayName("mapModuleNameToId - Debe retornar ID del módulo para nombre válido")
    void testMapModuleNameToIdReturnsModuleId() {
        // Arrange & Act
        Integer result = mapper.mapModuleNameToId(MODULE_NAME);

        // Assert
        assertEquals(MODULE_ID, result);
    }

    @Test
    @DisplayName("mapModuleNameToId - Debe retornar null para nombre null")
    void testMapModuleNameToIdReturnsNullForNullName() {
        // Arrange & Act
        Integer result = mapper.mapModuleNameToId(null);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("mapModuleNameToId - Debe retornar null para nombre vacío")
    void testMapModuleNameToIdReturnsNullForEmptyName() {
        // Arrange & Act
        Integer result = mapper.mapModuleNameToId("");

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("mapModuleNameToId - Debe retornar null para nombre con solo espacios")
    void testMapModuleNameToIdReturnsNullForWhitespaceName() {
        // Arrange & Act
        Integer result = mapper.mapModuleNameToId("   ");

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("mapModuleNameToId - Debe retornar null para nombre inválido")
    void testMapModuleNameToIdReturnsNullForInvalidName() {
        // Arrange & Act
        Integer result = mapper.mapModuleNameToId("Módulo inexistente");

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("mapModuleNameToId - Debe mapear todos los módulos")
    void testMapModuleNameToIdMapsAllModules() {
        // Arrange & Act & Assert
        for (DocumentModule module : DocumentModule.values()) {
            Integer result = mapper.mapModuleNameToId(module.getName());
            assertEquals(module.getId(), result);
        }
    }

    // ========== BIDIRECTIONAL MAPPING TESTS ==========

    @Test
    @DisplayName("Mapeo bidireccional - Domain -> Entity -> Domain debe preservar datos")
    void testBidirectionalMappingPreservesData() {
        // Arrange
        DocumentType originalDomain = DocumentType.builder()
                .id(ID)
                .prefix(PREFIX)
                .name(NAME)
                .documentClassId(DOCUMENT_CLASS_ID)
                .moduleId(MODULE_ID)
                .idEnterprise(ID_ENTERPRISE)
                .status(STATUS)
                .usageCount(USAGE_COUNT)
                .build();

        // Act
        DocumentTypeEntity entity = mapper.toEntity(originalDomain);
        DocumentType resultDomain = mapper.toDomain(entity);

        // Assert
        assertNotNull(resultDomain);
        assertEquals(originalDomain.getId(), resultDomain.getId());
        assertEquals(originalDomain.getPrefix(), resultDomain.getPrefix());
        assertEquals(originalDomain.getName(), resultDomain.getName());
        assertEquals(originalDomain.getDocumentClassId(), resultDomain.getDocumentClassId());
        assertEquals(originalDomain.getModuleId(), resultDomain.getModuleId());
        assertEquals(originalDomain.getIdEnterprise(), resultDomain.getIdEnterprise());
        assertEquals(originalDomain.getStatus(), resultDomain.getStatus());
        assertEquals(originalDomain.getUsageCount(), resultDomain.getUsageCount());
    }
}

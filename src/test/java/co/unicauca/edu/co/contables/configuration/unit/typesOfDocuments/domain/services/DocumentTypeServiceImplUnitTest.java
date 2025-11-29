package co.unicauca.edu.co.contables.configuration.unit.typesOfDocuments.domain.services;

import co.unicauca.edu.co.contables.configuration.classesOfDocuments.dataAccess.entity.DocumentClassEntity;
import co.unicauca.edu.co.contables.configuration.classesOfDocuments.dataAccess.repository.DocumentClassRepository;
import co.unicauca.edu.co.contables.configuration.commons.exceptions.documentClasses.DocumentClassesNotFoundException;
import co.unicauca.edu.co.contables.configuration.commons.exceptions.documentClasses.DocumentClassInactiveException;
import co.unicauca.edu.co.contables.configuration.commons.exceptions.documentTypes.DocumentTypeInUseException;
import co.unicauca.edu.co.contables.configuration.commons.exceptions.documentTypes.DocumentTypesAlreadyExistsException;
import co.unicauca.edu.co.contables.configuration.commons.exceptions.documentTypes.DocumentTypesNotFoundException;
import co.unicauca.edu.co.contables.configuration.commons.exceptions.documentTypes.InvalidModuleException;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.dataAccess.entity.DocumentTypeEntity;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.dataAccess.mapper.DocumentTypeDataMapper;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.dataAccess.repository.DocumentTypeRepository;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.enums.DocumentModule;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.mapper.DocumentTypeDomainMapper;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.models.DocumentType;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.services.DocumentTypeServiceImpl;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.presentation.DTO.request.DocumentTypeCreateReq;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.presentation.DTO.request.DocumentTypeUpdateReq;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DocumentTypeServiceImplUnitTest {

    @Mock
    private DocumentTypeRepository repository;

    @Mock
    private DocumentTypeDataMapper dataMapper;

    @Mock
    private DocumentTypeDomainMapper domainMapper;

    @Mock
    private DocumentClassRepository documentClassRepository;

    @InjectMocks
    private DocumentTypeServiceImpl documentTypeService;

    private static final Long ID = 1L;
    private static final String ID_ENTERPRISE = "ENT001";
    private static final String PREFIX = "FAC";
    private static final String NAME = "Factura de venta";
    private static final Long DOCUMENT_CLASS_ID = 10L;
    private static final Integer MODULE_ID = 3;
    private static final String MODULE_NAME = "Comercial";

    private DocumentTypeCreateReq createReq;
    private DocumentTypeUpdateReq updateReq;
    private DocumentTypeEntity entity;
    private DocumentType domain;
    private DocumentClassEntity documentClassEntity;

    @BeforeEach
    void setUp() {
        createReq = DocumentTypeCreateReq.builder()
                .idEnterprise(ID_ENTERPRISE)
                .prefix(PREFIX)
                .name(NAME)
                .documentClassId(DOCUMENT_CLASS_ID)
                .moduleId(MODULE_ID)
                .build();

        updateReq = DocumentTypeUpdateReq.builder()
                .id(ID)
                .idEnterprise(ID_ENTERPRISE)
                .prefix(PREFIX)
                .name(NAME)
                .documentClassId(DOCUMENT_CLASS_ID)
                .moduleId(MODULE_ID)
                .build();

        documentClassEntity = DocumentClassEntity.builder()
                .id(DOCUMENT_CLASS_ID)
                .name("Clase de prueba")
                .idEnterprise(ID_ENTERPRISE)
                .status(true)
                .build();

        entity = DocumentTypeEntity.builder()
                .id(ID)
                .prefix("Fac")
                .name("Factura de venta")
                .documentClass(documentClassEntity)
                .module(MODULE_NAME)
                .idEnterprise(ID_ENTERPRISE)
                .status(true)
                .usageCount(0)
                .build();

        domain = DocumentType.builder()
                .id(ID)
                .prefix("Fac")
                .name("Factura de venta")
                .documentClassId(DOCUMENT_CLASS_ID)
                .moduleId(MODULE_ID)
                .idEnterprise(ID_ENTERPRISE)
                .status(true)
                .usageCount(0)
                .build();
    }

    // ========== CREATE TESTS ==========

    @Test
    @DisplayName("create - Debe crear tipo de documento exitosamente")
    void testCreateSuccessfully() {
        // Arrange
        when(repository.existsByPrefixAndIdEnterprise(anyString(), eq(ID_ENTERPRISE))).thenReturn(false);
        when(repository.existsByNameAndIdEnterprise(anyString(), eq(ID_ENTERPRISE))).thenReturn(false);
        when(documentClassRepository.findByIdAndIdEnterpriseAndStatus(DOCUMENT_CLASS_ID, ID_ENTERPRISE, true))
                .thenReturn(Optional.of(documentClassEntity));
        when(domainMapper.toDomain(createReq)).thenReturn(domain);
        when(dataMapper.toEntity(any(DocumentType.class))).thenReturn(entity);
        when(repository.save(any(DocumentTypeEntity.class))).thenReturn(entity);
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        // Act
        DocumentType result = documentTypeService.create(createReq);

        // Assert
        assertNotNull(result);
        verify(repository).save(any(DocumentTypeEntity.class));
    }

    @Test
    @DisplayName("create - Debe lanzar excepción cuando módulo ID es inválido")
    void testCreateThrowsExceptionWhenModuleIdInvalid() {
        // Arrange
        createReq.setModuleId(999);

        // Act & Assert
        assertThrows(InvalidModuleException.class, () -> documentTypeService.create(createReq));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create - Debe lanzar excepción cuando prefijo ya existe en empresa")
    void testCreateThrowsExceptionWhenPrefixAlreadyExists() {
        // Arrange
        when(repository.existsByPrefixAndIdEnterprise(anyString(), eq(ID_ENTERPRISE))).thenReturn(true);

        // Act & Assert
        assertThrows(DocumentTypesAlreadyExistsException.class, () -> documentTypeService.create(createReq));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create - Debe lanzar excepción cuando nombre ya existe en empresa")
    void testCreateThrowsExceptionWhenNameAlreadyExists() {
        // Arrange
        when(repository.existsByPrefixAndIdEnterprise(anyString(), eq(ID_ENTERPRISE))).thenReturn(false);
        when(repository.existsByNameAndIdEnterprise(anyString(), eq(ID_ENTERPRISE))).thenReturn(true);

        // Act & Assert
        assertThrows(DocumentTypesAlreadyExistsException.class, () -> documentTypeService.create(createReq));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create - Debe lanzar excepción cuando clase de documento no existe")
    void testCreateThrowsExceptionWhenDocumentClassNotFound() {
        // Arrange
        when(repository.existsByPrefixAndIdEnterprise(anyString(), eq(ID_ENTERPRISE))).thenReturn(false);
        when(repository.existsByNameAndIdEnterprise(anyString(), eq(ID_ENTERPRISE))).thenReturn(false);
        when(documentClassRepository.findByIdAndIdEnterpriseAndStatus(DOCUMENT_CLASS_ID, ID_ENTERPRISE, true))
                .thenReturn(Optional.empty());
        when(documentClassRepository.findByIdAndIdEnterprise(DOCUMENT_CLASS_ID, ID_ENTERPRISE))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(DocumentClassesNotFoundException.class, () -> documentTypeService.create(createReq));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create - Debe lanzar excepción cuando clase de documento está inactiva")
    void testCreateThrowsExceptionWhenDocumentClassInactive() {
        // Arrange
        DocumentClassEntity inactiveClass = DocumentClassEntity.builder()
                .id(DOCUMENT_CLASS_ID)
                .name("Clase inactiva")
                .status(false)
                .build();
        when(repository.existsByPrefixAndIdEnterprise(anyString(), eq(ID_ENTERPRISE))).thenReturn(false);
        when(repository.existsByNameAndIdEnterprise(anyString(), eq(ID_ENTERPRISE))).thenReturn(false);
        when(documentClassRepository.findByIdAndIdEnterpriseAndStatus(DOCUMENT_CLASS_ID, ID_ENTERPRISE, true))
                .thenReturn(Optional.empty());
        when(documentClassRepository.findByIdAndIdEnterprise(DOCUMENT_CLASS_ID, ID_ENTERPRISE))
                .thenReturn(Optional.of(inactiveClass));

        // Act & Assert
        assertThrows(DocumentClassInactiveException.class, () -> documentTypeService.create(createReq));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create - Debe estandarizar nombre y prefijo al crear")
    void testCreateStandardizesNameAndPrefix() {
        // Arrange
        createReq.setName("  FACTURA   de   VENTA  ");
        createReq.setPrefix("  fac  ");
        when(repository.existsByPrefixAndIdEnterprise(anyString(), eq(ID_ENTERPRISE))).thenReturn(false);
        when(repository.existsByNameAndIdEnterprise(anyString(), eq(ID_ENTERPRISE))).thenReturn(false);
        when(documentClassRepository.findByIdAndIdEnterpriseAndStatus(DOCUMENT_CLASS_ID, ID_ENTERPRISE, true))
                .thenReturn(Optional.of(documentClassEntity));
        when(domainMapper.toDomain(createReq)).thenReturn(domain);
        when(dataMapper.toEntity(any(DocumentType.class))).thenReturn(entity);
        when(repository.save(any(DocumentTypeEntity.class))).thenReturn(entity);
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        // Act
        documentTypeService.create(createReq);

        // Assert
        verify(repository).save(any(DocumentTypeEntity.class));
    }

    // ========== UPDATE TESTS ==========

    @Test
    @DisplayName("update - Debe actualizar tipo de documento exitosamente")
    void testUpdateSuccessfully() {
        // Arrange
        when(repository.findByIdAndIdEnterprise(ID, ID_ENTERPRISE)).thenReturn(Optional.of(entity));
        when(dataMapper.toDomain(entity)).thenReturn(domain);
        when(documentClassRepository.findByIdAndIdEnterpriseAndStatus(DOCUMENT_CLASS_ID, ID_ENTERPRISE, true))
                .thenReturn(Optional.of(documentClassEntity));
        when(repository.save(any(DocumentTypeEntity.class))).thenReturn(entity);

        // Act
        DocumentType result = documentTypeService.update(updateReq);

        // Assert
        assertNotNull(result);
        verify(repository).save(any(DocumentTypeEntity.class));
    }

    @Test
    @DisplayName("update - Debe lanzar excepción cuando módulo ID es inválido")
    void testUpdateThrowsExceptionWhenModuleIdInvalid() {
        // Arrange
        updateReq.setModuleId(999);

        // Act & Assert
        assertThrows(InvalidModuleException.class, () -> documentTypeService.update(updateReq));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update - Debe lanzar excepción cuando tipo de documento no existe")
    void testUpdateThrowsExceptionWhenNotFound() {
        // Arrange
        when(repository.findByIdAndIdEnterprise(ID, ID_ENTERPRISE)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(DocumentTypesNotFoundException.class, () -> documentTypeService.update(updateReq));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update - Debe lanzar excepción cuando tipo de documento está en uso")
    void testUpdateThrowsExceptionWhenInUse() {
        // Arrange
        DocumentType domainInUse = DocumentType.builder()
                .id(ID)
                .usageCount(5)
                .build();
        when(repository.findByIdAndIdEnterprise(ID, ID_ENTERPRISE)).thenReturn(Optional.of(entity));
        when(dataMapper.toDomain(entity)).thenReturn(domainInUse);

        // Act & Assert
        assertThrows(DocumentTypeInUseException.class, () -> documentTypeService.update(updateReq));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update - Debe lanzar excepción cuando prefijo ya existe en otra entidad")
    void testUpdateThrowsExceptionWhenPrefixExistsInOtherEntity() {
        // Arrange
        updateReq.setPrefix("NUEVO");
        when(repository.findByIdAndIdEnterprise(ID, ID_ENTERPRISE)).thenReturn(Optional.of(entity));
        when(dataMapper.toDomain(entity)).thenReturn(domain);
        when(repository.existsByPrefixAndIdEnterpriseAndIdNot(anyString(), eq(ID_ENTERPRISE), eq(ID))).thenReturn(true);

        // Act & Assert
        assertThrows(DocumentTypesAlreadyExistsException.class, () -> documentTypeService.update(updateReq));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update - Debe lanzar excepción cuando nombre ya existe en otra entidad")
    void testUpdateThrowsExceptionWhenNameExistsInOtherEntity() {
        // Arrange
        updateReq.setName("Nuevo nombre");
        when(repository.findByIdAndIdEnterprise(ID, ID_ENTERPRISE)).thenReturn(Optional.of(entity));
        when(dataMapper.toDomain(entity)).thenReturn(domain);
        when(repository.existsByPrefixAndIdEnterpriseAndIdNot(anyString(), eq(ID_ENTERPRISE), eq(ID))).thenReturn(false);
        when(repository.existsByNameAndIdEnterpriseAndIdNot(anyString(), eq(ID_ENTERPRISE), eq(ID))).thenReturn(true);

        // Act & Assert
        assertThrows(DocumentTypesAlreadyExistsException.class, () -> documentTypeService.update(updateReq));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update - Debe lanzar excepción cuando clase de documento no existe")
    void testUpdateThrowsExceptionWhenDocumentClassNotFound() {
        // Arrange
        when(repository.findByIdAndIdEnterprise(ID, ID_ENTERPRISE)).thenReturn(Optional.of(entity));
        when(dataMapper.toDomain(entity)).thenReturn(domain);
        when(documentClassRepository.findByIdAndIdEnterpriseAndStatus(DOCUMENT_CLASS_ID, ID_ENTERPRISE, true))
                .thenReturn(Optional.empty());
        when(documentClassRepository.findByIdAndIdEnterprise(DOCUMENT_CLASS_ID, ID_ENTERPRISE))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(DocumentClassesNotFoundException.class, () -> documentTypeService.update(updateReq));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update - Debe lanzar excepción cuando clase de documento está inactiva")
    void testUpdateThrowsExceptionWhenDocumentClassInactive() {
        // Arrange
        DocumentClassEntity inactiveClass = DocumentClassEntity.builder()
                .id(DOCUMENT_CLASS_ID)
                .name("Clase inactiva")
                .status(false)
                .build();
        when(repository.findByIdAndIdEnterprise(ID, ID_ENTERPRISE)).thenReturn(Optional.of(entity));
        when(dataMapper.toDomain(entity)).thenReturn(domain);
        when(documentClassRepository.findByIdAndIdEnterpriseAndStatus(DOCUMENT_CLASS_ID, ID_ENTERPRISE, true))
                .thenReturn(Optional.empty());
        when(documentClassRepository.findByIdAndIdEnterprise(DOCUMENT_CLASS_ID, ID_ENTERPRISE))
                .thenReturn(Optional.of(inactiveClass));

        // Act & Assert
        assertThrows(DocumentClassInactiveException.class, () -> documentTypeService.update(updateReq));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update - Debe validar unicidad cuando cambia la empresa")
    void testUpdateValidatesUniquenessWhenEnterpriseChanges() {
        // Arrange
        String newEnterprise = "ENT002";
        updateReq.setIdEnterprise(newEnterprise);
        when(repository.findByIdAndIdEnterprise(ID, newEnterprise)).thenReturn(Optional.of(entity));
        when(dataMapper.toDomain(entity)).thenReturn(domain);
        when(repository.existsByPrefixAndIdEnterpriseAndIdNot(anyString(), eq(newEnterprise), eq(ID))).thenReturn(true);

        // Act & Assert
        assertThrows(DocumentTypesAlreadyExistsException.class, () -> documentTypeService.update(updateReq));
    }

    // ========== FIND BY ID TESTS ==========

    @Test
    @DisplayName("findById - Debe retornar tipo de documento cuando existe")
    void testFindByIdReturnsDocumentType() {
        // Arrange
        when(repository.findByIdAndIdEnterprise(ID, ID_ENTERPRISE)).thenReturn(Optional.of(entity));
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        // Act
        DocumentType result = documentTypeService.findById(ID, ID_ENTERPRISE);

        // Assert
        assertNotNull(result);
        assertEquals(ID, result.getId());
    }

    @Test
    @DisplayName("findById - Debe lanzar excepción cuando no existe")
    void testFindByIdThrowsExceptionWhenNotFound() {
        // Arrange
        when(repository.findByIdAndIdEnterprise(ID, ID_ENTERPRISE)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(DocumentTypesNotFoundException.class, () -> documentTypeService.findById(ID, ID_ENTERPRISE));
    }

    // ========== FIND ALL BY ENTERPRISE TESTS ==========

    @Test
    @DisplayName("findAllByEnterprise - Debe retornar página de tipos de documento")
    void testFindAllByEnterpriseReturnsPage() {
        // Arrange
        int page = 0;
        int size = 10;
        Page<DocumentTypeEntity> entityPage = new PageImpl<>(List.of(entity));
        when(repository.findAllByIdEnterprise(eq(ID_ENTERPRISE), any(Pageable.class))).thenReturn(entityPage);
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        // Act
        Page<DocumentType> result = documentTypeService.findAllByEnterprise(ID_ENTERPRISE, page, size);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("findAllByEnterprise - Debe retornar página vacía cuando no hay tipos")
    void testFindAllByEnterpriseReturnsEmptyPage() {
        // Arrange
        int page = 0;
        int size = 10;
        Page<DocumentTypeEntity> emptyPage = Page.empty();
        when(repository.findAllByIdEnterprise(eq(ID_ENTERPRISE), any(Pageable.class))).thenReturn(emptyPage);

        // Act
        Page<DocumentType> result = documentTypeService.findAllByEnterprise(ID_ENTERPRISE, page, size);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========== FIND ALL BY ENTERPRISE WITH SORT TESTS ==========

    @Test
    @DisplayName("findAllByEnterprise con ordenamiento - Debe ordenar ascendente")
    void testFindAllByEnterpriseWithSortAscending() {
        // Arrange
        int page = 0;
        int size = 10;
        String sortField = "name";
        String sortOrder = "asc";
        Page<DocumentTypeEntity> entityPage = new PageImpl<>(List.of(entity));
        when(repository.findAllByIdEnterprise(eq(ID_ENTERPRISE), any(Pageable.class))).thenReturn(entityPage);
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        // Act
        Page<DocumentType> result = documentTypeService.findAllByEnterprise(ID_ENTERPRISE, page, size, sortField, sortOrder);

        // Assert
        assertNotNull(result);
        verify(repository).findAllByIdEnterprise(eq(ID_ENTERPRISE), any(Pageable.class));
    }

    @Test
    @DisplayName("findAllByEnterprise con ordenamiento - Debe ordenar descendente")
    void testFindAllByEnterpriseWithSortDescending() {
        // Arrange
        int page = 0;
        int size = 10;
        String sortField = "name";
        String sortOrder = "desc";
        Page<DocumentTypeEntity> entityPage = new PageImpl<>(List.of(entity));
        when(repository.findAllByIdEnterprise(eq(ID_ENTERPRISE), any(Pageable.class))).thenReturn(entityPage);
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        // Act
        Page<DocumentType> result = documentTypeService.findAllByEnterprise(ID_ENTERPRISE, page, size, sortField, sortOrder);

        // Assert
        assertNotNull(result);
        verify(repository).findAllByIdEnterprise(eq(ID_ENTERPRISE), any(Pageable.class));
    }

    // ========== FIND ALL BY MODULE AND ENTERPRISE TESTS ==========

    @Test
    @DisplayName("findAllByModuleAndEnterprise - Debe retornar lista de tipos de documento")
    void testFindAllByModuleAndEnterpriseReturnsList() {
        // Arrange
        when(repository.findAllByModuleAndIdEnterprise(anyString(), eq(ID_ENTERPRISE))).thenReturn(List.of(entity));
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        // Act
        List<DocumentType> result = documentTypeService.findAllByModuleAndEnterprise(MODULE_ID, ID_ENTERPRISE);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("findAllByModuleAndEnterprise - Debe lanzar excepción cuando módulo ID es inválido")
    void testFindAllByModuleAndEnterpriseThrowsExceptionWhenModuleInvalid() {
        // Arrange
        Integer invalidModuleId = 999;

        // Act & Assert
        assertThrows(InvalidModuleException.class, 
                () -> documentTypeService.findAllByModuleAndEnterprise(invalidModuleId, ID_ENTERPRISE));
    }

    @Test
    @DisplayName("findAllByModuleAndEnterprise - Debe retornar lista vacía cuando no hay tipos")
    void testFindAllByModuleAndEnterpriseReturnsEmptyList() {
        // Arrange
        when(repository.findAllByModuleAndIdEnterprise(anyString(), eq(ID_ENTERPRISE))).thenReturn(List.of());

        // Act
        List<DocumentType> result = documentTypeService.findAllByModuleAndEnterprise(MODULE_ID, ID_ENTERPRISE);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========== CHANGE STATE TESTS ==========

    @Test
    @DisplayName("changeState - Debe activar tipo de documento")
    void testChangeStateActivate() {
        // Arrange
        entity.setStatus(false);
        when(repository.findByIdAndIdEnterprise(ID, ID_ENTERPRISE)).thenReturn(Optional.of(entity));
        when(repository.save(any(DocumentTypeEntity.class))).thenReturn(entity);
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        // Act
        DocumentType result = documentTypeService.changeState(ID, ID_ENTERPRISE, true);

        // Assert
        assertNotNull(result);
        verify(repository).save(entity);
    }

    @Test
    @DisplayName("changeState - Debe desactivar tipo de documento")
    void testChangeStateDeactivate() {
        // Arrange
        when(repository.findByIdAndIdEnterprise(ID, ID_ENTERPRISE)).thenReturn(Optional.of(entity));
        when(repository.save(any(DocumentTypeEntity.class))).thenReturn(entity);
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        // Act
        DocumentType result = documentTypeService.changeState(ID, ID_ENTERPRISE, false);

        // Assert
        assertNotNull(result);
        verify(repository).save(entity);
    }

    @Test
    @DisplayName("changeState - Debe lanzar excepción cuando no existe")
    void testChangeStateThrowsExceptionWhenNotFound() {
        // Arrange
        when(repository.findByIdAndIdEnterprise(ID, ID_ENTERPRISE)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(DocumentTypesNotFoundException.class, 
                () -> documentTypeService.changeState(ID, ID_ENTERPRISE, true));
        verify(repository, never()).save(any());
    }

    // ========== DELETE TESTS ==========

    @Test
    @DisplayName("delete - Debe eliminar tipo de documento exitosamente")
    void testDeleteSuccessfully() {
        // Arrange
        when(repository.findByIdAndIdEnterprise(ID, ID_ENTERPRISE)).thenReturn(Optional.of(entity));
        when(dataMapper.toDomain(entity)).thenReturn(domain);
        doNothing().when(repository).delete(entity);

        // Act
        DocumentType result = documentTypeService.Delete(ID, ID_ENTERPRISE);

        // Assert
        assertNotNull(result);
        verify(repository).delete(entity);
    }

    @Test
    @DisplayName("delete - Debe lanzar excepción cuando no existe")
    void testDeleteThrowsExceptionWhenNotFound() {
        // Arrange
        when(repository.findByIdAndIdEnterprise(ID, ID_ENTERPRISE)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(DocumentTypesNotFoundException.class, 
                () -> documentTypeService.Delete(ID, ID_ENTERPRISE));
        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("delete - Debe lanzar excepción cuando tipo de documento está en uso")
    void testDeleteThrowsExceptionWhenInUse() {
        // Arrange
        DocumentType domainInUse = DocumentType.builder()
                .id(ID)
                .usageCount(5)
                .build();
        when(repository.findByIdAndIdEnterprise(ID, ID_ENTERPRISE)).thenReturn(Optional.of(entity));
        when(dataMapper.toDomain(entity)).thenReturn(domainInUse);

        // Act & Assert
        assertThrows(DocumentTypeInUseException.class, 
                () -> documentTypeService.Delete(ID, ID_ENTERPRISE));
        verify(repository, never()).delete(any());
    }

    // ========== GET ALL MODULES TESTS ==========

    @Test
    @DisplayName("getAllModules - Debe retornar todos los módulos")
    void testGetAllModulesReturnsAllModules() {
        // Arrange & Act
        List<DocumentModule> result = documentTypeService.getAllModules();

        // Assert
        assertNotNull(result);
        assertEquals(8, result.size());
    }

    @Test
    @DisplayName("getAllModules - Debe contener módulo COMERCIAL")
    void testGetAllModulesContainsComercial() {
        // Arrange & Act
        List<DocumentModule> result = documentTypeService.getAllModules();

        // Assert
        assertTrue(result.contains(DocumentModule.COMERCIAL));
    }

    // ========== COUNT ALL BY ENTERPRISE TESTS ==========

    @Test
    @DisplayName("countAllByEnterprise - Debe retornar cantidad correcta")
    void testCountAllByEnterpriseReturnsCount() {
        // Arrange
        long expectedCount = 5L;
        when(repository.countByIdEnterprise(ID_ENTERPRISE)).thenReturn(expectedCount);

        // Act
        long result = documentTypeService.countAllByEnterprise(ID_ENTERPRISE);

        // Assert
        assertEquals(expectedCount, result);
    }

    @Test
    @DisplayName("countAllByEnterprise - Debe retornar cero cuando no hay tipos")
    void testCountAllByEnterpriseReturnsZero() {
        // Arrange
        when(repository.countByIdEnterprise(ID_ENTERPRISE)).thenReturn(0L);

        // Act
        long result = documentTypeService.countAllByEnterprise(ID_ENTERPRISE);

        // Assert
        assertEquals(0L, result);
    }

    // ========== FIND BY ENTERPRISE AND NAME CONTAINING TESTS ==========

    @Test
    @DisplayName("findByEnterpriseAndNameContaining - Debe retornar tipos que coinciden con búsqueda")
    void testFindByEnterpriseAndNameContainingReturnsMatches() {
        // Arrange
        String search = "Factura";
        int page = 0;
        int size = 10;
        String sortField = "name";
        String sortOrder = "asc";
        Page<DocumentTypeEntity> entityPage = new PageImpl<>(List.of(entity));
        when(repository.findByIdEnterpriseAndNameContainingIgnoreCase(eq(ID_ENTERPRISE), eq(search), any(Pageable.class)))
                .thenReturn(entityPage);
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        // Act
        Page<DocumentType> result = documentTypeService.findByEnterpriseAndNameContaining(
                ID_ENTERPRISE, search, page, size, sortField, sortOrder);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("findByEnterpriseAndNameContaining - Debe ordenar descendente")
    void testFindByEnterpriseAndNameContainingWithDescSort() {
        // Arrange
        String search = "Factura";
        int page = 0;
        int size = 10;
        String sortField = "name";
        String sortOrder = "desc";
        Page<DocumentTypeEntity> entityPage = new PageImpl<>(List.of(entity));
        when(repository.findByIdEnterpriseAndNameContainingIgnoreCase(eq(ID_ENTERPRISE), eq(search), any(Pageable.class)))
                .thenReturn(entityPage);
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        // Act
        Page<DocumentType> result = documentTypeService.findByEnterpriseAndNameContaining(
                ID_ENTERPRISE, search, page, size, sortField, sortOrder);

        // Assert
        assertNotNull(result);
        verify(repository).findByIdEnterpriseAndNameContainingIgnoreCase(eq(ID_ENTERPRISE), eq(search), any(Pageable.class));
    }

    @Test
    @DisplayName("findByEnterpriseAndNameContaining - Debe retornar página vacía cuando no hay coincidencias")
    void testFindByEnterpriseAndNameContainingReturnsEmptyPage() {
        // Arrange
        String search = "NoExiste";
        int page = 0;
        int size = 10;
        String sortField = "name";
        String sortOrder = "asc";
        Page<DocumentTypeEntity> emptyPage = Page.empty();
        when(repository.findByIdEnterpriseAndNameContainingIgnoreCase(eq(ID_ENTERPRISE), eq(search), any(Pageable.class)))
                .thenReturn(emptyPage);

        // Act
        Page<DocumentType> result = documentTypeService.findByEnterpriseAndNameContaining(
                ID_ENTERPRISE, search, page, size, sortField, sortOrder);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========== COUNT BY ENTERPRISE AND NAME CONTAINING TESTS ==========

    @Test
    @DisplayName("countByEnterpriseAndNameContaining - Debe retornar cantidad correcta")
    void testCountByEnterpriseAndNameContainingReturnsCount() {
        // Arrange
        String search = "Factura";
        long expectedCount = 3L;
        when(repository.countByIdEnterpriseAndNameContainingIgnoreCase(ID_ENTERPRISE, search)).thenReturn(expectedCount);

        // Act
        long result = documentTypeService.countByEnterpriseAndNameContaining(ID_ENTERPRISE, search);

        // Assert
        assertEquals(expectedCount, result);
    }

    @Test
    @DisplayName("countByEnterpriseAndNameContaining - Debe retornar cero cuando no hay coincidencias")
    void testCountByEnterpriseAndNameContainingReturnsZero() {
        // Arrange
        String search = "NoExiste";
        when(repository.countByIdEnterpriseAndNameContainingIgnoreCase(ID_ENTERPRISE, search)).thenReturn(0L);

        // Act
        long result = documentTypeService.countByEnterpriseAndNameContaining(ID_ENTERPRISE, search);

        // Assert
        assertEquals(0L, result);
    }

    // ========== UPDATE USAGE COUNT TESTS ==========

    @Test
    @DisplayName("updateUsageCount - Debe actualizar contador de uso exitosamente")
    void testUpdateUsageCountSuccessfully() {
        // Arrange
        Integer newUsageCount = 10;
        when(repository.findByIdAndIdEnterprise(ID, ID_ENTERPRISE)).thenReturn(Optional.of(entity));
        when(repository.save(any(DocumentTypeEntity.class))).thenReturn(entity);

        // Act
        documentTypeService.updateUsageCount(ID, ID_ENTERPRISE, newUsageCount);

        // Assert
        verify(repository).save(entity);
        assertEquals(newUsageCount, entity.getUsageCount());
    }

    @Test
    @DisplayName("updateUsageCount - Debe lanzar excepción cuando tipo de documento no existe")
    void testUpdateUsageCountThrowsExceptionWhenNotFound() {
        // Arrange
        Integer newUsageCount = 10;
        when(repository.findByIdAndIdEnterprise(ID, ID_ENTERPRISE)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, 
                () -> documentTypeService.updateUsageCount(ID, ID_ENTERPRISE, newUsageCount));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("updateUsageCount - Debe actualizar contador a cero")
    void testUpdateUsageCountToZero() {
        // Arrange
        entity.setUsageCount(5);
        when(repository.findByIdAndIdEnterprise(ID, ID_ENTERPRISE)).thenReturn(Optional.of(entity));
        when(repository.save(any(DocumentTypeEntity.class))).thenReturn(entity);

        // Act
        documentTypeService.updateUsageCount(ID, ID_ENTERPRISE, 0);

        // Assert
        verify(repository).save(entity);
        assertEquals(0, entity.getUsageCount());
    }
}

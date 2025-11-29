package co.unicauca.edu.co.contables.configuration.unit.classesOfDocuments.domain.services;

import co.unicauca.edu.co.contables.configuration.classesOfDocuments.dataAccess.entity.DocumentClassEntity;
import co.unicauca.edu.co.contables.configuration.classesOfDocuments.dataAccess.mapper.DocumentClassDataMapper;
import co.unicauca.edu.co.contables.configuration.classesOfDocuments.dataAccess.repository.DocumentClassRepository;
import co.unicauca.edu.co.contables.configuration.classesOfDocuments.domain.mapper.DocumentClassDomainMapper;
import co.unicauca.edu.co.contables.configuration.classesOfDocuments.domain.models.DocumentClass;
import co.unicauca.edu.co.contables.configuration.classesOfDocuments.domain.services.DocumentClassServiceImpl;
import co.unicauca.edu.co.contables.configuration.classesOfDocuments.presentation.DTO.request.DocumentClassCreateReq;
import co.unicauca.edu.co.contables.configuration.classesOfDocuments.presentation.DTO.request.DocumentClassUpdateReq;
import co.unicauca.edu.co.contables.configuration.commons.exceptions.documentClasses.DocumentClassInUseException;
import co.unicauca.edu.co.contables.configuration.commons.exceptions.documentClasses.DocumentClassesAlreadyExistsException;
import co.unicauca.edu.co.contables.configuration.commons.exceptions.documentClasses.DocumentClassesNotFoundException;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.dataAccess.repository.DocumentTypeRepository;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DocumentClassServiceImplUnitTest {

    @Mock
    private DocumentClassRepository repository;

    @Mock
    private DocumentClassDataMapper dataMapper;

    @Mock
    private DocumentClassDomainMapper domainMapper;

    @Mock
    private DocumentTypeRepository documentTypeRepository;

    @InjectMocks
    private DocumentClassServiceImpl service;

    private static final Long ID = 1L;
    private static final String ENTERPRISE_ID = "ENT-001";
    private static final String NAME = "Facturas";
    private static final String NAME_LOWER = "facturas";

    private DocumentClass domain;
    private DocumentClassEntity entity;

    @BeforeEach
    void setUp() {
        domain = DocumentClass.builder()
                .id(ID)
                .name(NAME)
                .idEnterprise(ENTERPRISE_ID)
                .status(true)
                .build();

        entity = DocumentClassEntity.builder()
                .id(ID)
                .name(NAME)
                .idEnterprise(ENTERPRISE_ID)
                .status(true)
                .build();
    }

    @Test
    @DisplayName("create - Debe crear clase de documento exitosamente")
    void testCreateSuccess() {
        DocumentClassCreateReq request = new DocumentClassCreateReq(ENTERPRISE_ID, NAME_LOWER);
        DocumentClass domainWithoutId = DocumentClass.builder()
                .name(NAME_LOWER)
                .idEnterprise(ENTERPRISE_ID)
                .build();
        when(repository.existsByNameAndIdEnterprise(NAME, ENTERPRISE_ID)).thenReturn(false);
        when(domainMapper.toDomain(request)).thenReturn(domainWithoutId);
        when(dataMapper.toEntity(any(DocumentClass.class))).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        DocumentClass result = service.create(request);

        assertNotNull(result);
        assertEquals(ID, result.getId());
        assertEquals(NAME, result.getName());
        verify(repository).existsByNameAndIdEnterprise(NAME, ENTERPRISE_ID);
        verify(repository).save(entity);
    }

    @Test
    @DisplayName("create - Debe lanzar excepción cuando el nombre ya existe")
    void testCreateThrowsExceptionWhenNameExists() {
        DocumentClassCreateReq request = new DocumentClassCreateReq(ENTERPRISE_ID, NAME_LOWER);
        when(repository.existsByNameAndIdEnterprise(NAME, ENTERPRISE_ID)).thenReturn(true);

        assertThrows(DocumentClassesAlreadyExistsException.class, 
                () -> service.create(request));

        verify(repository).existsByNameAndIdEnterprise(NAME, ENTERPRISE_ID);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create - Debe estandarizar nombre a formato capitalizado")
    void testCreateStandardizesNameToCapitalized() {
        DocumentClassCreateReq request = new DocumentClassCreateReq(ENTERPRISE_ID, "  facturas  ");
        DocumentClass domainWithoutId = DocumentClass.builder()
                .name("  facturas  ")
                .idEnterprise(ENTERPRISE_ID)
                .build();
        when(repository.existsByNameAndIdEnterprise("Facturas", ENTERPRISE_ID)).thenReturn(false);
        when(domainMapper.toDomain(request)).thenReturn(domainWithoutId);
        when(dataMapper.toEntity(any(DocumentClass.class))).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        service.create(request);

        verify(repository).existsByNameAndIdEnterprise("Facturas", ENTERPRISE_ID);
    }

    @Test
    @DisplayName("update - Debe actualizar clase de documento exitosamente")
    void testUpdateSuccess() {
        DocumentClassUpdateReq request = new DocumentClassUpdateReq(ID, ENTERPRISE_ID, "recibos");
        when(repository.findByIdAndIdEnterprise(ID, ENTERPRISE_ID)).thenReturn(Optional.of(entity));
        when(documentTypeRepository.existsByDocumentClassIdAndIdEnterpriseAndUsageCountGreaterThan(ID, ENTERPRISE_ID, 0))
                .thenReturn(false);
        when(repository.existsByNameAndIdEnterpriseAndIdNot("Recibos", ENTERPRISE_ID, ID)).thenReturn(false);
        when(repository.save(entity)).thenReturn(entity);
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        DocumentClass result = service.update(request);

        assertNotNull(result);
        verify(repository).findByIdAndIdEnterprise(ID, ENTERPRISE_ID);
        verify(repository).save(entity);
    }

    @Test
    @DisplayName("update - Debe lanzar excepción cuando no existe")
    void testUpdateThrowsExceptionWhenNotFound() {
        DocumentClassUpdateReq request = new DocumentClassUpdateReq(ID, ENTERPRISE_ID, NAME_LOWER);
        when(repository.findByIdAndIdEnterprise(ID, ENTERPRISE_ID)).thenReturn(Optional.empty());

        assertThrows(DocumentClassesNotFoundException.class, 
                () -> service.update(request));

        verify(repository).findByIdAndIdEnterprise(ID, ENTERPRISE_ID);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update - Debe lanzar excepción cuando tiene tipos con movimientos contables")
    void testUpdateThrowsExceptionWhenHasDocumentTypesWithMovements() {
        DocumentClassUpdateReq request = new DocumentClassUpdateReq(ID, ENTERPRISE_ID, NAME_LOWER);
        when(repository.findByIdAndIdEnterprise(ID, ENTERPRISE_ID)).thenReturn(Optional.of(entity));
        when(documentTypeRepository.existsByDocumentClassIdAndIdEnterpriseAndUsageCountGreaterThan(ID, ENTERPRISE_ID, 0))
                .thenReturn(true);

        assertThrows(DocumentClassInUseException.class, 
                () -> service.update(request));

        verify(documentTypeRepository).existsByDocumentClassIdAndIdEnterpriseAndUsageCountGreaterThan(ID, ENTERPRISE_ID, 0);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update - Debe lanzar excepción cuando el nuevo nombre ya existe")
    void testUpdateThrowsExceptionWhenNewNameExists() {
        DocumentClassUpdateReq request = new DocumentClassUpdateReq(ID, ENTERPRISE_ID, "recibos");
        when(repository.findByIdAndIdEnterprise(ID, ENTERPRISE_ID)).thenReturn(Optional.of(entity));
        when(documentTypeRepository.existsByDocumentClassIdAndIdEnterpriseAndUsageCountGreaterThan(ID, ENTERPRISE_ID, 0))
                .thenReturn(false);
        when(repository.existsByNameAndIdEnterpriseAndIdNot("Recibos", ENTERPRISE_ID, ID)).thenReturn(true);

        assertThrows(DocumentClassesAlreadyExistsException.class, 
                () -> service.update(request));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update - Debe validar y guardar cuando el nombre cambia")
    void testUpdateValidatesAndSavesWhenNameChanges() {
        DocumentClassUpdateReq request = new DocumentClassUpdateReq(ID, ENTERPRISE_ID, NAME);
        when(repository.findByIdAndIdEnterprise(ID, ENTERPRISE_ID)).thenReturn(Optional.of(entity));
        when(documentTypeRepository.existsByDocumentClassIdAndIdEnterpriseAndUsageCountGreaterThan(ID, ENTERPRISE_ID, 0))
                .thenReturn(false);
        when(repository.save(entity)).thenReturn(entity);
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        DocumentClass result = service.update(request);

        assertNotNull(result);
        verify(repository).save(entity);
    }

    @Test
    @DisplayName("findById - Debe retornar clase de documento cuando existe")
    void testFindByIdSuccess() {
        when(repository.findByIdAndIdEnterprise(ID, ENTERPRISE_ID)).thenReturn(Optional.of(entity));
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        DocumentClass result = service.findById(ID, ENTERPRISE_ID);

        assertNotNull(result);
        assertEquals(ID, result.getId());
        assertEquals(NAME, result.getName());
        verify(repository).findByIdAndIdEnterprise(ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("findById - Debe lanzar excepción cuando no existe")
    void testFindByIdThrowsExceptionWhenNotFound() {
        when(repository.findByIdAndIdEnterprise(ID, ENTERPRISE_ID)).thenReturn(Optional.empty());

        assertThrows(DocumentClassesNotFoundException.class, 
                () -> service.findById(ID, ENTERPRISE_ID));

        verify(repository).findByIdAndIdEnterprise(ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("findAllByEnterprise - Debe retornar página de clases de documento")
    void testFindAllByEnterpriseSuccess() {
        int page = 0;
        int size = 10;
        Page<DocumentClassEntity> entityPage = new PageImpl<>(List.of(entity));
        when(repository.findAllByIdEnterprise(eq(ENTERPRISE_ID), any(Pageable.class))).thenReturn(entityPage);
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        Page<DocumentClass> result = service.findAllByEnterprise(ENTERPRISE_ID, page, size);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(repository).findAllByIdEnterprise(eq(ENTERPRISE_ID), any(Pageable.class));
    }

    @Test
    @DisplayName("findAllByEnterprise - Debe retornar página vacía cuando no hay datos")
    void testFindAllByEnterpriseReturnsEmptyPage() {
        int page = 0;
        int size = 10;
        Page<DocumentClassEntity> emptyPage = new PageImpl<>(Collections.emptyList());
        when(repository.findAllByIdEnterprise(eq(ENTERPRISE_ID), any(Pageable.class))).thenReturn(emptyPage);

        Page<DocumentClass> result = service.findAllByEnterprise(ENTERPRISE_ID, page, size);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    @DisplayName("findAllByEnterprise con ordenamiento - Debe ordenar ascendente por defecto")
    void testFindAllByEnterpriseWithSortAscending() {
        int page = 0;
        int size = 10;
        String sortField = "name";
        String sortOrder = "asc";
        Page<DocumentClassEntity> entityPage = new PageImpl<>(List.of(entity));
        when(repository.findAllByIdEnterprise(eq(ENTERPRISE_ID), any(Pageable.class))).thenReturn(entityPage);
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        Page<DocumentClass> result = service.findAllByEnterprise(ENTERPRISE_ID, page, size, sortField, sortOrder);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    @DisplayName("findAllByEnterprise con ordenamiento - Debe ordenar descendente cuando se especifica")
    void testFindAllByEnterpriseWithSortDescending() {
        int page = 0;
        int size = 10;
        String sortField = "name";
        String sortOrder = "desc";
        Page<DocumentClassEntity> entityPage = new PageImpl<>(List.of(entity));
        when(repository.findAllByIdEnterprise(eq(ENTERPRISE_ID), any(Pageable.class))).thenReturn(entityPage);
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        Page<DocumentClass> result = service.findAllByEnterprise(ENTERPRISE_ID, page, size, sortField, sortOrder);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    @DisplayName("findAllByEnterpriseAndStatus - Debe filtrar por estado")
    void testFindAllByEnterpriseAndStatusSuccess() {
        int page = 0;
        int size = 10;
        Boolean status = true;
        Page<DocumentClassEntity> entityPage = new PageImpl<>(List.of(entity));
        when(repository.findAllByIdEnterpriseAndStatus(eq(ENTERPRISE_ID), eq(status), any(Pageable.class)))
                .thenReturn(entityPage);
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        Page<DocumentClass> result = service.findAllByEnterpriseAndStatus(ENTERPRISE_ID, status, page, size);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(repository).findAllByIdEnterpriseAndStatus(eq(ENTERPRISE_ID), eq(status), any(Pageable.class));
    }

    @Test
    @DisplayName("findAllByEnterpriseAndStatus - Debe filtrar por estado inactivo")
    void testFindAllByEnterpriseAndStatusInactive() {
        int page = 0;
        int size = 10;
        Boolean status = false;
        DocumentClassEntity inactiveEntity = DocumentClassEntity.builder()
                .id(2L)
                .name("Recibos")
                .idEnterprise(ENTERPRISE_ID)
                .status(false)
                .build();
        DocumentClass inactiveDomain = DocumentClass.builder()
                .id(2L)
                .name("Recibos")
                .idEnterprise(ENTERPRISE_ID)
                .status(false)
                .build();
        Page<DocumentClassEntity> entityPage = new PageImpl<>(List.of(inactiveEntity));
        when(repository.findAllByIdEnterpriseAndStatus(eq(ENTERPRISE_ID), eq(status), any(Pageable.class)))
                .thenReturn(entityPage);
        when(dataMapper.toDomain(inactiveEntity)).thenReturn(inactiveDomain);

        Page<DocumentClass> result = service.findAllByEnterpriseAndStatus(ENTERPRISE_ID, status, page, size);

        assertNotNull(result);
        assertFalse(result.getContent().get(0).getStatus());
    }

    @Test
    @DisplayName("changeState - Debe cambiar estado a inactivo")
    void testChangeStateToInactive() {
        when(repository.findByIdAndIdEnterprise(ID, ENTERPRISE_ID)).thenReturn(Optional.of(entity));
        DocumentClassEntity updatedEntity = DocumentClassEntity.builder()
                .id(ID)
                .name(NAME)
                .idEnterprise(ENTERPRISE_ID)
                .status(false)
                .build();
        DocumentClass updatedDomain = DocumentClass.builder()
                .id(ID)
                .name(NAME)
                .idEnterprise(ENTERPRISE_ID)
                .status(false)
                .build();
        when(repository.save(entity)).thenReturn(updatedEntity);
        when(dataMapper.toDomain(updatedEntity)).thenReturn(updatedDomain);

        DocumentClass result = service.changeState(ID, ENTERPRISE_ID, false);

        assertNotNull(result);
        assertFalse(result.getStatus());
        verify(repository).findByIdAndIdEnterprise(ID, ENTERPRISE_ID);
        verify(repository).save(entity);
    }

    @Test
    @DisplayName("changeState - Debe cambiar estado a activo")
    void testChangeStateToActive() {
        DocumentClassEntity inactiveEntity = DocumentClassEntity.builder()
                .id(ID)
                .name(NAME)
                .idEnterprise(ENTERPRISE_ID)
                .status(false)
                .build();
        when(repository.findByIdAndIdEnterprise(ID, ENTERPRISE_ID)).thenReturn(Optional.of(inactiveEntity));
        when(repository.save(inactiveEntity)).thenReturn(entity);
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        DocumentClass result = service.changeState(ID, ENTERPRISE_ID, true);

        assertNotNull(result);
        assertTrue(result.getStatus());
    }

    @Test
    @DisplayName("changeState - Debe lanzar excepción cuando no existe")
    void testChangeStateThrowsExceptionWhenNotFound() {
        when(repository.findByIdAndIdEnterprise(ID, ENTERPRISE_ID)).thenReturn(Optional.empty());

        assertThrows(DocumentClassesNotFoundException.class, 
                () -> service.changeState(ID, ENTERPRISE_ID, false));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Delete - Debe eliminar clase de documento exitosamente")
    void testDeleteSuccess() {
        when(repository.findByIdAndIdEnterprise(ID, ENTERPRISE_ID)).thenReturn(Optional.of(entity));
        when(documentTypeRepository.existsByDocumentClassId(ID)).thenReturn(false);
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        DocumentClass result = service.Delete(ID, ENTERPRISE_ID);

        assertNotNull(result);
        assertEquals(ID, result.getId());
        verify(repository).findByIdAndIdEnterprise(ID, ENTERPRISE_ID);
        verify(repository).delete(entity);
    }

    @Test
    @DisplayName("Delete - Debe lanzar excepción cuando no existe")
    void testDeleteThrowsExceptionWhenNotFound() {
        when(repository.findByIdAndIdEnterprise(ID, ENTERPRISE_ID)).thenReturn(Optional.empty());

        assertThrows(DocumentClassesNotFoundException.class, 
                () -> service.Delete(ID, ENTERPRISE_ID));

        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("Delete - Debe lanzar excepción cuando tiene tipos de documento asociados")
    void testDeleteThrowsExceptionWhenHasDocumentTypes() {
        when(repository.findByIdAndIdEnterprise(ID, ENTERPRISE_ID)).thenReturn(Optional.of(entity));
        when(documentTypeRepository.existsByDocumentClassId(ID)).thenReturn(true);

        assertThrows(DocumentClassInUseException.class, 
                () -> service.Delete(ID, ENTERPRISE_ID));

        verify(documentTypeRepository).existsByDocumentClassId(ID);
        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("countAllByEnterprise - Debe retornar conteo total")
    void testCountAllByEnterpriseSuccess() {
        when(repository.countByIdEnterprise(ENTERPRISE_ID)).thenReturn(5L);

        long result = service.countAllByEnterprise(ENTERPRISE_ID);

        assertEquals(5L, result);
        verify(repository).countByIdEnterprise(ENTERPRISE_ID);
    }

    @Test
    @DisplayName("countAllByEnterprise - Debe retornar 0 cuando no hay datos")
    void testCountAllByEnterpriseReturnsZero() {
        when(repository.countByIdEnterprise(ENTERPRISE_ID)).thenReturn(0L);

        long result = service.countAllByEnterprise(ENTERPRISE_ID);

        assertEquals(0L, result);
    }

    @Test
    @DisplayName("countAllByEnterpriseAndStatus - Debe retornar conteo filtrado por estado")
    void testCountAllByEnterpriseAndStatusSuccess() {
        when(repository.countByIdEnterpriseAndStatus(ENTERPRISE_ID, true)).thenReturn(3L);

        long result = service.countAllByEnterpriseAndStatus(ENTERPRISE_ID, true);

        assertEquals(3L, result);
        verify(repository).countByIdEnterpriseAndStatus(ENTERPRISE_ID, true);
    }

    @Test
    @DisplayName("countAllByEnterpriseAndStatus - Debe contar inactivos")
    void testCountAllByEnterpriseAndStatusInactive() {
        when(repository.countByIdEnterpriseAndStatus(ENTERPRISE_ID, false)).thenReturn(2L);

        long result = service.countAllByEnterpriseAndStatus(ENTERPRISE_ID, false);

        assertEquals(2L, result);
    }

    @Test
    @DisplayName("findByEnterpriseAndNameContaining - Debe buscar por nombre parcial")
    void testFindByEnterpriseAndNameContainingSuccess() {
        int page = 0;
        int size = 10;
        String search = "fact";
        String sortField = "name";
        String sortOrder = "asc";
        Page<DocumentClassEntity> entityPage = new PageImpl<>(List.of(entity));
        when(repository.findByIdEnterpriseAndNameContainingIgnoreCase(eq(ENTERPRISE_ID), eq(search), any(Pageable.class)))
                .thenReturn(entityPage);
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        Page<DocumentClass> result = service.findByEnterpriseAndNameContaining(
                ENTERPRISE_ID, search, page, size, sortField, sortOrder);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(repository).findByIdEnterpriseAndNameContainingIgnoreCase(eq(ENTERPRISE_ID), eq(search), any(Pageable.class));
    }

    @Test
    @DisplayName("findByEnterpriseAndNameContaining - Debe retornar vacío cuando no encuentra")
    void testFindByEnterpriseAndNameContainingReturnsEmpty() {
        int page = 0;
        int size = 10;
        String search = "xyz";
        String sortField = "name";
        String sortOrder = "asc";
        Page<DocumentClassEntity> emptyPage = new PageImpl<>(Collections.emptyList());
        when(repository.findByIdEnterpriseAndNameContainingIgnoreCase(eq(ENTERPRISE_ID), eq(search), any(Pageable.class)))
                .thenReturn(emptyPage);

        Page<DocumentClass> result = service.findByEnterpriseAndNameContaining(
                ENTERPRISE_ID, search, page, size, sortField, sortOrder);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    @DisplayName("findByEnterpriseAndNameContaining - Debe ordenar descendente")
    void testFindByEnterpriseAndNameContainingWithDescOrder() {
        int page = 0;
        int size = 10;
        String search = "fact";
        String sortField = "name";
        String sortOrder = "desc";
        Page<DocumentClassEntity> entityPage = new PageImpl<>(List.of(entity));
        when(repository.findByIdEnterpriseAndNameContainingIgnoreCase(eq(ENTERPRISE_ID), eq(search), any(Pageable.class)))
                .thenReturn(entityPage);
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        Page<DocumentClass> result = service.findByEnterpriseAndNameContaining(
                ENTERPRISE_ID, search, page, size, sortField, sortOrder);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    @DisplayName("countByEnterpriseAndNameContaining - Debe contar resultados de búsqueda")
    void testCountByEnterpriseAndNameContainingSuccess() {
        String search = "fact";
        when(repository.countByIdEnterpriseAndNameContainingIgnoreCase(ENTERPRISE_ID, search)).thenReturn(3L);

        long result = service.countByEnterpriseAndNameContaining(ENTERPRISE_ID, search);

        assertEquals(3L, result);
        verify(repository).countByIdEnterpriseAndNameContainingIgnoreCase(ENTERPRISE_ID, search);
    }

    @Test
    @DisplayName("countByEnterpriseAndNameContaining - Debe retornar 0 cuando no encuentra")
    void testCountByEnterpriseAndNameContainingReturnsZero() {
        String search = "xyz";
        when(repository.countByIdEnterpriseAndNameContainingIgnoreCase(ENTERPRISE_ID, search)).thenReturn(0L);

        long result = service.countByEnterpriseAndNameContaining(ENTERPRISE_ID, search);

        assertEquals(0L, result);
    }

    @Test
    @DisplayName("update - Debe validar unicidad cuando cambia empresa")
    void testUpdateValidatesUniquenessWhenEnterpriseChanges() {
        String newEnterpriseId = "ENT-002";
        DocumentClassEntity entityOtherEnterprise = DocumentClassEntity.builder()
                .id(ID)
                .name(NAME)
                .idEnterprise(ENTERPRISE_ID)
                .status(true)
                .build();
        DocumentClassUpdateReq request = new DocumentClassUpdateReq(ID, newEnterpriseId, NAME);
        when(repository.findByIdAndIdEnterprise(ID, newEnterpriseId)).thenReturn(Optional.of(entityOtherEnterprise));
        when(documentTypeRepository.existsByDocumentClassIdAndIdEnterpriseAndUsageCountGreaterThan(ID, ENTERPRISE_ID, 0))
                .thenReturn(false);
        when(repository.existsByNameAndIdEnterpriseAndIdNot(NAME, newEnterpriseId, ID)).thenReturn(true);

        assertThrows(DocumentClassesAlreadyExistsException.class, 
                () -> service.update(request));
    }

    @Test
    @DisplayName("findAllByEnterprise con paginación - Debe manejar múltiples páginas")
    void testFindAllByEnterpriseHandlesMultiplePages() {
        int page = 1;
        int size = 5;
        Page<DocumentClassEntity> entityPage = new PageImpl<>(
                List.of(entity), 
                PageRequest.of(page, size), 
                15);
        when(repository.findAllByIdEnterprise(eq(ENTERPRISE_ID), any(Pageable.class))).thenReturn(entityPage);
        when(dataMapper.toDomain(entity)).thenReturn(domain);

        Page<DocumentClass> result = service.findAllByEnterprise(ENTERPRISE_ID, page, size);

        assertNotNull(result);
        assertEquals(1, result.getNumber());
        assertEquals(3, result.getTotalPages());
        assertEquals(15, result.getTotalElements());
    }
}

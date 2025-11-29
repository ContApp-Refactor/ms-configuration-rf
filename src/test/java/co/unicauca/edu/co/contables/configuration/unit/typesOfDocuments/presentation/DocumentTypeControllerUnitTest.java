package co.unicauca.edu.co.contables.configuration.unit.typesOfDocuments.presentation;

import co.unicauca.edu.co.contables.configuration.commons.utils.PaginationHelper;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.enums.DocumentModule;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.mapper.DocumentTypeDomainMapper;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.models.DocumentType;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.services.IDocumentTypeService;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.presentation.DTO.request.DocumentTypeCreateReq;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.presentation.DTO.request.DocumentTypeUpdateReq;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.presentation.DTO.response.DocumentModuleRes;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.presentation.DTO.response.DocumentTypeRes;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.presentation.controller.DocumentTypeController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DocumentTypeControllerUnitTest {

    @Mock
    private IDocumentTypeService service;

    @Mock
    private DocumentTypeDomainMapper mapper;

    @Mock
    private PaginationHelper paginationHelper;

    @InjectMocks
    private DocumentTypeController controller;

    private static final Long ID = 1L;
    private static final String ID_ENTERPRISE = "ENT001";
    private static final String PREFIX = "FAC";
    private static final String NAME = "Factura de venta";
    private static final Long DOCUMENT_CLASS_ID = 10L;
    private static final Integer MODULE_ID = 3;

    private DocumentTypeCreateReq createReq;
    private DocumentTypeUpdateReq updateReq;
    private DocumentType domain;
    private DocumentTypeRes response;

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

        domain = DocumentType.builder()
                .id(ID)
                .prefix(PREFIX)
                .name(NAME)
                .documentClassId(DOCUMENT_CLASS_ID)
                .moduleId(MODULE_ID)
                .idEnterprise(ID_ENTERPRISE)
                .status(true)
                .usageCount(0)
                .build();

        response = DocumentTypeRes.builder()
                .id(ID)
                .prefix(PREFIX)
                .name(NAME)
                .documentClassId(DOCUMENT_CLASS_ID)
                .moduleId(MODULE_ID)
                .idEnterprise(ID_ENTERPRISE)
                .status(true)
                .build();
    }

    // ========== CREATE TESTS ==========

    @Test
    @DisplayName("create - Debe crear tipo de documento y retornar OK")
    void testCreateReturnsOk() {
        // Arrange
        when(service.create(createReq)).thenReturn(domain);
        when(mapper.toRes(domain)).thenReturn(response);

        // Act
        ResponseEntity<DocumentTypeRes> result = controller.create(createReq);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(ID, result.getBody().getId());
        verify(service).create(createReq);
        verify(mapper).toRes(domain);
    }

    @Test
    @DisplayName("create - Debe retornar datos correctos del tipo de documento creado")
    void testCreateReturnsCorrectData() {
        // Arrange
        when(service.create(createReq)).thenReturn(domain);
        when(mapper.toRes(domain)).thenReturn(response);

        // Act
        ResponseEntity<DocumentTypeRes> result = controller.create(createReq);

        // Assert
        assertNotNull(result.getBody());
        assertEquals(NAME, result.getBody().getName());
        assertEquals(PREFIX, result.getBody().getPrefix());
        assertEquals(ID_ENTERPRISE, result.getBody().getIdEnterprise());
    }

    // ========== UPDATE TESTS ==========

    @Test
    @DisplayName("update - Debe actualizar tipo de documento y retornar OK")
    void testUpdateReturnsOk() {
        // Arrange
        when(service.update(updateReq)).thenReturn(domain);
        when(mapper.toRes(domain)).thenReturn(response);

        // Act
        ResponseEntity<DocumentTypeRes> result = controller.update(updateReq);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        verify(service).update(updateReq);
        verify(mapper).toRes(domain);
    }

    @Test
    @DisplayName("update - Debe retornar datos actualizados correctamente")
    void testUpdateReturnsUpdatedData() {
        // Arrange
        String updatedName = "Factura actualizada";
        DocumentType updatedDomain = DocumentType.builder()
                .id(ID)
                .name(updatedName)
                .prefix(PREFIX)
                .build();
        DocumentTypeRes updatedResponse = DocumentTypeRes.builder()
                .id(ID)
                .name(updatedName)
                .prefix(PREFIX)
                .build();
        when(service.update(updateReq)).thenReturn(updatedDomain);
        when(mapper.toRes(updatedDomain)).thenReturn(updatedResponse);

        // Act
        ResponseEntity<DocumentTypeRes> result = controller.update(updateReq);

        // Assert
        assertNotNull(result.getBody());
        assertEquals(updatedName, result.getBody().getName());
    }

    // ========== GET BY ID TESTS ==========

    @Test
    @DisplayName("getById - Debe retornar tipo de documento cuando existe")
    void testGetByIdReturnsDocumentType() {
        // Arrange
        when(service.findById(ID, ID_ENTERPRISE)).thenReturn(domain);
        when(mapper.toRes(domain)).thenReturn(response);

        // Act
        ResponseEntity<DocumentTypeRes> result = controller.getById(ID, ID_ENTERPRISE);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(ID, result.getBody().getId());
        verify(service).findById(ID, ID_ENTERPRISE);
    }

    @Test
    @DisplayName("getById - Debe llamar al servicio con parámetros correctos")
    void testGetByIdCallsServiceWithCorrectParams() {
        // Arrange
        Long specificId = 42L;
        String specificEnterprise = "ENT_SPECIFIC";
        when(service.findById(specificId, specificEnterprise)).thenReturn(domain);
        when(mapper.toRes(domain)).thenReturn(response);

        // Act
        controller.getById(specificId, specificEnterprise);

        // Assert
        verify(service).findById(specificId, specificEnterprise);
    }

    // ========== LIST (FIND ALL) TESTS ==========

    @Test
    @DisplayName("list - Debe retornar página de tipos de documento sin búsqueda")
    void testListReturnsPageWithoutSearch() {
        // Arrange
        Page<DocumentType> domainPage = new PageImpl<>(List.of(domain));
        Pageable pageable = PageRequest.of(0, 10);
        when(service.countAllByEnterprise(ID_ENTERPRISE)).thenReturn(1L);
        when(paginationHelper.createFlexiblePageable(any(), any(), eq(1L))).thenReturn(pageable);
        when(service.findAllByEnterprise(eq(ID_ENTERPRISE), eq(0), eq(10), eq("name"), eq("asc")))
                .thenReturn(domainPage);
        when(mapper.toRes(domain)).thenReturn(response);

        // Act
        ResponseEntity<Page<DocumentTypeRes>> result = controller.list(
                ID_ENTERPRISE, Optional.of(0), Optional.of(10), "name", "asc", null);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().getTotalElements());
    }

    @Test
    @DisplayName("list - Debe retornar página de tipos de documento con búsqueda")
    void testListReturnsPageWithSearch() {
        // Arrange
        String searchTerm = "Factura";
        Page<DocumentType> domainPage = new PageImpl<>(List.of(domain));
        Pageable pageable = PageRequest.of(0, 10);
        when(service.countByEnterpriseAndNameContaining(ID_ENTERPRISE, searchTerm)).thenReturn(1L);
        when(paginationHelper.createFlexiblePageable(any(), any(), eq(1L))).thenReturn(pageable);
        when(service.findByEnterpriseAndNameContaining(eq(ID_ENTERPRISE), eq(searchTerm), eq(0), eq(10), eq("name"), eq("asc")))
                .thenReturn(domainPage);
        when(mapper.toRes(domain)).thenReturn(response);

        // Act
        ResponseEntity<Page<DocumentTypeRes>> result = controller.list(
                ID_ENTERPRISE, Optional.of(0), Optional.of(10), "name", "asc", searchTerm);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        verify(service).findByEnterpriseAndNameContaining(eq(ID_ENTERPRISE), eq(searchTerm), anyInt(), anyInt(), anyString(), anyString());
    }

    @Test
    @DisplayName("list - Debe retornar página vacía cuando no hay tipos de documento")
    void testListReturnsEmptyPage() {
        // Arrange
        Page<DocumentType> emptyPage = Page.empty();
        Pageable pageable = PageRequest.of(0, 10);
        when(service.countAllByEnterprise(ID_ENTERPRISE)).thenReturn(0L);
        when(paginationHelper.createFlexiblePageable(any(), any(), eq(0L))).thenReturn(pageable);
        when(service.findAllByEnterprise(eq(ID_ENTERPRISE), eq(0), eq(10), eq("name"), eq("asc")))
                .thenReturn(emptyPage);

        // Act
        ResponseEntity<Page<DocumentTypeRes>> result = controller.list(
                ID_ENTERPRISE, Optional.of(0), Optional.of(10), "name", "asc", null);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isEmpty());
    }

    @Test
    @DisplayName("list - Debe usar countAllByEnterprise cuando search es vacío")
    void testListUsesCountAllWhenSearchEmpty() {
        // Arrange
        Page<DocumentType> domainPage = new PageImpl<>(List.of(domain));
        Pageable pageable = PageRequest.of(0, 10);
        when(service.countAllByEnterprise(ID_ENTERPRISE)).thenReturn(1L);
        when(paginationHelper.createFlexiblePageable(any(), any(), eq(1L))).thenReturn(pageable);
        when(service.findAllByEnterprise(eq(ID_ENTERPRISE), eq(0), eq(10), eq("name"), eq("asc")))
                .thenReturn(domainPage);
        when(mapper.toRes(domain)).thenReturn(response);

        // Act
        controller.list(ID_ENTERPRISE, Optional.of(0), Optional.of(10), "name", "asc", "   ");

        // Assert
        verify(service).countAllByEnterprise(ID_ENTERPRISE);
        verify(service, never()).countByEnterpriseAndNameContaining(any(), any());
    }

    @Test
    @DisplayName("list - Debe ordenar descendente cuando se especifica")
    void testListSortsDescending() {
        // Arrange
        Page<DocumentType> domainPage = new PageImpl<>(List.of(domain));
        Pageable pageable = PageRequest.of(0, 10);
        when(service.countAllByEnterprise(ID_ENTERPRISE)).thenReturn(1L);
        when(paginationHelper.createFlexiblePageable(any(), any(), eq(1L))).thenReturn(pageable);
        when(service.findAllByEnterprise(eq(ID_ENTERPRISE), eq(0), eq(10), eq("name"), eq("desc")))
                .thenReturn(domainPage);
        when(mapper.toRes(domain)).thenReturn(response);

        // Act
        controller.list(ID_ENTERPRISE, Optional.of(0), Optional.of(10), "name", "desc", null);

        // Assert
        verify(service).findAllByEnterprise(eq(ID_ENTERPRISE), eq(0), eq(10), eq("name"), eq("desc"));
    }

    // ========== LIST BY MODULE TESTS ==========

    @Test
    @DisplayName("listByModule - Debe retornar lista de tipos por módulo")
    void testListByModuleReturnsList() {
        // Arrange
        when(service.findAllByModuleAndEnterprise(MODULE_ID, ID_ENTERPRISE)).thenReturn(List.of(domain));
        when(mapper.toRes(domain)).thenReturn(response);

        // Act
        ResponseEntity<List<DocumentTypeRes>> result = controller.listByModule(ID_ENTERPRISE, MODULE_ID);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());
        verify(service).findAllByModuleAndEnterprise(MODULE_ID, ID_ENTERPRISE);
    }

    @Test
    @DisplayName("listByModule - Debe retornar lista vacía cuando no hay tipos")
    void testListByModuleReturnsEmptyList() {
        // Arrange
        when(service.findAllByModuleAndEnterprise(MODULE_ID, ID_ENTERPRISE)).thenReturn(List.of());

        // Act
        ResponseEntity<List<DocumentTypeRes>> result = controller.listByModule(ID_ENTERPRISE, MODULE_ID);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isEmpty());
    }

    @Test
    @DisplayName("listByModule - Debe mapear todos los elementos de la lista")
    void testListByModuleMapsAllElements() {
        // Arrange
        DocumentType domain2 = DocumentType.builder().id(2L).name("Tipo 2").build();
        DocumentTypeRes response2 = DocumentTypeRes.builder().id(2L).name("Tipo 2").build();
        when(service.findAllByModuleAndEnterprise(MODULE_ID, ID_ENTERPRISE)).thenReturn(List.of(domain, domain2));
        when(mapper.toRes(domain)).thenReturn(response);
        when(mapper.toRes(domain2)).thenReturn(response2);

        // Act
        ResponseEntity<List<DocumentTypeRes>> result = controller.listByModule(ID_ENTERPRISE, MODULE_ID);

        // Assert
        assertNotNull(result.getBody());
        assertEquals(2, result.getBody().size());
        verify(mapper, times(2)).toRes(any(DocumentType.class));
    }

    // ========== CHANGE STATE TESTS ==========

    @Test
    @DisplayName("changeState - Debe activar tipo de documento")
    void testChangeStateActivate() {
        // Arrange
        DocumentType activeDomain = DocumentType.builder().id(ID).status(true).build();
        DocumentTypeRes activeResponse = DocumentTypeRes.builder().id(ID).status(true).build();
        when(service.changeState(ID, ID_ENTERPRISE, true)).thenReturn(activeDomain);
        when(mapper.toRes(activeDomain)).thenReturn(activeResponse);

        // Act
        ResponseEntity<DocumentTypeRes> result = controller.changeState(ID, ID_ENTERPRISE, true);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().getStatus());
        verify(service).changeState(ID, ID_ENTERPRISE, true);
    }

    @Test
    @DisplayName("changeState - Debe desactivar tipo de documento")
    void testChangeStateDeactivate() {
        // Arrange
        DocumentType inactiveDomain = DocumentType.builder().id(ID).status(false).build();
        DocumentTypeRes inactiveResponse = DocumentTypeRes.builder().id(ID).status(false).build();
        when(service.changeState(ID, ID_ENTERPRISE, false)).thenReturn(inactiveDomain);
        when(mapper.toRes(inactiveDomain)).thenReturn(inactiveResponse);

        // Act
        ResponseEntity<DocumentTypeRes> result = controller.changeState(ID, ID_ENTERPRISE, false);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertFalse(result.getBody().getStatus());
        verify(service).changeState(ID, ID_ENTERPRISE, false);
    }

    // ========== DELETE TESTS ==========

    @Test
    @DisplayName("delete - Debe eliminar tipo de documento y retornar OK")
    void testDeleteReturnsOk() {
        // Arrange
        when(service.Delete(ID, ID_ENTERPRISE)).thenReturn(domain);
        when(mapper.toRes(domain)).thenReturn(response);

        // Act
        ResponseEntity<DocumentTypeRes> result = controller.Delete(ID, ID_ENTERPRISE);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        verify(service).Delete(ID, ID_ENTERPRISE);
    }

    @Test
    @DisplayName("delete - Debe retornar datos del tipo eliminado")
    void testDeleteReturnsDeletedData() {
        // Arrange
        when(service.Delete(ID, ID_ENTERPRISE)).thenReturn(domain);
        when(mapper.toRes(domain)).thenReturn(response);

        // Act
        ResponseEntity<DocumentTypeRes> result = controller.Delete(ID, ID_ENTERPRISE);

        // Assert
        assertNotNull(result.getBody());
        assertEquals(ID, result.getBody().getId());
        assertEquals(NAME, result.getBody().getName());
    }

    // ========== GET ALL MODULES TESTS ==========

    @Test
    @DisplayName("getAllModules - Debe retornar todos los módulos")
    void testGetAllModulesReturnsAllModules() {
        // Arrange
        List<DocumentModule> modules = Arrays.asList(DocumentModule.values());
        when(service.getAllModules()).thenReturn(modules);

        // Act
        ResponseEntity<List<DocumentModuleRes>> result = controller.getAllModules();

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(8, result.getBody().size());
    }

    @Test
    @DisplayName("getAllModules - Debe mapear ID y nombre de cada módulo")
    void testGetAllModulesMapsIdAndName() {
        // Arrange
        List<DocumentModule> modules = Arrays.asList(DocumentModule.values());
        when(service.getAllModules()).thenReturn(modules);

        // Act
        ResponseEntity<List<DocumentModuleRes>> result = controller.getAllModules();

        // Assert
        assertNotNull(result.getBody());
        DocumentModuleRes firstModule = result.getBody().get(0);
        assertEquals(DocumentModule.INVENTARIO_PROMEDIO_PONDERADO.getId(), firstModule.getId());
        assertEquals(DocumentModule.INVENTARIO_PROMEDIO_PONDERADO.getName(), firstModule.getName());
    }

    @Test
    @DisplayName("getAllModules - Debe contener módulo COMERCIAL")
    void testGetAllModulesContainsComercial() {
        // Arrange
        List<DocumentModule> modules = Arrays.asList(DocumentModule.values());
        when(service.getAllModules()).thenReturn(modules);

        // Act
        ResponseEntity<List<DocumentModuleRes>> result = controller.getAllModules();

        // Assert
        assertNotNull(result.getBody());
        boolean containsComercial = result.getBody().stream()
                .anyMatch(m -> m.getName().equals("Comercial") && m.getId().equals(3));
        assertTrue(containsComercial);
    }

    @Test
    @DisplayName("getAllModules - Debe retornar lista vacía cuando no hay módulos")
    void testGetAllModulesReturnsEmptyList() {
        // Arrange
        when(service.getAllModules()).thenReturn(List.of());

        // Act
        ResponseEntity<List<DocumentModuleRes>> result = controller.getAllModules();

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isEmpty());
    }
}

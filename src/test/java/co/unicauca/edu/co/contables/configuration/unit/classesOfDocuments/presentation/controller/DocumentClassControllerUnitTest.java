package co.unicauca.edu.co.contables.configuration.unit.classesOfDocuments.presentation.controller;

import co.unicauca.edu.co.contables.configuration.classesOfDocuments.domain.mapper.DocumentClassDomainMapper;
import co.unicauca.edu.co.contables.configuration.classesOfDocuments.domain.models.DocumentClass;
import co.unicauca.edu.co.contables.configuration.classesOfDocuments.domain.services.IDocumentClassService;
import co.unicauca.edu.co.contables.configuration.classesOfDocuments.presentation.DTO.request.DocumentClassCreateReq;
import co.unicauca.edu.co.contables.configuration.classesOfDocuments.presentation.DTO.request.DocumentClassUpdateReq;
import co.unicauca.edu.co.contables.configuration.classesOfDocuments.presentation.DTO.response.DocumentClassRes;
import co.unicauca.edu.co.contables.configuration.classesOfDocuments.presentation.controller.DocumentClassController;
import co.unicauca.edu.co.contables.configuration.commons.utils.PaginationHelper;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DocumentClassControllerUnitTest {

    @Mock
    private IDocumentClassService service;

    @Mock
    private DocumentClassDomainMapper mapper;

    @Mock
    private PaginationHelper paginationHelper;

    @InjectMocks
    private DocumentClassController controller;

    private static final Long ID = 1L;
    private static final String ENTERPRISE_ID = "ENT-001";
    private static final String NAME = "Facturas";

    private DocumentClass domain;
    private DocumentClassRes response;

    @BeforeEach
    void setUp() {
        domain = DocumentClass.builder()
                .id(ID)
                .name(NAME)
                .idEnterprise(ENTERPRISE_ID)
                .status(true)
                .build();

        response = new DocumentClassRes();
        response.setId(ID);
        response.setName(NAME);
        response.setIdEnterprise(ENTERPRISE_ID);
        response.setStatus(true);
    }

    @Test
    @DisplayName("create - Debe crear clase de documento y retornar 200 OK")
    void testCreateSuccess() {
        // Arrange
        DocumentClassCreateReq request = new DocumentClassCreateReq(ENTERPRISE_ID, "facturas");
        when(service.create(request)).thenReturn(domain);
        when(mapper.toRes(domain)).thenReturn(response);

        // Act
        ResponseEntity<DocumentClassRes> result = controller.create(request);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(ID, result.getBody().getId());
        assertEquals(NAME, result.getBody().getName());
        verify(service).create(request);
        verify(mapper).toRes(domain);
    }

    @Test
    @DisplayName("update - Debe actualizar clase de documento y retornar 200 OK")
    void testUpdateSuccess() {
        // Arrange
        DocumentClassUpdateReq request = new DocumentClassUpdateReq(ID, ENTERPRISE_ID, "recibos");
        when(service.update(request)).thenReturn(domain);
        when(mapper.toRes(domain)).thenReturn(response);

        // Act
        ResponseEntity<DocumentClassRes> result = controller.update(request);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(ID, result.getBody().getId());
        verify(service).update(request);
        verify(mapper).toRes(domain);
    }

    @Test
    @DisplayName("getById - Debe retornar clase de documento cuando existe")
    void testGetByIdSuccess() {
        // Arrange
        when(service.findById(ID, ENTERPRISE_ID)).thenReturn(domain);
        when(mapper.toRes(domain)).thenReturn(response);

        // Act
        ResponseEntity<DocumentClassRes> result = controller.getById(ID, ENTERPRISE_ID);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(ID, result.getBody().getId());
        assertEquals(NAME, result.getBody().getName());
        verify(service).findById(ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("list - Debe retornar página de clases de documento sin filtro de búsqueda")
    void testListWithoutSearch() {
        // Arrange
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);
        Page<DocumentClass> domainPage = new PageImpl<>(List.of(domain));
        when(service.countAllByEnterprise(ENTERPRISE_ID)).thenReturn(1L);
        when(paginationHelper.createFlexiblePageable(Optional.of(page), Optional.of(size), 1L))
                .thenReturn(pageable);
        when(service.findAllByEnterprise(ENTERPRISE_ID, page, size, "name", "asc"))
                .thenReturn(domainPage);
        when(mapper.toRes(domain)).thenReturn(response);

        // Act
        ResponseEntity<Page<DocumentClassRes>> result = controller.list(
                ENTERPRISE_ID, Optional.of(page), Optional.of(size), "name", "asc", null);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().getContent().size());
        verify(service).countAllByEnterprise(ENTERPRISE_ID);
        verify(service).findAllByEnterprise(ENTERPRISE_ID, page, size, "name", "asc");
    }

    @Test
    @DisplayName("list - Debe retornar página de clases de documento con filtro de búsqueda")
    void testListWithSearch() {
        // Arrange
        int page = 0;
        int size = 10;
        String search = "fact";
        Pageable pageable = PageRequest.of(page, size);
        Page<DocumentClass> domainPage = new PageImpl<>(List.of(domain));
        when(service.countByEnterpriseAndNameContaining(ENTERPRISE_ID, search)).thenReturn(1L);
        when(paginationHelper.createFlexiblePageable(Optional.of(page), Optional.of(size), 1L))
                .thenReturn(pageable);
        when(service.findByEnterpriseAndNameContaining(ENTERPRISE_ID, search, page, size, "name", "asc"))
                .thenReturn(domainPage);
        when(mapper.toRes(domain)).thenReturn(response);

        // Act
        ResponseEntity<Page<DocumentClassRes>> result = controller.list(
                ENTERPRISE_ID, Optional.of(page), Optional.of(size), "name", "asc", search);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().getContent().size());
        verify(service).countByEnterpriseAndNameContaining(ENTERPRISE_ID, search);
        verify(service).findByEnterpriseAndNameContaining(ENTERPRISE_ID, search, page, size, "name", "asc");
    }

    @Test
    @DisplayName("list - Debe retornar página vacía cuando no hay datos")
    void testListReturnsEmptyPage() {
        // Arrange
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);
        Page<DocumentClass> emptyPage = new PageImpl<>(Collections.emptyList());
        when(service.countAllByEnterprise(ENTERPRISE_ID)).thenReturn(0L);
        when(paginationHelper.createFlexiblePageable(Optional.of(page), Optional.of(size), 0L))
                .thenReturn(pageable);
        when(service.findAllByEnterprise(ENTERPRISE_ID, page, size, "name", "asc"))
                .thenReturn(emptyPage);

        // Act
        ResponseEntity<Page<DocumentClassRes>> result = controller.list(
                ENTERPRISE_ID, Optional.of(page), Optional.of(size), "name", "asc", null);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().getContent().isEmpty());
    }

    @Test
    @DisplayName("list - Debe usar paginación flexible cuando no se especifican parámetros")
    void testListWithoutPaginationParams() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 5);
        Page<DocumentClass> domainPage = new PageImpl<>(List.of(domain));
        when(service.countAllByEnterprise(ENTERPRISE_ID)).thenReturn(5L);
        when(paginationHelper.createFlexiblePageable(Optional.empty(), Optional.empty(), 5L))
                .thenReturn(pageable);
        when(service.findAllByEnterprise(ENTERPRISE_ID, 0, 5, "name", "asc"))
                .thenReturn(domainPage);
        when(mapper.toRes(domain)).thenReturn(response);

        // Act
        ResponseEntity<Page<DocumentClassRes>> result = controller.list(
                ENTERPRISE_ID, Optional.empty(), Optional.empty(), "name", "asc", null);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(paginationHelper).createFlexiblePageable(Optional.empty(), Optional.empty(), 5L);
    }

    @Test
    @DisplayName("list - Debe ordenar descendente cuando se especifica")
    void testListWithDescendingOrder() {
        // Arrange
        int page = 0;
        int size = 10;
        String sortField = "name";
        String sortOrder = "desc";
        Pageable pageable = PageRequest.of(page, size);
        Page<DocumentClass> domainPage = new PageImpl<>(List.of(domain));
        when(service.countAllByEnterprise(ENTERPRISE_ID)).thenReturn(1L);
        when(paginationHelper.createFlexiblePageable(Optional.of(page), Optional.of(size), 1L))
                .thenReturn(pageable);
        when(service.findAllByEnterprise(ENTERPRISE_ID, page, size, sortField, sortOrder))
                .thenReturn(domainPage);
        when(mapper.toRes(domain)).thenReturn(response);

        // Act
        ResponseEntity<Page<DocumentClassRes>> result = controller.list(
                ENTERPRISE_ID, Optional.of(page), Optional.of(size), sortField, sortOrder, null);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(service).findAllByEnterprise(ENTERPRISE_ID, page, size, sortField, sortOrder);
    }

    @Test
    @DisplayName("listActive - Debe retornar página de clases de documento activas")
    void testListActiveSuccess() {
        // Arrange
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);
        Page<DocumentClass> domainPage = new PageImpl<>(List.of(domain));
        when(service.countAllByEnterpriseAndStatus(ENTERPRISE_ID, true)).thenReturn(1L);
        when(paginationHelper.createFlexiblePageable(Optional.of(page), Optional.of(size), 1L))
                .thenReturn(pageable);
        when(service.findAllByEnterpriseAndStatus(ENTERPRISE_ID, true, page, size))
                .thenReturn(domainPage);
        when(mapper.toRes(domain)).thenReturn(response);

        // Act
        ResponseEntity<Page<DocumentClassRes>> result = controller.listActive(
                ENTERPRISE_ID, Optional.of(page), Optional.of(size));

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().getContent().size());
        verify(service).countAllByEnterpriseAndStatus(ENTERPRISE_ID, true);
        verify(service).findAllByEnterpriseAndStatus(ENTERPRISE_ID, true, page, size);
    }

    @Test
    @DisplayName("listActive - Debe retornar página vacía cuando no hay activos")
    void testListActiveReturnsEmptyPage() {
        // Arrange
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);
        Page<DocumentClass> emptyPage = new PageImpl<>(Collections.emptyList());
        when(service.countAllByEnterpriseAndStatus(ENTERPRISE_ID, true)).thenReturn(0L);
        when(paginationHelper.createFlexiblePageable(Optional.of(page), Optional.of(size), 0L))
                .thenReturn(pageable);
        when(service.findAllByEnterpriseAndStatus(ENTERPRISE_ID, true, page, size))
                .thenReturn(emptyPage);

        // Act
        ResponseEntity<Page<DocumentClassRes>> result = controller.listActive(
                ENTERPRISE_ID, Optional.of(page), Optional.of(size));

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().getContent().isEmpty());
    }

    @Test
    @DisplayName("listActive - Debe usar paginación flexible sin parámetros")
    void testListActiveWithoutPaginationParams() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 3);
        Page<DocumentClass> domainPage = new PageImpl<>(List.of(domain));
        when(service.countAllByEnterpriseAndStatus(ENTERPRISE_ID, true)).thenReturn(3L);
        when(paginationHelper.createFlexiblePageable(Optional.empty(), Optional.empty(), 3L))
                .thenReturn(pageable);
        when(service.findAllByEnterpriseAndStatus(ENTERPRISE_ID, true, 0, 3))
                .thenReturn(domainPage);
        when(mapper.toRes(domain)).thenReturn(response);

        // Act
        ResponseEntity<Page<DocumentClassRes>> result = controller.listActive(
                ENTERPRISE_ID, Optional.empty(), Optional.empty());

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(paginationHelper).createFlexiblePageable(Optional.empty(), Optional.empty(), 3L);
    }

    @Test
    @DisplayName("changeState - Debe cambiar estado a inactivo y retornar 200 OK")
    void testChangeStateToInactive() {
        // Arrange
        DocumentClass inactiveDomain = DocumentClass.builder()
                .id(ID)
                .name(NAME)
                .idEnterprise(ENTERPRISE_ID)
                .status(false)
                .build();
        DocumentClassRes inactiveResponse = new DocumentClassRes();
        inactiveResponse.setId(ID);
        inactiveResponse.setName(NAME);
        inactiveResponse.setIdEnterprise(ENTERPRISE_ID);
        inactiveResponse.setStatus(false);
        when(service.changeState(ID, ENTERPRISE_ID, false)).thenReturn(inactiveDomain);
        when(mapper.toRes(inactiveDomain)).thenReturn(inactiveResponse);

        // Act
        ResponseEntity<DocumentClassRes> result = controller.changeState(ID, ENTERPRISE_ID, false);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertFalse(result.getBody().getStatus());
        verify(service).changeState(ID, ENTERPRISE_ID, false);
    }

    @Test
    @DisplayName("changeState - Debe cambiar estado a activo y retornar 200 OK")
    void testChangeStateToActive() {
        // Arrange
        when(service.changeState(ID, ENTERPRISE_ID, true)).thenReturn(domain);
        when(mapper.toRes(domain)).thenReturn(response);

        // Act
        ResponseEntity<DocumentClassRes> result = controller.changeState(ID, ENTERPRISE_ID, true);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().getStatus());
        verify(service).changeState(ID, ENTERPRISE_ID, true);
    }

    @Test
    @DisplayName("Delete - Debe eliminar clase de documento y retornar 200 OK")
    void testDeleteSuccess() {
        // Arrange
        when(service.Delete(ID, ENTERPRISE_ID)).thenReturn(domain);
        when(mapper.toRes(domain)).thenReturn(response);

        // Act
        ResponseEntity<DocumentClassRes> result = controller.Delete(ID, ENTERPRISE_ID);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(ID, result.getBody().getId());
        verify(service).Delete(ID, ENTERPRISE_ID);
        verify(mapper).toRes(domain);
    }

    @Test
    @DisplayName("list - Debe ignorar búsqueda vacía y usar findAll")
    void testListIgnoresEmptySearch() {
        // Arrange
        int page = 0;
        int size = 10;
        String search = "   ";
        Pageable pageable = PageRequest.of(page, size);
        Page<DocumentClass> domainPage = new PageImpl<>(List.of(domain));
        when(service.countAllByEnterprise(ENTERPRISE_ID)).thenReturn(1L);
        when(paginationHelper.createFlexiblePageable(Optional.of(page), Optional.of(size), 1L))
                .thenReturn(pageable);
        when(service.findAllByEnterprise(ENTERPRISE_ID, page, size, "name", "asc"))
                .thenReturn(domainPage);
        when(mapper.toRes(domain)).thenReturn(response);

        // Act
        ResponseEntity<Page<DocumentClassRes>> result = controller.list(
                ENTERPRISE_ID, Optional.of(page), Optional.of(size), "name", "asc", search);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(service).countAllByEnterprise(ENTERPRISE_ID);
        verify(service).findAllByEnterprise(ENTERPRISE_ID, page, size, "name", "asc");
        verify(service, never()).countByEnterpriseAndNameContaining(anyString(), anyString());
    }

    @Test
    @DisplayName("list - Debe mapear múltiples dominios a respuestas")
    void testListMapsMultipleDomains() {
        // Arrange
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);
        DocumentClass domain2 = DocumentClass.builder()
                .id(2L)
                .name("Recibos")
                .idEnterprise(ENTERPRISE_ID)
                .status(true)
                .build();
        DocumentClassRes response2 = new DocumentClassRes();
        response2.setId(2L);
        response2.setName("Recibos");
        response2.setIdEnterprise(ENTERPRISE_ID);
        response2.setStatus(true);
        Page<DocumentClass> domainPage = new PageImpl<>(List.of(domain, domain2));
        when(service.countAllByEnterprise(ENTERPRISE_ID)).thenReturn(2L);
        when(paginationHelper.createFlexiblePageable(Optional.of(page), Optional.of(size), 2L))
                .thenReturn(pageable);
        when(service.findAllByEnterprise(ENTERPRISE_ID, page, size, "name", "asc"))
                .thenReturn(domainPage);
        when(mapper.toRes(domain)).thenReturn(response);
        when(mapper.toRes(domain2)).thenReturn(response2);

        // Act
        ResponseEntity<Page<DocumentClassRes>> result = controller.list(
                ENTERPRISE_ID, Optional.of(page), Optional.of(size), "name", "asc", null);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(2, result.getBody().getContent().size());
        verify(mapper, times(2)).toRes(any(DocumentClass.class));
    }
}

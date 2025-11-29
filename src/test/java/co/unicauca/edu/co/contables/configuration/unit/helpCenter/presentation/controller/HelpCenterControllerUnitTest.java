package co.unicauca.edu.co.contables.configuration.unit.helpCenter.presentation.controller;

import co.unicauca.edu.co.contables.configuration.commons.utils.PaginationHelper;
import co.unicauca.edu.co.contables.configuration.helpCenter.domain.mapper.HelpCenterDomainMapper;
import co.unicauca.edu.co.contables.configuration.helpCenter.domain.models.DocumentModule;
import co.unicauca.edu.co.contables.configuration.helpCenter.domain.models.HelpCenter;
import co.unicauca.edu.co.contables.configuration.helpCenter.domain.services.IHelpCenterService;
import co.unicauca.edu.co.contables.configuration.helpCenter.presentation.DTO.request.HelpCenterCreateReq;
import co.unicauca.edu.co.contables.configuration.helpCenter.presentation.DTO.request.HelpCenterUpdateReq;
import co.unicauca.edu.co.contables.configuration.helpCenter.presentation.DTO.response.HelpCenterRes;
import co.unicauca.edu.co.contables.configuration.helpCenter.presentation.controller.HelpCenterController;
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
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HelpCenterControllerUnitTest {

    @Mock
    private IHelpCenterService service;

    @Mock
    private HelpCenterDomainMapper mapper;

    @Mock
    private PaginationHelper paginationHelper;

    @InjectMocks
    private HelpCenterController controller;

    private static final Long ID = 1L;
    private static final Integer MODULE_ID = 1;
    private static final String NAME = "Ayuda inventario";
    private static final String MODULE_NAME = "Inventario promedio ponderado";
    private static final String DESCRIPTION = "Descripción de ayuda";

    private HelpCenter domain;
    private HelpCenterRes response;
    private HelpCenterCreateReq createRequest;
    private HelpCenterUpdateReq updateRequest;

    @BeforeEach
    void setUp() {
        domain = HelpCenter.builder()
                .id(ID)
                .moduleId(MODULE_ID)
                .name(NAME)
                .description(DESCRIPTION)
                .status(true)
                .build();

        response = HelpCenterRes.builder()
                .id(ID)
                .moduleId(MODULE_ID)
                .moduleName(MODULE_NAME)
                .name(NAME)
                .description(DESCRIPTION)
                .status(true)
                .build();

        createRequest = HelpCenterCreateReq.builder()
                .moduleId(MODULE_ID)
                .name(NAME)
                .description(DESCRIPTION)
                .build();

        updateRequest = HelpCenterUpdateReq.builder()
                .id(ID)
                .moduleId(MODULE_ID)
                .name(NAME)
                .description(DESCRIPTION)
                .build();
    }

    // ========== CREATE TESTS ==========

    @Test
    @DisplayName("create - Debe crear registro de ayuda y retornar 200 OK")
    void testCreateSuccess() {
        // Arrange
        when(service.create(createRequest)).thenReturn(domain);
        when(mapper.toRes(domain)).thenReturn(response);

        // Act
        ResponseEntity<HelpCenterRes> result = controller.create(createRequest);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
        verify(service).create(createRequest);
        verify(mapper).toRes(domain);
    }

    // ========== UPDATE TESTS ==========

    @Test
    @DisplayName("update - Debe actualizar registro de ayuda y retornar 200 OK")
    void testUpdateSuccess() {
        // Arrange
        when(service.update(updateRequest)).thenReturn(domain);
        when(mapper.toRes(domain)).thenReturn(response);

        // Act
        ResponseEntity<HelpCenterRes> result = controller.update(updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
        verify(service).update(updateRequest);
        verify(mapper).toRes(domain);
    }

    // ========== GET BY ID TESTS ==========

    @Test
    @DisplayName("getById - Debe retornar registro de ayuda por ID")
    void testGetByIdSuccess() {
        // Arrange
        when(service.findById(ID)).thenReturn(domain);
        when(mapper.toRes(domain)).thenReturn(response);

        // Act
        ResponseEntity<HelpCenterRes> result = controller.getById(ID);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
        verify(service).findById(ID);
    }

    // ========== LIST TESTS ==========

    @Test
    @DisplayName("list - Debe retornar página de registros sin parámetros de paginación")
    void testListWithoutPagination() {
        // Arrange
        Page<HelpCenter> domainPage = new PageImpl<>(List.of(domain));
        Pageable pageable = PageRequest.of(0, 10);
        when(service.countAll()).thenReturn(10L);
        when(paginationHelper.createFlexiblePageable(Optional.empty(), Optional.empty(), 10L)).thenReturn(pageable);
        when(service.findAll(0, 10, "name", "asc")).thenReturn(domainPage);
        when(mapper.toRes(domain)).thenReturn(response);

        // Act
        ResponseEntity<Page<HelpCenterRes>> result = controller.list(
                Optional.empty(), Optional.empty(), "name", "asc", null);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().getTotalElements());
    }

    @Test
    @DisplayName("list - Debe retornar página de registros con paginación")
    void testListWithPagination() {
        // Arrange
        Page<HelpCenter> domainPage = new PageImpl<>(List.of(domain));
        Pageable pageable = PageRequest.of(0, 5);
        when(service.countAll()).thenReturn(10L);
        when(paginationHelper.createFlexiblePageable(Optional.of(0), Optional.of(5), 10L)).thenReturn(pageable);
        when(service.findAll(0, 5, "name", "asc")).thenReturn(domainPage);
        when(mapper.toRes(domain)).thenReturn(response);

        // Act
        ResponseEntity<Page<HelpCenterRes>> result = controller.list(
                Optional.of(0), Optional.of(5), "name", "asc", null);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    @DisplayName("list - Debe retornar página con búsqueda")
    void testListWithSearch() {
        // Arrange
        String search = "ayuda";
        Page<HelpCenter> domainPage = new PageImpl<>(List.of(domain));
        Pageable pageable = PageRequest.of(0, 10);
        when(service.countByNameContaining(search)).thenReturn(1L);
        when(paginationHelper.createFlexiblePageable(Optional.empty(), Optional.empty(), 1L)).thenReturn(pageable);
        when(service.findByNameContaining(search, 0, 10, "name", "asc")).thenReturn(domainPage);
        when(mapper.toRes(domain)).thenReturn(response);

        // Act
        ResponseEntity<Page<HelpCenterRes>> result = controller.list(
                Optional.empty(), Optional.empty(), "name", "asc", search);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(service).countByNameContaining(search);
        verify(service).findByNameContaining(search, 0, 10, "name", "asc");
    }

    @Test
    @DisplayName("list - Debe retornar página vacía cuando no hay resultados")
    void testListEmpty() {
        // Arrange
        Page<HelpCenter> emptyPage = new PageImpl<>(Collections.emptyList());
        Pageable pageable = PageRequest.of(0, 1);
        when(service.countAll()).thenReturn(0L);
        when(paginationHelper.createFlexiblePageable(Optional.empty(), Optional.empty(), 0L)).thenReturn(pageable);
        when(service.findAll(0, 1, "name", "asc")).thenReturn(emptyPage);

        // Act
        ResponseEntity<Page<HelpCenterRes>> result = controller.list(
                Optional.empty(), Optional.empty(), "name", "asc", null);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().isEmpty());
    }

    @Test
    @DisplayName("list - Debe ignorar búsqueda vacía")
    void testListWithEmptySearch() {
        // Arrange
        String search = "   ";
        Page<HelpCenter> domainPage = new PageImpl<>(List.of(domain));
        Pageable pageable = PageRequest.of(0, 10);
        when(service.countAll()).thenReturn(10L);
        when(paginationHelper.createFlexiblePageable(Optional.empty(), Optional.empty(), 10L)).thenReturn(pageable);
        when(service.findAll(0, 10, "name", "asc")).thenReturn(domainPage);
        when(mapper.toRes(domain)).thenReturn(response);

        // Act
        ResponseEntity<Page<HelpCenterRes>> result = controller.list(
                Optional.empty(), Optional.empty(), "name", "asc", search);

        // Assert
        assertNotNull(result);
        verify(service).countAll();
        verify(service).findAll(0, 10, "name", "asc");
    }

    @Test
    @DisplayName("list - Debe ordenar descendente")
    void testListWithDescendingOrder() {
        // Arrange
        Page<HelpCenter> domainPage = new PageImpl<>(List.of(domain));
        Pageable pageable = PageRequest.of(0, 10);
        when(service.countAll()).thenReturn(10L);
        when(paginationHelper.createFlexiblePageable(Optional.empty(), Optional.empty(), 10L)).thenReturn(pageable);
        when(service.findAll(0, 10, "name", "desc")).thenReturn(domainPage);
        when(mapper.toRes(domain)).thenReturn(response);

        // Act
        ResponseEntity<Page<HelpCenterRes>> result = controller.list(
                Optional.empty(), Optional.empty(), "name", "desc", null);

        // Assert
        assertNotNull(result);
        verify(service).findAll(0, 10, "name", "desc");
    }

    @Test
    @DisplayName("list - Debe ordenar por módulo")
    void testListWithSortByModule() {
        // Arrange
        Page<HelpCenter> domainPage = new PageImpl<>(List.of(domain));
        Pageable pageable = PageRequest.of(0, 10);
        when(service.countAll()).thenReturn(10L);
        when(paginationHelper.createFlexiblePageable(Optional.empty(), Optional.empty(), 10L)).thenReturn(pageable);
        when(service.findAll(0, 10, "module", "asc")).thenReturn(domainPage);
        when(mapper.toRes(domain)).thenReturn(response);

        // Act
        ResponseEntity<Page<HelpCenterRes>> result = controller.list(
                Optional.empty(), Optional.empty(), "module", "asc", null);

        // Assert
        assertNotNull(result);
        verify(service).findAll(0, 10, "module", "asc");
    }

    // ========== LIST BY MODULE TESTS ==========

    @Test
    @DisplayName("listByModule - Debe retornar lista de registros por módulo")
    void testListByModuleSuccess() {
        // Arrange
        when(service.findAllByModule(MODULE_ID)).thenReturn(List.of(domain));
        when(mapper.toRes(domain)).thenReturn(response);

        // Act
        ResponseEntity<List<HelpCenterRes>> result = controller.listByModule(MODULE_ID);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
        verify(service).findAllByModule(MODULE_ID);
    }

    @Test
    @DisplayName("listByModule - Debe retornar lista vacía cuando no hay registros")
    void testListByModuleEmpty() {
        // Arrange
        when(service.findAllByModule(MODULE_ID)).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<HelpCenterRes>> result = controller.listByModule(MODULE_ID);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().isEmpty());
    }

    @Test
    @DisplayName("listByModule - Debe retornar múltiples registros")
    void testListByModuleMultiple() {
        // Arrange
        HelpCenter domain2 = HelpCenter.builder().id(2L).moduleId(MODULE_ID).name("Otra ayuda").build();
        HelpCenterRes response2 = HelpCenterRes.builder().id(2L).moduleId(MODULE_ID).name("Otra ayuda").build();
        when(service.findAllByModule(MODULE_ID)).thenReturn(List.of(domain, domain2));
        when(mapper.toRes(domain)).thenReturn(response);
        when(mapper.toRes(domain2)).thenReturn(response2);

        // Act
        ResponseEntity<List<HelpCenterRes>> result = controller.listByModule(MODULE_ID);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getBody().size());
    }

    // ========== CHANGE STATE TESTS ==========

    @Test
    @DisplayName("changeState - Debe activar registro")
    void testChangeStateActivate() {
        // Arrange
        when(service.changeState(ID, true)).thenReturn(domain);
        when(mapper.toRes(domain)).thenReturn(response);

        // Act
        ResponseEntity<HelpCenterRes> result = controller.changeState(ID, true);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(service).changeState(ID, true);
    }

    @Test
    @DisplayName("changeState - Debe desactivar registro")
    void testChangeStateDeactivate() {
        // Arrange
        domain.setStatus(false);
        response.setStatus(false);
        when(service.changeState(ID, false)).thenReturn(domain);
        when(mapper.toRes(domain)).thenReturn(response);

        // Act
        ResponseEntity<HelpCenterRes> result = controller.changeState(ID, false);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertFalse(result.getBody().getStatus());
        verify(service).changeState(ID, false);
    }

    // ========== DELETE TESTS ==========

    @Test
    @DisplayName("delete - Debe eliminar registro y retornar 200 OK")
    void testDeleteSuccess() {
        // Arrange
        when(service.delete(ID)).thenReturn(domain);
        when(mapper.toRes(domain)).thenReturn(response);

        // Act
        ResponseEntity<HelpCenterRes> result = controller.delete(ID);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
        verify(service).delete(ID);
    }

    // ========== GET MODULES TESTS ==========

    @Test
    @DisplayName("getModules - Debe retornar lista de módulos disponibles")
    void testGetModulesSuccess() {
        // Act
        ResponseEntity<List<Map<String, Object>>> result = controller.getModules();

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(DocumentModule.values().length, result.getBody().size());
    }

    @Test
    @DisplayName("getModules - Debe incluir ID y nombre de cada módulo")
    void testGetModulesContainsIdAndName() {

        // Act
        ResponseEntity<List<Map<String, Object>>> result = controller.getModules();

        // Assert
        assertNotNull(result);
        List<Map<String, Object>> modules = result.getBody();
        assertNotNull(modules);
        assertFalse(modules.isEmpty());
        
        Map<String, Object> firstModule = modules.get(0);
        assertTrue(firstModule.containsKey("id"));
        assertTrue(firstModule.containsKey("name"));
    }

    @Test
    @DisplayName("getModules - Debe retornar módulo INVENTARIO_PROMEDIO_PONDERADO con ID 1")
    void testGetModulesContainsInventarioPromedioPonderado() {

        // Act
        ResponseEntity<List<Map<String, Object>>> result = controller.getModules();

        // Assert
        assertNotNull(result);
        List<Map<String, Object>> modules = result.getBody();
        
        boolean foundModule = modules.stream()
                .anyMatch(m -> m.get("id").equals(1) && 
                              m.get("name").equals("Inventario promedio ponderado"));
        
        assertTrue(foundModule);
    }

    @Test
    @DisplayName("getModules - Debe retornar todos los módulos del enum")
    void testGetModulesReturnsAllEnumValues() {

        // Act
        ResponseEntity<List<Map<String, Object>>> result = controller.getModules();

        // Assert
        assertNotNull(result);
        List<Map<String, Object>> modules = result.getBody();
        
        for (DocumentModule module : DocumentModule.values()) {
            boolean found = modules.stream()
                    .anyMatch(m -> m.get("id").equals(module.getId()) && 
                                  m.get("name").equals(module.getName()));
            assertTrue(found, "No se encontró el módulo: " + module.name());
        }
    }
}

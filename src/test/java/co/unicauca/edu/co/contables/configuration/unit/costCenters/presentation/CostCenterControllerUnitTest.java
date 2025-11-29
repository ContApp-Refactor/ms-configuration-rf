package co.unicauca.edu.co.contables.configuration.unit.costCenters.presentation;

import co.unicauca.edu.co.contables.configuration.commons.utils.PaginationHelper;
import co.unicauca.edu.co.contables.configuration.costCenters.domain.mapper.CostCenterDomainMapper;
import co.unicauca.edu.co.contables.configuration.costCenters.domain.models.CostCenter;
import co.unicauca.edu.co.contables.configuration.costCenters.domain.services.ICostCenterService;
import co.unicauca.edu.co.contables.configuration.costCenters.domain.services.IExportCostCenterService;
import co.unicauca.edu.co.contables.configuration.costCenters.presentation.DTO.request.CostCenterCreateReq;
import co.unicauca.edu.co.contables.configuration.costCenters.presentation.DTO.request.CostCenterUpdateReq;
import co.unicauca.edu.co.contables.configuration.costCenters.presentation.DTO.response.CostCenterRes;
import co.unicauca.edu.co.contables.configuration.costCenters.presentation.controller.CostCenterController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CostCenterControllerUnitTest {

    @Mock
    private ICostCenterService service;

    @Mock
    private IExportCostCenterService exportService;

    @Mock
    private CostCenterDomainMapper mapper;

    @Mock
    private PaginationHelper paginationHelper;

    @InjectMocks
    private CostCenterController controller;

    private static final Long ID = 1L;
    private static final String ENTERPRISE_ID = "ENT-001";
    private static final String CODE = "1001";
    private static final String NAME = "Centro de costo";

    private CostCenter domain;
    private CostCenterRes response;
    private CostCenterCreateReq createRequest;
    private CostCenterUpdateReq updateRequest;

    @BeforeEach
    void setUp() {
        domain = CostCenter.builder()
                .id(ID)
                .idEnterprise(ENTERPRISE_ID)
                .code(CODE)
                .name(NAME)
                .status(true)
                .usageCount(0)
                .build();

        response = CostCenterRes.builder()
                .id(ID)
                .idEnterprise(ENTERPRISE_ID)
                .code(CODE)
                .name(NAME)
                .status(true)
                .usageCount(0)
                .build();

        createRequest = CostCenterCreateReq.builder()
                .idEnterprise(ENTERPRISE_ID)
                .code(CODE)
                .name(NAME)
                .build();

        updateRequest = CostCenterUpdateReq.builder()
                .id(ID)
                .idEnterprise(ENTERPRISE_ID)
                .code(CODE)
                .name(NAME)
                .build();
    }

    // ========== CREATE TESTS ==========

    @Test
    @DisplayName("create - Debe crear centro de costo y retornar 200 OK")
    void testCreateSuccess() {
        // Arrange
        when(service.create(createRequest)).thenReturn(domain);
        when(mapper.toRes(domain)).thenReturn(response);

        // Act
        ResponseEntity<CostCenterRes> result = controller.create(createRequest);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
        verify(service).create(createRequest);
        verify(mapper).toRes(domain);
    }

    @Test
    @DisplayName("create - Debe crear centro de costo con padre")
    void testCreateWithParent() {
        // Arrange
        createRequest.setParentId(2L);
        when(service.create(createRequest)).thenReturn(domain);
        when(mapper.toRes(domain)).thenReturn(response);

        // Act
        ResponseEntity<CostCenterRes> result = controller.create(createRequest);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(service).create(createRequest);
    }

    // ========== UPDATE TESTS ==========

    @Test
    @DisplayName("update - Debe actualizar centro de costo y retornar 200 OK")
    void testUpdateSuccess() {
        // Arrange
        when(service.update(updateRequest)).thenReturn(domain);
        when(mapper.toRes(domain)).thenReturn(response);

        // Act
        ResponseEntity<CostCenterRes> result = controller.update(updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
        verify(service).update(updateRequest);
        verify(mapper).toRes(domain);
    }

    // ========== GET BY ID TESTS ==========

    @Test
    @DisplayName("getById - Debe retornar centro de costo por ID")
    void testGetByIdSuccess() {
        // Arrange
        when(service.findById(ID, ENTERPRISE_ID)).thenReturn(domain);
        when(mapper.toRes(domain)).thenReturn(response);

        // Act
        ResponseEntity<CostCenterRes> result = controller.getById(ID, ENTERPRISE_ID);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
        verify(service).findById(ID, ENTERPRISE_ID);
    }

    // ========== LIST HIERARCHICAL TESTS ==========

    @Test
    @DisplayName("listHierarchical - Debe retornar página jerárquica sin parámetros de paginación")
    void testListHierarchicalWithoutPagination() {
        // Arrange
        Page<CostCenter> domainPage = new PageImpl<>(List.of(domain));
        Pageable pageable = PageRequest.of(0, 10);
        when(service.countAllByEnterprise(ENTERPRISE_ID)).thenReturn(10L);
        when(paginationHelper.createFlexiblePageable(Optional.empty(), Optional.empty(), 10L)).thenReturn(pageable);
        when(service.findAllByEnterpriseHierarchical(ENTERPRISE_ID, 0, 10)).thenReturn(domainPage);
        when(mapper.toRes(domain)).thenReturn(response);

        // Act
        ResponseEntity<Page<CostCenterRes>> result = controller.listHierarchical(
                ENTERPRISE_ID, Optional.empty(), Optional.empty(), null);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().getTotalElements());
    }

    @Test
    @DisplayName("listHierarchical - Debe retornar página jerárquica con parámetros de paginación")
    void testListHierarchicalWithPagination() {
        // Arrange
        Page<CostCenter> domainPage = new PageImpl<>(List.of(domain));
        Pageable pageable = PageRequest.of(0, 5);
        when(service.countAllByEnterprise(ENTERPRISE_ID)).thenReturn(10L);
        when(paginationHelper.createFlexiblePageable(Optional.of(0), Optional.of(5), 10L)).thenReturn(pageable);
        when(service.findAllByEnterpriseHierarchical(ENTERPRISE_ID, 0, 5)).thenReturn(domainPage);
        when(mapper.toRes(domain)).thenReturn(response);

        // Act
        ResponseEntity<Page<CostCenterRes>> result = controller.listHierarchical(
                ENTERPRISE_ID, Optional.of(0), Optional.of(5), null);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    @DisplayName("listHierarchical - Debe retornar página con búsqueda")
    void testListHierarchicalWithSearch() {
        // Arrange
        String search = "centro";
        Page<CostCenter> domainPage = new PageImpl<>(List.of(domain));
        Pageable pageable = PageRequest.of(0, 10);
        when(service.countByEnterpriseAndSearch(ENTERPRISE_ID, search)).thenReturn(1L);
        when(paginationHelper.createFlexiblePageable(Optional.empty(), Optional.empty(), 1L)).thenReturn(pageable);
        when(service.findByEnterpriseAndSearch(ENTERPRISE_ID, search, 0, 10)).thenReturn(domainPage);
        when(mapper.toRes(domain)).thenReturn(response);

        // Act
        ResponseEntity<Page<CostCenterRes>> result = controller.listHierarchical(
                ENTERPRISE_ID, Optional.empty(), Optional.empty(), search);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(service).countByEnterpriseAndSearch(ENTERPRISE_ID, search);
        verify(service).findByEnterpriseAndSearch(ENTERPRISE_ID, search, 0, 10);
    }

    @Test
    @DisplayName("listHierarchical - Debe retornar página vacía cuando no hay resultados")
    void testListHierarchicalEmpty() {
        // Arrange
        Page<CostCenter> emptyPage = new PageImpl<>(Collections.emptyList());
        Pageable pageable = PageRequest.of(0, 1);
        when(service.countAllByEnterprise(ENTERPRISE_ID)).thenReturn(0L);
        when(paginationHelper.createFlexiblePageable(Optional.empty(), Optional.empty(), 0L)).thenReturn(pageable);
        when(service.findAllByEnterpriseHierarchical(ENTERPRISE_ID, 0, 1)).thenReturn(emptyPage);

        // Act
        ResponseEntity<Page<CostCenterRes>> result = controller.listHierarchical(
                ENTERPRISE_ID, Optional.empty(), Optional.empty(), null);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().isEmpty());
    }

    @Test
    @DisplayName("listHierarchical - Debe ignorar búsqueda vacía")
    void testListHierarchicalWithEmptySearch() {
        // Arrange
        String search = "   ";
        Page<CostCenter> domainPage = new PageImpl<>(List.of(domain));
        Pageable pageable = PageRequest.of(0, 10);
        when(service.countAllByEnterprise(ENTERPRISE_ID)).thenReturn(10L);
        when(paginationHelper.createFlexiblePageable(Optional.empty(), Optional.empty(), 10L)).thenReturn(pageable);
        when(service.findAllByEnterpriseHierarchical(ENTERPRISE_ID, 0, 10)).thenReturn(domainPage);
        when(mapper.toRes(domain)).thenReturn(response);

        // Act
        ResponseEntity<Page<CostCenterRes>> result = controller.listHierarchical(
                ENTERPRISE_ID, Optional.empty(), Optional.empty(), search);

        // Assert
        assertNotNull(result);
        verify(service).countAllByEnterprise(ENTERPRISE_ID);
        verify(service).findAllByEnterpriseHierarchical(ENTERPRISE_ID, 0, 10);
    }

    // ========== LIST BY STATUS TESTS ==========

    @Test
    @DisplayName("listByStatus - Debe retornar centros activos")
    void testListByStatusActive() {
        // Arrange
        Page<CostCenter> domainPage = new PageImpl<>(List.of(domain));
        Pageable pageable = PageRequest.of(0, 10);
        when(service.countAllByEnterpriseAndStatus(ENTERPRISE_ID, true)).thenReturn(5L);
        when(paginationHelper.createFlexiblePageable(Optional.empty(), Optional.empty(), 5L)).thenReturn(pageable);
        when(service.findAllByEnterpriseAndStatus(ENTERPRISE_ID, true, 0, 10)).thenReturn(domainPage);
        when(mapper.toRes(domain)).thenReturn(response);

        // Act
        ResponseEntity<Page<CostCenterRes>> result = controller.listByStatus(
                ENTERPRISE_ID, true, Optional.empty(), Optional.empty());

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(service).findAllByEnterpriseAndStatus(ENTERPRISE_ID, true, 0, 10);
    }

    @Test
    @DisplayName("listByStatus - Debe retornar centros inactivos")
    void testListByStatusInactive() {
        // Arrange
        CostCenter inactiveDomain = CostCenter.builder()
                .id(2L)
                .status(false)
                .build();
        CostCenterRes inactiveResponse = CostCenterRes.builder()
                .id(2L)
                .status(false)
                .build();
        Page<CostCenter> domainPage = new PageImpl<>(List.of(inactiveDomain));
        Pageable pageable = PageRequest.of(0, 10);
        when(service.countAllByEnterpriseAndStatus(ENTERPRISE_ID, false)).thenReturn(3L);
        when(paginationHelper.createFlexiblePageable(Optional.empty(), Optional.empty(), 3L)).thenReturn(pageable);
        when(service.findAllByEnterpriseAndStatus(ENTERPRISE_ID, false, 0, 10)).thenReturn(domainPage);
        when(mapper.toRes(inactiveDomain)).thenReturn(inactiveResponse);

        // Act
        ResponseEntity<Page<CostCenterRes>> result = controller.listByStatus(
                ENTERPRISE_ID, false, Optional.empty(), Optional.empty());

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(service).findAllByEnterpriseAndStatus(ENTERPRISE_ID, false, 0, 10);
    }

    @Test
    @DisplayName("listByStatus - Debe retornar página con paginación específica")
    void testListByStatusWithPagination() {
        // Arrange
        Page<CostCenter> domainPage = new PageImpl<>(List.of(domain));
        Pageable pageable = PageRequest.of(1, 5);
        when(service.countAllByEnterpriseAndStatus(ENTERPRISE_ID, true)).thenReturn(10L);
        when(paginationHelper.createFlexiblePageable(Optional.of(1), Optional.of(5), 10L)).thenReturn(pageable);
        when(service.findAllByEnterpriseAndStatus(ENTERPRISE_ID, true, 1, 5)).thenReturn(domainPage);
        when(mapper.toRes(domain)).thenReturn(response);

        // Act
        ResponseEntity<Page<CostCenterRes>> result = controller.listByStatus(
                ENTERPRISE_ID, true, Optional.of(1), Optional.of(5));

        // Assert
        assertNotNull(result);
        verify(service).findAllByEnterpriseAndStatus(ENTERPRISE_ID, true, 1, 5);
    }

    // ========== CHANGE STATE TESTS ==========

    @Test
    @DisplayName("changeState - Debe activar centro de costo")
    void testChangeStateActivate() {
        // Arrange
        when(service.changeState(ID, ENTERPRISE_ID, true)).thenReturn(domain);
        when(mapper.toRes(domain)).thenReturn(response);

        // Act
        ResponseEntity<CostCenterRes> result = controller.changeState(ID, ENTERPRISE_ID, true);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(service).changeState(ID, ENTERPRISE_ID, true);
    }

    @Test
    @DisplayName("changeState - Debe desactivar centro de costo")
    void testChangeStateDeactivate() {
        // Arrange
        domain.setStatus(false);
        response.setStatus(false);
        when(service.changeState(ID, ENTERPRISE_ID, false)).thenReturn(domain);
        when(mapper.toRes(domain)).thenReturn(response);

        // Act
        ResponseEntity<CostCenterRes> result = controller.changeState(ID, ENTERPRISE_ID, false);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertFalse(result.getBody().getStatus());
        verify(service).changeState(ID, ENTERPRISE_ID, false);
    }

    // ========== DELETE TESTS ==========

    @Test
    @DisplayName("delete - Debe eliminar centro de costo y retornar 200 OK")
    void testDeleteSuccess() {
        // Arrange
        when(service.delete(ID, ENTERPRISE_ID)).thenReturn(domain);
        when(mapper.toRes(domain)).thenReturn(response);

        // Act
        ResponseEntity<CostCenterRes> result = controller.delete(ID, ENTERPRISE_ID);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
        verify(service).delete(ID, ENTERPRISE_ID);
    }

    // ========== FIND ACTIVE LAST LEVEL TESTS ==========

    @Test
    @DisplayName("findActiveLastLevelCostCenters - Debe retornar lista de centros auxiliares")
    void testFindActiveLastLevelCostCentersSuccess() {
        // Arrange
        when(service.findActiveLastLevelCostCenters(ENTERPRISE_ID)).thenReturn(List.of(domain));
        when(mapper.toRes(domain)).thenReturn(response);

        // Act
        ResponseEntity<List<CostCenterRes>> result = controller.findActiveLastLevelCostCenters(ENTERPRISE_ID);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
        verify(service).findActiveLastLevelCostCenters(ENTERPRISE_ID);
    }

    @Test
    @DisplayName("findActiveLastLevelCostCenters - Debe retornar lista vacía cuando no hay auxiliares")
    void testFindActiveLastLevelCostCentersEmpty() {
        // Arrange
        when(service.findActiveLastLevelCostCenters(ENTERPRISE_ID)).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<CostCenterRes>> result = controller.findActiveLastLevelCostCenters(ENTERPRISE_ID);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().isEmpty());
    }

    @Test
    @DisplayName("findActiveLastLevelCostCenters - Debe retornar múltiples centros auxiliares")
    void testFindActiveLastLevelCostCentersMultiple() {
        // Arrange
        CostCenter domain2 = CostCenter.builder().id(2L).code("10011").build();
        CostCenter domain3 = CostCenter.builder().id(3L).code("10012").build();
        CostCenterRes response2 = CostCenterRes.builder().id(2L).code("10011").build();
        CostCenterRes response3 = CostCenterRes.builder().id(3L).code("10012").build();
        when(service.findActiveLastLevelCostCenters(ENTERPRISE_ID)).thenReturn(List.of(domain, domain2, domain3));
        when(mapper.toRes(domain)).thenReturn(response);
        when(mapper.toRes(domain2)).thenReturn(response2);
        when(mapper.toRes(domain3)).thenReturn(response3);

        // Act
        ResponseEntity<List<CostCenterRes>> result = controller.findActiveLastLevelCostCenters(ENTERPRISE_ID);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getBody().size());
    }

    // ========== EXPORT TESTS ==========

    @Test
    @DisplayName("exportCostCenters - Debe exportar todos los centros de costo")
    void testExportCostCentersAll() {
        // Arrange
        Resource resource = new ByteArrayResource(new byte[]{1, 2, 3});
        when(exportService.exportCostCenters(ENTERPRISE_ID, null)).thenReturn(resource);

        // Act
        ResponseEntity<Resource> result = controller.exportCostCenters(ENTERPRISE_ID, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getHeaders().containsKey(HttpHeaders.CONTENT_DISPOSITION));
        verify(exportService).exportCostCenters(ENTERPRISE_ID, null);
    }

    @Test
    @DisplayName("exportCostCenters - Debe exportar solo centros activos")
    void testExportCostCentersActiveOnly() {
        // Arrange
        Resource resource = new ByteArrayResource(new byte[]{1, 2, 3});
        when(exportService.exportCostCenters(ENTERPRISE_ID, true)).thenReturn(resource);

        // Act
        ResponseEntity<Resource> result = controller.exportCostCenters(ENTERPRISE_ID, true, null);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(exportService).exportCostCenters(ENTERPRISE_ID, true);
    }

    @Test
    @DisplayName("exportCostCenters - Debe exportar solo centros inactivos")
    void testExportCostCentersInactiveOnly() {
        // Arrange
        Resource resource = new ByteArrayResource(new byte[]{1, 2, 3});
        when(exportService.exportCostCenters(ENTERPRISE_ID, false)).thenReturn(resource);

        // Act
        ResponseEntity<Resource> result = controller.exportCostCenters(ENTERPRISE_ID, false, null);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(exportService).exportCostCenters(ENTERPRISE_ID, false);
    }

    @Test
    @DisplayName("exportCostCenters - Debe incluir nombre de empresa en el archivo")
    void testExportCostCentersWithCompanyName() {
        // Arrange
        String companyName = "Mi Empresa";
        Resource resource = new ByteArrayResource(new byte[]{1, 2, 3});
        when(exportService.exportCostCenters(ENTERPRISE_ID, null)).thenReturn(resource);

        // Act
        ResponseEntity<Resource> result = controller.exportCostCenters(ENTERPRISE_ID, null, companyName);

        // Assert
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        String contentDisposition = result.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
        assertNotNull(contentDisposition);
        assertTrue(contentDisposition.contains("attachment"));
    }

    @Test
    @DisplayName("exportCostCenters - Debe generar header Content-Disposition correcto")
    void testExportCostCentersContentDispositionHeader() {
        // Arrange
        Resource resource = new ByteArrayResource(new byte[]{1, 2, 3});
        when(exportService.exportCostCenters(ENTERPRISE_ID, true)).thenReturn(resource);

        // Act
        ResponseEntity<Resource> result = controller.exportCostCenters(ENTERPRISE_ID, true, "Empresa");

        // Assert
        assertNotNull(result);
        String contentDisposition = result.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
        assertNotNull(contentDisposition);
        assertTrue(contentDisposition.startsWith("attachment; filename=\""));
        assertTrue(contentDisposition.endsWith("\""));
    }
}

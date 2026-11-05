package co.unicauca.edu.co.contables.accounting.catalogue.unit.taxes.infraestructure.input.rest.controller;

import co.unicauca.edu.co.contables.commons.utils.PaginationHelper;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.input.ITaxChangeStateInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.input.ITaxCreateInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.input.ITaxDeleteInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.input.ITaxSearchInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.input.ITaxUpdateInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.DTO.TaxDTO;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.models.Tax;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.input.rest.controller.TaxController;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.input.rest.dto.request.TaxCreateReq;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.input.rest.dto.request.TaxUpdateReq;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.input.rest.dto.response.TaxChangeStateRes;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.input.rest.dto.response.TaxCreateRes;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.input.rest.dto.response.TaxSearchRes;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.input.rest.dto.response.TaxUpdateRes;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.input.rest.mapper.ITaxChangeStateRestMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.input.rest.mapper.ITaxCreateRestMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.input.rest.mapper.ITaxSearchRestMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.input.rest.mapper.ITaxUpdateRestMapper;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TaxControllerUnitTest {

    @Mock
    private ITaxCreateRestMapper taxCreateRestMapper;

    @Mock
    private ITaxSearchRestMapper taxSearchRestMapper;

    @Mock
    private ITaxCreateInputPort taxCreateInputPort;

    @Mock
    private ITaxUpdateInputPort taxUpdateInputPort;

    @Mock
    private ITaxSearchInputPort taxSearchInputPort;

    @Mock
    private ITaxUpdateRestMapper taxUpdateRestMapper;

    @Mock
    private ITaxDeleteInputPort taxDeleteInputPort;

    @Mock
    private ITaxChangeStateInputPort taxChangeStateInputPort;

    @Mock
    private ITaxChangeStateRestMapper taxChangeStateRestMapper;

    @Mock
    private PaginationHelper paginationHelper;

    @InjectMocks
    private TaxController taxController;

    private String idEnterprise;
    private Long taxId;
    private Tax tax;
    private TaxDTO taxDTO;
    private TaxCreateReq taxCreateReq;
    private TaxCreateRes taxCreateRes;
    private TaxUpdateReq taxUpdateReq;
    private TaxUpdateRes taxUpdateRes;
    private TaxSearchRes taxSearchRes;
    private TaxChangeStateRes taxChangeStateRes;

    @BeforeEach
    void setUp() {
        idEnterprise = "ENT-001";
        taxId = 1L;

        tax = Tax.builder()
                .id(taxId)
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19%")
                .interest(19.0)
                .status(true)
                .usageCount(0)
                .build();

        taxDTO = TaxDTO.builder()
                .id(taxId)
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19%")
                .interest(19.0)
                .salesTaxId(100L)
                .purchaseTaxId(200L)
                .build();

        taxCreateReq = TaxCreateReq.builder()
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19%")
                .interest(19.0)
                .salesTaxId(100L)
                .purchaseTaxId(200L)
                .build();

        taxCreateRes = TaxCreateRes.builder()
                .id(taxId)
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19%")
                .interest(19.0)
                .salesTaxId(100L)
                .purchaseTaxId(200L)
                .status(true)
                .usageCount(0)
                .build();

        taxUpdateReq = TaxUpdateReq.builder()
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19% actualizado")
                .interest(19.0)
                .salesTaxId(100L)
                .purchaseTaxId(200L)
                .build();

        taxUpdateRes = TaxUpdateRes.builder()
                .id(taxId)
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19% actualizado")
                .interest(19.0)
                .purchaseTax(200L)
                .salesTax(100L)
                .status(true)
                .usageCount(0)
                .build();

        taxSearchRes = TaxSearchRes.builder()
                .id(taxId)
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19%")
                .interest(19.0)
                .purchaseTax("24080502 - Cuenta compras")
                .salesTax("24080501 - Cuenta ventas")
                .status(true)
                .usageCount(0)
                .build();

        taxChangeStateRes = TaxChangeStateRes.builder()
                .id(taxId)
                .code("IVA19")
                .description("Impuesto al valor agregado 19%")
                .status(false)
                .usageCount(0)
                .message("Estado cambiado exitosamente")
                .build();
    }

    // ==================== Tests de createTax ====================

    @Test
    @DisplayName("Debe crear impuesto exitosamente")
    void testCreateTaxSuccess() {
        // Arrange
        when(taxCreateRestMapper.toDomain(taxCreateReq)).thenReturn(taxDTO);
        when(taxCreateInputPort.createTax(taxDTO)).thenReturn(tax);
        when(taxCreateRestMapper.toCreateResponse(tax)).thenReturn(taxCreateRes);

        // Act
        ResponseEntity<TaxCreateRes> response = taxController.createTax(taxCreateReq);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(taxId, response.getBody().getId());
        assertEquals("IVA19", response.getBody().getCode());
        verify(taxCreateRestMapper).toDomain(taxCreateReq);
        verify(taxCreateInputPort).createTax(taxDTO);
        verify(taxCreateRestMapper).toCreateResponse(tax);
    }

    @Test
    @DisplayName("Debe invocar mappers y servicios en orden correcto al crear")
    void testCreateTaxInvokesInCorrectOrder() {
        // Arrange
        when(taxCreateRestMapper.toDomain(taxCreateReq)).thenReturn(taxDTO);
        when(taxCreateInputPort.createTax(taxDTO)).thenReturn(tax);
        when(taxCreateRestMapper.toCreateResponse(tax)).thenReturn(taxCreateRes);

        // Act
        taxController.createTax(taxCreateReq);

        // Assert
        var inOrder = inOrder(taxCreateRestMapper, taxCreateInputPort);
        inOrder.verify(taxCreateRestMapper).toDomain(taxCreateReq);
        inOrder.verify(taxCreateInputPort).createTax(taxDTO);
        inOrder.verify(taxCreateRestMapper).toCreateResponse(tax);
    }

    // ==================== Tests de getTax ====================

    @Test
    @DisplayName("Debe obtener impuesto por código exitosamente")
    void testGetTaxByCodeSuccess() {
        // Arrange
        String code = "IVA19";
        when(taxSearchInputPort.getTax(code, idEnterprise)).thenReturn(tax);
        when(taxSearchRestMapper.toSearchResponse(tax)).thenReturn(taxSearchRes);

        // Act
        ResponseEntity<TaxSearchRes> response = taxController.getTax(code, idEnterprise);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(code, response.getBody().getCode());
        verify(taxSearchInputPort).getTax(code, idEnterprise);
        verify(taxSearchRestMapper).toSearchResponse(tax);
    }

    // ==================== Tests de getTaxesPaginated ====================

    @Test
    @DisplayName("Debe obtener impuestos paginados sin búsqueda")
    void testGetTaxesPaginatedWithoutSearch() {
        // Arrange
        long totalRecords = 10L;
        Pageable pageable = PageRequest.of(0, 10);
        List<Tax> taxes = Collections.singletonList(tax);
        Page<Tax> taxPage = new PageImpl<>(taxes, pageable, totalRecords);
        when(taxSearchInputPort.countTaxesByEnterprise(idEnterprise)).thenReturn(totalRecords);
        when(paginationHelper.createFlexiblePageable(any(), any(), eq(totalRecords))).thenReturn(pageable);
        when(taxSearchInputPort.getTaxesPaginated(idEnterprise, 0, 10, "description", "asc")).thenReturn(taxPage);
        when(taxSearchRestMapper.toSearchResponse(tax)).thenReturn(taxSearchRes);

        // Act
        ResponseEntity<Page<TaxSearchRes>> response = taxController.getTaxesPaginated(
                idEnterprise, Optional.empty(), Optional.empty(), "description", "asc", null);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        verify(taxSearchInputPort).countTaxesByEnterprise(idEnterprise);
        verify(taxSearchInputPort).getTaxesPaginated(idEnterprise, 0, 10, "description", "asc");
    }

    @Test
    @DisplayName("Debe obtener impuestos paginados con búsqueda")
    void testGetTaxesPaginatedWithSearch() {
        // Arrange
        String search = "IVA";
        long totalRecords = 5L;
        Pageable pageable = PageRequest.of(0, 10);
        List<Tax> taxes = Collections.singletonList(tax);
        Page<Tax> taxPage = new PageImpl<>(taxes, pageable, totalRecords);
        when(taxSearchInputPort.countTaxesByEnterpriseAndCodeOrDescription(idEnterprise, search)).thenReturn(totalRecords);
        when(paginationHelper.createFlexiblePageable(any(), any(), eq(totalRecords))).thenReturn(pageable);
        when(taxSearchInputPort.getTaxesByCodeOrDescriptionPaginated(idEnterprise, search, 0, 10, "description", "asc")).thenReturn(taxPage);
        when(taxSearchRestMapper.toSearchResponse(tax)).thenReturn(taxSearchRes);

        // Act
        ResponseEntity<Page<TaxSearchRes>> response = taxController.getTaxesPaginated(
                idEnterprise, Optional.empty(), Optional.empty(), "description", "asc", search);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(taxSearchInputPort).countTaxesByEnterpriseAndCodeOrDescription(idEnterprise, search);
        verify(taxSearchInputPort).getTaxesByCodeOrDescriptionPaginated(idEnterprise, search, 0, 10, "description", "asc");
    }

    @Test
    @DisplayName("Debe obtener impuestos paginados con búsqueda vacía como sin búsqueda")
    void testGetTaxesPaginatedWithEmptySearch() {
        // Arrange
        String search = "   ";
        long totalRecords = 10L;
        Pageable pageable = PageRequest.of(0, 10);
        List<Tax> taxes = Collections.singletonList(tax);
        Page<Tax> taxPage = new PageImpl<>(taxes, pageable, totalRecords);
        when(taxSearchInputPort.countTaxesByEnterprise(idEnterprise)).thenReturn(totalRecords);
        when(paginationHelper.createFlexiblePageable(any(), any(), eq(totalRecords))).thenReturn(pageable);
        when(taxSearchInputPort.getTaxesPaginated(idEnterprise, 0, 10, "code", "desc")).thenReturn(taxPage);
        when(taxSearchRestMapper.toSearchResponse(tax)).thenReturn(taxSearchRes);

        // Act
        ResponseEntity<Page<TaxSearchRes>> response = taxController.getTaxesPaginated(
                idEnterprise, Optional.empty(), Optional.empty(), "code", "desc", search);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(taxSearchInputPort).countTaxesByEnterprise(idEnterprise);
        verify(taxSearchInputPort).getTaxesPaginated(idEnterprise, 0, 10, "code", "desc");
    }

    @Test
    @DisplayName("Debe obtener impuestos paginados con parámetros de paginación personalizados")
    void testGetTaxesPaginatedWithCustomPagination() {
        // Arrange
        long totalRecords = 50L;
        Pageable pageable = PageRequest.of(2, 5);
        List<Tax> taxes = Collections.singletonList(tax);
        Page<Tax> taxPage = new PageImpl<>(taxes, pageable, totalRecords);
        when(taxSearchInputPort.countTaxesByEnterprise(idEnterprise)).thenReturn(totalRecords);
        when(paginationHelper.createFlexiblePageable(eq(Optional.of(2)), eq(Optional.of(5)), eq(totalRecords))).thenReturn(pageable);
        when(taxSearchInputPort.getTaxesPaginated(idEnterprise, 2, 5, "description", "asc")).thenReturn(taxPage);
        when(taxSearchRestMapper.toSearchResponse(tax)).thenReturn(taxSearchRes);

        // Act
        ResponseEntity<Page<TaxSearchRes>> response = taxController.getTaxesPaginated(
                idEnterprise, Optional.of(2), Optional.of(5), "description", "asc", null);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(paginationHelper).createFlexiblePageable(Optional.of(2), Optional.of(5), totalRecords);
    }

    // ==================== Tests de getActiveTaxes ====================

    @Test
    @DisplayName("Debe obtener impuestos activos exitosamente")
    void testGetActiveTaxesSuccess() {
        // Arrange
        List<Tax> activeTaxes = Arrays.asList(tax);
        List<TaxSearchRes> activeTaxesRes = Arrays.asList(taxSearchRes);
        when(taxSearchInputPort.getActiveTaxes(idEnterprise)).thenReturn(activeTaxes);
        when(taxSearchRestMapper.toSearchListResponse(activeTaxes)).thenReturn(activeTaxesRes);

        // Act
        ResponseEntity<List<TaxSearchRes>> response = taxController.getActiveTaxes(idEnterprise);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(taxSearchInputPort).getActiveTaxes(idEnterprise);
        verify(taxSearchRestMapper).toSearchListResponse(activeTaxes);
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay impuestos activos")
    void testGetActiveTaxesReturnsEmptyList() {
        // Arrange
        List<Tax> emptyList = Collections.emptyList();
        List<TaxSearchRes> emptyResList = Collections.emptyList();
        when(taxSearchInputPort.getActiveTaxes(idEnterprise)).thenReturn(emptyList);
        when(taxSearchRestMapper.toSearchListResponse(emptyList)).thenReturn(emptyResList);

        // Act
        ResponseEntity<List<TaxSearchRes>> response = taxController.getActiveTaxes(idEnterprise);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    // ==================== Tests de updateTax ====================

    @Test
    @DisplayName("Debe actualizar impuesto exitosamente")
    void testUpdateTaxSuccess() {
        // Arrange
        when(taxUpdateRestMapper.toDomain(taxUpdateReq)).thenReturn(taxDTO);
        when(taxUpdateInputPort.update(taxDTO, taxId)).thenReturn(tax);
        when(taxUpdateRestMapper.toCreateResponse(tax)).thenReturn(taxUpdateRes);

        // Act
        ResponseEntity<TaxUpdateRes> response = taxController.updateTax(taxId, taxUpdateReq);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(taxId, response.getBody().getId());
        verify(taxUpdateRestMapper).toDomain(taxUpdateReq);
        verify(taxUpdateInputPort).update(taxDTO, taxId);
        verify(taxUpdateRestMapper).toCreateResponse(tax);
    }

    @Test
    @DisplayName("Debe invocar mappers y servicios en orden correcto al actualizar")
    void testUpdateTaxInvokesInCorrectOrder() {
        // Arrange
        when(taxUpdateRestMapper.toDomain(taxUpdateReq)).thenReturn(taxDTO);
        when(taxUpdateInputPort.update(taxDTO, taxId)).thenReturn(tax);
        when(taxUpdateRestMapper.toCreateResponse(tax)).thenReturn(taxUpdateRes);

        // Act
        taxController.updateTax(taxId, taxUpdateReq);

        // Assert
        var inOrder = inOrder(taxUpdateRestMapper, taxUpdateInputPort);
        inOrder.verify(taxUpdateRestMapper).toDomain(taxUpdateReq);
        inOrder.verify(taxUpdateInputPort).update(taxDTO, taxId);
        inOrder.verify(taxUpdateRestMapper).toCreateResponse(tax);
    }

    // ==================== Tests de deleteByCode ====================

    @Test
    @DisplayName("Debe eliminar impuesto exitosamente y retornar NO_CONTENT")
    void testDeleteByCodeSuccess() {
        // Arrange
        when(taxDeleteInputPort.deleteByCode(taxId, idEnterprise)).thenReturn(true);

        // Act
        ResponseEntity<String> response = taxController.deleteByCode(taxId, idEnterprise);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(taxDeleteInputPort).deleteByCode(taxId, idEnterprise);
    }

    @Test
    @DisplayName("Debe retornar NOT_FOUND cuando el impuesto no existe")
    void testDeleteByCodeNotFound() {
        // Arrange
        when(taxDeleteInputPort.deleteByCode(taxId, idEnterprise)).thenReturn(false);

        // Act
        ResponseEntity<String> response = taxController.deleteByCode(taxId, idEnterprise);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().contains("Impuesto no encontrado"));
        verify(taxDeleteInputPort).deleteByCode(taxId, idEnterprise);
    }

    // ==================== Tests de changeState ====================

    @Test
    @DisplayName("Debe cambiar estado a activo exitosamente")
    void testChangeStateToActiveSuccess() {
        // Arrange
        Boolean newStatus = true;
        Tax activatedTax = Tax.builder()
                .id(taxId)
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19%")
                .status(true)
                .build();
        TaxChangeStateRes activeResponse = TaxChangeStateRes.builder()
                .id(taxId)
                .code("IVA19")
                .status(true)
                .message("Estado cambiado exitosamente")
                .build();
        when(taxChangeStateInputPort.changeState(taxId, idEnterprise, newStatus)).thenReturn(activatedTax);
        when(taxChangeStateRestMapper.toChangeStateResponse(activatedTax)).thenReturn(activeResponse);

        // Act
        ResponseEntity<TaxChangeStateRes> response = taxController.changeState(taxId, idEnterprise, newStatus);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getStatus());
        verify(taxChangeStateInputPort).changeState(taxId, idEnterprise, newStatus);
        verify(taxChangeStateRestMapper).toChangeStateResponse(activatedTax);
    }

    @Test
    @DisplayName("Debe cambiar estado a inactivo exitosamente")
    void testChangeStateToInactiveSuccess() {
        // Arrange
        Boolean newStatus = false;
        Tax deactivatedTax = Tax.builder()
                .id(taxId)
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19%")
                .status(false)
                .build();
        when(taxChangeStateInputPort.changeState(taxId, idEnterprise, newStatus)).thenReturn(deactivatedTax);
        when(taxChangeStateRestMapper.toChangeStateResponse(deactivatedTax)).thenReturn(taxChangeStateRes);

        // Act
        ResponseEntity<TaxChangeStateRes> response = taxController.changeState(taxId, idEnterprise, newStatus);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().getStatus());
        verify(taxChangeStateInputPort).changeState(taxId, idEnterprise, newStatus);
    }

    @Test
    @DisplayName("Debe invocar servicios y mappers en orden correcto al cambiar estado")
    void testChangeStateInvokesInCorrectOrder() {
        // Arrange
        Boolean newStatus = true;
        when(taxChangeStateInputPort.changeState(taxId, idEnterprise, newStatus)).thenReturn(tax);
        when(taxChangeStateRestMapper.toChangeStateResponse(tax)).thenReturn(taxChangeStateRes);

        // Act
        taxController.changeState(taxId, idEnterprise, newStatus);

        // Assert
        var inOrder = inOrder(taxChangeStateInputPort, taxChangeStateRestMapper);
        inOrder.verify(taxChangeStateInputPort).changeState(taxId, idEnterprise, newStatus);
        inOrder.verify(taxChangeStateRestMapper).toChangeStateResponse(tax);
    }

    // ==================== Tests adicionales de cobertura ====================

    @Test
    @DisplayName("Debe manejar diferentes empresas correctamente en getTax")
    void testGetTaxWithDifferentEnterprise() {
        // Arrange
        String differentEnterpriseId = "ENT-002";
        String code = "RET4";
        Tax differentTax = Tax.builder()
                .id(2L)
                .idEnterprise(differentEnterpriseId)
                .code(code)
                .description("Retención 4%")
                .interest(4.0)
                .status(true)
                .build();
        TaxSearchRes differentTaxRes = TaxSearchRes.builder()
                .id(2L)
                .idEnterprise(differentEnterpriseId)
                .code(code)
                .description("Retención 4%")
                .interest(4.0)
                .status(true)
                .build();
        when(taxSearchInputPort.getTax(code, differentEnterpriseId)).thenReturn(differentTax);
        when(taxSearchRestMapper.toSearchResponse(differentTax)).thenReturn(differentTaxRes);

        // Act
        ResponseEntity<TaxSearchRes> response = taxController.getTax(code, differentEnterpriseId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(differentEnterpriseId, response.getBody().getIdEnterprise());
    }

    @Test
    @DisplayName("Debe obtener múltiples impuestos activos")
    void testGetMultipleActiveTaxes() {
        // Arrange
        Tax tax2 = Tax.builder()
                .id(2L)
                .idEnterprise(idEnterprise)
                .code("RET4")
                .description("Retención 4%")
                .status(true)
                .build();
        TaxSearchRes taxSearchRes2 = TaxSearchRes.builder()
                .id(2L)
                .idEnterprise(idEnterprise)
                .code("RET4")
                .description("Retención 4%")
                .status(true)
                .build();
        List<Tax> activeTaxes = Arrays.asList(tax, tax2);
        List<TaxSearchRes> activeTaxesRes = Arrays.asList(taxSearchRes, taxSearchRes2);
        when(taxSearchInputPort.getActiveTaxes(idEnterprise)).thenReturn(activeTaxes);
        when(taxSearchRestMapper.toSearchListResponse(activeTaxes)).thenReturn(activeTaxesRes);

        // Act
        ResponseEntity<List<TaxSearchRes>> response = taxController.getActiveTaxes(idEnterprise);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.getBody().size());
    }

    @Test
    @DisplayName("Debe obtener página vacía cuando no hay impuestos")
    void testGetTaxesPaginatedReturnsEmptyPage() {
        // Arrange
        long totalRecords = 0L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Tax> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(taxSearchInputPort.countTaxesByEnterprise(idEnterprise)).thenReturn(totalRecords);
        when(paginationHelper.createFlexiblePageable(any(), any(), eq(totalRecords))).thenReturn(pageable);
        when(taxSearchInputPort.getTaxesPaginated(idEnterprise, 0, 10, "description", "asc")).thenReturn(emptyPage);

        // Act
        ResponseEntity<Page<TaxSearchRes>> response = taxController.getTaxesPaginated(
                idEnterprise, Optional.empty(), Optional.empty(), "description", "asc", null);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getContent().isEmpty());
    }
}

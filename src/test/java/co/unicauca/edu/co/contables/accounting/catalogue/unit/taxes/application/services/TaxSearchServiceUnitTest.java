package co.unicauca.edu.co.contables.accounting.catalogue.unit.taxes.application.services;

import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.taxes.TaxNotFoundException;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.output.ITaxSearchOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.services.TaxSearchService;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.services.TaxValidationService;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.models.Tax;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TaxSearchServiceUnitTest {

    @Mock
    private ITaxSearchOutputPort taxSearchOutputPort;

    @Mock
    private TaxValidationService taxValidationService;

    @InjectMocks
    private TaxSearchService taxSearchService;

    private String idEnterprise;
    private Tax tax1;
    private Tax tax2;

    @BeforeEach
    void setUp() {
        idEnterprise = "ENT-001";

        tax1 = Tax.builder()
                .id(1L)
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19%")
                .interest(19.0)
                .status(true)
                .usageCount(0)
                .build();

        tax2 = Tax.builder()
                .id(2L)
                .idEnterprise(idEnterprise)
                .code("RET4")
                .description("Retención 4%")
                .interest(4.0)
                .status(true)
                .usageCount(0)
                .build();
    }

    @Test
    @DisplayName("Debe obtener impuesto por código exitosamente")
    void testGetTaxByCodeSuccess() {
        // Arrange
        String code = "IVA19";
        doNothing().when(taxValidationService).validateTaxExists(code, idEnterprise);
        when(taxSearchOutputPort.getTax(code, idEnterprise)).thenReturn(tax1);

        // Act
        Tax result = taxSearchService.getTax(code, idEnterprise);

        // Assert
        assertNotNull(result);
        assertEquals(code, result.getCode());
        assertEquals(idEnterprise, result.getIdEnterprise());
        verify(taxValidationService).validateTaxExists(code, idEnterprise);
        verify(taxSearchOutputPort).getTax(code, idEnterprise);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el impuesto no existe por código")
    void testGetTaxByCodeThrowsExceptionWhenNotFound() {
        // Arrange
        String code = "NOEXISTE";
        doThrow(new TaxNotFoundException("No se encontró un impuesto con código '" + code + "'"))
                .when(taxValidationService).validateTaxExists(code, idEnterprise);

        // Act & Assert
        TaxNotFoundException exception = assertThrows(TaxNotFoundException.class,
                () -> taxSearchService.getTax(code, idEnterprise));

        assertTrue(exception.getMessage().contains(code));
        verify(taxValidationService).validateTaxExists(code, idEnterprise);
        verify(taxSearchOutputPort, never()).getTax(any(), any());
    }

    @Test
    @DisplayName("Debe obtener lista de impuestos activos")
    void testGetActiveTaxesSuccess() {
        // Arrange
        List<Tax> activeTaxes = Arrays.asList(tax1, tax2);
        when(taxSearchOutputPort.getActiveTaxes(idEnterprise)).thenReturn(activeTaxes);

        // Act
        List<Tax> result = taxSearchService.getActiveTaxes(idEnterprise);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(Tax::getStatus));
        verify(taxSearchOutputPort).getActiveTaxes(idEnterprise);
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay impuestos activos")
    void testGetActiveTaxesReturnsEmptyList() {
        // Arrange
        when(taxSearchOutputPort.getActiveTaxes(idEnterprise)).thenReturn(Collections.emptyList());

        // Act
        List<Tax> result = taxSearchService.getActiveTaxes(idEnterprise);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(taxSearchOutputPort).getActiveTaxes(idEnterprise);
    }

    @Test
    @DisplayName("Debe obtener impuestos paginados correctamente")
    void testGetTaxesPaginatedSuccess() {
        // Arrange
        int page = 0;
        int size = 10;
        String sortField = "code";
        String sortOrder = "asc";
        List<Tax> taxes = Arrays.asList(tax1, tax2);
        Page<Tax> expectedPage = new PageImpl<>(taxes);
        when(taxSearchOutputPort.getTaxesPaginated(idEnterprise, page, size, sortField, sortOrder))
                .thenReturn(expectedPage);

        // Act
        Page<Tax> result = taxSearchService.getTaxesPaginated(idEnterprise, page, size, sortField, sortOrder);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalElements());
        verify(taxSearchOutputPort).getTaxesPaginated(idEnterprise, page, size, sortField, sortOrder);
    }

    @Test
    @DisplayName("Debe obtener página vacía cuando no hay impuestos")
    void testGetTaxesPaginatedReturnsEmptyPage() {
        // Arrange
        int page = 0;
        int size = 10;
        String sortField = "code";
        String sortOrder = "asc";
        Page<Tax> emptyPage = new PageImpl<>(Collections.emptyList());
        when(taxSearchOutputPort.getTaxesPaginated(idEnterprise, page, size, sortField, sortOrder))
                .thenReturn(emptyPage);

        // Act
        Page<Tax> result = taxSearchService.getTaxesPaginated(idEnterprise, page, size, sortField, sortOrder);

        // Assert
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
        verify(taxSearchOutputPort).getTaxesPaginated(idEnterprise, page, size, sortField, sortOrder);
    }

    @Test
    @DisplayName("Debe obtener impuestos paginados con ordenamiento descendente")
    void testGetTaxesPaginatedWithDescOrder() {
        // Arrange
        int page = 0;
        int size = 5;
        String sortField = "description";
        String sortOrder = "desc";
        List<Tax> taxes = Arrays.asList(tax2, tax1);
        Page<Tax> expectedPage = new PageImpl<>(taxes);
        when(taxSearchOutputPort.getTaxesPaginated(idEnterprise, page, size, sortField, sortOrder))
                .thenReturn(expectedPage);

        // Act
        Page<Tax> result = taxSearchService.getTaxesPaginated(idEnterprise, page, size, sortField, sortOrder);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(taxSearchOutputPort).getTaxesPaginated(idEnterprise, page, size, sortField, sortOrder);
    }

    @Test
    @DisplayName("Debe filtrar impuestos por código o descripción paginados")
    void testGetTaxesByCodeOrDescriptionPaginatedSuccess() {
        // Arrange
        String search = "IVA";
        int page = 0;
        int size = 10;
        String sortField = "code";
        String sortOrder = "asc";
        List<Tax> filteredTaxes = Collections.singletonList(tax1);
        Page<Tax> expectedPage = new PageImpl<>(filteredTaxes);
        when(taxSearchOutputPort.getTaxesByCodeOrDescriptionPaginated(idEnterprise, search, page, size, sortField, sortOrder))
                .thenReturn(expectedPage);

        // Act
        Page<Tax> result = taxSearchService.getTaxesByCodeOrDescriptionPaginated(idEnterprise, search, page, size, sortField, sortOrder);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("IVA19", result.getContent().get(0).getCode());
        verify(taxSearchOutputPort).getTaxesByCodeOrDescriptionPaginated(idEnterprise, search, page, size, sortField, sortOrder);
    }

    @Test
    @DisplayName("Debe retornar página vacía cuando no hay coincidencias en búsqueda")
    void testGetTaxesByCodeOrDescriptionPaginatedNoMatches() {
        // Arrange
        String search = "NOEXISTE";
        int page = 0;
        int size = 10;
        String sortField = "code";
        String sortOrder = "asc";
        Page<Tax> emptyPage = new PageImpl<>(Collections.emptyList());
        when(taxSearchOutputPort.getTaxesByCodeOrDescriptionPaginated(idEnterprise, search, page, size, sortField, sortOrder))
                .thenReturn(emptyPage);

        // Act
        Page<Tax> result = taxSearchService.getTaxesByCodeOrDescriptionPaginated(idEnterprise, search, page, size, sortField, sortOrder);

        // Assert
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        verify(taxSearchOutputPort).getTaxesByCodeOrDescriptionPaginated(idEnterprise, search, page, size, sortField, sortOrder);
    }

    @Test
    @DisplayName("Debe contar total de impuestos por empresa")
    void testCountTaxesByEnterpriseSuccess() {
        // Arrange
        long expectedCount = 5L;
        when(taxSearchOutputPort.countTaxesByEnterprise(idEnterprise)).thenReturn(expectedCount);

        // Act
        long result = taxSearchService.countTaxesByEnterprise(idEnterprise);

        // Assert
        assertEquals(expectedCount, result);
        verify(taxSearchOutputPort).countTaxesByEnterprise(idEnterprise);
    }

    @Test
    @DisplayName("Debe retornar cero cuando no hay impuestos en la empresa")
    void testCountTaxesByEnterpriseReturnsZero() {
        // Arrange
        when(taxSearchOutputPort.countTaxesByEnterprise(idEnterprise)).thenReturn(0L);

        // Act
        long result = taxSearchService.countTaxesByEnterprise(idEnterprise);

        // Assert
        assertEquals(0L, result);
        verify(taxSearchOutputPort).countTaxesByEnterprise(idEnterprise);
    }

    @Test
    @DisplayName("Debe contar impuestos filtrados por código o descripción")
    void testCountTaxesByEnterpriseAndCodeOrDescriptionSuccess() {
        // Arrange
        String search = "Retención";
        long expectedCount = 3L;
        when(taxSearchOutputPort.countTaxesByEnterpriseAndCodeOrDescription(idEnterprise, search))
                .thenReturn(expectedCount);

        // Act
        long result = taxSearchService.countTaxesByEnterpriseAndCodeOrDescription(idEnterprise, search);

        // Assert
        assertEquals(expectedCount, result);
        verify(taxSearchOutputPort).countTaxesByEnterpriseAndCodeOrDescription(idEnterprise, search);
    }

    @Test
    @DisplayName("Debe retornar cero cuando no hay coincidencias en conteo filtrado")
    void testCountTaxesByEnterpriseAndCodeOrDescriptionReturnsZero() {
        // Arrange
        String search = "NOEXISTE";
        when(taxSearchOutputPort.countTaxesByEnterpriseAndCodeOrDescription(idEnterprise, search))
                .thenReturn(0L);

        // Act
        long result = taxSearchService.countTaxesByEnterpriseAndCodeOrDescription(idEnterprise, search);

        // Assert
        assertEquals(0L, result);
        verify(taxSearchOutputPort).countTaxesByEnterpriseAndCodeOrDescription(idEnterprise, search);
    }

    @Test
    @DisplayName("Debe manejar diferentes empresas en búsqueda por código")
    void testGetTaxWithDifferentEnterprise() {
        // Arrange
        String differentEnterpriseId = "ENT-002";
        String code = "IVA5";
        Tax taxFromDifferentEnterprise = Tax.builder()
                .id(3L)
                .idEnterprise(differentEnterpriseId)
                .code(code)
                .description("IVA 5%")
                .interest(5.0)
                .status(true)
                .build();
        doNothing().when(taxValidationService).validateTaxExists(code, differentEnterpriseId);
        when(taxSearchOutputPort.getTax(code, differentEnterpriseId)).thenReturn(taxFromDifferentEnterprise);

        // Act
        Tax result = taxSearchService.getTax(code, differentEnterpriseId);

        // Assert
        assertNotNull(result);
        assertEquals(differentEnterpriseId, result.getIdEnterprise());
        assertEquals(code, result.getCode());
        verify(taxValidationService).validateTaxExists(code, differentEnterpriseId);
        verify(taxSearchOutputPort).getTax(code, differentEnterpriseId);
    }

    @Test
    @DisplayName("Debe ejecutar validación antes de búsqueda por código")
    void testValidationExecutedBeforeSearch() {
        // Arrange
        String code = "IVA19";
        doNothing().when(taxValidationService).validateTaxExists(code, idEnterprise);
        when(taxSearchOutputPort.getTax(code, idEnterprise)).thenReturn(tax1);

        // Act
        taxSearchService.getTax(code, idEnterprise);

        // Assert
        var inOrder = inOrder(taxValidationService, taxSearchOutputPort);
        inOrder.verify(taxValidationService).validateTaxExists(code, idEnterprise);
        inOrder.verify(taxSearchOutputPort).getTax(code, idEnterprise);
    }

    @Test
    @DisplayName("Debe obtener impuestos activos de diferentes empresas sin interferencia")
    void testGetActiveTaxesFromDifferentEnterprises() {
        // Arrange
        String enterpriseA = "ENT-A";
        String enterpriseB = "ENT-B";
        List<Tax> taxesA = Collections.singletonList(tax1);
        List<Tax> taxesB = Collections.singletonList(tax2);
        when(taxSearchOutputPort.getActiveTaxes(enterpriseA)).thenReturn(taxesA);
        when(taxSearchOutputPort.getActiveTaxes(enterpriseB)).thenReturn(taxesB);

        // Act
        List<Tax> resultA = taxSearchService.getActiveTaxes(enterpriseA);
        List<Tax> resultB = taxSearchService.getActiveTaxes(enterpriseB);

        // Assert
        assertEquals(1, resultA.size());
        assertEquals(1, resultB.size());
        assertNotEquals(resultA.get(0).getCode(), resultB.get(0).getCode());
        verify(taxSearchOutputPort).getActiveTaxes(enterpriseA);
        verify(taxSearchOutputPort).getActiveTaxes(enterpriseB);
    }

    @Test
    @DisplayName("Debe manejar paginación en segunda página")
    void testGetTaxesPaginatedSecondPage() {
        // Arrange
        int page = 1;
        int size = 1;
        String sortField = "code";
        String sortOrder = "asc";
        List<Tax> secondPageTaxes = Collections.singletonList(tax2);
        Page<Tax> expectedPage = new PageImpl<>(secondPageTaxes);
        when(taxSearchOutputPort.getTaxesPaginated(idEnterprise, page, size, sortField, sortOrder))
                .thenReturn(expectedPage);

        // Act
        Page<Tax> result = taxSearchService.getTaxesPaginated(idEnterprise, page, size, sortField, sortOrder);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("RET4", result.getContent().get(0).getCode());
        verify(taxSearchOutputPort).getTaxesPaginated(idEnterprise, page, size, sortField, sortOrder);
    }
}

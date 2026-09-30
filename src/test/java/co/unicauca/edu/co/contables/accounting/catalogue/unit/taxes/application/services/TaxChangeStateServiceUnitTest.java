package co.unicauca.edu.co.contables.accounting.catalogue.unit.taxes.application.services;

import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.taxes.TaxNotFoundException;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.output.ITaxChangeStateOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.services.TaxChangeStateService;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TaxChangeStateServiceUnitTest {

    @Mock
    private ITaxChangeStateOutputPort taxChangeStateOutputPort;

    @Mock
    private TaxValidationService validationService;

    @InjectMocks
    private TaxChangeStateService taxChangeStateService;

    private Long taxId;
    private String idEnterprise;
    private Tax tax;

    @BeforeEach
    void setUp() {
        taxId = 1L;
        idEnterprise = "ENT-001";

        tax = Tax.builder()
                .id(taxId)
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19%")
                .interest(19.0)
                .status(true)
                .build();
    }

    @Test
    @DisplayName("Debe activar impuesto exitosamente cuando existe")
    void testChangeStateToActiveSuccess() {
        Boolean newStatus = true;
        Tax expectedTax = Tax.builder()
                .id(taxId)
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19%")
                .interest(19.0)
                .status(true)
                .build();
        doNothing().when(validationService).validateTaxExists(taxId, idEnterprise);
        when(taxChangeStateOutputPort.changeState(taxId, idEnterprise, newStatus)).thenReturn(expectedTax);

        Tax result = taxChangeStateService.changeState(taxId, idEnterprise, newStatus);

        assertNotNull(result);
        assertEquals(taxId, result.getId());
        assertEquals(idEnterprise, result.getIdEnterprise());
        assertTrue(result.getStatus());
        verify(validationService).validateTaxExists(taxId, idEnterprise);
        verify(taxChangeStateOutputPort).changeState(taxId, idEnterprise, newStatus);
    }

    @Test
    @DisplayName("Debe desactivar impuesto exitosamente cuando existe")
    void testChangeStateToInactiveSuccess() {
        Boolean newStatus = false;
        Tax expectedTax = Tax.builder()
                .id(taxId)
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19%")
                .interest(19.0)
                .status(false)
                .build();
        doNothing().when(validationService).validateTaxExists(taxId, idEnterprise);
        when(taxChangeStateOutputPort.changeState(taxId, idEnterprise, newStatus)).thenReturn(expectedTax);

        Tax result = taxChangeStateService.changeState(taxId, idEnterprise, newStatus);

        assertNotNull(result);
        assertEquals(taxId, result.getId());
        assertFalse(result.getStatus());
        verify(validationService).validateTaxExists(taxId, idEnterprise);
        verify(taxChangeStateOutputPort).changeState(taxId, idEnterprise, newStatus);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el impuesto no existe")
    void testChangeStateThrowsExceptionWhenTaxNotFound() {
        Boolean newStatus = true;
        doThrow(new TaxNotFoundException("No se encontró un impuesto con ID '" + taxId + "'"))
                .when(validationService).validateTaxExists(taxId, idEnterprise);

        TaxNotFoundException exception = assertThrows(TaxNotFoundException.class,
                () -> taxChangeStateService.changeState(taxId, idEnterprise, newStatus));

        assertTrue(exception.getMessage().contains(String.valueOf(taxId)));
        verify(validationService).validateTaxExists(taxId, idEnterprise);
        verify(taxChangeStateOutputPort, never()).changeState(any(), any(), any());
    }

    @Test
    @DisplayName("Debe llamar a validación antes de cambiar estado")
    void testValidationIsCalledBeforeStateChange() {
        Boolean newStatus = true;
        doNothing().when(validationService).validateTaxExists(taxId, idEnterprise);
        when(taxChangeStateOutputPort.changeState(taxId, idEnterprise, newStatus)).thenReturn(tax);

        taxChangeStateService.changeState(taxId, idEnterprise, newStatus);

        var inOrder = inOrder(validationService, taxChangeStateOutputPort);
        inOrder.verify(validationService).validateTaxExists(taxId, idEnterprise);
        inOrder.verify(taxChangeStateOutputPort).changeState(taxId, idEnterprise, newStatus);
    }

    @Test
    @DisplayName("Debe manejar diferentes IDs de empresa correctamente")
    void testChangeStateWithDifferentEnterpriseId() {
        String differentEnterpriseId = "ENT-002";
        Long differentTaxId = 5L;
        Boolean newStatus = false;
        Tax differentTax = Tax.builder()
                .id(differentTaxId)
                .idEnterprise(differentEnterpriseId)
                .code("RET4")
                .description("Retención 4%")
                .interest(4.0)
                .status(false)
                .build();
        doNothing().when(validationService).validateTaxExists(differentTaxId, differentEnterpriseId);
        when(taxChangeStateOutputPort.changeState(differentTaxId, differentEnterpriseId, newStatus))
                .thenReturn(differentTax);

        Tax result = taxChangeStateService.changeState(differentTaxId, differentEnterpriseId, newStatus);

        assertNotNull(result);
        assertEquals(differentTaxId, result.getId());
        assertEquals(differentEnterpriseId, result.getIdEnterprise());
        verify(validationService).validateTaxExists(differentTaxId, differentEnterpriseId);
        verify(taxChangeStateOutputPort).changeState(differentTaxId, differentEnterpriseId, newStatus);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando impuesto no pertenece a la empresa")
    void testChangeStateThrowsExceptionWhenTaxNotBelongsToEnterprise() {
        String wrongEnterpriseId = "ENT-999";
        Boolean newStatus = true;
        doThrow(new TaxNotFoundException("No se encontró un impuesto con ID '" + taxId + "'"))
                .when(validationService).validateTaxExists(taxId, wrongEnterpriseId);

        TaxNotFoundException exception = assertThrows(TaxNotFoundException.class,
                () -> taxChangeStateService.changeState(taxId, wrongEnterpriseId, newStatus));

        assertNotNull(exception);
        verify(validationService).validateTaxExists(taxId, wrongEnterpriseId);
        verify(taxChangeStateOutputPort, never()).changeState(any(), any(), any());
    }

    @Test
    @DisplayName("Debe propagar correctamente el estado null al puerto de salida")
    void testChangeStateWithNullStatus() {
        Boolean nullStatus = null;
        Tax expectedTax = Tax.builder()
                .id(taxId)
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19%")
                .interest(19.0)
                .status(null)
                .build();
        doNothing().when(validationService).validateTaxExists(taxId, idEnterprise);
        when(taxChangeStateOutputPort.changeState(taxId, idEnterprise, nullStatus)).thenReturn(expectedTax);

        Tax result = taxChangeStateService.changeState(taxId, idEnterprise, nullStatus);

        assertNotNull(result);
        assertNull(result.getStatus());
        verify(taxChangeStateOutputPort).changeState(taxId, idEnterprise, nullStatus);
    }
}

package co.unicauca.edu.co.contables.accounting.catalogue.unit.taxes.application.services;

import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.taxes.TaxInUseException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.taxes.TaxNotFoundException;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.output.ITaxDeleteOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.output.ITaxSearchOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.services.TaxDeleteService;
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
class TaxDeleteServiceUnitTest {

    @Mock
    private ITaxDeleteOutputPort taxDeleteOutputPort;

    @Mock
    private ITaxSearchOutputPort taxSearchOutputPort;

    @Mock
    private TaxValidationService taxValidationService;

    @InjectMocks
    private TaxDeleteService taxDeleteService;

    private Long taxId;
    private String idEnterprise;
    private Tax taxWithoutUsage;
    private Tax taxWithUsage;

    @BeforeEach
    void setUp() {
        taxId = 1L;
        idEnterprise = "ENT-001";

        taxWithoutUsage = Tax.builder()
                .id(taxId)
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19%")
                .interest(19.0)
                .status(true)
                .usageCount(0)
                .build();

        taxWithUsage = Tax.builder()
                .id(taxId)
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19%")
                .interest(19.0)
                .status(true)
                .usageCount(5)
                .build();
    }

    @Test
    @DisplayName("Debe eliminar impuesto exitosamente cuando existe y no tiene uso")
    void testDeleteByCodeSuccess() {
        doNothing().when(taxValidationService).validateTaxExists(taxId, idEnterprise);
        when(taxSearchOutputPort.getTaxByIdAndEnterprise(taxId, idEnterprise)).thenReturn(taxWithoutUsage);
        when(taxDeleteOutputPort.deleteByCode(taxId, idEnterprise)).thenReturn(true);

        boolean result = taxDeleteService.deleteByCode(taxId, idEnterprise);

        assertTrue(result);
        verify(taxValidationService).validateTaxExists(taxId, idEnterprise);
        verify(taxSearchOutputPort).getTaxByIdAndEnterprise(taxId, idEnterprise);
        verify(taxDeleteOutputPort).deleteByCode(taxId, idEnterprise);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el impuesto no existe")
    void testDeleteByCodeThrowsExceptionWhenTaxNotFound() {
        doThrow(new TaxNotFoundException("No se encontró un impuesto con ID '" + taxId + "'"))
                .when(taxValidationService).validateTaxExists(taxId, idEnterprise);

        TaxNotFoundException exception = assertThrows(TaxNotFoundException.class,
                () -> taxDeleteService.deleteByCode(taxId, idEnterprise));

        assertTrue(exception.getMessage().contains(String.valueOf(taxId)));
        verify(taxValidationService).validateTaxExists(taxId, idEnterprise);
        verify(taxSearchOutputPort, never()).getTaxByIdAndEnterprise(any(), any());
        verify(taxDeleteOutputPort, never()).deleteByCode(anyLong(), any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el impuesto tiene movimientos contables")
    void testDeleteByCodeThrowsExceptionWhenTaxInUse() {
        doNothing().when(taxValidationService).validateTaxExists(taxId, idEnterprise);
        when(taxSearchOutputPort.getTaxByIdAndEnterprise(taxId, idEnterprise)).thenReturn(taxWithUsage);

        TaxInUseException exception = assertThrows(TaxInUseException.class,
                () -> taxDeleteService.deleteByCode(taxId, idEnterprise));

        assertTrue(exception.getMessage().contains("eliminar"));
        assertTrue(exception.getMessage().contains(taxWithUsage.getCode()));
        verify(taxValidationService).validateTaxExists(taxId, idEnterprise);
        verify(taxSearchOutputPort).getTaxByIdAndEnterprise(taxId, idEnterprise);
        verify(taxDeleteOutputPort, never()).deleteByCode(anyLong(), any());
    }

    @Test
    @DisplayName("Debe ejecutar validaciones en el orden correcto")
    void testValidationsExecutedInCorrectOrder() {
        doNothing().when(taxValidationService).validateTaxExists(taxId, idEnterprise);
        when(taxSearchOutputPort.getTaxByIdAndEnterprise(taxId, idEnterprise)).thenReturn(taxWithoutUsage);
        when(taxDeleteOutputPort.deleteByCode(taxId, idEnterprise)).thenReturn(true);

        taxDeleteService.deleteByCode(taxId, idEnterprise);

        var inOrder = inOrder(taxValidationService, taxSearchOutputPort, taxDeleteOutputPort);
        inOrder.verify(taxValidationService).validateTaxExists(taxId, idEnterprise);
        inOrder.verify(taxSearchOutputPort).getTaxByIdAndEnterprise(taxId, idEnterprise);
        inOrder.verify(taxDeleteOutputPort).deleteByCode(taxId, idEnterprise);
    }

    @Test
    @DisplayName("Debe retornar false cuando el puerto de salida retorna false")
    void testDeleteByCodeReturnsFalseWhenOutputPortReturnsFalse() {
        doNothing().when(taxValidationService).validateTaxExists(taxId, idEnterprise);
        when(taxSearchOutputPort.getTaxByIdAndEnterprise(taxId, idEnterprise)).thenReturn(taxWithoutUsage);
        when(taxDeleteOutputPort.deleteByCode(taxId, idEnterprise)).thenReturn(false);

        boolean result = taxDeleteService.deleteByCode(taxId, idEnterprise);

        assertFalse(result);
        verify(taxDeleteOutputPort).deleteByCode(taxId, idEnterprise);
    }

    @Test
    @DisplayName("Debe manejar diferentes IDs de empresa correctamente")
    void testDeleteByCodeWithDifferentEnterpriseId() {
        String differentEnterpriseId = "ENT-002";
        Long differentTaxId = 5L;
        Tax differentTax = Tax.builder()
                .id(differentTaxId)
                .idEnterprise(differentEnterpriseId)
                .code("RET4")
                .description("Retención 4%")
                .interest(4.0)
                .status(true)
                .usageCount(0)
                .build();
        doNothing().when(taxValidationService).validateTaxExists(differentTaxId, differentEnterpriseId);
        when(taxSearchOutputPort.getTaxByIdAndEnterprise(differentTaxId, differentEnterpriseId)).thenReturn(differentTax);
        when(taxDeleteOutputPort.deleteByCode(differentTaxId, differentEnterpriseId)).thenReturn(true);

        boolean result = taxDeleteService.deleteByCode(differentTaxId, differentEnterpriseId);

        assertTrue(result);
        verify(taxValidationService).validateTaxExists(differentTaxId, differentEnterpriseId);
        verify(taxSearchOutputPort).getTaxByIdAndEnterprise(differentTaxId, differentEnterpriseId);
        verify(taxDeleteOutputPort).deleteByCode(differentTaxId, differentEnterpriseId);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando impuesto no pertenece a la empresa")
    void testDeleteByCodeThrowsExceptionWhenTaxNotBelongsToEnterprise() {
        String wrongEnterpriseId = "ENT-999";
        doThrow(new TaxNotFoundException("No se encontró un impuesto con ID '" + taxId + "'"))
                .when(taxValidationService).validateTaxExists(taxId, wrongEnterpriseId);

        TaxNotFoundException exception = assertThrows(TaxNotFoundException.class,
                () -> taxDeleteService.deleteByCode(taxId, wrongEnterpriseId));

        assertNotNull(exception);
        verify(taxValidationService).validateTaxExists(taxId, wrongEnterpriseId);
        verify(taxSearchOutputPort, never()).getTaxByIdAndEnterprise(any(), any());
        verify(taxDeleteOutputPort, never()).deleteByCode(anyLong(), any());
    }

    @Test
    @DisplayName("Debe eliminar impuesto con usageCount null (sin uso)")
    void testDeleteByCodeWithNullUsageCount() {
        Tax taxWithNullUsage = Tax.builder()
                .id(taxId)
                .idEnterprise(idEnterprise)
                .code("ICA")
                .description("Impuesto de industria y comercio")
                .interest(1.5)
                .status(true)
                .usageCount(null)
                .build();
        doNothing().when(taxValidationService).validateTaxExists(taxId, idEnterprise);
        when(taxSearchOutputPort.getTaxByIdAndEnterprise(taxId, idEnterprise)).thenReturn(taxWithNullUsage);
        when(taxDeleteOutputPort.deleteByCode(taxId, idEnterprise)).thenReturn(true);

        boolean result = taxDeleteService.deleteByCode(taxId, idEnterprise);

        assertTrue(result);
        verify(taxDeleteOutputPort).deleteByCode(taxId, idEnterprise);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando impuesto tiene exactamente un uso")
    void testDeleteByCodeThrowsExceptionWhenTaxHasOneUsage() {
        Tax taxWithOneUsage = Tax.builder()
                .id(taxId)
                .idEnterprise(idEnterprise)
                .code("TASA")
                .description("Tasa especial")
                .interest(2.0)
                .status(true)
                .usageCount(1)
                .build();
        doNothing().when(taxValidationService).validateTaxExists(taxId, idEnterprise);
        when(taxSearchOutputPort.getTaxByIdAndEnterprise(taxId, idEnterprise)).thenReturn(taxWithOneUsage);

        TaxInUseException exception = assertThrows(TaxInUseException.class,
                () -> taxDeleteService.deleteByCode(taxId, idEnterprise));

        assertTrue(exception.getMessage().contains("eliminar"));
        verify(taxDeleteOutputPort, never()).deleteByCode(anyLong(), any());
    }

    @Test
    @DisplayName("Debe eliminar impuesto inactivo sin uso")
    void testDeleteByCodeWithInactiveTaxWithoutUsage() {
        Tax inactiveTax = Tax.builder()
                .id(taxId)
                .idEnterprise(idEnterprise)
                .code("OLD_TAX")
                .description("Impuesto antiguo")
                .interest(10.0)
                .status(false)
                .usageCount(0)
                .build();
        doNothing().when(taxValidationService).validateTaxExists(taxId, idEnterprise);
        when(taxSearchOutputPort.getTaxByIdAndEnterprise(taxId, idEnterprise)).thenReturn(inactiveTax);
        when(taxDeleteOutputPort.deleteByCode(taxId, idEnterprise)).thenReturn(true);

        boolean result = taxDeleteService.deleteByCode(taxId, idEnterprise);

        assertTrue(result);
        verify(taxDeleteOutputPort).deleteByCode(taxId, idEnterprise);
    }

    @Test
    @DisplayName("Debe lanzar excepción para impuesto inactivo con uso")
    void testDeleteByCodeThrowsExceptionWhenInactiveTaxHasUsage() {
        Tax inactiveTaxWithUsage = Tax.builder()
                .id(taxId)
                .idEnterprise(idEnterprise)
                .code("OLD_USED")
                .description("Impuesto antiguo con uso")
                .interest(8.0)
                .status(false)
                .usageCount(10)
                .build();
        doNothing().when(taxValidationService).validateTaxExists(taxId, idEnterprise);
        when(taxSearchOutputPort.getTaxByIdAndEnterprise(taxId, idEnterprise)).thenReturn(inactiveTaxWithUsage);

        TaxInUseException exception = assertThrows(TaxInUseException.class,
                () -> taxDeleteService.deleteByCode(taxId, idEnterprise));

        assertTrue(exception.getMessage().contains("eliminar"));
        assertTrue(exception.getMessage().contains(inactiveTaxWithUsage.getCode()));
        verify(taxDeleteOutputPort, never()).deleteByCode(anyLong(), any());
    }
}

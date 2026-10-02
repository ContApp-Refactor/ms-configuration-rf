package co.unicauca.edu.co.contables.accounting.catalogue.unit.taxes.application.services;

import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.AccountCatalogueInactiveException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.AccountCatalogueNotFoundException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.taxes.DuplicateTaxAccountsException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.taxes.InvalidAccountDigitsException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.taxes.TaxAlreadyExistsException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.taxes.TaxInUseException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.taxes.TaxNotFoundException;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.output.ITaxSearchOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.output.ITaxUpdateOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.services.TaxUpdateService;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.services.TaxValidationService;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.DTO.TaxDTO;
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
class TaxUpdateServiceUnitTest {

    @Mock
    private ITaxUpdateOutputPort taxUpdateOutputPort;

    @Mock
    private ITaxSearchOutputPort taxSearchOutputPort;

    @Mock
    private TaxValidationService taxValidationService;

    @InjectMocks
    private TaxUpdateService taxUpdateService;

    private Long taxId;
    private String idEnterprise;
    private TaxDTO taxDTO;
    private Tax existingTax;
    private Tax updatedTax;

    @BeforeEach
    void setUp() {
        taxId = 1L;
        idEnterprise = "ENT-001";

        taxDTO = TaxDTO.builder()
                .id(taxId)
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19% actualizado")
                .interest(19.0)
                .salesTaxId(100L)
                .purchaseTaxId(200L)
                .build();

        existingTax = Tax.builder()
                .id(taxId)
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19%")
                .interest(19.0)
                .status(true)
                .usageCount(0)
                .build();

        updatedTax = Tax.builder()
                .id(taxId)
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19% actualizado")
                .interest(19.0)
                .status(true)
                .usageCount(0)
                .build();
    }

    @Test
    @DisplayName("Debe actualizar impuesto exitosamente con todas las validaciones pasando")
    void testUpdateTaxSuccess() {
        // Arrange
        doNothing().when(taxValidationService).validateTaxExists(taxId, idEnterprise);
        when(taxSearchOutputPort.getTaxByIdAndEnterprise(taxId, idEnterprise)).thenReturn(existingTax);
        doNothing().when(taxValidationService).validateTaxCodeNotExistsExcludingId(taxDTO.getCode(), idEnterprise, taxId);
        doNothing().when(taxValidationService).validateAccountDigits(taxDTO.getSalesTaxId(), taxDTO.getPurchaseTaxId(), idEnterprise);
        doNothing().when(taxValidationService).validateDifferentTaxAccounts(taxDTO.getSalesTaxId(), taxDTO.getPurchaseTaxId());
        when(taxUpdateOutputPort.update(taxDTO, taxId)).thenReturn(updatedTax);

        // Act
        Tax result = taxUpdateService.update(taxDTO, taxId);

        // Assert
        assertNotNull(result);
        assertEquals(taxId, result.getId());
        assertEquals("Impuesto al valor agregado 19% actualizado", result.getDescription());
        verify(taxValidationService).validateTaxExists(taxId, idEnterprise);
        verify(taxSearchOutputPort).getTaxByIdAndEnterprise(taxId, idEnterprise);
        verify(taxValidationService).validateTaxCodeNotExistsExcludingId(taxDTO.getCode(), idEnterprise, taxId);
        verify(taxValidationService).validateAccountDigits(taxDTO.getSalesTaxId(), taxDTO.getPurchaseTaxId(), idEnterprise);
        verify(taxValidationService).validateDifferentTaxAccounts(taxDTO.getSalesTaxId(), taxDTO.getPurchaseTaxId());
        verify(taxUpdateOutputPort).update(taxDTO, taxId);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el impuesto no existe")
    void testUpdateThrowsExceptionWhenTaxNotFound() {
        // Arrange
        doThrow(new TaxNotFoundException("No se encontró un impuesto con ID '" + taxId + "'"))
                .when(taxValidationService).validateTaxExists(taxId, idEnterprise);

        // Act & Assert
        TaxNotFoundException exception = assertThrows(TaxNotFoundException.class,
                () -> taxUpdateService.update(taxDTO, taxId));

        assertTrue(exception.getMessage().contains(String.valueOf(taxId)));
        verify(taxValidationService).validateTaxExists(taxId, idEnterprise);
        verify(taxSearchOutputPort, never()).getTaxByIdAndEnterprise(any(), any());
        verify(taxUpdateOutputPort, never()).update(any(), anyLong());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el impuesto tiene movimientos contables")
    void testUpdateThrowsExceptionWhenTaxInUse() {
        // Arrange
        Tax taxWithUsage = Tax.builder()
                .id(taxId)
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19%")
                .interest(19.0)
                .status(true)
                .usageCount(5)
                .build();
        doNothing().when(taxValidationService).validateTaxExists(taxId, idEnterprise);
        when(taxSearchOutputPort.getTaxByIdAndEnterprise(taxId, idEnterprise)).thenReturn(taxWithUsage);

        // Act & Assert
        TaxInUseException exception = assertThrows(TaxInUseException.class,
                () -> taxUpdateService.update(taxDTO, taxId));

        assertTrue(exception.getMessage().contains("editar"));
        assertTrue(exception.getMessage().contains(taxWithUsage.getCode()));
        verify(taxValidationService).validateTaxExists(taxId, idEnterprise);
        verify(taxSearchOutputPort).getTaxByIdAndEnterprise(taxId, idEnterprise);
        verify(taxValidationService, never()).validateTaxCodeNotExistsExcludingId(any(), any(), anyLong());
        verify(taxUpdateOutputPort, never()).update(any(), anyLong());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el nuevo código ya existe en otro impuesto")
    void testUpdateThrowsExceptionWhenCodeAlreadyExists() {
        // Arrange
        TaxDTO dtoWithNewCode = TaxDTO.builder()
                .id(taxId)
                .idEnterprise(idEnterprise)
                .code("IVA5")
                .description("Cambio a IVA 5%")
                .interest(5.0)
                .salesTaxId(100L)
                .purchaseTaxId(200L)
                .build();
        doNothing().when(taxValidationService).validateTaxExists(taxId, idEnterprise);
        when(taxSearchOutputPort.getTaxByIdAndEnterprise(taxId, idEnterprise)).thenReturn(existingTax);
        doThrow(new TaxAlreadyExistsException("Ya existe un impuesto con código 'IVA5'"))
                .when(taxValidationService).validateTaxCodeNotExistsExcludingId(dtoWithNewCode.getCode(), idEnterprise, taxId);

        // Act & Assert
        TaxAlreadyExistsException exception = assertThrows(TaxAlreadyExistsException.class,
                () -> taxUpdateService.update(dtoWithNewCode, taxId));

        assertTrue(exception.getMessage().contains("IVA5"));
        verify(taxValidationService).validateTaxCodeNotExistsExcludingId(dtoWithNewCode.getCode(), idEnterprise, taxId);
        verify(taxValidationService, never()).validateAccountDigits(any(), any(), any());
        verify(taxUpdateOutputPort, never()).update(any(), anyLong());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la cuenta de impuesto de ventas no existe")
    void testUpdateThrowsExceptionWhenSalesAccountNotFound() {
        // Arrange
        doNothing().when(taxValidationService).validateTaxExists(taxId, idEnterprise);
        when(taxSearchOutputPort.getTaxByIdAndEnterprise(taxId, idEnterprise)).thenReturn(existingTax);
        doNothing().when(taxValidationService).validateTaxCodeNotExistsExcludingId(taxDTO.getCode(), idEnterprise, taxId);
        doThrow(new AccountCatalogueNotFoundException("La cuenta especificada no existe"))
                .when(taxValidationService).validateAccountDigits(taxDTO.getSalesTaxId(), taxDTO.getPurchaseTaxId(), idEnterprise);

        // Act & Assert
        AccountCatalogueNotFoundException exception = assertThrows(AccountCatalogueNotFoundException.class,
                () -> taxUpdateService.update(taxDTO, taxId));

        assertTrue(exception.getMessage().contains("cuenta especificada no existe"));
        verify(taxValidationService).validateAccountDigits(taxDTO.getSalesTaxId(), taxDTO.getPurchaseTaxId(), idEnterprise);
        verify(taxValidationService, never()).validateDifferentTaxAccounts(any(), any());
        verify(taxUpdateOutputPort, never()).update(any(), anyLong());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la cuenta de impuesto está inactiva")
    void testUpdateThrowsExceptionWhenAccountIsInactive() {
        // Arrange
        doNothing().when(taxValidationService).validateTaxExists(taxId, idEnterprise);
        when(taxSearchOutputPort.getTaxByIdAndEnterprise(taxId, idEnterprise)).thenReturn(existingTax);
        doNothing().when(taxValidationService).validateTaxCodeNotExistsExcludingId(taxDTO.getCode(), idEnterprise, taxId);
        doThrow(new AccountCatalogueInactiveException("La cuenta '24080501' está inactiva"))
                .when(taxValidationService).validateAccountDigits(taxDTO.getSalesTaxId(), taxDTO.getPurchaseTaxId(), idEnterprise);

        // Act & Assert
        AccountCatalogueInactiveException exception = assertThrows(AccountCatalogueInactiveException.class,
                () -> taxUpdateService.update(taxDTO, taxId));

        assertTrue(exception.getMessage().contains("inactiva"));
        verify(taxUpdateOutputPort, never()).update(any(), anyLong());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la cuenta no tiene 8 dígitos")
    void testUpdateThrowsExceptionWhenAccountDigitsInvalid() {
        // Arrange
        doNothing().when(taxValidationService).validateTaxExists(taxId, idEnterprise);
        when(taxSearchOutputPort.getTaxByIdAndEnterprise(taxId, idEnterprise)).thenReturn(existingTax);
        doNothing().when(taxValidationService).validateTaxCodeNotExistsExcludingId(taxDTO.getCode(), idEnterprise, taxId);
        doThrow(new InvalidAccountDigitsException())
                .when(taxValidationService).validateAccountDigits(taxDTO.getSalesTaxId(), taxDTO.getPurchaseTaxId(), idEnterprise);

        // Act & Assert
        assertThrows(InvalidAccountDigitsException.class,
                () -> taxUpdateService.update(taxDTO, taxId));

        verify(taxUpdateOutputPort, never()).update(any(), anyLong());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando las cuentas de venta y compra son iguales")
    void testUpdateThrowsExceptionWhenSameAccounts() {
        // Arrange
        TaxDTO dtoWithSameAccounts = TaxDTO.builder()
                .id(taxId)
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto con mismas cuentas")
                .interest(19.0)
                .salesTaxId(100L)
                .purchaseTaxId(100L)
                .build();
        doNothing().when(taxValidationService).validateTaxExists(taxId, idEnterprise);
        when(taxSearchOutputPort.getTaxByIdAndEnterprise(taxId, idEnterprise)).thenReturn(existingTax);
        doNothing().when(taxValidationService).validateTaxCodeNotExistsExcludingId(dtoWithSameAccounts.getCode(), idEnterprise, taxId);
        doNothing().when(taxValidationService).validateAccountDigits(dtoWithSameAccounts.getSalesTaxId(), dtoWithSameAccounts.getPurchaseTaxId(), idEnterprise);
        doThrow(new DuplicateTaxAccountsException("Las cuentas de impuesto de venta e impuesto de compra deben ser diferentes"))
                .when(taxValidationService).validateDifferentTaxAccounts(dtoWithSameAccounts.getSalesTaxId(), dtoWithSameAccounts.getPurchaseTaxId());

        // Act & Assert
        DuplicateTaxAccountsException exception = assertThrows(DuplicateTaxAccountsException.class,
                () -> taxUpdateService.update(dtoWithSameAccounts, taxId));

        assertTrue(exception.getMessage().contains("diferentes"));
        verify(taxValidationService).validateDifferentTaxAccounts(dtoWithSameAccounts.getSalesTaxId(), dtoWithSameAccounts.getPurchaseTaxId());
        verify(taxUpdateOutputPort, never()).update(any(), anyLong());
    }

    @Test
    @DisplayName("Debe ejecutar validaciones en el orden correcto")
    void testValidationsExecutedInCorrectOrder() {
        // Arrange
        doNothing().when(taxValidationService).validateTaxExists(taxId, idEnterprise);
        when(taxSearchOutputPort.getTaxByIdAndEnterprise(taxId, idEnterprise)).thenReturn(existingTax);
        doNothing().when(taxValidationService).validateTaxCodeNotExistsExcludingId(taxDTO.getCode(), idEnterprise, taxId);
        doNothing().when(taxValidationService).validateAccountDigits(taxDTO.getSalesTaxId(), taxDTO.getPurchaseTaxId(), idEnterprise);
        doNothing().when(taxValidationService).validateDifferentTaxAccounts(taxDTO.getSalesTaxId(), taxDTO.getPurchaseTaxId());
        when(taxUpdateOutputPort.update(taxDTO, taxId)).thenReturn(updatedTax);

        // Act
        taxUpdateService.update(taxDTO, taxId);

        // Assert
        var inOrder = inOrder(taxValidationService, taxSearchOutputPort, taxUpdateOutputPort);
        inOrder.verify(taxValidationService).validateTaxExists(taxId, idEnterprise);
        inOrder.verify(taxSearchOutputPort).getTaxByIdAndEnterprise(taxId, idEnterprise);
        inOrder.verify(taxValidationService).validateTaxCodeNotExistsExcludingId(taxDTO.getCode(), idEnterprise, taxId);
        inOrder.verify(taxValidationService).validateAccountDigits(taxDTO.getSalesTaxId(), taxDTO.getPurchaseTaxId(), idEnterprise);
        inOrder.verify(taxValidationService).validateDifferentTaxAccounts(taxDTO.getSalesTaxId(), taxDTO.getPurchaseTaxId());
        inOrder.verify(taxUpdateOutputPort).update(taxDTO, taxId);
    }

    @Test
    @DisplayName("Debe actualizar impuesto sin cuenta de ventas (salesTaxId null)")
    void testUpdateTaxWithNullSalesTaxId() {
        // Arrange
        TaxDTO dtoWithoutSales = TaxDTO.builder()
                .id(taxId)
                .idEnterprise(idEnterprise)
                .code("RET4")
                .description("Retención 4%")
                .interest(4.0)
                .salesTaxId(null)
                .purchaseTaxId(200L)
                .build();
        Tax updatedTaxWithoutSales = Tax.builder()
                .id(taxId)
                .idEnterprise(idEnterprise)
                .code("RET4")
                .description("Retención 4%")
                .interest(4.0)
                .status(true)
                .build();
        doNothing().when(taxValidationService).validateTaxExists(taxId, idEnterprise);
        when(taxSearchOutputPort.getTaxByIdAndEnterprise(taxId, idEnterprise)).thenReturn(existingTax);
        doNothing().when(taxValidationService).validateTaxCodeNotExistsExcludingId(dtoWithoutSales.getCode(), idEnterprise, taxId);
        doNothing().when(taxValidationService).validateAccountDigits(null, dtoWithoutSales.getPurchaseTaxId(), idEnterprise);
        doNothing().when(taxValidationService).validateDifferentTaxAccounts(null, dtoWithoutSales.getPurchaseTaxId());
        when(taxUpdateOutputPort.update(dtoWithoutSales, taxId)).thenReturn(updatedTaxWithoutSales);

        // Act
        Tax result = taxUpdateService.update(dtoWithoutSales, taxId);

        // Assert
        assertNotNull(result);
        assertEquals("RET4", result.getCode());
        verify(taxUpdateOutputPort).update(dtoWithoutSales, taxId);
    }

    @Test
    @DisplayName("Debe actualizar impuesto sin cuenta de compras (purchaseTaxId null)")
    void testUpdateTaxWithNullPurchaseTaxId() {
        // Arrange
        TaxDTO dtoWithoutPurchase = TaxDTO.builder()
                .id(taxId)
                .idEnterprise(idEnterprise)
                .code("ICA")
                .description("Impuesto de industria y comercio")
                .interest(1.5)
                .salesTaxId(100L)
                .purchaseTaxId(null)
                .build();
        Tax updatedTaxWithoutPurchase = Tax.builder()
                .id(taxId)
                .idEnterprise(idEnterprise)
                .code("ICA")
                .description("Impuesto de industria y comercio")
                .interest(1.5)
                .status(true)
                .build();
        doNothing().when(taxValidationService).validateTaxExists(taxId, idEnterprise);
        when(taxSearchOutputPort.getTaxByIdAndEnterprise(taxId, idEnterprise)).thenReturn(existingTax);
        doNothing().when(taxValidationService).validateTaxCodeNotExistsExcludingId(dtoWithoutPurchase.getCode(), idEnterprise, taxId);
        doNothing().when(taxValidationService).validateAccountDigits(dtoWithoutPurchase.getSalesTaxId(), null, idEnterprise);
        doNothing().when(taxValidationService).validateDifferentTaxAccounts(dtoWithoutPurchase.getSalesTaxId(), null);
        when(taxUpdateOutputPort.update(dtoWithoutPurchase, taxId)).thenReturn(updatedTaxWithoutPurchase);

        // Act
        Tax result = taxUpdateService.update(dtoWithoutPurchase, taxId);

        // Assert
        assertNotNull(result);
        assertEquals("ICA", result.getCode());
        verify(taxUpdateOutputPort).update(dtoWithoutPurchase, taxId);
    }

    @Test
    @DisplayName("Debe actualizar impuesto sin ninguna cuenta contable asociada")
    void testUpdateTaxWithBothAccountsNull() {
        // Arrange
        TaxDTO dtoWithoutAccounts = TaxDTO.builder()
                .id(taxId)
                .idEnterprise(idEnterprise)
                .code("TASA")
                .description("Tasa especial")
                .interest(2.0)
                .salesTaxId(null)
                .purchaseTaxId(null)
                .build();
        Tax updatedTaxWithoutAccounts = Tax.builder()
                .id(taxId)
                .idEnterprise(idEnterprise)
                .code("TASA")
                .description("Tasa especial")
                .interest(2.0)
                .status(true)
                .build();
        doNothing().when(taxValidationService).validateTaxExists(taxId, idEnterprise);
        when(taxSearchOutputPort.getTaxByIdAndEnterprise(taxId, idEnterprise)).thenReturn(existingTax);
        doNothing().when(taxValidationService).validateTaxCodeNotExistsExcludingId(dtoWithoutAccounts.getCode(), idEnterprise, taxId);
        doNothing().when(taxValidationService).validateAccountDigits(null, null, idEnterprise);
        doNothing().when(taxValidationService).validateDifferentTaxAccounts(null, null);
        when(taxUpdateOutputPort.update(dtoWithoutAccounts, taxId)).thenReturn(updatedTaxWithoutAccounts);

        // Act
        Tax result = taxUpdateService.update(dtoWithoutAccounts, taxId);

        // Assert
        assertNotNull(result);
        assertEquals("TASA", result.getCode());
        verify(taxUpdateOutputPort).update(dtoWithoutAccounts, taxId);
    }

    @Test
    @DisplayName("Debe manejar diferentes empresas en actualización")
    void testUpdateTaxWithDifferentEnterprise() {
        // Arrange
        String differentEnterpriseId = "ENT-002";
        Long differentTaxId = 5L;
        TaxDTO dtoForDifferentEnterprise = TaxDTO.builder()
                .id(differentTaxId)
                .idEnterprise(differentEnterpriseId)
                .code("IVA5")
                .description("IVA 5% actualizado")
                .interest(5.0)
                .salesTaxId(300L)
                .purchaseTaxId(400L)
                .build();
        Tax existingTaxDifferentEnterprise = Tax.builder()
                .id(differentTaxId)
                .idEnterprise(differentEnterpriseId)
                .code("IVA5")
                .description("IVA 5%")
                .interest(5.0)
                .status(true)
                .usageCount(0)
                .build();
        Tax updatedTaxDifferentEnterprise = Tax.builder()
                .id(differentTaxId)
                .idEnterprise(differentEnterpriseId)
                .code("IVA5")
                .description("IVA 5% actualizado")
                .interest(5.0)
                .status(true)
                .build();
        doNothing().when(taxValidationService).validateTaxExists(differentTaxId, differentEnterpriseId);
        when(taxSearchOutputPort.getTaxByIdAndEnterprise(differentTaxId, differentEnterpriseId)).thenReturn(existingTaxDifferentEnterprise);
        doNothing().when(taxValidationService).validateTaxCodeNotExistsExcludingId(dtoForDifferentEnterprise.getCode(), differentEnterpriseId, differentTaxId);
        doNothing().when(taxValidationService).validateAccountDigits(dtoForDifferentEnterprise.getSalesTaxId(), dtoForDifferentEnterprise.getPurchaseTaxId(), differentEnterpriseId);
        doNothing().when(taxValidationService).validateDifferentTaxAccounts(dtoForDifferentEnterprise.getSalesTaxId(), dtoForDifferentEnterprise.getPurchaseTaxId());
        when(taxUpdateOutputPort.update(dtoForDifferentEnterprise, differentTaxId)).thenReturn(updatedTaxDifferentEnterprise);

        // Act
        Tax result = taxUpdateService.update(dtoForDifferentEnterprise, differentTaxId);

        // Assert
        assertNotNull(result);
        assertEquals(differentEnterpriseId, result.getIdEnterprise());
        assertEquals("IVA5", result.getCode());
        verify(taxValidationService).validateTaxExists(differentTaxId, differentEnterpriseId);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando impuesto tiene exactamente un uso")
    void testUpdateThrowsExceptionWhenTaxHasOneUsage() {
        // Arrange
        Tax taxWithOneUsage = Tax.builder()
                .id(taxId)
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19%")
                .interest(19.0)
                .status(true)
                .usageCount(1)
                .build();
        doNothing().when(taxValidationService).validateTaxExists(taxId, idEnterprise);
        when(taxSearchOutputPort.getTaxByIdAndEnterprise(taxId, idEnterprise)).thenReturn(taxWithOneUsage);

        // Act & Assert
        TaxInUseException exception = assertThrows(TaxInUseException.class,
                () -> taxUpdateService.update(taxDTO, taxId));

        assertTrue(exception.getMessage().contains("editar"));
        verify(taxUpdateOutputPort, never()).update(any(), anyLong());
    }

    @Test
    @DisplayName("Debe actualizar impuesto con usageCount null (sin uso)")
    void testUpdateTaxWithNullUsageCount() {
        // Arrange
        Tax taxWithNullUsage = Tax.builder()
                .id(taxId)
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19%")
                .interest(19.0)
                .status(true)
                .usageCount(null)
                .build();
        doNothing().when(taxValidationService).validateTaxExists(taxId, idEnterprise);
        when(taxSearchOutputPort.getTaxByIdAndEnterprise(taxId, idEnterprise)).thenReturn(taxWithNullUsage);
        doNothing().when(taxValidationService).validateTaxCodeNotExistsExcludingId(taxDTO.getCode(), idEnterprise, taxId);
        doNothing().when(taxValidationService).validateAccountDigits(taxDTO.getSalesTaxId(), taxDTO.getPurchaseTaxId(), idEnterprise);
        doNothing().when(taxValidationService).validateDifferentTaxAccounts(taxDTO.getSalesTaxId(), taxDTO.getPurchaseTaxId());
        when(taxUpdateOutputPort.update(taxDTO, taxId)).thenReturn(updatedTax);

        // Act
        Tax result = taxUpdateService.update(taxDTO, taxId);

        // Assert
        assertNotNull(result);
        verify(taxUpdateOutputPort).update(taxDTO, taxId);
    }

    @Test
    @DisplayName("Debe propagar correctamente los datos del DTO al puerto de salida")
    void testUpdatePassesCorrectDataToOutputPort() {
        // Arrange
        doNothing().when(taxValidationService).validateTaxExists(taxId, idEnterprise);
        when(taxSearchOutputPort.getTaxByIdAndEnterprise(taxId, idEnterprise)).thenReturn(existingTax);
        doNothing().when(taxValidationService).validateTaxCodeNotExistsExcludingId(taxDTO.getCode(), idEnterprise, taxId);
        doNothing().when(taxValidationService).validateAccountDigits(taxDTO.getSalesTaxId(), taxDTO.getPurchaseTaxId(), idEnterprise);
        doNothing().when(taxValidationService).validateDifferentTaxAccounts(taxDTO.getSalesTaxId(), taxDTO.getPurchaseTaxId());
        when(taxUpdateOutputPort.update(taxDTO, taxId)).thenReturn(updatedTax);

        // Act
        taxUpdateService.update(taxDTO, taxId);

        // Assert
        verify(taxUpdateOutputPort).update(argThat(dto ->
                dto.getCode().equals("IVA19") &&
                dto.getIdEnterprise().equals(idEnterprise) &&
                dto.getDescription().equals("Impuesto al valor agregado 19% actualizado") &&
                dto.getInterest().equals(19.0) &&
                dto.getSalesTaxId().equals(100L) &&
                dto.getPurchaseTaxId().equals(200L)
        ), eq(taxId));
    }
}

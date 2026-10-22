package co.unicauca.edu.co.contables.accounting.catalogue.unit.taxes.application.services;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.output.IAccountCatalogueSearchOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.AccountCatalogueInactiveException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.AccountCatalogueNotFoundException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.taxes.DuplicateTaxAccountsException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.taxes.InvalidAccountDigitsException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.taxes.TaxAlreadyExistsException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.taxes.TaxNotFoundException;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.output.ITaxSearchOutputPort;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TaxValidationServiceUnitTest {

    @Mock
    private ITaxSearchOutputPort taxSearchOutputPort;

    @Mock
    private IAccountCatalogueSearchOutputPort accountCatalogueSearchOutputPort;

    @InjectMocks
    private TaxValidationService taxValidationService;

    private String idEnterprise;
    private Long salesTaxId;
    private Long purchaseTaxId;
    private AccountCatalogue validSalesAccount;
    private AccountCatalogue validPurchaseAccount;

    @BeforeEach
    void setUp() {
        idEnterprise = "ENT-001";
        salesTaxId = 100L;
        purchaseTaxId = 200L;

        validSalesAccount = AccountCatalogue.builder()
                .id(salesTaxId)
                .idEnterprise(idEnterprise)
                .code("24080501")
                .description("Cuenta de impuesto ventas")
                .status(true)
                .build();

        validPurchaseAccount = AccountCatalogue.builder()
                .id(purchaseTaxId)
                .idEnterprise(idEnterprise)
                .code("24080502")
                .description("Cuenta de impuesto compras")
                .status(true)
                .build();
    }

    // ==================== Tests para validateAccountDigits ====================

    @Test
    @DisplayName("Debe validar cuentas de impuesto exitosamente cuando ambas son válidas")
    void testValidateAccountDigitsSuccess() {
        // Arrange
        when(accountCatalogueSearchOutputPort.getAccountCatalogueByIdAndIdEnterprise(salesTaxId, idEnterprise))
                .thenReturn(validSalesAccount);
        when(accountCatalogueSearchOutputPort.getAccountCatalogueByIdAndIdEnterprise(purchaseTaxId, idEnterprise))
                .thenReturn(validPurchaseAccount);

        // Act & Assert
        assertDoesNotThrow(() -> taxValidationService.validateAccountDigits(salesTaxId, purchaseTaxId, idEnterprise));

        verify(accountCatalogueSearchOutputPort).getAccountCatalogueByIdAndIdEnterprise(salesTaxId, idEnterprise);
        verify(accountCatalogueSearchOutputPort).getAccountCatalogueByIdAndIdEnterprise(purchaseTaxId, idEnterprise);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando cuenta de ventas no existe")
    void testValidateAccountDigitsThrowsExceptionWhenSalesAccountNotFound() {
        // Arrange
        when(accountCatalogueSearchOutputPort.getAccountCatalogueByIdAndIdEnterprise(salesTaxId, idEnterprise))
                .thenReturn(null);

        // Act & Assert
        AccountCatalogueNotFoundException exception = assertThrows(AccountCatalogueNotFoundException.class,
                () -> taxValidationService.validateAccountDigits(salesTaxId, purchaseTaxId, idEnterprise));

        assertTrue(exception.getMessage().contains("cuenta especificada no existe"));
        verify(accountCatalogueSearchOutputPort).getAccountCatalogueByIdAndIdEnterprise(salesTaxId, idEnterprise);
        verify(accountCatalogueSearchOutputPort, never()).getAccountCatalogueByIdAndIdEnterprise(purchaseTaxId, idEnterprise);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando cuenta de ventas está inactiva")
    void testValidateAccountDigitsThrowsExceptionWhenSalesAccountInactive() {
        // Arrange
        AccountCatalogue inactiveSalesAccount = AccountCatalogue.builder()
                .id(salesTaxId)
                .idEnterprise(idEnterprise)
                .code("24080501")
                .description("Cuenta inactiva")
                .status(false)
                .build();
        when(accountCatalogueSearchOutputPort.getAccountCatalogueByIdAndIdEnterprise(salesTaxId, idEnterprise))
                .thenReturn(inactiveSalesAccount);

        // Act & Assert
        AccountCatalogueInactiveException exception = assertThrows(AccountCatalogueInactiveException.class,
                () -> taxValidationService.validateAccountDigits(salesTaxId, purchaseTaxId, idEnterprise));

        assertTrue(exception.getMessage().contains("inactiva"));
        assertTrue(exception.getMessage().contains("24080501"));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando código de cuenta de ventas no tiene 8 dígitos")
    void testValidateAccountDigitsThrowsExceptionWhenSalesAccountCodeInvalid() {
        // Arrange
        AccountCatalogue accountWithShortCode = AccountCatalogue.builder()
                .id(salesTaxId)
                .idEnterprise(idEnterprise)
                .code("2408")
                .description("Cuenta con código corto")
                .status(true)
                .build();
        when(accountCatalogueSearchOutputPort.getAccountCatalogueByIdAndIdEnterprise(salesTaxId, idEnterprise))
                .thenReturn(accountWithShortCode);

        // Act & Assert
        assertThrows(InvalidAccountDigitsException.class,
                () -> taxValidationService.validateAccountDigits(salesTaxId, purchaseTaxId, idEnterprise));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando cuenta de compras no existe")
    void testValidateAccountDigitsThrowsExceptionWhenPurchaseAccountNotFound() {
        // Arrange
        when(accountCatalogueSearchOutputPort.getAccountCatalogueByIdAndIdEnterprise(salesTaxId, idEnterprise))
                .thenReturn(validSalesAccount);
        when(accountCatalogueSearchOutputPort.getAccountCatalogueByIdAndIdEnterprise(purchaseTaxId, idEnterprise))
                .thenReturn(null);

        // Act & Assert
        AccountCatalogueNotFoundException exception = assertThrows(AccountCatalogueNotFoundException.class,
                () -> taxValidationService.validateAccountDigits(salesTaxId, purchaseTaxId, idEnterprise));

        assertTrue(exception.getMessage().contains("cuenta especificada no existe"));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando cuenta de compras está inactiva")
    void testValidateAccountDigitsThrowsExceptionWhenPurchaseAccountInactive() {
        // Arrange
        AccountCatalogue inactivePurchaseAccount = AccountCatalogue.builder()
                .id(purchaseTaxId)
                .idEnterprise(idEnterprise)
                .code("24080502")
                .description("Cuenta inactiva")
                .status(false)
                .build();
        when(accountCatalogueSearchOutputPort.getAccountCatalogueByIdAndIdEnterprise(salesTaxId, idEnterprise))
                .thenReturn(validSalesAccount);
        when(accountCatalogueSearchOutputPort.getAccountCatalogueByIdAndIdEnterprise(purchaseTaxId, idEnterprise))
                .thenReturn(inactivePurchaseAccount);

        // Act & Assert
        AccountCatalogueInactiveException exception = assertThrows(AccountCatalogueInactiveException.class,
                () -> taxValidationService.validateAccountDigits(salesTaxId, purchaseTaxId, idEnterprise));

        assertTrue(exception.getMessage().contains("inactiva"));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando código de cuenta de compras no tiene 8 dígitos")
    void testValidateAccountDigitsThrowsExceptionWhenPurchaseAccountCodeInvalid() {
        // Arrange
        AccountCatalogue accountWithLongCode = AccountCatalogue.builder()
                .id(purchaseTaxId)
                .idEnterprise(idEnterprise)
                .code("240805021234")
                .description("Cuenta con código largo")
                .status(true)
                .build();
        when(accountCatalogueSearchOutputPort.getAccountCatalogueByIdAndIdEnterprise(salesTaxId, idEnterprise))
                .thenReturn(validSalesAccount);
        when(accountCatalogueSearchOutputPort.getAccountCatalogueByIdAndIdEnterprise(purchaseTaxId, idEnterprise))
                .thenReturn(accountWithLongCode);

        // Act & Assert
        assertThrows(InvalidAccountDigitsException.class,
                () -> taxValidationService.validateAccountDigits(salesTaxId, purchaseTaxId, idEnterprise));
    }

    @Test
    @DisplayName("Debe validar exitosamente cuando salesTaxId es null")
    void testValidateAccountDigitsWithNullSalesTaxId() {
        // Arrange
        when(accountCatalogueSearchOutputPort.getAccountCatalogueByIdAndIdEnterprise(purchaseTaxId, idEnterprise))
                .thenReturn(validPurchaseAccount);

        // Act & Assert
        assertDoesNotThrow(() -> taxValidationService.validateAccountDigits(null, purchaseTaxId, idEnterprise));

        verify(accountCatalogueSearchOutputPort, never()).getAccountCatalogueByIdAndIdEnterprise(eq(null), any());
        verify(accountCatalogueSearchOutputPort).getAccountCatalogueByIdAndIdEnterprise(purchaseTaxId, idEnterprise);
    }

    @Test
    @DisplayName("Debe validar exitosamente cuando purchaseTaxId es null")
    void testValidateAccountDigitsWithNullPurchaseTaxId() {
        // Arrange
        when(accountCatalogueSearchOutputPort.getAccountCatalogueByIdAndIdEnterprise(salesTaxId, idEnterprise))
                .thenReturn(validSalesAccount);

        // Act & Assert
        assertDoesNotThrow(() -> taxValidationService.validateAccountDigits(salesTaxId, null, idEnterprise));

        verify(accountCatalogueSearchOutputPort).getAccountCatalogueByIdAndIdEnterprise(salesTaxId, idEnterprise);
    }

    @Test
    @DisplayName("Debe validar exitosamente cuando ambos IDs son null")
    void testValidateAccountDigitsWithBothNull() {
        // Arrange - No se necesita configurar mocks

        // Act & Assert
        assertDoesNotThrow(() -> taxValidationService.validateAccountDigits(null, null, idEnterprise));

        verify(accountCatalogueSearchOutputPort, never()).getAccountCatalogueByIdAndIdEnterprise(any(), any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando código de cuenta es null")
    void testValidateAccountDigitsThrowsExceptionWhenAccountCodeIsNull() {
        // Arrange
        AccountCatalogue accountWithNullCode = AccountCatalogue.builder()
                .id(salesTaxId)
                .idEnterprise(idEnterprise)
                .code(null)
                .description("Cuenta sin código")
                .status(true)
                .build();
        when(accountCatalogueSearchOutputPort.getAccountCatalogueByIdAndIdEnterprise(salesTaxId, idEnterprise))
                .thenReturn(accountWithNullCode);

        // Act & Assert
        assertThrows(InvalidAccountDigitsException.class,
                () -> taxValidationService.validateAccountDigits(salesTaxId, purchaseTaxId, idEnterprise));
    }

    // ==================== Tests para validateAccountActive ====================

    @Test
    @DisplayName("Debe validar cuenta activa exitosamente")
    void testValidateAccountActiveSuccess() {
        // Arrange
        Long accountId = 300L;
        AccountCatalogue activeAccount = AccountCatalogue.builder()
                .id(accountId)
                .idEnterprise(idEnterprise)
                .code("11050101")
                .description("Cuenta activa")
                .status(true)
                .build();
        when(accountCatalogueSearchOutputPort.getAccountCatalogueByIdAndIdEnterprise(accountId, idEnterprise))
                .thenReturn(activeAccount);

        // Act & Assert
        assertDoesNotThrow(() -> taxValidationService.validateAccountActive(accountId, idEnterprise));

        verify(accountCatalogueSearchOutputPort).getAccountCatalogueByIdAndIdEnterprise(accountId, idEnterprise);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando cuenta no existe en validateAccountActive")
    void testValidateAccountActiveThrowsExceptionWhenNotFound() {
        // Arrange
        Long accountId = 300L;
        when(accountCatalogueSearchOutputPort.getAccountCatalogueByIdAndIdEnterprise(accountId, idEnterprise))
                .thenReturn(null);

        // Act & Assert
        AccountCatalogueNotFoundException exception = assertThrows(AccountCatalogueNotFoundException.class,
                () -> taxValidationService.validateAccountActive(accountId, idEnterprise));

        assertTrue(exception.getMessage().contains("cuenta especificada no existe"));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando cuenta está inactiva en validateAccountActive")
    void testValidateAccountActiveThrowsExceptionWhenInactive() {
        // Arrange
        Long accountId = 300L;
        AccountCatalogue inactiveAccount = AccountCatalogue.builder()
                .id(accountId)
                .idEnterprise(idEnterprise)
                .code("11050101")
                .description("Cuenta inactiva")
                .status(false)
                .build();
        when(accountCatalogueSearchOutputPort.getAccountCatalogueByIdAndIdEnterprise(accountId, idEnterprise))
                .thenReturn(inactiveAccount);

        // Act & Assert
        AccountCatalogueInactiveException exception = assertThrows(AccountCatalogueInactiveException.class,
                () -> taxValidationService.validateAccountActive(accountId, idEnterprise));

        assertTrue(exception.getMessage().contains("inactiva"));
    }

    @Test
    @DisplayName("Debe no validar cuando accountId es null en validateAccountActive")
    void testValidateAccountActiveWithNullAccountId() {
        // Arrange - No se necesita configurar mocks

        // Act & Assert
        assertDoesNotThrow(() -> taxValidationService.validateAccountActive(null, idEnterprise));

        verify(accountCatalogueSearchOutputPort, never()).getAccountCatalogueByIdAndIdEnterprise(any(), any());
    }

    // ==================== Tests para validateTaxCodeNotExists ====================

    @Test
    @DisplayName("Debe validar código no existente exitosamente")
    void testValidateTaxCodeNotExistsSuccess() {
        // Arrange
        String code = "NEWCODE";
        List<Tax> existingTaxes = Arrays.asList(
                Tax.builder().id(1L).code("IVA19").build(),
                Tax.builder().id(2L).code("RET4").build()
        );
        when(taxSearchOutputPort.getTaxesByEnterprise(idEnterprise)).thenReturn(existingTaxes);

        // Act & Assert
        assertDoesNotThrow(() -> taxValidationService.validateTaxCodeNotExists(code, idEnterprise));

        verify(taxSearchOutputPort).getTaxesByEnterprise(idEnterprise);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando código ya existe")
    void testValidateTaxCodeNotExistsThrowsExceptionWhenCodeExists() {
        // Arrange
        String code = "IVA19";
        List<Tax> existingTaxes = Arrays.asList(
                Tax.builder().id(1L).code("IVA19").build(),
                Tax.builder().id(2L).code("RET4").build()
        );
        when(taxSearchOutputPort.getTaxesByEnterprise(idEnterprise)).thenReturn(existingTaxes);

        // Act & Assert
        TaxAlreadyExistsException exception = assertThrows(TaxAlreadyExistsException.class,
                () -> taxValidationService.validateTaxCodeNotExists(code, idEnterprise));

        assertTrue(exception.getMessage().contains("IVA19"));
    }

    @Test
    @DisplayName("Debe detectar código duplicado con diferente case")
    void testValidateTaxCodeNotExistsDetectsDuplicateWithDifferentCase() {
        // Arrange
        String code = "iva19";
        List<Tax> existingTaxes = Collections.singletonList(
                Tax.builder().id(1L).code("IVA19").build()
        );
        when(taxSearchOutputPort.getTaxesByEnterprise(idEnterprise)).thenReturn(existingTaxes);

        // Act & Assert
        assertThrows(TaxAlreadyExistsException.class,
                () -> taxValidationService.validateTaxCodeNotExists(code, idEnterprise));
    }

    @Test
    @DisplayName("Debe no validar cuando código es null")
    void testValidateTaxCodeNotExistsWithNullCode() {
        // Arrange - No se necesita configurar mocks

        // Act & Assert
        assertDoesNotThrow(() -> taxValidationService.validateTaxCodeNotExists(null, idEnterprise));

        verify(taxSearchOutputPort, never()).getTaxesByEnterprise(any());
    }

    @Test
    @DisplayName("Debe no validar cuando código está vacío")
    void testValidateTaxCodeNotExistsWithEmptyCode() {
        // Arrange - No se necesita configurar mocks

        // Act & Assert
        assertDoesNotThrow(() -> taxValidationService.validateTaxCodeNotExists("", idEnterprise));

        verify(taxSearchOutputPort, never()).getTaxesByEnterprise(any());
    }

    @Test
    @DisplayName("Debe no validar cuando código tiene solo espacios")
    void testValidateTaxCodeNotExistsWithWhitespaceCode() {
        // Arrange - No se necesita configurar mocks

        // Act & Assert
        assertDoesNotThrow(() -> taxValidationService.validateTaxCodeNotExists("   ", idEnterprise));

        verify(taxSearchOutputPort, never()).getTaxesByEnterprise(any());
    }

    @Test
    @DisplayName("Debe validar exitosamente cuando lista de impuestos está vacía")
    void testValidateTaxCodeNotExistsWithEmptyTaxList() {
        // Arrange
        String code = "ANYCODE";
        when(taxSearchOutputPort.getTaxesByEnterprise(idEnterprise)).thenReturn(Collections.emptyList());

        // Act & Assert
        assertDoesNotThrow(() -> taxValidationService.validateTaxCodeNotExists(code, idEnterprise));

        verify(taxSearchOutputPort).getTaxesByEnterprise(idEnterprise);
    }

    // ==================== Tests para validateTaxCodeNotExistsExcludingId ====================

    @Test
    @DisplayName("Debe validar código no existente excluyendo ID exitosamente")
    void testValidateTaxCodeNotExistsExcludingIdSuccess() {
        // Arrange
        String code = "NEWCODE";
        Long excludeId = 1L;
        List<Tax> existingTaxes = Arrays.asList(
                Tax.builder().id(1L).code("IVA19").build(),
                Tax.builder().id(2L).code("RET4").build()
        );
        when(taxSearchOutputPort.getTaxesByEnterprise(idEnterprise)).thenReturn(existingTaxes);

        // Act & Assert
        assertDoesNotThrow(() -> taxValidationService.validateTaxCodeNotExistsExcludingId(code, idEnterprise, excludeId));

        verify(taxSearchOutputPort).getTaxesByEnterprise(idEnterprise);
    }

    @Test
    @DisplayName("Debe permitir mismo código cuando es del ID excluido")
    void testValidateTaxCodeNotExistsExcludingIdAllowsSameCodeForExcludedId() {
        // Arrange
        String code = "IVA19";
        Long excludeId = 1L;
        List<Tax> existingTaxes = Arrays.asList(
                Tax.builder().id(1L).code("IVA19").build(),
                Tax.builder().id(2L).code("RET4").build()
        );
        when(taxSearchOutputPort.getTaxesByEnterprise(idEnterprise)).thenReturn(existingTaxes);

        // Act & Assert
        assertDoesNotThrow(() -> taxValidationService.validateTaxCodeNotExistsExcludingId(code, idEnterprise, excludeId));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando código existe en otro impuesto")
    void testValidateTaxCodeNotExistsExcludingIdThrowsExceptionWhenCodeExistsInOther() {
        // Arrange
        String code = "RET4";
        Long excludeId = 1L;
        List<Tax> existingTaxes = Arrays.asList(
                Tax.builder().id(1L).code("IVA19").build(),
                Tax.builder().id(2L).code("RET4").build()
        );
        when(taxSearchOutputPort.getTaxesByEnterprise(idEnterprise)).thenReturn(existingTaxes);

        // Act & Assert
        TaxAlreadyExistsException exception = assertThrows(TaxAlreadyExistsException.class,
                () -> taxValidationService.validateTaxCodeNotExistsExcludingId(code, idEnterprise, excludeId));

        assertTrue(exception.getMessage().contains("RET4"));
    }

    @Test
    @DisplayName("Debe no validar cuando código es null en exclusión")
    void testValidateTaxCodeNotExistsExcludingIdWithNullCode() {
        // Arrange
        Long excludeId = 1L;

        // Act & Assert
        assertDoesNotThrow(() -> taxValidationService.validateTaxCodeNotExistsExcludingId(null, idEnterprise, excludeId));

        verify(taxSearchOutputPort, never()).getTaxesByEnterprise(any());
    }

    // ==================== Tests para validateTaxExists por código ====================

    @Test
    @DisplayName("Debe validar existencia de impuesto por código exitosamente")
    void testValidateTaxExistsByCodeSuccess() {
        // Arrange
        String code = "IVA19";
        Tax existingTax = Tax.builder().id(1L).code(code).idEnterprise(idEnterprise).build();
        when(taxSearchOutputPort.getTax(code, idEnterprise)).thenReturn(existingTax);

        // Act & Assert
        assertDoesNotThrow(() -> taxValidationService.validateTaxExists(code, idEnterprise));

        verify(taxSearchOutputPort).getTax(code, idEnterprise);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando impuesto no existe por código")
    void testValidateTaxExistsByCodeThrowsExceptionWhenNotFound() {
        // Arrange
        String code = "NOEXISTE";
        when(taxSearchOutputPort.getTax(code, idEnterprise)).thenReturn(null);

        // Act & Assert
        TaxNotFoundException exception = assertThrows(TaxNotFoundException.class,
                () -> taxValidationService.validateTaxExists(code, idEnterprise));

        assertTrue(exception.getMessage().contains(code));
    }

    // ==================== Tests para validateTaxExists por ID ====================

    @Test
    @DisplayName("Debe validar existencia de impuesto por ID exitosamente")
    void testValidateTaxExistsByIdSuccess() {
        // Arrange
        Long taxId = 1L;
        Tax existingTax = Tax.builder().id(taxId).code("IVA19").idEnterprise(idEnterprise).build();
        when(taxSearchOutputPort.getTaxByIdAndEnterprise(taxId, idEnterprise)).thenReturn(existingTax);

        // Act & Assert
        assertDoesNotThrow(() -> taxValidationService.validateTaxExists(taxId, idEnterprise));

        verify(taxSearchOutputPort).getTaxByIdAndEnterprise(taxId, idEnterprise);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando impuesto no existe por ID")
    void testValidateTaxExistsByIdThrowsExceptionWhenNotFound() {
        // Arrange
        Long taxId = 999L;
        when(taxSearchOutputPort.getTaxByIdAndEnterprise(taxId, idEnterprise)).thenReturn(null);

        // Act & Assert
        TaxNotFoundException exception = assertThrows(TaxNotFoundException.class,
                () -> taxValidationService.validateTaxExists(taxId, idEnterprise));

        assertTrue(exception.getMessage().contains(String.valueOf(taxId)));
    }

    // ==================== Tests para validateDifferentTaxAccounts ====================

    @Test
    @DisplayName("Debe validar cuentas diferentes exitosamente")
    void testValidateDifferentTaxAccountsSuccess() {
        // Arrange
        Long salesId = 100L;
        Long purchaseId = 200L;

        // Act & Assert
        assertDoesNotThrow(() -> taxValidationService.validateDifferentTaxAccounts(salesId, purchaseId));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando cuentas son iguales")
    void testValidateDifferentTaxAccountsThrowsExceptionWhenSameAccounts() {
        // Arrange
        Long sameId = 100L;

        // Act & Assert
        DuplicateTaxAccountsException exception = assertThrows(DuplicateTaxAccountsException.class,
                () -> taxValidationService.validateDifferentTaxAccounts(sameId, sameId));

        assertTrue(exception.getMessage().contains("diferentes"));
    }

    @Test
    @DisplayName("Debe validar exitosamente cuando salesTaxId es null")
    void testValidateDifferentTaxAccountsWithNullSalesTaxId() {
        // Arrange
        Long purchaseId = 200L;

        // Act & Assert
        assertDoesNotThrow(() -> taxValidationService.validateDifferentTaxAccounts(null, purchaseId));
    }

    @Test
    @DisplayName("Debe validar exitosamente cuando purchaseTaxId es null")
    void testValidateDifferentTaxAccountsWithNullPurchaseTaxId() {
        // Arrange
        Long salesId = 100L;

        // Act & Assert
        assertDoesNotThrow(() -> taxValidationService.validateDifferentTaxAccounts(salesId, null));
    }

    @Test
    @DisplayName("Debe validar exitosamente cuando ambas cuentas son null")
    void testValidateDifferentTaxAccountsWithBothNull() {
        // Arrange - No se necesita configurar

        // Act & Assert
        assertDoesNotThrow(() -> taxValidationService.validateDifferentTaxAccounts(null, null));
    }
}

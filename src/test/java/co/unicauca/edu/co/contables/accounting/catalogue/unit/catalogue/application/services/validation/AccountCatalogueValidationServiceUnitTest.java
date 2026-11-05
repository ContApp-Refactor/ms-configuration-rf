package co.unicauca.edu.co.contables.accounting.catalogue.unit.catalogue.application.services.validation;

import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.dataAccess.repository.BankAccountRepository;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.output.IAccountCatalogueSearchOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.validation.AccountCatalogueValidationService;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.FinancialStatusEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.AccountCatalogueAlreadyExistsException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.AccountCatalogueAssociatedWithBankAccountException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.AccountCatalogueAssociatedWithPaymentMethodException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.AccountCatalogueAssociatedWithTaxException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.AccountCatalogueDescriptionAlreadyExistsException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.AccountCatalogueNotFoundException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.InvalidAccountCodeException;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.dataAccess.repository.PaymentMethodRepository;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters.repository.ITaxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccountCatalogueValidationServiceUnitTest {

    @Mock
    private IAccountCatalogueSearchOutputPort accountCatalogueSearchOutputPort;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private PaymentMethodRepository paymentMethodRepository;

    @Mock
    private ITaxRepository taxRepository;

    @InjectMocks
    private AccountCatalogueValidationService validationService;

    private String entId;
    private AccountCatalogue account;

    @BeforeEach
    void setUp() {
        entId = "ENT-001";
        account = AccountCatalogue.builder()
                .id(1L)
                .code("11050101")
                .description("Cuenta de prueba")
                .idEnterprise(entId)
                .build();
    }

    // ========== Tests para validateAccountCode ==========

    @Test
    @DisplayName("Debe validar código de 1 dígito correctamente")
    void testValidateAccountCode1Digit() {
        assertDoesNotThrow(() -> validationService.validateAccountCode("1"));
    }

    @Test
    @DisplayName("Debe validar código de 2 dígitos correctamente")
    void testValidateAccountCode2Digits() {
        assertDoesNotThrow(() -> validationService.validateAccountCode("11"));
    }

    @Test
    @DisplayName("Debe validar código de 4 dígitos correctamente")
    void testValidateAccountCode4Digits() {
        assertDoesNotThrow(() -> validationService.validateAccountCode("1105"));
    }

    @Test
    @DisplayName("Debe validar código de 6 dígitos correctamente")
    void testValidateAccountCode6Digits() {
        assertDoesNotThrow(() -> validationService.validateAccountCode("110501"));
    }

    @Test
    @DisplayName("Debe validar código de 8 dígitos correctamente")
    void testValidateAccountCode8Digits() {
        assertDoesNotThrow(() -> validationService.validateAccountCode("11050101"));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando código es null")
    void testValidateAccountCodeNull() {
        assertThrows(InvalidAccountCodeException.class,
                () -> validationService.validateAccountCode(null));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando código está vacío")
    void testValidateAccountCodeEmpty() {
        assertThrows(InvalidAccountCodeException.class,
                () -> validationService.validateAccountCode(""));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando código contiene letras")
    void testValidateAccountCodeWithLetters() {
        assertThrows(InvalidAccountCodeException.class,
                () -> validationService.validateAccountCode("ABC123"));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando código tiene longitud inválida de 3")
    void testValidateAccountCodeInvalidLength3() {
        assertThrows(InvalidAccountCodeException.class,
                () -> validationService.validateAccountCode("123"));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando código tiene longitud inválida de 5")
    void testValidateAccountCodeInvalidLength5() {
        assertThrows(InvalidAccountCodeException.class,
                () -> validationService.validateAccountCode("12345"));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando código tiene longitud inválida de 7")
    void testValidateAccountCodeInvalidLength7() {
        assertThrows(InvalidAccountCodeException.class,
                () -> validationService.validateAccountCode("1234567"));
    }

    // ========== Tests para validateAccountDoesNotExist ==========

    @Test
    @DisplayName("Debe pasar validación cuando cuenta no existe")
    void testValidateAccountDoesNotExistSuccess() {
        // Arrange
        when(accountCatalogueSearchOutputPort.getAccountCatalogueByCode("1105", entId)).thenReturn(null);

        // Act & Assert
        assertDoesNotThrow(() -> validationService.validateAccountDoesNotExist("1105", entId));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando cuenta ya existe")
    void testValidateAccountDoesNotExistThrowsException() {
        // Arrange
        when(accountCatalogueSearchOutputPort.getAccountCatalogueByCode("1105", entId)).thenReturn(account);

        // Act & Assert
        assertThrows(AccountCatalogueAlreadyExistsException.class,
                () -> validationService.validateAccountDoesNotExist("1105", entId));
    }

    // ========== Tests para validateAccountDoesNotExistExcluding ==========

    @Test
    @DisplayName("Debe pasar validación cuando no existe otra cuenta con el código")
    void testValidateAccountDoesNotExistExcludingSuccess() {
        // Arrange
        when(accountCatalogueSearchOutputPort.getAccountCatalogueByCode("1105", entId)).thenReturn(null);

        // Act & Assert
        assertDoesNotThrow(() -> validationService.validateAccountDoesNotExistExcluding("1105", entId, 1L));
    }

    @Test
    @DisplayName("Debe pasar validación cuando la cuenta encontrada es la misma que se excluye")
    void testValidateAccountDoesNotExistExcludingSameAccount() {
        // Arrange
        account.setId(1L);
        when(accountCatalogueSearchOutputPort.getAccountCatalogueByCode("1105", entId)).thenReturn(account);

        // Act & Assert
        assertDoesNotThrow(() -> validationService.validateAccountDoesNotExistExcluding("1105", entId, 1L));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando existe otra cuenta con el código")
    void testValidateAccountDoesNotExistExcludingDifferentAccount() {
        // Arrange
        account.setId(2L);
        when(accountCatalogueSearchOutputPort.getAccountCatalogueByCode("1105", entId)).thenReturn(account);

        // Act & Assert
        assertThrows(AccountCatalogueAlreadyExistsException.class,
                () -> validationService.validateAccountDoesNotExistExcluding("1105", entId, 1L));
    }

    @Test
    @DisplayName("Debe omitir validación cuando código es null")
    void testValidateAccountDoesNotExistExcludingNullCode() {
        assertDoesNotThrow(() -> validationService.validateAccountDoesNotExistExcluding(null, entId, 1L));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando entId es null")
    void testValidateAccountDoesNotExistExcludingNullEntId() {
        assertThrows(IllegalArgumentException.class,
                () -> validationService.validateAccountDoesNotExistExcluding("1105", null, 1L));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando excludeId es null")
    void testValidateAccountDoesNotExistExcludingNullExcludeId() {
        assertThrows(IllegalArgumentException.class,
                () -> validationService.validateAccountDoesNotExistExcluding("1105", entId, null));
    }

    // ========== Tests para validateAccountExists ==========

    @Test
    @DisplayName("Debe retornar cuenta cuando existe")
    void testValidateAccountExistsSuccess() {
        // Arrange
        when(accountCatalogueSearchOutputPort.getAccountCatalogueByCode("1105", entId)).thenReturn(account);

        // Act
        AccountCatalogue result = validationService.validateAccountExists("1105", entId);

        // Assert
        assertNotNull(result);
        assertEquals(account.getId(), result.getId());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando cuenta no existe")
    void testValidateAccountExistsNotFound() {
        // Arrange
        when(accountCatalogueSearchOutputPort.getAccountCatalogueByCode("1105", entId)).thenReturn(null);

        // Act & Assert
        assertThrows(AccountCatalogueNotFoundException.class,
                () -> validationService.validateAccountExists("1105", entId));
    }

    // ========== Tests para validateAccountExistsByIdAndEnterprise ==========

    @Test
    @DisplayName("Debe retornar cuenta cuando existe por ID y empresa")
    void testValidateAccountExistsByIdAndEnterpriseSuccess() {
        // Arrange
        when(accountCatalogueSearchOutputPort.getAccountCatalogueByIdAndIdEnterprise(1L, entId)).thenReturn(account);

        // Act
        AccountCatalogue result = validationService.validateAccountExistsByIdAndEnterprise(1L, entId);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando cuenta no existe por ID y empresa")
    void testValidateAccountExistsByIdAndEnterpriseNotFound() {
        // Arrange
        when(accountCatalogueSearchOutputPort.getAccountCatalogueByIdAndIdEnterprise(1L, entId)).thenReturn(null);

        // Act & Assert
        assertThrows(AccountCatalogueNotFoundException.class,
                () -> validationService.validateAccountExistsByIdAndEnterprise(1L, entId));
    }

    // ========== Tests para validateAccountNotAssociatedWithTaxes ==========

    @Test
    @DisplayName("Debe pasar validación cuando cuenta no tiene impuestos asociados")
    void testValidateAccountNotAssociatedWithTaxesSuccess() {
        // Arrange
        when(taxRepository.existsBySalesTaxCodeOrPurchaseTaxCode(account.getCode(), account.getIdEnterprise()))
                .thenReturn(false);

        // Act & Assert
        assertDoesNotThrow(() -> validationService.validateAccountNotAssociatedWithTaxes(account));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando cuenta tiene impuestos asociados")
    void testValidateAccountNotAssociatedWithTaxesThrowsException() {
        // Arrange
        when(taxRepository.existsBySalesTaxCodeOrPurchaseTaxCode(account.getCode(), account.getIdEnterprise()))
                .thenReturn(true);

        // Act & Assert
        assertThrows(AccountCatalogueAssociatedWithTaxException.class,
                () -> validationService.validateAccountNotAssociatedWithTaxes(account));
    }

    // ========== Tests para validateAccountAndChildrenNotAssociatedWithTaxes ==========

    @Test
    @DisplayName("Debe validar recursivamente cuenta padre e hijos sin impuestos")
    void testValidateAccountAndChildrenNotAssociatedWithTaxesSuccess() {
        // Arrange
        AccountCatalogue child = AccountCatalogue.builder()
                .id(2L)
                .code("1105010101")
                .idEnterprise(entId)
                .build();
        List<AccountCatalogue> children = new ArrayList<>();
        children.add(child);
        account.setChildren(children);

        when(taxRepository.existsBySalesTaxCodeOrPurchaseTaxCode(anyString(), anyString())).thenReturn(false);

        // Act & Assert
        assertDoesNotThrow(() -> validationService.validateAccountAndChildrenNotAssociatedWithTaxes(account));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando hijo tiene impuestos asociados")
    void testValidateAccountAndChildrenNotAssociatedWithTaxesChildHasTaxes() {
        // Arrange
        AccountCatalogue child = AccountCatalogue.builder()
                .id(2L)
                .code("1105010101")
                .idEnterprise(entId)
                .build();
        List<AccountCatalogue> children = new ArrayList<>();
        children.add(child);
        account.setChildren(children);

        when(taxRepository.existsBySalesTaxCodeOrPurchaseTaxCode(account.getCode(), entId)).thenReturn(false);
        when(taxRepository.existsBySalesTaxCodeOrPurchaseTaxCode(child.getCode(), entId)).thenReturn(true);

        // Act & Assert
        assertThrows(AccountCatalogueAssociatedWithTaxException.class,
                () -> validationService.validateAccountAndChildrenNotAssociatedWithTaxes(account));
    }

    // ========== Tests para validateAccountNotAssociatedWithBankAccounts ==========

    @Test
    @DisplayName("Debe pasar validación cuando cuenta no tiene cuentas bancarias asociadas")
    void testValidateAccountNotAssociatedWithBankAccountsSuccess() {
        // Arrange
        when(bankAccountRepository.existsByAccountingAccountIdAndIdEnterprise(account.getId(), entId))
                .thenReturn(false);

        // Act & Assert
        assertDoesNotThrow(() -> validationService.validateAccountNotAssociatedWithBankAccounts(account));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando cuenta tiene cuentas bancarias asociadas")
    void testValidateAccountNotAssociatedWithBankAccountsThrowsException() {
        // Arrange
        when(bankAccountRepository.existsByAccountingAccountIdAndIdEnterprise(account.getId(), entId))
                .thenReturn(true);

        // Act & Assert
        assertThrows(AccountCatalogueAssociatedWithBankAccountException.class,
                () -> validationService.validateAccountNotAssociatedWithBankAccounts(account));
    }

    // ========== Tests para validateAccountNotAssociatedWithPaymentMethods ==========

    @Test
    @DisplayName("Debe pasar validación cuando cuenta no tiene métodos de pago asociados")
    void testValidateAccountNotAssociatedWithPaymentMethodsSuccess() {
        // Arrange
        when(paymentMethodRepository.existsByAccountingAccountIdAndIdEnterprise(account.getId(), entId))
                .thenReturn(false);

        // Act & Assert
        assertDoesNotThrow(() -> validationService.validateAccountNotAssociatedWithPaymentMethods(account));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando cuenta tiene métodos de pago asociados")
    void testValidateAccountNotAssociatedWithPaymentMethodsThrowsException() {
        // Arrange
        when(paymentMethodRepository.existsByAccountingAccountIdAndIdEnterprise(account.getId(), entId))
                .thenReturn(true);

        // Act & Assert
        assertThrows(AccountCatalogueAssociatedWithPaymentMethodException.class,
                () -> validationService.validateAccountNotAssociatedWithPaymentMethods(account));
    }

    // ========== Tests para validateAccountDescription ==========

    @Test
    @DisplayName("Debe validar descripción válida")
    void testValidateAccountDescriptionSuccess() {
        assertDoesNotThrow(() -> validationService.validateAccountDescription("Cuenta de prueba"));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando descripción es null")
    void testValidateAccountDescriptionNull() {
        assertThrows(InvalidAccountCodeException.class,
                () -> validationService.validateAccountDescription(null));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando descripción está vacía")
    void testValidateAccountDescriptionEmpty() {
        assertThrows(InvalidAccountCodeException.class,
                () -> validationService.validateAccountDescription(""));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando descripción tiene múltiples espacios consecutivos")
    void testValidateAccountDescriptionMultipleSpaces() {
        assertThrows(InvalidAccountCodeException.class,
                () -> validationService.validateAccountDescription("Cuenta  de  prueba"));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando descripción contiene caracteres inválidos")
    void testValidateAccountDescriptionInvalidCharacters() {
        assertThrows(InvalidAccountCodeException.class,
                () -> validationService.validateAccountDescription("Cuenta@#$%"));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando descripción tiene menos de 2 caracteres")
    void testValidateAccountDescriptionTooShort() {
        assertThrows(InvalidAccountCodeException.class,
                () -> validationService.validateAccountDescription("A"));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando descripción excede 100 caracteres")
    void testValidateAccountDescriptionTooLong() {
        String longDescription = "A".repeat(101);
        assertThrows(InvalidAccountCodeException.class,
                () -> validationService.validateAccountDescription(longDescription));
    }

    @Test
    @DisplayName("Debe aceptar descripción con caracteres especiales permitidos")
    void testValidateAccountDescriptionWithAllowedSpecialChars() {
        assertDoesNotThrow(() -> validationService.validateAccountDescription("Cuenta - prueba, (test)."));
    }

    @Test
    @DisplayName("Debe aceptar descripción con tildes")
    void testValidateAccountDescriptionWithAccents() {
        assertDoesNotThrow(() -> validationService.validateAccountDescription("Crédito público"));
    }

    @Test
    @DisplayName("Debe aceptar descripción con ñ")
    void testValidateAccountDescriptionWithEne() {
        assertDoesNotThrow(() -> validationService.validateAccountDescription("Año fiscal"));
    }

    // ========== Tests para validateAccountDescriptionDoesNotExist ==========

    @Test
    @DisplayName("Debe pasar validación cuando descripción no existe")
    void testValidateAccountDescriptionDoesNotExistSuccess() {
        // Arrange
        when(accountCatalogueSearchOutputPort.getAccountCatalogueByDescriptionIgnoreCaseAndIdEnterprise(
                "Nueva cuenta", entId)).thenReturn(null);

        // Act & Assert
        assertDoesNotThrow(() -> validationService.validateAccountDescriptionDoesNotExist("Nueva cuenta", entId));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando descripción ya existe")
    void testValidateAccountDescriptionDoesNotExistThrowsException() {
        // Arrange
        when(accountCatalogueSearchOutputPort.getAccountCatalogueByDescriptionIgnoreCaseAndIdEnterprise(
                "Cuenta existente", entId)).thenReturn(account);

        // Act & Assert
        assertThrows(AccountCatalogueDescriptionAlreadyExistsException.class,
                () -> validationService.validateAccountDescriptionDoesNotExist("Cuenta existente", entId));
    }

    @Test
    @DisplayName("Debe omitir validación cuando descripción es null")
    void testValidateAccountDescriptionDoesNotExistNullDescription() {
        assertDoesNotThrow(() -> validationService.validateAccountDescriptionDoesNotExist(null, entId));
    }

    // ========== Tests para validateAccountDescriptionDoesNotExistExcluding ==========

    @Test
    @DisplayName("Debe pasar validación cuando descripción no existe excluyendo cuenta")
    void testValidateAccountDescriptionDoesNotExistExcludingSuccess() {
        // Arrange
        when(accountCatalogueSearchOutputPort.getAccountCatalogueByDescriptionIgnoreCaseAndIdEnterprise(
                "Cuenta", entId)).thenReturn(null);

        // Act & Assert
        assertDoesNotThrow(
                () -> validationService.validateAccountDescriptionDoesNotExistExcluding("Cuenta", entId, 1L));
    }

    @Test
    @DisplayName("Debe pasar cuando la cuenta encontrada es la misma que se excluye")
    void testValidateAccountDescriptionDoesNotExistExcludingSameAccount() {
        // Arrange
        account.setId(1L);
        when(accountCatalogueSearchOutputPort.getAccountCatalogueByDescriptionIgnoreCaseAndIdEnterprise(
                "Cuenta", entId)).thenReturn(account);

        // Act & Assert
        assertDoesNotThrow(
                () -> validationService.validateAccountDescriptionDoesNotExistExcluding("Cuenta", entId, 1L));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando existe otra cuenta con la descripción")
    void testValidateAccountDescriptionDoesNotExistExcludingDifferentAccount() {
        // Arrange
        account.setId(2L);
        when(accountCatalogueSearchOutputPort.getAccountCatalogueByDescriptionIgnoreCaseAndIdEnterprise(
                "Cuenta", entId)).thenReturn(account);

        // Act & Assert
        assertThrows(AccountCatalogueDescriptionAlreadyExistsException.class,
                () -> validationService.validateAccountDescriptionDoesNotExistExcluding("Cuenta", entId, 1L));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando entId es null para descripción excluyendo")
    void testValidateAccountDescriptionDoesNotExistExcludingNullEntId() {
        assertThrows(IllegalArgumentException.class,
                () -> validationService.validateAccountDescriptionDoesNotExistExcluding("Cuenta", null, 1L));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando excludeId es null para descripción excluyendo")
    void testValidateAccountDescriptionDoesNotExistExcludingNullExcludeId() {
        assertThrows(IllegalArgumentException.class,
                () -> validationService.validateAccountDescriptionDoesNotExistExcluding("Cuenta", entId, null));
    }

    // ========== Tests para validateCrossingAndCostCenterOnlyForAuxiliaryAccounts ==========

    @Test
    @DisplayName("Debe pasar validación cuando crossing true en cuenta auxiliar")
    void testValidateCrossingTrueAuxiliaryAccount() {
        // Arrange
        account.setCode("11050101");
        account.setCrossing(true);

        // Act & Assert
        assertDoesNotThrow(() -> validationService.validateCrossingAndCostCenterOnlyForAuxiliaryAccounts(account));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando crossing true en cuenta no auxiliar")
    void testValidateCrossingTrueNonAuxiliaryAccount() {
        // Arrange
        account.setCode("1105");
        account.setCrossing(true);

        // Act & Assert
        assertThrows(InvalidAccountCodeException.class,
                () -> validationService.validateCrossingAndCostCenterOnlyForAuxiliaryAccounts(account));
    }

    @Test
    @DisplayName("Debe pasar validación cuando costCenter true en cuenta auxiliar")
    void testValidateCostCenterTrueAuxiliaryAccount() {
        // Arrange
        account.setCode("11050101");
        account.setCostCenter(true);

        // Act & Assert
        assertDoesNotThrow(() -> validationService.validateCrossingAndCostCenterOnlyForAuxiliaryAccounts(account));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando costCenter true en cuenta no auxiliar")
    void testValidateCostCenterTrueNonAuxiliaryAccount() {
        // Arrange
        account.setCode("1105");
        account.setCostCenter(true);

        // Act & Assert
        assertThrows(InvalidAccountCodeException.class,
                () -> validationService.validateCrossingAndCostCenterOnlyForAuxiliaryAccounts(account));
    }

    @Test
    @DisplayName("Debe pasar validación cuando crossing y costCenter son false")
    void testValidateCrossingAndCostCenterFalse() {
        // Arrange
        account.setCode("1105");
        account.setCrossing(false);
        account.setCostCenter(false);

        // Act & Assert
        assertDoesNotThrow(() -> validationService.validateCrossingAndCostCenterOnlyForAuxiliaryAccounts(account));
    }

    @Test
    @DisplayName("Debe pasar validación cuando crossing y costCenter son null")
    void testValidateCrossingAndCostCenterNull() {
        // Arrange
        account.setCode("1105");
        account.setCrossing(null);
        account.setCostCenter(null);

        // Act & Assert
        assertDoesNotThrow(() -> validationService.validateCrossingAndCostCenterOnlyForAuxiliaryAccounts(account));
    }

    // ========== Tests para validateCostCenterRequiresIncomeStatement ==========

    @Test
    @DisplayName("Debe pasar validación cuando costCenter true con Estado de Resultados")
    void testValidateCostCenterWithIncomeStatement() {
        // Arrange
        account.setCode("11050101");
        account.setCostCenter(true);
        account.setFinancialStatus(FinancialStatusEnum.INCOMESTATEMENT);

        // Act & Assert
        assertDoesNotThrow(() -> validationService.validateCostCenterRequiresIncomeStatement(account));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando costCenter true sin Estado de Resultados")
    void testValidateCostCenterWithoutIncomeStatement() {
        // Arrange
        account.setCode("11050101");
        account.setCostCenter(true);
        account.setFinancialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION);

        // Act & Assert
        assertThrows(InvalidAccountCodeException.class,
                () -> validationService.validateCostCenterRequiresIncomeStatement(account));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando costCenter true y financialStatus null")
    void testValidateCostCenterWithNullFinancialStatus() {
        // Arrange
        account.setCode("11050101");
        account.setCostCenter(true);
        account.setFinancialStatus(null);

        // Act & Assert
        assertThrows(InvalidAccountCodeException.class,
                () -> validationService.validateCostCenterRequiresIncomeStatement(account));
    }

    @Test
    @DisplayName("Debe pasar validación cuando costCenter false")
    void testValidateCostCenterFalse() {
        // Arrange
        account.setCode("11050101");
        account.setCostCenter(false);
        account.setFinancialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION);

        // Act & Assert
        assertDoesNotThrow(() -> validationService.validateCostCenterRequiresIncomeStatement(account));
    }

    // ========== Tests para validateParentCodePrefix ==========

    @Test
    @DisplayName("Debe pasar validación cuando código hijo inicia con código padre")
    void testValidateParentCodePrefixSuccess() {
        assertDoesNotThrow(() -> validationService.validateParentCodePrefix("1105", "11"));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando código hijo no inicia con código padre")
    void testValidateParentCodePrefixMismatch() {
        assertThrows(InvalidAccountCodeException.class,
                () -> validationService.validateParentCodePrefix("1205", "11"));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando código de cuenta es null")
    void testValidateParentCodePrefixNullAccountCode() {
        assertThrows(InvalidAccountCodeException.class,
                () -> validationService.validateParentCodePrefix(null, "11"));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando código padre es null")
    void testValidateParentCodePrefixNullParentCode() {
        assertThrows(InvalidAccountCodeException.class,
                () -> validationService.validateParentCodePrefix("1105", null));
    }
}

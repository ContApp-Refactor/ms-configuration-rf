package co.unicauca.edu.co.contables.accounting.catalogue.unit.taxes.application.services;

import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.AccountCatalogueInactiveException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.AccountCatalogueNotFoundException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.taxes.DuplicateTaxAccountsException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.taxes.InvalidAccountDigitsException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.taxes.TaxAlreadyExistsException;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.output.ITaxCreateOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.services.TaxCreateService;
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
class TaxCreateServiceUnitTest {

    @Mock
    private ITaxCreateOutputPort taxCreateOutputPort;

    @Mock
    private TaxValidationService taxValidationService;

    @InjectMocks
    private TaxCreateService taxCreateService;

    private TaxDTO taxDTO;
    private Tax expectedTax;
    private String idEnterprise;

    @BeforeEach
    void setUp() {
        idEnterprise = "ENT-001";

        taxDTO = TaxDTO.builder()
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19%")
                .interest(19.0)
                .salesTaxId(100L)
                .purchaseTaxId(200L)
                .build();

        expectedTax = Tax.builder()
                .id(1L)
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19%")
                .interest(19.0)
                .status(true)
                .build();
    }

    @Test
    @DisplayName("Debe crear impuesto exitosamente con todas las validaciones pasando")
    void testCreateTaxSuccess() {
        doNothing().when(taxValidationService).validateTaxCodeNotExists(taxDTO.getCode(), taxDTO.getIdEnterprise());
        doNothing().when(taxValidationService).validateAccountDigits(taxDTO.getSalesTaxId(), taxDTO.getPurchaseTaxId(), taxDTO.getIdEnterprise());
        doNothing().when(taxValidationService).validateDifferentTaxAccounts(taxDTO.getSalesTaxId(), taxDTO.getPurchaseTaxId());
        when(taxCreateOutputPort.createTax(taxDTO)).thenReturn(expectedTax);

        Tax result = taxCreateService.createTax(taxDTO);

        assertNotNull(result);
        assertEquals(expectedTax.getId(), result.getId());
        assertEquals(expectedTax.getCode(), result.getCode());
        assertEquals(expectedTax.getDescription(), result.getDescription());
        assertEquals(expectedTax.getInterest(), result.getInterest());
        verify(taxValidationService).validateTaxCodeNotExists(taxDTO.getCode(), taxDTO.getIdEnterprise());
        verify(taxValidationService).validateAccountDigits(taxDTO.getSalesTaxId(), taxDTO.getPurchaseTaxId(), taxDTO.getIdEnterprise());
        verify(taxValidationService).validateDifferentTaxAccounts(taxDTO.getSalesTaxId(), taxDTO.getPurchaseTaxId());
        verify(taxCreateOutputPort).createTax(taxDTO);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el código de impuesto ya existe")
    void testCreateTaxThrowsExceptionWhenCodeAlreadyExists() {
        doThrow(new TaxAlreadyExistsException("Ya existe un impuesto con código 'IVA19'"))
                .when(taxValidationService).validateTaxCodeNotExists(taxDTO.getCode(), taxDTO.getIdEnterprise());

        TaxAlreadyExistsException exception = assertThrows(TaxAlreadyExistsException.class,
                () -> taxCreateService.createTax(taxDTO));

        assertTrue(exception.getMessage().contains("IVA19"));
        verify(taxValidationService).validateTaxCodeNotExists(taxDTO.getCode(), taxDTO.getIdEnterprise());
        verify(taxValidationService, never()).validateAccountDigits(any(), any(), any());
        verify(taxValidationService, never()).validateDifferentTaxAccounts(any(), any());
        verify(taxCreateOutputPort, never()).createTax(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la cuenta de impuesto de ventas no existe")
    void testCreateTaxThrowsExceptionWhenSalesAccountNotFound() {
        doNothing().when(taxValidationService).validateTaxCodeNotExists(taxDTO.getCode(), taxDTO.getIdEnterprise());
        doThrow(new AccountCatalogueNotFoundException("La cuenta especificada no existe"))
                .when(taxValidationService).validateAccountDigits(taxDTO.getSalesTaxId(), taxDTO.getPurchaseTaxId(), taxDTO.getIdEnterprise());

        AccountCatalogueNotFoundException exception = assertThrows(AccountCatalogueNotFoundException.class,
                () -> taxCreateService.createTax(taxDTO));

        assertTrue(exception.getMessage().contains("cuenta especificada no existe"));
        verify(taxValidationService).validateTaxCodeNotExists(taxDTO.getCode(), taxDTO.getIdEnterprise());
        verify(taxValidationService).validateAccountDigits(taxDTO.getSalesTaxId(), taxDTO.getPurchaseTaxId(), taxDTO.getIdEnterprise());
        verify(taxValidationService, never()).validateDifferentTaxAccounts(any(), any());
        verify(taxCreateOutputPort, never()).createTax(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la cuenta de impuesto está inactiva")
    void testCreateTaxThrowsExceptionWhenAccountIsInactive() {
        doNothing().when(taxValidationService).validateTaxCodeNotExists(taxDTO.getCode(), taxDTO.getIdEnterprise());
        doThrow(new AccountCatalogueInactiveException("La cuenta '24080501' está inactiva"))
                .when(taxValidationService).validateAccountDigits(taxDTO.getSalesTaxId(), taxDTO.getPurchaseTaxId(), taxDTO.getIdEnterprise());

        AccountCatalogueInactiveException exception = assertThrows(AccountCatalogueInactiveException.class,
                () -> taxCreateService.createTax(taxDTO));

        assertTrue(exception.getMessage().contains("inactiva"));
        verify(taxValidationService).validateTaxCodeNotExists(taxDTO.getCode(), taxDTO.getIdEnterprise());
        verify(taxValidationService).validateAccountDigits(taxDTO.getSalesTaxId(), taxDTO.getPurchaseTaxId(), taxDTO.getIdEnterprise());
        verify(taxCreateOutputPort, never()).createTax(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la cuenta no tiene 8 dígitos")
    void testCreateTaxThrowsExceptionWhenAccountDigitsInvalid() {
        doNothing().when(taxValidationService).validateTaxCodeNotExists(taxDTO.getCode(), taxDTO.getIdEnterprise());
        doThrow(new InvalidAccountDigitsException())
                .when(taxValidationService).validateAccountDigits(taxDTO.getSalesTaxId(), taxDTO.getPurchaseTaxId(), taxDTO.getIdEnterprise());

        assertThrows(InvalidAccountDigitsException.class,
                () -> taxCreateService.createTax(taxDTO));

        verify(taxValidationService).validateTaxCodeNotExists(taxDTO.getCode(), taxDTO.getIdEnterprise());
        verify(taxValidationService).validateAccountDigits(taxDTO.getSalesTaxId(), taxDTO.getPurchaseTaxId(), taxDTO.getIdEnterprise());
        verify(taxCreateOutputPort, never()).createTax(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando las cuentas de venta y compra son iguales")
    void testCreateTaxThrowsExceptionWhenSameAccounts() {
        TaxDTO dtoWithSameAccounts = TaxDTO.builder()
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19%")
                .interest(19.0)
                .salesTaxId(100L)
                .purchaseTaxId(100L)
                .build();
        doNothing().when(taxValidationService).validateTaxCodeNotExists(dtoWithSameAccounts.getCode(), dtoWithSameAccounts.getIdEnterprise());
        doNothing().when(taxValidationService).validateAccountDigits(dtoWithSameAccounts.getSalesTaxId(), dtoWithSameAccounts.getPurchaseTaxId(), dtoWithSameAccounts.getIdEnterprise());
        doThrow(new DuplicateTaxAccountsException("Las cuentas de impuesto de venta e impuesto de compra deben ser diferentes"))
                .when(taxValidationService).validateDifferentTaxAccounts(dtoWithSameAccounts.getSalesTaxId(), dtoWithSameAccounts.getPurchaseTaxId());

        DuplicateTaxAccountsException exception = assertThrows(DuplicateTaxAccountsException.class,
                () -> taxCreateService.createTax(dtoWithSameAccounts));

        assertTrue(exception.getMessage().contains("diferentes"));
        verify(taxValidationService).validateTaxCodeNotExists(dtoWithSameAccounts.getCode(), dtoWithSameAccounts.getIdEnterprise());
        verify(taxValidationService).validateAccountDigits(dtoWithSameAccounts.getSalesTaxId(), dtoWithSameAccounts.getPurchaseTaxId(), dtoWithSameAccounts.getIdEnterprise());
        verify(taxValidationService).validateDifferentTaxAccounts(dtoWithSameAccounts.getSalesTaxId(), dtoWithSameAccounts.getPurchaseTaxId());
        verify(taxCreateOutputPort, never()).createTax(any());
    }

    @Test
    @DisplayName("Debe crear impuesto sin cuenta de ventas (salesTaxId null)")
    void testCreateTaxWithNullSalesTaxId() {
        TaxDTO dtoWithoutSales = TaxDTO.builder()
                .idEnterprise(idEnterprise)
                .code("RET4")
                .description("Retención 4%")
                .interest(4.0)
                .salesTaxId(null)
                .purchaseTaxId(200L)
                .build();
        Tax expectedTaxWithoutSales = Tax.builder()
                .id(2L)
                .idEnterprise(idEnterprise)
                .code("RET4")
                .description("Retención 4%")
                .interest(4.0)
                .status(true)
                .build();
        doNothing().when(taxValidationService).validateTaxCodeNotExists(dtoWithoutSales.getCode(), dtoWithoutSales.getIdEnterprise());
        doNothing().when(taxValidationService).validateAccountDigits(null, dtoWithoutSales.getPurchaseTaxId(), dtoWithoutSales.getIdEnterprise());
        doNothing().when(taxValidationService).validateDifferentTaxAccounts(null, dtoWithoutSales.getPurchaseTaxId());
        when(taxCreateOutputPort.createTax(dtoWithoutSales)).thenReturn(expectedTaxWithoutSales);

        Tax result = taxCreateService.createTax(dtoWithoutSales);

        assertNotNull(result);
        assertEquals("RET4", result.getCode());
        verify(taxCreateOutputPort).createTax(dtoWithoutSales);
    }

    @Test
    @DisplayName("Debe crear impuesto sin cuenta de compras (purchaseTaxId null)")
    void testCreateTaxWithNullPurchaseTaxId() {
        TaxDTO dtoWithoutPurchase = TaxDTO.builder()
                .idEnterprise(idEnterprise)
                .code("ICA")
                .description("Impuesto de industria y comercio")
                .interest(1.5)
                .salesTaxId(100L)
                .purchaseTaxId(null)
                .build();
        Tax expectedTaxWithoutPurchase = Tax.builder()
                .id(3L)
                .idEnterprise(idEnterprise)
                .code("ICA")
                .description("Impuesto de industria y comercio")
                .interest(1.5)
                .status(true)
                .build();
        doNothing().when(taxValidationService).validateTaxCodeNotExists(dtoWithoutPurchase.getCode(), dtoWithoutPurchase.getIdEnterprise());
        doNothing().when(taxValidationService).validateAccountDigits(dtoWithoutPurchase.getSalesTaxId(), null, dtoWithoutPurchase.getIdEnterprise());
        doNothing().when(taxValidationService).validateDifferentTaxAccounts(dtoWithoutPurchase.getSalesTaxId(), null);
        when(taxCreateOutputPort.createTax(dtoWithoutPurchase)).thenReturn(expectedTaxWithoutPurchase);

        Tax result = taxCreateService.createTax(dtoWithoutPurchase);

        assertNotNull(result);
        assertEquals("ICA", result.getCode());
        verify(taxCreateOutputPort).createTax(dtoWithoutPurchase);
    }

    @Test
    @DisplayName("Debe crear impuesto sin ninguna cuenta contable asociada")
    void testCreateTaxWithBothAccountsNull() {
        TaxDTO dtoWithoutAccounts = TaxDTO.builder()
                .idEnterprise(idEnterprise)
                .code("TASA")
                .description("Tasa especial")
                .interest(2.0)
                .salesTaxId(null)
                .purchaseTaxId(null)
                .build();
        Tax expectedTaxWithoutAccounts = Tax.builder()
                .id(4L)
                .idEnterprise(idEnterprise)
                .code("TASA")
                .description("Tasa especial")
                .interest(2.0)
                .status(true)
                .build();
        doNothing().when(taxValidationService).validateTaxCodeNotExists(dtoWithoutAccounts.getCode(), dtoWithoutAccounts.getIdEnterprise());
        doNothing().when(taxValidationService).validateAccountDigits(null, null, dtoWithoutAccounts.getIdEnterprise());
        doNothing().when(taxValidationService).validateDifferentTaxAccounts(null, null);
        when(taxCreateOutputPort.createTax(dtoWithoutAccounts)).thenReturn(expectedTaxWithoutAccounts);

        Tax result = taxCreateService.createTax(dtoWithoutAccounts);

        assertNotNull(result);
        assertEquals("TASA", result.getCode());
        verify(taxCreateOutputPort).createTax(dtoWithoutAccounts);
    }

    @Test
    @DisplayName("Debe ejecutar validaciones en el orden correcto")
    void testValidationsExecutedInCorrectOrder() {
        doNothing().when(taxValidationService).validateTaxCodeNotExists(taxDTO.getCode(), taxDTO.getIdEnterprise());
        doNothing().when(taxValidationService).validateAccountDigits(taxDTO.getSalesTaxId(), taxDTO.getPurchaseTaxId(), taxDTO.getIdEnterprise());
        doNothing().when(taxValidationService).validateDifferentTaxAccounts(taxDTO.getSalesTaxId(), taxDTO.getPurchaseTaxId());
        when(taxCreateOutputPort.createTax(taxDTO)).thenReturn(expectedTax);

        taxCreateService.createTax(taxDTO);

        var inOrder = inOrder(taxValidationService, taxCreateOutputPort);
        inOrder.verify(taxValidationService).validateTaxCodeNotExists(taxDTO.getCode(), taxDTO.getIdEnterprise());
        inOrder.verify(taxValidationService).validateAccountDigits(taxDTO.getSalesTaxId(), taxDTO.getPurchaseTaxId(), taxDTO.getIdEnterprise());
        inOrder.verify(taxValidationService).validateDifferentTaxAccounts(taxDTO.getSalesTaxId(), taxDTO.getPurchaseTaxId());
        inOrder.verify(taxCreateOutputPort).createTax(taxDTO);
    }

    @Test
    @DisplayName("Debe crear impuesto con código normalizado por validación")
    void testCreateTaxWithCodeNormalization() {
        TaxDTO dtoWithSpaces = TaxDTO.builder()
                .idEnterprise(idEnterprise)
                .code("  IVA 19  ")
                .description("Impuesto con espacios")
                .interest(19.0)
                .salesTaxId(100L)
                .purchaseTaxId(200L)
                .build();
        Tax expectedTaxNormalized = Tax.builder()
                .id(5L)
                .idEnterprise(idEnterprise)
                .code("IVA 19")
                .description("Impuesto con espacios")
                .interest(19.0)
                .status(true)
                .build();
        doNothing().when(taxValidationService).validateTaxCodeNotExists(dtoWithSpaces.getCode(), dtoWithSpaces.getIdEnterprise());
        doNothing().when(taxValidationService).validateAccountDigits(dtoWithSpaces.getSalesTaxId(), dtoWithSpaces.getPurchaseTaxId(), dtoWithSpaces.getIdEnterprise());
        doNothing().when(taxValidationService).validateDifferentTaxAccounts(dtoWithSpaces.getSalesTaxId(), dtoWithSpaces.getPurchaseTaxId());
        when(taxCreateOutputPort.createTax(dtoWithSpaces)).thenReturn(expectedTaxNormalized);

        Tax result = taxCreateService.createTax(dtoWithSpaces);

        assertNotNull(result);
        verify(taxValidationService).validateTaxCodeNotExists(dtoWithSpaces.getCode(), dtoWithSpaces.getIdEnterprise());
    }

    @Test
    @DisplayName("Debe crear impuesto con diferentes empresas")
    void testCreateTaxWithDifferentEnterprises() {
        String differentEnterpriseId = "ENT-002";
        TaxDTO dtoForDifferentEnterprise = TaxDTO.builder()
                .idEnterprise(differentEnterpriseId)
                .code("IVA5")
                .description("IVA 5%")
                .interest(5.0)
                .salesTaxId(300L)
                .purchaseTaxId(400L)
                .build();
        Tax expectedTaxDifferentEnterprise = Tax.builder()
                .id(6L)
                .idEnterprise(differentEnterpriseId)
                .code("IVA5")
                .description("IVA 5%")
                .interest(5.0)
                .status(true)
                .build();
        doNothing().when(taxValidationService).validateTaxCodeNotExists(dtoForDifferentEnterprise.getCode(), differentEnterpriseId);
        doNothing().when(taxValidationService).validateAccountDigits(dtoForDifferentEnterprise.getSalesTaxId(), dtoForDifferentEnterprise.getPurchaseTaxId(), differentEnterpriseId);
        doNothing().when(taxValidationService).validateDifferentTaxAccounts(dtoForDifferentEnterprise.getSalesTaxId(), dtoForDifferentEnterprise.getPurchaseTaxId());
        when(taxCreateOutputPort.createTax(dtoForDifferentEnterprise)).thenReturn(expectedTaxDifferentEnterprise);

        Tax result = taxCreateService.createTax(dtoForDifferentEnterprise);

        assertNotNull(result);
        assertEquals(differentEnterpriseId, result.getIdEnterprise());
        assertEquals("IVA5", result.getCode());
    }

    @Test
    @DisplayName("Debe propagar correctamente los datos del DTO al puerto de salida")
    void testCreateTaxPassesCorrectDataToOutputPort() {
        doNothing().when(taxValidationService).validateTaxCodeNotExists(taxDTO.getCode(), taxDTO.getIdEnterprise());
        doNothing().when(taxValidationService).validateAccountDigits(taxDTO.getSalesTaxId(), taxDTO.getPurchaseTaxId(), taxDTO.getIdEnterprise());
        doNothing().when(taxValidationService).validateDifferentTaxAccounts(taxDTO.getSalesTaxId(), taxDTO.getPurchaseTaxId());
        when(taxCreateOutputPort.createTax(taxDTO)).thenReturn(expectedTax);

        taxCreateService.createTax(taxDTO);

        verify(taxCreateOutputPort).createTax(argThat(dto ->
                dto.getCode().equals("IVA19") &&
                dto.getIdEnterprise().equals(idEnterprise) &&
                dto.getDescription().equals("Impuesto al valor agregado 19%") &&
                dto.getInterest().equals(19.0) &&
                dto.getSalesTaxId().equals(100L) &&
                dto.getPurchaseTaxId().equals(200L)
        ));
    }
}

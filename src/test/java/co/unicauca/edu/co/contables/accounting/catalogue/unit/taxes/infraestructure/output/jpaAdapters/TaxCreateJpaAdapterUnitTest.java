package co.unicauca.edu.co.contables.accounting.catalogue.unit.taxes.infraestructure.output.jpaAdapters;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.entity.AccountCatalogueEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.repository.IAccountCatalogueRepository;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.services.TaxValidationService;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.DTO.TaxDTO;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.models.Tax;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters.TaxCreateJpaAdapter;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters.entity.TaxEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters.mapper.ITaxCreateMapper;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TaxCreateJpaAdapterUnitTest {

    @Mock
    private ITaxCreateMapper taxCreateMapper;

    @Mock
    private ITaxRepository taxRepository;

    @Mock
    private IAccountCatalogueRepository accountCatalogueRepository;

    @Mock
    private TaxValidationService taxValidationService;

    @InjectMocks
    private TaxCreateJpaAdapter taxCreateJpaAdapter;

    private String idEnterprise;
    private Long taxId;
    private Long salesTaxId;
    private Long purchaseTaxId;
    private TaxDTO taxDTO;
    private Tax tax;
    private TaxEntity taxEntity;
    private AccountCatalogueEntity salesTaxAccount;
    private AccountCatalogueEntity purchaseTaxAccount;

    @BeforeEach
    void setUp() {
        idEnterprise = "ENT-001";
        taxId = 1L;
        salesTaxId = 100L;
        purchaseTaxId = 200L;

        taxDTO = TaxDTO.builder()
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19%")
                .interest(19.0)
                .salesTaxId(salesTaxId)
                .purchaseTaxId(purchaseTaxId)
                .build();

        salesTaxAccount = AccountCatalogueEntity.builder()
                .id(salesTaxId)
                .idEnterprise(idEnterprise)
                .code("24080501")
                .description("Cuenta de ventas")
                .status(true)
                .salesTaxes(new ArrayList<>())
                .build();

        purchaseTaxAccount = AccountCatalogueEntity.builder()
                .id(purchaseTaxId)
                .idEnterprise(idEnterprise)
                .code("24080502")
                .description("Cuenta de compras")
                .status(true)
                .purchaseTaxes(new ArrayList<>())
                .build();

        taxEntity = TaxEntity.builder()
                .id(taxId)
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19%")
                .interest(19.0)
                .status(true)
                .usageCount(0)
                .salesTax(salesTaxAccount)
                .purchaseTax(purchaseTaxAccount)
                .build();

        tax = Tax.builder()
                .id(taxId)
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19%")
                .interest(19.0)
                .status(true)
                .usageCount(0)
                .build();
    }

    @Test
    @DisplayName("Debe crear impuesto exitosamente con cuentas de venta y compra")
    void testCreateTaxWithBothAccounts() {
        // Arrange
        doNothing().when(taxValidationService).validateTaxCodeNotExists(taxDTO.getCode(), taxDTO.getIdEnterprise());
        doNothing().when(taxValidationService).validateAccountDigits(salesTaxId, purchaseTaxId, idEnterprise);
        doNothing().when(taxValidationService).validateDifferentTaxAccounts(salesTaxId, purchaseTaxId);
        when(accountCatalogueRepository.findByIdAndIdEnterprise(salesTaxId, idEnterprise)).thenReturn(salesTaxAccount);
        when(accountCatalogueRepository.findByIdAndIdEnterprise(purchaseTaxId, idEnterprise)).thenReturn(purchaseTaxAccount);
        when(taxCreateMapper.toEntity(any(Tax.class))).thenReturn(taxEntity);
        when(taxRepository.save(any(TaxEntity.class))).thenReturn(taxEntity);
        when(taxCreateMapper.toModel(taxEntity)).thenReturn(tax);

        // Act
        Tax result = taxCreateJpaAdapter.createTax(taxDTO);

        // Assert
        assertNotNull(result);
        assertEquals(taxId, result.getId());
        assertEquals("IVA19", result.getCode());
        verify(taxValidationService).validateTaxCodeNotExists(taxDTO.getCode(), taxDTO.getIdEnterprise());
        verify(taxValidationService).validateAccountDigits(salesTaxId, purchaseTaxId, idEnterprise);
        verify(taxValidationService).validateDifferentTaxAccounts(salesTaxId, purchaseTaxId);
        verify(taxRepository).save(any(TaxEntity.class));
    }

    @Test
    @DisplayName("Debe crear impuesto exitosamente sin cuenta de ventas")
    void testCreateTaxWithoutSalesAccount() {
        // Arrange
        TaxDTO dtoWithoutSales = TaxDTO.builder()
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19%")
                .interest(19.0)
                .salesTaxId(null)
                .purchaseTaxId(purchaseTaxId)
                .build();
        doNothing().when(taxValidationService).validateTaxCodeNotExists(anyString(), anyString());
        doNothing().when(taxValidationService).validateAccountDigits(any(), any(), anyString());
        doNothing().when(taxValidationService).validateDifferentTaxAccounts(any(), any());
        when(accountCatalogueRepository.findByIdAndIdEnterprise(purchaseTaxId, idEnterprise)).thenReturn(purchaseTaxAccount);
        when(taxCreateMapper.toEntity(any(Tax.class))).thenReturn(taxEntity);
        when(taxRepository.save(any(TaxEntity.class))).thenReturn(taxEntity);
        when(taxCreateMapper.toModel(taxEntity)).thenReturn(tax);

        // Act
        Tax result = taxCreateJpaAdapter.createTax(dtoWithoutSales);

        // Assert
        assertNotNull(result);
        verify(accountCatalogueRepository, never()).findByIdAndIdEnterprise(eq(salesTaxId), anyString());
        verify(accountCatalogueRepository).findByIdAndIdEnterprise(purchaseTaxId, idEnterprise);
    }

    @Test
    @DisplayName("Debe crear impuesto exitosamente sin cuenta de compras")
    void testCreateTaxWithoutPurchaseAccount() {
        // Arrange
        TaxDTO dtoWithoutPurchase = TaxDTO.builder()
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19%")
                .interest(19.0)
                .salesTaxId(salesTaxId)
                .purchaseTaxId(null)
                .build();
        doNothing().when(taxValidationService).validateTaxCodeNotExists(anyString(), anyString());
        doNothing().when(taxValidationService).validateAccountDigits(any(), any(), anyString());
        doNothing().when(taxValidationService).validateDifferentTaxAccounts(any(), any());
        when(accountCatalogueRepository.findByIdAndIdEnterprise(salesTaxId, idEnterprise)).thenReturn(salesTaxAccount);
        when(taxCreateMapper.toEntity(any(Tax.class))).thenReturn(taxEntity);
        when(taxRepository.save(any(TaxEntity.class))).thenReturn(taxEntity);
        when(taxCreateMapper.toModel(taxEntity)).thenReturn(tax);

        // Act
        Tax result = taxCreateJpaAdapter.createTax(dtoWithoutPurchase);

        // Assert
        assertNotNull(result);
        verify(accountCatalogueRepository).findByIdAndIdEnterprise(salesTaxId, idEnterprise);
        verify(accountCatalogueRepository, never()).findByIdAndIdEnterprise(eq(purchaseTaxId), anyString());
    }

    @Test
    @DisplayName("Debe crear impuesto sin cuentas asociadas")
    void testCreateTaxWithoutAnyAccount() {
        // Arrange
        TaxDTO dtoWithoutAccounts = TaxDTO.builder()
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19%")
                .interest(19.0)
                .salesTaxId(null)
                .purchaseTaxId(null)
                .build();
        doNothing().when(taxValidationService).validateTaxCodeNotExists(anyString(), anyString());
        doNothing().when(taxValidationService).validateAccountDigits(any(), any(), anyString());
        doNothing().when(taxValidationService).validateDifferentTaxAccounts(any(), any());
        when(taxCreateMapper.toEntity(any(Tax.class))).thenReturn(taxEntity);
        when(taxRepository.save(any(TaxEntity.class))).thenReturn(taxEntity);
        when(taxCreateMapper.toModel(taxEntity)).thenReturn(tax);

        // Act
        Tax result = taxCreateJpaAdapter.createTax(dtoWithoutAccounts);

        // Assert
        assertNotNull(result);
        verify(accountCatalogueRepository, never()).findByIdAndIdEnterprise(anyLong(), anyString());
    }

    @Test
    @DisplayName("Debe invocar validaciones antes de crear el impuesto")
    void testCreateTaxInvokesValidationsFirst() {
        // Arrange
        doNothing().when(taxValidationService).validateTaxCodeNotExists(taxDTO.getCode(), taxDTO.getIdEnterprise());
        doNothing().when(taxValidationService).validateAccountDigits(salesTaxId, purchaseTaxId, idEnterprise);
        doNothing().when(taxValidationService).validateDifferentTaxAccounts(salesTaxId, purchaseTaxId);
        when(accountCatalogueRepository.findByIdAndIdEnterprise(salesTaxId, idEnterprise)).thenReturn(salesTaxAccount);
        when(accountCatalogueRepository.findByIdAndIdEnterprise(purchaseTaxId, idEnterprise)).thenReturn(purchaseTaxAccount);
        when(taxCreateMapper.toEntity(any(Tax.class))).thenReturn(taxEntity);
        when(taxRepository.save(any(TaxEntity.class))).thenReturn(taxEntity);
        when(taxCreateMapper.toModel(taxEntity)).thenReturn(tax);

        // Act
        taxCreateJpaAdapter.createTax(taxDTO);

        // Assert
        var inOrder = inOrder(taxValidationService, accountCatalogueRepository, taxRepository);
        inOrder.verify(taxValidationService).validateTaxCodeNotExists(taxDTO.getCode(), taxDTO.getIdEnterprise());
        inOrder.verify(taxValidationService).validateAccountDigits(salesTaxId, purchaseTaxId, idEnterprise);
        inOrder.verify(taxValidationService).validateDifferentTaxAccounts(salesTaxId, purchaseTaxId);
        inOrder.verify(accountCatalogueRepository).findByIdAndIdEnterprise(salesTaxId, idEnterprise);
        inOrder.verify(taxRepository).save(any(TaxEntity.class));
    }

    @Test
    @DisplayName("Debe agregar entidad a la colección salesTaxes cuando existe cuenta de ventas")
    void testCreateTaxAddsSalesTaxToCollection() {
        // Arrange
        doNothing().when(taxValidationService).validateTaxCodeNotExists(anyString(), anyString());
        doNothing().when(taxValidationService).validateAccountDigits(any(), any(), anyString());
        doNothing().when(taxValidationService).validateDifferentTaxAccounts(any(), any());
        when(accountCatalogueRepository.findByIdAndIdEnterprise(salesTaxId, idEnterprise)).thenReturn(salesTaxAccount);
        when(accountCatalogueRepository.findByIdAndIdEnterprise(purchaseTaxId, idEnterprise)).thenReturn(purchaseTaxAccount);
        when(taxCreateMapper.toEntity(any(Tax.class))).thenReturn(taxEntity);
        when(taxRepository.save(any(TaxEntity.class))).thenReturn(taxEntity);
        when(taxCreateMapper.toModel(taxEntity)).thenReturn(tax);

        // Act
        taxCreateJpaAdapter.createTax(taxDTO);

        // Assert
        assertTrue(salesTaxAccount.getSalesTaxes().contains(taxEntity));
    }

    @Test
    @DisplayName("Debe agregar entidad a la colección purchaseTaxes cuando existe cuenta de compras")
    void testCreateTaxAddsPurchaseTaxToCollection() {
        // Arrange
        doNothing().when(taxValidationService).validateTaxCodeNotExists(anyString(), anyString());
        doNothing().when(taxValidationService).validateAccountDigits(any(), any(), anyString());
        doNothing().when(taxValidationService).validateDifferentTaxAccounts(any(), any());
        when(accountCatalogueRepository.findByIdAndIdEnterprise(salesTaxId, idEnterprise)).thenReturn(salesTaxAccount);
        when(accountCatalogueRepository.findByIdAndIdEnterprise(purchaseTaxId, idEnterprise)).thenReturn(purchaseTaxAccount);
        when(taxCreateMapper.toEntity(any(Tax.class))).thenReturn(taxEntity);
        when(taxRepository.save(any(TaxEntity.class))).thenReturn(taxEntity);
        when(taxCreateMapper.toModel(taxEntity)).thenReturn(tax);

        // Act
        taxCreateJpaAdapter.createTax(taxDTO);

        // Assert
        assertTrue(purchaseTaxAccount.getPurchaseTaxes().contains(taxEntity));
    }

    @Test
    @DisplayName("Debe inicializar colección salesTaxes cuando es null")
    void testCreateTaxInitializesSalesTaxesWhenNull() {
        // Arrange
        salesTaxAccount.setSalesTaxes(null);
        doNothing().when(taxValidationService).validateTaxCodeNotExists(anyString(), anyString());
        doNothing().when(taxValidationService).validateAccountDigits(any(), any(), anyString());
        doNothing().when(taxValidationService).validateDifferentTaxAccounts(any(), any());
        when(accountCatalogueRepository.findByIdAndIdEnterprise(salesTaxId, idEnterprise)).thenReturn(salesTaxAccount);
        when(accountCatalogueRepository.findByIdAndIdEnterprise(purchaseTaxId, idEnterprise)).thenReturn(purchaseTaxAccount);
        when(taxCreateMapper.toEntity(any(Tax.class))).thenReturn(taxEntity);
        when(taxRepository.save(any(TaxEntity.class))).thenReturn(taxEntity);
        when(taxCreateMapper.toModel(taxEntity)).thenReturn(tax);

        // Act
        taxCreateJpaAdapter.createTax(taxDTO);

        // Assert
        assertNotNull(salesTaxAccount.getSalesTaxes());
        assertTrue(salesTaxAccount.getSalesTaxes().contains(taxEntity));
    }

    @Test
    @DisplayName("Debe inicializar colección purchaseTaxes cuando es null")
    void testCreateTaxInitializesPurchaseTaxesWhenNull() {
        // Arrange
        purchaseTaxAccount.setPurchaseTaxes(null);
        doNothing().when(taxValidationService).validateTaxCodeNotExists(anyString(), anyString());
        doNothing().when(taxValidationService).validateAccountDigits(any(), any(), anyString());
        doNothing().when(taxValidationService).validateDifferentTaxAccounts(any(), any());
        when(accountCatalogueRepository.findByIdAndIdEnterprise(salesTaxId, idEnterprise)).thenReturn(salesTaxAccount);
        when(accountCatalogueRepository.findByIdAndIdEnterprise(purchaseTaxId, idEnterprise)).thenReturn(purchaseTaxAccount);
        when(taxCreateMapper.toEntity(any(Tax.class))).thenReturn(taxEntity);
        when(taxRepository.save(any(TaxEntity.class))).thenReturn(taxEntity);
        when(taxCreateMapper.toModel(taxEntity)).thenReturn(tax);

        // Act
        taxCreateJpaAdapter.createTax(taxDTO);

        // Assert
        assertNotNull(purchaseTaxAccount.getPurchaseTaxes());
        assertTrue(purchaseTaxAccount.getPurchaseTaxes().contains(taxEntity));
    }

    @Test
    @DisplayName("Debe asignar cuentas relacionadas a la entidad antes de guardar")
    void testCreateTaxAssignsRelatedAccountsToEntity() {
        // Arrange
        TaxEntity entityWithoutAccounts = TaxEntity.builder()
                .id(taxId)
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19%")
                .interest(19.0)
                .status(true)
                .usageCount(0)
                .build();
        doNothing().when(taxValidationService).validateTaxCodeNotExists(anyString(), anyString());
        doNothing().when(taxValidationService).validateAccountDigits(any(), any(), anyString());
        doNothing().when(taxValidationService).validateDifferentTaxAccounts(any(), any());
        when(accountCatalogueRepository.findByIdAndIdEnterprise(salesTaxId, idEnterprise)).thenReturn(salesTaxAccount);
        when(accountCatalogueRepository.findByIdAndIdEnterprise(purchaseTaxId, idEnterprise)).thenReturn(purchaseTaxAccount);
        when(taxCreateMapper.toEntity(any(Tax.class))).thenReturn(entityWithoutAccounts);
        when(taxRepository.save(any(TaxEntity.class))).thenAnswer(invocation -> {
            TaxEntity saved = invocation.getArgument(0);
            assertEquals(salesTaxAccount, saved.getSalesTax());
            assertEquals(purchaseTaxAccount, saved.getPurchaseTax());
            return saved;
        });
        when(taxCreateMapper.toModel(any(TaxEntity.class))).thenReturn(tax);

        // Act
        taxCreateJpaAdapter.createTax(taxDTO);

        // Assert
        verify(taxRepository).save(argThat(entity -> 
            entity.getSalesTax() == salesTaxAccount && 
            entity.getPurchaseTax() == purchaseTaxAccount
        ));
    }

    @Test
    @DisplayName("Debe mapear correctamente el modelo de dominio intermedio")
    void testCreateTaxMapsIntermediateDomainModel() {
        // Arrange
        doNothing().when(taxValidationService).validateTaxCodeNotExists(anyString(), anyString());
        doNothing().when(taxValidationService).validateAccountDigits(any(), any(), anyString());
        doNothing().when(taxValidationService).validateDifferentTaxAccounts(any(), any());
        when(accountCatalogueRepository.findByIdAndIdEnterprise(salesTaxId, idEnterprise)).thenReturn(salesTaxAccount);
        when(accountCatalogueRepository.findByIdAndIdEnterprise(purchaseTaxId, idEnterprise)).thenReturn(purchaseTaxAccount);
        when(taxCreateMapper.toEntity(any(Tax.class))).thenReturn(taxEntity);
        when(taxRepository.save(any(TaxEntity.class))).thenReturn(taxEntity);
        when(taxCreateMapper.toModel(taxEntity)).thenReturn(tax);

        // Act
        taxCreateJpaAdapter.createTax(taxDTO);

        // Assert
        verify(taxCreateMapper).toEntity(argThat(taxArg ->
            taxArg.getCode().equals("IVA19") &&
            taxArg.getDescription().equals("Impuesto al valor agregado 19%") &&
            taxArg.getInterest().equals(19.0) &&
            taxArg.getIdEnterprise().equals(idEnterprise)
        ));
    }

    @Test
    @DisplayName("Debe retornar el modelo mapeado desde la entidad guardada")
    void testCreateTaxReturnsMappedModel() {
        // Arrange
        Tax expectedTax = Tax.builder()
                .id(taxId)
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19%")
                .interest(19.0)
                .status(true)
                .usageCount(0)
                .build();
        doNothing().when(taxValidationService).validateTaxCodeNotExists(anyString(), anyString());
        doNothing().when(taxValidationService).validateAccountDigits(any(), any(), anyString());
        doNothing().when(taxValidationService).validateDifferentTaxAccounts(any(), any());
        when(accountCatalogueRepository.findByIdAndIdEnterprise(salesTaxId, idEnterprise)).thenReturn(salesTaxAccount);
        when(accountCatalogueRepository.findByIdAndIdEnterprise(purchaseTaxId, idEnterprise)).thenReturn(purchaseTaxAccount);
        when(taxCreateMapper.toEntity(any(Tax.class))).thenReturn(taxEntity);
        when(taxRepository.save(any(TaxEntity.class))).thenReturn(taxEntity);
        when(taxCreateMapper.toModel(taxEntity)).thenReturn(expectedTax);

        // Act
        Tax result = taxCreateJpaAdapter.createTax(taxDTO);

        // Assert
        assertEquals(expectedTax.getId(), result.getId());
        assertEquals(expectedTax.getCode(), result.getCode());
        assertEquals(expectedTax.getDescription(), result.getDescription());
        assertEquals(expectedTax.getInterest(), result.getInterest());
        verify(taxCreateMapper).toModel(taxEntity);
    }

    @Test
    @DisplayName("Debe propagar excepción cuando validación de código duplicado falla")
    void testCreateTaxThrowsExceptionWhenCodeExists() {
        // Arrange
        RuntimeException expectedException = new RuntimeException("El código ya existe");
        doThrow(expectedException).when(taxValidationService).validateTaxCodeNotExists(taxDTO.getCode(), taxDTO.getIdEnterprise());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> taxCreateJpaAdapter.createTax(taxDTO));
        
        assertEquals("El código ya existe", exception.getMessage());
        verify(taxRepository, never()).save(any(TaxEntity.class));
    }

    @Test
    @DisplayName("Debe propagar excepción cuando validación de dígitos de cuenta falla")
    void testCreateTaxThrowsExceptionWhenAccountDigitsInvalid() {
        // Arrange
        doNothing().when(taxValidationService).validateTaxCodeNotExists(anyString(), anyString());
        RuntimeException expectedException = new RuntimeException("Dígitos de cuenta inválidos");
        doThrow(expectedException).when(taxValidationService).validateAccountDigits(salesTaxId, purchaseTaxId, idEnterprise);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> taxCreateJpaAdapter.createTax(taxDTO));
        
        assertEquals("Dígitos de cuenta inválidos", exception.getMessage());
        verify(taxRepository, never()).save(any(TaxEntity.class));
    }

    @Test
    @DisplayName("Debe propagar excepción cuando cuentas de impuestos son iguales")
    void testCreateTaxThrowsExceptionWhenSameAccounts() {
        // Arrange
        doNothing().when(taxValidationService).validateTaxCodeNotExists(anyString(), anyString());
        doNothing().when(taxValidationService).validateAccountDigits(any(), any(), anyString());
        RuntimeException expectedException = new RuntimeException("Las cuentas deben ser diferentes");
        doThrow(expectedException).when(taxValidationService).validateDifferentTaxAccounts(salesTaxId, purchaseTaxId);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> taxCreateJpaAdapter.createTax(taxDTO));
        
        assertEquals("Las cuentas deben ser diferentes", exception.getMessage());
        verify(taxRepository, never()).save(any(TaxEntity.class));
    }
}

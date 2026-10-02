package co.unicauca.edu.co.contables.accounting.catalogue.unit.taxes.infraestructure.output.jpaAdapters;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.entity.AccountCatalogueEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.repository.IAccountCatalogueRepository;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.services.TaxValidationService;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.DTO.TaxDTO;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.models.Tax;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters.TaxUpdateJpaAdapter;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters.entity.TaxEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters.mapper.ITaxUpdateMapper;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TaxUpdateJpaAdapterUnitTest {

    @Mock
    private ITaxRepository taxRepository;

    @Mock
    private ITaxUpdateMapper taxUpdateMapper;

    @Mock
    private IAccountCatalogueRepository accountCatalogueRepository;

    @Mock
    private TaxValidationService taxValidationService;

    @InjectMocks
    private TaxUpdateJpaAdapter taxUpdateJpaAdapter;

    private String idEnterprise;
    private Long taxId;
    private Long salesTaxId;
    private Long purchaseTaxId;
    private TaxDTO taxDTO;
    private TaxEntity taxEntity;
    private Tax tax;
    private AccountCatalogueEntity salesTaxAccount;
    private AccountCatalogueEntity purchaseTaxAccount;

    @BeforeEach
    void setUp() {
        idEnterprise = "ENT-001";
        taxId = 1L;
        salesTaxId = 100L;
        purchaseTaxId = 200L;

        salesTaxAccount = AccountCatalogueEntity.builder()
                .id(salesTaxId)
                .idEnterprise(idEnterprise)
                .code("24080501")
                .description("Cuenta de ventas")
                .status(true)
                .build();

        purchaseTaxAccount = AccountCatalogueEntity.builder()
                .id(purchaseTaxId)
                .idEnterprise(idEnterprise)
                .code("24080502")
                .description("Cuenta de compras")
                .status(true)
                .build();

        taxDTO = TaxDTO.builder()
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19%")
                .interest(19.0)
                .salesTaxId(salesTaxId)
                .purchaseTaxId(purchaseTaxId)
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
    @DisplayName("Debe actualizar impuesto exitosamente sin cambiar cuentas ni código")
    void testUpdateSuccessWithoutChangingAccountsOrCode() {
        // Arrange
        TaxDTO updateDTO = TaxDTO.builder()
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("IVA 19% actualizado")
                .interest(19.5)
                .salesTaxId(salesTaxId)
                .purchaseTaxId(purchaseTaxId)
                .build();
        when(taxRepository.findByIdAndIdEnterprise(taxId, idEnterprise)).thenReturn(taxEntity);
        doNothing().when(taxValidationService).validateAccountDigits(salesTaxId, purchaseTaxId, idEnterprise);
        doNothing().when(taxValidationService).validateDifferentTaxAccounts(salesTaxId, purchaseTaxId);
        when(taxRepository.save(any(TaxEntity.class))).thenReturn(taxEntity);
        when(taxUpdateMapper.toModel(taxEntity)).thenReturn(tax);

        // Act
        Tax result = taxUpdateJpaAdapter.update(updateDTO, taxId);

        // Assert
        assertNotNull(result);
        verify(taxRepository).findByIdAndIdEnterprise(taxId, idEnterprise);
        verify(taxValidationService).validateAccountDigits(salesTaxId, purchaseTaxId, idEnterprise);
        verify(taxValidationService).validateDifferentTaxAccounts(salesTaxId, purchaseTaxId);
        verify(taxValidationService, never()).validateTaxCodeNotExists(anyString(), anyString());
        verify(accountCatalogueRepository, never()).findByIdAndIdEnterprise(anyLong(), anyString());
        verify(taxRepository).save(any(TaxEntity.class));
    }

    @Test
    @DisplayName("Debe retornar null cuando el impuesto no existe")
    void testUpdateReturnsNullWhenTaxNotFound() {
        // Arrange
        when(taxRepository.findByIdAndIdEnterprise(taxId, idEnterprise)).thenReturn(null);

        // Act
        Tax result = taxUpdateJpaAdapter.update(taxDTO, taxId);

        // Assert
        assertNull(result);
        verify(taxRepository).findByIdAndIdEnterprise(taxId, idEnterprise);
        verify(taxRepository, never()).save(any(TaxEntity.class));
        verify(taxValidationService, never()).validateAccountDigits(any(), any(), anyString());
    }

    @Test
    @DisplayName("Debe actualizar código cuando cambia y validar que no existe")
    void testUpdateValidatesCodeWhenChanged() {
        // Arrange
        TaxDTO updateDTO = TaxDTO.builder()
                .idEnterprise(idEnterprise)
                .code("IVA21")
                .description("Impuesto al valor agregado 21%")
                .interest(21.0)
                .salesTaxId(salesTaxId)
                .purchaseTaxId(purchaseTaxId)
                .build();
        when(taxRepository.findByIdAndIdEnterprise(taxId, idEnterprise)).thenReturn(taxEntity);
        doNothing().when(taxValidationService).validateAccountDigits(any(), any(), anyString());
        doNothing().when(taxValidationService).validateDifferentTaxAccounts(any(), any());
        doNothing().when(taxValidationService).validateTaxCodeNotExists("IVA21", idEnterprise);
        when(taxRepository.save(any(TaxEntity.class))).thenReturn(taxEntity);
        when(taxUpdateMapper.toModel(taxEntity)).thenReturn(tax);

        // Act
        Tax result = taxUpdateJpaAdapter.update(updateDTO, taxId);

        // Assert
        assertNotNull(result);
        verify(taxValidationService).validateTaxCodeNotExists("IVA21", idEnterprise);
    }

    @Test
    @DisplayName("Debe actualizar salesTax cuando cambia el ID")
    void testUpdateSalesTaxWhenIdChanges() {
        // Arrange
        Long newSalesTaxId = 300L;
        AccountCatalogueEntity newSalesTax = AccountCatalogueEntity.builder()
                .id(newSalesTaxId)
                .idEnterprise(idEnterprise)
                .code("24080503")
                .build();
        TaxDTO updateDTO = TaxDTO.builder()
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19%")
                .interest(19.0)
                .salesTaxId(newSalesTaxId)
                .purchaseTaxId(purchaseTaxId)
                .build();
        when(taxRepository.findByIdAndIdEnterprise(taxId, idEnterprise)).thenReturn(taxEntity);
        doNothing().when(taxValidationService).validateAccountDigits(any(), any(), anyString());
        doNothing().when(taxValidationService).validateDifferentTaxAccounts(any(), any());
        when(accountCatalogueRepository.findByIdAndIdEnterprise(newSalesTaxId, idEnterprise)).thenReturn(newSalesTax);
        when(taxRepository.save(any(TaxEntity.class))).thenReturn(taxEntity);
        when(taxUpdateMapper.toModel(taxEntity)).thenReturn(tax);

        // Act
        Tax result = taxUpdateJpaAdapter.update(updateDTO, taxId);

        // Assert
        assertNotNull(result);
        verify(accountCatalogueRepository).findByIdAndIdEnterprise(newSalesTaxId, idEnterprise);
    }

    @Test
    @DisplayName("Debe actualizar purchaseTax cuando cambia el ID")
    void testUpdatePurchaseTaxWhenIdChanges() {
        // Arrange
        Long newPurchaseTaxId = 400L;
        AccountCatalogueEntity newPurchaseTax = AccountCatalogueEntity.builder()
                .id(newPurchaseTaxId)
                .idEnterprise(idEnterprise)
                .code("24080504")
                .build();
        TaxDTO updateDTO = TaxDTO.builder()
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19%")
                .interest(19.0)
                .salesTaxId(salesTaxId)
                .purchaseTaxId(newPurchaseTaxId)
                .build();
        when(taxRepository.findByIdAndIdEnterprise(taxId, idEnterprise)).thenReturn(taxEntity);
        doNothing().when(taxValidationService).validateAccountDigits(any(), any(), anyString());
        doNothing().when(taxValidationService).validateDifferentTaxAccounts(any(), any());
        when(accountCatalogueRepository.findByIdAndIdEnterprise(newPurchaseTaxId, idEnterprise)).thenReturn(newPurchaseTax);
        when(taxRepository.save(any(TaxEntity.class))).thenReturn(taxEntity);
        when(taxUpdateMapper.toModel(taxEntity)).thenReturn(tax);

        // Act
        Tax result = taxUpdateJpaAdapter.update(updateDTO, taxId);

        // Assert
        assertNotNull(result);
        verify(accountCatalogueRepository).findByIdAndIdEnterprise(newPurchaseTaxId, idEnterprise);
    }

    @Test
    @DisplayName("Debe establecer salesTax a null cuando se elimina")
    void testUpdateSetsSalesTaxToNullWhenRemoved() {
        // Arrange
        TaxDTO updateDTO = TaxDTO.builder()
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19%")
                .interest(19.0)
                .salesTaxId(null)
                .purchaseTaxId(purchaseTaxId)
                .build();
        when(taxRepository.findByIdAndIdEnterprise(taxId, idEnterprise)).thenReturn(taxEntity);
        doNothing().when(taxValidationService).validateAccountDigits(any(), any(), anyString());
        doNothing().when(taxValidationService).validateDifferentTaxAccounts(any(), any());
        when(taxRepository.save(any(TaxEntity.class))).thenAnswer(invocation -> {
            TaxEntity saved = invocation.getArgument(0);
            assertNull(saved.getSalesTax());
            return saved;
        });
        when(taxUpdateMapper.toModel(any(TaxEntity.class))).thenReturn(tax);

        // Act
        Tax result = taxUpdateJpaAdapter.update(updateDTO, taxId);

        // Assert
        assertNotNull(result);
        verify(accountCatalogueRepository, never()).findByIdAndIdEnterprise(eq(salesTaxId), anyString());
    }

    @Test
    @DisplayName("Debe establecer purchaseTax a null cuando se elimina")
    void testUpdateSetsPurchaseTaxToNullWhenRemoved() {
        // Arrange
        TaxDTO updateDTO = TaxDTO.builder()
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19%")
                .interest(19.0)
                .salesTaxId(salesTaxId)
                .purchaseTaxId(null)
                .build();
        when(taxRepository.findByIdAndIdEnterprise(taxId, idEnterprise)).thenReturn(taxEntity);
        doNothing().when(taxValidationService).validateAccountDigits(any(), any(), anyString());
        doNothing().when(taxValidationService).validateDifferentTaxAccounts(any(), any());
        when(taxRepository.save(any(TaxEntity.class))).thenAnswer(invocation -> {
            TaxEntity saved = invocation.getArgument(0);
            assertNull(saved.getPurchaseTax());
            return saved;
        });
        when(taxUpdateMapper.toModel(any(TaxEntity.class))).thenReturn(tax);

        // Act
        Tax result = taxUpdateJpaAdapter.update(updateDTO, taxId);

        // Assert
        assertNotNull(result);
        verify(accountCatalogueRepository, never()).findByIdAndIdEnterprise(eq(purchaseTaxId), anyString());
    }

    @Test
    @DisplayName("Debe manejar entidad sin salesTax previo al actualizar")
    void testUpdateHandlesNullPreviousSalesTax() {
        // Arrange
        TaxEntity entityWithoutSales = TaxEntity.builder()
                .id(taxId)
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19%")
                .interest(19.0)
                .status(true)
                .salesTax(null)
                .purchaseTax(purchaseTaxAccount)
                .build();
        when(taxRepository.findByIdAndIdEnterprise(taxId, idEnterprise)).thenReturn(entityWithoutSales);
        doNothing().when(taxValidationService).validateAccountDigits(any(), any(), anyString());
        doNothing().when(taxValidationService).validateDifferentTaxAccounts(any(), any());
        when(accountCatalogueRepository.findByIdAndIdEnterprise(salesTaxId, idEnterprise)).thenReturn(salesTaxAccount);
        when(taxRepository.save(any(TaxEntity.class))).thenReturn(entityWithoutSales);
        when(taxUpdateMapper.toModel(any(TaxEntity.class))).thenReturn(tax);

        // Act
        Tax result = taxUpdateJpaAdapter.update(taxDTO, taxId);

        // Assert
        assertNotNull(result);
        verify(accountCatalogueRepository).findByIdAndIdEnterprise(salesTaxId, idEnterprise);
    }

    @Test
    @DisplayName("Debe manejar entidad sin purchaseTax previo al actualizar")
    void testUpdateHandlesNullPreviousPurchaseTax() {
        // Arrange
        TaxEntity entityWithoutPurchase = TaxEntity.builder()
                .id(taxId)
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19%")
                .interest(19.0)
                .status(true)
                .salesTax(salesTaxAccount)
                .purchaseTax(null)
                .build();
        when(taxRepository.findByIdAndIdEnterprise(taxId, idEnterprise)).thenReturn(entityWithoutPurchase);
        doNothing().when(taxValidationService).validateAccountDigits(any(), any(), anyString());
        doNothing().when(taxValidationService).validateDifferentTaxAccounts(any(), any());
        when(accountCatalogueRepository.findByIdAndIdEnterprise(purchaseTaxId, idEnterprise)).thenReturn(purchaseTaxAccount);
        when(taxRepository.save(any(TaxEntity.class))).thenReturn(entityWithoutPurchase);
        when(taxUpdateMapper.toModel(any(TaxEntity.class))).thenReturn(tax);

        // Act
        Tax result = taxUpdateJpaAdapter.update(taxDTO, taxId);

        // Assert
        assertNotNull(result);
        verify(accountCatalogueRepository).findByIdAndIdEnterprise(purchaseTaxId, idEnterprise);
    }

    @Test
    @DisplayName("Debe propagar excepción cuando validación de dígitos falla")
    void testUpdateThrowsExceptionWhenAccountDigitsInvalid() {
        // Arrange
        when(taxRepository.findByIdAndIdEnterprise(taxId, idEnterprise)).thenReturn(taxEntity);
        RuntimeException expectedException = new RuntimeException("Dígitos de cuenta inválidos");
        doThrow(expectedException).when(taxValidationService).validateAccountDigits(salesTaxId, purchaseTaxId, idEnterprise);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> taxUpdateJpaAdapter.update(taxDTO, taxId));
        
        assertEquals("Dígitos de cuenta inválidos", exception.getMessage());
        verify(taxRepository, never()).save(any(TaxEntity.class));
    }

    @Test
    @DisplayName("Debe propagar excepción cuando cuentas son iguales")
    void testUpdateThrowsExceptionWhenSameAccounts() {
        // Arrange
        when(taxRepository.findByIdAndIdEnterprise(taxId, idEnterprise)).thenReturn(taxEntity);
        doNothing().when(taxValidationService).validateAccountDigits(any(), any(), anyString());
        RuntimeException expectedException = new RuntimeException("Las cuentas deben ser diferentes");
        doThrow(expectedException).when(taxValidationService).validateDifferentTaxAccounts(salesTaxId, purchaseTaxId);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> taxUpdateJpaAdapter.update(taxDTO, taxId));
        
        assertEquals("Las cuentas deben ser diferentes", exception.getMessage());
        verify(taxRepository, never()).save(any(TaxEntity.class));
    }

    @Test
    @DisplayName("Debe propagar excepción cuando código nuevo ya existe")
    void testUpdateThrowsExceptionWhenNewCodeExists() {
        // Arrange
        TaxDTO updateDTO = TaxDTO.builder()
                .idEnterprise(idEnterprise)
                .code("IVA21")
                .description("Impuesto al valor agregado 21%")
                .interest(21.0)
                .salesTaxId(salesTaxId)
                .purchaseTaxId(purchaseTaxId)
                .build();
        when(taxRepository.findByIdAndIdEnterprise(taxId, idEnterprise)).thenReturn(taxEntity);
        doNothing().when(taxValidationService).validateAccountDigits(any(), any(), anyString());
        doNothing().when(taxValidationService).validateDifferentTaxAccounts(any(), any());
        RuntimeException expectedException = new RuntimeException("El código ya existe");
        doThrow(expectedException).when(taxValidationService).validateTaxCodeNotExists("IVA21", idEnterprise);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> taxUpdateJpaAdapter.update(updateDTO, taxId));
        
        assertEquals("El código ya existe", exception.getMessage());
        verify(taxRepository, never()).save(any(TaxEntity.class));
    }

    @Test
    @DisplayName("Debe actualizar descripción e interés correctamente")
    void testUpdateDescriptionAndInterest() {
        // Arrange
        TaxDTO updateDTO = TaxDTO.builder()
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Nueva descripción del impuesto")
                .interest(20.0)
                .salesTaxId(salesTaxId)
                .purchaseTaxId(purchaseTaxId)
                .build();
        when(taxRepository.findByIdAndIdEnterprise(taxId, idEnterprise)).thenReturn(taxEntity);
        doNothing().when(taxValidationService).validateAccountDigits(any(), any(), anyString());
        doNothing().when(taxValidationService).validateDifferentTaxAccounts(any(), any());
        when(taxRepository.save(any(TaxEntity.class))).thenAnswer(invocation -> {
            TaxEntity saved = invocation.getArgument(0);
            assertEquals("Nueva descripción del impuesto", saved.getDescription());
            assertEquals(20.0, saved.getInterest());
            return saved;
        });
        when(taxUpdateMapper.toModel(any(TaxEntity.class))).thenReturn(tax);

        // Act
        Tax result = taxUpdateJpaAdapter.update(updateDTO, taxId);

        // Assert
        assertNotNull(result);
        verify(taxRepository).save(argThat(entity -> 
            entity.getDescription().equals("Nueva descripción del impuesto") &&
            entity.getInterest().equals(20.0)
        ));
    }

    @Test
    @DisplayName("Debe invocar validaciones en orden correcto")
    void testUpdateInvokesValidationsInCorrectOrder() {
        // Arrange
        when(taxRepository.findByIdAndIdEnterprise(taxId, idEnterprise)).thenReturn(taxEntity);
        doNothing().when(taxValidationService).validateAccountDigits(any(), any(), anyString());
        doNothing().when(taxValidationService).validateDifferentTaxAccounts(any(), any());
        when(taxRepository.save(any(TaxEntity.class))).thenReturn(taxEntity);
        when(taxUpdateMapper.toModel(taxEntity)).thenReturn(tax);

        // Act
        taxUpdateJpaAdapter.update(taxDTO, taxId);

        // Assert
        var inOrder = inOrder(taxRepository, taxValidationService, taxUpdateMapper);
        inOrder.verify(taxRepository).findByIdAndIdEnterprise(taxId, idEnterprise);
        inOrder.verify(taxValidationService).validateAccountDigits(salesTaxId, purchaseTaxId, idEnterprise);
        inOrder.verify(taxValidationService).validateDifferentTaxAccounts(salesTaxId, purchaseTaxId);
        inOrder.verify(taxRepository).save(any(TaxEntity.class));
        inOrder.verify(taxUpdateMapper).toModel(any(TaxEntity.class));
    }

    @Test
    @DisplayName("Debe actualizar ambas cuentas cuando ambas cambian")
    void testUpdateBothAccountsWhenBothChange() {
        // Arrange
        Long newSalesTaxId = 300L;
        Long newPurchaseTaxId = 400L;
        AccountCatalogueEntity newSalesTax = AccountCatalogueEntity.builder()
                .id(newSalesTaxId)
                .idEnterprise(idEnterprise)
                .code("24080503")
                .build();
        AccountCatalogueEntity newPurchaseTax = AccountCatalogueEntity.builder()
                .id(newPurchaseTaxId)
                .idEnterprise(idEnterprise)
                .code("24080504")
                .build();
        TaxDTO updateDTO = TaxDTO.builder()
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19%")
                .interest(19.0)
                .salesTaxId(newSalesTaxId)
                .purchaseTaxId(newPurchaseTaxId)
                .build();
        when(taxRepository.findByIdAndIdEnterprise(taxId, idEnterprise)).thenReturn(taxEntity);
        doNothing().when(taxValidationService).validateAccountDigits(any(), any(), anyString());
        doNothing().when(taxValidationService).validateDifferentTaxAccounts(any(), any());
        when(accountCatalogueRepository.findByIdAndIdEnterprise(newSalesTaxId, idEnterprise)).thenReturn(newSalesTax);
        when(accountCatalogueRepository.findByIdAndIdEnterprise(newPurchaseTaxId, idEnterprise)).thenReturn(newPurchaseTax);
        when(taxRepository.save(any(TaxEntity.class))).thenReturn(taxEntity);
        when(taxUpdateMapper.toModel(taxEntity)).thenReturn(tax);

        // Act
        Tax result = taxUpdateJpaAdapter.update(updateDTO, taxId);

        // Assert
        assertNotNull(result);
        verify(accountCatalogueRepository).findByIdAndIdEnterprise(newSalesTaxId, idEnterprise);
        verify(accountCatalogueRepository).findByIdAndIdEnterprise(newPurchaseTaxId, idEnterprise);
    }

    @Test
    @DisplayName("Debe no buscar cuentas cuando los IDs son iguales")
    void testUpdateDoesNotFetchAccountsWhenIdsAreSame() {
        // Arrange
        when(taxRepository.findByIdAndIdEnterprise(taxId, idEnterprise)).thenReturn(taxEntity);
        doNothing().when(taxValidationService).validateAccountDigits(any(), any(), anyString());
        doNothing().when(taxValidationService).validateDifferentTaxAccounts(any(), any());
        when(taxRepository.save(any(TaxEntity.class))).thenReturn(taxEntity);
        when(taxUpdateMapper.toModel(taxEntity)).thenReturn(tax);

        // Act
        taxUpdateJpaAdapter.update(taxDTO, taxId);

        // Assert
        verify(accountCatalogueRepository, never()).findByIdAndIdEnterprise(anyLong(), anyString());
    }
}

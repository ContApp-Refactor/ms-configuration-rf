package co.unicauca.edu.co.contables.accounting.catalogue.unit.catalogue.infraestructure.adapters.output.jpaAdapters;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.ClassificationEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.FinancialStatusEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.NatureEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.AccountCatalogueUpdateJpaAdapter;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.entity.AccountCatalogueEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.mapper.IAccountCatalogueUpdateMapper;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.repository.IAccountCatalogueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccountCatalogueUpdateJpaAdapterUnitTest {

    @Mock
    private IAccountCatalogueRepository accountCatalogueRepository;

    @Mock
    private IAccountCatalogueUpdateMapper accountCatalogueUpdateMapper;

    @InjectMocks
    private AccountCatalogueUpdateJpaAdapter adapter;

    private AccountCatalogueEntity accountEntity;
    private AccountCatalogue accountCatalogue;
    private AccountCatalogueEntity childEntity;

    private static final Long ACCOUNT_ID = 1L;
    private static final Long CHILD_ID = 2L;
    private static final String CODE = "1105";
    private static final String NEW_CODE = "1106";
    private static final String DESCRIPTION = "Caja";
    private static final String ENTERPRISE_ID = "ENT001";

    @BeforeEach
    void setUp() {
        accountEntity = AccountCatalogueEntity.builder()
                .id(ACCOUNT_ID)
                .code(CODE)
                .description(DESCRIPTION)
                .idEnterprise(ENTERPRISE_ID)
                .status(true)
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.CURRENTASSETS)
                .crossing(false)
                .costCenter(false)
                .amount(BigDecimal.ZERO)
                .usageCount(5)
                .build();

        accountCatalogue = AccountCatalogue.builder()
                .id(ACCOUNT_ID)
                .code(CODE)
                .description(DESCRIPTION)
                .idEnterprise(ENTERPRISE_ID)
                .status(true)
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.CURRENTASSETS)
                .crossing(false)
                .costCenter(false)
                .amount(BigDecimal.TEN)
                .build();

        childEntity = AccountCatalogueEntity.builder()
                .id(CHILD_ID)
                .code("110501")
                .description("Caja General")
                .idEnterprise(ENTERPRISE_ID)
                .status(true)
                .usageCount(0)
                .build();
    }

    // ========== Tests de updateAccountCatalogue - Cuenta no encontrada ==========

    @Test
    @DisplayName("Debe retornar null cuando la cuenta no existe")
    void testUpdateAccountCatalogueReturnsNullWhenNotFound() {
        // Arrange
        when(accountCatalogueRepository.findByIdAndIdEnterprise(ACCOUNT_ID, ENTERPRISE_ID)).thenReturn(null);

        // Act
        AccountCatalogue result = adapter.updateAccountCatalogue(ACCOUNT_ID, accountCatalogue);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("No debe guardar cuando la cuenta no existe")
    void testUpdateAccountCatalogueDoesNotSaveWhenNotFound() {
        // Arrange
        when(accountCatalogueRepository.findByIdAndIdEnterprise(ACCOUNT_ID, ENTERPRISE_ID)).thenReturn(null);

        // Act
        adapter.updateAccountCatalogue(ACCOUNT_ID, accountCatalogue);

        // Assert
        verify(accountCatalogueRepository, never()).save(any());
    }

    // ========== Tests de updateAccountCatalogue - Actualización básica ==========

    @Test
    @DisplayName("Debe actualizar cuenta correctamente")
    void testUpdateAccountCatalogueUpdatesSuccessfully() {
        // Arrange
        when(accountCatalogueRepository.findByIdAndIdEnterprise(ACCOUNT_ID, ENTERPRISE_ID)).thenReturn(accountEntity);
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        AccountCatalogue result = adapter.updateAccountCatalogue(ACCOUNT_ID, accountCatalogue);

        // Assert
        assertNotNull(result);
        verify(accountCatalogueRepository).save(accountEntity);
    }

    @Test
    @DisplayName("Debe actualizar descripción de cuenta")
    void testUpdateAccountCatalogueUpdatesDescription() {
        // Arrange
        String newDescription = "Nueva Descripción";
        accountCatalogue.setDescription(newDescription);
        when(accountCatalogueRepository.findByIdAndIdEnterprise(ACCOUNT_ID, ENTERPRISE_ID)).thenReturn(accountEntity);
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.updateAccountCatalogue(ACCOUNT_ID, accountCatalogue);

        // Assert
        assertEquals(newDescription, accountEntity.getDescription());
    }

    @Test
    @DisplayName("Debe actualizar classification de cuenta")
    void testUpdateAccountCatalogueUpdatesClassification() {
        // Arrange
        accountCatalogue.setClassification(ClassificationEnum.NONCURRENTASSETS);
        when(accountCatalogueRepository.findByIdAndIdEnterprise(ACCOUNT_ID, ENTERPRISE_ID)).thenReturn(accountEntity);
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.updateAccountCatalogue(ACCOUNT_ID, accountCatalogue);

        // Assert
        assertEquals(ClassificationEnum.NONCURRENTASSETS, accountEntity.getClassification());
    }

    @Test
    @DisplayName("Debe actualizar financialStatus de cuenta")
    void testUpdateAccountCatalogueUpdatesFinancialStatus() {
        // Arrange
        accountCatalogue.setFinancialStatus(FinancialStatusEnum.INCOMESTATEMENT);
        when(accountCatalogueRepository.findByIdAndIdEnterprise(ACCOUNT_ID, ENTERPRISE_ID)).thenReturn(accountEntity);
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.updateAccountCatalogue(ACCOUNT_ID, accountCatalogue);

        // Assert
        assertEquals(FinancialStatusEnum.INCOMESTATEMENT, accountEntity.getFinancialStatus());
    }

    @Test
    @DisplayName("Debe actualizar nature de cuenta")
    void testUpdateAccountCatalogueUpdatesNature() {
        // Arrange
        accountCatalogue.setNature(NatureEnum.CREDIT);
        when(accountCatalogueRepository.findByIdAndIdEnterprise(ACCOUNT_ID, ENTERPRISE_ID)).thenReturn(accountEntity);
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.updateAccountCatalogue(ACCOUNT_ID, accountCatalogue);

        // Assert
        assertEquals(NatureEnum.CREDIT, accountEntity.getNature());
    }

    @Test
    @DisplayName("Debe actualizar crossing de cuenta")
    void testUpdateAccountCatalogueUpdatesCrossing() {
        // Arrange
        accountCatalogue.setCrossing(true);
        when(accountCatalogueRepository.findByIdAndIdEnterprise(ACCOUNT_ID, ENTERPRISE_ID)).thenReturn(accountEntity);
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.updateAccountCatalogue(ACCOUNT_ID, accountCatalogue);

        // Assert
        assertTrue(accountEntity.getCrossing());
    }

    @Test
    @DisplayName("Debe actualizar costCenter de cuenta")
    void testUpdateAccountCatalogueUpdatesCostCenter() {
        // Arrange
        accountCatalogue.setCostCenter(true);
        when(accountCatalogueRepository.findByIdAndIdEnterprise(ACCOUNT_ID, ENTERPRISE_ID)).thenReturn(accountEntity);
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.updateAccountCatalogue(ACCOUNT_ID, accountCatalogue);

        // Assert
        assertTrue(accountEntity.getCostCenter());
    }

    @Test
    @DisplayName("Debe actualizar amount de cuenta")
    void testUpdateAccountCatalogueUpdatesAmount() {
        // Arrange
        BigDecimal newAmount = new BigDecimal("1000.50");
        accountCatalogue.setAmount(newAmount);
        when(accountCatalogueRepository.findByIdAndIdEnterprise(ACCOUNT_ID, ENTERPRISE_ID)).thenReturn(accountEntity);
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.updateAccountCatalogue(ACCOUNT_ID, accountCatalogue);

        // Assert
        assertEquals(newAmount, accountEntity.getAmount());
    }

    // ========== Tests de preservación de usageCount ==========

    @Test
    @DisplayName("Debe preservar usageCount actual al actualizar")
    void testUpdateAccountCataloguePreservesUsageCount() {
        // Arrange
        Integer originalUsageCount = 5;
        accountEntity.setUsageCount(originalUsageCount);
        when(accountCatalogueRepository.findByIdAndIdEnterprise(ACCOUNT_ID, ENTERPRISE_ID)).thenReturn(accountEntity);
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.updateAccountCatalogue(ACCOUNT_ID, accountCatalogue);

        // Assert
        assertEquals(originalUsageCount, accountEntity.getUsageCount());
    }

    @Test
    @DisplayName("Debe mantener usageCount cero si era cero")
    void testUpdateAccountCataloguePreservesZeroUsageCount() {
        // Arrange
        accountEntity.setUsageCount(0);
        when(accountCatalogueRepository.findByIdAndIdEnterprise(ACCOUNT_ID, ENTERPRISE_ID)).thenReturn(accountEntity);
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.updateAccountCatalogue(ACCOUNT_ID, accountCatalogue);

        // Assert
        assertEquals(0, accountEntity.getUsageCount());
    }

    // ========== Tests de cambio de código sin hijos ==========

    @Test
    @DisplayName("Debe actualizar código de cuenta")
    void testUpdateAccountCatalogueUpdatesCode() {
        // Arrange
        accountCatalogue.setCode(NEW_CODE);
        when(accountCatalogueRepository.findByIdAndIdEnterprise(ACCOUNT_ID, ENTERPRISE_ID)).thenReturn(accountEntity);
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueRepository.findByParentIdAndIdEnterprise(ACCOUNT_ID, ENTERPRISE_ID))
                .thenReturn(Collections.emptyList());
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.updateAccountCatalogue(ACCOUNT_ID, accountCatalogue);

        // Assert
        assertEquals(NEW_CODE, accountEntity.getCode());
    }

    @Test
    @DisplayName("No debe buscar hijos cuando código no cambia")
    void testUpdateAccountCatalogueDoesNotSearchChildrenWhenCodeUnchanged() {
        // Arrange
        when(accountCatalogueRepository.findByIdAndIdEnterprise(ACCOUNT_ID, ENTERPRISE_ID)).thenReturn(accountEntity);
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.updateAccountCatalogue(ACCOUNT_ID, accountCatalogue);

        // Assert
        verify(accountCatalogueRepository, never()).findByParentIdAndIdEnterprise(anyLong(), anyString());
    }

    @Test
    @DisplayName("Debe buscar hijos cuando código cambia")
    void testUpdateAccountCatalogueSearchesChildrenWhenCodeChanges() {
        // Arrange
        accountCatalogue.setCode(NEW_CODE);
        when(accountCatalogueRepository.findByIdAndIdEnterprise(ACCOUNT_ID, ENTERPRISE_ID)).thenReturn(accountEntity);
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueRepository.findByParentIdAndIdEnterprise(ACCOUNT_ID, ENTERPRISE_ID))
                .thenReturn(Collections.emptyList());
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.updateAccountCatalogue(ACCOUNT_ID, accountCatalogue);

        // Assert
        verify(accountCatalogueRepository).findByParentIdAndIdEnterprise(ACCOUNT_ID, ENTERPRISE_ID);
    }

    // ========== Tests de cambio de código con hijos ==========

    @Test
    @DisplayName("Debe actualizar código de hijo cuando código padre cambia")
    void testUpdateAccountCatalogueUpdatesChildCodeWhenParentCodeChanges() {
        // Arrange
        accountCatalogue.setCode(NEW_CODE);
        childEntity.setCode(CODE + "01");
        when(accountCatalogueRepository.findByIdAndIdEnterprise(ACCOUNT_ID, ENTERPRISE_ID)).thenReturn(accountEntity);
        when(accountCatalogueRepository.save(any(AccountCatalogueEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(accountCatalogueRepository.findByParentIdAndIdEnterprise(ACCOUNT_ID, ENTERPRISE_ID))
                .thenReturn(List.of(childEntity));
        when(accountCatalogueRepository.findByParentIdAndIdEnterprise(CHILD_ID, ENTERPRISE_ID))
                .thenReturn(Collections.emptyList());
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.updateAccountCatalogue(ACCOUNT_ID, accountCatalogue);

        // Assert
        assertEquals(NEW_CODE + "01", childEntity.getCode());
    }

    @Test
    @DisplayName("Debe guardar hijo con código actualizado")
    void testUpdateAccountCatalogueSavesChildWithUpdatedCode() {
        // Arrange
        accountCatalogue.setCode(NEW_CODE);
        childEntity.setCode(CODE + "01");
        when(accountCatalogueRepository.findByIdAndIdEnterprise(ACCOUNT_ID, ENTERPRISE_ID)).thenReturn(accountEntity);
        when(accountCatalogueRepository.save(any(AccountCatalogueEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(accountCatalogueRepository.findByParentIdAndIdEnterprise(ACCOUNT_ID, ENTERPRISE_ID))
                .thenReturn(List.of(childEntity));
        when(accountCatalogueRepository.findByParentIdAndIdEnterprise(CHILD_ID, ENTERPRISE_ID))
                .thenReturn(Collections.emptyList());
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.updateAccountCatalogue(ACCOUNT_ID, accountCatalogue);

        // Assert
        verify(accountCatalogueRepository, times(2)).save(any(AccountCatalogueEntity.class));
    }

    @Test
    @DisplayName("Debe actualizar códigos de múltiples hijos")
    void testUpdateAccountCatalogueUpdatesMultipleChildrenCodes() {
        // Arrange
        accountCatalogue.setCode(NEW_CODE);
        AccountCatalogueEntity childEntity2 = AccountCatalogueEntity.builder()
                .id(3L)
                .code(CODE + "02")
                .idEnterprise(ENTERPRISE_ID)
                .build();
        childEntity.setCode(CODE + "01");

        when(accountCatalogueRepository.findByIdAndIdEnterprise(ACCOUNT_ID, ENTERPRISE_ID)).thenReturn(accountEntity);
        when(accountCatalogueRepository.save(any(AccountCatalogueEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(accountCatalogueRepository.findByParentIdAndIdEnterprise(ACCOUNT_ID, ENTERPRISE_ID))
                .thenReturn(List.of(childEntity, childEntity2));
        when(accountCatalogueRepository.findByParentIdAndIdEnterprise(CHILD_ID, ENTERPRISE_ID))
                .thenReturn(Collections.emptyList());
        when(accountCatalogueRepository.findByParentIdAndIdEnterprise(3L, ENTERPRISE_ID))
                .thenReturn(Collections.emptyList());
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.updateAccountCatalogue(ACCOUNT_ID, accountCatalogue);

        // Assert
        assertEquals(NEW_CODE + "01", childEntity.getCode());
        assertEquals(NEW_CODE + "02", childEntity2.getCode());
    }

    // ========== Tests de cambio de código con jerarquía profunda ==========

    @Test
    @DisplayName("Debe actualizar códigos recursivamente en jerarquía de 3 niveles")
    void testUpdateAccountCatalogueUpdatesCodesRecursively() {
        // Arrange
        accountCatalogue.setCode(NEW_CODE);
        childEntity.setCode(CODE + "01");
        AccountCatalogueEntity grandChildEntity = AccountCatalogueEntity.builder()
                .id(3L)
                .code(CODE + "0101")
                .idEnterprise(ENTERPRISE_ID)
                .build();

        when(accountCatalogueRepository.findByIdAndIdEnterprise(ACCOUNT_ID, ENTERPRISE_ID)).thenReturn(accountEntity);
        when(accountCatalogueRepository.save(any(AccountCatalogueEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(accountCatalogueRepository.findByParentIdAndIdEnterprise(ACCOUNT_ID, ENTERPRISE_ID))
                .thenReturn(List.of(childEntity));
        when(accountCatalogueRepository.findByParentIdAndIdEnterprise(CHILD_ID, ENTERPRISE_ID))
                .thenReturn(List.of(grandChildEntity));
        when(accountCatalogueRepository.findByParentIdAndIdEnterprise(3L, ENTERPRISE_ID))
                .thenReturn(Collections.emptyList());
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.updateAccountCatalogue(ACCOUNT_ID, accountCatalogue);

        // Assert
        assertEquals(NEW_CODE + "01", childEntity.getCode());
        assertEquals(NEW_CODE + "0101", grandChildEntity.getCode());
    }

    @Test
    @DisplayName("Debe guardar 3 entidades en jerarquía de 3 niveles")
    void testUpdateAccountCatalogueSavesThreeEntitiesInThreeLevelHierarchy() {
        // Arrange
        accountCatalogue.setCode(NEW_CODE);
        childEntity.setCode(CODE + "01");
        AccountCatalogueEntity grandChildEntity = AccountCatalogueEntity.builder()
                .id(3L)
                .code(CODE + "0101")
                .idEnterprise(ENTERPRISE_ID)
                .build();

        when(accountCatalogueRepository.findByIdAndIdEnterprise(ACCOUNT_ID, ENTERPRISE_ID)).thenReturn(accountEntity);
        when(accountCatalogueRepository.save(any(AccountCatalogueEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(accountCatalogueRepository.findByParentIdAndIdEnterprise(ACCOUNT_ID, ENTERPRISE_ID))
                .thenReturn(List.of(childEntity));
        when(accountCatalogueRepository.findByParentIdAndIdEnterprise(CHILD_ID, ENTERPRISE_ID))
                .thenReturn(List.of(grandChildEntity));
        when(accountCatalogueRepository.findByParentIdAndIdEnterprise(3L, ENTERPRISE_ID))
                .thenReturn(Collections.emptyList());
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.updateAccountCatalogue(ACCOUNT_ID, accountCatalogue);

        // Assert
        verify(accountCatalogueRepository, times(3)).save(any(AccountCatalogueEntity.class));
    }

    // ========== Tests de hijo que no comienza con código padre ==========

    @Test
    @DisplayName("No debe actualizar hijo si código no comienza con código padre")
    void testUpdateAccountCatalogueDoesNotUpdateChildWithDifferentPrefix() {
        // Arrange
        accountCatalogue.setCode(NEW_CODE);
        childEntity.setCode("999901");
        String originalChildCode = childEntity.getCode();

        when(accountCatalogueRepository.findByIdAndIdEnterprise(ACCOUNT_ID, ENTERPRISE_ID)).thenReturn(accountEntity);
        when(accountCatalogueRepository.save(any(AccountCatalogueEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(accountCatalogueRepository.findByParentIdAndIdEnterprise(ACCOUNT_ID, ENTERPRISE_ID))
                .thenReturn(List.of(childEntity));
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.updateAccountCatalogue(ACCOUNT_ID, accountCatalogue);

        // Assert
        assertEquals(originalChildCode, childEntity.getCode());
    }

    // ========== Tests de incrementUsageCount ==========

    @Test
    @DisplayName("Debe incrementar usageCount correctamente")
    void testIncrementUsageCountIncrementsSuccessfully() {
        // Arrange
        when(accountCatalogueRepository.incrementUsageCount(ACCOUNT_ID)).thenReturn(1);
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        AccountCatalogue result = adapter.incrementUsageCount(ACCOUNT_ID);

        // Assert
        assertNotNull(result);
        verify(accountCatalogueRepository).incrementUsageCount(ACCOUNT_ID);
    }

    @Test
    @DisplayName("Debe retornar null cuando incrementUsageCount no actualiza filas")
    void testIncrementUsageCountReturnsNullWhenNoRowsUpdated() {
        // Arrange
        when(accountCatalogueRepository.incrementUsageCount(ACCOUNT_ID)).thenReturn(0);

        // Act
        AccountCatalogue result = adapter.incrementUsageCount(ACCOUNT_ID);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Debe recuperar cuenta actualizada después de incrementar")
    void testIncrementUsageCountRetrievesUpdatedAccount() {
        // Arrange
        when(accountCatalogueRepository.incrementUsageCount(ACCOUNT_ID)).thenReturn(1);
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.incrementUsageCount(ACCOUNT_ID);

        // Assert
        verify(accountCatalogueRepository).findById(ACCOUNT_ID);
    }

    @Test
    @DisplayName("Debe retornar null si cuenta no existe después de incrementar")
    void testIncrementUsageCountReturnsNullIfAccountNotFoundAfterIncrement() {
        // Arrange
        when(accountCatalogueRepository.incrementUsageCount(ACCOUNT_ID)).thenReturn(1);
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.empty());

        // Act
        AccountCatalogue result = adapter.incrementUsageCount(ACCOUNT_ID);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Debe mapear entidad a dominio después de incrementar")
    void testIncrementUsageCountMapsEntityToDomain() {
        // Arrange
        when(accountCatalogueRepository.incrementUsageCount(ACCOUNT_ID)).thenReturn(1);
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.incrementUsageCount(ACCOUNT_ID);

        // Assert
        verify(accountCatalogueUpdateMapper).toAccountCatalogue(accountEntity);
    }

    // ========== Tests de updateAmount ==========

    @Test
    @DisplayName("Debe actualizar amount correctamente")
    void testUpdateAmountUpdatesSuccessfully() {
        // Arrange
        BigDecimal newAmount = new BigDecimal("500.00");
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);

        // Act
        boolean result = adapter.updateAmount(ACCOUNT_ID, newAmount);

        // Assert
        assertTrue(result);
        assertEquals(newAmount, accountEntity.getAmount());
    }

    @Test
    @DisplayName("Debe retornar false cuando cuenta no existe")
    void testUpdateAmountReturnsFalseWhenAccountNotFound() {
        // Arrange
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.empty());

        // Act
        boolean result = adapter.updateAmount(ACCOUNT_ID, BigDecimal.TEN);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("No debe guardar cuando cuenta no existe")
    void testUpdateAmountDoesNotSaveWhenAccountNotFound() {
        // Arrange
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.empty());

        // Act
        adapter.updateAmount(ACCOUNT_ID, BigDecimal.TEN);

        // Assert
        verify(accountCatalogueRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe incrementar usageCount al actualizar amount")
    void testUpdateAmountIncrementsUsageCount() {
        // Arrange
        Integer originalUsageCount = 5;
        accountEntity.setUsageCount(originalUsageCount);
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);

        // Act
        adapter.updateAmount(ACCOUNT_ID, BigDecimal.TEN);

        // Assert
        assertEquals(originalUsageCount + 1, accountEntity.getUsageCount());
    }

    @Test
    @DisplayName("Debe guardar cuenta después de actualizar amount")
    void testUpdateAmountSavesAccount() {
        // Arrange
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);

        // Act
        adapter.updateAmount(ACCOUNT_ID, BigDecimal.TEN);

        // Assert
        verify(accountCatalogueRepository).save(accountEntity);
    }

    @Test
    @DisplayName("Debe actualizar amount con valor cero")
    void testUpdateAmountWithZeroValue() {
        // Arrange
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);

        // Act
        boolean result = adapter.updateAmount(ACCOUNT_ID, BigDecimal.ZERO);

        // Assert
        assertTrue(result);
        assertEquals(BigDecimal.ZERO, accountEntity.getAmount());
    }

    @Test
    @DisplayName("Debe actualizar amount con valor negativo")
    void testUpdateAmountWithNegativeValue() {
        // Arrange
        BigDecimal negativeAmount = new BigDecimal("-100.00");
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);

        // Act
        boolean result = adapter.updateAmount(ACCOUNT_ID, negativeAmount);

        // Assert
        assertTrue(result);
        assertEquals(negativeAmount, accountEntity.getAmount());
    }

    // ========== Tests de mapeo ==========

    @Test
    @DisplayName("Debe llamar al mapper con entidad recargada")
    void testUpdateAccountCatalogueCallsMapperWithSavedEntity() {
        // Arrange
        when(accountCatalogueRepository.findByIdAndIdEnterprise(ACCOUNT_ID, ENTERPRISE_ID)).thenReturn(accountEntity);
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.updateAccountCatalogue(ACCOUNT_ID, accountCatalogue);

        // Assert
        verify(accountCatalogueUpdateMapper).toAccountCatalogue(accountEntity);
    }

    @Test
    @DisplayName("Debe retornar resultado del mapper")
    void testUpdateAccountCatalogueReturnsMapperResult() {
        // Arrange
        AccountCatalogue expectedResult = AccountCatalogue.builder()
                .id(99L)
                .code("TEST")
                .build();
        when(accountCatalogueRepository.findByIdAndIdEnterprise(ACCOUNT_ID, ENTERPRISE_ID)).thenReturn(accountEntity);
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(expectedResult);

        // Act
        AccountCatalogue result = adapter.updateAccountCatalogue(ACCOUNT_ID, accountCatalogue);

        // Assert
        assertEquals(expectedResult, result);
    }

    // ========== Tests de parámetros correctos ==========

    @Test
    @DisplayName("Debe buscar por ID y enterpriseId correctos")
    void testUpdateAccountCatalogueSearchesByCorrectId() {
        // Arrange
        Long specificId = 999L;
        when(accountCatalogueRepository.findByIdAndIdEnterprise(specificId, ENTERPRISE_ID)).thenReturn(null);

        // Act
        adapter.updateAccountCatalogue(specificId, accountCatalogue);

        // Assert
        verify(accountCatalogueRepository).findByIdAndIdEnterprise(specificId, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe pasar enterpriseId correcto al buscar hijos")
    void testUpdateAccountCataloguePassesCorrectEnterpriseIdToFindChildren() {
        // Arrange
        String customEnterpriseId = "CUSTOM_ENT";
        accountCatalogue.setIdEnterprise(customEnterpriseId);
        accountCatalogue.setCode(NEW_CODE);
        when(accountCatalogueRepository.findByIdAndIdEnterprise(ACCOUNT_ID, customEnterpriseId)).thenReturn(accountEntity);
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueRepository.findByParentIdAndIdEnterprise(ACCOUNT_ID, customEnterpriseId))
                .thenReturn(Collections.emptyList());
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.updateAccountCatalogue(ACCOUNT_ID, accountCatalogue);

        // Assert
        verify(accountCatalogueRepository).findByParentIdAndIdEnterprise(ACCOUNT_ID, customEnterpriseId);
    }
}

package co.unicauca.edu.co.contables.accounting.catalogue.unit.catalogue.infraestructure.adapters.output.jpaAdapters;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.ClassificationEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.FinancialStatusEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.NatureEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.AccountCatalogueSearchJpaAdapter;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.entity.AccountCatalogueEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.mapper.IItemAccountCatalogueSearchMapper;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccountCatalogueSearchJpaAdapterUnitTest {

    @Mock
    private IAccountCatalogueRepository accountCatalogueRepository;

    @Mock
    private IItemAccountCatalogueSearchMapper itemAccountCatalogueSearchMapper;

    @InjectMocks
    private AccountCatalogueSearchJpaAdapter adapter;

    private AccountCatalogueEntity accountEntity;
    private AccountCatalogue accountCatalogue;
    private Pageable pageable;

    private static final Long ACCOUNT_ID = 1L;
    private static final String CODE = "1105";
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
                .build();

        accountCatalogue = AccountCatalogue.builder()
                .id(ACCOUNT_ID)
                .code(CODE)
                .description(DESCRIPTION)
                .idEnterprise(ENTERPRISE_ID)
                .status(true)
                .build();

        pageable = PageRequest.of(0, 10);
    }

    // ========== Tests de getAccountCatalogueByCode ==========

    @Test
    @DisplayName("Debe retornar cuenta cuando existe por código")
    void testGetAccountCatalogueByCodeReturnsAccountWhenExists() {
        // Arrange
        when(accountCatalogueRepository.findByCode(CODE, ENTERPRISE_ID)).thenReturn(accountEntity);
        when(itemAccountCatalogueSearchMapper.toDomain(accountEntity)).thenReturn(accountCatalogue);

        // Act
        AccountCatalogue result = adapter.getAccountCatalogueByCode(CODE, ENTERPRISE_ID);

        // Assert
        assertNotNull(result);
        assertEquals(CODE, result.getCode());
    }

    @Test
    @DisplayName("Debe retornar null cuando no existe por código")
    void testGetAccountCatalogueByCodeReturnsNullWhenNotExists() {
        // Arrange
        when(accountCatalogueRepository.findByCode(CODE, ENTERPRISE_ID)).thenReturn(null);
        when(itemAccountCatalogueSearchMapper.toDomain(null)).thenReturn(null);

        // Act
        AccountCatalogue result = adapter.getAccountCatalogueByCode(CODE, ENTERPRISE_ID);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Debe llamar al repositorio con parámetros correctos")
    void testGetAccountCatalogueByCodeCallsRepositoryWithCorrectParams() {
        // Arrange
        when(accountCatalogueRepository.findByCode(CODE, ENTERPRISE_ID)).thenReturn(accountEntity);
        when(itemAccountCatalogueSearchMapper.toDomain(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.getAccountCatalogueByCode(CODE, ENTERPRISE_ID);

        // Assert
        verify(accountCatalogueRepository).findByCode(CODE, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe llamar al mapper con la entidad encontrada")
    void testGetAccountCatalogueByCodeCallsMapperWithEntity() {
        // Arrange
        when(accountCatalogueRepository.findByCode(CODE, ENTERPRISE_ID)).thenReturn(accountEntity);
        when(itemAccountCatalogueSearchMapper.toDomain(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.getAccountCatalogueByCode(CODE, ENTERPRISE_ID);

        // Assert
        verify(itemAccountCatalogueSearchMapper).toDomain(accountEntity);
    }

    // ========== Tests de getAccountCatalogueTreeByCode ==========

    @Test
    @DisplayName("Debe retornar null cuando no hay datos de jerarquía")
    void testGetAccountCatalogueTreeByCodeReturnsNullWhenNoData() {
        // Arrange
        when(accountCatalogueRepository.findHierarchyByCode(CODE, ENTERPRISE_ID))
                .thenReturn(Collections.emptyList());

        // Act
        AccountCatalogue result = adapter.getAccountCatalogueTreeByCode(CODE, ENTERPRISE_ID);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Debe construir árbol desde datos de jerarquía")
    void testGetAccountCatalogueTreeByCodeBuildsTree() {
        // Arrange
        Object[] rootRow = new Object[]{
                1L, "1", "Activos", null,
                (short) NatureEnum.DEBIT.ordinal(),
                (short) FinancialStatusEnum.STATEMENTFINANCIALPOSITION.ordinal(),
                (short) ClassificationEnum.CURRENTASSETS.ordinal(),
                false, false, true, ENTERPRISE_ID
        };
        List<Object[]> hierarchyData = new ArrayList<>();
        hierarchyData.add(rootRow);
        when(accountCatalogueRepository.findHierarchyByCode(CODE, ENTERPRISE_ID))
                .thenReturn(hierarchyData);

        // Act
        AccountCatalogue result = adapter.getAccountCatalogueTreeByCode(CODE, ENTERPRISE_ID);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("1", result.getCode());
    }

    @Test
    @DisplayName("Debe construir árbol con relaciones padre-hijo")
    void testGetAccountCatalogueTreeByCodeBuildsParentChildRelations() {
        // Arrange
        Object[] parentRow = new Object[]{
                1L, "1", "Activos", null,
                (short) 0, (short) 0, (short) 0,
                false, false, true, ENTERPRISE_ID
        };
        Object[] childRow = new Object[]{
                2L, "11", "Activos Corrientes", 1L,
                (short) 0, (short) 0, (short) 0,
                false, false, true, ENTERPRISE_ID
        };
        List<Object[]> hierarchyData = Arrays.asList(parentRow, childRow);
        when(accountCatalogueRepository.findHierarchyByCode(CODE, ENTERPRISE_ID))
                .thenReturn(hierarchyData);

        // Act
        AccountCatalogue result = adapter.getAccountCatalogueTreeByCode(CODE, ENTERPRISE_ID);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getChildren());
        assertEquals(1, result.getChildren().size());
        assertEquals("11", result.getChildren().get(0).getCode());
    }

    @Test
    @DisplayName("Debe manejar enums con ordinal válido")
    void testGetAccountCatalogueTreeByCodeHandlesValidEnumOrdinals() {
        // Arrange
        Object[] row = new Object[]{
                1L, "1", "Activos", null,
                (short) NatureEnum.CREDIT.ordinal(),
                (short) FinancialStatusEnum.INCOMESTATEMENT.ordinal(),
                (short) ClassificationEnum.OPERATINGREVENUES.ordinal(),
                true, true, true, ENTERPRISE_ID
        };
        List<Object[]> hierarchyData = new ArrayList<>();
        hierarchyData.add(row);
        when(accountCatalogueRepository.findHierarchyByCode(CODE, ENTERPRISE_ID))
                .thenReturn(hierarchyData);

        // Act
        AccountCatalogue result = adapter.getAccountCatalogueTreeByCode(CODE, ENTERPRISE_ID);

        // Assert
        assertEquals(NatureEnum.CREDIT, result.getNature());
        assertEquals(FinancialStatusEnum.INCOMESTATEMENT, result.getFinancialStatus());
        assertEquals(ClassificationEnum.OPERATINGREVENUES, result.getClassification());
    }

    @Test
    @DisplayName("Debe manejar enums null")
    void testGetAccountCatalogueTreeByCodeHandlesNullEnums() {
        // Arrange
        Object[] row = new Object[]{
                1L, "1", "Activos", null,
                null, null, null,
                false, false, true, ENTERPRISE_ID
        };
        List<Object[]> hierarchyData = new ArrayList<>();
        hierarchyData.add(row);
        when(accountCatalogueRepository.findHierarchyByCode(CODE, ENTERPRISE_ID))
                .thenReturn(hierarchyData);

        // Act
        AccountCatalogue result = adapter.getAccountCatalogueTreeByCode(CODE, ENTERPRISE_ID);

        // Assert
        assertNull(result.getNature());
        assertNull(result.getFinancialStatus());
        assertNull(result.getClassification());
    }

    @Test
    @DisplayName("Debe manejar jerarquía de múltiples niveles")
    void testGetAccountCatalogueTreeByCodeHandlesMultipleLevels() {
        // Arrange
        Object[] level1 = new Object[]{1L, "1", "Activos", null, (short) 0, (short) 0, (short) 0, false, false, true, ENTERPRISE_ID};
        Object[] level2 = new Object[]{2L, "11", "Corrientes", 1L, (short) 0, (short) 0, (short) 0, false, false, true, ENTERPRISE_ID};
        Object[] level3 = new Object[]{3L, "1105", "Caja", 2L, (short) 0, (short) 0, (short) 0, false, false, true, ENTERPRISE_ID};
        List<Object[]> hierarchyData = Arrays.asList(level1, level2, level3);
        when(accountCatalogueRepository.findHierarchyByCode(CODE, ENTERPRISE_ID))
                .thenReturn(hierarchyData);

        // Act
        AccountCatalogue result = adapter.getAccountCatalogueTreeByCode(CODE, ENTERPRISE_ID);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getChildren().size());
        assertEquals(1, result.getChildren().get(0).getChildren().size());
    }

    // ========== Tests de getAccountCatalogueById ==========

    @Test
    @DisplayName("Debe retornar cuenta cuando existe por ID")
    void testGetAccountCatalogueByIdReturnsAccountWhenExists() {
        // Arrange
        when(accountCatalogueRepository.findByIdAndIdEnterprise(ACCOUNT_ID, ENTERPRISE_ID))
                .thenReturn(accountEntity);
        when(itemAccountCatalogueSearchMapper.toDomain(accountEntity)).thenReturn(accountCatalogue);

        // Act
        AccountCatalogue result = adapter.getAccountCatalogueById(ACCOUNT_ID, ENTERPRISE_ID);

        // Assert
        assertNotNull(result);
        assertEquals(ACCOUNT_ID, result.getId());
    }

    @Test
    @DisplayName("Debe retornar null cuando no existe por ID")
    void testGetAccountCatalogueByIdReturnsNullWhenNotExists() {
        // Arrange
        when(accountCatalogueRepository.findByIdAndIdEnterprise(ACCOUNT_ID, ENTERPRISE_ID))
                .thenReturn(null);
        when(itemAccountCatalogueSearchMapper.toDomain(null)).thenReturn(null);

        // Act
        AccountCatalogue result = adapter.getAccountCatalogueById(ACCOUNT_ID, ENTERPRISE_ID);

        // Assert
        assertNull(result);
    }

    // ========== Tests de getAccountCatalogueByIdAndIdEnterprise ==========

    @Test
    @DisplayName("Debe retornar cuenta cuando existe por ID y empresa")
    void testGetAccountCatalogueByIdAndIdEnterpriseReturnsAccountWhenExists() {
        // Arrange
        when(accountCatalogueRepository.findByIdAndIdEnterprise(ACCOUNT_ID, ENTERPRISE_ID))
                .thenReturn(accountEntity);
        when(itemAccountCatalogueSearchMapper.toDomain(accountEntity)).thenReturn(accountCatalogue);

        // Act
        AccountCatalogue result = adapter.getAccountCatalogueByIdAndIdEnterprise(ACCOUNT_ID, ENTERPRISE_ID);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Debe retornar null cuando no existe por ID y empresa")
    void testGetAccountCatalogueByIdAndIdEnterpriseReturnsNullWhenNotExists() {
        // Arrange
        when(accountCatalogueRepository.findByIdAndIdEnterprise(ACCOUNT_ID, ENTERPRISE_ID))
                .thenReturn(null);
        when(itemAccountCatalogueSearchMapper.toDomain(null)).thenReturn(null);

        // Act
        AccountCatalogue result = adapter.getAccountCatalogueByIdAndIdEnterprise(ACCOUNT_ID, ENTERPRISE_ID);

        // Assert
        assertNull(result);
    }

    // ========== Tests de getAccountCatalogueTreeByIdAndIdEnterprise ==========

    @Test
    @DisplayName("Debe retornar árbol cuando existe por ID y empresa")
    void testGetAccountCatalogueTreeByIdAndIdEnterpriseReturnsTreeWhenExists() {
        // Arrange
        when(accountCatalogueRepository.findByIdAndIdEnterprise(ACCOUNT_ID, ENTERPRISE_ID))
                .thenReturn(accountEntity);
        when(itemAccountCatalogueSearchMapper.toDomainTree(accountEntity)).thenReturn(accountCatalogue);

        // Act
        AccountCatalogue result = adapter.getAccountCatalogueTreeByIdAndIdEnterprise(ACCOUNT_ID, ENTERPRISE_ID);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Debe llamar toDomainTree para obtener árbol completo")
    void testGetAccountCatalogueTreeByIdAndIdEnterpriseCallsToDomainTree() {
        // Arrange
        when(accountCatalogueRepository.findByIdAndIdEnterprise(ACCOUNT_ID, ENTERPRISE_ID))
                .thenReturn(accountEntity);
        when(itemAccountCatalogueSearchMapper.toDomainTree(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.getAccountCatalogueTreeByIdAndIdEnterprise(ACCOUNT_ID, ENTERPRISE_ID);

        // Assert
        verify(itemAccountCatalogueSearchMapper).toDomainTree(accountEntity);
    }

    // ========== Tests de getAccountCatalogueByDescriptionIgnoreCaseAndIdEnterprise ==========

    @Test
    @DisplayName("Debe retornar cuenta cuando existe por descripción")
    void testGetAccountCatalogueByDescriptionReturnsAccountWhenExists() {
        // Arrange
        when(accountCatalogueRepository.findByDescriptionIgnoreCaseAndIdEnterprise(DESCRIPTION, ENTERPRISE_ID))
                .thenReturn(accountEntity);
        when(itemAccountCatalogueSearchMapper.toDomain(accountEntity)).thenReturn(accountCatalogue);

        // Act
        AccountCatalogue result = adapter.getAccountCatalogueByDescriptionIgnoreCaseAndIdEnterprise(DESCRIPTION, ENTERPRISE_ID);

        // Assert
        assertNotNull(result);
        assertEquals(DESCRIPTION, result.getDescription());
    }

    @Test
    @DisplayName("Debe retornar null cuando no existe por descripción")
    void testGetAccountCatalogueByDescriptionReturnsNullWhenNotExists() {
        // Arrange
        when(accountCatalogueRepository.findByDescriptionIgnoreCaseAndIdEnterprise(DESCRIPTION, ENTERPRISE_ID))
                .thenReturn(null);
        when(itemAccountCatalogueSearchMapper.toDomain(null)).thenReturn(null);

        // Act
        AccountCatalogue result = adapter.getAccountCatalogueByDescriptionIgnoreCaseAndIdEnterprise(DESCRIPTION, ENTERPRISE_ID);

        // Assert
        assertNull(result);
    }

    // ========== Tests de getAuxiliaryAccountsByIdEnterprise ==========

    @Test
    @DisplayName("Debe retornar lista de cuentas auxiliares")
    void testGetAuxiliaryAccountsByIdEnterpriseReturnsList() {
        // Arrange
        List<AccountCatalogueEntity> entities = List.of(accountEntity);
        when(accountCatalogueRepository.findAuxiliaryAccountsByIdEnterprise(ENTERPRISE_ID))
                .thenReturn(entities);
        when(itemAccountCatalogueSearchMapper.toDomain(accountEntity)).thenReturn(accountCatalogue);

        // Act
        List<AccountCatalogue> result = adapter.getAuxiliaryAccountsByIdEnterprise(ENTERPRISE_ID);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay cuentas auxiliares")
    void testGetAuxiliaryAccountsByIdEnterpriseReturnsEmptyList() {
        // Arrange
        when(accountCatalogueRepository.findAuxiliaryAccountsByIdEnterprise(ENTERPRISE_ID))
                .thenReturn(Collections.emptyList());

        // Act
        List<AccountCatalogue> result = adapter.getAuxiliaryAccountsByIdEnterprise(ENTERPRISE_ID);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Debe mapear todas las entidades a dominio")
    void testGetAuxiliaryAccountsByIdEnterpriseMapsAllEntities() {
        // Arrange
        AccountCatalogueEntity entity2 = AccountCatalogueEntity.builder().id(2L).code("11050102").build();
        List<AccountCatalogueEntity> entities = List.of(accountEntity, entity2);
        AccountCatalogue catalogue2 = AccountCatalogue.builder().id(2L).code("11050102").build();
        
        when(accountCatalogueRepository.findAuxiliaryAccountsByIdEnterprise(ENTERPRISE_ID))
                .thenReturn(entities);
        when(itemAccountCatalogueSearchMapper.toDomain(accountEntity)).thenReturn(accountCatalogue);
        when(itemAccountCatalogueSearchMapper.toDomain(entity2)).thenReturn(catalogue2);

        // Act
        List<AccountCatalogue> result = adapter.getAuxiliaryAccountsByIdEnterprise(ENTERPRISE_ID);

        // Assert
        assertEquals(2, result.size());
        verify(itemAccountCatalogueSearchMapper, times(2)).toDomain(any(AccountCatalogueEntity.class));
    }

    // ========== Tests de getAuxiliaryAccountsWithCrossingByIdEnterprise ==========

    @Test
    @DisplayName("Debe retornar lista de cuentas auxiliares con crossing")
    void testGetAuxiliaryAccountsWithCrossingReturnsList() {
        // Arrange
        List<AccountCatalogueEntity> entities = List.of(accountEntity);
        when(accountCatalogueRepository.findAuxiliaryAccountsWithCrossingByIdEnterprise(ENTERPRISE_ID))
                .thenReturn(entities);
        when(itemAccountCatalogueSearchMapper.toDomain(accountEntity)).thenReturn(accountCatalogue);

        // Act
        List<AccountCatalogue> result = adapter.getAuxiliaryAccountsWithCrossingByIdEnterprise(ENTERPRISE_ID);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay cuentas con crossing")
    void testGetAuxiliaryAccountsWithCrossingReturnsEmptyList() {
        // Arrange
        when(accountCatalogueRepository.findAuxiliaryAccountsWithCrossingByIdEnterprise(ENTERPRISE_ID))
                .thenReturn(Collections.emptyList());

        // Act
        List<AccountCatalogue> result = adapter.getAuxiliaryAccountsWithCrossingByIdEnterprise(ENTERPRISE_ID);

        // Assert
        assertTrue(result.isEmpty());
    }

    // ========== Tests de getAllAccountCataloguesByIdEnterprise ==========

    @Test
    @DisplayName("Debe retornar página de cuentas por empresa")
    void testGetAllAccountCataloguesByIdEnterpriseReturnsPage() {
        // Arrange
        Page<AccountCatalogueEntity> entityPage = new PageImpl<>(List.of(accountEntity));
        when(accountCatalogueRepository.findAllByIdEnterpriseOrderByCode(ENTERPRISE_ID, pageable))
                .thenReturn(entityPage);
        when(itemAccountCatalogueSearchMapper.toDomain(accountEntity)).thenReturn(accountCatalogue);

        // Act
        Page<AccountCatalogue> result = adapter.getAllAccountCataloguesByIdEnterprise(ENTERPRISE_ID, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    @DisplayName("Debe retornar página vacía cuando no hay cuentas")
    void testGetAllAccountCataloguesByIdEnterpriseReturnsEmptyPage() {
        // Arrange
        Page<AccountCatalogueEntity> emptyPage = new PageImpl<>(Collections.emptyList());
        when(accountCatalogueRepository.findAllByIdEnterpriseOrderByCode(ENTERPRISE_ID, pageable))
                .thenReturn(emptyPage);

        // Act
        Page<AccountCatalogue> result = adapter.getAllAccountCataloguesByIdEnterprise(ENTERPRISE_ID, pageable);

        // Assert
        assertTrue(result.getContent().isEmpty());
    }

    // ========== Tests de getAllAccountCataloguesByIdEnterpriseAndStatus ==========

    @Test
    @DisplayName("Debe retornar todas las cuentas cuando status es null")
    void testGetAllByIdEnterpriseAndStatusReturnsAllWhenStatusNull() {
        // Arrange
        Page<AccountCatalogueEntity> entityPage = new PageImpl<>(List.of(accountEntity));
        when(accountCatalogueRepository.findAllByIdEnterpriseOrderByCode(ENTERPRISE_ID, pageable))
                .thenReturn(entityPage);
        when(itemAccountCatalogueSearchMapper.toDomain(accountEntity)).thenReturn(accountCatalogue);

        // Act
        Page<AccountCatalogue> result = adapter.getAllAccountCataloguesByIdEnterpriseAndStatus(ENTERPRISE_ID, null, pageable);

        // Assert
        assertNotNull(result);
        verify(accountCatalogueRepository).findAllByIdEnterpriseOrderByCode(ENTERPRISE_ID, pageable);
    }

    @Test
    @DisplayName("Debe retornar solo cuentas activas cuando status es true")
    void testGetAllByIdEnterpriseAndStatusReturnsActiveWhenStatusTrue() {
        // Arrange
        Page<AccountCatalogueEntity> entityPage = new PageImpl<>(List.of(accountEntity));
        when(accountCatalogueRepository.findAllActiveByIdEnterpriseOrderByCode(ENTERPRISE_ID, pageable))
                .thenReturn(entityPage);
        when(itemAccountCatalogueSearchMapper.toDomain(accountEntity)).thenReturn(accountCatalogue);

        // Act
        Page<AccountCatalogue> result = adapter.getAllAccountCataloguesByIdEnterpriseAndStatus(ENTERPRISE_ID, true, pageable);

        // Assert
        assertNotNull(result);
        verify(accountCatalogueRepository).findAllActiveByIdEnterpriseOrderByCode(ENTERPRISE_ID, pageable);
    }

    @Test
    @DisplayName("Debe retornar solo cuentas inactivas cuando status es false")
    void testGetAllByIdEnterpriseAndStatusReturnsInactiveWhenStatusFalse() {
        // Arrange
        Page<AccountCatalogueEntity> entityPage = new PageImpl<>(List.of(accountEntity));
        when(accountCatalogueRepository.findAllInactiveByIdEnterpriseOrderByCode(ENTERPRISE_ID, pageable))
                .thenReturn(entityPage);
        when(itemAccountCatalogueSearchMapper.toDomain(accountEntity)).thenReturn(accountCatalogue);

        // Act
        Page<AccountCatalogue> result = adapter.getAllAccountCataloguesByIdEnterpriseAndStatus(ENTERPRISE_ID, false, pageable);

        // Assert
        assertNotNull(result);
        verify(accountCatalogueRepository).findAllInactiveByIdEnterpriseOrderByCode(ENTERPRISE_ID, pageable);
    }

    // ========== Tests de getAllAccountCataloguesForExport ==========

    @Test
    @DisplayName("Debe retornar todas las cuentas para exportar cuando status es null")
    void testGetAllAccountCataloguesForExportReturnsAllWhenStatusNull() {
        // Arrange
        Page<AccountCatalogueEntity> entityPage = new PageImpl<>(List.of(accountEntity));
        when(accountCatalogueRepository.findAllByIdEnterpriseForExport(ENTERPRISE_ID, pageable))
                .thenReturn(entityPage);
        when(itemAccountCatalogueSearchMapper.toDomain(accountEntity)).thenReturn(accountCatalogue);

        // Act
        Page<AccountCatalogue> result = adapter.getAllAccountCataloguesForExport(ENTERPRISE_ID, null, pageable);

        // Assert
        assertNotNull(result);
        verify(accountCatalogueRepository).findAllByIdEnterpriseForExport(ENTERPRISE_ID, pageable);
    }

    @Test
    @DisplayName("Debe retornar solo cuentas activas para exportar cuando status es true")
    void testGetAllAccountCataloguesForExportReturnsActiveWhenStatusTrue() {
        // Arrange
        Page<AccountCatalogueEntity> entityPage = new PageImpl<>(List.of(accountEntity));
        when(accountCatalogueRepository.findAllActiveByIdEnterpriseForExport(ENTERPRISE_ID, pageable))
                .thenReturn(entityPage);
        when(itemAccountCatalogueSearchMapper.toDomain(accountEntity)).thenReturn(accountCatalogue);

        // Act
        Page<AccountCatalogue> result = adapter.getAllAccountCataloguesForExport(ENTERPRISE_ID, true, pageable);

        // Assert
        assertNotNull(result);
        verify(accountCatalogueRepository).findAllActiveByIdEnterpriseForExport(ENTERPRISE_ID, pageable);
    }

    @Test
    @DisplayName("Debe retornar solo cuentas inactivas para exportar cuando status es false")
    void testGetAllAccountCataloguesForExportReturnsInactiveWhenStatusFalse() {
        // Arrange
        Page<AccountCatalogueEntity> entityPage = new PageImpl<>(List.of(accountEntity));
        when(accountCatalogueRepository.findAllInactiveByIdEnterpriseForExport(ENTERPRISE_ID, pageable))
                .thenReturn(entityPage);
        when(itemAccountCatalogueSearchMapper.toDomain(accountEntity)).thenReturn(accountCatalogue);

        // Act
        Page<AccountCatalogue> result = adapter.getAllAccountCataloguesForExport(ENTERPRISE_ID, false, pageable);

        // Assert
        assertNotNull(result);
        verify(accountCatalogueRepository).findAllInactiveByIdEnterpriseForExport(ENTERPRISE_ID, pageable);
    }

    // ========== Tests de getAllAccountsByEnterprise ==========

    @Test
    @DisplayName("Debe retornar lista de cuentas por empresa")
    void testGetAllAccountsByEnterpriseReturnsList() {
        // Arrange
        List<AccountCatalogueEntity> entities = List.of(accountEntity);
        when(accountCatalogueRepository.findByIdEnterpriseOrderByCode(ENTERPRISE_ID))
                .thenReturn(entities);
        when(itemAccountCatalogueSearchMapper.toDomain(accountEntity)).thenReturn(accountCatalogue);

        // Act
        List<AccountCatalogue> result = adapter.getAllAccountsByEnterprise(ENTERPRISE_ID);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay cuentas")
    void testGetAllAccountsByEnterpriseReturnsEmptyList() {
        // Arrange
        when(accountCatalogueRepository.findByIdEnterpriseOrderByCode(ENTERPRISE_ID))
                .thenReturn(Collections.emptyList());

        // Act
        List<AccountCatalogue> result = adapter.getAllAccountsByEnterprise(ENTERPRISE_ID);

        // Assert
        assertTrue(result.isEmpty());
    }

    // ========== Tests de getAccountsByCodeOrDescription ==========

    @Test
    @DisplayName("Debe retornar lista de cuentas por código o descripción")
    void testGetAccountsByCodeOrDescriptionReturnsList() {
        // Arrange
        String search = "Caja";
        List<AccountCatalogueEntity> entities = List.of(accountEntity);
        when(accountCatalogueRepository.findByIdEnterpriseAndCodeOrDescription(ENTERPRISE_ID, search))
                .thenReturn(entities);
        when(itemAccountCatalogueSearchMapper.toDomain(accountEntity)).thenReturn(accountCatalogue);

        // Act
        List<AccountCatalogue> result = adapter.getAccountsByCodeOrDescription(ENTERPRISE_ID, search);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay coincidencias")
    void testGetAccountsByCodeOrDescriptionReturnsEmptyList() {
        // Arrange
        String search = "NoExiste";
        when(accountCatalogueRepository.findByIdEnterpriseAndCodeOrDescription(ENTERPRISE_ID, search))
                .thenReturn(Collections.emptyList());

        // Act
        List<AccountCatalogue> result = adapter.getAccountsByCodeOrDescription(ENTERPRISE_ID, search);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Debe llamar al repositorio con parámetros correctos de búsqueda")
    void testGetAccountsByCodeOrDescriptionCallsRepositoryWithCorrectParams() {
        // Arrange
        String search = "1105";
        when(accountCatalogueRepository.findByIdEnterpriseAndCodeOrDescription(ENTERPRISE_ID, search))
                .thenReturn(Collections.emptyList());

        // Act
        adapter.getAccountsByCodeOrDescription(ENTERPRISE_ID, search);

        // Assert
        verify(accountCatalogueRepository).findByIdEnterpriseAndCodeOrDescription(ENTERPRISE_ID, search);
    }

    // ========== Tests de buildTreeFromHierarchyData - Casos edge ==========

    @Test
    @DisplayName("Debe manejar ordinal de enum fuera de rango como null")
    void testBuildTreeHandlesOutOfRangeEnumOrdinal() {
        // Arrange
        Object[] row = new Object[]{
                1L, "1", "Activos", null,
                (short) 100, (short) 100, (short) 100,
                false, false, true, ENTERPRISE_ID
        };
        List<Object[]> hierarchyData = new ArrayList<>();
        hierarchyData.add(row);
        when(accountCatalogueRepository.findHierarchyByCode(CODE, ENTERPRISE_ID))
                .thenReturn(hierarchyData);

        // Act
        AccountCatalogue result = adapter.getAccountCatalogueTreeByCode(CODE, ENTERPRISE_ID);

        // Assert
        assertNull(result.getNature());
        assertNull(result.getFinancialStatus());
        assertNull(result.getClassification());
    }

    @Test
    @DisplayName("Debe manejar ordinal negativo como null")
    void testBuildTreeHandlesNegativeEnumOrdinal() {
        // Arrange
        Object[] row = new Object[]{
                1L, "1", "Activos", null,
                (short) -1, (short) -1, (short) -1,
                false, false, true, ENTERPRISE_ID
        };
        List<Object[]> hierarchyData = new ArrayList<>();
        hierarchyData.add(row);
        when(accountCatalogueRepository.findHierarchyByCode(CODE, ENTERPRISE_ID))
                .thenReturn(hierarchyData);

        // Act
        AccountCatalogue result = adapter.getAccountCatalogueTreeByCode(CODE, ENTERPRISE_ID);

        // Assert
        assertNull(result.getNature());
        assertNull(result.getFinancialStatus());
        assertNull(result.getClassification());
    }

    @Test
    @DisplayName("Debe construir árbol con múltiples hijos en mismo nivel")
    void testBuildTreeWithMultipleChildrenSameLevel() {
        // Arrange
        Object[] parent = new Object[]{1L, "1", "Activos", null, (short) 0, (short) 0, (short) 0, false, false, true, ENTERPRISE_ID};
        Object[] child1 = new Object[]{2L, "11", "Corrientes", 1L, (short) 0, (short) 0, (short) 0, false, false, true, ENTERPRISE_ID};
        Object[] child2 = new Object[]{3L, "12", "No Corrientes", 1L, (short) 0, (short) 0, (short) 0, false, false, true, ENTERPRISE_ID};
        List<Object[]> hierarchyData = Arrays.asList(parent, child1, child2);
        when(accountCatalogueRepository.findHierarchyByCode(CODE, ENTERPRISE_ID))
                .thenReturn(hierarchyData);

        // Act
        AccountCatalogue result = adapter.getAccountCatalogueTreeByCode(CODE, ENTERPRISE_ID);

        // Assert
        assertEquals(2, result.getChildren().size());
    }

    @Test
    @DisplayName("Debe asignar campos booleanos correctamente")
    void testBuildTreeAssignsBooleanFieldsCorrectly() {
        // Arrange
        Object[] row = new Object[]{
                1L, "1", "Activos", null,
                (short) 0, (short) 0, (short) 0,
                true, true, false, ENTERPRISE_ID
        };
        List<Object[]> hierarchyData = new ArrayList<>();
        hierarchyData.add(row);
        when(accountCatalogueRepository.findHierarchyByCode(CODE, ENTERPRISE_ID))
                .thenReturn(hierarchyData);

        // Act
        AccountCatalogue result = adapter.getAccountCatalogueTreeByCode(CODE, ENTERPRISE_ID);

        // Assert
        assertTrue(result.getCrossing());
        assertTrue(result.getCostCenter());
        assertFalse(result.getStatus());
    }

    @Test
    @DisplayName("Debe asignar idEnterprise correctamente")
    void testBuildTreeAssignsEnterpriseIdCorrectly() {
        // Arrange
        String customEnterpriseId = "CUSTOM_ENT";
        Object[] row = new Object[]{
                1L, "1", "Activos", null,
                (short) 0, (short) 0, (short) 0,
                false, false, true, customEnterpriseId
        };
        List<Object[]> hierarchyData = new ArrayList<>();
        hierarchyData.add(row);
        when(accountCatalogueRepository.findHierarchyByCode(CODE, customEnterpriseId))
                .thenReturn(hierarchyData);

        // Act
        AccountCatalogue result = adapter.getAccountCatalogueTreeByCode(CODE, customEnterpriseId);

        // Assert
        assertEquals(customEnterpriseId, result.getIdEnterprise());
    }

    @Test
    @DisplayName("Debe inicializar children como lista vacía")
    void testBuildTreeInitializesEmptyChildrenList() {
        // Arrange
        Object[] row = new Object[]{
                1L, "1", "Activos", null,
                (short) 0, (short) 0, (short) 0,
                false, false, true, ENTERPRISE_ID
        };
        List<Object[]> hierarchyData = new ArrayList<>();
        hierarchyData.add(row);
        when(accountCatalogueRepository.findHierarchyByCode(CODE, ENTERPRISE_ID))
                .thenReturn(hierarchyData);

        // Act
        AccountCatalogue result = adapter.getAccountCatalogueTreeByCode(CODE, ENTERPRISE_ID);

        // Assert
        assertNotNull(result.getChildren());
    }
}

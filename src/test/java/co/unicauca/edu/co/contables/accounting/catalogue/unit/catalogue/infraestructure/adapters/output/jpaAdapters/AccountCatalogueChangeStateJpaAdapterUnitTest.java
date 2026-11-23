package co.unicauca.edu.co.contables.accounting.catalogue.unit.catalogue.infraestructure.adapters.output.jpaAdapters;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.AccountCatalogueChangeStateJpaAdapter;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccountCatalogueChangeStateJpaAdapterUnitTest {

    @Mock
    private IAccountCatalogueRepository accountCatalogueRepository;

    @Mock
    private IAccountCatalogueUpdateMapper accountCatalogueUpdateMapper;

    @InjectMocks
    private AccountCatalogueChangeStateJpaAdapter adapter;

    private AccountCatalogueEntity accountEntity;
    private AccountCatalogueEntity parentEntity;
    private AccountCatalogueEntity grandParentEntity;
    private AccountCatalogue accountCatalogue;

    private static final Long ACCOUNT_ID = 1L;
    private static final Long PARENT_ID = 2L;
    private static final Long GRANDPARENT_ID = 3L;
    private static final String ENTERPRISE_ID = "ENT001";
    private static final String TENANT_ID = "TENANT001";

    @BeforeEach
    void setUp() {
        grandParentEntity = AccountCatalogueEntity.builder()
                .id(GRANDPARENT_ID)
                .code("1")
                .description("Activos")
                .idEnterprise(ENTERPRISE_ID)
                .tenantId(TENANT_ID)
                .status(true)
                .parent(null)
                .build();

        parentEntity = AccountCatalogueEntity.builder()
                .id(PARENT_ID)
                .code("11")
                .description("Activos Corrientes")
                .idEnterprise(ENTERPRISE_ID)
                .tenantId(TENANT_ID)
                .status(true)
                .parent(grandParentEntity)
                .build();

        accountEntity = AccountCatalogueEntity.builder()
                .id(ACCOUNT_ID)
                .code("1105")
                .description("Caja")
                .idEnterprise(ENTERPRISE_ID)
                .tenantId(TENANT_ID)
                .status(true)
                .parent(parentEntity)
                .build();

        accountCatalogue = AccountCatalogue.builder()
                .id(ACCOUNT_ID)
                .code("1105")
                .description("Caja")
                .idEnterprise(ENTERPRISE_ID)
                .status(true)
                .build();
    }

    // ========== Tests de changeState - Cuenta no encontrada ==========

    @Test
    @DisplayName("Debe retornar null cuando la cuenta no existe")
    void testChangeStateReturnsNullWhenAccountNotFound() {
        // Arrange
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.empty());

        // Act
        AccountCatalogue result = adapter.changeState(ACCOUNT_ID, true);

        // Assert
        assertNull(result);
        verify(accountCatalogueRepository).findById(ACCOUNT_ID);
        verify(accountCatalogueRepository, never()).save(any());
    }

    @Test
    @DisplayName("No debe buscar descendientes cuando la cuenta no existe")
    void testChangeStateDoesNotSearchDescendantsWhenAccountNotFound() {
        // Arrange
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.empty());

        // Act
        adapter.changeState(ACCOUNT_ID, false);

        // Assert
        verify(accountCatalogueRepository, never()).findDescendantIds(anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("No debe llamar al mapper cuando la cuenta no existe")
    void testChangeStateDoesNotCallMapperWhenAccountNotFound() {
        // Arrange
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.empty());

        // Act
        adapter.changeState(ACCOUNT_ID, true);

        // Assert
        verify(accountCatalogueUpdateMapper, never()).toAccountCatalogue(any());
    }

    // ========== Tests de changeState - Activación básica ==========

    @Test
    @DisplayName("Debe activar cuenta correctamente")
    void testChangeStateActivatesAccountSuccessfully() {
        // Arrange
        accountEntity.setStatus(false);
        accountEntity.setParent(null);
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        AccountCatalogue result = adapter.changeState(ACCOUNT_ID, true);

        // Assert
        assertNotNull(result);
        assertTrue(accountEntity.getStatus());
        verify(accountCatalogueRepository).save(accountEntity);
    }

    @Test
    @DisplayName("Debe establecer estado true al activar")
    void testChangeStateSetsStatusTrueWhenActivating() {
        // Arrange
        accountEntity.setStatus(false);
        accountEntity.setParent(null);
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.changeState(ACCOUNT_ID, true);

        // Assert
        assertTrue(accountEntity.getStatus());
    }

    @Test
    @DisplayName("Debe retornar cuenta mapeada después de activar")
    void testChangeStateReturnsMappedAccountAfterActivation() {
        // Arrange
        accountEntity.setParent(null);
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        AccountCatalogue result = adapter.changeState(ACCOUNT_ID, true);

        // Assert
        assertEquals(accountCatalogue, result);
        verify(accountCatalogueUpdateMapper).toAccountCatalogue(accountEntity);
    }

    // ========== Tests de changeState - Desactivación básica ==========

    @Test
    @DisplayName("Debe desactivar cuenta correctamente")
    void testChangeStateDeactivatesAccountSuccessfully() {
        // Arrange
        accountEntity.setStatus(true);
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueRepository.findDescendantIds(ACCOUNT_ID, ENTERPRISE_ID, TENANT_ID))
                .thenReturn(Collections.emptyList());
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        AccountCatalogue result = adapter.changeState(ACCOUNT_ID, false);

        // Assert
        assertNotNull(result);
        assertFalse(accountEntity.getStatus());
    }

    @Test
    @DisplayName("Debe establecer estado false al desactivar")
    void testChangeStateSetsStatusFalseWhenDeactivating() {
        // Arrange
        accountEntity.setStatus(true);
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueRepository.findDescendantIds(ACCOUNT_ID, ENTERPRISE_ID, TENANT_ID))
                .thenReturn(Collections.emptyList());
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.changeState(ACCOUNT_ID, false);

        // Assert
        assertFalse(accountEntity.getStatus());
    }

    @Test
    @DisplayName("Debe buscar descendientes al desactivar")
    void testChangeStateSearchesDescendantsWhenDeactivating() {
        // Arrange
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueRepository.findDescendantIds(ACCOUNT_ID, ENTERPRISE_ID, TENANT_ID))
                .thenReturn(Collections.emptyList());
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.changeState(ACCOUNT_ID, false);

        // Assert
        verify(accountCatalogueRepository).findDescendantIds(ACCOUNT_ID, ENTERPRISE_ID, TENANT_ID);
    }

    // ========== Tests de activación con jerarquía de padres ==========

    @Test
    @DisplayName("Debe activar padre inactivo al activar cuenta hija")
    void testChangeStateActivatesInactiveParentWhenActivatingChild() {
        // Arrange
        accountEntity.setStatus(false);
        parentEntity.setStatus(false);
        parentEntity.setParent(null);
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        when(accountCatalogueRepository.save(any(AccountCatalogueEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.changeState(ACCOUNT_ID, true);

        // Assert
        assertTrue(parentEntity.getStatus());
        verify(accountCatalogueRepository, times(2)).save(any(AccountCatalogueEntity.class));
    }

    @Test
    @DisplayName("Debe activar toda la jerarquía de padres inactivos recursivamente")
    void testChangeStateActivatesEntireParentHierarchyRecursively() {
        // Arrange
        accountEntity.setStatus(false);
        parentEntity.setStatus(false);
        grandParentEntity.setStatus(false);
        grandParentEntity.setParent(null);
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        when(accountCatalogueRepository.save(any(AccountCatalogueEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.changeState(ACCOUNT_ID, true);

        // Assert
        assertTrue(accountEntity.getStatus());
        assertTrue(parentEntity.getStatus());
        assertTrue(grandParentEntity.getStatus());
        verify(accountCatalogueRepository, times(3)).save(any(AccountCatalogueEntity.class));
    }

    @Test
    @DisplayName("No debe activar padre si ya está activo")
    void testChangeStateDoesNotActivateParentIfAlreadyActive() {
        // Arrange
        accountEntity.setStatus(false);
        parentEntity.setStatus(true);
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        when(accountCatalogueRepository.save(any(AccountCatalogueEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.changeState(ACCOUNT_ID, true);

        // Assert
        verify(accountCatalogueRepository, times(1)).save(accountEntity);
    }

    @Test
    @DisplayName("Debe detener activación recursiva cuando encuentra padre activo")
    void testChangeStateStopsRecursionWhenFindingActiveParent() {
        // Arrange
        accountEntity.setStatus(false);
        parentEntity.setStatus(false);
        grandParentEntity.setStatus(true);
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        when(accountCatalogueRepository.save(any(AccountCatalogueEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.changeState(ACCOUNT_ID, true);

        // Assert
        assertTrue(accountEntity.getStatus());
        assertTrue(parentEntity.getStatus());
        verify(accountCatalogueRepository, times(2)).save(any(AccountCatalogueEntity.class));
    }

    @Test
    @DisplayName("No debe intentar activar padres si cuenta no tiene padre")
    void testChangeStateDoesNotActivateParentsIfNoParent() {
        // Arrange
        accountEntity.setStatus(false);
        accountEntity.setParent(null);
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.changeState(ACCOUNT_ID, true);

        // Assert
        verify(accountCatalogueRepository, times(1)).save(accountEntity);
    }

    // ========== Tests de desactivación con descendientes ==========

    @Test
    @DisplayName("Debe desactivar descendientes cuando existen")
    void testChangeStateDeactivatesDescendantsWhenTheyExist() {
        // Arrange
        List<Long> descendantIds = List.of(10L, 11L, 12L);
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueRepository.findDescendantIds(ACCOUNT_ID, ENTERPRISE_ID, TENANT_ID))
                .thenReturn(descendantIds);
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.changeState(ACCOUNT_ID, false);

        // Assert
        verify(accountCatalogueRepository).updateStatusByIds(false, descendantIds, ENTERPRISE_ID, TENANT_ID);
    }

    @Test
    @DisplayName("No debe llamar updateStatusByIds cuando no hay descendientes")
    void testChangeStateDoesNotUpdateWhenNoDescendants() {
        // Arrange
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueRepository.findDescendantIds(ACCOUNT_ID, ENTERPRISE_ID, TENANT_ID))
                .thenReturn(Collections.emptyList());
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.changeState(ACCOUNT_ID, false);

        // Assert
        verify(accountCatalogueRepository, never()).updateStatusByIds(anyBoolean(), anyList(), anyString(), anyString());
    }

    @Test
    @DisplayName("Debe procesar descendientes en batches cuando hay muchos")
    void testChangeStateProcessesDescendantsInBatchesForLargeLists() {
        // Arrange
        List<Long> descendantIds = new ArrayList<>();
        for (long i = 1; i <= 1200; i++) {
            descendantIds.add(i);
        }
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueRepository.findDescendantIds(ACCOUNT_ID, ENTERPRISE_ID, TENANT_ID))
                .thenReturn(descendantIds);
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.changeState(ACCOUNT_ID, false);

        // Assert
        verify(accountCatalogueRepository, times(3)).updateStatusByIds(eq(false), anyList(), eq(ENTERPRISE_ID), eq(TENANT_ID));
    }

    @Test
    @DisplayName("Debe usar batch de tamaño 500")
    void testChangeStateUsesBatchSizeOf500() {
        // Arrange
        List<Long> descendantIds = new ArrayList<>();
        for (long i = 1; i <= 500; i++) {
            descendantIds.add(i);
        }
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueRepository.findDescendantIds(ACCOUNT_ID, ENTERPRISE_ID, TENANT_ID))
                .thenReturn(descendantIds);
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.changeState(ACCOUNT_ID, false);

        // Assert
        verify(accountCatalogueRepository, times(1)).updateStatusByIds(eq(false), eq(descendantIds), eq(ENTERPRISE_ID), eq(TENANT_ID));
    }

    @Test
    @DisplayName("Debe procesar último batch parcial correctamente")
    void testChangeStateProcessesPartialLastBatchCorrectly() {
        // Arrange
        List<Long> descendantIds = new ArrayList<>();
        for (long i = 1; i <= 550; i++) {
            descendantIds.add(i);
        }
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueRepository.findDescendantIds(ACCOUNT_ID, ENTERPRISE_ID, TENANT_ID))
                .thenReturn(descendantIds);
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.changeState(ACCOUNT_ID, false);

        // Assert
        verify(accountCatalogueRepository, times(2)).updateStatusByIds(eq(false), anyList(), eq(ENTERPRISE_ID), eq(TENANT_ID));
    }

    @Test
    @DisplayName("Debe pasar estado false a updateStatusByIds al desactivar")
    void testChangeStatePassesFalseStatusToUpdateByIds() {
        // Arrange
        List<Long> descendantIds = List.of(10L, 11L);
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueRepository.findDescendantIds(ACCOUNT_ID, ENTERPRISE_ID, TENANT_ID))
                .thenReturn(descendantIds);
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.changeState(ACCOUNT_ID, false);

        // Assert
        verify(accountCatalogueRepository).updateStatusByIds(eq(false), anyList(), anyString(), anyString());
    }

    // ========== Tests de activación - No busca descendientes ==========

    @Test
    @DisplayName("No debe buscar descendientes al activar")
    void testChangeStateDoesNotSearchDescendantsWhenActivating() {
        // Arrange
        accountEntity.setParent(null);
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.changeState(ACCOUNT_ID, true);

        // Assert
        verify(accountCatalogueRepository, never()).findDescendantIds(anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("No debe llamar updateStatusByIds al activar")
    void testChangeStateDoesNotCallUpdateStatusByIdsWhenActivating() {
        // Arrange
        accountEntity.setParent(null);
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.changeState(ACCOUNT_ID, true);

        // Assert
        verify(accountCatalogueRepository, never()).updateStatusByIds(anyBoolean(), anyList(), anyString(), anyString());
    }

    // ========== Tests de persistencia ==========

    @Test
    @DisplayName("Debe guardar cuenta en repositorio al cambiar estado")
    void testChangeStateSavesAccountToRepository() {
        // Arrange
        accountEntity.setParent(null);
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.changeState(ACCOUNT_ID, true);

        // Assert
        verify(accountCatalogueRepository).save(accountEntity);
    }

    @Test
    @DisplayName("Debe llamar findById con el id correcto")
    void testChangeStateCallsFindByIdWithCorrectId() {
        // Arrange
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.empty());

        // Act
        adapter.changeState(ACCOUNT_ID, true);

        // Assert
        verify(accountCatalogueRepository).findById(ACCOUNT_ID);
    }

    // ========== Tests de mapeo ==========

    @Test
    @DisplayName("Debe mapear entidad a modelo de dominio")
    void testChangeStateMapsEntityToDomainModel() {
        // Arrange
        accountEntity.setParent(null);
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        AccountCatalogue result = adapter.changeState(ACCOUNT_ID, true);

        // Assert
        assertNotNull(result);
        verify(accountCatalogueUpdateMapper).toAccountCatalogue(accountEntity);
    }

    @Test
    @DisplayName("Debe retornar resultado del mapper")
    void testChangeStateReturnsMapperResult() {
        // Arrange
        accountEntity.setParent(null);
        AccountCatalogue expectedResult = AccountCatalogue.builder()
                .id(99L)
                .code("TEST")
                .build();
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(expectedResult);

        // Act
        AccountCatalogue result = adapter.changeState(ACCOUNT_ID, true);

        // Assert
        assertEquals(expectedResult, result);
    }

    // ========== Tests de parámetros en findDescendantIds ==========

    @Test
    @DisplayName("Debe pasar idEnterprise correcto a findDescendantIds")
    void testChangeStatePassesCorrectEnterpriseIdToFindDescendants() {
        // Arrange
        String customEnterpriseId = "CUSTOM_ENT";
        accountEntity.setIdEnterprise(customEnterpriseId);
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueRepository.findDescendantIds(ACCOUNT_ID, customEnterpriseId, TENANT_ID))
                .thenReturn(Collections.emptyList());
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.changeState(ACCOUNT_ID, false);

        // Assert
        verify(accountCatalogueRepository).findDescendantIds(ACCOUNT_ID, customEnterpriseId, TENANT_ID);
    }

    @Test
    @DisplayName("Debe pasar tenantId correcto a findDescendantIds")
    void testChangeStatePassesCorrectTenantIdToFindDescendants() {
        // Arrange
        String customTenantId = "CUSTOM_TENANT";
        accountEntity.setTenantId(customTenantId);
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueRepository.findDescendantIds(ACCOUNT_ID, ENTERPRISE_ID, customTenantId))
                .thenReturn(Collections.emptyList());
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.changeState(ACCOUNT_ID, false);

        // Assert
        verify(accountCatalogueRepository).findDescendantIds(ACCOUNT_ID, ENTERPRISE_ID, customTenantId);
    }

    @Test
    @DisplayName("Debe pasar id de cuenta correcto a findDescendantIds")
    void testChangeStatePassesCorrectAccountIdToFindDescendants() {
        // Arrange
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueRepository.findDescendantIds(ACCOUNT_ID, ENTERPRISE_ID, TENANT_ID))
                .thenReturn(Collections.emptyList());
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.changeState(ACCOUNT_ID, false);

        // Assert
        verify(accountCatalogueRepository).findDescendantIds(eq(ACCOUNT_ID), anyString(), anyString());
    }

    // ========== Tests de parámetros en updateStatusByIds ==========

    @Test
    @DisplayName("Debe pasar idEnterprise correcto a updateStatusByIds")
    void testChangeStatePassesCorrectEnterpriseIdToUpdateByIds() {
        // Arrange
        String customEnterpriseId = "CUSTOM_ENT";
        accountEntity.setIdEnterprise(customEnterpriseId);
        List<Long> descendantIds = List.of(10L);
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueRepository.findDescendantIds(ACCOUNT_ID, customEnterpriseId, TENANT_ID))
                .thenReturn(descendantIds);
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.changeState(ACCOUNT_ID, false);

        // Assert
        verify(accountCatalogueRepository).updateStatusByIds(anyBoolean(), anyList(), eq(customEnterpriseId), anyString());
    }

    @Test
    @DisplayName("Debe pasar tenantId correcto a updateStatusByIds")
    void testChangeStatePassesCorrectTenantIdToUpdateByIds() {
        // Arrange
        String customTenantId = "CUSTOM_TENANT";
        accountEntity.setTenantId(customTenantId);
        List<Long> descendantIds = List.of(10L);
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueRepository.findDescendantIds(ACCOUNT_ID, ENTERPRISE_ID, customTenantId))
                .thenReturn(descendantIds);
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.changeState(ACCOUNT_ID, false);

        // Assert
        verify(accountCatalogueRepository).updateStatusByIds(anyBoolean(), anyList(), anyString(), eq(customTenantId));
    }

    // ========== Tests de casos edge en batches ==========

    @Test
    @DisplayName("Debe manejar exactamente 500 descendientes en un batch")
    void testChangeStateHandlesExactly500DescendantsInOneBatch() {
        // Arrange
        List<Long> descendantIds = new ArrayList<>();
        for (long i = 1; i <= 500; i++) {
            descendantIds.add(i);
        }
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueRepository.findDescendantIds(ACCOUNT_ID, ENTERPRISE_ID, TENANT_ID))
                .thenReturn(descendantIds);
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.changeState(ACCOUNT_ID, false);

        // Assert
        verify(accountCatalogueRepository, times(1)).updateStatusByIds(eq(false), anyList(), eq(ENTERPRISE_ID), eq(TENANT_ID));
    }

    @Test
    @DisplayName("Debe manejar 501 descendientes en dos batches")
    void testChangeStateHandles501DescendantsInTwoBatches() {
        // Arrange
        List<Long> descendantIds = new ArrayList<>();
        for (long i = 1; i <= 501; i++) {
            descendantIds.add(i);
        }
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueRepository.findDescendantIds(ACCOUNT_ID, ENTERPRISE_ID, TENANT_ID))
                .thenReturn(descendantIds);
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.changeState(ACCOUNT_ID, false);

        // Assert
        verify(accountCatalogueRepository, times(2)).updateStatusByIds(eq(false), anyList(), eq(ENTERPRISE_ID), eq(TENANT_ID));
    }

    @Test
    @DisplayName("Debe manejar exactamente 1000 descendientes en dos batches")
    void testChangeStateHandles1000DescendantsInTwoBatches() {
        // Arrange
        List<Long> descendantIds = new ArrayList<>();
        for (long i = 1; i <= 1000; i++) {
            descendantIds.add(i);
        }
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueRepository.findDescendantIds(ACCOUNT_ID, ENTERPRISE_ID, TENANT_ID))
                .thenReturn(descendantIds);
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.changeState(ACCOUNT_ID, false);

        // Assert
        verify(accountCatalogueRepository, times(2)).updateStatusByIds(eq(false), anyList(), eq(ENTERPRISE_ID), eq(TENANT_ID));
    }

    @Test
    @DisplayName("Debe manejar un solo descendiente")
    void testChangeStateHandlesSingleDescendant() {
        // Arrange
        List<Long> descendantIds = List.of(10L);
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueRepository.findDescendantIds(ACCOUNT_ID, ENTERPRISE_ID, TENANT_ID))
                .thenReturn(descendantIds);
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.changeState(ACCOUNT_ID, false);

        // Assert
        verify(accountCatalogueRepository, times(1)).updateStatusByIds(eq(false), eq(descendantIds), eq(ENTERPRISE_ID), eq(TENANT_ID));
    }

    // ========== Tests de jerarquía profunda ==========

    @Test
    @DisplayName("Debe activar jerarquía de 4 niveles completa")
    void testChangeStateActivatesFourLevelHierarchy() {
        // Arrange
        AccountCatalogueEntity greatGrandParent = AccountCatalogueEntity.builder()
                .id(4L)
                .code("1")
                .status(false)
                .parent(null)
                .build();
        grandParentEntity.setParent(greatGrandParent);
        grandParentEntity.setStatus(false);
        parentEntity.setStatus(false);
        accountEntity.setStatus(false);

        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        when(accountCatalogueRepository.save(any(AccountCatalogueEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.changeState(ACCOUNT_ID, true);

        // Assert
        assertTrue(accountEntity.getStatus());
        assertTrue(parentEntity.getStatus());
        assertTrue(grandParentEntity.getStatus());
        assertTrue(greatGrandParent.getStatus());
        verify(accountCatalogueRepository, times(4)).save(any(AccountCatalogueEntity.class));
    }

    @Test
    @DisplayName("Debe activar solo los padres inactivos en jerarquía mixta")
    void testChangeStateActivatesOnlyInactiveParentsInMixedHierarchy() {
        // Arrange
        AccountCatalogueEntity greatGrandParent = AccountCatalogueEntity.builder()
                .id(4L)
                .code("1")
                .status(true)
                .parent(null)
                .build();
        grandParentEntity.setParent(greatGrandParent);
        grandParentEntity.setStatus(false);
        parentEntity.setStatus(true);
        accountEntity.setStatus(false);

        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        when(accountCatalogueRepository.save(any(AccountCatalogueEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.changeState(ACCOUNT_ID, true);

        // Assert
        assertTrue(accountEntity.getStatus());
        verify(accountCatalogueRepository, times(1)).save(any(AccountCatalogueEntity.class));
    }

    // ========== Tests con diferentes IDs ==========

    @Test
    @DisplayName("Debe funcionar con ID diferente de cuenta")
    void testChangeStateWorksWithDifferentAccountId() {
        // Arrange
        Long differentId = 999L;
        AccountCatalogueEntity differentEntity = AccountCatalogueEntity.builder()
                .id(differentId)
                .code("9999")
                .idEnterprise(ENTERPRISE_ID)
                .tenantId(TENANT_ID)
                .status(true)
                .parent(null)
                .build();
        when(accountCatalogueRepository.findById(differentId)).thenReturn(Optional.of(differentEntity));
        when(accountCatalogueRepository.save(differentEntity)).thenReturn(differentEntity);
        when(accountCatalogueRepository.findDescendantIds(differentId, ENTERPRISE_ID, TENANT_ID))
                .thenReturn(Collections.emptyList());
        when(accountCatalogueUpdateMapper.toAccountCatalogue(differentEntity)).thenReturn(accountCatalogue);

        // Act
        AccountCatalogue result = adapter.changeState(differentId, false);

        // Assert
        assertNotNull(result);
        verify(accountCatalogueRepository).findById(differentId);
    }

    // ========== Tests de orden de operaciones ==========

    @Test
    @DisplayName("Debe guardar cuenta antes de activar padres")
    void testChangeStateSavesAccountBeforeActivatingParents() {
        // Arrange
        accountEntity.setStatus(false);
        parentEntity.setStatus(false);
        parentEntity.setParent(null);
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        when(accountCatalogueRepository.save(any(AccountCatalogueEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.changeState(ACCOUNT_ID, true);

        // Assert
        var inOrder = inOrder(accountCatalogueRepository);
        inOrder.verify(accountCatalogueRepository).save(accountEntity);
        inOrder.verify(accountCatalogueRepository).save(parentEntity);
    }

    @Test
    @DisplayName("Debe guardar cuenta antes de desactivar descendientes")
    void testChangeStateSavesAccountBeforeDeactivatingDescendants() {
        // Arrange
        List<Long> descendantIds = List.of(10L);
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueRepository.findDescendantIds(ACCOUNT_ID, ENTERPRISE_ID, TENANT_ID))
                .thenReturn(descendantIds);
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.changeState(ACCOUNT_ID, false);

        // Assert
        var inOrder = inOrder(accountCatalogueRepository);
        inOrder.verify(accountCatalogueRepository).save(accountEntity);
        inOrder.verify(accountCatalogueRepository).updateStatusByIds(anyBoolean(), anyList(), anyString(), anyString());
    }

    @Test
    @DisplayName("Debe mapear después de todas las operaciones de guardado")
    void testChangeStateMapsAfterAllSaveOperations() {
        // Arrange
        accountEntity.setStatus(false);
        parentEntity.setStatus(false);
        parentEntity.setParent(null);
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        when(accountCatalogueRepository.save(any(AccountCatalogueEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(accountCatalogueUpdateMapper.toAccountCatalogue(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.changeState(ACCOUNT_ID, true);

        // Assert
        var inOrder = inOrder(accountCatalogueRepository, accountCatalogueUpdateMapper);
        inOrder.verify(accountCatalogueRepository, times(2)).save(any(AccountCatalogueEntity.class));
        inOrder.verify(accountCatalogueUpdateMapper).toAccountCatalogue(accountEntity);
    }
}

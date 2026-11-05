package co.unicauca.edu.co.contables.accounting.catalogue.unit.catalogue.infraestructure.adapters.output.jpaAdapters;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.AccountCatalogueCreateJpaAdapter;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.entity.AccountCatalogueEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.mapper.IAccountCatalogueCreateMapper;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccountCatalogueCreateJpaAdapterUnitTest {

    @Mock
    private IAccountCatalogueRepository accountCatalogueRepository;

    @Mock
    private IAccountCatalogueCreateMapper accountCatalogueCreateMapper;

    @InjectMocks
    private AccountCatalogueCreateJpaAdapter adapter;

    private AccountCatalogue accountCatalogue;
    private AccountCatalogue parentCatalogue;
    private AccountCatalogueEntity accountEntity;
    private AccountCatalogueEntity parentEntity;

    private static final Long ACCOUNT_ID = 1L;
    private static final Long PARENT_ID = 2L;
    private static final String ENTERPRISE_ID = "ENT001";
    private static final String TENANT_ID = "TENANT001";
    private static final String CODE_AUXILIARY = "11050101";
    private static final String CODE_SUBCUENTA = "110501";
    private static final String CODE_CUENTA = "1105";
    private static final String CODE_GRUPO = "11";
    private static final String CODE_CLASE = "1";

    @BeforeEach
    void setUp() {
        parentCatalogue = AccountCatalogue.builder()
                .id(PARENT_ID)
                .code(CODE_SUBCUENTA)
                .description("Caja General")
                .idEnterprise(ENTERPRISE_ID)
                .build();

        accountCatalogue = AccountCatalogue.builder()
                .id(null)
                .code(CODE_AUXILIARY)
                .description("Caja Principal")
                .idEnterprise(ENTERPRISE_ID)
                .parent(parentCatalogue)
                .build();

        parentEntity = AccountCatalogueEntity.builder()
                .id(PARENT_ID)
                .code(CODE_SUBCUENTA)
                .description("Caja General")
                .idEnterprise(ENTERPRISE_ID)
                .tenantId(TENANT_ID)
                .status(true)
                .build();

        accountEntity = AccountCatalogueEntity.builder()
                .id(ACCOUNT_ID)
                .code(CODE_AUXILIARY)
                .description("Caja Principal")
                .idEnterprise(ENTERPRISE_ID)
                .tenantId(TENANT_ID)
                .status(true)
                .parent(parentEntity)
                .build();
    }

    // ========== Tests de createAccountCatalogue - Mapper retorna null ==========

    @Test
    @DisplayName("Debe retornar null cuando el mapper retorna null")
    void testCreateAccountCatalogueReturnsNullWhenMapperReturnsNull() {
        // Arrange
        accountCatalogue.setParent(null);
        when(accountCatalogueCreateMapper.toEntity(accountCatalogue, null)).thenReturn(null);

        // Act
        AccountCatalogue result = adapter.createAccountCatalogue(accountCatalogue);

        // Assert
        assertNull(result);
        verify(accountCatalogueRepository, never()).save(any());
    }

    @Test
    @DisplayName("No debe buscar código cuando mapper retorna null")
    void testCreateAccountCatalogueDoesNotSearchCodeWhenMapperReturnsNull() {
        // Arrange
        accountCatalogue.setParent(null);
        when(accountCatalogueCreateMapper.toEntity(accountCatalogue, null)).thenReturn(null);

        // Act
        adapter.createAccountCatalogue(accountCatalogue);

        // Assert
        verify(accountCatalogueRepository, never()).findByCode(anyString(), anyString());
    }

    // ========== Tests de createAccountCatalogue - Sin padre ==========

    @Test
    @DisplayName("Debe crear cuenta sin padre correctamente")
    void testCreateAccountCatalogueWithoutParentSuccessfully() {
        // Arrange
        accountCatalogue.setParent(null);
        accountEntity.setParent(null);
        accountEntity.setCode(CODE_CLASE);
        when(accountCatalogueCreateMapper.toEntity(accountCatalogue, null)).thenReturn(accountEntity);
        when(accountCatalogueRepository.findByCode(CODE_CLASE, ENTERPRISE_ID)).thenReturn(null);
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueCreateMapper.toModel(accountEntity)).thenReturn(accountCatalogue);

        // Act
        AccountCatalogue result = adapter.createAccountCatalogue(accountCatalogue);

        // Assert
        assertNotNull(result);
        verify(accountCatalogueRepository).save(accountEntity);
    }

    @Test
    @DisplayName("No debe buscar padre cuando parent es null")
    void testCreateAccountCatalogueDoesNotSearchParentWhenNull() {
        // Arrange
        accountCatalogue.setParent(null);
        accountEntity.setParent(null);
        accountEntity.setCode(CODE_CLASE);
        when(accountCatalogueCreateMapper.toEntity(accountCatalogue, null)).thenReturn(accountEntity);
        when(accountCatalogueRepository.findByCode(CODE_CLASE, ENTERPRISE_ID)).thenReturn(null);
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueCreateMapper.toModel(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.createAccountCatalogue(accountCatalogue);

        // Assert
        verify(accountCatalogueCreateMapper).toEntity(accountCatalogue, null);
    }

    // ========== Tests de createAccountCatalogue - Con padre ==========

    @Test
    @DisplayName("Debe buscar padre por código cuando tiene parent")
    void testCreateAccountCatalogueSearchesParentByCode() {
        // Arrange
        when(accountCatalogueRepository.findByCode(CODE_SUBCUENTA, ENTERPRISE_ID)).thenReturn(parentEntity);
        when(accountCatalogueCreateMapper.toEntity(accountCatalogue, parentEntity)).thenReturn(accountEntity);
        when(accountCatalogueRepository.findByCode(CODE_AUXILIARY, ENTERPRISE_ID)).thenReturn(null);
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueCreateMapper.toModel(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.createAccountCatalogue(accountCatalogue);

        // Assert
        verify(accountCatalogueRepository).findByCode(CODE_SUBCUENTA, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe pasar padre encontrado al mapper")
    void testCreateAccountCataloguePassesParentToMapper() {
        // Arrange
        when(accountCatalogueRepository.findByCode(CODE_SUBCUENTA, ENTERPRISE_ID)).thenReturn(parentEntity);
        when(accountCatalogueCreateMapper.toEntity(accountCatalogue, parentEntity)).thenReturn(accountEntity);
        when(accountCatalogueRepository.findByCode(CODE_AUXILIARY, ENTERPRISE_ID)).thenReturn(null);
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueCreateMapper.toModel(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.createAccountCatalogue(accountCatalogue);

        // Assert
        verify(accountCatalogueCreateMapper).toEntity(accountCatalogue, parentEntity);
    }

    @Test
    @DisplayName("Debe crear cuenta con padre correctamente")
    void testCreateAccountCatalogueWithParentSuccessfully() {
        // Arrange
        when(accountCatalogueRepository.findByCode(CODE_SUBCUENTA, ENTERPRISE_ID)).thenReturn(parentEntity);
        when(accountCatalogueCreateMapper.toEntity(accountCatalogue, parentEntity)).thenReturn(accountEntity);
        when(accountCatalogueRepository.findByCode(CODE_AUXILIARY, ENTERPRISE_ID)).thenReturn(null);
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueCreateMapper.toModel(accountEntity)).thenReturn(accountCatalogue);

        // Act
        AccountCatalogue result = adapter.createAccountCatalogue(accountCatalogue);

        // Assert
        assertNotNull(result);
        verify(accountCatalogueRepository).save(accountEntity);
    }

    // ========== Tests de createAccountCatalogue - Código ya existe ==========

    @Test
    @DisplayName("Debe retornar cuenta existente cuando el código ya existe")
    void testCreateAccountCatalogueReturnsExistingWhenCodeExists() {
        // Arrange
        accountCatalogue.setParent(null);
        AccountCatalogueEntity existingEntity = AccountCatalogueEntity.builder()
                .id(99L)
                .code(CODE_AUXILIARY)
                .idEnterprise(ENTERPRISE_ID)
                .build();
        AccountCatalogue existingCatalogue = AccountCatalogue.builder()
                .id(99L)
                .code(CODE_AUXILIARY)
                .build();

        when(accountCatalogueCreateMapper.toEntity(accountCatalogue, null)).thenReturn(accountEntity);
        when(accountCatalogueRepository.findByCode(CODE_AUXILIARY, ENTERPRISE_ID))
                .thenReturn(existingEntity);
        when(accountCatalogueCreateMapper.toModel(existingEntity)).thenReturn(existingCatalogue);

        // Act
        AccountCatalogue result = adapter.createAccountCatalogue(accountCatalogue);

        // Assert
        assertEquals(99L, result.getId());
        verify(accountCatalogueRepository, never()).save(any());
    }

    @Test
    @DisplayName("No debe guardar cuando el código ya existe")
    void testCreateAccountCatalogueDoesNotSaveWhenCodeExists() {
        // Arrange
        accountCatalogue.setParent(null);
        when(accountCatalogueCreateMapper.toEntity(accountCatalogue, null)).thenReturn(accountEntity);
        when(accountCatalogueRepository.findByCode(CODE_AUXILIARY, ENTERPRISE_ID)).thenReturn(accountEntity);
        when(accountCatalogueCreateMapper.toModel(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.createAccountCatalogue(accountCatalogue);

        // Assert
        verify(accountCatalogueRepository, never()).save(any());
    }

    @Test
    @DisplayName("No debe activar jerarquía padre cuando código ya existe")
    void testCreateAccountCatalogueDoesNotActivateHierarchyWhenCodeExists() {
        // Arrange
        accountCatalogue.setParent(null);
        when(accountCatalogueCreateMapper.toEntity(accountCatalogue, null)).thenReturn(accountEntity);
        when(accountCatalogueRepository.findByCode(CODE_AUXILIARY, ENTERPRISE_ID)).thenReturn(accountEntity);
        when(accountCatalogueCreateMapper.toModel(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.createAccountCatalogue(accountCatalogue);

        // Assert
        verify(accountCatalogueRepository, never()).updateStatusByCodes(anyBoolean(), anyList(), anyString(), anyString());
    }

    // ========== Tests de activación de jerarquía padre ==========

    @Test
    @DisplayName("Debe activar jerarquía padre completa para cuenta auxiliar")
    void testCreateAccountCatalogueActivatesFullHierarchyForAuxiliary() {
        // Arrange
        accountCatalogue.setParent(null);
        when(accountCatalogueCreateMapper.toEntity(accountCatalogue, null)).thenReturn(accountEntity);
        when(accountCatalogueRepository.findByCode(CODE_AUXILIARY, ENTERPRISE_ID)).thenReturn(null);
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueCreateMapper.toModel(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.createAccountCatalogue(accountCatalogue);

        // Assert
        verify(accountCatalogueRepository).updateStatusByCodes(
                eq(true),
                argThat(list -> list.size() == 4 && 
                        list.contains(CODE_SUBCUENTA) &&
                        list.contains(CODE_CUENTA) &&
                        list.contains(CODE_GRUPO) &&
                        list.contains(CODE_CLASE)),
                eq(ENTERPRISE_ID),
                eq(TENANT_ID)
        );
    }

    @Test
    @DisplayName("Debe activar 3 niveles de jerarquía para subcuenta")
    void testCreateAccountCatalogueActivatesThreeLevelsForSubcuenta() {
        // Arrange
        accountCatalogue.setParent(null);
        accountEntity.setCode(CODE_SUBCUENTA);
        when(accountCatalogueCreateMapper.toEntity(accountCatalogue, null)).thenReturn(accountEntity);
        when(accountCatalogueRepository.findByCode(CODE_SUBCUENTA, ENTERPRISE_ID)).thenReturn(null);
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueCreateMapper.toModel(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.createAccountCatalogue(accountCatalogue);

        // Assert
        verify(accountCatalogueRepository).updateStatusByCodes(
                eq(true),
                argThat(list -> list.size() == 3 &&
                        list.contains(CODE_CUENTA) &&
                        list.contains(CODE_GRUPO) &&
                        list.contains(CODE_CLASE)),
                eq(ENTERPRISE_ID),
                eq(TENANT_ID)
        );
    }

    @Test
    @DisplayName("Debe activar 2 niveles de jerarquía para cuenta")
    void testCreateAccountCatalogueActivatesTwoLevelsForCuenta() {
        // Arrange
        accountCatalogue.setParent(null);
        accountEntity.setCode(CODE_CUENTA);
        when(accountCatalogueCreateMapper.toEntity(accountCatalogue, null)).thenReturn(accountEntity);
        when(accountCatalogueRepository.findByCode(CODE_CUENTA, ENTERPRISE_ID)).thenReturn(null);
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueCreateMapper.toModel(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.createAccountCatalogue(accountCatalogue);

        // Assert
        verify(accountCatalogueRepository).updateStatusByCodes(
                eq(true),
                argThat(list -> list.size() == 2 &&
                        list.contains(CODE_GRUPO) &&
                        list.contains(CODE_CLASE)),
                eq(ENTERPRISE_ID),
                eq(TENANT_ID)
        );
    }

    @Test
    @DisplayName("Debe activar 1 nivel de jerarquía para grupo")
    void testCreateAccountCatalogueActivatesOneLevelForGrupo() {
        // Arrange
        accountCatalogue.setParent(null);
        accountEntity.setCode(CODE_GRUPO);
        when(accountCatalogueCreateMapper.toEntity(accountCatalogue, null)).thenReturn(accountEntity);
        when(accountCatalogueRepository.findByCode(CODE_GRUPO, ENTERPRISE_ID)).thenReturn(null);
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueCreateMapper.toModel(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.createAccountCatalogue(accountCatalogue);

        // Assert
        verify(accountCatalogueRepository).updateStatusByCodes(
                eq(true),
                argThat(list -> list.size() == 1 && list.contains(CODE_CLASE)),
                eq(ENTERPRISE_ID),
                eq(TENANT_ID)
        );
    }

    @Test
    @DisplayName("No debe activar jerarquía para cuenta clase (raíz)")
    void testCreateAccountCatalogueDoesNotActivateHierarchyForClase() {
        // Arrange
        accountCatalogue.setParent(null);
        accountEntity.setCode(CODE_CLASE);
        when(accountCatalogueCreateMapper.toEntity(accountCatalogue, null)).thenReturn(accountEntity);
        when(accountCatalogueRepository.findByCode(CODE_CLASE, ENTERPRISE_ID)).thenReturn(null);
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueCreateMapper.toModel(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.createAccountCatalogue(accountCatalogue);

        // Assert
        verify(accountCatalogueRepository, never()).updateStatusByCodes(anyBoolean(), anyList(), anyString(), anyString());
    }

    // ========== Tests de cálculo de códigos padre (getParentCode) ==========

    @Test
    @DisplayName("Debe calcular código padre correcto para auxiliar 8 dígitos")
    void testGetParentCodeCalculatesCorrectlyForAuxiliary() {
        // Arrange
        accountCatalogue.setParent(null);
        accountEntity.setCode("12345678");
        when(accountCatalogueCreateMapper.toEntity(accountCatalogue, null)).thenReturn(accountEntity);
        when(accountCatalogueRepository.findByCode("12345678", ENTERPRISE_ID)).thenReturn(null);
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueCreateMapper.toModel(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.createAccountCatalogue(accountCatalogue);

        // Assert
        verify(accountCatalogueRepository).updateStatusByCodes(
                eq(true),
                argThat(list -> list.get(0).equals("123456")),
                anyString(),
                anyString()
        );
    }

    @Test
    @DisplayName("Debe calcular código padre correcto para subcuenta 6 dígitos")
    void testGetParentCodeCalculatesCorrectlyForSubcuenta() {
        // Arrange
        accountCatalogue.setParent(null);
        accountEntity.setCode("123456");
        when(accountCatalogueCreateMapper.toEntity(accountCatalogue, null)).thenReturn(accountEntity);
        when(accountCatalogueRepository.findByCode("123456", ENTERPRISE_ID)).thenReturn(null);
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueCreateMapper.toModel(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.createAccountCatalogue(accountCatalogue);

        // Assert
        verify(accountCatalogueRepository).updateStatusByCodes(
                eq(true),
                argThat(list -> list.get(0).equals("1234")),
                anyString(),
                anyString()
        );
    }

    @Test
    @DisplayName("Debe calcular código padre correcto para cuenta 4 dígitos")
    void testGetParentCodeCalculatesCorrectlyForCuenta() {
        // Arrange
        accountCatalogue.setParent(null);
        accountEntity.setCode("1234");
        when(accountCatalogueCreateMapper.toEntity(accountCatalogue, null)).thenReturn(accountEntity);
        when(accountCatalogueRepository.findByCode("1234", ENTERPRISE_ID)).thenReturn(null);
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueCreateMapper.toModel(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.createAccountCatalogue(accountCatalogue);

        // Assert
        verify(accountCatalogueRepository).updateStatusByCodes(
                eq(true),
                argThat(list -> list.get(0).equals("12")),
                anyString(),
                anyString()
        );
    }

    @Test
    @DisplayName("Debe calcular código padre correcto para grupo 2 dígitos")
    void testGetParentCodeCalculatesCorrectlyForGrupo() {
        // Arrange
        accountCatalogue.setParent(null);
        accountEntity.setCode("12");
        when(accountCatalogueCreateMapper.toEntity(accountCatalogue, null)).thenReturn(accountEntity);
        when(accountCatalogueRepository.findByCode("12", ENTERPRISE_ID)).thenReturn(null);
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueCreateMapper.toModel(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.createAccountCatalogue(accountCatalogue);

        // Assert
        verify(accountCatalogueRepository).updateStatusByCodes(
                eq(true),
                argThat(list -> list.get(0).equals("1")),
                anyString(),
                anyString()
        );
    }

    // ========== Tests de createAllAccountCatalogues - Lista vacía ==========

    @Test
    @DisplayName("Debe retornar lista vacía cuando se pasa lista vacía")
    void testCreateAllAccountCataloguesReturnsEmptyListForEmptyInput() {
        // Arrange
        List<AccountCatalogue> emptyList = new ArrayList<>();
        when(accountCatalogueRepository.saveAll(anyList())).thenReturn(Collections.emptyList());

        // Act
        List<AccountCatalogue> result = adapter.createAllAccountCatalogues(emptyList);

        // Assert
        assertTrue(result.isEmpty());
    }

    // ========== Tests de createAllAccountCatalogues - Sin padres ==========

    @Test
    @DisplayName("Debe crear múltiples cuentas sin padres correctamente")
    void testCreateAllAccountCataloguesWithoutParentsSuccessfully() {
        // Arrange
        AccountCatalogue account1 = AccountCatalogue.builder()
                .code("1")
                .idEnterprise(ENTERPRISE_ID)
                .parent(null)
                .build();
        AccountCatalogue account2 = AccountCatalogue.builder()
                .code("2")
                .idEnterprise(ENTERPRISE_ID)
                .parent(null)
                .build();
        List<AccountCatalogue> accounts = List.of(account1, account2);

        AccountCatalogueEntity entity1 = AccountCatalogueEntity.builder().id(1L).code("1").build();
        AccountCatalogueEntity entity2 = AccountCatalogueEntity.builder().id(2L).code("2").build();

        when(accountCatalogueCreateMapper.toEntity(account1, null)).thenReturn(entity1);
        when(accountCatalogueCreateMapper.toEntity(account2, null)).thenReturn(entity2);
        when(accountCatalogueRepository.saveAll(anyList())).thenReturn(List.of(entity1, entity2));
        when(accountCatalogueCreateMapper.toModel(entity1)).thenReturn(account1);
        when(accountCatalogueCreateMapper.toModel(entity2)).thenReturn(account2);

        // Act
        List<AccountCatalogue> result = adapter.createAllAccountCatalogues(accounts);

        // Assert
        assertEquals(2, result.size());
        verify(accountCatalogueRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("No debe buscar padres cuando ninguna cuenta tiene parent")
    void testCreateAllAccountCataloguesDoesNotSearchParentsWhenNone() {
        // Arrange
        AccountCatalogue account1 = AccountCatalogue.builder()
                .code("1")
                .idEnterprise(ENTERPRISE_ID)
                .parent(null)
                .build();
        List<AccountCatalogue> accounts = List.of(account1);

        AccountCatalogueEntity entity1 = AccountCatalogueEntity.builder().id(1L).code("1").build();

        when(accountCatalogueCreateMapper.toEntity(account1, null)).thenReturn(entity1);
        when(accountCatalogueRepository.saveAll(anyList())).thenReturn(List.of(entity1));
        when(accountCatalogueCreateMapper.toModel(entity1)).thenReturn(account1);

        // Act
        adapter.createAllAccountCatalogues(accounts);

        // Assert
        verify(accountCatalogueRepository, never()).findByCodesIn(anyList(), anyString());
    }

    // ========== Tests de createAllAccountCatalogues - Con padres ==========

    @Test
    @DisplayName("Debe pre-cargar padres en batch cuando hay cuentas con parent")
    void testCreateAllAccountCataloguesPreloadsParentsInBatch() {
        // Arrange
        AccountCatalogue parentCat = AccountCatalogue.builder()
                .code("11")
                .build();
        AccountCatalogue account1 = AccountCatalogue.builder()
                .code("1105")
                .idEnterprise(ENTERPRISE_ID)
                .parent(parentCat)
                .build();
        List<AccountCatalogue> accounts = List.of(account1);

        when(accountCatalogueRepository.findByCodesIn(anyList(), eq(ENTERPRISE_ID)))
                .thenReturn(List.of(parentEntity));
        when(accountCatalogueCreateMapper.toEntity(eq(account1), any())).thenReturn(accountEntity);
        when(accountCatalogueRepository.saveAll(anyList())).thenReturn(List.of(accountEntity));
        when(accountCatalogueCreateMapper.toModel(accountEntity)).thenReturn(account1);

        // Act
        adapter.createAllAccountCatalogues(accounts);

        // Assert
        verify(accountCatalogueRepository).findByCodesIn(anyList(), eq(ENTERPRISE_ID));
    }

    @Test
    @DisplayName("Debe usar padre encontrado del mapa de padres pre-cargados")
    void testCreateAllAccountCataloguesUsesParentFromPreloadedMap() {
        // Arrange
        AccountCatalogue parentCat = AccountCatalogue.builder()
                .code(CODE_SUBCUENTA)
                .build();
        AccountCatalogue account1 = AccountCatalogue.builder()
                .code(CODE_AUXILIARY)
                .idEnterprise(ENTERPRISE_ID)
                .parent(parentCat)
                .build();
        List<AccountCatalogue> accounts = List.of(account1);

        parentEntity.setCode(CODE_SUBCUENTA);
        when(accountCatalogueRepository.findByCodesIn(anyList(), eq(ENTERPRISE_ID)))
                .thenReturn(List.of(parentEntity));
        when(accountCatalogueCreateMapper.toEntity(account1, parentEntity)).thenReturn(accountEntity);
        when(accountCatalogueRepository.saveAll(anyList())).thenReturn(List.of(accountEntity));
        when(accountCatalogueCreateMapper.toModel(accountEntity)).thenReturn(account1);

        // Act
        adapter.createAllAccountCatalogues(accounts);

        // Assert
        verify(accountCatalogueCreateMapper).toEntity(account1, parentEntity);
    }

    @Test
    @DisplayName("Debe manejar múltiples cuentas con diferentes padres")
    void testCreateAllAccountCataloguesHandlesMultipleParents() {
        // Arrange
        AccountCatalogue parent1Cat = AccountCatalogue.builder().code("11").build();
        AccountCatalogue parent2Cat = AccountCatalogue.builder().code("21").build();
        AccountCatalogue account1 = AccountCatalogue.builder()
                .code("1105")
                .idEnterprise(ENTERPRISE_ID)
                .parent(parent1Cat)
                .build();
        AccountCatalogue account2 = AccountCatalogue.builder()
                .code("2105")
                .idEnterprise(ENTERPRISE_ID)
                .parent(parent2Cat)
                .build();
        List<AccountCatalogue> accounts = List.of(account1, account2);

        AccountCatalogueEntity parent1Entity = AccountCatalogueEntity.builder()
                .id(10L).code("11").build();
        AccountCatalogueEntity parent2Entity = AccountCatalogueEntity.builder()
                .id(20L).code("21").build();
        AccountCatalogueEntity entity1 = AccountCatalogueEntity.builder()
                .id(1L).code("1105").build();
        AccountCatalogueEntity entity2 = AccountCatalogueEntity.builder()
                .id(2L).code("2105").build();

        when(accountCatalogueRepository.findByCodesIn(anyList(), eq(ENTERPRISE_ID)))
                .thenReturn(List.of(parent1Entity, parent2Entity));
        when(accountCatalogueCreateMapper.toEntity(account1, parent1Entity)).thenReturn(entity1);
        when(accountCatalogueCreateMapper.toEntity(account2, parent2Entity)).thenReturn(entity2);
        when(accountCatalogueRepository.saveAll(anyList())).thenReturn(List.of(entity1, entity2));
        when(accountCatalogueCreateMapper.toModel(entity1)).thenReturn(account1);
        when(accountCatalogueCreateMapper.toModel(entity2)).thenReturn(account2);

        // Act
        List<AccountCatalogue> result = adapter.createAllAccountCatalogues(accounts);

        // Assert
        assertEquals(2, result.size());
    }

    // ========== Tests de createAllAccountCatalogues - Mapper retorna null ==========

    @Test
    @DisplayName("Debe filtrar cuentas cuando mapper retorna null")
    void testCreateAllAccountCataloguesFiltersWhenMapperReturnsNull() {
        // Arrange
        AccountCatalogue account1 = AccountCatalogue.builder()
                .code("1")
                .idEnterprise(ENTERPRISE_ID)
                .parent(null)
                .build();
        AccountCatalogue account2 = AccountCatalogue.builder()
                .code("2")
                .idEnterprise(ENTERPRISE_ID)
                .parent(null)
                .build();
        List<AccountCatalogue> accounts = List.of(account1, account2);

        AccountCatalogueEntity entity1 = AccountCatalogueEntity.builder().id(1L).code("1").build();

        when(accountCatalogueCreateMapper.toEntity(account1, null)).thenReturn(entity1);
        when(accountCatalogueCreateMapper.toEntity(account2, null)).thenReturn(null);
        when(accountCatalogueRepository.saveAll(anyList())).thenReturn(List.of(entity1));
        when(accountCatalogueCreateMapper.toModel(entity1)).thenReturn(account1);

        // Act
        List<AccountCatalogue> result = adapter.createAllAccountCatalogues(accounts);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("No debe incluir entidades null en batch de guardado")
    void testCreateAllAccountCataloguesDoesNotIncludeNullEntities() {
        // Arrange
        AccountCatalogue account1 = AccountCatalogue.builder()
                .code("1")
                .idEnterprise(ENTERPRISE_ID)
                .parent(null)
                .build();
        List<AccountCatalogue> accounts = List.of(account1);

        when(accountCatalogueCreateMapper.toEntity(account1, null)).thenReturn(null);
        when(accountCatalogueRepository.saveAll(anyList())).thenReturn(Collections.emptyList());

        // Act
        adapter.createAllAccountCatalogues(accounts);

        // Assert
        verify(accountCatalogueRepository).saveAll(argThat(iterable -> !iterable.iterator().hasNext()));
    }

    // ========== Tests de createAllAccountCatalogues - Parent con código null ==========

    @Test
    @DisplayName("Debe manejar parent con código null")
    void testCreateAllAccountCataloguesHandlesParentWithNullCode() {
        // Arrange
        AccountCatalogue parentCat = AccountCatalogue.builder()
                .code(null)
                .build();
        AccountCatalogue account1 = AccountCatalogue.builder()
                .code("1105")
                .idEnterprise(ENTERPRISE_ID)
                .parent(parentCat)
                .build();
        List<AccountCatalogue> accounts = List.of(account1);

        AccountCatalogueEntity entity1 = AccountCatalogueEntity.builder().id(1L).code("1105").build();

        when(accountCatalogueCreateMapper.toEntity(account1, null)).thenReturn(entity1);
        when(accountCatalogueRepository.saveAll(anyList())).thenReturn(List.of(entity1));
        when(accountCatalogueCreateMapper.toModel(entity1)).thenReturn(account1);

        // Act
        List<AccountCatalogue> result = adapter.createAllAccountCatalogues(accounts);

        // Assert
        assertEquals(1, result.size());
        verify(accountCatalogueCreateMapper).toEntity(account1, null);
    }

    // ========== Tests de códigos padre únicos ==========

    @Test
    @DisplayName("Debe eliminar códigos padre duplicados antes de buscar")
    void testCreateAllAccountCataloguesDeduplicatesParentCodes() {
        // Arrange
        AccountCatalogue parentCat = AccountCatalogue.builder().code("11").build();
        AccountCatalogue account1 = AccountCatalogue.builder()
                .code("1105")
                .idEnterprise(ENTERPRISE_ID)
                .parent(parentCat)
                .build();
        AccountCatalogue account2 = AccountCatalogue.builder()
                .code("1110")
                .idEnterprise(ENTERPRISE_ID)
                .parent(parentCat)
                .build();
        List<AccountCatalogue> accounts = List.of(account1, account2);

        parentEntity.setCode("11");
        when(accountCatalogueRepository.findByCodesIn(anyList(), eq(ENTERPRISE_ID)))
                .thenReturn(List.of(parentEntity));
        when(accountCatalogueCreateMapper.toEntity(any(), any())).thenReturn(accountEntity);
        when(accountCatalogueRepository.saveAll(anyList())).thenReturn(List.of(accountEntity, accountEntity));
        when(accountCatalogueCreateMapper.toModel(any())).thenReturn(account1);

        // Act
        adapter.createAllAccountCatalogues(accounts);

        // Assert
        verify(accountCatalogueRepository).findByCodesIn(
                argThat(list -> list.size() == 1 && list.contains("11")),
                eq(ENTERPRISE_ID)
        );
    }

    // ========== Tests de mapeo de resultados ==========

    @Test
    @DisplayName("Debe mapear todas las entidades guardadas a modelos")
    void testCreateAllAccountCataloguesMapsAllSavedEntities() {
        // Arrange
        AccountCatalogue account1 = AccountCatalogue.builder()
                .code("1")
                .idEnterprise(ENTERPRISE_ID)
                .parent(null)
                .build();
        AccountCatalogue account2 = AccountCatalogue.builder()
                .code("2")
                .idEnterprise(ENTERPRISE_ID)
                .parent(null)
                .build();
        List<AccountCatalogue> accounts = List.of(account1, account2);

        AccountCatalogueEntity entity1 = AccountCatalogueEntity.builder().id(1L).code("1").build();
        AccountCatalogueEntity entity2 = AccountCatalogueEntity.builder().id(2L).code("2").build();

        when(accountCatalogueCreateMapper.toEntity(account1, null)).thenReturn(entity1);
        when(accountCatalogueCreateMapper.toEntity(account2, null)).thenReturn(entity2);
        when(accountCatalogueRepository.saveAll(anyList())).thenReturn(List.of(entity1, entity2));
        when(accountCatalogueCreateMapper.toModel(entity1)).thenReturn(account1);
        when(accountCatalogueCreateMapper.toModel(entity2)).thenReturn(account2);

        // Act
        adapter.createAllAccountCatalogues(accounts);

        // Assert
        verify(accountCatalogueCreateMapper).toModel(entity1);
        verify(accountCatalogueCreateMapper).toModel(entity2);
    }

    // ========== Tests de orden de operaciones ==========

    @Test
    @DisplayName("Debe buscar padre antes de crear entidad")
    void testCreateAccountCatalogueSearchesParentBeforeCreatingEntity() {
        // Arrange
        when(accountCatalogueRepository.findByCode(CODE_SUBCUENTA, ENTERPRISE_ID)).thenReturn(parentEntity);
        when(accountCatalogueCreateMapper.toEntity(accountCatalogue, parentEntity)).thenReturn(accountEntity);
        when(accountCatalogueRepository.findByCode(CODE_AUXILIARY, ENTERPRISE_ID)).thenReturn(null);
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueCreateMapper.toModel(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.createAccountCatalogue(accountCatalogue);

        // Assert
        var inOrder = inOrder(accountCatalogueRepository, accountCatalogueCreateMapper);
        inOrder.verify(accountCatalogueRepository).findByCode(CODE_SUBCUENTA, ENTERPRISE_ID);
        inOrder.verify(accountCatalogueCreateMapper).toEntity(accountCatalogue, parentEntity);
    }

    @Test
    @DisplayName("Debe guardar antes de activar jerarquía padre")
    void testCreateAccountCatalogueSavesBeforeActivatingHierarchy() {
        // Arrange
        accountCatalogue.setParent(null);
        when(accountCatalogueCreateMapper.toEntity(accountCatalogue, null)).thenReturn(accountEntity);
        when(accountCatalogueRepository.findByCode(CODE_AUXILIARY, ENTERPRISE_ID)).thenReturn(null);
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueCreateMapper.toModel(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.createAccountCatalogue(accountCatalogue);

        // Assert
        var inOrder = inOrder(accountCatalogueRepository);
        inOrder.verify(accountCatalogueRepository).save(accountEntity);
        inOrder.verify(accountCatalogueRepository).updateStatusByCodes(anyBoolean(), anyList(), anyString(), anyString());
    }

    // ========== Tests de parámetros correctos en updateStatusByCodes ==========

    @Test
    @DisplayName("Debe pasar idEnterprise correcto a updateStatusByCodes")
    void testCreateAccountCataloguePassesCorrectEnterpriseIdToUpdateStatus() {
        // Arrange
        String customEnterpriseId = "CUSTOM_ENT";
        accountCatalogue.setParent(null);
        accountCatalogue.setIdEnterprise(customEnterpriseId);
        accountEntity.setIdEnterprise(customEnterpriseId);
        when(accountCatalogueCreateMapper.toEntity(accountCatalogue, null)).thenReturn(accountEntity);
        when(accountCatalogueRepository.findByCode(CODE_AUXILIARY, customEnterpriseId)).thenReturn(null);
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueCreateMapper.toModel(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.createAccountCatalogue(accountCatalogue);

        // Assert
        verify(accountCatalogueRepository).updateStatusByCodes(eq(true), anyList(), eq(customEnterpriseId), anyString());
    }

    @Test
    @DisplayName("Debe pasar tenantId correcto a updateStatusByCodes")
    void testCreateAccountCataloguePassesCorrectTenantIdToUpdateStatus() {
        // Arrange
        String customTenantId = "CUSTOM_TENANT";
        accountCatalogue.setParent(null);
        accountEntity.setTenantId(customTenantId);
        when(accountCatalogueCreateMapper.toEntity(accountCatalogue, null)).thenReturn(accountEntity);
        when(accountCatalogueRepository.findByCode(CODE_AUXILIARY, ENTERPRISE_ID)).thenReturn(null);
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueCreateMapper.toModel(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.createAccountCatalogue(accountCatalogue);

        // Assert
        verify(accountCatalogueRepository).updateStatusByCodes(eq(true), anyList(), anyString(), eq(customTenantId));
    }

    @Test
    @DisplayName("Debe pasar true como estado a updateStatusByCodes")
    void testCreateAccountCataloguePassesTrueStatusToUpdateByCodes() {
        // Arrange
        accountCatalogue.setParent(null);
        when(accountCatalogueCreateMapper.toEntity(accountCatalogue, null)).thenReturn(accountEntity);
        when(accountCatalogueRepository.findByCode(CODE_AUXILIARY, ENTERPRISE_ID)).thenReturn(null);
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueCreateMapper.toModel(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.createAccountCatalogue(accountCatalogue);

        // Assert
        verify(accountCatalogueRepository).updateStatusByCodes(eq(true), anyList(), anyString(), anyString());
    }

    // ========== Tests de código con longitud no estándar ==========

    @Test
    @DisplayName("No debe activar jerarquía para código con longitud no estándar")
    void testCreateAccountCatalogueDoesNotActivateForNonStandardLength() {
        // Arrange
        accountCatalogue.setParent(null);
        accountEntity.setCode("123");
        when(accountCatalogueCreateMapper.toEntity(accountCatalogue, null)).thenReturn(accountEntity);
        when(accountCatalogueRepository.findByCode("123", ENTERPRISE_ID)).thenReturn(null);
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueCreateMapper.toModel(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.createAccountCatalogue(accountCatalogue);

        // Assert
        verify(accountCatalogueRepository, never()).updateStatusByCodes(anyBoolean(), anyList(), anyString(), anyString());
    }

    @Test
    @DisplayName("No debe activar jerarquía para código de 5 dígitos")
    void testCreateAccountCatalogueDoesNotActivateForFiveDigitCode() {
        // Arrange
        accountCatalogue.setParent(null);
        accountEntity.setCode("12345");
        when(accountCatalogueCreateMapper.toEntity(accountCatalogue, null)).thenReturn(accountEntity);
        when(accountCatalogueRepository.findByCode("12345", ENTERPRISE_ID)).thenReturn(null);
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueCreateMapper.toModel(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.createAccountCatalogue(accountCatalogue);

        // Assert
        verify(accountCatalogueRepository, never()).updateStatusByCodes(anyBoolean(), anyList(), anyString(), anyString());
    }

    @Test
    @DisplayName("No debe activar jerarquía para código de 7 dígitos")
    void testCreateAccountCatalogueDoesNotActivateForSevenDigitCode() {
        // Arrange
        accountCatalogue.setParent(null);
        accountEntity.setCode("1234567");
        when(accountCatalogueCreateMapper.toEntity(accountCatalogue, null)).thenReturn(accountEntity);
        when(accountCatalogueRepository.findByCode("1234567", ENTERPRISE_ID)).thenReturn(null);
        when(accountCatalogueRepository.save(accountEntity)).thenReturn(accountEntity);
        when(accountCatalogueCreateMapper.toModel(accountEntity)).thenReturn(accountCatalogue);

        // Act
        adapter.createAccountCatalogue(accountCatalogue);

        // Assert
        verify(accountCatalogueRepository, never()).updateStatusByCodes(anyBoolean(), anyList(), anyString(), anyString());
    }
}

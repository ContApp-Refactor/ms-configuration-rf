package co.unicauca.edu.co.contables.accounting.catalogue.unit.catalogue.infraestructure.adapters.output.jpaAdapters;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.AccountCatalogueDeleteJpaAdapter;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.entity.AccountCatalogueEntity;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccountCatalogueDeleteJpaAdapterUnitTest {

    @Mock
    private IAccountCatalogueRepository accountCatalogueRepository;

    @InjectMocks
    private AccountCatalogueDeleteJpaAdapter adapter;

    private AccountCatalogueEntity accountEntity;
    private AccountCatalogueEntity childEntity1;
    private AccountCatalogueEntity childEntity2;
    private AccountCatalogueEntity grandChildEntity;

    private static final Long ACCOUNT_ID = 1L;
    private static final Long CHILD_ID_1 = 2L;
    private static final Long CHILD_ID_2 = 3L;
    private static final Long GRANDCHILD_ID = 4L;
    private static final String ENTERPRISE_ID = "ENT001";
    private static final String TENANT_ID = "TENANT001";

    @BeforeEach
    void setUp() {
        grandChildEntity = AccountCatalogueEntity.builder()
                .id(GRANDCHILD_ID)
                .code("11050101")
                .description("Caja Menor")
                .idEnterprise(ENTERPRISE_ID)
                .tenantId(TENANT_ID)
                .status(true)
                .children(new ArrayList<>())
                .build();

        childEntity1 = AccountCatalogueEntity.builder()
                .id(CHILD_ID_1)
                .code("110501")
                .description("Caja General")
                .idEnterprise(ENTERPRISE_ID)
                .tenantId(TENANT_ID)
                .status(true)
                .children(new ArrayList<>())
                .build();

        childEntity2 = AccountCatalogueEntity.builder()
                .id(CHILD_ID_2)
                .code("110502")
                .description("Caja Auxiliar")
                .idEnterprise(ENTERPRISE_ID)
                .tenantId(TENANT_ID)
                .status(true)
                .children(new ArrayList<>())
                .build();

        accountEntity = AccountCatalogueEntity.builder()
                .id(ACCOUNT_ID)
                .code("1105")
                .description("Caja")
                .idEnterprise(ENTERPRISE_ID)
                .tenantId(TENANT_ID)
                .status(true)
                .children(new ArrayList<>())
                .build();
    }

    // ========== Tests de deleteById - Cuenta no encontrada ==========

    @Test
    @DisplayName("No debe eliminar cuando la cuenta no existe")
    void testDeleteByIdDoesNotDeleteWhenAccountNotFound() {
        // Arrange
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.empty());

        // Act
        adapter.deleteById(ACCOUNT_ID);

        // Assert
        verify(accountCatalogueRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Debe buscar cuenta por id antes de eliminar")
    void testDeleteByIdSearchesAccountById() {
        // Arrange
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.empty());

        // Act
        adapter.deleteById(ACCOUNT_ID);

        // Assert
        verify(accountCatalogueRepository).findById(ACCOUNT_ID);
    }

    @Test
    @DisplayName("No debe llamar deletePhysical cuando cuenta no existe")
    void testDeleteByIdDoesNotCallDeletePhysicalWhenNotFound() {
        // Arrange
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.empty());

        // Act
        adapter.deleteById(ACCOUNT_ID);

        // Assert
        verify(accountCatalogueRepository, never()).delete(any(AccountCatalogueEntity.class));
    }

    // ========== Tests de deleteById - Cuenta sin hijos ==========

    @Test
    @DisplayName("Debe eliminar cuenta sin hijos correctamente")
    void testDeleteByIdDeletesAccountWithoutChildren() {
        // Arrange
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        doNothing().when(accountCatalogueRepository).delete(accountEntity);

        // Act
        adapter.deleteById(ACCOUNT_ID);

        // Assert
        verify(accountCatalogueRepository).delete(accountEntity);
    }

    @Test
    @DisplayName("Debe eliminar solo una vez cuando no tiene hijos")
    void testDeleteByIdDeletesOnlyOnceWhenNoChildren() {
        // Arrange
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        doNothing().when(accountCatalogueRepository).delete(accountEntity);

        // Act
        adapter.deleteById(ACCOUNT_ID);

        // Assert
        verify(accountCatalogueRepository, times(1)).delete(any(AccountCatalogueEntity.class));
    }

    @Test
    @DisplayName("Debe manejar cuenta con lista de hijos vacía")
    void testDeleteByIdHandlesEmptyChildrenList() {
        // Arrange
        accountEntity.setChildren(new ArrayList<>());
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        doNothing().when(accountCatalogueRepository).delete(accountEntity);

        // Act
        adapter.deleteById(ACCOUNT_ID);

        // Assert
        verify(accountCatalogueRepository, times(1)).delete(accountEntity);
    }

    @Test
    @DisplayName("Debe manejar cuenta con children null")
    void testDeleteByIdHandlesNullChildren() {
        // Arrange
        accountEntity.setChildren(null);
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        doNothing().when(accountCatalogueRepository).delete(accountEntity);

        // Act
        adapter.deleteById(ACCOUNT_ID);

        // Assert
        verify(accountCatalogueRepository, times(1)).delete(accountEntity);
    }

    // ========== Tests de deleteById - Cuenta con un hijo ==========

    @Test
    @DisplayName("Debe eliminar cuenta con un hijo")
    void testDeleteByIdDeletesAccountWithOneChild() {
        // Arrange
        accountEntity.getChildren().add(childEntity1);
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        doNothing().when(accountCatalogueRepository).delete(any(AccountCatalogueEntity.class));

        // Act
        adapter.deleteById(ACCOUNT_ID);

        // Assert
        verify(accountCatalogueRepository).delete(childEntity1);
        verify(accountCatalogueRepository).delete(accountEntity);
    }

    @Test
    @DisplayName("Debe eliminar hijo antes que padre")
    void testDeleteByIdDeletesChildBeforeParent() {
        // Arrange
        accountEntity.getChildren().add(childEntity1);
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        doNothing().when(accountCatalogueRepository).delete(any(AccountCatalogueEntity.class));

        // Act
        adapter.deleteById(ACCOUNT_ID);

        // Assert
        var inOrder = inOrder(accountCatalogueRepository);
        inOrder.verify(accountCatalogueRepository).delete(childEntity1);
        inOrder.verify(accountCatalogueRepository).delete(accountEntity);
    }

    @Test
    @DisplayName("Debe eliminar 2 entidades cuando tiene un hijo")
    void testDeleteByIdDeletesTwoEntitiesWithOneChild() {
        // Arrange
        accountEntity.getChildren().add(childEntity1);
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        doNothing().when(accountCatalogueRepository).delete(any(AccountCatalogueEntity.class));

        // Act
        adapter.deleteById(ACCOUNT_ID);

        // Assert
        verify(accountCatalogueRepository, times(2)).delete(any(AccountCatalogueEntity.class));
    }

    // ========== Tests de deleteById - Cuenta con múltiples hijos ==========

    @Test
    @DisplayName("Debe eliminar cuenta con múltiples hijos")
    void testDeleteByIdDeletesAccountWithMultipleChildren() {
        // Arrange
        accountEntity.getChildren().add(childEntity1);
        accountEntity.getChildren().add(childEntity2);
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        doNothing().when(accountCatalogueRepository).delete(any(AccountCatalogueEntity.class));

        // Act
        adapter.deleteById(ACCOUNT_ID);

        // Assert
        verify(accountCatalogueRepository).delete(childEntity1);
        verify(accountCatalogueRepository).delete(childEntity2);
        verify(accountCatalogueRepository).delete(accountEntity);
    }

    @Test
    @DisplayName("Debe eliminar todos los hijos antes que el padre")
    void testDeleteByIdDeletesAllChildrenBeforeParent() {
        // Arrange
        accountEntity.getChildren().add(childEntity1);
        accountEntity.getChildren().add(childEntity2);
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        doNothing().when(accountCatalogueRepository).delete(any(AccountCatalogueEntity.class));

        // Act
        adapter.deleteById(ACCOUNT_ID);

        // Assert
        var inOrder = inOrder(accountCatalogueRepository);
        inOrder.verify(accountCatalogueRepository).delete(childEntity1);
        inOrder.verify(accountCatalogueRepository).delete(childEntity2);
        inOrder.verify(accountCatalogueRepository).delete(accountEntity);
    }

    @Test
    @DisplayName("Debe eliminar 3 entidades cuando tiene dos hijos")
    void testDeleteByIdDeletesThreeEntitiesWithTwoChildren() {
        // Arrange
        accountEntity.getChildren().add(childEntity1);
        accountEntity.getChildren().add(childEntity2);
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        doNothing().when(accountCatalogueRepository).delete(any(AccountCatalogueEntity.class));

        // Act
        adapter.deleteById(ACCOUNT_ID);

        // Assert
        verify(accountCatalogueRepository, times(3)).delete(any(AccountCatalogueEntity.class));
    }

    // ========== Tests de deleteById - Jerarquía de 3 niveles ==========

    @Test
    @DisplayName("Debe eliminar jerarquía de 3 niveles correctamente")
    void testDeleteByIdDeletesThreeLevelHierarchy() {
        // Arrange
        childEntity1.getChildren().add(grandChildEntity);
        accountEntity.getChildren().add(childEntity1);
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        doNothing().when(accountCatalogueRepository).delete(any(AccountCatalogueEntity.class));

        // Act
        adapter.deleteById(ACCOUNT_ID);

        // Assert
        verify(accountCatalogueRepository).delete(grandChildEntity);
        verify(accountCatalogueRepository).delete(childEntity1);
        verify(accountCatalogueRepository).delete(accountEntity);
    }

    @Test
    @DisplayName("Debe eliminar nieto antes que hijo y padre")
    void testDeleteByIdDeletesGrandchildBeforeChildAndParent() {
        // Arrange
        childEntity1.getChildren().add(grandChildEntity);
        accountEntity.getChildren().add(childEntity1);
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        doNothing().when(accountCatalogueRepository).delete(any(AccountCatalogueEntity.class));

        // Act
        adapter.deleteById(ACCOUNT_ID);

        // Assert
        var inOrder = inOrder(accountCatalogueRepository);
        inOrder.verify(accountCatalogueRepository).delete(grandChildEntity);
        inOrder.verify(accountCatalogueRepository).delete(childEntity1);
        inOrder.verify(accountCatalogueRepository).delete(accountEntity);
    }

    @Test
    @DisplayName("Debe eliminar 3 entidades en jerarquía de 3 niveles")
    void testDeleteByIdDeletesThreeEntitiesInThreeLevelHierarchy() {
        // Arrange
        childEntity1.getChildren().add(grandChildEntity);
        accountEntity.getChildren().add(childEntity1);
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        doNothing().when(accountCatalogueRepository).delete(any(AccountCatalogueEntity.class));

        // Act
        adapter.deleteById(ACCOUNT_ID);

        // Assert
        verify(accountCatalogueRepository, times(3)).delete(any(AccountCatalogueEntity.class));
    }

    // ========== Tests de deleteById - Jerarquía compleja ==========

    @Test
    @DisplayName("Debe eliminar jerarquía compleja con múltiples ramas")
    void testDeleteByIdDeletesComplexHierarchyWithMultipleBranches() {
        // Arrange
        AccountCatalogueEntity grandChild2 = AccountCatalogueEntity.builder()
                .id(5L)
                .code("11050102")
                .children(new ArrayList<>())
                .build();
        
        childEntity1.getChildren().add(grandChildEntity);
        childEntity1.getChildren().add(grandChild2);
        accountEntity.getChildren().add(childEntity1);
        accountEntity.getChildren().add(childEntity2);
        
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        doNothing().when(accountCatalogueRepository).delete(any(AccountCatalogueEntity.class));

        // Act
        adapter.deleteById(ACCOUNT_ID);

        // Assert
        verify(accountCatalogueRepository).delete(grandChildEntity);
        verify(accountCatalogueRepository).delete(grandChild2);
        verify(accountCatalogueRepository).delete(childEntity1);
        verify(accountCatalogueRepository).delete(childEntity2);
        verify(accountCatalogueRepository).delete(accountEntity);
    }

    @Test
    @DisplayName("Debe eliminar 5 entidades en jerarquía compleja")
    void testDeleteByIdDeletesFiveEntitiesInComplexHierarchy() {
        // Arrange
        AccountCatalogueEntity grandChild2 = AccountCatalogueEntity.builder()
                .id(5L)
                .code("11050102")
                .children(new ArrayList<>())
                .build();
        
        childEntity1.getChildren().add(grandChildEntity);
        childEntity1.getChildren().add(grandChild2);
        accountEntity.getChildren().add(childEntity1);
        accountEntity.getChildren().add(childEntity2);
        
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        doNothing().when(accountCatalogueRepository).delete(any(AccountCatalogueEntity.class));

        // Act
        adapter.deleteById(ACCOUNT_ID);

        // Assert
        verify(accountCatalogueRepository, times(5)).delete(any(AccountCatalogueEntity.class));
    }

    // ========== Tests de deleteById - Jerarquía de 4 niveles ==========

    @Test
    @DisplayName("Debe eliminar jerarquía de 4 niveles correctamente")
    void testDeleteByIdDeletesFourLevelHierarchy() {
        // Arrange
        AccountCatalogueEntity greatGrandChild = AccountCatalogueEntity.builder()
                .id(5L)
                .code("1105010101")
                .children(new ArrayList<>())
                .build();
        
        grandChildEntity.getChildren().add(greatGrandChild);
        childEntity1.getChildren().add(grandChildEntity);
        accountEntity.getChildren().add(childEntity1);
        
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        doNothing().when(accountCatalogueRepository).delete(any(AccountCatalogueEntity.class));

        // Act
        adapter.deleteById(ACCOUNT_ID);

        // Assert
        var inOrder = inOrder(accountCatalogueRepository);
        inOrder.verify(accountCatalogueRepository).delete(greatGrandChild);
        inOrder.verify(accountCatalogueRepository).delete(grandChildEntity);
        inOrder.verify(accountCatalogueRepository).delete(childEntity1);
        inOrder.verify(accountCatalogueRepository).delete(accountEntity);
    }

    @Test
    @DisplayName("Debe eliminar 4 entidades en jerarquía de 4 niveles")
    void testDeleteByIdDeletesFourEntitiesInFourLevelHierarchy() {
        // Arrange
        AccountCatalogueEntity greatGrandChild = AccountCatalogueEntity.builder()
                .id(5L)
                .code("1105010101")
                .children(new ArrayList<>())
                .build();
        
        grandChildEntity.getChildren().add(greatGrandChild);
        childEntity1.getChildren().add(grandChildEntity);
        accountEntity.getChildren().add(childEntity1);
        
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        doNothing().when(accountCatalogueRepository).delete(any(AccountCatalogueEntity.class));

        // Act
        adapter.deleteById(ACCOUNT_ID);

        // Assert
        verify(accountCatalogueRepository, times(4)).delete(any(AccountCatalogueEntity.class));
    }

    // ========== Tests de deleteById - Diferentes IDs ==========

    @Test
    @DisplayName("Debe funcionar con diferentes IDs de cuenta")
    void testDeleteByIdWorksWithDifferentAccountIds() {
        // Arrange
        Long differentId = 999L;
        AccountCatalogueEntity differentEntity = AccountCatalogueEntity.builder()
                .id(differentId)
                .code("9999")
                .children(new ArrayList<>())
                .build();
        
        when(accountCatalogueRepository.findById(differentId)).thenReturn(Optional.of(differentEntity));
        doNothing().when(accountCatalogueRepository).delete(differentEntity);

        // Act
        adapter.deleteById(differentId);

        // Assert
        verify(accountCatalogueRepository).findById(differentId);
        verify(accountCatalogueRepository).delete(differentEntity);
    }

    @Test
    @DisplayName("Debe buscar con el ID correcto proporcionado")
    void testDeleteByIdSearchesWithCorrectId() {
        // Arrange
        Long specificId = 12345L;
        when(accountCatalogueRepository.findById(specificId)).thenReturn(Optional.empty());

        // Act
        adapter.deleteById(specificId);

        // Assert
        verify(accountCatalogueRepository).findById(specificId);
    }

    // ========== Tests de deleteById - Hijo con lista null ==========

    @Test
    @DisplayName("Debe manejar hijo con children null en jerarquía")
    void testDeleteByIdHandlesChildWithNullChildrenInHierarchy() {
        // Arrange
        childEntity1.setChildren(null);
        accountEntity.getChildren().add(childEntity1);
        
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        doNothing().when(accountCatalogueRepository).delete(any(AccountCatalogueEntity.class));

        // Act
        adapter.deleteById(ACCOUNT_ID);

        // Assert
        verify(accountCatalogueRepository).delete(childEntity1);
        verify(accountCatalogueRepository).delete(accountEntity);
    }

    @Test
    @DisplayName("Debe manejar mezcla de hijos con y sin children")
    void testDeleteByIdHandlesMixedChildrenStates() {
        // Arrange
        childEntity1.setChildren(null);
        childEntity2.setChildren(new ArrayList<>());
        accountEntity.getChildren().add(childEntity1);
        accountEntity.getChildren().add(childEntity2);
        
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        doNothing().when(accountCatalogueRepository).delete(any(AccountCatalogueEntity.class));

        // Act
        adapter.deleteById(ACCOUNT_ID);

        // Assert
        verify(accountCatalogueRepository, times(3)).delete(any(AccountCatalogueEntity.class));
    }

    // ========== Tests de verificación de entidades específicas ==========

    @Test
    @DisplayName("Debe eliminar la entidad correcta encontrada por ID")
    void testDeleteByIdDeletesCorrectEntityFoundById() {
        // Arrange
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        doNothing().when(accountCatalogueRepository).delete(accountEntity);

        // Act
        adapter.deleteById(ACCOUNT_ID);

        // Assert
        verify(accountCatalogueRepository).delete(argThat(entity -> 
                entity.getId().equals(ACCOUNT_ID) && 
                entity.getCode().equals("1105")));
    }

    @Test
    @DisplayName("Debe eliminar hijos con sus IDs correctos")
    void testDeleteByIdDeletesChildrenWithCorrectIds() {
        // Arrange
        accountEntity.getChildren().add(childEntity1);
        accountEntity.getChildren().add(childEntity2);
        
        when(accountCatalogueRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(accountEntity));
        doNothing().when(accountCatalogueRepository).delete(any(AccountCatalogueEntity.class));

        // Act
        adapter.deleteById(ACCOUNT_ID);

        // Assert
        verify(accountCatalogueRepository).delete(argThat(entity -> entity.getId().equals(CHILD_ID_1)));
        verify(accountCatalogueRepository).delete(argThat(entity -> entity.getId().equals(CHILD_ID_2)));
    }
}

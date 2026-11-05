package co.unicauca.edu.co.contables.accounting.catalogue.unit.taxes.infraestructure.output.jpaAdapters;

import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters.TaxDeleteJpaAdapter;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters.entity.TaxEntity;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TaxDeleteJpaAdapterUnitTest {

    @Mock
    private ITaxRepository taxRepository;

    @InjectMocks
    private TaxDeleteJpaAdapter taxDeleteJpaAdapter;

    private Long taxId;
    private String idEnterprise;
    private TaxEntity taxEntity;

    @BeforeEach
    void setUp() {
        taxId = 1L;
        idEnterprise = "ENT-001";

        taxEntity = TaxEntity.builder()
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
    @DisplayName("Debe eliminar impuesto exitosamente cuando existe")
    void testDeleteByCodeSuccessWhenTaxExists() {
        // Arrange
        when(taxRepository.findByIdAndIdEnterprise(taxId, idEnterprise)).thenReturn(taxEntity);
        doNothing().when(taxRepository).delete(taxEntity);

        // Act
        boolean result = taxDeleteJpaAdapter.deleteByCode(taxId, idEnterprise);

        // Assert
        assertTrue(result);
        verify(taxRepository).findByIdAndIdEnterprise(taxId, idEnterprise);
        verify(taxRepository).delete(taxEntity);
    }

    @Test
    @DisplayName("Debe retornar false cuando el impuesto no existe")
    void testDeleteByCodeReturnsFalseWhenTaxNotFound() {
        // Arrange
        when(taxRepository.findByIdAndIdEnterprise(taxId, idEnterprise)).thenReturn(null);

        // Act
        boolean result = taxDeleteJpaAdapter.deleteByCode(taxId, idEnterprise);

        // Assert
        assertFalse(result);
        verify(taxRepository).findByIdAndIdEnterprise(taxId, idEnterprise);
        verify(taxRepository, never()).delete(any(TaxEntity.class));
    }

    @Test
    @DisplayName("Debe retornar false cuando la empresa no coincide")
    void testDeleteByCodeReturnsFalseWhenEnterpriseNotMatch() {
        // Arrange
        String differentEnterprise = "ENT-002";
        when(taxRepository.findByIdAndIdEnterprise(taxId, differentEnterprise)).thenReturn(null);

        // Act
        boolean result = taxDeleteJpaAdapter.deleteByCode(taxId, differentEnterprise);

        // Assert
        assertFalse(result);
        verify(taxRepository).findByIdAndIdEnterprise(taxId, differentEnterprise);
        verify(taxRepository, never()).delete(any(TaxEntity.class));
    }

    @Test
    @DisplayName("Debe invocar repositorio en orden correcto al eliminar")
    void testDeleteByCodeInvokesRepositoryInCorrectOrder() {
        // Arrange
        when(taxRepository.findByIdAndIdEnterprise(taxId, idEnterprise)).thenReturn(taxEntity);
        doNothing().when(taxRepository).delete(taxEntity);

        // Act
        taxDeleteJpaAdapter.deleteByCode(taxId, idEnterprise);

        // Assert
        var inOrder = inOrder(taxRepository);
        inOrder.verify(taxRepository).findByIdAndIdEnterprise(taxId, idEnterprise);
        inOrder.verify(taxRepository).delete(taxEntity);
    }

    @Test
    @DisplayName("Debe manejar ID inexistente correctamente")
    void testDeleteByCodeWithNonExistentId() {
        // Arrange
        Long nonExistentId = 999L;
        when(taxRepository.findByIdAndIdEnterprise(nonExistentId, idEnterprise)).thenReturn(null);

        // Act
        boolean result = taxDeleteJpaAdapter.deleteByCode(nonExistentId, idEnterprise);

        // Assert
        assertFalse(result);
        verify(taxRepository).findByIdAndIdEnterprise(nonExistentId, idEnterprise);
    }

    @Test
    @DisplayName("Debe eliminar impuesto inactivo exitosamente")
    void testDeleteByCodeSuccessWithInactiveTax() {
        // Arrange
        TaxEntity inactiveTax = TaxEntity.builder()
                .id(taxId)
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19%")
                .interest(19.0)
                .status(false)
                .usageCount(0)
                .build();
        when(taxRepository.findByIdAndIdEnterprise(taxId, idEnterprise)).thenReturn(inactiveTax);
        doNothing().when(taxRepository).delete(inactiveTax);

        // Act
        boolean result = taxDeleteJpaAdapter.deleteByCode(taxId, idEnterprise);

        // Assert
        assertTrue(result);
        verify(taxRepository).delete(inactiveTax);
    }

    @Test
    @DisplayName("Debe eliminar impuesto con usageCount mayor a cero")
    void testDeleteByCodeSuccessWithUsageCount() {
        // Arrange
        TaxEntity taxWithUsage = TaxEntity.builder()
                .id(taxId)
                .idEnterprise(idEnterprise)
                .code("IVA19")
                .description("Impuesto al valor agregado 19%")
                .interest(19.0)
                .status(true)
                .usageCount(5)
                .build();
        when(taxRepository.findByIdAndIdEnterprise(taxId, idEnterprise)).thenReturn(taxWithUsage);
        doNothing().when(taxRepository).delete(taxWithUsage);

        // Act
        boolean result = taxDeleteJpaAdapter.deleteByCode(taxId, idEnterprise);

        // Assert
        assertTrue(result);
        verify(taxRepository).delete(taxWithUsage);
    }

    @Test
    @DisplayName("Debe manejar empresa con ID vacío")
    void testDeleteByCodeWithEmptyEnterpriseId() {
        // Arrange
        String emptyEnterpriseId = "";
        when(taxRepository.findByIdAndIdEnterprise(taxId, emptyEnterpriseId)).thenReturn(null);

        // Act
        boolean result = taxDeleteJpaAdapter.deleteByCode(taxId, emptyEnterpriseId);

        // Assert
        assertFalse(result);
        verify(taxRepository).findByIdAndIdEnterprise(taxId, emptyEnterpriseId);
    }

    @Test
    @DisplayName("Debe eliminar correctamente con diferentes IDs de empresa")
    void testDeleteByCodeWithDifferentEnterprises() {
        // Arrange
        String enterprise1 = "ENT-001";
        String enterprise2 = "ENT-002";
        TaxEntity taxEnterprise1 = TaxEntity.builder()
                .id(taxId)
                .idEnterprise(enterprise1)
                .code("IVA19")
                .build();
        when(taxRepository.findByIdAndIdEnterprise(taxId, enterprise1)).thenReturn(taxEnterprise1);
        when(taxRepository.findByIdAndIdEnterprise(taxId, enterprise2)).thenReturn(null);

        // Act
        boolean result1 = taxDeleteJpaAdapter.deleteByCode(taxId, enterprise1);
        boolean result2 = taxDeleteJpaAdapter.deleteByCode(taxId, enterprise2);

        // Assert
        assertTrue(result1);
        assertFalse(result2);
    }
}

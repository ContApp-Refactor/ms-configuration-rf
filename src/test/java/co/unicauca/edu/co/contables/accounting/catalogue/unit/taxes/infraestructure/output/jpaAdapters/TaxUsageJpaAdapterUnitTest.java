package co.unicauca.edu.co.contables.accounting.catalogue.unit.taxes.infraestructure.output.jpaAdapters;

import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters.TaxUsageJpaAdapter;
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

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TaxUsageJpaAdapterUnitTest {

    @Mock
    private ITaxRepository taxRepository;

    @InjectMocks
    private TaxUsageJpaAdapter taxUsageJpaAdapter;

    private Long taxId;
    private String enterpriseId;

    @BeforeEach
    void setUp() {
        taxId = 1L;
        enterpriseId = "ENT-001";
    }

    @Test
    @DisplayName("Debe incrementar el contador de uso exitosamente")
    void testIncrementUsageCountSuccess() {
        // Arrange
        doNothing().when(taxRepository).incrementUsageCount(taxId, enterpriseId);

        // Act
        taxUsageJpaAdapter.incrementUsageCount(taxId, enterpriseId);

        // Assert
        verify(taxRepository).incrementUsageCount(taxId, enterpriseId);
    }

    @Test
    @DisplayName("Debe invocar repositorio con parámetros correctos")
    void testIncrementUsageCountInvokesWithCorrectParams() {
        // Arrange
        Long specificTaxId = 100L;
        String specificEnterpriseId = "ENT-100";
        doNothing().when(taxRepository).incrementUsageCount(specificTaxId, specificEnterpriseId);

        // Act
        taxUsageJpaAdapter.incrementUsageCount(specificTaxId, specificEnterpriseId);

        // Assert
        verify(taxRepository).incrementUsageCount(eq(specificTaxId), eq(specificEnterpriseId));
        verify(taxRepository, times(1)).incrementUsageCount(anyLong(), anyString());
    }

    @Test
    @DisplayName("Debe manejar múltiples incrementos para el mismo impuesto")
    void testIncrementUsageCountMultipleTimes() {
        // Arrange
        doNothing().when(taxRepository).incrementUsageCount(taxId, enterpriseId);

        // Act
        taxUsageJpaAdapter.incrementUsageCount(taxId, enterpriseId);
        taxUsageJpaAdapter.incrementUsageCount(taxId, enterpriseId);
        taxUsageJpaAdapter.incrementUsageCount(taxId, enterpriseId);

        // Assert
        verify(taxRepository, times(3)).incrementUsageCount(taxId, enterpriseId);
    }

    @Test
    @DisplayName("Debe manejar incrementos para diferentes impuestos")
    void testIncrementUsageCountDifferentTaxes() {
        // Arrange
        Long taxId1 = 1L;
        Long taxId2 = 2L;
        Long taxId3 = 3L;
        doNothing().when(taxRepository).incrementUsageCount(anyLong(), anyString());

        // Act
        taxUsageJpaAdapter.incrementUsageCount(taxId1, enterpriseId);
        taxUsageJpaAdapter.incrementUsageCount(taxId2, enterpriseId);
        taxUsageJpaAdapter.incrementUsageCount(taxId3, enterpriseId);

        // Assert
        verify(taxRepository).incrementUsageCount(taxId1, enterpriseId);
        verify(taxRepository).incrementUsageCount(taxId2, enterpriseId);
        verify(taxRepository).incrementUsageCount(taxId3, enterpriseId);
    }

    @Test
    @DisplayName("Debe manejar incrementos para diferentes empresas")
    void testIncrementUsageCountDifferentEnterprises() {
        // Arrange
        String enterprise1 = "ENT-001";
        String enterprise2 = "ENT-002";
        doNothing().when(taxRepository).incrementUsageCount(anyLong(), anyString());

        // Act
        taxUsageJpaAdapter.incrementUsageCount(taxId, enterprise1);
        taxUsageJpaAdapter.incrementUsageCount(taxId, enterprise2);

        // Assert
        verify(taxRepository).incrementUsageCount(taxId, enterprise1);
        verify(taxRepository).incrementUsageCount(taxId, enterprise2);
    }

    @Test
    @DisplayName("Debe propagar excepción cuando el repositorio falla")
    void testIncrementUsageCountPropagatesException() {
        // Arrange
        RuntimeException expectedException = new RuntimeException("Error de base de datos");
        doThrow(expectedException).when(taxRepository).incrementUsageCount(taxId, enterpriseId);

        // Act & Assert
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
            () -> taxUsageJpaAdapter.incrementUsageCount(taxId, enterpriseId));
        
        verify(taxRepository).incrementUsageCount(taxId, enterpriseId);
    }
}

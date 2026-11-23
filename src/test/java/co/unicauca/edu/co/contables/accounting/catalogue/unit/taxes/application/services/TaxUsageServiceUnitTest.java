package co.unicauca.edu.co.contables.accounting.catalogue.unit.taxes.application.services;

import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.output.ITaxUsageOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.services.TaxUsageService;
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
class TaxUsageServiceUnitTest {

    @Mock
    private ITaxUsageOutputPort taxUsageOutputPort;

    @InjectMocks
    private TaxUsageService taxUsageService;

    private Long taxId;
    private String enterpriseId;

    @BeforeEach
    void setUp() {
        taxId = 1L;
        enterpriseId = "ENT-001";
    }

    @Test
    @DisplayName("Debe incrementar contador de uso exitosamente")
    void testIncrementUsageCountSuccess() {
        // Arrange
        doNothing().when(taxUsageOutputPort).incrementUsageCount(taxId, enterpriseId);

        // Act
        taxUsageService.incrementUsageCount(taxId, enterpriseId);

        // Assert
        verify(taxUsageOutputPort).incrementUsageCount(taxId, enterpriseId);
    }

    @Test
    @DisplayName("Debe llamar al puerto de salida con los parámetros correctos")
    void testIncrementUsageCountPassesCorrectParameters() {
        // Arrange
        Long specificTaxId = 99L;
        String specificEnterpriseId = "ENT-SPECIFIC";
        doNothing().when(taxUsageOutputPort).incrementUsageCount(specificTaxId, specificEnterpriseId);

        // Act
        taxUsageService.incrementUsageCount(specificTaxId, specificEnterpriseId);

        // Assert
        verify(taxUsageOutputPort).incrementUsageCount(eq(specificTaxId), eq(specificEnterpriseId));
        verify(taxUsageOutputPort, times(1)).incrementUsageCount(anyLong(), anyString());
    }

    @Test
    @DisplayName("Debe manejar diferentes IDs de empresa")
    void testIncrementUsageCountWithDifferentEnterprises() {
        // Arrange
        String enterpriseA = "ENT-A";
        String enterpriseB = "ENT-B";
        doNothing().when(taxUsageOutputPort).incrementUsageCount(taxId, enterpriseA);
        doNothing().when(taxUsageOutputPort).incrementUsageCount(taxId, enterpriseB);

        // Act
        taxUsageService.incrementUsageCount(taxId, enterpriseA);
        taxUsageService.incrementUsageCount(taxId, enterpriseB);

        // Assert
        verify(taxUsageOutputPort).incrementUsageCount(taxId, enterpriseA);
        verify(taxUsageOutputPort).incrementUsageCount(taxId, enterpriseB);
    }

    @Test
    @DisplayName("Debe manejar diferentes IDs de impuesto para misma empresa")
    void testIncrementUsageCountWithDifferentTaxIds() {
        // Arrange
        Long taxId1 = 1L;
        Long taxId2 = 2L;
        Long taxId3 = 3L;
        doNothing().when(taxUsageOutputPort).incrementUsageCount(anyLong(), eq(enterpriseId));

        // Act
        taxUsageService.incrementUsageCount(taxId1, enterpriseId);
        taxUsageService.incrementUsageCount(taxId2, enterpriseId);
        taxUsageService.incrementUsageCount(taxId3, enterpriseId);

        // Assert
        verify(taxUsageOutputPort).incrementUsageCount(taxId1, enterpriseId);
        verify(taxUsageOutputPort).incrementUsageCount(taxId2, enterpriseId);
        verify(taxUsageOutputPort).incrementUsageCount(taxId3, enterpriseId);
        verify(taxUsageOutputPort, times(3)).incrementUsageCount(anyLong(), eq(enterpriseId));
    }

    @Test
    @DisplayName("Debe propagar excepción cuando el puerto de salida falla")
    void testIncrementUsageCountPropagatesException() {
        // Arrange
        RuntimeException expectedException = new RuntimeException("Error de persistencia");
        doThrow(expectedException).when(taxUsageOutputPort).incrementUsageCount(taxId, enterpriseId);

        // Act & Assert
        try {
            taxUsageService.incrementUsageCount(taxId, enterpriseId);
        } catch (RuntimeException e) {
            verify(taxUsageOutputPort).incrementUsageCount(taxId, enterpriseId);
        }
    }

    @Test
    @DisplayName("Debe invocar puerto de salida exactamente una vez por llamada")
    void testIncrementUsageCountCallsOutputPortOnce() {
        // Arrange
        doNothing().when(taxUsageOutputPort).incrementUsageCount(taxId, enterpriseId);

        // Act
        taxUsageService.incrementUsageCount(taxId, enterpriseId);

        // Assert
        verify(taxUsageOutputPort, times(1)).incrementUsageCount(taxId, enterpriseId);
        verifyNoMoreInteractions(taxUsageOutputPort);
    }

    @Test
    @DisplayName("Debe incrementar uso para impuesto con ID alto")
    void testIncrementUsageCountWithHighTaxId() {
        // Arrange
        Long highTaxId = 999999L;
        doNothing().when(taxUsageOutputPort).incrementUsageCount(highTaxId, enterpriseId);

        // Act
        taxUsageService.incrementUsageCount(highTaxId, enterpriseId);

        // Assert
        verify(taxUsageOutputPort).incrementUsageCount(highTaxId, enterpriseId);
    }

    @Test
    @DisplayName("Debe manejar ID de empresa con caracteres especiales")
    void testIncrementUsageCountWithSpecialEnterpriseId() {
        // Arrange
        String specialEnterpriseId = "ENT-001-TEST_SPECIAL";
        doNothing().when(taxUsageOutputPort).incrementUsageCount(taxId, specialEnterpriseId);

        // Act
        taxUsageService.incrementUsageCount(taxId, specialEnterpriseId);

        // Assert
        verify(taxUsageOutputPort).incrementUsageCount(taxId, specialEnterpriseId);
    }
}

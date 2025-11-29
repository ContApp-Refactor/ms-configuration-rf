package co.unicauca.edu.co.contables.configuration.unit.typesOfDocuments.domain.services;

import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.models.DocumentType;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.services.DocumentTypeUsageService;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.services.IDocumentTypeService;
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
class DocumentTypeUsageServiceUnitTest {

    @Mock
    private IDocumentTypeService documentTypeService;

    @InjectMocks
    private DocumentTypeUsageService documentTypeUsageService;

    private static final Long DOCUMENT_TYPE_ID = 1L;
    private static final String ENTERPRISE_ID = "ENT001";

    private DocumentType documentType;

    @BeforeEach
    void setUp() {
        documentType = DocumentType.builder()
                .id(DOCUMENT_TYPE_ID)
                .name("Factura de venta")
                .prefix("FAC")
                .idEnterprise(ENTERPRISE_ID)
                .usageCount(0)
                .status(true)
                .build();
    }

    // ========== INCREMENT USAGE COUNT TESTS ==========

    @Test
    @DisplayName("incrementUsageCount - Debe incrementar contador de uso de 0 a 1")
    void testIncrementUsageCountFromZeroToOne() {
        // Arrange
        documentType.setUsageCount(0);
        when(documentTypeService.findById(DOCUMENT_TYPE_ID, ENTERPRISE_ID)).thenReturn(documentType);
        doNothing().when(documentTypeService).updateUsageCount(DOCUMENT_TYPE_ID, ENTERPRISE_ID, 1);

        // Act
        documentTypeUsageService.incrementUsageCount(DOCUMENT_TYPE_ID, ENTERPRISE_ID);

        // Assert
        verify(documentTypeService).findById(DOCUMENT_TYPE_ID, ENTERPRISE_ID);
        verify(documentTypeService).updateUsageCount(DOCUMENT_TYPE_ID, ENTERPRISE_ID, 1);
    }

    @Test
    @DisplayName("incrementUsageCount - Debe incrementar contador de uso de 5 a 6")
    void testIncrementUsageCountFromFiveToSix() {
        // Arrange
        documentType.setUsageCount(5);
        when(documentTypeService.findById(DOCUMENT_TYPE_ID, ENTERPRISE_ID)).thenReturn(documentType);
        doNothing().when(documentTypeService).updateUsageCount(DOCUMENT_TYPE_ID, ENTERPRISE_ID, 6);

        // Act
        documentTypeUsageService.incrementUsageCount(DOCUMENT_TYPE_ID, ENTERPRISE_ID);

        // Assert
        verify(documentTypeService).findById(DOCUMENT_TYPE_ID, ENTERPRISE_ID);
        verify(documentTypeService).updateUsageCount(DOCUMENT_TYPE_ID, ENTERPRISE_ID, 6);
    }

    @Test
    @DisplayName("incrementUsageCount - Debe incrementar contador de uso con valor alto")
    void testIncrementUsageCountWithHighValue() {
        // Arrange
        documentType.setUsageCount(999);
        when(documentTypeService.findById(DOCUMENT_TYPE_ID, ENTERPRISE_ID)).thenReturn(documentType);
        doNothing().when(documentTypeService).updateUsageCount(DOCUMENT_TYPE_ID, ENTERPRISE_ID, 1000);

        // Act
        documentTypeUsageService.incrementUsageCount(DOCUMENT_TYPE_ID, ENTERPRISE_ID);

        // Assert
        verify(documentTypeService).findById(DOCUMENT_TYPE_ID, ENTERPRISE_ID);
        verify(documentTypeService).updateUsageCount(DOCUMENT_TYPE_ID, ENTERPRISE_ID, 1000);
    }

    @Test
    @DisplayName("incrementUsageCount - Debe propagar excepción cuando tipo de documento no existe")
    void testIncrementUsageCountThrowsExceptionWhenNotFound() {
        // Arrange
        when(documentTypeService.findById(DOCUMENT_TYPE_ID, ENTERPRISE_ID))
                .thenThrow(new RuntimeException("Tipo de documento no encontrado"));

        // Act & Assert
        assertThrows(RuntimeException.class, 
                () -> documentTypeUsageService.incrementUsageCount(DOCUMENT_TYPE_ID, ENTERPRISE_ID));
        verify(documentTypeService).findById(DOCUMENT_TYPE_ID, ENTERPRISE_ID);
        verify(documentTypeService, never()).updateUsageCount(any(), any(), any());
    }

    @Test
    @DisplayName("incrementUsageCount - Debe propagar excepción cuando falla actualización")
    void testIncrementUsageCountThrowsExceptionWhenUpdateFails() {
        // Arrange
        documentType.setUsageCount(0);
        when(documentTypeService.findById(DOCUMENT_TYPE_ID, ENTERPRISE_ID)).thenReturn(documentType);
        doThrow(new RuntimeException("Error al actualizar")).when(documentTypeService)
                .updateUsageCount(DOCUMENT_TYPE_ID, ENTERPRISE_ID, 1);

        // Act & Assert
        assertThrows(RuntimeException.class, 
                () -> documentTypeUsageService.incrementUsageCount(DOCUMENT_TYPE_ID, ENTERPRISE_ID));
        verify(documentTypeService).findById(DOCUMENT_TYPE_ID, ENTERPRISE_ID);
        verify(documentTypeService).updateUsageCount(DOCUMENT_TYPE_ID, ENTERPRISE_ID, 1);
    }

    @Test
    @DisplayName("incrementUsageCount - Debe llamar findById con parámetros correctos")
    void testIncrementUsageCountCallsFindByIdWithCorrectParams() {
        // Arrange
        Long specificDocTypeId = 42L;
        String specificEnterpriseId = "ENT_SPECIFIC";
        DocumentType specificDocType = DocumentType.builder()
                .id(specificDocTypeId)
                .usageCount(10)
                .build();
        when(documentTypeService.findById(specificDocTypeId, specificEnterpriseId)).thenReturn(specificDocType);

        // Act
        documentTypeUsageService.incrementUsageCount(specificDocTypeId, specificEnterpriseId);

        // Assert
        verify(documentTypeService).findById(specificDocTypeId, specificEnterpriseId);
        verify(documentTypeService).updateUsageCount(specificDocTypeId, specificEnterpriseId, 11);
    }

    @Test
    @DisplayName("incrementUsageCount - Debe calcular nuevo contador correctamente")
    void testIncrementUsageCountCalculatesNewCountCorrectly() {
        // Arrange
        int currentCount = 25;
        int expectedNewCount = 26;
        documentType.setUsageCount(currentCount);
        when(documentTypeService.findById(DOCUMENT_TYPE_ID, ENTERPRISE_ID)).thenReturn(documentType);

        // Act
        documentTypeUsageService.incrementUsageCount(DOCUMENT_TYPE_ID, ENTERPRISE_ID);

        // Assert
        verify(documentTypeService).updateUsageCount(DOCUMENT_TYPE_ID, ENTERPRISE_ID, expectedNewCount);
    }
}

package co.unicauca.edu.co.contables.configuration.unit.typesOfDocuments.domain.messageBroker;

import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.messageBroker.SpringEventsDocumentTypeUsageListener;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.messageBroker.dto.DocumentTypeUsageDto;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.messageBroker.dto.EventDto;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.messageBroker.enums.EventUsageType;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.services.IDocumentTypeUsage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SpringEventsDocumentTypeUsageListenerTest {

    @Mock
    private IDocumentTypeUsage documentTypeUsage;

    @InjectMocks
    private SpringEventsDocumentTypeUsageListener documentTypeUsageListener;

    private static final Long DOCUMENT_TYPE_ID = 1L;
    private static final String ENTERPRISE_ID = "ENT001";
    private static final Integer QUANTITY_USED = 5;

    private DocumentTypeUsageDto validUsageDto;
    private EventDto<DocumentTypeUsageDto, EventUsageType> validEvent;

    @BeforeEach
    void setUp() {
        validUsageDto = new DocumentTypeUsageDto(DOCUMENT_TYPE_ID, ENTERPRISE_ID, QUANTITY_USED);
        validEvent = new EventDto<>(validUsageDto, EventUsageType.USED);
    }

    @Test
    @DisplayName("onDocumentTypeUsed - Debe procesar evento válido e incrementar uso")
    void testHandleDocumentTypeEventWithValidEvent() throws IOException {

        documentTypeUsageListener.onDocumentTypeUsed(validUsageDto);

        verify(documentTypeUsage).incrementUsageCount(DOCUMENT_TYPE_ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("onDocumentTypeUsed - Debe ignorar evento null sin procesar")
    void testHandleDocumentTypeEventWithNullEvent() throws IOException {

        documentTypeUsageListener.onDocumentTypeUsed(null);

        verify(documentTypeUsage, never()).incrementUsageCount(any(), any());
    }

    @Test
    @DisplayName("isValidEvent - Debe retornar false cuando documentTypeId es null")
    void testIsValidEventReturnsFalseWhenDocumentTypeIdNull() throws IOException {

        DocumentTypeUsageDto dtoWithNullId = new DocumentTypeUsageDto(null, ENTERPRISE_ID, QUANTITY_USED);
        EventDto<DocumentTypeUsageDto, EventUsageType> event = new EventDto<>(dtoWithNullId, EventUsageType.USED);

        documentTypeUsageListener.onDocumentTypeUsed(event.getData());

        verify(documentTypeUsage, never()).incrementUsageCount(any(), any());
    }

    @Test
    @DisplayName("isValidEvent - Debe retornar false cuando enterpriseId es null")
    void testIsValidEventReturnsFalseWhenEnterpriseIdNull() throws IOException {

        DocumentTypeUsageDto dtoWithNullEnterprise = new DocumentTypeUsageDto(DOCUMENT_TYPE_ID, null, QUANTITY_USED);
        EventDto<DocumentTypeUsageDto, EventUsageType> event = new EventDto<>(dtoWithNullEnterprise, EventUsageType.USED);

        documentTypeUsageListener.onDocumentTypeUsed(event.getData());

        verify(documentTypeUsage, never()).incrementUsageCount(any(), any());
    }

    @Test
    @DisplayName("isValidEvent - Debe retornar false cuando enterpriseId está vacío")
    void testIsValidEventReturnsFalseWhenEnterpriseIdEmpty() throws IOException {

        DocumentTypeUsageDto dtoWithEmptyEnterprise = new DocumentTypeUsageDto(DOCUMENT_TYPE_ID, "", QUANTITY_USED);
        EventDto<DocumentTypeUsageDto, EventUsageType> event = new EventDto<>(dtoWithEmptyEnterprise, EventUsageType.USED);

        documentTypeUsageListener.onDocumentTypeUsed(event.getData());

        verify(documentTypeUsage, never()).incrementUsageCount(any(), any());
    }

    @Test
    @DisplayName("isValidEvent - Debe retornar false cuando enterpriseId tiene solo espacios")
    void testIsValidEventReturnsFalseWhenEnterpriseIdOnlySpaces() throws IOException {

        DocumentTypeUsageDto dtoWithSpacesEnterprise = new DocumentTypeUsageDto(DOCUMENT_TYPE_ID, "   ", QUANTITY_USED);
        EventDto<DocumentTypeUsageDto, EventUsageType> event = new EventDto<>(dtoWithSpacesEnterprise, EventUsageType.USED);

        documentTypeUsageListener.onDocumentTypeUsed(event.getData());

        verify(documentTypeUsage, never()).incrementUsageCount(any(), any());
    }

    @Test
    @DisplayName("isValidEvent - Debe retornar false cuando quantityUsed es null")
    void testIsValidEventReturnsFalseWhenQuantityUsedNull() throws IOException {

        DocumentTypeUsageDto dtoWithNullQuantity = new DocumentTypeUsageDto(DOCUMENT_TYPE_ID, ENTERPRISE_ID, null);
        EventDto<DocumentTypeUsageDto, EventUsageType> event = new EventDto<>(dtoWithNullQuantity, EventUsageType.USED);

        documentTypeUsageListener.onDocumentTypeUsed(event.getData());

        verify(documentTypeUsage, never()).incrementUsageCount(any(), any());
    }

    @Test
    @DisplayName("isValidEvent - Debe retornar false cuando quantityUsed es cero")
    void testIsValidEventReturnsFalseWhenQuantityUsedZero() throws IOException {

        DocumentTypeUsageDto dtoWithZeroQuantity = new DocumentTypeUsageDto(DOCUMENT_TYPE_ID, ENTERPRISE_ID, 0);
        EventDto<DocumentTypeUsageDto, EventUsageType> event = new EventDto<>(dtoWithZeroQuantity, EventUsageType.USED);

        documentTypeUsageListener.onDocumentTypeUsed(event.getData());

        verify(documentTypeUsage, never()).incrementUsageCount(any(), any());
    }

    @Test
    @DisplayName("isValidEvent - Debe retornar false cuando quantityUsed es negativo")
    void testIsValidEventReturnsFalseWhenQuantityUsedNegative() throws IOException {

        DocumentTypeUsageDto dtoWithNegativeQuantity = new DocumentTypeUsageDto(DOCUMENT_TYPE_ID, ENTERPRISE_ID, -1);
        EventDto<DocumentTypeUsageDto, EventUsageType> event = new EventDto<>(dtoWithNegativeQuantity, EventUsageType.USED);

        documentTypeUsageListener.onDocumentTypeUsed(event.getData());

        verify(documentTypeUsage, never()).incrementUsageCount(any(), any());
    }

    @Test
    @DisplayName("onDocumentTypeUsed - Debe llamar incrementUsageCount con datos correctos")
    void testProcessEventCallsIncrementUsageCount() throws IOException {

        documentTypeUsageListener.onDocumentTypeUsed(validUsageDto);

        verify(documentTypeUsage).incrementUsageCount(DOCUMENT_TYPE_ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("onDocumentTypeUsed - Debe procesar evento con quantityUsed mayor a 1")
    void testProcessEventWithQuantityGreaterThanOne() throws IOException {

        DocumentTypeUsageDto dto = new DocumentTypeUsageDto(DOCUMENT_TYPE_ID, ENTERPRISE_ID, 100);
        EventDto<DocumentTypeUsageDto, EventUsageType> event = new EventDto<>(dto, EventUsageType.USED);

        documentTypeUsageListener.onDocumentTypeUsed(event.getData());

        verify(documentTypeUsage).incrementUsageCount(DOCUMENT_TYPE_ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("onDocumentTypeUsed - Debe manejar excepción del servicio")
    void testProcessEventHandlesServiceException() throws IOException {

        doThrow(new RuntimeException("Error de servicio")).when(documentTypeUsage)
                .incrementUsageCount(DOCUMENT_TYPE_ID, ENTERPRISE_ID);

        assertDoesNotThrow(() -> documentTypeUsageListener.onDocumentTypeUsed(validUsageDto));
    }

    @Test
    @DisplayName("onDocumentTypeUsed - Debe extraer tipo USED correctamente")
    void testExtractEventTypeReturnsUsedType() throws IOException {

        documentTypeUsageListener.onDocumentTypeUsed(validUsageDto);

        verify(documentTypeUsage).incrementUsageCount(any(), any());
    }

}

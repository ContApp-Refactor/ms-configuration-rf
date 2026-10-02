package co.unicauca.edu.co.contables.accounting.catalogue.unit.bankAccounts.domain.messageBroker;

import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.services.IBankAccountUsage;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.messageBroker.BankAccountUsageListener;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.messageBroker.dto.BankAccountUsageDto;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.messageBroker.dto.EventDto;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.messageBroker.enums.EventUsageType;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.amqp.core.Message;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BankAccountUsageListenerUnitTest {

    @Mock
    private IBankAccountUsage bankAccountUsage;

    @Mock
    private Channel channel;

    @Mock
    private Message message;

    @InjectMocks
    private BankAccountUsageListener bankAccountUsageListener;

    private EventDto<BankAccountUsageDto, EventUsageType> validEvent;
    private BankAccountUsageDto validUsageDto;

    private static final Long BANK_ACCOUNT_ID = 1L;
    private static final String ENTERPRISE_ID = "ENT-001";
    private static final Integer QUANTITY_USED = 1;
    private static final long DELIVERY_TAG = 123L;

    @BeforeEach
    void setUp() {
        validUsageDto = new BankAccountUsageDto();
        validUsageDto.setBankAccountId(BANK_ACCOUNT_ID);
        validUsageDto.setEnterpriseId(ENTERPRISE_ID);
        validUsageDto.setQuantityUsed(QUANTITY_USED);

        validEvent = new EventDto<>();
        validEvent.setData(validUsageDto);
        validEvent.setType(EventUsageType.USED);
    }

    // ==================== Tests de handleBankAccountEvent ====================

    @Test
    @DisplayName("Debe procesar evento válido correctamente")
    void testHandleBankAccountEvent_WithValidEvent_ProcessesSuccessfully() throws Exception {
        // Arrange
        doNothing().when(bankAccountUsage).incrementUsageCount(BANK_ACCOUNT_ID, ENTERPRISE_ID);

        // Act
        bankAccountUsageListener.handleBankAccountEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(bankAccountUsage).incrementUsageCount(BANK_ACCOUNT_ID, ENTERPRISE_ID);
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Debe enviar ACK después de procesar evento válido")
    void testHandleBankAccountEvent_SendsAcknowledgment() throws Exception {
        // Arrange
        doNothing().when(bankAccountUsage).incrementUsageCount(BANK_ACCOUNT_ID, ENTERPRISE_ID);

        // Act
        bankAccountUsageListener.handleBankAccountEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Debe invocar incrementUsageCount con parámetros correctos")
    void testHandleBankAccountEvent_InvokesIncrementUsageCountWithCorrectParameters() throws Exception {
        // Arrange
        doNothing().when(bankAccountUsage).incrementUsageCount(BANK_ACCOUNT_ID, ENTERPRISE_ID);

        // Act
        bankAccountUsageListener.handleBankAccountEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(bankAccountUsage).incrementUsageCount(BANK_ACCOUNT_ID, ENTERPRISE_ID);
    }

    // ==================== Tests de isValidEvent - Validación indirecta ====================

    @Test
    @DisplayName("Debe rechazar evento nulo")
    void testIsValidEvent_WithNullEvent_DoesNotProcessEvent() throws Exception {
        // Act
        bankAccountUsageListener.handleBankAccountEvent(null, message, channel, DELIVERY_TAG);

        // Assert
        verify(bankAccountUsage, never()).incrementUsageCount(any(), any());
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Debe rechazar evento con tipo nulo")
    void testIsValidEvent_WithNullType_DoesNotProcessEvent() throws Exception {
        // Arrange
        validEvent.setType(null);

        // Act
        bankAccountUsageListener.handleBankAccountEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(bankAccountUsage, never()).incrementUsageCount(any(), any());
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Debe rechazar evento con data nulo")
    void testIsValidEvent_WithNullData_DoesNotProcessEvent() throws Exception {
        // Arrange
        validEvent.setData(null);

        // Act
        bankAccountUsageListener.handleBankAccountEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(bankAccountUsage, never()).incrementUsageCount(any(), any());
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Debe rechazar evento con bankAccountId nulo")
    void testIsValidEvent_WithNullBankAccountId_DoesNotProcessEvent() throws Exception {
        // Arrange
        validUsageDto.setBankAccountId(null);

        // Act
        bankAccountUsageListener.handleBankAccountEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(bankAccountUsage, never()).incrementUsageCount(any(), any());
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Debe rechazar evento con enterpriseId nulo")
    void testIsValidEvent_WithNullEnterpriseId_DoesNotProcessEvent() throws Exception {
        // Arrange
        validUsageDto.setEnterpriseId(null);

        // Act
        bankAccountUsageListener.handleBankAccountEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(bankAccountUsage, never()).incrementUsageCount(any(), any());
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Debe rechazar evento con enterpriseId vacío")
    void testIsValidEvent_WithEmptyEnterpriseId_DoesNotProcessEvent() throws Exception {
        // Arrange
        validUsageDto.setEnterpriseId("");

        // Act
        bankAccountUsageListener.handleBankAccountEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(bankAccountUsage, never()).incrementUsageCount(any(), any());
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Debe rechazar evento con enterpriseId en blanco")
    void testIsValidEvent_WithBlankEnterpriseId_DoesNotProcessEvent() throws Exception {
        // Arrange
        validUsageDto.setEnterpriseId("   ");

        // Act
        bankAccountUsageListener.handleBankAccountEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(bankAccountUsage, never()).incrementUsageCount(any(), any());
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Debe rechazar evento con quantityUsed nulo")
    void testIsValidEvent_WithNullQuantityUsed_DoesNotProcessEvent() throws Exception {
        // Arrange
        validUsageDto.setQuantityUsed(null);

        // Act
        bankAccountUsageListener.handleBankAccountEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(bankAccountUsage, never()).incrementUsageCount(any(), any());
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Debe rechazar evento con quantityUsed cero")
    void testIsValidEvent_WithZeroQuantityUsed_DoesNotProcessEvent() throws Exception {
        // Arrange
        validUsageDto.setQuantityUsed(0);

        // Act
        bankAccountUsageListener.handleBankAccountEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(bankAccountUsage, never()).incrementUsageCount(any(), any());
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Debe rechazar evento con quantityUsed negativo")
    void testIsValidEvent_WithNegativeQuantityUsed_DoesNotProcessEvent() throws Exception {
        // Arrange
        validUsageDto.setQuantityUsed(-1);

        // Act
        bankAccountUsageListener.handleBankAccountEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(bankAccountUsage, never()).incrementUsageCount(any(), any());
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Debe aceptar evento con quantityUsed positivo")
    void testIsValidEvent_WithPositiveQuantityUsed_ProcessesEvent() throws Exception {
        // Arrange
        validUsageDto.setQuantityUsed(10);
        doNothing().when(bankAccountUsage).incrementUsageCount(BANK_ACCOUNT_ID, ENTERPRISE_ID);

        // Act
        bankAccountUsageListener.handleBankAccountEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(bankAccountUsage).incrementUsageCount(BANK_ACCOUNT_ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe aceptar evento válido completo")
    void testIsValidEvent_WithValidEvent_ProcessesEvent() throws Exception {
        // Arrange
        doNothing().when(bankAccountUsage).incrementUsageCount(BANK_ACCOUNT_ID, ENTERPRISE_ID);

        // Act
        bankAccountUsageListener.handleBankAccountEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(bankAccountUsage).incrementUsageCount(BANK_ACCOUNT_ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe procesar evento tipo USED")
    void testHandleBankAccountEvent_WithUsedType_ProcessesSuccessfully() throws Exception {
        // Arrange
        validEvent.setType(EventUsageType.USED);
        doNothing().when(bankAccountUsage).incrementUsageCount(BANK_ACCOUNT_ID, ENTERPRISE_ID);

        // Act
        bankAccountUsageListener.handleBankAccountEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(bankAccountUsage).incrementUsageCount(BANK_ACCOUNT_ID, ENTERPRISE_ID);
    }

    // ==================== Tests de manejo de excepciones ====================

    @Test
    @DisplayName("Debe manejar excepción en processEvent y enviar ACK")
    void testHandleBankAccountEvent_WhenExceptionOccurs_SendsAck() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Error al incrementar uso")).when(bankAccountUsage)
                .incrementUsageCount(BANK_ACCOUNT_ID, ENTERPRISE_ID);

        // Act
        bankAccountUsageListener.handleBankAccountEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Debe continuar procesamiento después de excepción")
    void testHandleBankAccountEvent_WhenExceptionOccurs_DoesNotThrowException() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Error al incrementar uso")).when(bankAccountUsage)
                .incrementUsageCount(BANK_ACCOUNT_ID, ENTERPRISE_ID);

        // Act & Assert
        bankAccountUsageListener.handleBankAccountEvent(validEvent, message, channel, DELIVERY_TAG);
        verify(bankAccountUsage).incrementUsageCount(BANK_ACCOUNT_ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe enviar ACK aunque falle el procesamiento")
    void testHandleBankAccountEvent_WhenProcessingFails_StillSendsAck() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Error interno")).when(bankAccountUsage)
                .incrementUsageCount(BANK_ACCOUNT_ID, ENTERPRISE_ID);

        // Act
        bankAccountUsageListener.handleBankAccountEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Debe manejar excepción en basicAck sin propagar")
    void testHandleBankAccountEvent_WhenAckFails_DoesNotThrowException() throws Exception {
        // Arrange
        doNothing().when(bankAccountUsage).incrementUsageCount(BANK_ACCOUNT_ID, ENTERPRISE_ID);
        doThrow(new RuntimeException("Error en ACK")).when(channel).basicAck(DELIVERY_TAG, false);

        // Act & Assert
        bankAccountUsageListener.handleBankAccountEvent(validEvent, message, channel, DELIVERY_TAG);
        verify(bankAccountUsage).incrementUsageCount(BANK_ACCOUNT_ID, ENTERPRISE_ID);
    }

    // ==================== Tests de validación de parámetros de RabbitMQ ====================

    @Test
    @DisplayName("Debe invocar basicAck con múltiple = false")
    void testHandleBankAccountEvent_UsesBasicAckWithMultipleFalse() throws Exception {
        // Arrange
        doNothing().when(bankAccountUsage).incrementUsageCount(BANK_ACCOUNT_ID, ENTERPRISE_ID);

        // Act
        bankAccountUsageListener.handleBankAccountEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Debe usar deliveryTag correcto para ACK")
    void testHandleBankAccountEvent_UsesCorrectDeliveryTag() throws Exception {
        // Arrange
        long customDeliveryTag = 999L;
        doNothing().when(bankAccountUsage).incrementUsageCount(BANK_ACCOUNT_ID, ENTERPRISE_ID);

        // Act
        bankAccountUsageListener.handleBankAccountEvent(validEvent, message, channel, customDeliveryTag);

        // Assert
        verify(channel).basicAck(customDeliveryTag, false);
    }

    @Test
    @DisplayName("Debe invocar incrementUsageCount solo una vez por evento")
    void testHandleBankAccountEvent_InvokesIncrementUsageCountOncePerEvent() throws Exception {
        // Arrange
        doNothing().when(bankAccountUsage).incrementUsageCount(BANK_ACCOUNT_ID, ENTERPRISE_ID);

        // Act
        bankAccountUsageListener.handleBankAccountEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(bankAccountUsage, times(1)).incrementUsageCount(BANK_ACCOUNT_ID, ENTERPRISE_ID);
    }

    // ==================== Tests de escenarios múltiples ====================

    @Test
    @DisplayName("Debe procesar múltiples eventos consecutivos")
    void testHandleBankAccountEvent_ProcessMultipleEvents_AllProcessedSuccessfully() throws Exception {
        // Arrange
        doNothing().when(bankAccountUsage).incrementUsageCount(any(), any());

        // Act
        bankAccountUsageListener.handleBankAccountEvent(validEvent, message, channel, DELIVERY_TAG);
        bankAccountUsageListener.handleBankAccountEvent(validEvent, message, channel, DELIVERY_TAG + 1);
        bankAccountUsageListener.handleBankAccountEvent(validEvent, message, channel, DELIVERY_TAG + 2);

        // Assert
        verify(bankAccountUsage, times(3)).incrementUsageCount(BANK_ACCOUNT_ID, ENTERPRISE_ID);
        verify(channel).basicAck(DELIVERY_TAG, false);
        verify(channel).basicAck(DELIVERY_TAG + 1, false);
        verify(channel).basicAck(DELIVERY_TAG + 2, false);
    }

    @Test
    @DisplayName("Debe procesar eventos con diferentes bankAccountIds")
    void testHandleBankAccountEvent_WithDifferentBankAccountIds_ProcessesCorrectly() throws Exception {
        // Arrange
        Long bankAccountId1 = 1L;
        Long bankAccountId2 = 2L;
        Long bankAccountId3 = 3L;

        BankAccountUsageDto data1 = new BankAccountUsageDto(bankAccountId1, ENTERPRISE_ID, QUANTITY_USED);
        BankAccountUsageDto data2 = new BankAccountUsageDto(bankAccountId2, ENTERPRISE_ID, QUANTITY_USED);
        BankAccountUsageDto data3 = new BankAccountUsageDto(bankAccountId3, ENTERPRISE_ID, QUANTITY_USED);

        EventDto<BankAccountUsageDto, EventUsageType> event1 = new EventDto<>(data1, EventUsageType.USED);
        EventDto<BankAccountUsageDto, EventUsageType> event2 = new EventDto<>(data2, EventUsageType.USED);
        EventDto<BankAccountUsageDto, EventUsageType> event3 = new EventDto<>(data3, EventUsageType.USED);

        doNothing().when(bankAccountUsage).incrementUsageCount(any(), any());

        // Act
        bankAccountUsageListener.handleBankAccountEvent(event1, message, channel, DELIVERY_TAG);
        bankAccountUsageListener.handleBankAccountEvent(event2, message, channel, DELIVERY_TAG + 1);
        bankAccountUsageListener.handleBankAccountEvent(event3, message, channel, DELIVERY_TAG + 2);

        // Assert
        verify(bankAccountUsage).incrementUsageCount(bankAccountId1, ENTERPRISE_ID);
        verify(bankAccountUsage).incrementUsageCount(bankAccountId2, ENTERPRISE_ID);
        verify(bankAccountUsage).incrementUsageCount(bankAccountId3, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe procesar eventos con diferentes enterpriseIds")
    void testHandleBankAccountEvent_WithDifferentEnterpriseIds_ProcessesCorrectly() throws Exception {
        // Arrange
        String enterpriseId1 = "ENT-001";
        String enterpriseId2 = "ENT-002";
        String enterpriseId3 = "ENT-003";

        BankAccountUsageDto data1 = new BankAccountUsageDto(BANK_ACCOUNT_ID, enterpriseId1, QUANTITY_USED);
        BankAccountUsageDto data2 = new BankAccountUsageDto(BANK_ACCOUNT_ID, enterpriseId2, QUANTITY_USED);
        BankAccountUsageDto data3 = new BankAccountUsageDto(BANK_ACCOUNT_ID, enterpriseId3, QUANTITY_USED);

        EventDto<BankAccountUsageDto, EventUsageType> event1 = new EventDto<>(data1, EventUsageType.USED);
        EventDto<BankAccountUsageDto, EventUsageType> event2 = new EventDto<>(data2, EventUsageType.USED);
        EventDto<BankAccountUsageDto, EventUsageType> event3 = new EventDto<>(data3, EventUsageType.USED);

        doNothing().when(bankAccountUsage).incrementUsageCount(any(), any());

        // Act
        bankAccountUsageListener.handleBankAccountEvent(event1, message, channel, DELIVERY_TAG);
        bankAccountUsageListener.handleBankAccountEvent(event2, message, channel, DELIVERY_TAG + 1);
        bankAccountUsageListener.handleBankAccountEvent(event3, message, channel, DELIVERY_TAG + 2);

        // Assert
        verify(bankAccountUsage).incrementUsageCount(BANK_ACCOUNT_ID, enterpriseId1);
        verify(bankAccountUsage).incrementUsageCount(BANK_ACCOUNT_ID, enterpriseId2);
        verify(bankAccountUsage).incrementUsageCount(BANK_ACCOUNT_ID, enterpriseId3);
    }

    @Test
    @DisplayName("Debe procesar evento con quantityUsed = 1")
    void testHandleBankAccountEvent_WithQuantityUsedOne_ProcessesSuccessfully() throws Exception {
        // Arrange
        validUsageDto.setQuantityUsed(1);
        doNothing().when(bankAccountUsage).incrementUsageCount(BANK_ACCOUNT_ID, ENTERPRISE_ID);

        // Act
        bankAccountUsageListener.handleBankAccountEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(bankAccountUsage).incrementUsageCount(BANK_ACCOUNT_ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe procesar evento con quantityUsed grande")
    void testHandleBankAccountEvent_WithLargeQuantityUsed_ProcessesSuccessfully() throws Exception {
        // Arrange
        validUsageDto.setQuantityUsed(1000000);
        doNothing().when(bankAccountUsage).incrementUsageCount(BANK_ACCOUNT_ID, ENTERPRISE_ID);

        // Act
        bankAccountUsageListener.handleBankAccountEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(bankAccountUsage).incrementUsageCount(BANK_ACCOUNT_ID, ENTERPRISE_ID);
    }
}

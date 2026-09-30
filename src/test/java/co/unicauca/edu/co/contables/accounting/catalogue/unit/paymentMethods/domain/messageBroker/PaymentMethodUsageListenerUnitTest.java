package co.unicauca.edu.co.contables.accounting.catalogue.unit.paymentMethods.domain.messageBroker;

import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.services.IPaymentMethodUsage;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.messageBroker.PaymentMethodUsageListener;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.messageBroker.dto.PaymentMethodUsageDto;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.messageBroker.dto.EventDto;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.messageBroker.enums.EventUsageType;
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
class PaymentMethodUsageListenerUnitTest {

    @Mock
    private IPaymentMethodUsage paymentMethodUsagePort;

    @Mock
    private Channel channel;

    @Mock
    private Message message;

    @InjectMocks
    private PaymentMethodUsageListener paymentMethodUsageListener;

    private EventDto<PaymentMethodUsageDto, EventUsageType> validEvent;
    private PaymentMethodUsageDto validUsageDto;

    private static final Long PAYMENT_METHOD_ID = 1L;
    private static final String ENTERPRISE_ID = "ENT-001";
    private static final Integer QUANTITY_USED = 1;
    private static final long DELIVERY_TAG = 123L;

    @BeforeEach
    void setUp() {
        validUsageDto = new PaymentMethodUsageDto();
        validUsageDto.setPaymentMethodId(PAYMENT_METHOD_ID);
        validUsageDto.setEnterpriseId(ENTERPRISE_ID);
        validUsageDto.setQuantityUsed(QUANTITY_USED);

        validEvent = new EventDto<>();
        validEvent.setData(validUsageDto);
        validEvent.setType(EventUsageType.USED);
    }

    @Test
    @DisplayName("Debe procesar evento válido correctamente")
    void testHandlePaymentMethodEvent_WithValidEvent_ProcessesSuccessfully() throws Exception {
        // Arrange
        doNothing().when(paymentMethodUsagePort).incrementUsageCount(PAYMENT_METHOD_ID, ENTERPRISE_ID);

        // Act
        paymentMethodUsageListener.handlePaymentMethodEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(paymentMethodUsagePort).incrementUsageCount(PAYMENT_METHOD_ID, ENTERPRISE_ID);
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Debe enviar ACK después de procesar evento válido")
    void testHandlePaymentMethodEvent_SendsAcknowledgment() throws Exception {
        // Arrange
        doNothing().when(paymentMethodUsagePort).incrementUsageCount(PAYMENT_METHOD_ID, ENTERPRISE_ID);

        // Act
        paymentMethodUsageListener.handlePaymentMethodEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Debe invocar incrementUsageCount con parámetros correctos")
    void testHandlePaymentMethodEvent_InvokesIncrementUsageCountWithCorrectParameters() throws Exception {
        // Arrange
        doNothing().when(paymentMethodUsagePort).incrementUsageCount(PAYMENT_METHOD_ID, ENTERPRISE_ID);

        // Act
        paymentMethodUsageListener.handlePaymentMethodEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(paymentMethodUsagePort).incrementUsageCount(PAYMENT_METHOD_ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe rechazar evento nulo")
    void testIsValidEvent_WithNullEvent_DoesNotProcessEvent() throws Exception {
        // Act
        paymentMethodUsageListener.handlePaymentMethodEvent(null, message, channel, DELIVERY_TAG);

        // Assert
        verify(paymentMethodUsagePort, never()).incrementUsageCount(any(), any());
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Debe rechazar evento con tipo nulo")
    void testIsValidEvent_WithNullType_DoesNotProcessEvent() throws Exception {
        // Arrange
        validEvent.setType(null);

        // Act
        paymentMethodUsageListener.handlePaymentMethodEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(paymentMethodUsagePort, never()).incrementUsageCount(any(), any());
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Debe rechazar evento con data nulo")
    void testIsValidEvent_WithNullData_DoesNotProcessEvent() throws Exception {
        // Arrange
        validEvent.setData(null);

        // Act
        paymentMethodUsageListener.handlePaymentMethodEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(paymentMethodUsagePort, never()).incrementUsageCount(any(), any());
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Debe rechazar evento con paymentMethodId nulo")
    void testIsValidEvent_WithNullPaymentMethodId_DoesNotProcessEvent() throws Exception {
        // Arrange
        validUsageDto.setPaymentMethodId(null);

        // Act
        paymentMethodUsageListener.handlePaymentMethodEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(paymentMethodUsagePort, never()).incrementUsageCount(any(), any());
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Debe rechazar evento con enterpriseId nulo")
    void testIsValidEvent_WithNullEnterpriseId_DoesNotProcessEvent() throws Exception {
        // Arrange
        validUsageDto.setEnterpriseId(null);

        // Act
        paymentMethodUsageListener.handlePaymentMethodEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(paymentMethodUsagePort, never()).incrementUsageCount(any(), any());
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Debe rechazar evento con enterpriseId vacío")
    void testIsValidEvent_WithEmptyEnterpriseId_DoesNotProcessEvent() throws Exception {
        // Arrange
        validUsageDto.setEnterpriseId("");

        // Act
        paymentMethodUsageListener.handlePaymentMethodEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(paymentMethodUsagePort, never()).incrementUsageCount(any(), any());
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Debe rechazar evento con enterpriseId en blanco")
    void testIsValidEvent_WithBlankEnterpriseId_DoesNotProcessEvent() throws Exception {
        // Arrange
        validUsageDto.setEnterpriseId("   ");

        // Act
        paymentMethodUsageListener.handlePaymentMethodEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(paymentMethodUsagePort, never()).incrementUsageCount(any(), any());
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Debe rechazar evento con quantityUsed nulo")
    void testIsValidEvent_WithNullQuantityUsed_DoesNotProcessEvent() throws Exception {
        // Arrange
        validUsageDto.setQuantityUsed(null);

        // Act
        paymentMethodUsageListener.handlePaymentMethodEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(paymentMethodUsagePort, never()).incrementUsageCount(any(), any());
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Debe rechazar evento con quantityUsed cero")
    void testIsValidEvent_WithZeroQuantityUsed_DoesNotProcessEvent() throws Exception {
        // Arrange
        validUsageDto.setQuantityUsed(0);

        // Act
        paymentMethodUsageListener.handlePaymentMethodEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(paymentMethodUsagePort, never()).incrementUsageCount(any(), any());
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Debe rechazar evento con quantityUsed negativo")
    void testIsValidEvent_WithNegativeQuantityUsed_DoesNotProcessEvent() throws Exception {
        // Arrange
        validUsageDto.setQuantityUsed(-1);

        // Act
        paymentMethodUsageListener.handlePaymentMethodEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(paymentMethodUsagePort, never()).incrementUsageCount(any(), any());
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Debe aceptar evento con quantityUsed positivo")
    void testIsValidEvent_WithPositiveQuantityUsed_ProcessesEvent() throws Exception {
        // Arrange
        validUsageDto.setQuantityUsed(10);
        doNothing().when(paymentMethodUsagePort).incrementUsageCount(PAYMENT_METHOD_ID, ENTERPRISE_ID);

        // Act
        paymentMethodUsageListener.handlePaymentMethodEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(paymentMethodUsagePort).incrementUsageCount(PAYMENT_METHOD_ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe aceptar evento válido completo")
    void testIsValidEvent_WithValidEvent_ProcessesEvent() throws Exception {
        // Arrange
        doNothing().when(paymentMethodUsagePort).incrementUsageCount(PAYMENT_METHOD_ID, ENTERPRISE_ID);

        // Act
        paymentMethodUsageListener.handlePaymentMethodEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(paymentMethodUsagePort).incrementUsageCount(PAYMENT_METHOD_ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe procesar evento tipo USED")
    void testHandlePaymentMethodEvent_WithUsedType_ProcessesSuccessfully() throws Exception {
        // Arrange
        validEvent.setType(EventUsageType.USED);
        doNothing().when(paymentMethodUsagePort).incrementUsageCount(PAYMENT_METHOD_ID, ENTERPRISE_ID);

        // Act
        paymentMethodUsageListener.handlePaymentMethodEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(paymentMethodUsagePort).incrementUsageCount(PAYMENT_METHOD_ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe manejar excepción en processEvent y enviar ACK")
    void testHandlePaymentMethodEvent_WhenExceptionOccurs_SendsAck() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Error al incrementar uso")).when(paymentMethodUsagePort)
                .incrementUsageCount(PAYMENT_METHOD_ID, ENTERPRISE_ID);

        // Act
        paymentMethodUsageListener.handlePaymentMethodEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Debe continuar procesamiento después de excepción")
    void testHandlePaymentMethodEvent_WhenExceptionOccurs_DoesNotThrowException() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Error al incrementar uso")).when(paymentMethodUsagePort)
                .incrementUsageCount(PAYMENT_METHOD_ID, ENTERPRISE_ID);

        // Act & Assert
        paymentMethodUsageListener.handlePaymentMethodEvent(validEvent, message, channel, DELIVERY_TAG);
        verify(paymentMethodUsagePort).incrementUsageCount(PAYMENT_METHOD_ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe enviar ACK aunque falle el procesamiento")
    void testHandlePaymentMethodEvent_WhenProcessingFails_StillSendsAck() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Error interno")).when(paymentMethodUsagePort)
                .incrementUsageCount(PAYMENT_METHOD_ID, ENTERPRISE_ID);

        // Act
        paymentMethodUsageListener.handlePaymentMethodEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Debe manejar excepción en basicAck sin propagar")
    void testHandlePaymentMethodEvent_WhenAckFails_DoesNotThrowException() throws Exception {
        // Arrange
        doNothing().when(paymentMethodUsagePort).incrementUsageCount(PAYMENT_METHOD_ID, ENTERPRISE_ID);
        doThrow(new RuntimeException("Error en ACK")).when(channel).basicAck(DELIVERY_TAG, false);

        // Act & Assert
        paymentMethodUsageListener.handlePaymentMethodEvent(validEvent, message, channel, DELIVERY_TAG);
        verify(paymentMethodUsagePort).incrementUsageCount(PAYMENT_METHOD_ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe invocar basicAck con múltiple = false")
    void testHandlePaymentMethodEvent_UsesBasicAckWithMultipleFalse() throws Exception {
        // Arrange
        doNothing().when(paymentMethodUsagePort).incrementUsageCount(PAYMENT_METHOD_ID, ENTERPRISE_ID);

        // Act
        paymentMethodUsageListener.handlePaymentMethodEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Debe usar deliveryTag correcto para ACK")
    void testHandlePaymentMethodEvent_UsesCorrectDeliveryTag() throws Exception {
        // Arrange
        long customDeliveryTag = 999L;
        doNothing().when(paymentMethodUsagePort).incrementUsageCount(PAYMENT_METHOD_ID, ENTERPRISE_ID);

        // Act
        paymentMethodUsageListener.handlePaymentMethodEvent(validEvent, message, channel, customDeliveryTag);

        // Assert
        verify(channel).basicAck(customDeliveryTag, false);
    }

    @Test
    @DisplayName("Debe invocar incrementUsageCount solo una vez por evento")
    void testHandlePaymentMethodEvent_InvokesIncrementUsageCountOncePerEvent() throws Exception {
        // Arrange
        doNothing().when(paymentMethodUsagePort).incrementUsageCount(PAYMENT_METHOD_ID, ENTERPRISE_ID);

        // Act
        paymentMethodUsageListener.handlePaymentMethodEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(paymentMethodUsagePort, times(1)).incrementUsageCount(PAYMENT_METHOD_ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe procesar múltiples eventos consecutivos")
    void testHandlePaymentMethodEvent_ProcessMultipleEvents_AllProcessedSuccessfully() throws Exception {
        // Arrange
        doNothing().when(paymentMethodUsagePort).incrementUsageCount(any(), any());

        // Act
        paymentMethodUsageListener.handlePaymentMethodEvent(validEvent, message, channel, DELIVERY_TAG);
        paymentMethodUsageListener.handlePaymentMethodEvent(validEvent, message, channel, DELIVERY_TAG + 1);
        paymentMethodUsageListener.handlePaymentMethodEvent(validEvent, message, channel, DELIVERY_TAG + 2);

        // Assert
        verify(paymentMethodUsagePort, times(3)).incrementUsageCount(PAYMENT_METHOD_ID, ENTERPRISE_ID);
        verify(channel).basicAck(DELIVERY_TAG, false);
        verify(channel).basicAck(DELIVERY_TAG + 1, false);
        verify(channel).basicAck(DELIVERY_TAG + 2, false);
    }

    @Test
    @DisplayName("Debe procesar eventos con diferentes paymentMethodIds")
    void testHandlePaymentMethodEvent_WithDifferentPaymentMethodIds_ProcessesCorrectly() throws Exception {
        // Arrange
        Long paymentMethodId1 = 1L;
        Long paymentMethodId2 = 2L;
        Long paymentMethodId3 = 3L;

        PaymentMethodUsageDto data1 = new PaymentMethodUsageDto(paymentMethodId1, ENTERPRISE_ID, QUANTITY_USED);
        PaymentMethodUsageDto data2 = new PaymentMethodUsageDto(paymentMethodId2, ENTERPRISE_ID, QUANTITY_USED);
        PaymentMethodUsageDto data3 = new PaymentMethodUsageDto(paymentMethodId3, ENTERPRISE_ID, QUANTITY_USED);

        EventDto<PaymentMethodUsageDto, EventUsageType> event1 = new EventDto<>(data1, EventUsageType.USED);
        EventDto<PaymentMethodUsageDto, EventUsageType> event2 = new EventDto<>(data2, EventUsageType.USED);
        EventDto<PaymentMethodUsageDto, EventUsageType> event3 = new EventDto<>(data3, EventUsageType.USED);

        doNothing().when(paymentMethodUsagePort).incrementUsageCount(any(), any());

        // Act
        paymentMethodUsageListener.handlePaymentMethodEvent(event1, message, channel, DELIVERY_TAG);
        paymentMethodUsageListener.handlePaymentMethodEvent(event2, message, channel, DELIVERY_TAG + 1);
        paymentMethodUsageListener.handlePaymentMethodEvent(event3, message, channel, DELIVERY_TAG + 2);

        // Assert
        verify(paymentMethodUsagePort).incrementUsageCount(paymentMethodId1, ENTERPRISE_ID);
        verify(paymentMethodUsagePort).incrementUsageCount(paymentMethodId2, ENTERPRISE_ID);
        verify(paymentMethodUsagePort).incrementUsageCount(paymentMethodId3, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe procesar eventos con diferentes enterpriseIds")
    void testHandlePaymentMethodEvent_WithDifferentEnterpriseIds_ProcessesCorrectly() throws Exception {
        // Arrange
        String enterpriseId1 = "ENT-001";
        String enterpriseId2 = "ENT-002";
        String enterpriseId3 = "ENT-003";

        PaymentMethodUsageDto data1 = new PaymentMethodUsageDto(PAYMENT_METHOD_ID, enterpriseId1, QUANTITY_USED);
        PaymentMethodUsageDto data2 = new PaymentMethodUsageDto(PAYMENT_METHOD_ID, enterpriseId2, QUANTITY_USED);
        PaymentMethodUsageDto data3 = new PaymentMethodUsageDto(PAYMENT_METHOD_ID, enterpriseId3, QUANTITY_USED);

        EventDto<PaymentMethodUsageDto, EventUsageType> event1 = new EventDto<>(data1, EventUsageType.USED);
        EventDto<PaymentMethodUsageDto, EventUsageType> event2 = new EventDto<>(data2, EventUsageType.USED);
        EventDto<PaymentMethodUsageDto, EventUsageType> event3 = new EventDto<>(data3, EventUsageType.USED);

        doNothing().when(paymentMethodUsagePort).incrementUsageCount(any(), any());

        // Act
        paymentMethodUsageListener.handlePaymentMethodEvent(event1, message, channel, DELIVERY_TAG);
        paymentMethodUsageListener.handlePaymentMethodEvent(event2, message, channel, DELIVERY_TAG + 1);
        paymentMethodUsageListener.handlePaymentMethodEvent(event3, message, channel, DELIVERY_TAG + 2);

        // Assert
        verify(paymentMethodUsagePort).incrementUsageCount(PAYMENT_METHOD_ID, enterpriseId1);
        verify(paymentMethodUsagePort).incrementUsageCount(PAYMENT_METHOD_ID, enterpriseId2);
        verify(paymentMethodUsagePort).incrementUsageCount(PAYMENT_METHOD_ID, enterpriseId3);
    }

    @Test
    @DisplayName("Debe procesar evento con quantityUsed = 1")
    void testHandlePaymentMethodEvent_WithQuantityUsedOne_ProcessesSuccessfully() throws Exception {
        // Arrange
        validUsageDto.setQuantityUsed(1);
        doNothing().when(paymentMethodUsagePort).incrementUsageCount(PAYMENT_METHOD_ID, ENTERPRISE_ID);

        // Act
        paymentMethodUsageListener.handlePaymentMethodEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(paymentMethodUsagePort).incrementUsageCount(PAYMENT_METHOD_ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe procesar evento con quantityUsed grande")
    void testHandlePaymentMethodEvent_WithLargeQuantityUsed_ProcessesSuccessfully() throws Exception {
        // Arrange
        validUsageDto.setQuantityUsed(1000000);
        doNothing().when(paymentMethodUsagePort).incrementUsageCount(PAYMENT_METHOD_ID, ENTERPRISE_ID);

        // Act
        paymentMethodUsageListener.handlePaymentMethodEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(paymentMethodUsagePort).incrementUsageCount(PAYMENT_METHOD_ID, ENTERPRISE_ID);
    }
}

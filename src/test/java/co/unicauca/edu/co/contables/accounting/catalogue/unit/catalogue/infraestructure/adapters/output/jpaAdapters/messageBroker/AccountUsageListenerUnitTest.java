package co.unicauca.edu.co.contables.accounting.catalogue.unit.catalogue.infraestructure.adapters.output.jpaAdapters.messageBroker;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.input.IAccountCatalogueUsagePort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.messageBroker.AccountUsageListener;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.messageBroker.dto.AccountUsedEventDto;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.messageBroker.dto.EventDto;
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

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccountUsageListenerUnitTest {

    @Mock
    private IAccountCatalogueUsagePort accountCatalogueUsagePort;

    @Mock
    private Channel channel;

    @Mock
    private Message message;

    @InjectMocks
    private AccountUsageListener listener;

    private EventDto<AccountUsedEventDto> validEvent;
    private AccountUsedEventDto validEventData;
    private static final Long ACCOUNT_ID = 1L;
    private static final String ENTERPRISE_ID = "ENT001";
    private static final long DELIVERY_TAG = 123L;

    @BeforeEach
    void setUp() {
        validEventData = new AccountUsedEventDto();
        validEventData.setAccount(ACCOUNT_ID);
        validEventData.setEnterpriseId(ENTERPRISE_ID);
        validEventData.setSourceAccountType("ID");

        validEvent = new EventDto<>("ACCOUNT_USED", validEventData);
    }

    // ========== Tests de handleAccountUsageEvent ==========

    @Test
    @DisplayName("Debe procesar evento válido correctamente")
    void testHandleAccountUsageEventProcessesValidEvent() throws IOException {
        // Arrange
        doNothing().when(accountCatalogueUsagePort).incrementUsageCount(validEventData);
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act
        listener.handleAccountUsageEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(accountCatalogueUsagePort).incrementUsageCount(validEventData);
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Debe hacer acknowledge del mensaje después de procesar")
    void testHandleAccountUsageEventAcknowledgesMessage() throws IOException {
        // Arrange
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act
        listener.handleAccountUsageEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    // ========== Tests de isValidEvent - Evento null ==========

    @Test
    @DisplayName("Debe retornar false cuando evento es null")
    void testIsValidEventReturnsFalseWhenEventIsNull() throws IOException {
        // Arrange
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act
        listener.handleAccountUsageEvent(null, message, channel, DELIVERY_TAG);

        // Assert
        verify(accountCatalogueUsagePort, never()).incrementUsageCount(any());
    }

    // ========== Tests de isValidEvent - Data null ==========

    @Test
    @DisplayName("Debe retornar false cuando data del evento es null")
    void testIsValidEventReturnsFalseWhenDataIsNull() throws IOException {
        // Arrange
        EventDto<AccountUsedEventDto> eventWithNullData = new EventDto<>("ACCOUNT_USED", null);
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act
        listener.handleAccountUsageEvent(eventWithNullData, message, channel, DELIVERY_TAG);

        // Assert
        verify(accountCatalogueUsagePort, never()).incrementUsageCount(any());
    }

    // ========== Tests de isValidEvent - Account null ==========

    @Test
    @DisplayName("Debe retornar false cuando account es null")
    void testIsValidEventReturnsFalseWhenAccountIsNull() throws IOException {
        // Arrange
        validEventData.setAccount(null);
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act
        listener.handleAccountUsageEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(accountCatalogueUsagePort, never()).incrementUsageCount(any());
    }

    // ========== Tests de isValidEvent - EnterpriseId null o vacío ==========

    @Test
    @DisplayName("Debe retornar false cuando enterpriseId es null")
    void testIsValidEventReturnsFalseWhenEnterpriseIdIsNull() throws IOException {
        // Arrange
        validEventData.setEnterpriseId(null);
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act
        listener.handleAccountUsageEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(accountCatalogueUsagePort, never()).incrementUsageCount(any());
    }

    @Test
    @DisplayName("Debe retornar false cuando enterpriseId es vacío")
    void testIsValidEventReturnsFalseWhenEnterpriseIdIsEmpty() throws IOException {
        // Arrange
        validEventData.setEnterpriseId("");
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act
        listener.handleAccountUsageEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(accountCatalogueUsagePort, never()).incrementUsageCount(any());
    }

    @Test
    @DisplayName("Debe retornar false cuando enterpriseId es solo espacios")
    void testIsValidEventReturnsFalseWhenEnterpriseIdIsBlank() throws IOException {
        // Arrange
        validEventData.setEnterpriseId("   ");
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act
        listener.handleAccountUsageEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(accountCatalogueUsagePort, never()).incrementUsageCount(any());
    }

    // ========== Tests de isValidEvent - SourceAccountType null o vacío ==========

    @Test
    @DisplayName("Debe retornar false cuando sourceAccountType es null")
    void testIsValidEventReturnsFalseWhenSourceAccountTypeIsNull() throws IOException {
        // Arrange
        validEventData.setSourceAccountType(null);
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act
        listener.handleAccountUsageEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(accountCatalogueUsagePort, never()).incrementUsageCount(any());
    }

    @Test
    @DisplayName("Debe retornar false cuando sourceAccountType es vacío")
    void testIsValidEventReturnsFalseWhenSourceAccountTypeIsEmpty() throws IOException {
        // Arrange
        validEventData.setSourceAccountType("");
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act
        listener.handleAccountUsageEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(accountCatalogueUsagePort, never()).incrementUsageCount(any());
    }

    @Test
    @DisplayName("Debe retornar false cuando sourceAccountType es solo espacios")
    void testIsValidEventReturnsFalseWhenSourceAccountTypeIsBlank() throws IOException {
        // Arrange
        validEventData.setSourceAccountType("   ");
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act
        listener.handleAccountUsageEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(accountCatalogueUsagePort, never()).incrementUsageCount(any());
    }

    // ========== Tests de isValidEvent - SourceAccountType inválido ==========

    @Test
    @DisplayName("Debe retornar false cuando sourceAccountType es inválido")
    void testIsValidEventReturnsFalseWhenSourceAccountTypeIsInvalid() throws IOException {
        // Arrange
        validEventData.setSourceAccountType("INVALID");
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act
        listener.handleAccountUsageEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(accountCatalogueUsagePort, never()).incrementUsageCount(any());
    }

    @Test
    @DisplayName("Debe retornar false cuando sourceAccountType es NUMBER")
    void testIsValidEventReturnsFalseWhenSourceAccountTypeIsNumber() throws IOException {
        // Arrange
        validEventData.setSourceAccountType("NUMBER");
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act
        listener.handleAccountUsageEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(accountCatalogueUsagePort, never()).incrementUsageCount(any());
    }

    // ========== Tests de isValidEvent - SourceAccountType válidos ==========

    @Test
    @DisplayName("Debe aceptar sourceAccountType ID en mayúsculas")
    void testIsValidEventAcceptsIdUppercase() throws IOException {
        // Arrange
        validEventData.setSourceAccountType("ID");
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act
        listener.handleAccountUsageEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(accountCatalogueUsagePort).incrementUsageCount(validEventData);
    }

    @Test
    @DisplayName("Debe aceptar sourceAccountType id en minúsculas")
    void testIsValidEventAcceptsIdLowercase() throws IOException {
        // Arrange
        validEventData.setSourceAccountType("id");
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act
        listener.handleAccountUsageEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(accountCatalogueUsagePort).incrementUsageCount(validEventData);
    }

    @Test
    @DisplayName("Debe aceptar sourceAccountType Id mixto")
    void testIsValidEventAcceptsIdMixedCase() throws IOException {
        // Arrange
        validEventData.setSourceAccountType("Id");
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act
        listener.handleAccountUsageEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(accountCatalogueUsagePort).incrementUsageCount(validEventData);
    }

    @Test
    @DisplayName("Debe aceptar sourceAccountType CODE en mayúsculas")
    void testIsValidEventAcceptsCodeUppercase() throws IOException {
        // Arrange
        validEventData.setSourceAccountType("CODE");
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act
        listener.handleAccountUsageEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(accountCatalogueUsagePort).incrementUsageCount(validEventData);
    }

    @Test
    @DisplayName("Debe aceptar sourceAccountType code en minúsculas")
    void testIsValidEventAcceptsCodeLowercase() throws IOException {
        // Arrange
        validEventData.setSourceAccountType("code");
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act
        listener.handleAccountUsageEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(accountCatalogueUsagePort).incrementUsageCount(validEventData);
    }

    @Test
    @DisplayName("Debe aceptar sourceAccountType Code mixto")
    void testIsValidEventAcceptsCodeMixedCase() throws IOException {
        // Arrange
        validEventData.setSourceAccountType("Code");
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act
        listener.handleAccountUsageEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(accountCatalogueUsagePort).incrementUsageCount(validEventData);
    }

    // ========== Tests de processEvent - Manejo de errores ==========

    @Test
    @DisplayName("Debe continuar sin lanzar excepción cuando el servicio falla")
    void testProcessEventContinuesWhenServiceFails() throws IOException {
        // Arrange
        doThrow(new RuntimeException("Error de servicio")).when(accountCatalogueUsagePort).incrementUsageCount(any());
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act & Assert
        assertDoesNotThrow(() -> listener.handleAccountUsageEvent(validEvent, message, channel, DELIVERY_TAG));
    }

    @Test
    @DisplayName("Debe hacer acknowledge incluso cuando el servicio falla")
    void testAcknowledgesEvenWhenServiceFails() throws IOException {
        // Arrange
        doThrow(new RuntimeException("Error")).when(accountCatalogueUsagePort).incrementUsageCount(any());
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act
        listener.handleAccountUsageEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    // ========== Tests de getEntityType ==========

    @Test
    @DisplayName("Debe procesar evento con tipo de entidad AccountUsage")
    void testGetEntityTypeReturnsAccountUsage() throws IOException {
        // Arrange
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act
        listener.handleAccountUsageEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(accountCatalogueUsagePort).incrementUsageCount(validEventData);
    }

    // ========== Tests de extractEventType ==========

    @Test
    @DisplayName("Debe procesar evento con tipo ACCOUNT_USED")
    void testExtractEventTypeFromEvent() throws IOException {
        // Arrange
        EventDto<AccountUsedEventDto> eventWithType = new EventDto<>("ACCOUNT_USED", validEventData);
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act
        listener.handleAccountUsageEvent(eventWithType, message, channel, DELIVERY_TAG);

        // Assert
        verify(accountCatalogueUsagePort).incrementUsageCount(validEventData);
    }

    // ========== Tests de valores de account ==========

    @Test
    @DisplayName("Debe procesar evento con account igual a 0")
    void testProcessEventWithZeroAccount() throws IOException {
        // Arrange
        validEventData.setAccount(0L);
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act
        listener.handleAccountUsageEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(accountCatalogueUsagePort).incrementUsageCount(validEventData);
    }

    @Test
    @DisplayName("Debe procesar evento con account de valor grande")
    void testProcessEventWithLargeAccount() throws IOException {
        // Arrange
        validEventData.setAccount(9999999999L);
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act
        listener.handleAccountUsageEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(accountCatalogueUsagePort).incrementUsageCount(validEventData);
    }

    @Test
    @DisplayName("Debe procesar evento con account negativo")
    void testProcessEventWithNegativeAccount() throws IOException {
        // Arrange
        validEventData.setAccount(-1L);
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act
        listener.handleAccountUsageEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(accountCatalogueUsagePort).incrementUsageCount(validEventData);
    }

    // ========== Tests de diferentes deliveryTag ==========

    @Test
    @DisplayName("Debe usar el deliveryTag correcto para acknowledge")
    void testUsesCorrectDeliveryTag() throws IOException {
        // Arrange
        long customDeliveryTag = 999L;
        doNothing().when(channel).basicAck(customDeliveryTag, false);

        // Act
        listener.handleAccountUsageEvent(validEvent, message, channel, customDeliveryTag);

        // Assert
        verify(channel).basicAck(customDeliveryTag, false);
    }

    // ========== Tests de flujo completo ==========

    @Test
    @DisplayName("Debe ejecutar flujo completo con evento válido tipo ID")
    void testFullFlowWithValidIdEvent() throws IOException {
        // Arrange
        validEventData.setAccount(12345L);
        validEventData.setEnterpriseId("ENTERPRISE123");
        validEventData.setSourceAccountType("ID");
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act
        listener.handleAccountUsageEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(accountCatalogueUsagePort).incrementUsageCount(argThat(data ->
                data.getAccount().equals(12345L) &&
                data.getEnterpriseId().equals("ENTERPRISE123") &&
                data.getSourceAccountType().equals("ID")
        ));
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Debe ejecutar flujo completo con evento válido tipo CODE")
    void testFullFlowWithValidCodeEvent() throws IOException {
        // Arrange
        validEventData.setAccount(11050101L);
        validEventData.setEnterpriseId("ENT999");
        validEventData.setSourceAccountType("CODE");
        doNothing().when(channel).basicAck(DELIVERY_TAG, false);

        // Act
        listener.handleAccountUsageEvent(validEvent, message, channel, DELIVERY_TAG);

        // Assert
        verify(accountCatalogueUsagePort).incrementUsageCount(argThat(data ->
                data.getAccount().equals(11050101L) &&
                data.getEnterpriseId().equals("ENT999") &&
                data.getSourceAccountType().equals("CODE")
        ));
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    // ========== Tests de múltiples eventos ==========

    @Test
    @DisplayName("Debe procesar múltiples eventos consecutivos")
    void testProcessMultipleConsecutiveEvents() throws IOException {
        // Arrange
        doNothing().when(channel).basicAck(anyLong(), eq(false));

        AccountUsedEventDto data1 = new AccountUsedEventDto(1L, "ENT001", "ID");
        AccountUsedEventDto data2 = new AccountUsedEventDto(2L, "ENT002", "CODE");
        AccountUsedEventDto data3 = new AccountUsedEventDto(3L, "ENT003", "ID");

        EventDto<AccountUsedEventDto> event1 = new EventDto<>("ACCOUNT_USED", data1);
        EventDto<AccountUsedEventDto> event2 = new EventDto<>("ACCOUNT_USED", data2);
        EventDto<AccountUsedEventDto> event3 = new EventDto<>("ACCOUNT_USED", data3);

        // Act
        listener.handleAccountUsageEvent(event1, message, channel, 1L);
        listener.handleAccountUsageEvent(event2, message, channel, 2L);
        listener.handleAccountUsageEvent(event3, message, channel, 3L);

        // Assert
        verify(accountCatalogueUsagePort, times(3)).incrementUsageCount(any());
        verify(channel, times(3)).basicAck(anyLong(), eq(false));
    }

    // ========== Tests de error en acknowledge ==========

    @Test
    @DisplayName("Debe continuar sin excepción cuando acknowledge falla")
    void testContinuesWhenAcknowledgeFails() throws IOException {
        // Arrange
        doThrow(new IOException("Channel error")).when(channel).basicAck(DELIVERY_TAG, false);

        // Act & Assert
        assertDoesNotThrow(() -> listener.handleAccountUsageEvent(validEvent, message, channel, DELIVERY_TAG));
    }
}

package co.unicauca.edu.co.contables.accounting.catalogue.unit.catalogue.infraestructure.adapters.output.jpaAdapters.messageBroker;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.input.IAccountCatalogueUsagePort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.messageBroker.SpringEventsAccountUsageListener;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.messageBroker.dto.AccountUsedEventDto;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.messageBroker.dto.EventDto;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SpringEventsAccountUsageListenerTest {

    @Mock
    private IAccountCatalogueUsagePort accountCatalogueUsagePort;

    @InjectMocks
    private SpringEventsAccountUsageListener listener;

    private EventDto<AccountUsedEventDto> validEvent;
    private AccountUsedEventDto validEventData;
    private static final Long ACCOUNT_ID = 1L;
    private static final String ENTERPRISE_ID = "ENT001";

    @BeforeEach
    void setUp() {
        validEventData = new AccountUsedEventDto();
        validEventData.setAccount(ACCOUNT_ID);
        validEventData.setEnterpriseId(ENTERPRISE_ID);
        validEventData.setSourceAccountType("ID");

        validEvent = new EventDto<>("ACCOUNT_USED", validEventData);
    }

    @Test
    @DisplayName("Debe procesar evento válido correctamente")
    void testHandleAccountUsageEventProcessesValidEvent() throws IOException {

        doNothing().when(accountCatalogueUsagePort).incrementUsageCount(validEventData);

        listener.onAccountUsed(validEventData);

        verify(accountCatalogueUsagePort).incrementUsageCount(validEventData);
    }

    @Test
    @DisplayName("Debe retornar false cuando evento es null")
    void testIsValidEventReturnsFalseWhenEventIsNull() throws IOException {

        listener.onAccountUsed(null);

        verify(accountCatalogueUsagePort, never()).incrementUsageCount(any());
    }

    @Test
    @DisplayName("Debe retornar false cuando account es null")
    void testIsValidEventReturnsFalseWhenAccountIsNull() throws IOException {

        validEventData.setAccount(null);

        listener.onAccountUsed(validEventData);

        verify(accountCatalogueUsagePort, never()).incrementUsageCount(any());
    }

    @Test
    @DisplayName("Debe retornar false cuando enterpriseId es null")
    void testIsValidEventReturnsFalseWhenEnterpriseIdIsNull() throws IOException {

        validEventData.setEnterpriseId(null);

        listener.onAccountUsed(validEventData);

        verify(accountCatalogueUsagePort, never()).incrementUsageCount(any());
    }

    @Test
    @DisplayName("Debe retornar false cuando enterpriseId es vacío")
    void testIsValidEventReturnsFalseWhenEnterpriseIdIsEmpty() throws IOException {

        validEventData.setEnterpriseId("");

        listener.onAccountUsed(validEventData);

        verify(accountCatalogueUsagePort, never()).incrementUsageCount(any());
    }

    @Test
    @DisplayName("Debe retornar false cuando enterpriseId es solo espacios")
    void testIsValidEventReturnsFalseWhenEnterpriseIdIsBlank() throws IOException {

        validEventData.setEnterpriseId("   ");

        listener.onAccountUsed(validEventData);

        verify(accountCatalogueUsagePort, never()).incrementUsageCount(any());
    }

    @Test
    @DisplayName("Debe retornar false cuando sourceAccountType es null")
    void testIsValidEventReturnsFalseWhenSourceAccountTypeIsNull() throws IOException {

        validEventData.setSourceAccountType(null);

        listener.onAccountUsed(validEventData);

        verify(accountCatalogueUsagePort, never()).incrementUsageCount(any());
    }

    @Test
    @DisplayName("Debe retornar false cuando sourceAccountType es vacío")
    void testIsValidEventReturnsFalseWhenSourceAccountTypeIsEmpty() throws IOException {

        validEventData.setSourceAccountType("");

        listener.onAccountUsed(validEventData);

        verify(accountCatalogueUsagePort, never()).incrementUsageCount(any());
    }

    @Test
    @DisplayName("Debe retornar false cuando sourceAccountType es solo espacios")
    void testIsValidEventReturnsFalseWhenSourceAccountTypeIsBlank() throws IOException {

        validEventData.setSourceAccountType("   ");

        listener.onAccountUsed(validEventData);

        verify(accountCatalogueUsagePort, never()).incrementUsageCount(any());
    }

    @Test
    @DisplayName("Debe retornar false cuando sourceAccountType es inválido")
    void testIsValidEventReturnsFalseWhenSourceAccountTypeIsInvalid() throws IOException {

        validEventData.setSourceAccountType("INVALID");

        listener.onAccountUsed(validEventData);

        verify(accountCatalogueUsagePort, never()).incrementUsageCount(any());
    }

    @Test
    @DisplayName("Debe retornar false cuando sourceAccountType es NUMBER")
    void testIsValidEventReturnsFalseWhenSourceAccountTypeIsNumber() throws IOException {

        validEventData.setSourceAccountType("NUMBER");

        listener.onAccountUsed(validEventData);

        verify(accountCatalogueUsagePort, never()).incrementUsageCount(any());
    }

    @Test
    @DisplayName("Debe aceptar sourceAccountType ID en mayúsculas")
    void testIsValidEventAcceptsIdUppercase() throws IOException {

        validEventData.setSourceAccountType("ID");

        listener.onAccountUsed(validEventData);

        verify(accountCatalogueUsagePort).incrementUsageCount(validEventData);
    }

    @Test
    @DisplayName("Debe aceptar sourceAccountType id en minúsculas")
    void testIsValidEventAcceptsIdLowercase() throws IOException {

        validEventData.setSourceAccountType("id");

        listener.onAccountUsed(validEventData);

        verify(accountCatalogueUsagePort).incrementUsageCount(validEventData);
    }

    @Test
    @DisplayName("Debe aceptar sourceAccountType Id mixto")
    void testIsValidEventAcceptsIdMixedCase() throws IOException {

        validEventData.setSourceAccountType("Id");

        listener.onAccountUsed(validEventData);

        verify(accountCatalogueUsagePort).incrementUsageCount(validEventData);
    }

    @Test
    @DisplayName("Debe aceptar sourceAccountType CODE en mayúsculas")
    void testIsValidEventAcceptsCodeUppercase() throws IOException {

        validEventData.setSourceAccountType("CODE");

        listener.onAccountUsed(validEventData);

        verify(accountCatalogueUsagePort).incrementUsageCount(validEventData);
    }

    @Test
    @DisplayName("Debe aceptar sourceAccountType code en minúsculas")
    void testIsValidEventAcceptsCodeLowercase() throws IOException {

        validEventData.setSourceAccountType("code");

        listener.onAccountUsed(validEventData);

        verify(accountCatalogueUsagePort).incrementUsageCount(validEventData);
    }

    @Test
    @DisplayName("Debe aceptar sourceAccountType Code mixto")
    void testIsValidEventAcceptsCodeMixedCase() throws IOException {

        validEventData.setSourceAccountType("Code");

        listener.onAccountUsed(validEventData);

        verify(accountCatalogueUsagePort).incrementUsageCount(validEventData);
    }

    @Test
    @DisplayName("Debe continuar sin lanzar excepción cuando el servicio falla")
    void testProcessEventContinuesWhenServiceFails() throws IOException {

        doThrow(new RuntimeException("Error de servicio")).when(accountCatalogueUsagePort).incrementUsageCount(any());

        assertDoesNotThrow(() -> listener.onAccountUsed(validEventData));
    }

    @Test
    @DisplayName("Debe procesar evento de cuenta")
    void testOnAccountUsedProcessesAccount() throws IOException {

        listener.onAccountUsed(validEventData);

        verify(accountCatalogueUsagePort).incrementUsageCount(validEventData);
    }

    @Test
    @DisplayName("Debe procesar evento con tipo ACCOUNT_USED")
    void testOnAccountUsedIgnoresEnvelopeType() throws IOException {

        EventDto<AccountUsedEventDto> eventWithType = new EventDto<>("ACCOUNT_USED", validEventData);

        listener.onAccountUsed(validEventData);

        verify(accountCatalogueUsagePort).incrementUsageCount(validEventData);
    }

    @Test
    @DisplayName("Debe procesar evento con account igual a 0")
    void testProcessEventWithZeroAccount() throws IOException {

        validEventData.setAccount(0L);

        listener.onAccountUsed(validEventData);

        verify(accountCatalogueUsagePort).incrementUsageCount(validEventData);
    }

    @Test
    @DisplayName("Debe procesar evento con account de valor grande")
    void testProcessEventWithLargeAccount() throws IOException {

        validEventData.setAccount(9999999999L);

        listener.onAccountUsed(validEventData);

        verify(accountCatalogueUsagePort).incrementUsageCount(validEventData);
    }

    @Test
    @DisplayName("Debe procesar evento con account negativo")
    void testProcessEventWithNegativeAccount() throws IOException {

        validEventData.setAccount(-1L);

        listener.onAccountUsed(validEventData);

        verify(accountCatalogueUsagePort).incrementUsageCount(validEventData);
    }

    @Test
    @DisplayName("Debe ejecutar flujo completo con evento válido tipo ID")
    void testFullFlowWithValidIdEvent() throws IOException {

        validEventData.setAccount(12345L);
        validEventData.setEnterpriseId("ENTERPRISE123");
        validEventData.setSourceAccountType("ID");

        listener.onAccountUsed(validEventData);

        verify(accountCatalogueUsagePort).incrementUsageCount(argThat(data ->
                data.getAccount().equals(12345L) &&
                data.getEnterpriseId().equals("ENTERPRISE123") &&
                data.getSourceAccountType().equals("ID")
        ));
    }

    @Test
    @DisplayName("Debe ejecutar flujo completo con evento válido tipo CODE")
    void testFullFlowWithValidCodeEvent() throws IOException {

        validEventData.setAccount(11050101L);
        validEventData.setEnterpriseId("ENT999");
        validEventData.setSourceAccountType("CODE");

        listener.onAccountUsed(validEventData);

        verify(accountCatalogueUsagePort).incrementUsageCount(argThat(data ->
                data.getAccount().equals(11050101L) &&
                data.getEnterpriseId().equals("ENT999") &&
                data.getSourceAccountType().equals("CODE")
        ));
    }

    @Test
    @DisplayName("Debe procesar múltiples eventos consecutivos")
    void testProcessMultipleConsecutiveEvents() throws IOException {

        AccountUsedEventDto data1 = new AccountUsedEventDto(1L, "ENT001", "ID");
        AccountUsedEventDto data2 = new AccountUsedEventDto(2L, "ENT002", "CODE");
        AccountUsedEventDto data3 = new AccountUsedEventDto(3L, "ENT003", "ID");

        EventDto<AccountUsedEventDto> event1 = new EventDto<>("ACCOUNT_USED", data1);
        EventDto<AccountUsedEventDto> event2 = new EventDto<>("ACCOUNT_USED", data2);
        EventDto<AccountUsedEventDto> event3 = new EventDto<>("ACCOUNT_USED", data3);

        listener.onAccountUsed(data1);
        listener.onAccountUsed(data2);
        listener.onAccountUsed(data3);

        verify(accountCatalogueUsagePort, times(3)).incrementUsageCount(any());
    }

}

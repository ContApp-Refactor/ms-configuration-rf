package co.unicauca.edu.co.contables.accounting.catalogue.unit.bankAccounts.domain.messageBroker;

import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.services.IBankAccountUsage;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.messageBroker.SpringEventsBankAccountUsageListener;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.messageBroker.dto.BankAccountUsageDto;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.messageBroker.dto.EventDto;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.messageBroker.enums.EventUsageType;
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
class SpringEventsBankAccountUsageListenerTest {

    @Mock
    private IBankAccountUsage bankAccountUsage;

    @InjectMocks
    private SpringEventsBankAccountUsageListener bankAccountUsageListener;

    private EventDto<BankAccountUsageDto, EventUsageType> validEvent;
    private BankAccountUsageDto validUsageDto;

    private static final Long BANK_ACCOUNT_ID = 1L;
    private static final String ENTERPRISE_ID = "ENT-001";
    private static final Integer QUANTITY_USED = 1;

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

    @Test
    @DisplayName("Debe procesar evento válido correctamente")
    void testHandleBankAccountEvent_WithValidEvent_ProcessesSuccessfully() throws Exception {

        doNothing().when(bankAccountUsage).incrementUsageCount(BANK_ACCOUNT_ID, ENTERPRISE_ID);

        bankAccountUsageListener.onBankAccountUsed(validUsageDto);

        verify(bankAccountUsage).incrementUsageCount(BANK_ACCOUNT_ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe invocar incrementUsageCount con parámetros correctos")
    void testHandleBankAccountEvent_InvokesIncrementUsageCountWithCorrectParameters() throws Exception {

        doNothing().when(bankAccountUsage).incrementUsageCount(BANK_ACCOUNT_ID, ENTERPRISE_ID);

        bankAccountUsageListener.onBankAccountUsed(validUsageDto);

        verify(bankAccountUsage).incrementUsageCount(BANK_ACCOUNT_ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe rechazar evento nulo")
    void testIsValidEvent_WithNullEvent_DoesNotProcessEvent() throws Exception {

        bankAccountUsageListener.onBankAccountUsed(null);

        verify(bankAccountUsage, never()).incrementUsageCount(any(), any());
    }

    @Test
    @DisplayName("Debe rechazar evento con bankAccountId nulo")
    void testIsValidEvent_WithNullBankAccountId_DoesNotProcessEvent() throws Exception {

        validUsageDto.setBankAccountId(null);

        bankAccountUsageListener.onBankAccountUsed(validUsageDto);

        verify(bankAccountUsage, never()).incrementUsageCount(any(), any());
    }

    @Test
    @DisplayName("Debe rechazar evento con enterpriseId nulo")
    void testIsValidEvent_WithNullEnterpriseId_DoesNotProcessEvent() throws Exception {

        validUsageDto.setEnterpriseId(null);

        bankAccountUsageListener.onBankAccountUsed(validUsageDto);

        verify(bankAccountUsage, never()).incrementUsageCount(any(), any());
    }

    @Test
    @DisplayName("Debe rechazar evento con enterpriseId vacío")
    void testIsValidEvent_WithEmptyEnterpriseId_DoesNotProcessEvent() throws Exception {

        validUsageDto.setEnterpriseId("");

        bankAccountUsageListener.onBankAccountUsed(validUsageDto);

        verify(bankAccountUsage, never()).incrementUsageCount(any(), any());
    }

    @Test
    @DisplayName("Debe rechazar evento con enterpriseId en blanco")
    void testIsValidEvent_WithBlankEnterpriseId_DoesNotProcessEvent() throws Exception {

        validUsageDto.setEnterpriseId("   ");

        bankAccountUsageListener.onBankAccountUsed(validUsageDto);

        verify(bankAccountUsage, never()).incrementUsageCount(any(), any());
    }

    @Test
    @DisplayName("Debe rechazar evento con quantityUsed nulo")
    void testIsValidEvent_WithNullQuantityUsed_DoesNotProcessEvent() throws Exception {

        validUsageDto.setQuantityUsed(null);

        bankAccountUsageListener.onBankAccountUsed(validUsageDto);

        verify(bankAccountUsage, never()).incrementUsageCount(any(), any());
    }

    @Test
    @DisplayName("Debe rechazar evento con quantityUsed cero")
    void testIsValidEvent_WithZeroQuantityUsed_DoesNotProcessEvent() throws Exception {

        validUsageDto.setQuantityUsed(0);

        bankAccountUsageListener.onBankAccountUsed(validUsageDto);

        verify(bankAccountUsage, never()).incrementUsageCount(any(), any());
    }

    @Test
    @DisplayName("Debe rechazar evento con quantityUsed negativo")
    void testIsValidEvent_WithNegativeQuantityUsed_DoesNotProcessEvent() throws Exception {

        validUsageDto.setQuantityUsed(-1);

        bankAccountUsageListener.onBankAccountUsed(validUsageDto);

        verify(bankAccountUsage, never()).incrementUsageCount(any(), any());
    }

    @Test
    @DisplayName("Debe aceptar evento con quantityUsed positivo")
    void testIsValidEvent_WithPositiveQuantityUsed_ProcessesEvent() throws Exception {

        validUsageDto.setQuantityUsed(10);
        doNothing().when(bankAccountUsage).incrementUsageCount(BANK_ACCOUNT_ID, ENTERPRISE_ID);

        bankAccountUsageListener.onBankAccountUsed(validUsageDto);

        verify(bankAccountUsage).incrementUsageCount(BANK_ACCOUNT_ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe aceptar evento válido completo")
    void testIsValidEvent_WithValidEvent_ProcessesEvent() throws Exception {

        doNothing().when(bankAccountUsage).incrementUsageCount(BANK_ACCOUNT_ID, ENTERPRISE_ID);

        bankAccountUsageListener.onBankAccountUsed(validUsageDto);

        verify(bankAccountUsage).incrementUsageCount(BANK_ACCOUNT_ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe procesar evento tipo USED")
    void testHandleBankAccountEvent_WithUsedType_ProcessesSuccessfully() throws Exception {

        validEvent.setType(EventUsageType.USED);
        doNothing().when(bankAccountUsage).incrementUsageCount(BANK_ACCOUNT_ID, ENTERPRISE_ID);

        bankAccountUsageListener.onBankAccountUsed(validUsageDto);

        verify(bankAccountUsage).incrementUsageCount(BANK_ACCOUNT_ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe continuar procesamiento después de excepción")
    void testHandleBankAccountEvent_WhenExceptionOccurs_DoesNotThrowException() throws Exception {

        doThrow(new RuntimeException("Error al incrementar uso")).when(bankAccountUsage)
                .incrementUsageCount(BANK_ACCOUNT_ID, ENTERPRISE_ID);

        bankAccountUsageListener.onBankAccountUsed(validUsageDto);
        verify(bankAccountUsage).incrementUsageCount(BANK_ACCOUNT_ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe invocar incrementUsageCount solo una vez por evento")
    void testHandleBankAccountEvent_InvokesIncrementUsageCountOncePerEvent() throws Exception {

        doNothing().when(bankAccountUsage).incrementUsageCount(BANK_ACCOUNT_ID, ENTERPRISE_ID);

        bankAccountUsageListener.onBankAccountUsed(validUsageDto);

        verify(bankAccountUsage, times(1)).incrementUsageCount(BANK_ACCOUNT_ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe procesar múltiples eventos consecutivos")
    void testHandleBankAccountEvent_ProcessMultipleEvents_AllProcessedSuccessfully() throws Exception {

        doNothing().when(bankAccountUsage).incrementUsageCount(any(), any());

        bankAccountUsageListener.onBankAccountUsed(validUsageDto);
        bankAccountUsageListener.onBankAccountUsed(validUsageDto);
        bankAccountUsageListener.onBankAccountUsed(validUsageDto);

        verify(bankAccountUsage, times(3)).incrementUsageCount(BANK_ACCOUNT_ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe procesar eventos con diferentes bankAccountIds")
    void testHandleBankAccountEvent_WithDifferentBankAccountIds_ProcessesCorrectly() throws Exception {

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

        bankAccountUsageListener.onBankAccountUsed(data1);
        bankAccountUsageListener.onBankAccountUsed(data2);
        bankAccountUsageListener.onBankAccountUsed(data3);

        verify(bankAccountUsage).incrementUsageCount(bankAccountId1, ENTERPRISE_ID);
        verify(bankAccountUsage).incrementUsageCount(bankAccountId2, ENTERPRISE_ID);
        verify(bankAccountUsage).incrementUsageCount(bankAccountId3, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe procesar eventos con diferentes enterpriseIds")
    void testHandleBankAccountEvent_WithDifferentEnterpriseIds_ProcessesCorrectly() throws Exception {

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

        bankAccountUsageListener.onBankAccountUsed(data1);
        bankAccountUsageListener.onBankAccountUsed(data2);
        bankAccountUsageListener.onBankAccountUsed(data3);

        verify(bankAccountUsage).incrementUsageCount(BANK_ACCOUNT_ID, enterpriseId1);
        verify(bankAccountUsage).incrementUsageCount(BANK_ACCOUNT_ID, enterpriseId2);
        verify(bankAccountUsage).incrementUsageCount(BANK_ACCOUNT_ID, enterpriseId3);
    }

    @Test
    @DisplayName("Debe procesar evento con quantityUsed = 1")
    void testHandleBankAccountEvent_WithQuantityUsedOne_ProcessesSuccessfully() throws Exception {

        validUsageDto.setQuantityUsed(1);
        doNothing().when(bankAccountUsage).incrementUsageCount(BANK_ACCOUNT_ID, ENTERPRISE_ID);

        bankAccountUsageListener.onBankAccountUsed(validUsageDto);

        verify(bankAccountUsage).incrementUsageCount(BANK_ACCOUNT_ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Debe procesar evento con quantityUsed grande")
    void testHandleBankAccountEvent_WithLargeQuantityUsed_ProcessesSuccessfully() throws Exception {

        validUsageDto.setQuantityUsed(1000000);
        doNothing().when(bankAccountUsage).incrementUsageCount(BANK_ACCOUNT_ID, ENTERPRISE_ID);

        bankAccountUsageListener.onBankAccountUsed(validUsageDto);

        verify(bankAccountUsage).incrementUsageCount(BANK_ACCOUNT_ID, ENTERPRISE_ID);
    }
}

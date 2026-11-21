package co.unicauca.edu.co.contables.accounting.catalogue.unit.paymentMethods.domain.messageBroker;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.messageBroker.SpringEventsPaymentMethodUsageListener;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.services.IPaymentMethodUsage;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.used.PaymentMethodUsedEventDto;

@ExtendWith(MockitoExtension.class)
class SpringEventsPaymentMethodUsageListenerTest {

    private static final Long PAYMENT_METHOD_ID = 1L;
    private static final String ENTERPRISE_ID = "ENT-001";
    private static final Integer QUANTITY_USED = 1;

    @Mock
    private IPaymentMethodUsage paymentMethodUsagePort;

    @InjectMocks
    private SpringEventsPaymentMethodUsageListener listener;

    private PaymentMethodUsedEventDto validEvent;

    @BeforeEach
    void setUp() {
        validEvent = new PaymentMethodUsedEventDto(PAYMENT_METHOD_ID, ENTERPRISE_ID, QUANTITY_USED);
    }

    @Test
    @DisplayName("Evento válido - incrementa el uso del método de pago")
    void eventoValido_incrementaUso() {
        listener.onPaymentMethodUsed(validEvent);

        verify(paymentMethodUsagePort).incrementUsageCount(PAYMENT_METHOD_ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Evento válido - incrementa exactamente una vez")
    void eventoValido_incrementaUnaVez() {
        listener.onPaymentMethodUsed(validEvent);

        verify(paymentMethodUsagePort, times(1)).incrementUsageCount(PAYMENT_METHOD_ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Evento nulo - no procesa y no lanza")
    void eventoNulo_noProcesa() {
        assertDoesNotThrow(() -> listener.onPaymentMethodUsed(null));

        verify(paymentMethodUsagePort, never()).incrementUsageCount(any(), anyString());
    }

    @Test
    @DisplayName("paymentMethodId nulo - no procesa")
    void paymentMethodIdNulo_noProcesa() {
        validEvent.setPaymentMethodId(null);

        listener.onPaymentMethodUsed(validEvent);

        verify(paymentMethodUsagePort, never()).incrementUsageCount(any(), anyString());
    }

    @Test
    @DisplayName("enterpriseId nulo - no procesa")
    void enterpriseIdNulo_noProcesa() {
        validEvent.setEnterpriseId(null);

        listener.onPaymentMethodUsed(validEvent);

        verify(paymentMethodUsagePort, never()).incrementUsageCount(any(), any());
    }

    @Test
    @DisplayName("enterpriseId en blanco - no procesa")
    void enterpriseIdEnBlanco_noProcesa() {
        validEvent.setEnterpriseId("   ");

        listener.onPaymentMethodUsed(validEvent);

        verify(paymentMethodUsagePort, never()).incrementUsageCount(any(), any());
    }

    @Test
    @DisplayName("quantityUsed nulo - no procesa")
    void quantityUsedNulo_noProcesa() {
        validEvent.setQuantityUsed(null);

        listener.onPaymentMethodUsed(validEvent);

        verify(paymentMethodUsagePort, never()).incrementUsageCount(any(), any());
    }

    @Test
    @DisplayName("quantityUsed cero o negativo - no procesa")
    void quantityUsedNoPositivo_noProcesa() {
        validEvent.setQuantityUsed(0);
        listener.onPaymentMethodUsed(validEvent);

        validEvent.setQuantityUsed(-1);
        listener.onPaymentMethodUsed(validEvent);

        verify(paymentMethodUsagePort, never()).incrementUsageCount(any(), any());
    }

    @Test
    @DisplayName("Fallo del caso de uso - se registra y no se propaga")
    void falloDelCasoDeUso_noPropaga() {
        doThrow(new RuntimeException("Error al incrementar uso"))
                .when(paymentMethodUsagePort).incrementUsageCount(PAYMENT_METHOD_ID, ENTERPRISE_ID);

        assertDoesNotThrow(() -> listener.onPaymentMethodUsed(validEvent));

        verify(paymentMethodUsagePort).incrementUsageCount(PAYMENT_METHOD_ID, ENTERPRISE_ID);
    }

    @Test
    @DisplayName("Eventos con distintos IDs - cada uno incrementa su propio contador")
    void eventosDistintos_incrementanCadaContador() {
        listener.onPaymentMethodUsed(new PaymentMethodUsedEventDto(1L, ENTERPRISE_ID, 1));
        listener.onPaymentMethodUsed(new PaymentMethodUsedEventDto(2L, ENTERPRISE_ID, 3));

        verify(paymentMethodUsagePort).incrementUsageCount(1L, ENTERPRISE_ID);
        verify(paymentMethodUsagePort).incrementUsageCount(2L, ENTERPRISE_ID);
    }
}

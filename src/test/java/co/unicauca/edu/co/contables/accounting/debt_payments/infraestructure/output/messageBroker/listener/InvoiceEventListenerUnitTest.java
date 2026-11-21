package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.listener;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.test.util.ReflectionTestUtils;

import com.rabbitmq.client.Channel;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports.IEventRecoveryActionPort;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports.IMessageErrorHandlingPort;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.adapter.InvoicePersistenceAdapter;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.EventDto;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.InvoiceSyncDto;

@ExtendWith(MockitoExtension.class)
@DisplayName("debt_payments InvoiceEventListener Unit Tests")
class InvoiceEventListenerUnitTest {

    private static final long DELIVERY_TAG = 123L;
    private static final long FACT_CODE = 1001L;

    @Mock
    private InvoicePersistenceAdapter invoicePersistenceAdapter;

    @Mock
    private IMessageErrorHandlingPort messageErrorHandlingPort;

    @Mock
    private IEventRecoveryActionPort<EventDto<InvoiceSyncDto>> invoiceRecoveryActionPort;

    @Mock
    private Channel channel;

    private TestableInvoiceEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new TestableInvoiceEventListener(
                invoicePersistenceAdapter, messageErrorHandlingPort, invoiceRecoveryActionPort);

        ReflectionTestUtils.invokeMethod(listener, "init");
    }

    @Test
    @DisplayName("SALE válido - persiste el DTO y hace ACK")
    void saleValido_persisteYAck() throws IOException {
        EventDto<InvoiceSyncDto> event = saleEvent();

        listener.handleInvoiceEvent(message(), event, channel, DELIVERY_TAG);

        ArgumentCaptor<InvoiceSyncDto> captor = ArgumentCaptor.forClass(InvoiceSyncDto.class);
        verify(invoicePersistenceAdapter).saveOrUpdate(captor.capture());
        assertSame(event.getData(), captor.getValue());
        verify(channel).basicAck(DELIVERY_TAG, false);
        verifyNoInteractions(messageErrorHandlingPort);
        verifyNoInteractions(invoiceRecoveryActionPort);
    }

    @Test
    @DisplayName("DELETED - borra por factCode y hace ACK")
    void deleted_borraYAck() throws IOException {
        EventDto<InvoiceSyncDto> event = saleEvent();
        event.setType("DELETED");

        listener.handleInvoiceEvent(message(), event, channel, DELIVERY_TAG);

        verify(invoicePersistenceAdapter).delete(FACT_CODE);
        verify(invoicePersistenceAdapter, never()).saveOrUpdate(any());
        verify(channel).basicAck(DELIVERY_TAG, false);
        verifyNoInteractions(messageErrorHandlingPort);
    }

    @Test
    @DisplayName("Tipo desconocido - descarta, hace ACK y no persiste error")
    void tipoDesconocido_descartaYAck() throws IOException {
        EventDto<InvoiceSyncDto> event = saleEvent();
        event.setType("RETURNONSALE");

        listener.handleInvoiceEvent(message(), event, channel, DELIVERY_TAG);

        verifyNoInteractions(invoicePersistenceAdapter);
        verify(channel).basicAck(DELIVERY_TAG, false);
        verifyNoInteractions(messageErrorHandlingPort);
    }

    @Test
    @DisplayName("Evento nulo - validación, error persistido y ACK")
    void eventoNulo_validacionYErrorPersistido() throws IOException {
        listener.handleRawMessage(null, channel, DELIVERY_TAG);

        verify(messageErrorHandlingPort).saveProcessingError(
                isNull(),
                eq("Validation failed: Event is null"),
                eq("{\"error\": \"Event is null\"}"),
                eq("Invoice"));
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Data nula - validación, error persistido y ACK")
    void dataNula_validacionYErrorPersistido() throws IOException {
        EventDto<InvoiceSyncDto> event = new EventDto<>("SALE", null);

        listener.handleInvoiceEvent(message(), event, channel, DELIVERY_TAG);

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("SALE"),
                eq("Validation failed: Event data is null"),
                eq("{\"error\": \"Event data is null\", \"eventType\": \"SALE\"}"),
                eq("Invoice"));
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Tipo nulo - validación, error persistido y ACK")
    void tipoNulo_validacionYErrorPersistido() throws IOException {
        EventDto<InvoiceSyncDto> event = saleEvent();
        event.setType(null);

        listener.handleInvoiceEvent(message(), event, channel, DELIVERY_TAG);

        verify(messageErrorHandlingPort).saveProcessingError(
                isNull(),
                eq("Validation failed: Event type is null"),
                contains("\"factCode\":1001"),
                eq("Invoice"));
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("factCode nulo - validación, error persistido y ACK")
    void factCodeNulo_validacionYErrorPersistido() throws IOException {
        EventDto<InvoiceSyncDto> event = saleEvent();
        event.getData().setFactCode(null);

        listener.handleInvoiceEvent(message(), event, channel, DELIVERY_TAG);

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("SALE"),
                eq("Validation failed: FactCode is null"),
                contains("\"factCode\":null"),
                eq("Invoice"));
        verify(channel).basicAck(DELIVERY_TAG, false);
        verifyNoInteractions(invoicePersistenceAdapter);
    }

    @Test
    @DisplayName("accountingAccount nulo - validación, error persistido y ACK")
    void accountingAccountNulo_validacionYErrorPersistido() throws IOException {
        EventDto<InvoiceSyncDto> event = saleEvent();
        event.getData().setAccountingAccount(null);

        listener.handleInvoiceEvent(message(), event, channel, DELIVERY_TAG);

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("SALE"),
                eq("Validation failed: AccountingAccount is null"),
                contains("\"accountingAccount\":null"),
                eq("Invoice"));
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("entId nulo - validación, error persistido y ACK")
    void entIdNulo_validacionYErrorPersistido() throws IOException {
        EventDto<InvoiceSyncDto> event = saleEvent();
        event.getData().setEntId(null);

        listener.handleInvoiceEvent(message(), event, channel, DELIVERY_TAG);

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("SALE"),
                eq("Validation failed: EntId is null"),
                contains("\"entId\":null"),
                eq("Invoice"));
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("expirationDate nula - validación, error persistido y ACK")
    void expirationDateNula_validacionYErrorPersistido() throws IOException {
        EventDto<InvoiceSyncDto> event = saleEvent();
        event.getData().setExpirationDate(null);

        listener.handleInvoiceEvent(message(), event, channel, DELIVERY_TAG);

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("SALE"),
                eq("Validation failed: ExpirationDate is null"),
                contains("\"expirationDate\":null"),
                eq("Invoice"));
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("pendingValue nulo - validación, error persistido y ACK")
    void pendingValueNulo_validacionYErrorPersistido() throws IOException {
        EventDto<InvoiceSyncDto> event = saleEvent();
        event.getData().setPendingValue(null);

        listener.handleInvoiceEvent(message(), event, channel, DELIVERY_TAG);

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("SALE"),
                eq("Validation failed: PendingValue is null"),
                contains("\"pendingValue\":null"),
                eq("Invoice"));
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("thirdId nulo - validación, error persistido y ACK")
    void thirdIdNulo_validacionYErrorPersistido() throws IOException {
        EventDto<InvoiceSyncDto> event = saleEvent();
        event.getData().setThirdId(null);

        listener.handleInvoiceEvent(message(), event, channel, DELIVERY_TAG);

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("SALE"),
                eq("Validation failed: ThirdId is null"),
                contains("\"thirdId\":null"),
                eq("Invoice"));
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("totalPay nulo - validación, error persistido y ACK")
    void totalPayNulo_validacionYErrorPersistido() throws IOException {
        EventDto<InvoiceSyncDto> event = saleEvent();
        event.getData().setTotalPay(null);

        listener.handleInvoiceEvent(message(), event, channel, DELIVERY_TAG);

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("SALE"),
                eq("Validation failed: TotalPay is null"),
                contains("\"totalPay\":null"),
                eq("Invoice"));
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("totalValue nulo - validación, error persistido y ACK")
    void totalValueNulo_validacionYErrorPersistido() throws IOException {
        EventDto<InvoiceSyncDto> event = saleEvent();
        event.getData().setTotalValue(null);

        listener.handleInvoiceEvent(message(), event, channel, DELIVERY_TAG);

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("SALE"),
                eq("Validation failed: TotalValue is null"),
                contains("\"totalValue\":null"),
                eq("Invoice"));
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("creationDate nula - no se valida en este módulo (paridad legada)")
    void creationDateNula_noSeValida() throws IOException {
        EventDto<InvoiceSyncDto> event = saleEvent();
        event.getData().setCreationDate(null);

        listener.handleInvoiceEvent(message(), event, channel, DELIVERY_TAG);

        verify(invoicePersistenceAdapter).saveOrUpdate(any(InvoiceSyncDto.class));
        verify(channel).basicAck(DELIVERY_TAG, false);
        verifyNoInteractions(messageErrorHandlingPort);
    }

    @Test
    @DisplayName("Error de persistencia - intenta recovery (stub lanza), persiste sin sufijo y hace ACK")
    void errorDePersistencia_recoveryStubLanzaYPersisteSinSufijo() throws IOException {
        doThrow(new RuntimeException("db down"))
                .when(invoicePersistenceAdapter).saveOrUpdate(any(InvoiceSyncDto.class));
        when(invoiceRecoveryActionPort.canHandle(any())).thenThrow(
                new UnsupportedOperationException("Unimplemented method 'canHandle'"));

        assertDoesNotThrow(() -> listener.handleInvoiceEvent(message(), saleEvent(), channel, DELIVERY_TAG));

        verify(invoiceRecoveryActionPort).canHandle(any(EventDto.class));
        verify(invoiceRecoveryActionPort, never()).executeRecoveryAction(any());
        verify(messageErrorHandlingPort).saveProcessingError(
                eq("SALE"),
                eq("Processing error: db down"),
                contains("\"factCode\":1001"),
                eq("Invoice"));
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Recovery exitoso - agrega el sufijo (Recovery action executed)")
    void recoveryExitoso_agregaSufijo() throws IOException {
        doThrow(new RuntimeException("db down"))
                .when(invoicePersistenceAdapter).saveOrUpdate(any(InvoiceSyncDto.class));
        when(invoiceRecoveryActionPort.canHandle(any())).thenReturn(true);
        when(invoiceRecoveryActionPort.executeRecoveryAction(any())).thenReturn(true);

        listener.handleInvoiceEvent(message(), saleEvent(), channel, DELIVERY_TAG);

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("SALE"),
                eq("Processing error: db down (Recovery action executed)"),
                contains("\"factCode\":1001"),
                eq("Invoice"));
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Recovery fallido - no agrega sufijo")
    void recoveryFallido_sinSufijo() throws IOException {
        doThrow(new RuntimeException("db down"))
                .when(invoicePersistenceAdapter).saveOrUpdate(any(InvoiceSyncDto.class));
        when(invoiceRecoveryActionPort.canHandle(any())).thenReturn(true);
        when(invoiceRecoveryActionPort.executeRecoveryAction(any())).thenReturn(false);

        listener.handleInvoiceEvent(message(), saleEvent(), channel, DELIVERY_TAG);

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("SALE"),
                eq("Processing error: db down"),
                contains("\"factCode\":1001"),
                eq("Invoice"));
    }

    @Test
    @DisplayName("Recovery no aplicable - no ejecuta la acción ni agrega sufijo")
    void recoveryNoAplicable_noEjecuta() throws IOException {
        doThrow(new RuntimeException("db down"))
                .when(invoicePersistenceAdapter).saveOrUpdate(any(InvoiceSyncDto.class));
        when(invoiceRecoveryActionPort.canHandle(any())).thenReturn(false);

        listener.handleInvoiceEvent(message(), saleEvent(), channel, DELIVERY_TAG);

        verify(invoiceRecoveryActionPort, never()).executeRecoveryAction(any());
        verify(messageErrorHandlingPort).saveProcessingError(
                eq("SALE"),
                eq("Processing error: db down"),
                contains("\"factCode\":1001"),
                eq("Invoice"));
    }

    @Test
    @DisplayName("DELETED con error - persiste procesamiento, hace ACK y no propaga")
    void deletedConError_persisteProcesamientoYAck() throws IOException {
        doThrow(new RuntimeException("db down")).when(invoicePersistenceAdapter).delete(FACT_CODE);
        EventDto<InvoiceSyncDto> event = saleEvent();
        event.setType("DELETED");

        assertDoesNotThrow(() -> listener.handleInvoiceEvent(message(), event, channel, DELIVERY_TAG));

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("DELETED"),
                eq("Processing error: db down"),
                contains("\"factCode\":1001"),
                eq("Invoice"));
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Fallo al persistir la validación - hace ACK y no propaga")
    void falloAlPersistirValidacion_haceAckYNoPropaga() throws IOException {
        doThrow(new RuntimeException("db down")).when(messageErrorHandlingPort)
                .saveProcessingError(any(), anyString(), anyString(), anyString());
        EventDto<InvoiceSyncDto> event = saleEvent();
        event.getData().setFactCode(null);

        assertDoesNotThrow(() -> listener.handleInvoiceEvent(message(), event, channel, DELIVERY_TAG));

        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Fallo al persistir el procesamiento - no hace ACK (comportamiento legado)")
    void falloAlPersistirProcesamiento_noHaceAck() throws IOException {
        doThrow(new RuntimeException("db down"))
                .when(invoicePersistenceAdapter).saveOrUpdate(any(InvoiceSyncDto.class));
        doThrow(new RuntimeException("error db down")).when(messageErrorHandlingPort)
                .saveProcessingError(any(), anyString(), anyString(), anyString());

        assertDoesNotThrow(() -> listener.handleInvoiceEvent(message(), saleEvent(), channel, DELIVERY_TAG));

        verify(channel, never()).basicAck(anyLong(), eq(false));
    }

    @Test
    @DisplayName("Fallo del ACK - no propaga")
    void falloAck_noPropaga() throws IOException {
        doThrow(new IOException("channel closed")).when(channel).basicAck(anyLong(), eq(false));

        assertDoesNotThrow(() -> listener.handleInvoiceEvent(message(), saleEvent(), channel, DELIVERY_TAG));
    }

    private static class TestableInvoiceEventListener extends InvoiceEventListener {

        TestableInvoiceEventListener(InvoicePersistenceAdapter invoicePersistenceAdapter,
                IMessageErrorHandlingPort messageErrorHandlingPort,
                IEventRecoveryActionPort<EventDto<InvoiceSyncDto>> invoiceRecoveryActionPort) {
            super(invoicePersistenceAdapter, messageErrorHandlingPort, invoiceRecoveryActionPort);
        }

        void handleRawMessage(EventDto<InvoiceSyncDto> event, Channel channel, long deliveryTag) {
            super.handleMessage(event, channel, deliveryTag);
        }
    }

    private Message message() {
        return new Message(new byte[0]);
    }

    private EventDto<InvoiceSyncDto> saleEvent() {
        InvoiceSyncDto data = new InvoiceSyncDto();
        data.setFactCode(FACT_CODE);
        data.setEntId("ENT-1");
        data.setThirdId(55L);
        data.setTotalValue(100000L);
        data.setTotalPay(40000L);
        data.setPendingValue(60000L);
        data.setCreationDate(LocalDate.of(2026, 1, 15));
        data.setExpirationDate(LocalDate.of(2026, 2, 15));
        data.setActive(true);
        data.setAccountingAccount(130505L);
        return new EventDto<>("SALE", data);
    }
}

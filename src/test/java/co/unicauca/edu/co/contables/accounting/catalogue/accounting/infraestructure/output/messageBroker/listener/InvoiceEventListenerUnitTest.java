package co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.listener;

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
import java.math.BigDecimal;
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

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.input.IInvoiceProcessInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.InvoiceReplica;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.DTO.EventDTO;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.DTO.InvoiceSyncDto;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.adapter.InvoicePersistenceAdapter;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.mapper.IInvoiceEventMapper;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports.IEventRecoveryActionPort;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports.IMessageErrorHandlingPort;

@ExtendWith(MockitoExtension.class)
@DisplayName("catalogue/accounting InvoiceEventListener Unit Tests")
class InvoiceEventListenerUnitTest {

    private static final long DELIVERY_TAG = 123L;
    private static final long FACT_CODE = 1001L;

    @Mock
    private InvoicePersistenceAdapter invoicePersistenceAdapter;

    @Mock
    private IMessageErrorHandlingPort messageErrorHandlingPort;

    @Mock
    private IEventRecoveryActionPort<EventDTO<InvoiceSyncDto>> eventRecoveryActionPort;

    @Mock
    private IInvoiceProcessInputPort invoiceProcessInputPort;

    @Mock
    private IInvoiceEventMapper invoiceEventMapper;

    @Mock
    private Channel channel;

    private TestableInvoiceEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new TestableInvoiceEventListener(
                invoicePersistenceAdapter, messageErrorHandlingPort,
                eventRecoveryActionPort, invoiceProcessInputPort, invoiceEventMapper);

        ReflectionTestUtils.invokeMethod(listener, "init");
    }

    @Test
    @DisplayName("SALE válido - mapea, procesa y hace ACK")
    void saleValido_mapeaProcesaYAck() throws IOException {
        InvoiceReplica replica = new InvoiceReplica();
        replica.setFactCode(String.valueOf(FACT_CODE));
        when(invoiceEventMapper.toDomain(any(InvoiceSyncDto.class))).thenReturn(replica);
        EventDTO<InvoiceSyncDto> event = saleEvent();

        listener.handleInvoiceEvent(message(), event, channel, DELIVERY_TAG);

        ArgumentCaptor<InvoiceSyncDto> captor = ArgumentCaptor.forClass(InvoiceSyncDto.class);
        verify(invoiceEventMapper).toDomain(captor.capture());
        assertSame(event.getData(), captor.getValue());
        verify(invoiceProcessInputPort).processInvoiceCreation(replica);
        verify(channel).basicAck(DELIVERY_TAG, false);
        verifyNoInteractions(messageErrorHandlingPort);
    }

    @Test
    @DisplayName("SALE válido - el modelo de dominio llega al caso de uso")
    void saleValido_modeloDeDominioAlCasoDeUso() throws IOException {
        InvoiceReplica replica = new InvoiceReplica();
        replica.setFactCode("1001");
        replica.setTotalValue(new BigDecimal("100000"));
        when(invoiceEventMapper.toDomain(any(InvoiceSyncDto.class))).thenReturn(replica);

        listener.handleInvoiceEvent(message(), saleEvent(), channel, DELIVERY_TAG);

        verify(invoiceProcessInputPort).processInvoiceCreation(replica);
    }

    @Test
    @DisplayName("DELETED - borra por factCode y hace ACK")
    void deleted_borraYAck() throws IOException {
        EventDTO<InvoiceSyncDto> event = saleEvent();
        event.setType("DELETED");

        listener.handleInvoiceEvent(message(), event, channel, DELIVERY_TAG);

        verify(invoicePersistenceAdapter).delete(FACT_CODE);
        verifyNoInteractions(invoiceEventMapper);
        verifyNoInteractions(invoiceProcessInputPort);
        verify(channel).basicAck(DELIVERY_TAG, false);
        verifyNoInteractions(messageErrorHandlingPort);
    }

    @Test
    @DisplayName("Tipo desconocido - descarta, hace ACK y no persiste error")
    void tipoDesconocido_descartaYAck() throws IOException {
        EventDTO<InvoiceSyncDto> event = saleEvent();
        event.setType("RETURNONSALE");

        listener.handleInvoiceEvent(message(), event, channel, DELIVERY_TAG);

        verifyNoInteractions(invoiceEventMapper);
        verifyNoInteractions(invoiceProcessInputPort);
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
        EventDTO<InvoiceSyncDto> event = new EventDTO<>("SALE", null);

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
        EventDTO<InvoiceSyncDto> event = saleEvent();
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
        EventDTO<InvoiceSyncDto> event = saleEvent();
        event.getData().setFactCode(null);

        listener.handleInvoiceEvent(message(), event, channel, DELIVERY_TAG);

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("SALE"),
                eq("Validation failed: FactCode is null"),
                contains("\"factCode\":null"),
                eq("Invoice"));
        verify(channel).basicAck(DELIVERY_TAG, false);
        verifyNoInteractions(invoiceEventMapper);
    }

    @Test
    @DisplayName("accountingAccount nulo - validación, error persistido y ACK")
    void accountingAccountNulo_validacionYErrorPersistido() throws IOException {
        EventDTO<InvoiceSyncDto> event = saleEvent();
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
        EventDTO<InvoiceSyncDto> event = saleEvent();
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
        EventDTO<InvoiceSyncDto> event = saleEvent();
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
        EventDTO<InvoiceSyncDto> event = saleEvent();
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
        EventDTO<InvoiceSyncDto> event = saleEvent();
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
        EventDTO<InvoiceSyncDto> event = saleEvent();
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
        EventDTO<InvoiceSyncDto> event = saleEvent();
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
    @DisplayName("creationDate nula - validación, error persistido y ACK")
    void creationDateNula_validacionYErrorPersistido() throws IOException {
        EventDTO<InvoiceSyncDto> event = saleEvent();
        event.getData().setCreationDate(null);

        listener.handleInvoiceEvent(message(), event, channel, DELIVERY_TAG);

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("SALE"),
                eq("Validation failed: CreationDate is null"),
                contains("\"creationDate\":null"),
                eq("Invoice"));
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Error del caso de uso - persiste procesamiento, hace ACK y no propaga")
    void errorDelCasoDeUso_persisteProcesamientoYAck() throws IOException {
        when(invoiceEventMapper.toDomain(any(InvoiceSyncDto.class))).thenReturn(new InvoiceReplica());
        doThrow(new RuntimeException("db down"))
                .when(invoiceProcessInputPort).processInvoiceCreation(any());

        assertDoesNotThrow(() -> listener.handleInvoiceEvent(message(), saleEvent(), channel, DELIVERY_TAG));

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("SALE"),
                eq("Processing error: db down"),
                contains("\"factCode\":1001"),
                eq("Invoice"));
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Error del mapper - persiste procesamiento, hace ACK y no propaga")
    void errorDelMapper_persisteProcesamientoYAck() throws IOException {
        when(invoiceEventMapper.toDomain(any(InvoiceSyncDto.class)))
                .thenThrow(new RuntimeException("mapper failure"));

        assertDoesNotThrow(() -> listener.handleInvoiceEvent(message(), saleEvent(), channel, DELIVERY_TAG));

        verify(invoiceProcessInputPort, never()).processInvoiceCreation(any());
        verify(messageErrorHandlingPort).saveProcessingError(
                eq("SALE"),
                eq("Processing error: mapper failure"),
                contains("\"factCode\":1001"),
                eq("Invoice"));
        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Fallo al persistir la validación - hace ACK y no propaga")
    void falloAlPersistirValidacion_haceAckYNoPropaga() throws IOException {
        doThrow(new RuntimeException("db down")).when(messageErrorHandlingPort)
                .saveProcessingError(any(), anyString(), anyString(), anyString());
        EventDTO<InvoiceSyncDto> event = saleEvent();
        event.getData().setFactCode(null);

        assertDoesNotThrow(() -> listener.handleInvoiceEvent(message(), event, channel, DELIVERY_TAG));

        verify(channel).basicAck(DELIVERY_TAG, false);
    }

    @Test
    @DisplayName("Fallo al persistir el procesamiento - no hace ACK (comportamiento legado)")
    void falloAlPersistirProcesamiento_noHaceAck() throws IOException {
        when(invoiceEventMapper.toDomain(any(InvoiceSyncDto.class))).thenReturn(new InvoiceReplica());
        doThrow(new RuntimeException("db down"))
                .when(invoiceProcessInputPort).processInvoiceCreation(any());
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

    @Test
    @DisplayName("Recovery action - el puerto legado se inyecta pero nunca se invoca")
    void recoveryAction_nuncaSeInvoca() throws IOException {
        when(invoiceEventMapper.toDomain(any(InvoiceSyncDto.class))).thenReturn(new InvoiceReplica());
        doThrow(new RuntimeException("db down"))
                .when(invoiceProcessInputPort).processInvoiceCreation(any());

        listener.handleInvoiceEvent(message(), saleEvent(), channel, DELIVERY_TAG);

        verifyNoInteractions(eventRecoveryActionPort);
    }

    private static class TestableInvoiceEventListener extends InvoiceEventListener {

        TestableInvoiceEventListener(InvoicePersistenceAdapter invoicePersistenceAdapter,
                IMessageErrorHandlingPort messageErrorHandlingPort,
                IEventRecoveryActionPort<EventDTO<InvoiceSyncDto>> eventRecoveryActionPort,
                IInvoiceProcessInputPort invoiceProcessInputPort,
                IInvoiceEventMapper invoiceEventMapper) {
            super(invoicePersistenceAdapter, messageErrorHandlingPort,
                    eventRecoveryActionPort, invoiceProcessInputPort, invoiceEventMapper);
        }

        void handleRawMessage(EventDTO<InvoiceSyncDto> event, Channel channel, long deliveryTag) {
            super.handleMessage(event, channel, deliveryTag);
        }
    }

    private Message message() {
        return new Message(new byte[0]);
    }

    private EventDTO<InvoiceSyncDto> saleEvent() {
        InvoiceSyncDto data = new InvoiceSyncDto();
        data.setFactCode(FACT_CODE);
        data.setEntId("ENT-1");
        data.setThirdId(55L);
        data.setTotalValue(new BigDecimal("100000"));
        data.setTotalPay(new BigDecimal("40000"));
        data.setPendingValue(new BigDecimal("60000"));
        data.setCreationDate(LocalDate.of(2026, 1, 15));
        data.setExpirationDate(LocalDate.of(2026, 2, 15));
        data.setActive(true);
        data.setAccountingAccount(130505L);
        return new EventDTO<>("SALE", data);
    }
}

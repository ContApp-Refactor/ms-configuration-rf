package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.listener;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports.IEventRecoveryActionPort;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports.IMessageErrorHandlingPort;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.adapter.InvoicePersistenceAdapter;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.EventDto;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.InvoiceSyncDto;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.InvoiceSaleEvent;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.ReceiptSalesDtoRequest;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.enums.EventFactureType;

@ExtendWith(MockitoExtension.class)
@DisplayName("debt_payments SpringEventsInvoiceEventListener Unit Tests")
class SpringEventsInvoiceEventListenerTest {

    private static final long FACT_CODE = 1001L;
    private static final String ENT_ID = "ENT-1";
    private static final long THIRD_ID = 55L;

    @Mock
    private InvoicePersistenceAdapter invoicePersistenceAdapter;

    @Mock
    private IMessageErrorHandlingPort messageErrorHandlingPort;

    @Mock
    private IEventRecoveryActionPort<EventDto<InvoiceSyncDto>> invoiceRecoveryActionPort;

    private SpringEventsInvoiceEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new SpringEventsInvoiceEventListener(
                invoicePersistenceAdapter, messageErrorHandlingPort, invoiceRecoveryActionPort);
    }

    @Test
    @DisplayName("SALE válido - copia el DTO sin conversión y lo persiste")
    void saleValido_copiaYPersiste() {
        ReceiptSalesDtoRequest source = sale();

        listener.onInvoiceSale(new InvoiceSaleEvent(source));

        ArgumentCaptor<InvoiceSyncDto> captor = ArgumentCaptor.forClass(InvoiceSyncDto.class);
        verify(invoicePersistenceAdapter).saveOrUpdate(captor.capture());
        InvoiceSyncDto dto = captor.getValue();
        assertEquals(FACT_CODE, dto.getFactCode());
        assertEquals(ENT_ID, dto.getEntId());
        assertEquals(THIRD_ID, dto.getThirdId());
        assertEquals(100000L, dto.getTotalValue());
        assertEquals(40000L, dto.getTotalPay());
        assertEquals(60000L, dto.getPendingValue());
        assertEquals(LocalDate.of(2026, 1, 15), dto.getCreationDate());
        assertEquals(LocalDate.of(2026, 2, 15), dto.getExpirationDate());
        assertEquals(true, dto.isActive());
        assertEquals(130505L, dto.getAccountingAccount());
        verifyNoInteractions(messageErrorHandlingPort);
        verifyNoInteractions(invoiceRecoveryActionPort);
    }

    @Test
    @DisplayName("SALE válido - el DTO del productor viaja como copia, no como referencia")
    void saleValido_esCopiaIndependiente() {
        ReceiptSalesDtoRequest source = sale();

        listener.onInvoiceSale(new InvoiceSaleEvent(source));

        ArgumentCaptor<InvoiceSyncDto> captor = ArgumentCaptor.forClass(InvoiceSyncDto.class);
        verify(invoicePersistenceAdapter).saveOrUpdate(captor.capture());
        assertNotSame(source, captor.getValue());
    }

    @Test
    @DisplayName("Tipo desconocido - se descarta sin procesar ni persistir error")
    void tipoDesconocido_seDescarta() {
        InvoiceSaleEvent event = new InvoiceSaleEvent(sale());
        event.setType(EventFactureType.PURCHASE);

        listener.onInvoiceSale(event);

        verifyNoInteractions(invoicePersistenceAdapter);
        verifyNoInteractions(messageErrorHandlingPort);
        verifyNoInteractions(invoiceRecoveryActionPort);
    }

    @Test
    @DisplayName("DELETED - borra por factCode (paridad con el switch legado)")
    void deleted_borraPorFactCode() {
        InvoiceSyncDto dto = localDto();
        EventDto<InvoiceSyncDto> event = new EventDto<>("DELETED", dto);

        ReflectionTestUtils.invokeMethod(listener, "handleInvoiceEvent", event);

        verify(invoicePersistenceAdapter).delete(FACT_CODE);
        verify(invoicePersistenceAdapter, never()).saveOrUpdate(any());
        verifyNoInteractions(messageErrorHandlingPort);
    }

    @Test
    @DisplayName("Evento nulo - persiste validación con tipo nulo")
    void eventoNulo_persisteValidacion() {
        assertDoesNotThrow(() -> listener.onInvoiceSale(null));

        verify(messageErrorHandlingPort).saveProcessingError(
                isNull(),
                eq("Validation failed: Event is null"),
                eq("{\"error\": \"Event is null\"}"),
                eq("Invoice"));
        verifyNoInteractions(invoicePersistenceAdapter);
    }

    @Test
    @DisplayName("Data nula - persiste validación con el tipo del evento")
    void dataNula_persisteValidacion() {
        listener.onInvoiceSale(new InvoiceSaleEvent(null));

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("SALE"),
                eq("Validation failed: Event data is null"),
                eq("{\"error\": \"Event data is null\", \"eventType\": \"SALE\"}"),
                eq("Invoice"));
        verifyNoInteractions(invoicePersistenceAdapter);
    }

    @Test
    @DisplayName("Tipo nulo - persiste validación con el JSON del DTO")
    void tipoNulo_persisteValidacion() {
        InvoiceSaleEvent event = new InvoiceSaleEvent(sale());
        event.setType(null);

        listener.onInvoiceSale(event);

        verify(messageErrorHandlingPort).saveProcessingError(
                isNull(),
                eq("Validation failed: Event type is null"),
                contains("\"factCode\":1001"),
                eq("Invoice"));
        verifyNoInteractions(invoicePersistenceAdapter);
    }

    @Test
    @DisplayName("factCode nulo - validación, error persistido y sin persistencia")
    void factCodeNulo_persisteValidacion() {
        InvoiceSaleEvent event = new InvoiceSaleEvent(sale());
        event.getData().setFactCode(null);

        listener.onInvoiceSale(event);

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("SALE"),
                eq("Validation failed: FactCode is null"),
                contains("\"factCode\":null"),
                eq("Invoice"));
        verifyNoInteractions(invoicePersistenceAdapter);
    }

    @Test
    @DisplayName("accountingAccount nulo - validación, error persistido y sin persistencia")
    void accountingAccountNulo_persisteValidacion() {
        InvoiceSaleEvent event = new InvoiceSaleEvent(sale());
        event.getData().setAccountingAccount(null);

        listener.onInvoiceSale(event);

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("SALE"),
                eq("Validation failed: AccountingAccount is null"),
                contains("\"accountingAccount\":null"),
                eq("Invoice"));
        verifyNoInteractions(invoicePersistenceAdapter);
    }

    @Test
    @DisplayName("entId nulo - validación, error persistido y sin persistencia")
    void entIdNulo_persisteValidacion() {
        InvoiceSaleEvent event = new InvoiceSaleEvent(sale());
        event.getData().setEntId(null);

        listener.onInvoiceSale(event);

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("SALE"),
                eq("Validation failed: EntId is null"),
                contains("\"entId\":null"),
                eq("Invoice"));
        verifyNoInteractions(invoicePersistenceAdapter);
    }

    @Test
    @DisplayName("expirationDate nula - validación, error persistido y sin persistencia")
    void expirationDateNula_persisteValidacion() {
        InvoiceSaleEvent event = new InvoiceSaleEvent(sale());
        event.getData().setExpirationDate(null);

        listener.onInvoiceSale(event);

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("SALE"),
                eq("Validation failed: ExpirationDate is null"),
                contains("\"expirationDate\":null"),
                eq("Invoice"));
        verifyNoInteractions(invoicePersistenceAdapter);
    }

    @Test
    @DisplayName("pendingValue nulo - validación, error persistido y sin persistencia")
    void pendingValueNulo_persisteValidacion() {
        InvoiceSaleEvent event = new InvoiceSaleEvent(sale());
        event.getData().setPendingValue(null);

        listener.onInvoiceSale(event);

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("SALE"),
                eq("Validation failed: PendingValue is null"),
                contains("\"pendingValue\":null"),
                eq("Invoice"));
        verifyNoInteractions(invoicePersistenceAdapter);
    }

    @Test
    @DisplayName("thirdId nulo - validación, error persistido y sin persistencia")
    void thirdIdNulo_persisteValidacion() {
        InvoiceSaleEvent event = new InvoiceSaleEvent(sale());
        event.getData().setThirdId(null);

        listener.onInvoiceSale(event);

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("SALE"),
                eq("Validation failed: ThirdId is null"),
                contains("\"thirdId\":null"),
                eq("Invoice"));
        verifyNoInteractions(invoicePersistenceAdapter);
    }

    @Test
    @DisplayName("totalPay nulo - validación, error persistido y sin persistencia")
    void totalPayNulo_persisteValidacion() {
        InvoiceSaleEvent event = new InvoiceSaleEvent(sale());
        event.getData().setTotalPay(null);

        listener.onInvoiceSale(event);

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("SALE"),
                eq("Validation failed: TotalPay is null"),
                contains("\"totalPay\":null"),
                eq("Invoice"));
        verifyNoInteractions(invoicePersistenceAdapter);
    }

    @Test
    @DisplayName("totalValue nulo - validación, error persistido y sin persistencia")
    void totalValueNulo_persisteValidacion() {
        InvoiceSaleEvent event = new InvoiceSaleEvent(sale());
        event.getData().setTotalValue(null);

        listener.onInvoiceSale(event);

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("SALE"),
                eq("Validation failed: TotalValue is null"),
                contains("\"totalValue\":null"),
                eq("Invoice"));
        verifyNoInteractions(invoicePersistenceAdapter);
    }

    @Test
    @DisplayName("CreationDate nula - no se valida en este módulo (paridad legada)")
    void creationDateNula_noSeValida() {
        InvoiceSaleEvent event = new InvoiceSaleEvent(sale());
        event.getData().setCreationDate(null);

        listener.onInvoiceSale(event);

        verify(invoicePersistenceAdapter).saveOrUpdate(any(InvoiceSyncDto.class));
        verifyNoInteractions(messageErrorHandlingPort);
    }

    @Test
    @DisplayName("Error de persistencia - intenta recovery, persiste Processing error y lo traga")
    void errorDePersistencia_intentaRecoveryYPersiste() {
        when(invoiceRecoveryActionPort.canHandle(any())).thenThrow(
                new UnsupportedOperationException("Unimplemented method 'canHandle'"));

        doThrow(new RuntimeException("db down"))
                .when(invoicePersistenceAdapter).saveOrUpdate(any(InvoiceSyncDto.class));

        assertDoesNotThrow(() -> listener.onInvoiceSale(new InvoiceSaleEvent(sale())));

        verify(invoiceRecoveryActionPort).canHandle(any(EventDto.class));
        verify(invoiceRecoveryActionPort, never()).executeRecoveryAction(any());
        verify(messageErrorHandlingPort).saveProcessingError(
                eq("SALE"),
                eq("Processing error: db down"),
                contains("\"factCode\":1001"),
                eq("Invoice"));
    }

    @Test
    @DisplayName("Recovery exitoso - agrega el sufijo (Recovery action executed)")
    void recoveryExitoso_agregaSufijo() {
        doThrow(new RuntimeException("db down"))
                .when(invoicePersistenceAdapter).saveOrUpdate(any(InvoiceSyncDto.class));
        when(invoiceRecoveryActionPort.canHandle(any())).thenReturn(true);
        when(invoiceRecoveryActionPort.executeRecoveryAction(any())).thenReturn(true);

        listener.onInvoiceSale(new InvoiceSaleEvent(sale()));

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("SALE"),
                eq("Processing error: db down (Recovery action executed)"),
                contains("\"factCode\":1001"),
                eq("Invoice"));
    }

    @Test
    @DisplayName("Recovery fallido - no agrega sufijo")
    void recoveryFallido_sinSufijo() {
        doThrow(new RuntimeException("db down"))
                .when(invoicePersistenceAdapter).saveOrUpdate(any(InvoiceSyncDto.class));
        when(invoiceRecoveryActionPort.canHandle(any())).thenReturn(true);
        when(invoiceRecoveryActionPort.executeRecoveryAction(any())).thenReturn(false);

        listener.onInvoiceSale(new InvoiceSaleEvent(sale()));

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("SALE"),
                eq("Processing error: db down"),
                contains("\"factCode\":1001"),
                eq("Invoice"));
    }

    @Test
    @DisplayName("Recovery no aplicable - no ejecuta la acción ni agrega sufijo")
    void recoveryNoAplicable_noEjecuta() {
        doThrow(new RuntimeException("db down"))
                .when(invoicePersistenceAdapter).saveOrUpdate(any(InvoiceSyncDto.class));
        when(invoiceRecoveryActionPort.canHandle(any())).thenReturn(false);

        listener.onInvoiceSale(new InvoiceSaleEvent(sale()));

        verify(invoiceRecoveryActionPort, never()).executeRecoveryAction(any());
        verify(messageErrorHandlingPort).saveProcessingError(
                eq("SALE"),
                eq("Processing error: db down"),
                contains("\"factCode\":1001"),
                eq("Invoice"));
    }

    @Test
    @DisplayName("Sin puerto de recovery - persiste el error sin sufijo")
    void sinPuertoRecovery_persisteSinSufijo() {
        SpringEventsInvoiceEventListener sinRecovery = new SpringEventsInvoiceEventListener(
                invoicePersistenceAdapter, messageErrorHandlingPort, null);
        doThrow(new RuntimeException("db down"))
                .when(invoicePersistenceAdapter).saveOrUpdate(any(InvoiceSyncDto.class));

        sinRecovery.onInvoiceSale(new InvoiceSaleEvent(sale()));

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("SALE"),
                eq("Processing error: db down"),
                contains("\"factCode\":1001"),
                eq("Invoice"));
    }

    @Test
    @DisplayName("Fallo al persistir el error de procesamiento - no propaga")
    void falloAlPersistirProcesamiento_noPropaga() {
        doThrow(new RuntimeException("db down"))
                .when(invoicePersistenceAdapter).saveOrUpdate(any(InvoiceSyncDto.class));
        doThrow(new RuntimeException("error db down")).when(messageErrorHandlingPort)
                .saveProcessingError(any(), anyString(), anyString(), anyString());

        assertDoesNotThrow(() -> listener.onInvoiceSale(new InvoiceSaleEvent(sale())));
    }

    @Test
    @DisplayName("Fallo al persistir la validación - no propaga")
    void falloAlPersistirValidacion_noPropaga() {
        doThrow(new RuntimeException("db down")).when(messageErrorHandlingPort)
                .saveProcessingError(any(), anyString(), anyString(), anyString());
        InvoiceSaleEvent event = new InvoiceSaleEvent(sale());
        event.getData().setFactCode(null);

        assertDoesNotThrow(() -> listener.onInvoiceSale(event));
    }

    @Test
    @DisplayName("DELETED con error de persistencia - persiste Processing error y lo traga")
    void deletedConError_persisteProcesamiento() {
        doThrow(new RuntimeException("db down")).when(invoicePersistenceAdapter).delete(FACT_CODE);
        EventDto<InvoiceSyncDto> event = new EventDto<>("DELETED", localDto());

        assertDoesNotThrow(() -> ReflectionTestUtils.invokeMethod(listener, "handleInvoiceEvent", event));

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("DELETED"),
                eq("Processing error: db down"),
                contains("\"factCode\":1001"),
                eq("Invoice"));
    }

    private InvoiceSyncDto localDto() {
        InvoiceSyncDto dto = new InvoiceSyncDto();
        dto.setFactCode(FACT_CODE);
        dto.setEntId(ENT_ID);
        dto.setThirdId(THIRD_ID);
        dto.setTotalValue(100000L);
        dto.setTotalPay(40000L);
        dto.setPendingValue(60000L);
        dto.setCreationDate(LocalDate.of(2026, 1, 15));
        dto.setExpirationDate(LocalDate.of(2026, 2, 15));
        dto.setActive(true);
        dto.setAccountingAccount(130505L);
        return dto;
    }

    private ReceiptSalesDtoRequest sale() {
        return ReceiptSalesDtoRequest.builder()
                .factCode(FACT_CODE)
                .entId(ENT_ID)
                .thirdId(THIRD_ID)
                .totalPay(40000L)
                .totalValue(100000L)
                .pendingValue(60000L)
                .creationDate(LocalDate.of(2026, 1, 15))
                .expirationDate(LocalDate.of(2026, 2, 15))
                .active(true)
                .accountingAccount(130505L)
                .build();
    }
}

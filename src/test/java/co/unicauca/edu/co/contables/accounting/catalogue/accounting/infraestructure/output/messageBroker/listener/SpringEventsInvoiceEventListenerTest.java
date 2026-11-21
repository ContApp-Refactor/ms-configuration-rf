package co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.listener;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.input.IInvoiceProcessInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.InvoiceReplica;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.DTO.InvoiceSyncDto;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.adapter.InvoicePersistenceAdapter;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.mapper.IInvoiceEventMapper;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports.IMessageErrorHandlingPort;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.InvoiceSaleEvent;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.ReceiptSalesDtoRequest;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.enums.EventFactureType;

@ExtendWith(MockitoExtension.class)
@DisplayName("catalogue/accounting SpringEventsInvoiceEventListener Unit Tests")
class SpringEventsInvoiceEventListenerTest {

    private static final long FACT_CODE = 1001L;
    private static final String ENT_ID = "ENT-1";
    private static final long THIRD_ID = 55L;

    @Mock
    private InvoicePersistenceAdapter invoicePersistenceAdapter;

    @Mock
    private IMessageErrorHandlingPort messageErrorHandlingPort;

    @Mock
    private IInvoiceProcessInputPort invoiceProcessInputPort;

    @Mock
    private IInvoiceEventMapper invoiceEventMapper;

    private SpringEventsInvoiceEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new SpringEventsInvoiceEventListener(
                invoicePersistenceAdapter, messageErrorHandlingPort,
                invoiceProcessInputPort, invoiceEventMapper);
    }

    @Test
    @DisplayName("SALE válido - traduce Long a BigDecimal, mapea a dominio y procesa")
    void saleValido_traduceMapeaYProcesa() {
        InvoiceReplica replica = new InvoiceReplica();
        replica.setFactCode(String.valueOf(FACT_CODE));
        when(invoiceEventMapper.toDomain(any(InvoiceSyncDto.class))).thenReturn(replica);

        listener.onInvoiceSale(new InvoiceSaleEvent(sale()));

        ArgumentCaptor<InvoiceSyncDto> dtoCaptor = ArgumentCaptor.forClass(InvoiceSyncDto.class);
        verify(invoiceEventMapper).toDomain(dtoCaptor.capture());
        InvoiceSyncDto dto = dtoCaptor.getValue();
        assertEquals(FACT_CODE, dto.getFactCode());
        assertEquals(ENT_ID, dto.getEntId());
        assertEquals(THIRD_ID, dto.getThirdId());
        assertEquals(new BigDecimal("100000"), dto.getTotalValue());
        assertEquals(new BigDecimal("40000"), dto.getTotalPay());
        assertEquals(new BigDecimal("60000"), dto.getPendingValue());
        assertEquals(LocalDate.of(2026, 1, 15), dto.getCreationDate());
        assertEquals(LocalDate.of(2026, 2, 15), dto.getExpirationDate());
        assertEquals(130505L, dto.getAccountingAccount());

        verify(invoiceProcessInputPort).processInvoiceCreation(replica);
        verifyNoInteractions(messageErrorHandlingPort);
    }

    @Test
    @DisplayName("Tipo desconocido - se descarta sin procesar ni persistir error")
    void tipoDesconocido_seDescarta() {
        InvoiceSaleEvent event = new InvoiceSaleEvent(sale());
        event.setType(EventFactureType.PURCHASE);

        listener.onInvoiceSale(event);

        verifyNoInteractions(invoiceEventMapper);
        verifyNoInteractions(invoiceProcessInputPort);
        verifyNoInteractions(invoicePersistenceAdapter);
        verifyNoInteractions(messageErrorHandlingPort);
    }

    @Test
    @DisplayName("Evento nulo - persiste validación con tipo nulo")
    void eventoNulo_persisteValidacion() {
        assertDoesNotThrow(() -> listener.onInvoiceSale(null));

        verify(messageErrorHandlingPort).saveProcessingError(
                isNull(),
                org.mockito.ArgumentMatchers.eq("Validation failed: Event is null"),
                org.mockito.ArgumentMatchers.eq("{\"error\": \"Event is null\"}"),
                org.mockito.ArgumentMatchers.eq("Invoice"));
    }

    @Test
    @DisplayName("Data nula - persiste validación con el tipo del evento")
    void dataNula_persisteValidacion() {
        listener.onInvoiceSale(new InvoiceSaleEvent(null));

        verify(messageErrorHandlingPort).saveProcessingError(
                org.mockito.ArgumentMatchers.eq("SALE"),
                org.mockito.ArgumentMatchers.eq("Validation failed: Event data is null"),
                org.mockito.ArgumentMatchers.eq("{\"error\": \"Event data is null\", \"eventType\": \"SALE\"}"),
                org.mockito.ArgumentMatchers.eq("Invoice"));
    }

    @Test
    @DisplayName("Tipo nulo - persiste validación")
    void tipoNulo_persisteValidacion() {
        InvoiceSaleEvent event = new InvoiceSaleEvent(sale());
        event.setType(null);

        listener.onInvoiceSale(event);

        verify(messageErrorHandlingPort).saveProcessingError(
                isNull(),
                org.mockito.ArgumentMatchers.eq("Validation failed: Event type is null"),
                contains("\"factCode\":1001"),
                org.mockito.ArgumentMatchers.eq("Invoice"));
    }

    @Test
    @DisplayName("factCode nulo - persiste validación")
    void factCodeNulo_persisteValidacion() {
        ReceiptSalesDtoRequest dto = sale();
        dto.setFactCode(null);

        listener.onInvoiceSale(new InvoiceSaleEvent(dto));

        verify(messageErrorHandlingPort).saveProcessingError(
                org.mockito.ArgumentMatchers.eq("SALE"),
                org.mockito.ArgumentMatchers.eq("Validation failed: FactCode is null"),
                contains("\"factCode\":null"),
                org.mockito.ArgumentMatchers.eq("Invoice"));
        verifyNoInteractions(invoiceEventMapper);
    }

    @Test
    @DisplayName("accountingAccount nulo - persiste validación")
    void accountingAccountNulo_persisteValidacion() {
        ReceiptSalesDtoRequest dto = sale();
        dto.setAccountingAccount(null);

        listener.onInvoiceSale(new InvoiceSaleEvent(dto));

        verify(messageErrorHandlingPort).saveProcessingError(
                org.mockito.ArgumentMatchers.eq("SALE"),
                org.mockito.ArgumentMatchers.eq("Validation failed: AccountingAccount is null"),
                contains("\"accountingAccount\":null"),
                org.mockito.ArgumentMatchers.eq("Invoice"));
    }

    @Test
    @DisplayName("entId nulo - persiste validación")
    void entIdNulo_persisteValidacion() {
        ReceiptSalesDtoRequest dto = sale();
        dto.setEntId(null);

        listener.onInvoiceSale(new InvoiceSaleEvent(dto));

        verify(messageErrorHandlingPort).saveProcessingError(
                org.mockito.ArgumentMatchers.eq("SALE"),
                org.mockito.ArgumentMatchers.eq("Validation failed: EntId is null"),
                contains("\"entId\":null"),
                org.mockito.ArgumentMatchers.eq("Invoice"));
    }

    @Test
    @DisplayName("expirationDate nula - persiste validación")
    void expirationDateNula_persisteValidacion() {
        ReceiptSalesDtoRequest dto = sale();
        dto.setExpirationDate(null);

        listener.onInvoiceSale(new InvoiceSaleEvent(dto));

        verify(messageErrorHandlingPort).saveProcessingError(
                org.mockito.ArgumentMatchers.eq("SALE"),
                org.mockito.ArgumentMatchers.eq("Validation failed: ExpirationDate is null"),
                contains("\"expirationDate\":null"),
                org.mockito.ArgumentMatchers.eq("Invoice"));
    }

    @Test
    @DisplayName("pendingValue nulo - persiste validación")
    void pendingValueNulo_persisteValidacion() {
        ReceiptSalesDtoRequest dto = sale();
        dto.setPendingValue(null);

        listener.onInvoiceSale(new InvoiceSaleEvent(dto));

        verify(messageErrorHandlingPort).saveProcessingError(
                org.mockito.ArgumentMatchers.eq("SALE"),
                org.mockito.ArgumentMatchers.eq("Validation failed: PendingValue is null"),
                contains("\"pendingValue\":null"),
                org.mockito.ArgumentMatchers.eq("Invoice"));
    }

    @Test
    @DisplayName("thirdId nulo - persiste validación")
    void thirdIdNulo_persisteValidacion() {
        ReceiptSalesDtoRequest dto = sale();
        dto.setThirdId(null);

        listener.onInvoiceSale(new InvoiceSaleEvent(dto));

        verify(messageErrorHandlingPort).saveProcessingError(
                org.mockito.ArgumentMatchers.eq("SALE"),
                org.mockito.ArgumentMatchers.eq("Validation failed: ThirdId is null"),
                contains("\"thirdId\":null"),
                org.mockito.ArgumentMatchers.eq("Invoice"));
    }

    @Test
    @DisplayName("totalPay nulo - persiste validación")
    void totalPayNulo_persisteValidacion() {
        ReceiptSalesDtoRequest dto = sale();
        dto.setTotalPay(null);

        listener.onInvoiceSale(new InvoiceSaleEvent(dto));

        verify(messageErrorHandlingPort).saveProcessingError(
                org.mockito.ArgumentMatchers.eq("SALE"),
                org.mockito.ArgumentMatchers.eq("Validation failed: TotalPay is null"),
                contains("\"totalPay\":null"),
                org.mockito.ArgumentMatchers.eq("Invoice"));
    }

    @Test
    @DisplayName("totalValue nulo - persiste validación")
    void totalValueNulo_persisteValidacion() {
        ReceiptSalesDtoRequest dto = sale();
        dto.setTotalValue(null);

        listener.onInvoiceSale(new InvoiceSaleEvent(dto));

        verify(messageErrorHandlingPort).saveProcessingError(
                org.mockito.ArgumentMatchers.eq("SALE"),
                org.mockito.ArgumentMatchers.eq("Validation failed: TotalValue is null"),
                contains("\"totalValue\":null"),
                org.mockito.ArgumentMatchers.eq("Invoice"));
    }

    @Test
    @DisplayName("creationDate nula - persiste validación")
    void creationDateNula_persisteValidacion() {
        ReceiptSalesDtoRequest dto = sale();
        dto.setCreationDate(null);

        listener.onInvoiceSale(new InvoiceSaleEvent(dto));

        verify(messageErrorHandlingPort).saveProcessingError(
                org.mockito.ArgumentMatchers.eq("SALE"),
                org.mockito.ArgumentMatchers.eq("Validation failed: CreationDate is null"),
                contains("\"creationDate\":null"),
                org.mockito.ArgumentMatchers.eq("Invoice"));
    }

    @Test
    @DisplayName("Error del caso de uso - persiste procesamiento y no propaga")
    void errorDelCasoDeUso_persisteProcesamientoYNoPropaga() {
        when(invoiceEventMapper.toDomain(any(InvoiceSyncDto.class))).thenReturn(new InvoiceReplica());
        doThrow(new RuntimeException("db down"))
                .when(invoiceProcessInputPort).processInvoiceCreation(any());

        assertDoesNotThrow(() -> listener.onInvoiceSale(new InvoiceSaleEvent(sale())));

        verify(messageErrorHandlingPort).saveProcessingError(
                org.mockito.ArgumentMatchers.eq("SALE"),
                org.mockito.ArgumentMatchers.eq("Processing error: db down"),
                contains("\"factCode\":1001"),
                org.mockito.ArgumentMatchers.eq("Invoice"));
    }

    @Test
    @DisplayName("Error del mapper - persiste procesamiento y no propaga")
    void errorDelMapper_persisteProcesamientoYNoPropaga() {
        when(invoiceEventMapper.toDomain(any(InvoiceSyncDto.class)))
                .thenThrow(new RuntimeException("mapper failure"));

        assertDoesNotThrow(() -> listener.onInvoiceSale(new InvoiceSaleEvent(sale())));

        verify(invoiceProcessInputPort, never()).processInvoiceCreation(any());
        verify(messageErrorHandlingPort).saveProcessingError(
                org.mockito.ArgumentMatchers.eq("SALE"),
                org.mockito.ArgumentMatchers.eq("Processing error: mapper failure"),
                contains("\"factCode\":1001"),
                org.mockito.ArgumentMatchers.eq("Invoice"));
    }

    @Test
    @DisplayName("Fallo al persistir la validación - no propaga")
    void falloAlPersistirValidacion_noPropaga() {
        doThrow(new RuntimeException("db down")).when(messageErrorHandlingPort)
                .saveProcessingError(any(), anyString(), anyString(), anyString());
        ReceiptSalesDtoRequest dto = sale();
        dto.setFactCode(null);

        assertDoesNotThrow(() -> listener.onInvoiceSale(new InvoiceSaleEvent(dto)));
    }

    @Test
    @DisplayName("Fallo al persistir el procesamiento - no propaga")
    void falloAlPersistirProcesamiento_noPropaga() {
        when(invoiceEventMapper.toDomain(any(InvoiceSyncDto.class))).thenReturn(new InvoiceReplica());
        doThrow(new RuntimeException("db down"))
                .when(invoiceProcessInputPort).processInvoiceCreation(any());
        doThrow(new RuntimeException("error db down")).when(messageErrorHandlingPort)
                .saveProcessingError(any(), anyString(), anyString(), anyString());

        assertDoesNotThrow(() -> listener.onInvoiceSale(new InvoiceSaleEvent(sale())));
    }

    @Test
    @DisplayName("Orden de validación - FactCode se evalúa antes que AccountingAccount")
    void ordenDeValidacion_factCodePrimero() {
        ReceiptSalesDtoRequest dto = sale();
        dto.setFactCode(null);
        dto.setAccountingAccount(null);

        listener.onInvoiceSale(new InvoiceSaleEvent(dto));

        verify(messageErrorHandlingPort).saveProcessingError(
                org.mockito.ArgumentMatchers.eq("SALE"),
                org.mockito.ArgumentMatchers.eq("Validation failed: FactCode is null"),
                anyString(),
                org.mockito.ArgumentMatchers.eq("Invoice"));
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

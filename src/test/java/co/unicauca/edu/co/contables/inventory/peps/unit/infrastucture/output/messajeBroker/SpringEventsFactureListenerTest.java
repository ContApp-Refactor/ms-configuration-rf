package co.unicauca.edu.co.contables.inventory.peps.unit.infrastucture.output.messajeBroker;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports.IMessageErrorHandlingPort;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.KardexPurchaseDtoRequest;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.KardexSalesDtoRequest;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.PepsPurchaseEvent;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.PepsSalesEvent;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.enums.EventFactureType;
import co.unicauca.edu.co.contables.inventory.peps.application.ports.input.IKardexCommandPort;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker.SpringEventsFactureListener;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker.dto.KardexRabbitDto;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker.mapper.IKardexRabbitMQMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("peps SpringEventsFactureListener Unit Tests")
class SpringEventsFactureListenerTest {

    @Mock
    private IKardexCommandPort kardexCommandPort;

    @Mock
    private IKardexRabbitMQMapper kardexRabbitMQMapper;

    @Mock
    private IMessageErrorHandlingPort messageErrorHandlingPort;

    private SpringEventsFactureListener listener;

    private Kardex kardex;

    @BeforeEach
    void setUp() {
        listener = new SpringEventsFactureListener(kardexCommandPort, kardexRabbitMQMapper,
                messageErrorHandlingPort);

        Product product = new Product();
        product.setProductId(9001L);

        kardex = new Kardex();
        kardex.setProduct(product);
        kardex.setFactCode("12345");
        kardex.setQuantity(50);
        kardex.setUnitPrice(new BigDecimal("15.75"));
        kardex.setDetails("Compra de prueba");
    }

    @Test
    @DisplayName("PURCHASE - traduce el DTO y registra la compra")
    void purchase_traduceYRegistra() {
        when(kardexRabbitMQMapper.toDomain(any(KardexRabbitDto.class))).thenReturn(kardex);

        listener.onPepsPurchaseEvent(purchaseEvent(EventFactureType.PURCHASE));

        KardexRabbitDto data = capturedKardexDto();
        assertEquals(50L, data.getQuantity());
        assertEquals("12345", data.getFactCode());
        assertEquals(new BigDecimal("15.75"), data.getUnitPrice());
        assertEquals(9001L, data.getProductId());
        assertEquals("Compra de prueba", data.getDetails());

        verify(kardexCommandPort).registerPurchase(kardex);
        verifyNoInteractions(messageErrorHandlingPort);
    }

    @Test
    @DisplayName("SALE - traduce el DTO de venta sin unitPrice y registra la venta")
    void sale_traduceYRegistra() {
        when(kardexRabbitMQMapper.toDomain(any(KardexRabbitDto.class))).thenReturn(kardex);

        listener.onPepsSalesEvent(salesEvent(EventFactureType.SALE));

        KardexRabbitDto data = capturedKardexDto();
        assertEquals(10L, data.getQuantity());
        assertEquals("12345", data.getFactCode());
        assertNull(data.getUnitPrice());
        assertEquals(9001L, data.getProductId());

        verify(kardexCommandPort).registerSale(kardex);
        verifyNoInteractions(messageErrorHandlingPort);
    }

    @Test
    @DisplayName("RETURNONSALE - registra la devolución de venta")
    void returnOnSale_registra() {
        when(kardexRabbitMQMapper.toDomain(any(KardexRabbitDto.class))).thenReturn(kardex);

        listener.onPepsSalesEvent(salesEvent(EventFactureType.RETURNONSALE));

        verify(kardexCommandPort).registerSaleReturn(kardex);
    }

    @Test
    @DisplayName("RETURNONPURCHASE - registra la devolución de compra")
    void returnOnPurchase_registra() {
        when(kardexRabbitMQMapper.toDomain(any(KardexRabbitDto.class))).thenReturn(kardex);

        listener.onPepsSalesEvent(salesEvent(EventFactureType.RETURNONPURCHASE));

        verify(kardexCommandPort).registerPurchaseReturn(kardex);
    }

    @Test
    @DisplayName("NONCOMMERCIALENTRY - registra la entrada no comercial")
    void nonCommercialEntry_registra() {
        when(kardexRabbitMQMapper.toDomain(any(KardexRabbitDto.class))).thenReturn(kardex);

        listener.onPepsPurchaseEvent(purchaseEvent(EventFactureType.NONCOMMERCIALENTRY));

        verify(kardexCommandPort).registerNonCommercialEntry(kardex);
    }

    @Test
    @DisplayName("NONCOMMERCIALEXIT - registra la salida no comercial")
    void nonCommercialExit_registra() {
        when(kardexRabbitMQMapper.toDomain(any(KardexRabbitDto.class))).thenReturn(kardex);

        listener.onPepsSalesEvent(salesEvent(EventFactureType.NONCOMMERCIALEXIT));

        verify(kardexCommandPort).registerNonCommercialExit(kardex);
    }

    @Test
    @DisplayName("Evento nulo - persiste error de validación y no procesa")
    void eventoNulo_persisteError() {
        listener.onPepsPurchaseEvent(null);

        verify(messageErrorHandlingPort).saveProcessingError(
                isNull(),
                eq("Validation failed: Event is null"),
                eq("{\"error\": \"Event is null\"}"),
                eq("Kardex"));
        verifyNoInteractions(kardexRabbitMQMapper);
        verifyNoInteractions(kardexCommandPort);
    }

    @Test
    @DisplayName("Tipo nulo - persiste error de validación")
    void tipoNulo_persisteError() {
        listener.onPepsPurchaseEvent(purchaseEvent(null));

        verify(messageErrorHandlingPort).saveProcessingError(
                isNull(),
                eq("Validation failed: Event type is null"),
                anyString(),
                eq("Kardex"));
        verifyNoInteractions(kardexCommandPort);
    }

    @Test
    @DisplayName("Datos nulos - persiste error de validación con el tipo de evento")
    void datosNulos_persisteError() {
        listener.onPepsPurchaseEvent(new PepsPurchaseEvent(null, EventFactureType.PURCHASE));

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("PURCHASE"),
                eq("Validation failed: Event data is null"),
                contains("\"eventType\": \"PURCHASE\""),
                eq("Kardex"));
        verifyNoInteractions(kardexRabbitMQMapper);
    }

    @Test
    @DisplayName("Cantidad cero (campo ausente en el JSON) - persiste error de validación")
    void cantidadCero_persisteError() {
        PepsPurchaseEvent event = purchaseEvent(EventFactureType.PURCHASE);
        event.getData().setQuantity(0);

        listener.onPepsPurchaseEvent(event);

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("PURCHASE"),
                contains("quantity"),
                contains("\"quantity\":0"),
                eq("Kardex"));
        verifyNoInteractions(kardexCommandPort);
    }

    @Test
    @DisplayName("FactCode nulo - persiste error de validación")
    void factCodeNulo_persisteError() {
        PepsPurchaseEvent event = purchaseEvent(EventFactureType.PURCHASE);
        event.getData().setFactCode(null);

        listener.onPepsPurchaseEvent(event);

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("PURCHASE"),
                contains("factCode"),
                anyString(),
                eq("Kardex"));
        verifyNoInteractions(kardexCommandPort);
    }

    @Test
    @DisplayName("ProductId nulo - persiste error de validación")
    void productIdNulo_persisteError() {
        PepsSalesEvent event = salesEvent(EventFactureType.SALE);
        event.getData().setProductId(null);

        listener.onPepsSalesEvent(event);

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("SALE"),
                contains("productId"),
                anyString(),
                eq("Kardex"));
        verifyNoInteractions(kardexCommandPort);
    }

    @Test
    @DisplayName("PURCHASE sin unitPrice - persiste error de validación")
    void purchaseSinUnitPrice_persisteError() {
        PepsPurchaseEvent event = purchaseEvent(EventFactureType.PURCHASE);
        event.getData().setUnitPrice(null);

        listener.onPepsPurchaseEvent(event);

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("PURCHASE"),
                contains("unitPrice"),
                anyString(),
                eq("Kardex"));
        verifyNoInteractions(kardexCommandPort);
    }

    @Test
    @DisplayName("PURCHASE con unitPrice cero - persiste error de validación")
    void purchaseConUnitPriceCero_persisteError() {
        PepsPurchaseEvent event = purchaseEvent(EventFactureType.PURCHASE);
        event.getData().setUnitPrice(BigDecimal.ZERO);

        listener.onPepsPurchaseEvent(event);

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("PURCHASE"),
                contains("unitPrice"),
                anyString(),
                eq("Kardex"));
        verifyNoInteractions(kardexCommandPort);
    }

    @Test
    @DisplayName("SALE sin unitPrice - es válida y se procesa")
    void saleSinUnitPrice_esValida() {
        when(kardexRabbitMQMapper.toDomain(any(KardexRabbitDto.class))).thenReturn(kardex);

        listener.onPepsSalesEvent(salesEvent(EventFactureType.SALE));

        verify(kardexCommandPort).registerSale(kardex);
        verifyNoInteractions(messageErrorHandlingPort);
    }

    @Test
    @DisplayName("Error del mapper - persiste error de procesamiento y no propaga")
    void errorDelMapper_persisteError() {
        when(kardexRabbitMQMapper.toDomain(any(KardexRabbitDto.class)))
                .thenThrow(new RuntimeException("Mapping error"));

        assertDoesNotThrow(() -> listener.onPepsPurchaseEvent(purchaseEvent(EventFactureType.PURCHASE)));

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("PURCHASE"),
                contains("Processing error: Mapping error"),
                anyString(),
                eq("Kardex"));
        verifyNoInteractions(kardexCommandPort);
    }

    @Test
    @DisplayName("Error del comando - persiste error de procesamiento y no propaga")
    void errorDelComando_persisteError() {
        when(kardexRabbitMQMapper.toDomain(any(KardexRabbitDto.class))).thenReturn(kardex);
        when(kardexCommandPort.registerPurchase(any(Kardex.class)))
                .thenThrow(new RuntimeException("Database connection error"));

        assertDoesNotThrow(() -> listener.onPepsPurchaseEvent(purchaseEvent(EventFactureType.PURCHASE)));

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("PURCHASE"),
                contains("Processing error: Database connection error"),
                anyString(),
                eq("Kardex"));
    }

    @Test
    @DisplayName("Fallo al persistir el error - no propaga")
    void falloAlPersistirError_noPropaga() {
        doThrow(new RuntimeException("error db")).when(messageErrorHandlingPort)
                .saveProcessingError(any(), anyString(), anyString(), anyString());

        assertDoesNotThrow(() -> listener.onPepsPurchaseEvent(
                new PepsPurchaseEvent(null, EventFactureType.PURCHASE)));

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("PURCHASE"),
                eq("Validation failed: Event data is null"),
                anyString(),
                eq("Kardex"));
        verify(kardexCommandPort, never()).registerPurchase(any());
    }

    private PepsPurchaseEvent purchaseEvent(EventFactureType type) {
        return new PepsPurchaseEvent(new KardexPurchaseDtoRequest(
                50, 12345L, new BigDecimal("15.75"), 9001L, "Compra de prueba"), type);
    }

    private PepsSalesEvent salesEvent(EventFactureType type) {
        return new PepsSalesEvent(new KardexSalesDtoRequest(
                10, 12345L, 9001L, "Venta de prueba"), type);
    }

    private KardexRabbitDto capturedKardexDto() {
        ArgumentCaptor<KardexRabbitDto> captor = ArgumentCaptor.forClass(KardexRabbitDto.class);
        verify(kardexRabbitMQMapper).toDomain(captor.capture());
        return captor.getValue();
    }
}

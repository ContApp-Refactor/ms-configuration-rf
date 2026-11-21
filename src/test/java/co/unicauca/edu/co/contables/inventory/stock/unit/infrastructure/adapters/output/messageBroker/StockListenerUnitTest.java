package co.unicauca.edu.co.contables.inventory.stock.unit.infrastructure.adapters.output.messageBroker;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;

import com.rabbitmq.client.Channel;

import co.unicauca.edu.co.contables.inventory.stock.domain.model.Stock;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IStockCommandRepositoryPort;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.messageBroker.StockListener;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.messageBroker.dto.EventDto;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.messageBroker.dto.ProductAsyncDto;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.messageBroker.enums.EventStockType;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.messageBroker.mapper.IProductBrokerMapper;

@ExtendWith(MockitoExtension.class)
class StockListenerUnitTest {

    @Mock
    private IStockCommandRepositoryPort stockCommandPort;

    @Mock
    private IProductBrokerMapper productBrokerMapper;

    @Mock
    private Channel channel;

    @InjectMocks
    private StockListener stockListener;

    private ProductAsyncDto productAsyncDto;
    private EventDto<ProductAsyncDto, EventStockType> event;
    private Stock stock;

    @BeforeEach
    void setUp() {
        productAsyncDto = new ProductAsyncDto(1L, "Test Product", "ENT-123");
        event = new EventDto<>(EventStockType.CREATED, productAsyncDto);

        stock = new Stock();
        stock.setProductId(1L);
        stock.setName("Test Product");
        stock.setEnterpriseId("ENT-123");
    }

    @Test
    @DisplayName("CREATED - mapea y guarda el stock")
    void created_guardaStock() throws IOException {
        when(productBrokerMapper.toDomain(productAsyncDto)).thenReturn(stock);
        when(stockCommandPort.save(stock)).thenReturn(stock);

        stockListener.handleStockEvent(event, new Message(new byte[0]), channel, 1L);

        verify(stockCommandPort, times(1)).save(stock);
        verify(channel, times(1)).basicAck(1L, false);
    }

    @Test
    @DisplayName("UPDATED - actualiza por productId y nombre")
    void updated_actualizaStock() throws IOException {
        event.setType(EventStockType.UPDATED);
        when(productBrokerMapper.toDomain(productAsyncDto)).thenReturn(stock);
        when(stockCommandPort.update(1L, "Test Product")).thenReturn(stock);

        stockListener.handleStockEvent(event, new Message(new byte[0]), channel, 2L);

        verify(stockCommandPort, times(1)).update(1L, "Test Product");
        verify(channel, times(1)).basicAck(2L, false);
    }

    @Test
    @DisplayName("DELETED - elimina por productId")
    void deleted_eliminaStock() throws IOException {
        event.setType(EventStockType.DELETED);

        stockListener.handleStockEvent(event, new Message(new byte[0]), channel, 3L);

        verify(stockCommandPort, times(1)).deleteByProductId(1L);
        verify(channel, times(1)).basicAck(3L, false);
    }

    @Test
    @DisplayName("Evento nulo - descarta, hace ACK y no lanza")
    void eventoNulo_descarta() throws IOException {
        assertDoesNotThrow(() ->
                stockListener.handleStockEvent(null, new Message(new byte[0]), channel, 4L));

        verify(stockCommandPort, never()).save(any());
        verify(channel, times(1)).basicAck(4L, false);
    }

    @Test
    @DisplayName("Tipo nulo - descarta y hace ACK")
    void tipoNulo_descarta() throws IOException {
        event.setType(null);

        stockListener.handleStockEvent(event, new Message(new byte[0]), channel, 5L);

        verify(stockCommandPort, never()).save(any());
        verify(channel, times(1)).basicAck(5L, false);
    }

    @Test
    @DisplayName("Datos nulos - descarta y hace ACK")
    void datosNulos_descarta() throws IOException {
        event.setData(null);

        stockListener.handleStockEvent(event, new Message(new byte[0]), channel, 6L);

        verify(stockCommandPort, never()).save(any());
        verify(channel, times(1)).basicAck(6L, false);
    }

    @Test
    @DisplayName("productId nulo - descarta y hace ACK")
    void productIdNulo_descarta() throws IOException {
        productAsyncDto.setProductId(null);

        stockListener.handleStockEvent(event, new Message(new byte[0]), channel, 7L);

        verify(stockCommandPort, never()).save(any());
        verify(channel, times(1)).basicAck(7L, false);
    }

    @Test
    @DisplayName("enterpriseId nulo - descarta y hace ACK")
    void enterpriseIdNulo_descarta() throws IOException {
        productAsyncDto.setEnterpriseId(null);

        stockListener.handleStockEvent(event, new Message(new byte[0]), channel, 8L);

        verify(stockCommandPort, never()).save(any());
        verify(channel, times(1)).basicAck(8L, false);
    }

    @Test
    @DisplayName("enterpriseId vacío - descarta y hace ACK")
    void enterpriseIdVacio_descarta() throws IOException {
        productAsyncDto.setEnterpriseId("");

        stockListener.handleStockEvent(event, new Message(new byte[0]), channel, 9L);

        verify(stockCommandPort, never()).save(any());
        verify(channel, times(1)).basicAck(9L, false);
    }

    @Test
    @DisplayName("Nombre nulo - descarta y hace ACK")
    void nombreNulo_descarta() throws IOException {
        productAsyncDto.setName(null);

        stockListener.handleStockEvent(event, new Message(new byte[0]), channel, 10L);

        verify(stockCommandPort, never()).save(any());
        verify(channel, times(1)).basicAck(10L, false);
    }

    @Test
    @DisplayName("Nombre vacío - descarta y hace ACK")
    void nombreVacio_descarta() throws IOException {
        productAsyncDto.setName("");

        stockListener.handleStockEvent(event, new Message(new byte[0]), channel, 11L);

        verify(stockCommandPort, never()).save(any());
        verify(channel, times(1)).basicAck(11L, false);
    }

    @Test
    @DisplayName("Fallo de persistencia - se traga y hace ACK")
    void falloPersistencia_seTraga() throws IOException {
        when(productBrokerMapper.toDomain(productAsyncDto)).thenReturn(stock);
        when(stockCommandPort.save(stock)).thenThrow(new RuntimeException("Database down"));

        assertDoesNotThrow(() ->
                stockListener.handleStockEvent(event, new Message(new byte[0]), channel, 12L));

        verify(channel, times(1)).basicAck(12L, false);
    }

    @Test
    @DisplayName("Fallo del mapper - se traga y hace ACK")
    void falloMapper_seTraga() throws IOException {
        when(productBrokerMapper.toDomain(productAsyncDto)).thenThrow(new RuntimeException("Mapping error"));

        assertDoesNotThrow(() ->
                stockListener.handleStockEvent(event, new Message(new byte[0]), channel, 13L));

        verify(stockCommandPort, never()).save(any());
        verify(channel, times(1)).basicAck(13L, false);
    }

    @Test
    @DisplayName("Fallo del ACK - no propaga")
    void falloAck_noPropaga() throws IOException {
        when(productBrokerMapper.toDomain(productAsyncDto)).thenReturn(stock);
        when(stockCommandPort.save(stock)).thenReturn(stock);
        org.mockito.Mockito.doThrow(new IOException("channel closed"))
                .when(channel).basicAck(anyLong(), eq(false));

        assertDoesNotThrow(() ->
                stockListener.handleStockEvent(event, new Message(new byte[0]), channel, 14L));
    }
}

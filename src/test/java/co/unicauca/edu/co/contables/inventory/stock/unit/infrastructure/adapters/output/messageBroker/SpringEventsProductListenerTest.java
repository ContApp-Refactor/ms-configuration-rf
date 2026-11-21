package co.unicauca.edu.co.contables.inventory.stock.unit.infrastructure.adapters.output.messageBroker;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.unicauca.edu.co.contables.inventory.products.application.dto.ProductSyncDto;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.messageBroker.enums.EventType;
import co.unicauca.edu.co.contables.inventory.stock.domain.model.Stock;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IStockCommandRepositoryPort;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.messageBroker.SpringEventsProductListener;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.messageBroker.dto.ProductAsyncDto;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.messageBroker.mapper.IProductBrokerMapper;

@ExtendWith(MockitoExtension.class)
class SpringEventsProductListenerTest {

    @Mock
    private IStockCommandRepositoryPort stockCommandPort;

    @Mock
    private IProductBrokerMapper productBrokerMapper;

    @InjectMocks
    private SpringEventsProductListener listener;

    private ProductSyncDto productSyncDto;
    private co.unicauca.edu.co.contables.inventory.products.infraestructure.output.messageBroker.dto.EventDto<ProductSyncDto, EventType> event;
    private Stock stock;

    @BeforeEach
    void setUp() {
        productSyncDto = new ProductSyncDto(1L, "Test Product", "REF-001", "ENT-123",
                "Box of 10", 5, 12.5, true);

        event = new co.unicauca.edu.co.contables.inventory.products.infraestructure.output.messageBroker.dto.EventDto<>(
                productSyncDto, EventType.CREATED);

        stock = new Stock();
        stock.setProductId(1L);
        stock.setName("Test Product");
        stock.setEnterpriseId("ENT-123");
    }

    @Test
    @DisplayName("CREATED - traduce y guarda el stock")
    void created_guardaStock() {
        when(productBrokerMapper.toDomain(any(ProductAsyncDto.class))).thenReturn(stock);
        when(stockCommandPort.save(stock)).thenReturn(stock);

        listener.onProductEvent(event);

        verify(stockCommandPort).save(stock);
    }

    @Test
    @DisplayName("CREATED - solo traduce productId, name y enterpriseId")
    void created_traduceSoloCamposDeStock() {
        when(productBrokerMapper.toDomain(any(ProductAsyncDto.class))).thenReturn(stock);
        when(stockCommandPort.save(stock)).thenReturn(stock);

        listener.onProductEvent(event);

        ArgumentCaptor<ProductAsyncDto> captor = ArgumentCaptor.forClass(ProductAsyncDto.class);
        verify(productBrokerMapper).toDomain(captor.capture());
        ProductAsyncDto mapped = captor.getValue();
        assertEquals(1L, mapped.getProductId());
        assertEquals("Test Product", mapped.getName());
        assertEquals("ENT-123", mapped.getEnterpriseId());
    }

    @Test
    @DisplayName("UPDATED - actualiza por productId y nombre")
    void updated_actualizaStock() {
        event.setType(EventType.UPDATED);
        when(productBrokerMapper.toDomain(any(ProductAsyncDto.class))).thenReturn(stock);
        when(stockCommandPort.update(1L, "Test Product")).thenReturn(stock);

        listener.onProductEvent(event);

        verify(stockCommandPort).update(1L, "Test Product");
        verify(stockCommandPort, never()).save(any());
    }

    @Test
    @DisplayName("DELETED - elimina por productId sin mapear")
    void deleted_eliminaStock() {
        event.setType(EventType.DELETED);

        listener.onProductEvent(event);

        verify(stockCommandPort).deleteByProductId(1L);
        verify(productBrokerMapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("Evento nulo - descarta sin lanzar")
    void eventoNulo_descarta() {
        assertDoesNotThrow(() -> listener.onProductEvent(null));

        verify(stockCommandPort, never()).save(any());
    }

    @Test
    @DisplayName("Tipo nulo - descarta sin lanzar")
    void tipoNulo_descarta() {
        event.setType(null);

        listener.onProductEvent(event);

        verify(stockCommandPort, never()).save(any());
    }

    @Test
    @DisplayName("Datos nulos - descarta sin lanzar")
    void datosNulos_descarta() {
        event.setData(null);

        listener.onProductEvent(event);

        verify(stockCommandPort, never()).save(any());
    }

    @Test
    @DisplayName("productId nulo - descarta sin lanzar")
    void productIdNulo_descarta() {
        productSyncDto.setProductId(null);

        listener.onProductEvent(event);

        verify(stockCommandPort, never()).save(any());
    }

    @Test
    @DisplayName("enterpriseId nulo - descarta sin lanzar")
    void enterpriseIdNulo_descarta() {
        productSyncDto.setEnterpriseId(null);

        listener.onProductEvent(event);

        verify(stockCommandPort, never()).save(any());
    }

    @Test
    @DisplayName("enterpriseId vacío - descarta sin lanzar")
    void enterpriseIdVacio_descarta() {
        productSyncDto.setEnterpriseId("");

        listener.onProductEvent(event);

        verify(stockCommandPort, never()).save(any());
    }

    @Test
    @DisplayName("Nombre nulo - descarta sin lanzar")
    void nombreNulo_descarta() {
        productSyncDto.setName(null);

        listener.onProductEvent(event);

        verify(stockCommandPort, never()).save(any());
    }

    @Test
    @DisplayName("Nombre vacío - descarta sin lanzar")
    void nombreVacio_descarta() {
        productSyncDto.setName("");

        listener.onProductEvent(event);

        verify(stockCommandPort, never()).save(any());
    }

    @Test
    @DisplayName("Nombre en blanco - pasa la validación como en el listener Rabbit (isEmpty)")
    void nombreEnBlanco_procesa() {
        productSyncDto.setName("   ");
        when(productBrokerMapper.toDomain(any(ProductAsyncDto.class))).thenReturn(stock);
        when(stockCommandPort.save(stock)).thenReturn(stock);

        listener.onProductEvent(event);

        verify(stockCommandPort).save(stock);
    }

    @Test
    @DisplayName("Fallo de persistencia - se traga sin propagar")
    void falloPersistencia_seTraga() {
        when(productBrokerMapper.toDomain(any(ProductAsyncDto.class))).thenReturn(stock);
        when(stockCommandPort.save(stock)).thenThrow(new RuntimeException("Database down"));

        assertDoesNotThrow(() -> listener.onProductEvent(event));
    }

    @Test
    @DisplayName("Fallo del mapper - se traga sin propagar")
    void falloMapper_seTraga() {
        when(productBrokerMapper.toDomain(any(ProductAsyncDto.class)))
                .thenThrow(new RuntimeException("Mapping error"));

        assertDoesNotThrow(() -> listener.onProductEvent(event));

        verify(stockCommandPort, never()).save(any());
    }

    @Test
    @DisplayName("Secuencia CREATED/UPDATED/DELETED - procesa cada tipo")
    void secuencia_procesaCadaTipo() {
        when(productBrokerMapper.toDomain(any(ProductAsyncDto.class))).thenReturn(stock);
        when(stockCommandPort.save(stock)).thenReturn(stock);
        when(stockCommandPort.update(1L, "Test Product")).thenReturn(stock);
        when(stockCommandPort.deleteByProductId(1L)).thenReturn(1);

        listener.onProductEvent(event);
        event.setType(EventType.UPDATED);
        listener.onProductEvent(event);
        event.setType(EventType.DELETED);
        listener.onProductEvent(event);

        verify(stockCommandPort, times(1)).save(stock);
        verify(stockCommandPort, times(1)).update(1L, "Test Product");
        verify(stockCommandPort, times(1)).deleteByProductId(1L);
    }
}

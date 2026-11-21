package co.unicauca.edu.co.contables.inventory.kardex.unit.infrastructure.adapters.output.messageBroker;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
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

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.ports.IMessageErrorHandlingPort;
import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.kardex.domain.port.product.IProductCommandRepositoryPort;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.messageBroker.SpringEventsProductListener;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.messageBroker.dto.EventDto;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.messageBroker.dto.ProductAsyncDto;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.messageBroker.enums.EventProductType;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.messageBroker.mapper.ProductBrokerMapper;
import co.unicauca.edu.co.contables.inventory.products.application.dto.ProductSyncDto;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.messageBroker.enums.EventType;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IEventRecoveryActionPort;

@ExtendWith(MockitoExtension.class)
class SpringEventsProductListenerTest {

    @Mock
    private IProductCommandRepositoryPort productCommandPort;

    @Mock
    private ProductBrokerMapper productBrokerMapper;

    @Mock
    private IMessageErrorHandlingPort messageErrorHandlingPort;

    @Mock
    private IEventRecoveryActionPort<EventDto<ProductAsyncDto, EventProductType>> productRecoveryActionPort;

    @InjectMocks
    private SpringEventsProductListener listener;

    private ProductSyncDto productSyncDto;
    private co.unicauca.edu.co.contables.inventory.products.infraestructure.output.messageBroker.dto.EventDto<ProductSyncDto, EventType> event;
    private Product product;

    @BeforeEach
    void setUp() {
        productSyncDto = new ProductSyncDto(1L, "Test Product", "REF-001", "ENT-123",
                "Box of 10", 5, 12.5, true);

        event = new co.unicauca.edu.co.contables.inventory.products.infraestructure.output.messageBroker.dto.EventDto<>(
                productSyncDto, EventType.CREATED);

        product = new Product();
        product.setProductId(1L);
        product.setName("Test Product");
        product.setReference("REF-001");
        product.setEnterpriseId("ENT-123");
        product.setPresentation("Box of 10");
    }

    @Test
    @DisplayName("CREATED - mapea y guarda el producto")
    void created_guardaProducto() {
        when(productBrokerMapper.toDomain(any(ProductAsyncDto.class))).thenReturn(product);
        when(productCommandPort.save(product)).thenReturn("Product saved successfully");

        listener.onProductEvent(event);

        verify(productBrokerMapper).toDomain(any(ProductAsyncDto.class));
        verify(productCommandPort).save(product);
        verify(messageErrorHandlingPort, never()).saveProcessingError(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("CREATED - traduce todos los campos del contrato de products")
    void created_traduceCampos() {
        when(productBrokerMapper.toDomain(any(ProductAsyncDto.class))).thenReturn(product);
        when(productCommandPort.save(product)).thenReturn("saved");

        listener.onProductEvent(event);

        ArgumentCaptor<ProductAsyncDto> captor = ArgumentCaptor.forClass(ProductAsyncDto.class);
        verify(productBrokerMapper).toDomain(captor.capture());
        ProductAsyncDto mapped = captor.getValue();
        assertEquals(1L, mapped.getProductId());
        assertEquals("Test Product", mapped.getName());
        assertEquals("REF-001", mapped.getReference());
        assertEquals("ENT-123", mapped.getEnterpriseId());
        assertEquals("Box of 10", mapped.getPresentation());
        assertTrue(mapped.isState());
    }

    @Test
    @DisplayName("UPDATED - mapea y actualiza el producto")
    void updated_actualizaProducto() {
        event.setType(EventType.UPDATED);
        when(productBrokerMapper.toDomain(any(ProductAsyncDto.class))).thenReturn(product);
        when(productCommandPort.update(product)).thenReturn("Product updated successfully");

        listener.onProductEvent(event);

        verify(productCommandPort).update(product);
        verify(messageErrorHandlingPort, never()).saveProcessingError(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("DELETED - elimina por productId sin mapear")
    void deleted_eliminaProducto() {
        event.setType(EventType.DELETED);

        listener.onProductEvent(event);

        verify(productCommandPort).delete(1L);
        verify(productBrokerMapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("Evento nulo - persiste el error de validación y no lanza")
    void eventoNulo_persisteError() {
        assertDoesNotThrow(() -> listener.onProductEvent(null));

        verify(messageErrorHandlingPort).saveProcessingError(
                isNull(),
                contains("Validation failed: Event is null"),
                eq("{\"error\": \"Event is null\"}"),
                eq("Product"));
    }

    @Test
    @DisplayName("Tipo nulo - persiste el error de validación")
    void tipoNulo_persisteError() {
        event.setType(null);

        listener.onProductEvent(event);

        verify(messageErrorHandlingPort).saveProcessingError(
                isNull(),
                contains("Validation failed: Event type is null"),
                anyString(),
                eq("Product"));
    }

    @Test
    @DisplayName("Datos nulos - persiste el error de validación con el tipo")
    void datosNulos_persisteError() {
        event.setData(null);

        listener.onProductEvent(event);

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("CREATED"),
                contains("Validation failed: Event data is null"),
                contains("\"eventType\": \"CREATED\""),
                eq("Product"));
    }

    @Test
    @DisplayName("productId nulo - persiste el error de validación")
    void productIdNulo_persisteError() {
        productSyncDto.setProductId(null);

        listener.onProductEvent(event);

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("CREATED"),
                contains("Validation failed: Missing required field: productId"),
                anyString(),
                eq("Product"));
    }

    @Test
    @DisplayName("Nombre nulo - persiste el error de validación")
    void nombreNulo_persisteError() {
        productSyncDto.setName(null);

        listener.onProductEvent(event);

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("CREATED"),
                contains("Validation failed: Name is null or empty"),
                anyString(),
                eq("Product"));
    }

    @Test
    @DisplayName("Nombre en blanco - persiste el error de validación")
    void nombreBlanco_persisteError() {
        productSyncDto.setName("   ");

        listener.onProductEvent(event);

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("CREATED"),
                contains("Name is null or empty"),
                anyString(),
                eq("Product"));
    }

    @Test
    @DisplayName("Referencia nula - persiste el error de validación")
    void referenciaNula_persisteError() {
        productSyncDto.setReference(null);

        listener.onProductEvent(event);

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("CREATED"),
                contains("Reference is null or empty"),
                anyString(),
                eq("Product"));
    }

    @Test
    @DisplayName("Referencia vacía - persiste el error de validación")
    void referenciaVacia_persisteError() {
        productSyncDto.setReference("");

        listener.onProductEvent(event);

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("CREATED"),
                contains("Reference is null or empty"),
                anyString(),
                eq("Product"));
    }

    @Test
    @DisplayName("enterpriseId nulo - persiste el error de validación")
    void enterpriseIdNulo_persisteError() {
        productSyncDto.setEnterpriseId(null);

        listener.onProductEvent(event);

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("CREATED"),
                contains("EnterpriseId is null or empty"),
                anyString(),
                eq("Product"));
    }

    @Test
    @DisplayName("enterpriseId en blanco - persiste el error de validación")
    void enterpriseIdBlanco_persisteError() {
        productSyncDto.setEnterpriseId("  ");

        listener.onProductEvent(event);

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("CREATED"),
                contains("EnterpriseId is null or empty"),
                anyString(),
                eq("Product"));
    }

    @Test
    @DisplayName("presentation nula - es opcional y procesa")
    void presentationNula_procesa() {
        productSyncDto.setPresentation(null);
        when(productBrokerMapper.toDomain(any(ProductAsyncDto.class))).thenReturn(product);
        when(productCommandPort.save(product)).thenReturn("saved");

        listener.onProductEvent(event);

        verify(productCommandPort).save(product);
    }

    @Test
    @DisplayName("Fallo de persistencia - persiste el error y no propaga")
    void falloPersistencia_persisteError() {
        when(productBrokerMapper.toDomain(any(ProductAsyncDto.class))).thenReturn(product);
        when(productCommandPort.save(product)).thenThrow(new RuntimeException("Database connection error"));

        assertDoesNotThrow(() -> listener.onProductEvent(event));

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("CREATED"),
                contains("Processing error: Database connection error"),
                anyString(),
                eq("Product"));
    }

    @Test
    @DisplayName("Recuperación exitosa - anota la ejecución en el error")
    void recuperacionExitosa_anotaError() {
        when(productBrokerMapper.toDomain(any(ProductAsyncDto.class))).thenReturn(product);
        when(productCommandPort.save(product)).thenThrow(new RuntimeException("Duplicate product"));
        when(productRecoveryActionPort.canHandle(any())).thenReturn(true);
        when(productRecoveryActionPort.executeRecoveryAction(any())).thenReturn(true);

        listener.onProductEvent(event);

        verify(productRecoveryActionPort).canHandle(any(EventDto.class));
        verify(productRecoveryActionPort).executeRecoveryAction(any(EventDto.class));
        verify(messageErrorHandlingPort).saveProcessingError(
                eq("CREATED"),
                contains("Recovery action executed"),
                anyString(),
                eq("Product"));
    }

    @Test
    @DisplayName("Recuperación fallida - no anota ejecución")
    void recuperacionFallida_noAnota() {
        when(productBrokerMapper.toDomain(any(ProductAsyncDto.class))).thenReturn(product);
        when(productCommandPort.save(product)).thenThrow(new RuntimeException("Database error"));
        when(productRecoveryActionPort.canHandle(any())).thenReturn(true);
        when(productRecoveryActionPort.executeRecoveryAction(any())).thenReturn(false);

        listener.onProductEvent(event);

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("CREATED"),
                org.mockito.ArgumentMatchers.argThat(description ->
                        description.startsWith("Processing error") && !description.contains("Recovery action executed")),
                anyString(),
                eq("Product"));
    }

    @Test
    @DisplayName("Recuperación con excepción - se registra y no propaga")
    void recuperacionConExcepcion_noPropaga() {
        when(productBrokerMapper.toDomain(any(ProductAsyncDto.class))).thenReturn(product);
        when(productCommandPort.save(product)).thenThrow(new RuntimeException("Database error"));
        when(productRecoveryActionPort.canHandle(any())).thenReturn(true);
        when(productRecoveryActionPort.executeRecoveryAction(any())).thenThrow(new RuntimeException("Recovery failed"));

        assertDoesNotThrow(() -> listener.onProductEvent(event));

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("CREATED"),
                contains("Processing error"),
                anyString(),
                eq("Product"));
    }

    @Test
    @DisplayName("Fallo del mapper - persiste el error y no guarda")
    void falloMapper_persisteError() {
        when(productBrokerMapper.toDomain(any(ProductAsyncDto.class)))
                .thenThrow(new RuntimeException("Mapping error"));

        listener.onProductEvent(event);

        verify(messageErrorHandlingPort).saveProcessingError(
                eq("CREATED"),
                contains("Processing error: Mapping error"),
                anyString(),
                eq("Product"));
        verify(productCommandPort, never()).save(any());
    }

    @Test
    @DisplayName("Secuencia CREATED/UPDATED/DELETED - procesa cada tipo")
    void secuencia_procesaCadaTipo() {
        when(productBrokerMapper.toDomain(any(ProductAsyncDto.class))).thenReturn(product);
        when(productCommandPort.save(product)).thenReturn("saved");
        when(productCommandPort.update(product)).thenReturn("updated");
        when(productCommandPort.delete(1L)).thenReturn("deleted");

        listener.onProductEvent(event);
        event.setType(EventType.UPDATED);
        listener.onProductEvent(event);
        event.setType(EventType.DELETED);
        listener.onProductEvent(event);

        verify(productCommandPort, times(1)).save(product);
        verify(productCommandPort, times(1)).update(product);
        verify(productCommandPort, times(1)).delete(1L);
    }

    @Test
    @DisplayName("Fallo al guardar el error - no propaga")
    void falloAlGuardarError_noPropaga() {
        doThrow(new RuntimeException("Audit DB down"))
                .when(messageErrorHandlingPort)
                .saveProcessingError(isNull(), anyString(), anyString(), anyString());

        assertDoesNotThrow(() -> listener.onProductEvent(null));
    }

    @Test
    @DisplayName("DELETED sin productId - no elimina")
    void deletedSinProductId_noElimina() {
        event.setType(EventType.DELETED);
        productSyncDto.setProductId(null);

        listener.onProductEvent(event);

        verify(productCommandPort, never()).delete(any());
        verify(messageErrorHandlingPort).saveProcessingError(
                eq("DELETED"),
                contains("Missing required field: productId"),
                anyString(),
                eq("Product"));
    }
}

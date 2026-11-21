package co.unicauca.edu.co.contables.inventory.products.unit.infraestructure.output.messageBroker;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyLong;
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

import co.unicauca.edu.co.contables.inventory.products.application.ports.input.IProductUsagePort;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.messageBroker.SpringEventsProductUsageListener;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.messageBroker.dto.ProductUsageEventDto;

@ExtendWith(MockitoExtension.class)
class SpringEventsProductUsageListenerTest {

    private static final Long PRODUCT_ID = 1L;
    private static final String ENTERPRISE_ID = "ENT-001";
    private static final Integer QUANTITY_USED = 10;

    @Mock
    private IProductUsagePort productUsagePort;

    @InjectMocks
    private SpringEventsProductUsageListener listener;

    private ProductUsageEventDto validEvent;

    @BeforeEach
    void setUp() {
        validEvent = new ProductUsageEventDto(PRODUCT_ID, ENTERPRISE_ID, QUANTITY_USED);
    }

    @Test
    @DisplayName("Evento válido - incrementa el uso del producto")
    void eventoValido_incrementaUso() {
        listener.onProductUsed(validEvent);

        verify(productUsagePort).incrementUsageCount(PRODUCT_ID);
    }

    @Test
    @DisplayName("Evento válido - incrementa exactamente una vez")
    void eventoValido_incrementaUnaVez() {
        listener.onProductUsed(validEvent);

        verify(productUsagePort, times(1)).incrementUsageCount(PRODUCT_ID);
    }

    @Test
    @DisplayName("Evento nulo - no procesa y no lanza")
    void eventoNulo_noProcesa() {
        assertDoesNotThrow(() -> listener.onProductUsed(null));

        verify(productUsagePort, never()).incrementUsageCount(anyLong());
    }

    @Test
    @DisplayName("productId nulo - no procesa")
    void productIdNulo_noProcesa() {
        validEvent.setProductId(null);

        listener.onProductUsed(validEvent);

        verify(productUsagePort, never()).incrementUsageCount(anyLong());
    }

    @Test
    @DisplayName("quantityUsed nulo - no procesa")
    void quantityUsedNulo_noProcesa() {
        validEvent.setQuantityUsed(null);

        listener.onProductUsed(validEvent);

        verify(productUsagePort, never()).incrementUsageCount(anyLong());
    }

    @Test
    @DisplayName("quantityUsed cero o negativo - no procesa")
    void quantityUsedNoPositivo_noProcesa() {
        validEvent.setQuantityUsed(0);
        listener.onProductUsed(validEvent);

        validEvent.setQuantityUsed(-1);
        listener.onProductUsed(validEvent);

        verify(productUsagePort, never()).incrementUsageCount(anyLong());
    }

    @Test
    @DisplayName("Fallo del caso de uso - se registra y no se propaga")
    void falloDelCasoDeUso_noPropaga() {
        doThrow(new RuntimeException("Error en servicio"))
                .when(productUsagePort).incrementUsageCount(PRODUCT_ID);

        assertDoesNotThrow(() -> listener.onProductUsed(validEvent));

        verify(productUsagePort).incrementUsageCount(PRODUCT_ID);
    }

    @Test
    @DisplayName("Eventos con distintos IDs - cada uno incrementa su propio contador")
    void eventosDistintos_incrementanCadaContador() {
        listener.onProductUsed(new ProductUsageEventDto(1L, ENTERPRISE_ID, 1));
        listener.onProductUsed(new ProductUsageEventDto(2L, ENTERPRISE_ID, 3));

        verify(productUsagePort).incrementUsageCount(1L);
        verify(productUsagePort).incrementUsageCount(2L);
    }

    @Test
    @DisplayName("quantityUsed positivo - procesa")
    void quantityUsedPositivo_procesa() {
        validEvent.setQuantityUsed(100);

        listener.onProductUsed(validEvent);

        verify(productUsagePort).incrementUsageCount(PRODUCT_ID);
    }
}

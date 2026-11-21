package co.unicauca.edu.co.contables.inventory.peps.unit.infrastucture.output.messajeBroker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker.SpringEventsProductEventPublisher;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.messageBroker.dto.ProductUsageEventDto;

@ExtendWith(MockitoExtension.class)
class SpringEventsProductEventPublisherTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private SpringEventsProductEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new SpringEventsProductEventPublisher(eventPublisher);
    }

    @Test
    @DisplayName("publishUsedProductEvent - publica el DTO de products con los datos recibidos")
    void publishUsedProductEvent_publicaDto() {
        publisher.publishUsedProductEvent(42L, 7);

        ArgumentCaptor<ProductUsageEventDto> captor = ArgumentCaptor.forClass(ProductUsageEventDto.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertEquals(42L, captor.getValue().getProductId());
        assertEquals(7, captor.getValue().getQuantityUsed());

        assertNull(captor.getValue().getEnterpriseId());
    }

    @Test
    @DisplayName("publishUsedProductEvent - publica exactamente una vez")
    void publishUsedProductEvent_publicaUnaVez() {
        publisher.publishUsedProductEvent(42L, 7);

        verify(eventPublisher, times(1)).publishEvent(org.mockito.ArgumentMatchers.any(Object.class));
    }
}

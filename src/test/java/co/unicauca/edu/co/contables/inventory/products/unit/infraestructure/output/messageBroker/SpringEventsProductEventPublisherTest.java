package co.unicauca.edu.co.contables.inventory.products.unit.infraestructure.output.messageBroker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
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

import co.unicauca.edu.co.contables.inventory.products.application.dto.ProductSyncDto;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.messageBroker.SpringEventsProductEventPublisher;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.messageBroker.dto.EventDto;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.messageBroker.dto.ProductSyncEvent;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.messageBroker.enums.EventType;

@ExtendWith(MockitoExtension.class)
class SpringEventsProductEventPublisherTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private SpringEventsProductEventPublisher publisher;

    private ProductSyncDto product;

    @BeforeEach
    void setUp() {
        publisher = new SpringEventsProductEventPublisher(eventPublisher);
        product = new ProductSyncDto(42L, "Test Product", "REF-001", "ENT-1",
                "Box", 10, 25.5, true);
    }

    @Test
    @DisplayName("CREATED - publica el DTO del productor en el bus en proceso")
    void created_publicaEvento() {
        publisher.publishCreatedStockEvent(product);

        EventDto<?, ?> event = capturePublishedEvent();
        assertEquals(EventType.CREATED, event.getType());
        assertSame(product, event.getData());
    }

    @Test
    @DisplayName("UPDATED - publica el DTO del productor en el bus en proceso")
    void updated_publicaEvento() {
        publisher.publishUpdatedStockEvent(product);

        EventDto<?, ?> event = capturePublishedEvent();
        assertEquals(EventType.UPDATED, event.getType());
        assertSame(product, event.getData());
    }

    @Test
    @DisplayName("DELETED - publica el DTO del productor en el bus en proceso")
    void deleted_publicaEvento() {
        publisher.publishDeletedStockEvent(product);

        EventDto<?, ?> event = capturePublishedEvent();
        assertEquals(EventType.DELETED, event.getType());
        assertSame(product, event.getData());
    }

    @Test
    @DisplayName("Cada publicación emite exactamente un evento, sin ruta Rabbit")
    void cadaPublicacion_emiteUnEvento() {
        publisher.publishCreatedStockEvent(product);
        publisher.publishUpdatedStockEvent(product);
        publisher.publishDeletedStockEvent(product);

        verify(eventPublisher, times(3)).publishEvent(org.mockito.ArgumentMatchers.any(Object.class));
    }

    private EventDto<?, ?> capturePublishedEvent() {
        ArgumentCaptor<EventDto> captor = ArgumentCaptor.forClass(EventDto.class);
        verify(eventPublisher).publishEvent(captor.capture());
        EventDto<?, ?> published = captor.getValue();

        assertInstanceOf(ProductSyncEvent.class, published);
        return published;
    }
}

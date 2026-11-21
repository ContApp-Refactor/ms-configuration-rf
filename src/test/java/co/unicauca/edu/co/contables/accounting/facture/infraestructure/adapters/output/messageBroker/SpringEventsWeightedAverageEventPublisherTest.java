package co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.EventDto;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.KardexPurchaseDtoRequest;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.KardexSalesDtoRequest;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.WeightedAveragePurchaseEvent;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto.WeightedAverageSalesEvent;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.enums.EventFactureType;

@ExtendWith(MockitoExtension.class)
class SpringEventsWeightedAverageEventPublisherTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private SpringEventsWeightedAverageEventPublisher publisher;

    private KardexPurchaseDtoRequest purchase;
    private KardexSalesDtoRequest sale;

    @BeforeEach
    void setUp() {
        publisher = new SpringEventsWeightedAverageEventPublisher(eventPublisher);
        purchase = new KardexPurchaseDtoRequest(50, 12345L, new BigDecimal("15.75"), 9001L,
                "Compra de prueba");
        sale = new KardexSalesDtoRequest(10, 12345L, 9001L, "Venta de prueba");
    }

    @Test
    @DisplayName("PURCHASE - publica el DTO de compra en el bus en proceso")
    void purchase_publicaEvento() {
        publisher.publishPurchaseWeightedAverageEvent(purchase);

        EventDto<?, ?> event = capturePublishedEvent(WeightedAveragePurchaseEvent.class);
        assertEquals(EventFactureType.PURCHASE, event.getType());
        assertSame(purchase, event.getData());
    }

    @Test
    @DisplayName("SALE - publica el DTO de venta en el bus en proceso")
    void sale_publicaEvento() {
        publisher.publishSaleWeightedAverageEvent(sale);

        EventDto<?, ?> event = capturePublishedEvent(WeightedAverageSalesEvent.class);
        assertEquals(EventFactureType.SALE, event.getType());
        assertSame(sale, event.getData());
    }

    @Test
    @DisplayName("RETURNONSALE - publica el DTO de venta en el bus en proceso")
    void returnOnSale_publicaEvento() {
        publisher.publishReturnOnSaleWeightedAverageEvent(sale);

        EventDto<?, ?> event = capturePublishedEvent(WeightedAverageSalesEvent.class);
        assertEquals(EventFactureType.RETURNONSALE, event.getType());
        assertSame(sale, event.getData());
    }

    @Test
    @DisplayName("RETURNONPURCHASE - publica el DTO de venta en el bus en proceso")
    void returnOnPurchase_publicaEvento() {
        publisher.publishReturnOnPurchaseWeightedAverageEvent(sale);

        EventDto<?, ?> event = capturePublishedEvent(WeightedAverageSalesEvent.class);
        assertEquals(EventFactureType.RETURNONPURCHASE, event.getType());
        assertSame(sale, event.getData());
    }

    @Test
    @DisplayName("NONCOMMERCIALENTRY - publica el DTO de compra en el bus en proceso")
    void nonCommercialEntry_publicaEvento() {
        publisher.publishNonCommercialEntryWeightedAverageEvent(purchase);

        EventDto<?, ?> event = capturePublishedEvent(WeightedAveragePurchaseEvent.class);
        assertEquals(EventFactureType.NONCOMMERCIALENTRY, event.getType());
        assertSame(purchase, event.getData());
    }

    @Test
    @DisplayName("NONCOMMERCIALEXIT - publica el DTO de venta en el bus en proceso")
    void nonCommercialExit_publicaEvento() {
        publisher.publishNonCommercialExitWeightedAverageEvent(sale);

        EventDto<?, ?> event = capturePublishedEvent(WeightedAverageSalesEvent.class);
        assertEquals(EventFactureType.NONCOMMERCIALEXIT, event.getType());
        assertSame(sale, event.getData());
    }

    @Test
    @DisplayName("Cada publicación emite exactamente un evento, sin ruta Rabbit")
    void cadaPublicacion_emiteUnEvento() {
        publisher.publishPurchaseWeightedAverageEvent(purchase);
        publisher.publishSaleWeightedAverageEvent(sale);
        publisher.publishReturnOnSaleWeightedAverageEvent(sale);
        publisher.publishReturnOnPurchaseWeightedAverageEvent(sale);
        publisher.publishNonCommercialEntryWeightedAverageEvent(purchase);
        publisher.publishNonCommercialExitWeightedAverageEvent(sale);

        verify(eventPublisher, times(6)).publishEvent(any(Object.class));
    }

    private EventDto<?, ?> capturePublishedEvent(Class<?> expectedType) {
        ArgumentCaptor<EventDto> captor = ArgumentCaptor.forClass(EventDto.class);
        verify(eventPublisher).publishEvent(captor.capture());
        EventDto<?, ?> published = captor.getValue();
        assertInstanceOf(expectedType, published);
        return published;
    }
}
